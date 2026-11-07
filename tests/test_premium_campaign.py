"""Offline acceptance tests for the premium campaign controller."""
import datetime
import json
import pathlib
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
import premium_campaign as campaign

def receipt(phase="visual-identity", number=1, base="a"*40, candidate="b"*40):
    return {"phase": phase, "pass": number, "base_sha": base,
            "candidate_sha": candidate, "build_passed": True, "tests_passed": True,
            "screenshots_sha256": "c"*64, "comparison_sha256": "d"*64,
            "regression_reviewed": True, "accepted": True,
            "reviewer": "human-reviewer", "reviewed_at": "2026-11-07T00:00:00Z"}

class CampaignTests(unittest.TestCase):
    def test_exactly_800_unique_slots(self):
        values = campaign.slots()
        self.assertEqual(len(values), 800)
        self.assertEqual(len({campaign.key(x) for x in values}), 800)
        self.assertEqual(values[-1], {"phase": "final-refinement", "pass": 100})

    def test_reject_unchanged_source(self):
        with self.assertRaises(ValueError):
            campaign.validate(receipt(candidate="a"*40))

    def test_reject_unverified(self):
        for field in ("build_passed", "tests_passed", "regression_reviewed", "accepted"):
            r = receipt()
            r[field] = False
            with self.assertRaises(ValueError):
                campaign.validate(r)

    def test_ledger_duplicate_detection(self):
        with tempfile.TemporaryDirectory() as directory:
            path = pathlib.Path(directory) / "ledger.json"
            path.write_text(json.dumps([receipt(), receipt()]))
            with self.assertRaises(ValueError):
                campaign.load(path)

    def test_accept_then_reject_duplicate_and_out_of_order(self):
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory)
            ledger = root / "ledger.json"
            source = root / "receipt.json"
            source.write_text(json.dumps(receipt()))
            cmd = [sys.executable, str(pathlib.Path(campaign.__file__)), "--ledger", str(ledger), "accept", str(source)]
            first = subprocess.run(cmd, capture_output=True, text=True)
            self.assertEqual(first.returncode, 0, first.stderr)
            self.assertEqual(json.loads(first.stdout)["accepted"], 1)
            duplicate = subprocess.run(cmd, capture_output=True, text=True)
            self.assertNotEqual(duplicate.returncode, 0)
            source.write_text(json.dumps(receipt(number=3, base="b"*40, candidate="e"*40)))
            skipped = subprocess.run(cmd, capture_output=True, text=True)
            self.assertNotEqual(skipped.returncode, 0)

if __name__ == "__main__":
    unittest.main()
