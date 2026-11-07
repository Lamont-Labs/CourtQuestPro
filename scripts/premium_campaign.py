#!/usr/bin/env python3
"""CourtQuest 800-pass campaign ledger/controller. No provider calls or Git writes."""
import argparse
import datetime as dt
import hashlib
import json
import pathlib
import re
import sys

PHASES = ("visual-identity", "layout-composition", "typography", "glass-depth",
          "motion-interactions", "consistency", "usability", "final-refinement")
TOTAL = 800
SHA = re.compile(r"^[0-9a-f]{40}$")
DIGEST = re.compile(r"^[0-9a-f]{64}$")

def slots():
    return [{"phase": phase, "pass": n} for phase in PHASES for n in range(1, 101)]

def key(item):
    return (item["phase"], item["pass"])

def validate(receipt):
    required = {"phase", "pass", "base_sha", "candidate_sha", "build_passed",
                "tests_passed", "screenshots_sha256", "comparison_sha256",
                "regression_reviewed", "accepted", "reviewer", "reviewed_at"}
    if not required.issubset(receipt):
        raise ValueError("Missing receipt fields: " + ", ".join(sorted(required - receipt.keys())))
    if receipt["phase"] not in PHASES or type(receipt["pass"]) is not int or not 1 <= receipt["pass"] <= 100:
        raise ValueError("Invalid phase/pass")
    if not SHA.fullmatch(receipt["base_sha"]) or not SHA.fullmatch(receipt["candidate_sha"]):
        raise ValueError("Invalid commit SHA")
    if receipt["base_sha"] == receipt["candidate_sha"]:
        raise ValueError("A pass requires a distinct source commit")
    for field in ("build_passed", "tests_passed", "regression_reviewed", "accepted"):
        if receipt[field] is not True:
            raise ValueError(f"{field} must be true")
    for field in ("screenshots_sha256", "comparison_sha256"):
        if not isinstance(receipt[field], str) or not DIGEST.fullmatch(receipt[field]):
            raise ValueError(f"Invalid {field}")
    if not isinstance(receipt["reviewer"], str) or not receipt["reviewer"].strip():
        raise ValueError("Reviewer identity required")
    try:
        stamp = dt.datetime.fromisoformat(receipt["reviewed_at"].replace("Z", "+00:00"))
        if stamp.tzinfo is None:
            raise ValueError()
    except (TypeError, ValueError):
        raise ValueError("Timezone-aware reviewed_at required")
    return receipt

def load(path):
    if not path.exists():
        return []
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, list):
        raise ValueError("Ledger must be a JSON array")
    seen = set()
    for item in data:
        validate(item)
        if key(item) in seen:
            raise ValueError("Duplicate accepted slot")
        seen.add(key(item))
    return data

def save(path, records):
    path.parent.mkdir(parents=True, exist_ok=True)
    temp = path.with_suffix(path.suffix + ".tmp")
    temp.write_text(json.dumps(records, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    temp.replace(path)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--ledger", type=pathlib.Path, default=pathlib.Path("campaign/accepted.json"))
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("status")
    sub.add_parser("next")
    accept = sub.add_parser("accept")
    accept.add_argument("receipt", type=pathlib.Path)
    args = parser.parse_args()
    records = load(args.ledger)
    completed = {key(r) for r in records}
    if args.command == "accept":
        receipt = validate(json.loads(args.receipt.read_text(encoding="utf-8")))
        if key(receipt) in completed:
            raise ValueError("Slot already accepted; immutable ledger")
        expected = next((slot for slot in slots() if key(slot) not in completed), None)
        if expected is None or key(expected) != key(receipt):
            raise ValueError(f"Out-of-order receipt; expected {expected}")
        if records and receipt["base_sha"] != records[-1]["candidate_sha"]:
            raise ValueError("Candidate chain does not continue prior accepted commit")
        records.append(receipt)
        save(args.ledger, records)
        completed.add(key(receipt))
    next_slot = next((slot for slot in slots() if key(slot) not in completed), None)
    output = {"accepted": len(completed), "target": TOTAL, "remaining": TOTAL-len(completed),
              "next": next_slot, "complete": len(completed) == TOTAL}
    print(json.dumps(output, sort_keys=True))

if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, json.JSONDecodeError) as exc:
        print(f"Campaign validation failed: {exc}", file=sys.stderr)
        sys.exit(1)
