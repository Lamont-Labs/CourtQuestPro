"""Safety checks for proposed AI-driven Android visual edits."""
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
import validate_refinement_proposal as validator

def proposal(path="app/src/main/java/com/courtquestpro/Theme.kt"):
    return {"phase": "visual-identity", "pass": 1, "base_sha": "a"*40,
            "intent": "Improve hierarchy and contrast for the player dashboard",
            "changes": [{"path": path, "before_sha256": "b"*64,
                         "content": "package com.courtquestpro\n// Visual improvement\n"}]}

class ProposalTests(unittest.TestCase):
    def test_valid_proposal_does_not_apply(self):
        result = validator.validate(proposal())
        self.assertTrue(result["validated"])
        self.assertFalse(result["applied"])
        self.assertFalse(result["accepted"])

    def test_reject_path_traversal(self):
        for path in ("../app/src/main/java/Foo.kt",
                     "app/src/main/java/../../signing/debug.keystore",
                     "/app/src/main/java/Foo.kt",
                     "app/src/main/java/Foo.kt/../Bar.kt"):
            with self.subTest(path=path), self.assertRaises(ValueError):
                validator.validate(proposal(path))

    def test_reject_sensitive_paths(self):
        for path in (".github/workflows/release.yml", "signing/debug.keystore",
                     "app/build.gradle.kts", "app/src/main/res/secrets/keys.xml"):
            with self.subTest(path=path), self.assertRaises(ValueError):
                validator.validate(proposal(path))

    def test_reject_duplicate(self):
        p = proposal()
        p["changes"].append(dict(p["changes"][0]))
        with self.assertRaises(ValueError):
            validator.validate(p)

    def test_reject_oversized(self):
        p = proposal()
        p["changes"][0]["content"] = "x" * 180001
        with self.assertRaises(ValueError):
            validator.validate(p)

    def test_reject_unexpected_keys(self):
        p = proposal()
        p["auto_merge"] = True
        with self.assertRaises(ValueError):
            validator.validate(p)

    def test_reject_invalid_phase(self):
        p = proposal()
        p["phase"] = "unbounded-execution"
        with self.assertRaises(ValueError):
            validator.validate(p)

if __name__ == "__main__":
    unittest.main()
