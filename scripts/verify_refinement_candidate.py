#!/usr/bin/env python3
"""Prepare and verify one refinement in a disposable Git worktree.

This runner never commits, pushes, merges, signs, or publishes anything.
It is an operator-invoked local runner, not an autonomous model integration.
"""
import argparse
import hashlib
import json
import pathlib
import shutil
import subprocess
import sys
import tempfile
import time

from apply_refinement_proposal import apply

def command(args, cwd, timeout=600):
    return subprocess.run(args, cwd=cwd, check=True, capture_output=True,
                          text=True, timeout=timeout)

def verify(repo, proposal_path, output_dir, skip_build=False):
    repo = repo.resolve(strict=True)
    output_dir = output_dir.resolve()
    proposal = json.loads(proposal_path.read_text(encoding="utf-8"))
    base = proposal["base_sha"]
    actual = command(["git", "rev-parse", "--verify", "HEAD"], repo).stdout.strip()
    if actual != base:
        raise ValueError("Repository HEAD does not match proposal base SHA")
    if command(["git", "status", "--porcelain", "--untracked-files=no"], repo).stdout.strip():
        raise ValueError("Source repository must be clean")
    if output_dir == repo or repo in output_dir.parents:
        raise ValueError("Evidence output must be outside source repository")
    output_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="courtquest-refinement-") as tmp:
        checkout = pathlib.Path(tmp) / "checkout"
        # Git worktree shares object storage, but not working files.
        command(["git", "worktree", "add", "--detach", str(checkout), base], repo)
        report = {"base_sha": base, "phase": proposal["phase"], "pass": proposal["pass"],
                  "accepted": False, "build_passed": False, "tests_passed": False}
        try:
            result = apply(checkout, proposal, base)
            report["changes"] = result["changed_paths"]
            report["applied_to_disposable_checkout"] = True
            diff = command(["git", "diff", "--binary", "--", *result["changed_paths"]], checkout).stdout
            if not diff.strip():
                raise ValueError("No source diff produced")
            diff_bytes = diff.encode("utf-8")
            report["diff_sha256"] = hashlib.sha256(diff_bytes).hexdigest()
            (output_dir / "candidate.diff").write_bytes(diff_bytes)
            if not skip_build:
                gradle = checkout / "gradlew"
                gradle.chmod(gradle.stat().st_mode | 0o100)
                for task, label in [(":app:assembleDebug", "build_passed"),
                                    (":app:testDebugUnitTest", "tests_passed"),
                                    (":app:lintDebug", "lint_passed")]:
                    started = time.monotonic()
                    run = subprocess.run([str(gradle), "--no-daemon", "--console=plain", task],
                                         cwd=checkout, capture_output=True, text=True, timeout=1200)
                    (output_dir / (task.split(":")[-1] + ".log")).write_text(
                        run.stdout[-100000:] + "\n" + run.stderr[-50000:], encoding="utf-8")
                    report[label] = run.returncode == 0
                    report[label + "_seconds"] = round(time.monotonic() - started, 1)
                    if run.returncode:
                        raise RuntimeError(f"{task} failed with exit code {run.returncode}")
            else:
                report["verification_skipped"] = True
            return report
        finally:
            command(["git", "worktree", "remove", "--force", str(checkout)], repo)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("proposal", type=pathlib.Path)
    parser.add_argument("--repo", type=pathlib.Path, default=pathlib.Path("."))
    parser.add_argument("--output", type=pathlib.Path, required=True)
    parser.add_argument("--skip-build", action="store_true", help="Unit testing only; never counts as verified")
    args = parser.parse_args()
    output = args.output.resolve()
    try:
        report = verify(args.repo, args.proposal, output, args.skip_build)
        output.mkdir(parents=True, exist_ok=True)
        (output / "verification.json").write_text(json.dumps(report, indent=2) + "\n")
        print(json.dumps(report, sort_keys=True))
    except Exception as exc:
        output.mkdir(parents=True, exist_ok=True)
        (output / "failure.json").write_text(json.dumps(
            {"accepted": False, "error": f"{type(exc).__name__}: {exc}"}, indent=2) + "\n")
        print(f"Candidate verification failed: {exc}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
