#!/usr/bin/env python3
"""Create a detached, local-only commit from a verified refinement patch.

Never pushes, updates main, accepts a visual pass, or writes to the source checkout.
"""
import argparse
import json
import pathlib
import subprocess
import sys
import tempfile

from prepare_refinement_commit import prepare, command

def isolate(repo, proposal, verification, diff):
    repo = repo.resolve(strict=True)
    receipt = prepare(repo, proposal, verification, diff)
    with tempfile.TemporaryDirectory(prefix="courtquest-isolated-commit-") as temp:
        checkout = pathlib.Path(temp) / "checkout"
        command(["git", "worktree", "add", "--detach", str(checkout), receipt["base_sha"]], repo)
        try:
            command(["git", "apply", "--binary", "-"], checkout, diff)
            changed = command(["git", "diff", "--name-only"], checkout).stdout.decode().splitlines()
            if sorted(changed) != sorted(receipt["changed_paths"]):
                raise ValueError("Applied patch changed unexpected paths")
            command(["git", "add", "--", *changed], checkout)
            command(["git", "-c", "user.name=CourtQuest CI",
                     "-c", "user.email=courtquest-ci@users.noreply.github.com",
                     "commit", "-m", f"refinement: {receipt['phase']} pass {receipt['pass']}"], checkout)
            sha = command(["git", "rev-parse", "HEAD"], checkout).stdout.decode().strip()
            parent = command(["git", "rev-parse", "HEAD^"], checkout).stdout.decode().strip()
            if parent != receipt["base_sha"] or sha == parent:
                raise ValueError("Isolated commit parent mismatch")
            return {**receipt, "candidate_sha": sha, "source_committed": True,
                    "commit_location": "local-detached-worktree",
                    "pushed": False, "campaign_pass_accepted": False}
        finally:
            command(["git", "worktree", "remove", "--force", str(checkout)], repo)

def main():
    p = argparse.ArgumentParser()
    p.add_argument("proposal", type=pathlib.Path)
    p.add_argument("verification", type=pathlib.Path)
    p.add_argument("diff", type=pathlib.Path)
    p.add_argument("--repo", type=pathlib.Path, default=pathlib.Path("."))
    p.add_argument("--output", type=pathlib.Path, required=True)
    args = p.parse_args()
    result = isolate(args.repo, json.loads(args.proposal.read_text()),
                     json.loads(args.verification.read_text()), args.diff.read_bytes())
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n")
    print(json.dumps({"candidate_sha": result["candidate_sha"], "pushed": False,
                      "campaign_pass_accepted": False}))

if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, TypeError, subprocess.CalledProcessError, json.JSONDecodeError) as exc:
        print(f"Isolated commit rejected: {exc}", file=sys.stderr)
        sys.exit(1)
