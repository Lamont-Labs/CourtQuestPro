#!/usr/bin/env python3
"""Validate proposed AI refinement edits before any source is changed.

This tool intentionally does not apply edits, call a model, or grant Git write access.
"""
import argparse
import json
import pathlib
import re
import sys

ALLOWED_ROOTS = ("app/src/main/java/", "app/src/main/res/")
BLOCKED_PARTS = ("signing", "keystore", "secrets", ".github", "gradle", "build")
MAX_FILES = 12
MAX_TOTAL_BYTES = 180000
PHASES = ("visual-identity", "layout-composition", "typography", "glass-depth",
          "motion-interactions", "consistency", "usability", "final-refinement")
SHA = re.compile(r"^[0-9a-f]{40}$")

def validate_path(raw):
    if not isinstance(raw, str) or not raw or "\\" in raw:
        raise ValueError("Invalid path")
    p = pathlib.PurePosixPath(raw)
    if p.is_absolute() or ".." in p.parts or str(p) != raw:
        raise ValueError("Path traversal or noncanonical path")
    if not raw.startswith(ALLOWED_ROOTS):
        raise ValueError(f"Path outside Android UI allowlist: {raw}")
    if any(part.lower() in BLOCKED_PARTS for part in p.parts):
        raise ValueError(f"Protected path: {raw}")
    if p.suffix not in (".kt", ".xml"):
        raise ValueError(f"Unsupported UI file extension: {raw}")
    return raw

def validate(proposal):
    if not isinstance(proposal, dict):
        raise ValueError("Proposal must be an object")
    required = {"phase", "pass", "base_sha", "intent", "changes"}
    if set(proposal) != required:
        raise ValueError("Unexpected or missing proposal keys")
    if proposal["phase"] not in PHASES or type(proposal["pass"]) is not int or not 1 <= proposal["pass"] <= 100:
        raise ValueError("Invalid campaign slot")
    if not isinstance(proposal["base_sha"], str) or not SHA.fullmatch(proposal["base_sha"]):
        raise ValueError("Invalid base SHA")
    if not isinstance(proposal["intent"], str) or not 20 <= len(proposal["intent"]) <= 2000:
        raise ValueError("Specific refinement intent required")
    changes = proposal["changes"]
    if not isinstance(changes, list) or not 1 <= len(changes) <= MAX_FILES:
        raise ValueError("Invalid change count")
    seen = set()
    total = 0
    for change in changes:
        if not isinstance(change, dict) or set(change) != {"path", "before_sha256", "content"}:
            raise ValueError("Changes require path, before_sha256, content")
        path = validate_path(change["path"])
        if path in seen:
            raise ValueError("Duplicate change path")
        seen.add(path)
        digest = change["before_sha256"]
        if not isinstance(digest, str) or not re.fullmatch(r"[0-9a-f]{64}", digest):
            raise ValueError("Missing original-file digest")
        text = change["content"]
        if not isinstance(text, str) or not text:
            raise ValueError("Empty replacement content")
        total += len(text.encode("utf-8"))
        if total > MAX_TOTAL_BYTES:
            raise ValueError("Proposal exceeds size budget")
        if "com.courtquestpro" in text and ("applicationId =" in text or "namespace =" in text):
            raise ValueError("Application identity configuration cannot be modified")
    return {"phase": proposal["phase"], "pass": proposal["pass"],
            "files": len(changes), "bytes": total, "validated": True,
            "applied": False, "accepted": False}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("proposal", type=pathlib.Path)
    args = parser.parse_args()
    print(json.dumps(validate(json.loads(args.proposal.read_text(encoding="utf-8"))), sort_keys=True))

if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, UnicodeError, json.JSONDecodeError) as exc:
        print(f"Refinement proposal rejected: {exc}", file=sys.stderr)
        sys.exit(1)
