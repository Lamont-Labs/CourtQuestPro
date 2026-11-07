#!/usr/bin/env python3
"""Fail-closed technical checkpoint for one headless candidate (not visual acceptance)."""
import argparse
import hashlib
import json
import pathlib
import sys

def checkpoint(proposal, verification, diff):
    if proposal["base_sha"] != verification["base_sha"]:
        raise ValueError("Proposal and verification base SHA mismatch")
    if (proposal["phase"], proposal["pass"]) != (verification["phase"], verification["pass"]):
        raise ValueError("Proposal and verification campaign slot mismatch")
    if verification.get("accepted") is not False:
        raise ValueError("Verification must not claim campaign acceptance")
    for field in ("build_passed", "tests_passed", "lint_passed"):
        if verification.get(field) is not True:
            raise ValueError(f"Missing successful verification: {field}")
    if verification.get("verification_skipped"):
        raise ValueError("Skipped verification cannot be checkpointed")
    if not verification.get("changes"):
        raise ValueError("Missing candidate changed paths")
    proposed = [change["path"] for change in proposal["changes"]]
    if proposed != verification["changes"]:
        raise ValueError("Changed paths do not match proposal")
    if not diff:
        raise ValueError("Empty candidate diff")
    digest = hashlib.sha256(diff).hexdigest()
    if digest != verification.get("diff_sha256"):
        raise ValueError("Candidate diff digest mismatch")
    if not verification.get("candidate_apk_sha256"):
        raise ValueError("Missing verified APK digest")
    return {
        "schema": "courtquest-headless-checkpoint-v1",
        "phase": proposal["phase"], "pass": proposal["pass"],
        "base_sha": proposal["base_sha"],
        "changed_paths": proposed, "diff_sha256": digest,
        "candidate_apk_sha256": verification["candidate_apk_sha256"],
        "build_passed": True, "tests_passed": True, "lint_passed": True,
        "technical_checkpoint": True, "visual_quality_verified": False,
        "campaign_pass_accepted": False, "source_committed": False
    }

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("proposal", type=pathlib.Path)
    parser.add_argument("verification", type=pathlib.Path)
    parser.add_argument("diff", type=pathlib.Path)
    parser.add_argument("--output", type=pathlib.Path, required=True)
    args = parser.parse_args()
    result = checkpoint(json.loads(args.proposal.read_text()),
                        json.loads(args.verification.read_text()), args.diff.read_bytes())
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n")
    print(json.dumps(result, sort_keys=True))

if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError) as exc:
        print(f"Technical checkpoint rejected: {exc}", file=sys.stderr)
        sys.exit(1)
