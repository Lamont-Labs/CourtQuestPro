"""Verify v56 replacement, run the real Studio and collect Android window captures."""
import argparse
import hashlib
import io
import json
import re
import sys
import tarfile
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'scripts'))
import verify_android_daily_flow as ui
from verify_android_library_flow import seed_state, state
from verify_phone_update import saved_values_preserved


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run_test(method, folder):
    pkg = ui.PACKAGE
    result = ui.shell('am', 'instrument', '-w', '-e', 'class', f'{pkg}.JerseyStudioTest#{method}',
                      f'{pkg}.test/androidx.test.runner.AndroidJUnitRunner', timeout=420)
    (folder / f'{method}.txt').write_text(result + '\n')
    if 'OK (1 test)' not in result:
        (folder / 'failure-screen.png').write_bytes(ui.adb('exec-out', 'screencap', '-p'))
        if ui.shell('run-as', pkg, 'ls', 'files').find('jersey-proof') >= 0: collect(folder)
        raise RuntimeError(result)


def collect(folder):
    payload = ui.adb('exec-out', 'run-as', ui.PACKAGE, 'tar', '-cf', '-', '-C', 'files/jersey-proof', '.', timeout=180)
    with tarfile.open(fileobj=io.BytesIO(payload)) as archive:
        for entry in archive:
            if entry.isfile() and re.fullmatch(r'(?:\./)?[a-z0-9-]+\.(?:png|json)', entry.name):
                (folder / Path(entry.name).name).write_bytes(archive.extractfile(entry).read())


def upgrade(previous, candidate, folder):
    assert 'Success' in ui.adb('install', str(previous)).decode()
    assert re.search(r'versionCode=56\b', ui.shell('dumpsys', 'package', ui.PACKAGE))
    ui.seed('game-history')
    fixture = state()
    inks = dict(zip(('fabric', 'secondary', 'side', 'collar', 'armhole', 'numberInk', 'nameInk'),
                    [int(x, 16) - (1 << 24) for x in ('21344A', 'E1B95B', '6D3848', 'BD7A5D', '819F98', 'F3E7D3', 'D9C4AE')]))
    fixture.update(playerName='Morgan Lane', jerseyNumber=0, jerseyDoubleZero=True, jerseyDesign=dict(templateId='RETRO', **inks))
    seed_state(fixture)
    before = state()
    assert before['jerseyDesign'] == fixture['jerseyDesign'] and before['jerseyDoubleZero'] and before['matches'] and before['drillResults']
    ui.shell('am', 'force-stop', ui.PACKAGE)
    read_preferences = ('exec-out', 'run-as', ui.PACKAGE, 'cat', 'shared_prefs/court_quest_pro.xml')
    before_bytes = ui.adb(*read_preferences)
    assert 'Success' in ui.adb('install', '-r', str(candidate), timeout=180).decode()
    assert ui.adb(*read_preferences) == before_bytes
    assert re.search(r'versionCode=57\b', ui.shell('dumpsys', 'package', ui.PACKAGE))
    for _ in range(2):
        ui.open_app(); ui.tap(ui.wait_tagged('welcome-get-started')); ui.wait_tagged('dock-me')
        assert saved_values_preserved(before, state())
        ui.shell('am', 'force-stop', ui.PACKAGE)
    record = dict(versions=[56,57], android_install_replace=True, preferences_byte_identical_after_install=True,
                  saved_values_preserved_after_launch=True, saved_values_preserved_after_relaunch=True,
                  legacy_style='RETRO', legacy_number='00', checked_jersey_colors=7, matches=len(before['matches']),
                  drill_results=len(before['drillResults']), xp=before['totalXp'], input_apk_sha256=digest(candidate),
                  baseline_apk_sha256=digest(previous), preference_sha256=hashlib.sha256(before_bytes).hexdigest())
    (folder / 'install-upgrade.json').write_text(json.dumps(record, indent=2) + '\n')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for arg in ('previous', 'candidate', 'instrumentation', 'out'):
        parser.add_argument('--' + arg, type=Path, required=True)
    args = parser.parse_args()
    for _ in range(120):
        try:
            if ui.shell('getprop', 'sys.boot_completed', timeout=8) == '1': break
        except Exception: pass
        time.sleep(3)
    else: raise RuntimeError('Emulator boot timed out')
    assert ui.shell('getprop','ro.kernel.qemu') == '1'
    assert ui.adb('emu','avd','name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA'
    ui.shell('input','keyevent','KEYCODE_WAKEUP'); ui.shell('wm','dismiss-keyguard')
    ui.shell('settings','put','system','accelerometer_rotation','0')
    ui.shell('settings','put','system','user_rotation','0'); ui.shell('wm','density','320')
    ui.shell('wm','size','720x1600'); ui.shell('settings','put','system','font_scale','1.0')
    args.out.mkdir(parents=True, exist_ok=True)
    upgrade(args.previous.resolve(), args.candidate.resolve(), args.out)
    assert 'Success' in ui.adb('install','-r',str(args.instrumentation.resolve())).decode()
    legacy = args.out / 'migration'; legacy.mkdir(exist_ok=True)
    run_test('upgradedV56JerseyIsVisibleBeforeAnySave', legacy); collect(legacy)
    for label, size, scale, rotation in [('normal','720x1600','1.0','0'), ('compact','640x1280','1.3','0'),
                                         ('large-text','640x1280','2.0','0'), ('landscape','800x1600','1.0','1')]:
        folder = args.out / label; folder.mkdir(exist_ok=True)
        ui.shell('wm','size',size); ui.shell('settings','put','system','font_scale',scale)
        ui.shell('settings','put','system','user_rotation',rotation)
        assert ui.shell('pm','clear',ui.PACKAGE) == 'Success'
        run_test('customizationRestoresAcrossRecreationAndReturnsToCourt', folder)
        ui.shell('am','force-stop',ui.PACKAGE); run_test('coldStartRestoresPreviouslySavedJersey', folder)
        if label == 'normal': run_test('paletteFrameTimingAndRapidInput', folder)
        collect(folder)
        for group, sides in [('default',['front','back']),('custom',['front','back']),('plain',['front'])]:
            for style in ('arena','classic','fastbreak','retro'):
                for side in sides:
                    prefix = folder / f'{group}-{style}-{side}'
                    assert prefix.with_suffix('.png').is_file() and (folder / f'{prefix.name}-garment.png').is_file()
        (folder / 'configuration.txt').write_text(ui.shell('am','get-config') + '\n' + ui.shell('wm','size') + '\nfont_scale=' + scale + '\n')


if __name__ == '__main__': main()
