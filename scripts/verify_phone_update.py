"""Verify the real released APK, install over it on guarded QA, and seal release evidence."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
from xml.etree import ElementTree as ET

from verify_apk import EXPECTED_CERTIFICATE, require, run


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def identity(apk, tools, output, prefix):
    signature = run(str(tools / 'apksigner'), 'verify', '--verbose', '--print-certs', str(apk))
    require('Verified using v2 scheme (APK Signature Scheme v2): true' in signature,
            f'{prefix}: invalid v2 signature')
    certs = re.findall(r'Signer #\d+ certificate SHA-256 digest:\s*([0-9a-fA-F]{64})', signature)
    require(len(certs) == 1, f'{prefix}: expected one signing identity')
    badging = run(str(tools / 'aapt'), 'dump', 'badging', str(apk))
    match = re.search(r"^package: name='([^']+)' versionCode='(\d+)'", badging, re.M)
    require(match is not None, f'{prefix}: package/version missing')
    (output / f'{prefix}-signature.txt').write_text(signature)
    (output / f'{prefix}-badging.txt').write_text(badging)
    return dict(package_id=match[1], version_code=int(match[2]),
                certificate_sha256=certs[0].lower(), apk_sha256=digest(apk))


def compatible(old, new):
    require(old['package_id'] == new['package_id'] == 'com.courtquestpro', 'Package identity changed')
    require(old['certificate_sha256'] == new['certificate_sha256'] == EXPECTED_CERTIFICATE,
            'Original signing identity does not match the released v20 APK')
    require(old['version_code'] == 20 and new['version_code'] == 21 and
            new['version_code'] > old['version_code'], 'Expected a strictly increasing v20 -> v21 update')


def saved_values_preserved(before, after):
    if isinstance(before, dict):
        return isinstance(after, dict) and all(
            key in after and saved_values_preserved(value, after[key]) for key, value in before.items())
    if isinstance(before, list):
        return isinstance(after, list) and len(before) == len(after) and all(
            saved_values_preserved(a, b) for a, b in zip(before, after))
    return type(before) is type(after) and before == after


def identities(args):
    old = identity(args.previous, args.tools, args.output, 'v20')
    new = identity(args.candidate, args.tools, args.output, 'v21')
    compatible(old, new)
    (args.output / 'upgrade-identity.json').write_text(json.dumps(dict(previous=old, candidate=new), indent=2) + '\n')
    print(json.dumps(dict(previous=old, candidate=new), indent=2))


def upgrade(args):
    import verify_android_daily_flow as ui
    from verify_android_library_flow import seed_state
    # No caller can direct this fixture writer at an owner's physical phone.
    require(ui.shell('getprop', 'ro.kernel.qemu') == '1', 'Only the disposable QA emulator is allowed')
    require(ui.adb('emu', 'avd', 'name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA',
            'Wrong AVD: refusing to install or seed test data')
    ui.OUT = args.output
    require('Success' in ui.adb('install', str(args.previous.resolve())).decode(), 'Install released v20 failed')
    ui.seed('game-history')
    fixture = json.loads(ui.prefs()['state'].text)
    drill = dict(id='custom:upgrade:1', category='Shooting', title='Upgrade fixture',
                 level='Beginner', minutes=4, instructions='Record three controlled shots.',
                 target='Make three shots.', videoQuery='basketball form shooting', minimumSuccesses=3)
    measurement = dict(schemaId='success-count', minimumSuccesses=3, successLabel='successful',
                       attemptLabel='attempts', successButton='Record successful attempt',
                       description='Self-recorded upgrade fixture')
    step = dict(id='upgrade-step', drill=drill, measurement=measurement, required=False)
    constraints = dict(skill='Any skill', minutes=15, equipment=['Ball'],
                       space='Small space', maxLevel='Beginner')
    fixture.update(jerseyNumber=42, weeklyActivities=['Skills'], awardedKeys=['upgrade-fixture-award'],
                   playerScore=3, opponentScore=2, pointHistory=[True, True, True, False, False],
                   matchPlayer='Jordan', matchOpponent='QA opponent',
                   customDrills=[dict(drill=drill, familyId='upgrade-family', archived=False)],
                   favoriteDrillIds=[drill['id']],
                   missionDraft=dict(missionId='upgrade-mission', title='Upgrade mission', steps=[step],
                                     baseVersionId='upgrade-version', constraints=constraints),
                   missionVersions=[dict(id='upgrade-version', missionId='upgrade-mission', version=1,
                                         title='Upgrade mission', source='MANUAL', steps=[step],
                                         createdAt=fixture['activeDate'] + 'T12:00:00Z')])
    seed_state(fixture)  # Let the actual v20 application deserialize/canonicalize its saved state.
    before = json.loads(ui.prefs()['state'].text)
    require(before['totalXp'] == 100 and len(before['matches']) == 8 and len(before['drillResults']) == 2
            and before['jerseyNumber'] == 42 and before['playerScore'] == 3
            and len(before['customDrills']) == len(before['missionVersions']) == 1,
            'Released v20 did not retain the synthetic fixture')
    ui.screenshot('v20-before-update')
    ui.shell('am', 'force-stop', ui.PACKAGE)
    prefs_path = 'shared_prefs/court_quest_pro.xml'
    raw = ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat', prefs_path)
    result = ui.adb('install', '-r', str(args.candidate.resolve()), timeout=180).decode()
    require('Success' in result, f'Android rejected the in-place upgrade: {result}')
    require(ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat', prefs_path) == raw,
            'Package installation altered existing preferences')
    installed = ui.shell('dumpsys', 'package', ui.PACKAGE)
    require(re.search(r'versionCode=21\b', installed) is not None, 'Installed version is not 21')
    ui.open_app()
    after = json.loads(ui.prefs()['state'].text)
    require(saved_values_preserved(before, after), 'Existing v20 player data changed on first v21 launch')
    ui.wait_tagged('nav-open-handle')
    ui.screenshot('v21-after-update')
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.open_app()
    require(saved_values_preserved(before, json.loads(ui.prefs()['state'].text)),
            'Existing data changed after relaunch')
    report = dict(input_apk_sha256=digest(args.candidate), baseline_apk_sha256=digest(args.previous),
                  versions=[20, 21], android_install_replace=True, preferences_byte_identical_after_install=True,
                  saved_values_preserved_after_launch=True, saved_values_preserved_after_relaunch=True,
                  physical_device_test='NOT_PERFORMED', destructive_phone_operations='NONE')
    (args.output / 'install-upgrade.json').write_text(json.dumps(report, indent=2) + '\n')


def finalize(args):
    manifest = json.loads((args.output / 'apk-manifest.json').read_text())
    upgrade_report = json.loads((args.output / 'install-upgrade.json').read_text())
    identities_report = json.loads((args.output / 'upgrade-identity.json').read_text())
    compatible(identities_report['previous'], identities_report['candidate'])
    sha = digest(args.candidate)
    require(sha == manifest['apk_sha256'] == upgrade_report['input_apk_sha256'] ==
            identities_report['candidate']['apk_sha256'], 'Candidate evidence refers to different APKs')
    require(upgrade_report['baseline_apk_sha256'] == identities_report['previous']['apk_sha256']
            and upgrade_report['versions'] == [20, 21]
            and all(v for v in upgrade_report.values() if isinstance(v, bool)),
            'In-place upgrade evidence is incomplete or uses a different baseline')
    native = Path('native-evidence')
    require(digest(native / 'app/build/outputs/apk/debug/app-debug.apk') == sha,
            'Native tests did not exercise the exact release APK')
    revisions = (native / 'artifacts/source-revisions.txt').read_text().splitlines()
    tree = subprocess.check_output(['git', 'rev-parse', 'HEAD:app/src'], text=True).strip()
    require(revisions == [os.environ['GITHUB_SHA'], tree], 'Native evidence source mismatch')
    for name in ['candidate-result', 'game-result', 'library-result', 'mission-result', 'navigation-result']:
        report = json.loads((native / f'artifacts/android-daily-flow/{name}.json').read_text())
        require(report['input_apk_sha256'] == sha and all(v for v in report.values() if isinstance(v, bool)),
                f'{name} is incomplete or belongs to a different APK')
    suites = [ET.parse(p).getroot() for p in (native / 'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml')]
    require(sum(int(s.get('tests', 0)) for s in suites) >= 72 and not sum(
        int(s.get(k, 0)) for s in suites for k in ['errors', 'failures', 'skipped']), 'Android unit tests incomplete')
    issues = ET.parse(native / 'app/build/reports/lint-results-debug.xml').getroot().findall('issue')
    require(not any(i.get('severity') in ['Error', 'Fatal'] for i in issues), 'Android lint has errors')
    smoke = ET.parse(native / 'artifacts/premium-flow-toolkit/report.xml').getroot()
    suites = [smoke] if smoke.tag == 'testsuite' else list(smoke.iter('testsuite'))
    require(sum(int(s.get('tests', 0)) for s in suites) >= 1 and not sum(
        int(s.get(k, 0)) for s in suites for k in ['errors', 'failures', 'skipped']), 'Maestro evidence incomplete')
    manifest.update(upgrade_identity=identities_report, install_upgrade=upgrade_report,
                    exact_commit_ci=json.loads((args.output / 'ci-gates.json').read_text()))
    manifest['checks']['emulator_test'] = 'PASS_V20_TO_V21_IN_PLACE_AND_FULL_NATIVE_SAME_APK'
    (args.output / 'apk-manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
    print('All exact-binary release gates passed:', sha)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('phase', choices=['identity', 'upgrade', 'finalize'])
    parser.add_argument('--previous', type=Path, default=Path('previous/CourtQuestPro-Update-v20.apk'))
    parser.add_argument('--candidate', type=Path, default=Path('app/build/outputs/apk/debug/app-debug.apk'))
    parser.add_argument('--tools', type=Path, default=Path(os.environ.get('ANDROID_HOME', '')) / 'build-tools/35.0.1')
    parser.add_argument('--output', type=Path, default=Path('dist'))
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    dict(identity=identities, upgrade=upgrade, finalize=finalize)[args.phase](args)
