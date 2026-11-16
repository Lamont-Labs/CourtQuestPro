"""Fail-closed source and exact-commit checks for CourtQuest Pro phone APK v24."""
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

def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def api(path):
    return json.loads(subprocess.check_output(["gh", "api", f"repos/{REPO}/{path}"], text=True))


def source():
    sha = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    require(sha == os.environ.get("GITHUB_SHA"), "Checkout does not match release commit")
    require(os.environ.get("GITHUB_REF") == f"refs/heads/{BRANCH}", "Unexpected release branch")
    require("versionCode = 24" in Path("app/build.gradle.kts").read_text(),
            "Release must use Android version code 23")
    require('applicationId = "com.courtquestpro"' in Path("app/build.gradle.kts").read_text(),
            "Android application identity changed")
    require(subprocess.run(["git", "merge-base", "--is-ancestor", BASE, "HEAD"]).returncode == 0,
            "Approved redesign is not an ancestor of release source")
    for path in ["app/src/main/java/com/courtquestpro/CourtQuestBottomDock.kt",
                 "scripts/verify_premium_visuals.py"]:
        require(Path(path).is_file(), f"Required redesign component missing: {path}")
    release_check = subprocess.run(["gh", "api", f"repos/{REPO}/releases/tags/courtquest-phone-update-v24"], capture_output=True)
    require(release_check.returncode != 0, "v24 release already exists; never overwrite it")


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
            print(f"Exact-commit v24 gates: {state}", flush=True)
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
