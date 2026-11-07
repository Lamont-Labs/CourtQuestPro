#!/usr/bin/env python3
"""Offline tests for ordered headless continuation."""
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from continue_refinement_campaign import advance, slot

BASE = "a" * 40
NEXT = "b" * 40

def fixtures():
    state = {"next_index": 0, "base_sha": BASE, "history": []}
    receipt = {"phase": "visual-identity", "pass": 1, "base_sha": BASE,
               "technical_checkpoint": True, "campaign_pass_accepted": False,
               "visual_quality_verified": False, "source_committed": True,
               "candidate_sha": NEXT}
    return state, receipt

class ContinuationTests(unittest.TestCase):
    def test_advance_one_verified_committed_pass(self):
        state, receipt = fixtures()
        next_state = advance(state, receipt)
        self.assertEqual(next_state["next_index"], 1)
        self.assertEqual(next_state["base_sha"], NEXT)
        self.assertFalse(next_state["history"][0]["campaign_pass_accepted"])

    def test_uncommitted_checkpoint_cannot_advance(self):
        state, receipt = fixtures()
        receipt["source_committed"] = False
        with self.assertRaisesRegex(ValueError, "source commit"):
            advance(state, receipt)

    def test_out_of_order_rejected(self):
        state, receipt = fixtures()
        receipt["pass"] = 2
        with self.assertRaisesRegex(ValueError, "Out-of-order"):
            advance(state, receipt)

    def test_forged_or_failed_receipts_rejected(self):
        for key, value in [
            ("technical_checkpoint", False),
            ("campaign_pass_accepted", True),
            ("visual_quality_verified", True),
            ("candidate_sha", BASE),
            ("base_sha", NEXT),
        ]:
            with self.subTest(key=key):
                state, receipt = fixtures()
                receipt[key] = value
                with self.assertRaises(ValueError):
                    advance(state, receipt)

    def test_phase_boundary(self):
        self.assertEqual(slot(99), {"phase": "visual-identity", "pass": 100})
        self.assertEqual(slot(100), {"phase": "layout-composition", "pass": 1})
        with self.assertRaises(ValueError):
            slot(800)

if __name__ == "__main__":
    unittest.main()
