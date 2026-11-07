#!/usr/bin/env python3
"""Export a verified detached candidate as a portable Git bundle.

Requires a verified receipt and an existing candidate commit.
Does not push or update any branch.
"""
import argparse
import json
import pathlib
import re
import subprocess
import sys

SHA = re.compile(r"^[0-9a-f]{40}$")

def run(args, cwd):
    return subprocess.run(args, cwd=cwd, check=True, capture_output=True, text=True).stdout.strip()

def preserve(repo, receipt, output):
    repo = repo.resolve(strict=True)
    base = receipt.get("base_sha")
    candidate = receipt.get("candidate_sha")
    if not all(isinstance(s, str) and SHA.fullmatch(s) for s in (base, candidate)):
        raise ValueError("Valid base and candidate SHAs required")
    if receipt.get("technical_checkpoint") is not True or receipt.get("source_committed") is not True:
        raise ValueError("Verified committed candidate required")
    if receipt.get("campaign_pass_accepted") is not False or receipt.get("pushed") is not False:
        raise ValueError("Candidate must remain unaccepted and unpushed")
    if base == candidate or run(["git", "rev-parse", candidate + "^"], repo) != base:
        raise ValueError("Candidate must be a direct child of verified base")
    if output.exists():
        raise ValueError("Refusing to overwrite existing bundle")
    output.parent.mkdir(parents=True, exist_ok=True)
    ref = "refs/courtquest/checkpoints/" + candidate
    try:
        run(["git", "update-ref", ref, candidate], repo)
        run(["git", "bundle", "create", str(output), ref], repo)
        run(["git", "bundle", "verify", str(output)], repo)
        if not output.is_file() or output.stat().st_size == 0:
            raise ValueError("Empty checkpoint bundle")
    finally:
        run(["git", "update-ref", "-d", ref], repo)
    return {"bundle": str(output), "base_sha": base, "candidate_sha": candidate,
            "durable_when_artifact_uploaded": True, "pushed": False,
            "campaign_pass_accepted": False}

def main():
    p = argparse.ArgumentParser()
    p.add_argument("receipt", type=pathlib.Path)
    p.add_argument("--repo", type=pathlib.Path, default=pathlib.Path("."))
    p.add_argument("--output", type=pathlib.Path, required=True)
    args = p.parse_args()
    try:
        print(json.dumps(preserve(args.repo, json.loads(args.receipt.read_text()), args.output), sort_keys=True))
    except (OSError, ValueError, subprocess.CalledProcessError, json.JSONDecodeError) as exc:
        print(f"Checkpoint preservation rejected: {exc}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
