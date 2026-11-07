#!/usr/bin/env python3
"""Offline tests for detached verified commits and unchanged main checkout."""
import hashlib
import pathlib
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from commit_verified_refinement import isolate

PATH = "app/src/main/java/com/courtquestpro/AppHeader.kt"

class IsolatedCommitTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repo = pathlib.Path(self.temp.name)
        file = self.repo / PATH
        file.parent.mkdir(parents=True)
        file.write_text("package com.courtquestpro\nval padding = 12\n")
        subprocess.run(["git", "init", "-q"], cwd=self.repo, check=True)
        subprocess.run(["git", "add", "."], cwd=self.repo, check=True)
        subprocess.run(["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                        "commit", "-qm", "baseline"], cwd=self.repo, check=True)
        self.base = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=self.repo, text=True).strip()
        file.write_text("package com.courtquestpro\nval padding = 16\n")
        self.diff = subprocess.check_output(["git", "diff", "--binary", "--", PATH], cwd=self.repo)
        subprocess.run(["git", "checkout", "--", PATH], cwd=self.repo, check=True)
        self.proposal = {"base_sha": self.base, "phase": "visual-identity", "pass": 1,
                         "changes": [{"path": PATH}]}
        self.verification = {"base_sha": self.base, "phase": "visual-identity", "pass": 1,
                             "accepted": False, "build_passed": True, "tests_passed": True,
                             "lint_passed": True, "changes": [PATH],
                             "diff_sha256": hashlib.sha256(self.diff).hexdigest(),
                             "candidate_apk_sha256": "b" * 64}

    def test_detached_commit_preserves_main_checkout(self):
        original = (self.repo / PATH).read_bytes()
        receipt = isolate(self.repo, self.proposal, self.verification, self.diff)
        self.assertTrue(receipt["source_committed"])
        self.assertFalse(receipt["pushed"])
        self.assertFalse(receipt["campaign_pass_accepted"])
        self.assertNotEqual(receipt["candidate_sha"], self.base)
        self.assertEqual((self.repo / PATH).read_bytes(), original)
        head = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=self.repo, text=True).strip()
        self.assertEqual(head, self.base)

    def test_bundle_survives_worktree_cleanup_and_restores(self):
        bundle = self.repo / "candidate.bundle"
        receipt = isolate(self.repo, self.proposal, self.verification, self.diff, bundle)
        self.assertTrue(bundle.is_file())
        self.assertGreater(bundle.stat().st_size, 0)
        self.assertEqual(receipt["preservation"]["candidate_sha"], receipt["candidate_sha"])
        with tempfile.TemporaryDirectory() as directory:
            restored = pathlib.Path(directory) / "restored"
            subprocess.run(["git", "init", "-q", str(restored)], check=True)
            subprocess.run(["git", "fetch", "-q", str(bundle),
                            "refs/courtquest/checkpoints/" + receipt["candidate_sha"] +
                            ":refs/heads/restored-candidate"], cwd=restored, check=True)
            # Bundle stores a named checkpoint ref, not a default branch.
            refs = subprocess.check_output(
                ["git", "show-ref"], cwd=restored, text=True)
            self.assertIn(receipt["candidate_sha"], refs)
        self.assertEqual(subprocess.check_output(
            ["git", "rev-parse", "HEAD"], cwd=self.repo, text=True).strip(), self.base)

    def test_unverified_candidate_cannot_commit(self):
        self.verification["tests_passed"] = False
        with self.assertRaisesRegex(ValueError, "tests_passed"):
            isolate(self.repo, self.proposal, self.verification, self.diff)
        self.assertEqual(subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=self.repo, text=True).strip(), self.base)

if __name__ == "__main__":
    unittest.main()
