#!/usr/bin/env python3
"""One bounded AI-generated refinement proposal; never edits files or commits.

Uses local Ollama at 127.0.0.1; no external API credentials.
This is a proposal generator, NOT an unattended 800-pass campaign.
"""
import argparse
import hashlib
import json
import pathlib
import subprocess
import sys
import urllib.error
import urllib.request

from validate_refinement_proposal import validate, validate_path

MAX_INPUT = 28000
MAX_OUTPUT = 60000

def generate(root, target, phase, number, intent, model):
    validate_path(target)
    root = root.resolve(strict=True)
    source = root / target
    if not source.is_file() or source.is_symlink():
        raise ValueError("Target must be an existing regular UI file")
    original = source.read_bytes()
    if len(original) > MAX_INPUT:
        raise ValueError("Target exceeds bounded model input size; choose a smaller UI file")
    sha = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip()
    if subprocess.check_output(["git", "status", "--porcelain", "--untracked-files=no"], cwd=root, text=True).strip():
        raise ValueError("Repository must be clean")
    prompt = ("Improve only the supplied Android UI source file. Preserve package, public APIs, "
              "state persistence, accessibility, and existing functionality. Do not modify "
              "signing, package ID, or security boundaries. Return the complete revised file "
              "as plain text with no markdown fences. If uncertain, return the source unchanged.\n"
              f"Category: {phase}\nPass: {number}\nGoal: {intent}\n"
              f"Path: {target}\nOriginal source:\n{original.decode('utf-8')}")
    payload = {"model": model, "messages": [
        {"role": "system", "content": "You are an Android Kotlin UI refinement coding agent. Output only the complete revised source file."},
        {"role": "user", "content": prompt}],
        "stream": False, "options": {"temperature": 0.2, "num_predict": 4096}}
    req = urllib.request.Request("http://127.0.0.1:11434/api/chat",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"}, method="POST")
    with urllib.request.urlopen(req, timeout=420) as response:
        data = response.read(MAX_OUTPUT + 1)
    if len(data) > MAX_OUTPUT:
        raise ValueError("Local model response exceeds bounded output size")
    result = json.loads(data)
    revised = result["message"]["content"]
    if not isinstance(revised, str):
        raise ValueError("Model response is not source text")
    revised = revised.strip()
    if revised.startswith("```"):
        lines = revised.splitlines()
        if len(lines) < 3 or not lines[-1].strip() == "```":
            raise ValueError("Model returned an incomplete fenced source block")
        revised = "\n".join(lines[1:-1]) + "\n"
    if not revised.lstrip().startswith("package com.courtquestpro"):
        raise ValueError("Model response does not begin with the required Kotlin package declaration")
    if "kotlinx.parcelize" in revised and "kotlinx.parcelize" not in original.decode("utf-8"):
        raise ValueError("Model introduced an unsupported Parcelize dependency")
    if "".join(revised.split()) == "".join(original.decode("utf-8").split()):
        raise ValueError("Model changed only whitespace; no meaningful refinement proposed")
    proposal = {"phase": phase, "pass": number, "base_sha": sha, "intent": intent,
        "changes": [{"path": target, "before_sha256": hashlib.sha256(original).hexdigest(),
                     "content": revised}]}
    validate(proposal)
    return proposal

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--root", type=pathlib.Path, default=pathlib.Path("."))
    p.add_argument("--target", required=True)
    p.add_argument("--phase", default="visual-identity")
    p.add_argument("--pass-number", type=int, default=1)
    p.add_argument("--intent", required=True)
    p.add_argument("--model", default="qwen2.5-coder:1.5b-instruct")
    p.add_argument("--output", type=pathlib.Path, required=True)
    args = p.parse_args()
    try:
        proposal = generate(args.root, args.target, args.phase, args.pass_number, args.intent, args.model)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(proposal, indent=2) + "\n", encoding="utf-8")
        print(json.dumps({"proposal_generated": True, "accepted": False,
                          "phase": args.phase, "pass": args.pass_number,
                          "path": args.target, "output": str(args.output)}))
    except (ValueError, OSError, KeyError, IndexError, UnicodeError, json.JSONDecodeError,
            urllib.error.URLError, subprocess.CalledProcessError) as exc:
        print(f"Proposal generation failed: {type(exc).__name__}: {exc}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
