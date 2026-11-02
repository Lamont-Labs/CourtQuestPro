#!/usr/bin/env python3
"""Validate the built Android test APK and prepare release evidence."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
from zipfile import ZipFile


ROOT = Path(__file__).resolve().parents[1]
EXPECTED_CERTIFICATE = "25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise RuntimeError(message)


def run(*args: str) -> str:
    result = subprocess.run(args, text=True, capture_output=True, check=True)
    return result.stdout + result.stderr


def build_setting(source: str, name: str, pattern: str) -> str:
    match = re.search(r"\b" + re.escape(name) + r"\s*=\s*" + pattern, source)
    require(match is not None, f"Missing {name} in app/build.gradle.kts")
    return match.group(1)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apk", required=True, type=Path)
    parser.add_argument("--build-tools", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()

    source = (ROOT / "app/build.gradle.kts").read_text()
    package = build_setting(source, "applicationId", r'"([^"]+)"')
    version = build_setting(source, "versionName", r'"([^"]+)"')
    code = int(build_setting(source, "versionCode", r"(\d+)"))
    min_sdk = int(build_setting(source, "minSdk", r"(\d+)"))
    target_sdk = int(build_setting(source, "targetSdk", r"(\d+)"))

    require(args.apk.is_file(), "APK does not exist")
    with ZipFile(args.apk) as archive:
        require(archive.testzip() is None, "APK ZIP integrity failed")
        names = archive.namelist()
        require("AndroidManifest.xml" in names, "APK manifest is missing")
        dex = [name for name in names if re.fullmatch(r"classes\d*\.dex", name)]
        require(bool(dex), "APK DEX bytecode is missing")
        require(
            any(b"Lcom/courtquestpro/MainActivity;" in archive.read(name) for name in dex),
            "MainActivity is missing from packaged DEX bytecode",
        )

    signatures = run(str(args.build_tools / "apksigner"), "verify", "--verbose", "--print-certs", str(args.apk))
    require(
        "Verified using v2 scheme (APK Signature Scheme v2): true" in signatures,
        "APK v2 signature verification failed",
    )
    certificate = re.search(r"Signer #1 certificate SHA-256 digest:\s*([0-9a-fA-F]+)", signatures)
    require(certificate is not None, "APK signing certificate is missing")
    require(certificate.group(1).lower() == EXPECTED_CERTIFICATE, "APK signing identity changed")

    alignment = run(str(args.build_tools / "zipalign"), "-c", "-P", "16", "-v", "4", str(args.apk))
    badging = run(str(args.build_tools / "aapt"), "dump", "badging", str(args.apk))
    manifest_package = re.search(r"^package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", badging, re.M)
    require(manifest_package is not None, "APK package metadata is missing")
    require(
        manifest_package.groups() == (package, str(code), version),
        "APK package or version does not match source",
    )
    require(f"sdkVersion:'{min_sdk}'" in badging, "APK minimum SDK does not match source")
    require(f"targetSdkVersion:'{target_sdk}'" in badging, "APK target SDK does not match source")
    require(
        re.search(r"^launchable-activity: name='com\.courtquestpro\.MainActivity'", badging, re.M) is not None,
        "APK launcher activity is missing",
    )

    output = args.output
    output.mkdir(parents=True, exist_ok=True)
    apk_name = f"CourtQuestPro-{version}.apk"
    destination = output / apk_name
    shutil.copy2(args.apk, destination)
    digest = hashlib.sha256(destination.read_bytes()).hexdigest()
    report = {
        "application_name": "Court Quest Pro",
        "package_id": package,
        "version_name": version,
        "version_code": code,
        "build_type": "debug",
        "minimum_sdk": min_sdk,
        "target_sdk": target_sdk,
        "apk_filename": apk_name,
        "apk_size_bytes": destination.stat().st_size,
        "apk_sha256": digest,
        "certificate_sha256": EXPECTED_CERTIFICATE,
        "git_commit": os.environ.get("GITHUB_SHA"),
        "github_run_id": os.environ.get("GITHUB_RUN_ID"),
        "github_run_number": os.environ.get("GITHUB_RUN_NUMBER"),
        "checks": {
            "zip_integrity": "PASS",
            "apk_v2_signature": "PASS",
            "signing_continuity": "PASS",
            "zip_alignment_16kb": "PASS",
            "manifest_and_launcher": "PASS",
            "main_activity_in_dex": "PASS",
            "physical_device_test": "NOT_PERFORMED",
            "emulator_test": "NOT_PERFORMED",
        },
    }
    (output / "apk-manifest.json").write_text(json.dumps(report, indent=2) + "\n")
    (output / "SHA256SUMS.txt").write_text(f"{digest}  {apk_name}\n")
    (output / "signature-verification.txt").write_text(signatures)
    (output / "zip-alignment.txt").write_text(alignment)
    (output / "apk-badging.txt").write_text(badging)
    notes = (
        f"Download **{apk_name}** under **Assets** and open it on an Android 8.0 or newer phone.\n\n"
        "This is a signed development APK for direct installation and testing. "
        "The signing identity matches the original November 1 test build.\n\n"
        "Build checks: compilation, Android lint, APK v2 signature, signing continuity, "
        "ZIP integrity, 16 KB ZIP alignment, package metadata, launcher, and packaged MainActivity.\n\n"
        "**Phone and emulator testing have not been performed.** "
        "The original prototype limitations are documented in the repository README.\n\n"
        f"Version: {version} ({code})  \n"
        f"Commit: {os.environ.get('GITHUB_SHA', 'local build')}  \n"
        f"SHA-256: \x60{digest}\x60\n"
    )
    (output / "RELEASE_NOTES.md").write_text(notes)
    if github_output := os.environ.get("GITHUB_OUTPUT"):
        with open(github_output, "a") as stream:
            stream.write(f"version={version}\napk={apk_name}\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
