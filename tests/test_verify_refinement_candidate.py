"""Unit tests for disposable candidate verification guardrails."""
import json
import pathlib
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from verify_refinement_candidate import verify

class CandidateTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.repo = pathlib.Path(self.tmp.name) / "repo"
        self.repo.mkdir()
        subprocess.run(["git", "init", "-q", str(self.repo)], check=True)
        subprocess.run(["git", "config", "user.email", "tests@localhost"], cwd=self.repo, check=True)
        subprocess.run(["git", "config", "user.name", "Campaign Tests"], cwd=self.repo, check=True)
        self.rel = "app/src/main/java/com/courtquestpro/Theme.kt"
        self.file = self.repo / self.rel
        self.file.parent.mkdir(parents=True)
        self.file.write_text("package com.courtquestpro\n// original\n")
        subprocess.run(["git", "add", "."], cwd=self.repo, check=True)
        subprocess.run(["git", "commit", "-qm", "baseline"], cwd=self.repo, check=True)
        self.sha = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=self.repo, text=True).strip()
        import hashlib
        self.proposal = pathlib.Path(self.tmp.name) / "proposal.json"
        self.proposal.write_text(json.dumps({
            "phase": "visual-identity", "pass": 1, "base_sha": self.sha,
            "intent": "Improve the visual hierarchy and theme presentation",
            "changes": [{"path": self.rel,
                         "before_sha256": hashlib.sha256(self.file.read_bytes()).hexdigest(),
                         "content": "package com.courtquestpro\n// refined\n"}]}))
        self.out = pathlib.Path(self.tmp.name) / "evidence"

    def test_disposable_checkout_does_not_change_source(self):
        result = verify(self.repo, self.proposal, self.out, skip_build=True)
        self.assertFalse(result["accepted"])
        self.assertTrue(result["verification_skipped"])
        self.assertIn("original", self.file.read_text())
        self.assertTrue((self.out / "candidate.diff").is_file())
        self.assertEqual(subprocess.check_output(
            ["git", "worktree", "list", "--porcelain"], cwd=self.repo, text=True).count("worktree "), 1)

    def test_dirty_source_rejected(self):
        self.file.write_text("dirty")
        with self.assertRaises(ValueError):
            verify(self.repo, self.proposal, self.out, skip_build=True)

    def test_output_inside_repo_rejected(self):
        with self.assertRaises(ValueError):
            verify(self.repo, self.proposal, self.repo / "evidence", skip_build=True)

    def test_mismatched_revision_rejected(self):
        proposal = json.loads(self.proposal.read_text())
        proposal["base_sha"] = "0"*40
        self.proposal.write_text(json.dumps(proposal))
        with self.assertRaises(ValueError):
            verify(self.repo, self.proposal, self.out, skip_build=True)

if __name__ == "__main__":
    unittest.main()
