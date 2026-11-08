#!/usr/bin/env python3
"""Offline regression tests for the bounded Ollama proposal generator."""
import hashlib
import json
import pathlib
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from generate_refinement_proposal import generate

TARGET = "app/src/main/java/com/courtquestpro/AppHeader.kt"
ORIGINAL = "package com.courtquestpro\n\ninternal val padding = 12\n"
CHANGED = "package com.courtquestpro\n\ninternal val padding = 16\n"

class Response:
    def __init__(self, source):
        self.payload = json.dumps({"message": {"content": source}}).encode()
    def __enter__(self):
        return self
    def __exit__(self, *args):
        return False
    def read(self, count):
        return self.payload[:count]

class ProposalRetryTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = pathlib.Path(self.tmp.name)
        source = self.root / TARGET
        source.parent.mkdir(parents=True)
        source.write_text(ORIGINAL)
        subprocess.run(["git", "init", "-q"], cwd=self.root, check=True)
        subprocess.run(["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                        "add", "."], cwd=self.root, check=True)
        subprocess.run(["git", "-c", "user.name=Test", "-c", "user.email=test@example.invalid",
                        "commit", "-qm", "fixture"], cwd=self.root, check=True)

    def run_generator(self):
        return generate(self.root, TARGET, "visual-identity", 1,
                        "Improve header padding without changing existing public APIs", "offline-test")

    def test_retries_whitespace_then_accepts_real_change(self):
        with patch("generate_refinement_proposal.urllib.request.urlopen",
                   side_effect=[Response(ORIGINAL + "\n"), Response(CHANGED)]) as call:
            proposal = self.run_generator()
        self.assertEqual(call.call_count, 2)
        self.assertEqual(proposal["changes"][0]["content"], CHANGED.strip())
        self.assertEqual(proposal["changes"][0]["before_sha256"],
                         hashlib.sha256(ORIGINAL.encode()).hexdigest())

    def test_three_unchanged_responses_fail_closed(self):
        with patch("generate_refinement_proposal.urllib.request.urlopen",
                   side_effect=[Response(ORIGINAL)] * 3) as call:
            with self.assertRaisesRegex(ValueError, "All three model attempts rejected"):
                self.run_generator()
        self.assertEqual(call.call_count, 3)

    def test_escaped_newline_source_retries(self):
        corrupted = CHANGED.replace("\n", "\\n")
        with patch("generate_refinement_proposal.urllib.request.urlopen",
                   side_effect=[Response(corrupted), Response(CHANGED)]) as call:
            result = self.run_generator()
        self.assertEqual(call.call_count, 2)
        self.assertEqual(result["changes"][0]["content"], CHANGED.strip())

    def test_invalid_response_retries(self):
        with patch("generate_refinement_proposal.urllib.request.urlopen",
                   side_effect=[Response("invalid"), Response(CHANGED)]) as call:
            self.assertEqual(self.run_generator()["changes"][0]["content"], CHANGED.strip())
        self.assertEqual(call.call_count, 2)

if __name__ == "__main__":
    unittest.main()
