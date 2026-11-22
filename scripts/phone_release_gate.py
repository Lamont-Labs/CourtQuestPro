"""Fail-closed provenance and exact-commit CI gates for the phone update."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import time

BASE = 'dd800009eed893d9cded658ca29011badd359476'
BASE_RUN = 43537145774
REPO = 'Lamont-Labs/CourtQuestPro'
BRANCH = 'repair/premium-flow-stabilization'


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def api(path):
    return json.loads(subprocess.check_output(['gh', 'api', f'repos/{REPO}/{path}'], text=True))


def source():
    run = api(f'actions/runs/{BASE_RUN}')
    require(run['head_sha'] == BASE and run['head_branch'] == BRANCH and
            run['conclusion'] == 'success' and
            run['path'] == '.github/workflows/verify-native-library-flow.yml',
            'Verified stabilization baseline is not the expected successful native run')
    subprocess.run(['git', 'diff', '--exit-code', BASE, '--', 'app/src', 'signing',
                    'gradle', 'gradlew', 'gradlew.bat', 'build.gradle.kts',
                    'settings.gradle.kts', 'gradle.properties'], check=True)
    old = subprocess.check_output(['git', 'show', f'{BASE}:app/build.gradle.kts'], text=True)
    require(old.count('versionCode = 20') == 1, 'Ambiguous baseline version')
    require(Path('app/build.gradle.kts').read_text() == old.replace('versionCode = 20', 'versionCode = 21'),
            'Android build configuration changed beyond the authorized version increment')
    print('Native source, artwork, dependencies and signing preserved; only versionCode changed 20 -> 21')


def wait_for_ci(output, timeout=3900):
    sha = os.environ['GITHUB_SHA']
    deadline = time.monotonic() + timeout
    paths = ['android-development.yml', 'verify-compact-navigation.yml', 'verify-native-library-flow.yml']
    last = None
    while time.monotonic() < deadline:
        results = {}
        for path in paths:
            runs = api(f'actions/workflows/{path}/runs?branch={BRANCH}&per_page=50')['workflow_runs']
            matches = [r for r in runs if r['head_sha'] == sha and r['head_branch'] == BRANCH]
            if matches:
                run = max(matches, key=lambda r: r['id'])
                require(run['status'] != 'completed' or run['conclusion'] == 'success',
                        f"Release blocked: {path} concluded {run['conclusion']} for {sha}")
                results[path] = {k: run[k] for k in ['id', 'head_sha', 'status', 'conclusion', 'html_url']}
        state = [(p, r['status'], r['conclusion']) for p, r in results.items()]
        if state != last:
            print('Exact-commit release gates:', state, flush=True)
            last = state
        if len(results) == len(paths) and all(r['status'] == 'completed' for r in results.values()):
            output.write_text(json.dumps(results, indent=2) + '\n')
            return
        time.sleep(30)
    raise RuntimeError('Exact-commit Android checks did not pass before the release gate timeout')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('phase', choices=['source', 'ci'])
    parser.add_argument('--output', type=Path, default=Path('dist/ci-gates.json'))
    args = parser.parse_args()
    source() if args.phase == 'source' else wait_for_ci(args.output)
