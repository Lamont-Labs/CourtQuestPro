#!/usr/bin/env python3
"""Offline tests for fail-closed technical checkpoints."""
import hashlib
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from checkpoint_refinement import checkpoint

DIFF = b"diff --git a/a b/a\n+new\n"
DIGEST = hashlib.sha256(DIFF).hexdigest()
BASE = "a" * 40

def fixture():
    proposal = {
        "phase": "visual-identity", "pass": 1, "base_sha": BASE,
        "changes": [{"path": "app/src/main/java/com/courtquestpro/AppHeader.kt"}]
    }
    verification = {
        "phase": "visual-identity", "pass": 1, "base_sha": BASE,
        "accepted": False, "build_passed": True, "tests_passed": True,
        "lint_passed": True, "verification_skipped": False,
        "changes": ["app/src/main/java/com/courtquestpro/AppHeader.kt"],
        "diff_sha256": DIGEST, "candidate_apk_sha256": "b" * 64
    }
    return proposal, verification

class CheckpointTests(unittest.TestCase):
    def test_valid_checkpoint_does_not_accept_campaign_pass(self):
        p, v = fixture()
        receipt = checkpoint(p, v, DIFF)
        self.assertTrue(receipt["technical_checkpoint"])
        self.assertFalse(receipt["campaign_pass_accepted"])
        self.assertFalse(receipt["visual_quality_verified"])
        self.assertFalse(receipt["source_committed"])

    def test_failed_gates_rejected(self):
        mutations = [
            ("build_passed", False), ("tests_passed", False),
            ("lint_passed", False), ("verification_skipped", True),
            ("accepted", True), ("diff_sha256", "0" * 64),
            ("candidate_apk_sha256", ""), ("changes", []),
            ("base_sha", "c" * 40), ("pass", 2)
        ]
        for field, value in mutations:
            with self.subTest(field=field):
                p, v = fixture()
                v[field] = value
                with self.assertRaises(ValueError):
                    checkpoint(p, v, DIFF)

    def test_modified_diff_rejected(self):
        p, v = fixture()
        with self.assertRaisesRegex(ValueError, "digest mismatch"):
            checkpoint(p, v, DIFF + b"tamper")

    def test_proposed_paths_must_match(self):
        p, v = fixture()
        p["changes"][0]["path"] = "app/src/main/java/com/courtquestpro/Other.kt"
        with self.assertRaisesRegex(ValueError, "Changed paths"):
            checkpoint(p, v, DIFF)

if __name__ == "__main__":
    unittest.main()
