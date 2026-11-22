"""Fail-closed source and exact-commit checks for CourtQuest Pro phone APK v22."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import time


BASE = "4545d60ba392fd1ecfb302179186f65459c9c073"
BRANCH = "dev/premium-flow-toolkit"
REPO = "Lamont-Labs/CourtQuestPro"
WORKFLOWS = {
    "android-development.yml": ".github/workflows/android-development.yml",
    "verify-compact-navigation.yml": ".github/workflows/verify-compact-navigation.yml",
    "verify-native-library-flow.yml": ".github/workflows/verify-native-library-flow.yml",
    "premium-flow-toolkit.yml": ".github/workflows/premium-flow-toolkit.yml",
}
ALLOWED_FILES = {
    ".github/workflows/courtquest-phone-update-v22.yml",
    "app/build.gradle.kts",
    "scripts/phone_v22_release_gate.py",
    "scripts/test_android_flow_phone_update.py",
    "scripts/test_phone_v22_release_gate.py",
    "scripts/verify_phone_update.py",
}


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def api(path):
    return json.loads(subprocess.check_output(["gh", "api", f"repos/{REPO}/{path}"], text=True))


def validate_source(parent, ref, changed_files, base_gradle, current_gradle):
    require(parent == BASE, "v22 must be based directly on the approved redesigned commit 4545d60")
    require(ref == f"refs/heads/{BRANCH}", f"v22 must build from {BRANCH}")
    require(set(changed_files) == ALLOWED_FILES, "Release commit contains changes outside version and release verification")
    require(base_gradle.count("versionCode = 21") == 1, "Approved source does not contain exactly versionCode 21")
    require(current_gradle == base_gradle.replace("versionCode = 21", "versionCode = 22"),
            "Android build configuration changed beyond versionCode 21 -> 22")
    require("applicationId = \"com.courtquestpro\"" in current_gradle, "Android package ID changed")


def source():
    sha = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    require(sha == os.environ.get("GITHUB_SHA"), "Checkout does not match the exact workflow commit")
    parent = subprocess.check_output(["git", "rev-parse", "HEAD^"], text=True).strip()
    changed = subprocess.check_output(
        ["git", "diff", "--name-only", f"{BASE}..HEAD"], text=True
    ).splitlines()
    base_gradle = subprocess.check_output(
        ["git", "show", f"{BASE}:app/build.gradle.kts"], text=True
    )
    validate_source(
        parent,
        os.environ.get("GITHUB_REF", ""),
        changed,
        base_gradle,
        Path("app/build.gradle.kts").read_text(),
    )
    baseline_tree = subprocess.check_output(["git", "rev-parse", f"{BASE}:app/src"], text=True).strip()
    current_tree = subprocess.check_output(["git", "rev-parse", "HEAD:app/src"], text=True).strip()
    require(baseline_tree == current_tree, "Native app source differs from the approved redesign")
    print(f"Verified redesigned source {BASE}; only the v22 release/version gate changed.")


def latest_exact_run(workflow, sha, branch, runs):
    path = WORKFLOWS[workflow]
    matches = [
        run for run in runs
        if run.get("head_sha") == sha and run.get("head_branch") == branch
    ]
    if not matches:
        return None
    run = max(matches, key=lambda entry: entry["id"])
    require(run.get("path") == path, f"Unexpected workflow path for {workflow}")
    require(
        run["status"] != "completed" or run["conclusion"] == "success",
        f"Release blocked: {workflow} concluded {run.get('conclusion')} for {sha}",
    )
    return {key: run[key] for key in ("id", "head_sha", "head_branch", "path",
                                       "status", "conclusion", "html_url")}


def wait_for_ci(output, timeout=3900):
    sha = os.environ["GITHUB_SHA"]
    branch = os.environ.get("GITHUB_REF", "").removeprefix("refs/heads/")
    require(branch == BRANCH, f"Expected exact-commit checks on {BRANCH}")
    deadline = time.monotonic() + timeout
    last = None
    while time.monotonic() < deadline:
        results = {}
        for workflow in WORKFLOWS:
            runs = api(
                f"actions/workflows/{workflow}/runs?branch={branch}&per_page=50"
            )["workflow_runs"]
            result = latest_exact_run(workflow, sha, branch, runs)
            if result:
                results[workflow] = result
        state = [(name, run["status"], run["conclusion"]) for name, run in results.items()]
        if state != last:
            print(f"Exact-commit v22 gates: {state}", flush=True)
            last = state
        if len(results) == len(WORKFLOWS) and all(
            result["status"] == "completed" and result["conclusion"] == "success"
            for result in results.values()
        ):
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_text(json.dumps(results, indent=2) + "\n")
            return
        time.sleep(30)
    raise RuntimeError("All four exact-commit Android checks did not pass before timeout")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("phase", choices=["source", "ci"])
    parser.add_argument("--output", type=Path, default=Path("dist/ci-gates.json"))
    arguments = parser.parse_args()
    source() if arguments.phase == "source" else wait_for_ci(arguments.output)
