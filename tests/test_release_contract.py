"""Static, dependency-free regression guards for the Android release contract.

Run: python3 -m unittest discover -s tests -p 'test_*.py'
These checks are not substitutes for a signed APK build or device installation.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = (ROOT / ".github/workflows/android-apk.yml").read_text()
GRADLE = (ROOT / "app/build.gradle.kts").read_text()
VERIFIER = (ROOT / "scripts/verify_apk.py").read_text()


class ReleaseContractTests(unittest.TestCase):
    def test_release_is_manual_only(self):
        self.assertRegex(WORKFLOW, r"(?m)^on:\s*\n\s+workflow_dispatch:\s*$")
        self.assertNotRegex(WORKFLOW, r"(?m)^\s{2}(push|pull_request|schedule):")

    def test_release_cannot_overwrite_existing_assets(self):
        self.assertIn('gh api -i "repos/${GITHUB_REPOSITORY}/releases/tags/${tag}"', WORKFLOW)
        self.assertIn('Cannot confirm release tag availability; refusing publication.', WORKFLOW)
        self.assertIn('*" 404 "*)', WORKFLOW)
        self.assertIn('*" 200 "*)', WORKFLOW)
        self.assertIn("Release tag already exists; refusing to replace APK assets.", WORKFLOW)
        self.assertNotIn("--clobber", WORKFLOW)
        self.assertNotIn("gh release edit", WORKFLOW)

    def test_only_verified_release_assets_are_published(self):
        self.assertIn('test -s "$apk"', WORKFLOW)
        self.assertIn('sha256sum -c SHA256SUMS.txt', WORKFLOW)
        self.assertIn('gh release create "$tag" "$apk" dist/SHA256SUMS.txt', WORKFLOW)
        self.assertNotIn('gh release create "$tag" dist/*', WORKFLOW)

    def test_retained_artifacts_exclude_raw_build_logs(self):
        start = WORKFLOW.index("- name: Retain APK and verification evidence")
        end = WORKFLOW.index("- name: Publish APK to GitHub Releases", start)
        step = WORKFLOW[start:end]
        self.assertIn("dist/apk-manifest.json", step)
        self.assertIn("dist/SHA256SUMS.txt", step)
        self.assertNotIn("path: dist/", step)
        self.assertNotIn("dist/gradle-build.txt", step)

    def test_gradle_failures_propagate(self):
        # Restrict the assertion to this step; a later step cannot satisfy it.
        start = WORKFLOW.index("- name: Compile and run Android lint")
        end = WORKFLOW.index("- name: Verify installable APK", start)
        step = WORKFLOW[start:end]
        self.assertIn("set -euo pipefail", step)
        self.assertIn("./gradlew --no-daemon", step)
        self.assertIn("| tee dist/gradle-build.txt", step)

    def test_secrets_are_all_or_nothing(self):
        for secret in (
            "COURTQUEST_KEYSTORE_BASE64",
            "COURTQUEST_SIGNING_STORE_PASSWORD",
            "COURTQUEST_SIGNING_KEY_ALIAS",
            "COURTQUEST_SIGNING_KEY_PASSWORD",
        ):
            self.assertIn("secrets." + secret, WORKFLOW)
        self.assertIn("Incomplete signing secret set; refusing to build.", WORKFLOW)
        self.assertIn("Signing certificate mismatch; refusing to build.", WORKFLOW)
        self.assertIn("cmp -s", WORKFLOW)

    def test_temporary_keystore_is_cleaned(self):
        self.assertIn("Remove temporary signing material", WORKFLOW)
        self.assertIn("if: always()", WORKFLOW)
        self.assertIn('rm -f "${RUNNER_TEMP}/courtquest-preserved.keystore"', WORKFLOW)

    def test_package_and_certificate_are_pinned(self):
        self.assertIn('applicationId = "com.courtquestpro"', GRADLE)
        self.assertIn('EXPECTED_CERTIFICATE = "25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b"', VERIFIER)
        self.assertIn("APK signing identity changed", VERIFIER)
        self.assertIn("APK package or version does not match source", VERIFIER)

    def test_version_code_is_propagated(self):
        self.assertIn('environmentVariable("COURTQUEST_VERSION_CODE")', GRADLE)
        self.assertIn('COURTQUEST_VERSION_CODE=$version_code', WORKFLOW)
        self.assertIn('os.environ.get("COURTQUEST_VERSION_CODE", "1")', VERIFIER)
        self.assertIn("10000 + GITHUB_RUN_NUMBER", WORKFLOW)

    def test_release_requires_verification_first(self):
        verify = WORKFLOW.index("- name: Verify installable APK")
        publish = WORKFLOW.index("- name: Publish APK to GitHub Releases")
        self.assertLess(verify, publish)
        self.assertIn("python3 scripts/verify_apk.py", WORKFLOW)


if __name__ == "__main__":
    unittest.main()
