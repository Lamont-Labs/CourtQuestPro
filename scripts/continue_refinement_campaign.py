#!/usr/bin/env python3
"""Bounded headless campaign continuation; no Git writes or automatic acceptance."""
import argparse
import json
import pathlib
import sys

from premium_campaign import PHASES

TOTAL = len(PHASES) * 100

def slot(index):
    if not isinstance(index, int) or not 0 <= index < TOTAL:
        raise ValueError("Invalid campaign index")
    return {"phase": PHASES[index // 100], "pass": index % 100 + 1}

def advance(state, receipt):
    if set(state) != {"next_index", "base_sha", "history"}:
        raise ValueError("Invalid continuation state")
    index = state["next_index"]
    if type(index) is not int or not 0 <= index <= TOTAL:
        raise ValueError("Invalid next index")
    if not isinstance(state["history"], list) or len(state["history"]) != index:
        raise ValueError("History length mismatch")
    if not isinstance(state["base_sha"], str) or len(state["base_sha"]) != 40:
        raise ValueError("Missing base commit")
    if index == TOTAL:
        raise ValueError("Campaign exhausted")
    expected = slot(index)
    if (receipt.get("phase"), receipt.get("pass")) != (expected["phase"], expected["pass"]):
        raise ValueError("Out-of-order checkpoint")
    if receipt.get("base_sha") != state["base_sha"]:
        raise ValueError("Checkpoint does not continue expected base")
    if receipt.get("technical_checkpoint") is not True:
        raise ValueError("Technical verification required")
    if receipt.get("campaign_pass_accepted") is not False or receipt.get("visual_quality_verified") is not False:
        raise ValueError("Technical continuation cannot claim visual acceptance")
    if receipt.get("source_committed") is not True:
        raise ValueError("A new source commit is required to advance")
    candidate = receipt.get("candidate_sha")
    if not isinstance(candidate, str) or len(candidate) != 40 or candidate == state["base_sha"]:
        raise ValueError("Missing distinct candidate commit")
    return {
        "next_index": index + 1,
        "base_sha": candidate,
        "history": state["history"] + [{
            "phase": expected["phase"], "pass": expected["pass"],
            "base_sha": state["base_sha"], "candidate_sha": candidate,
            "technical_checkpoint": True, "campaign_pass_accepted": False
        }]
    }

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("state", type=pathlib.Path)
    parser.add_argument("receipt", type=pathlib.Path)
    parser.add_argument("--output", type=pathlib.Path, required=True)
    args = parser.parse_args()
    state = json.loads(args.state.read_text())
    receipt = json.loads(args.receipt.read_text())
    result = advance(state, receipt)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n")
    print(json.dumps({"technical_passes": result["next_index"],
                      "visual_passes_accepted": 0,
                      "next": slot(result["next_index"]) if result["next_index"] < TOTAL else None}))

if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError) as exc:
        print(f"Continuation rejected: {exc}", file=sys.stderr)
        sys.exit(1)
