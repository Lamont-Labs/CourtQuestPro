#!/usr/bin/env python3
"""Prepare a verified, non-pushing source checkpoint from a candidate diff.

Never commits, pushes, signs or accepts a campaign pass.
"""
import argparse
import hashlib
import json
import pathlib
import subprocess
import sys

from checkpoint_refinement import checkpoint

def command(args, cwd, input_bytes=None):
    return subprocess.run(args, cwd=cwd, input=input_bytes, stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE, check=True)

def prepare(repo, proposal, verification, diff):
    repo = repo.resolve(strict=True)
    receipt = checkpoint(proposal, verification, diff)
    base = command(["git", "rev-parse", "HEAD"], repo).stdout.decode().strip()
    if base != receipt["base_sha"]:
        raise ValueError("Repository HEAD differs from verified base")
    if command(["git", "status", "--porcelain", "--untracked-files=no"], repo).stdout.strip():
        raise ValueError("Tracked source has uncommitted changes")
    # git apply --check validates the patch without changing the source.
    command(["git", "apply", "--check", "--binary", "-"], repo, diff)
    receipt["ready_for_isolated_commit"] = True
    receipt["source_committed"] = False
    receipt["campaign_pass_accepted"] = False
    return receipt

def main():
    p = argparse.ArgumentParser()
    p.add_argument("proposal", type=pathlib.Path)
    p.add_argument("verification", type=pathlib.Path)
    p.add_argument("diff", type=pathlib.Path)
    p.add_argument("--repo", type=pathlib.Path, default=pathlib.Path("."))
    p.add_argument("--output", type=pathlib.Path, required=True)
    args = p.parse_args()
    result = prepare(args.repo, json.loads(args.proposal.read_text()),
                     json.loads(args.verification.read_text()), args.diff.read_bytes())
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n")
    print(json.dumps(result, sort_keys=True))

if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, TypeError, subprocess.CalledProcessError, json.JSONDecodeError) as exc:
        print(f"Source checkpoint preparation rejected: {exc}", file=sys.stderr)
        sys.exit(1)
