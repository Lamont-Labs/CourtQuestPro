"""Tests for digest-verified isolated UI edit application."""
import hashlib
import pathlib
import sys
import tempfile
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from apply_refinement_proposal import apply

SHA = "a"*40
REL = "app/src/main/java/com/courtquestpro/Theme.kt"

def proposal(before):
    return {"phase": "visual-identity", "pass": 1, "base_sha": SHA,
            "intent": "Improve the visual hierarchy of the player dashboard",
            "changes": [{"path": REL, "before_sha256": hashlib.sha256(before).hexdigest(),
                         "content": "package com.courtquestpro\n// updated theme\n"}]}

class ExecutorTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = pathlib.Path(self.tmp.name)
        self.path = self.root / REL
        self.path.parent.mkdir(parents=True)
        self.original = b"package com.courtquestpro\n// original theme\n"
        self.path.write_bytes(self.original)

    def test_dry_run_is_nonmutating(self):
        result = apply(self.root, proposal(self.original), SHA, True)
        self.assertFalse(result["applied"])
        self.assertEqual(self.path.read_bytes(), self.original)

    def test_apply_changes_only_target(self):
        untouched = self.path.parent / "Other.kt"
        untouched.write_text("unchanged")
        result = apply(self.root, proposal(self.original), SHA)
        self.assertTrue(result["applied"])
        self.assertFalse(result["accepted"])
        self.assertIn(b"updated theme", self.path.read_bytes())
        self.assertEqual(untouched.read_text(), "unchanged")

    def test_wrong_base_rejected(self):
        with self.assertRaises(ValueError):
            apply(self.root, proposal(self.original), "b"*40)
        self.assertEqual(self.path.read_bytes(), self.original)

    def test_stale_digest_rejected(self):
        with self.assertRaises(ValueError):
            apply(self.root, proposal(b"not the source"), SHA)
        self.assertEqual(self.path.read_bytes(), self.original)

    def test_symlink_rejected(self):
        external = self.root / "external.kt"
        external.write_text("protected")
        self.path.unlink()
        self.path.symlink_to(external)
        with self.assertRaises(ValueError):
            apply(self.root, proposal(b"protected"), SHA)
        self.assertEqual(external.read_text(), "protected")

    def test_preflight_prevents_partial_edit(self):
        p = proposal(self.original)
        p["changes"].append({"path": "app/src/main/java/com/courtquestpro/Missing.kt",
                             "before_sha256": "b"*64, "content": "new"})
        with self.assertRaises(ValueError):
            apply(self.root, p, SHA)
        self.assertEqual(self.path.read_bytes(), self.original)

if __name__ == "__main__":
    unittest.main()
