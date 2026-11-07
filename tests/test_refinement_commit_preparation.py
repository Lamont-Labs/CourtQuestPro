#!/usr/bin/env python3
"""Offline tests for verified patch preparation without mutating tracked source."""
import hashlib
import pathlib
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from prepare_refinement_commit import prepare

PATH = "app/src/main/java/com/courtquestpro/AppHeader.kt"

class PreparationTests(unittest.TestCase):
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

    def test_prepare_without_modifying_source(self):
        before = (self.repo / PATH).read_bytes()
        result = prepare(self.repo, self.proposal, self.verification, self.diff)
        self.assertTrue(result["ready_for_isolated_commit"])
        self.assertFalse(result["source_committed"])
        self.assertEqual((self.repo / PATH).read_bytes(), before)

    def test_dirty_repository_rejected(self):
        (self.repo / PATH).write_text("dirty")
        with self.assertRaisesRegex(ValueError, "uncommitted"):
            prepare(self.repo, self.proposal, self.verification, self.diff)

    def test_unapplicable_patch_rejected(self):
        invalid = self.diff.replace(b"padding = 12", b"padding = 99")
        self.verification["diff_sha256"] = hashlib.sha256(invalid).hexdigest()
        with self.assertRaises(subprocess.CalledProcessError):
            prepare(self.repo, self.proposal, self.verification, invalid)

    def test_failed_lint_rejected(self):
        self.verification["lint_passed"] = False
        with self.assertRaisesRegex(ValueError, "lint_passed"):
            prepare(self.repo, self.proposal, self.verification, self.diff)

if __name__ == "__main__":
    unittest.main()
