#!/usr/bin/env python3
"""Apply a validated UI proposal only to a disposable isolated worktree.

No Git commands, model access, credentials, or network operations are performed.
The caller must supply a dedicated clean directory containing a source snapshot.
"""
import argparse
import hashlib
import json
import os
import pathlib
import stat
import sys
import tempfile

from validate_refinement_proposal import validate

def digest(data):
    return hashlib.sha256(data).hexdigest()

def ensure_regular(root, relative):
    target = root / relative
    if not target.is_file() or target.is_symlink():
        raise ValueError(f"Target is missing, nonregular, or symlink: {relative}")
    current = root
    for part in pathlib.PurePosixPath(relative).parts:
        current = current / part
        if current.is_symlink():
            raise ValueError(f"Symlink component forbidden: {relative}")
    return target

def apply(root, proposal, expected_sha, dry_run=False):
    summary = validate(proposal)
    if proposal["base_sha"] != expected_sha:
        raise ValueError("Proposal base SHA does not match isolated checkout")
    root = root.resolve(strict=True)
    if not root.is_dir() or root.is_symlink():
        raise ValueError("Invalid isolated checkout")
    # All preconditions are verified before any file is modified.
    prepared = []
    for change in proposal["changes"]:
        target = ensure_regular(root, change["path"])
        existing = target.read_bytes()
        if digest(existing) != change["before_sha256"]:
            raise ValueError(f"Original digest mismatch: {change['path']}")
        replacement = change["content"].encode("utf-8")
        if existing == replacement:
            raise ValueError(f"No-op replacement: {change['path']}")
        prepared.append((target, replacement, stat.S_IMODE(target.stat().st_mode)))
    if dry_run:
        return {**summary, "dry_run": True, "applied": False}
    # Stage all replacement files before swapping any originals.
    staged = []
    backups = []
    try:
        for target, replacement, mode in prepared:
            fd, name = tempfile.mkstemp(prefix=".cq-proposal-", dir=target.parent)
            staged.append(pathlib.Path(name))
            with os.fdopen(fd, "wb") as out:
                out.write(replacement)
                out.flush()
                os.fsync(out.fileno())
            os.chmod(name, mode)
        for (target, _, _), stage in zip(prepared, staged):
            backup = target.read_bytes()
            backups.append((target, backup))
            os.replace(stage, target)
        return {**summary, "applied": True, "accepted": False,
                "changed_paths": [c["path"] for c in proposal["changes"]]}
    except Exception:
        # Restore previously replaced files on partial failure.
        for target, data in reversed(backups):
            target.write_bytes(data)
        raise
    finally:
        for stage in staged:
            stage.unlink(missing_ok=True)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("proposal", type=pathlib.Path)
    parser.add_argument("--checkout", type=pathlib.Path, required=True)
    parser.add_argument("--expected-sha", required=True)
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    proposal = json.loads(args.proposal.read_text(encoding="utf-8"))
    print(json.dumps(apply(args.checkout, proposal, args.expected_sha, args.dry_run), sort_keys=True))

if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, json.JSONDecodeError) as exc:
        print(f"Isolated proposal rejected: {exc}", file=sys.stderr)
        sys.exit(1)
