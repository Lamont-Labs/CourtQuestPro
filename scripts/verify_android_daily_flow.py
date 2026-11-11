from pathlib import Path
from xml.etree import ElementTree as ET
import argparse
import json
import os
import re
import subprocess
import time


ROOT = Path(__file__).resolve().parents[1]
SDK = Path(os.environ.get('ANDROID_HOME', str(ROOT.parent / 'courtquest-sdk')))
ADB = SDK / 'platform-tools/adb'
PACKAGE = 'com.courtquestpro'
OUT = Path(os.environ.get('COURTQUEST_UI_EVIDENCE', str(ROOT / 'artifacts/android-daily-flow')))


def adb(*args, data=None, timeout=120):
    result = subprocess.run([str(ADB), '-s', 'emulator-5562', *args], input=data,
                            capture_output=True, timeout=timeout)
    if result.returncode:
        raise RuntimeError(f'{args}: {result.stdout.decode(errors="replace")} {result.stderr.decode(errors="replace")}')
    return result.stdout


def shell(*args, **kwargs):
    return adb('shell', *args, **kwargs).decode(errors='replace').strip()


def nodes(label=None):
    last_error = None
    for attempt in range(3):
        try:
            shell('uiautomator', 'dump', '--compressed', '/data/local/tmp/cq-visual.xml', timeout=90)
            raw = adb('exec-out', 'cat', '/data/local/tmp/cq-visual.xml')
            parsed = ET.fromstring(raw)
            if label:
                (OUT / f'{label}.xml').write_bytes(raw)
            return list(parsed.iter('node'))
        except (ET.ParseError, RuntimeError, subprocess.TimeoutExpired) as error:
            last_error = error
            time.sleep(2)
    raise last_error


def find(items, text):
    return next((node for node in items if any(text.casefold() == value.strip().casefold()
                 for value in (node.get('text', ''), node.get('content-desc', '')))), None)


def require(items, text):
    node = find(items, text)
    assert node is not None, (text, [node.get('text') or node.get('content-desc') for node in items])
    return node


def tap(node):
    left, top, right, bottom = map(int, re.findall(r'\d+', node.attrib['bounds']))
    assert right > left and bottom > top, node.attrib
    shell('input', 'tap', str((left + right) // 2), str((top + bottom) // 2))
    time.sleep(0.5)


def screenshot(name):
    data = adb('exec-out', 'screencap', '-p')
    assert data.startswith(bytes.fromhex('89504e470d0a1a0a')) and len(data) > 10000
    (OUT / f'{name}.png').write_bytes(data)
    print('Captured', name, flush=True)


def open_app():
    print(shell('am', 'start', '-n', f'{PACKAGE}/.MainActivity', timeout=180), flush=True)
    time.sleep(3)


def prefs():
    root = ET.fromstring(adb('exec-out', 'run-as', PACKAGE, 'cat', 'shared_prefs/court_quest_pro.xml'))
    return {node.attrib['name']: node for node in root}


def seed():
    today = shell('date', '+%F')
    ids = ['s1', 'b1', 'f1', 'd1', 'r1', 'p1']
    state = dict(playerName='Jordan', difficulty='Intermediate', weeklyPlan='Balanced',
                 trainingDays=[1, 3, 5], weeklyActivities=[], totalXp=100, currentStreak=1,
                 longestStreak=2, lastStreakDate='', activeDate=today,
                 slots=[dict(id=f'slot-{i}', drillId=drill, complete=False) for i, drill in enumerate(ids)],
                 championshipDone=False, playerScore=0, opponentScore=0, pointHistory=[],
                 matchPlayer='', matchOpponent='', matches=[], awardedKeys=[],
                 drillResults=[dict(date=today, slotId='previous-1', drillId='s1', completed=15, attempted=20),
                               dict(date=today, slotId='previous-2', drillId='s1', completed=18, attempted=20)],
                 drillDrafts=[dict(date=today, slotId='slot-0', drillId='s1', successful=0,
                                  attempted=0, targetConfirmed=False, remainingSeconds=360)], showWarmupDone=False)
    root = ET.Element('map')
    ET.SubElement(root, 'boolean', name='onboarding_complete', value='true')
    ET.SubElement(root, 'string', name='state').text = json.dumps(state)
    shell('am', 'force-stop', PACKAGE)
    shell('run-as', PACKAGE, 'mkdir', '-p', 'shared_prefs')
    shell(f'run-as {PACKAGE} sh -c "cat > shared_prefs/court_quest_pro.xml"',
          data=ET.tostring(root, encoding='utf-8'))


def scroll_to(text, limit=10):
    for _ in range(limit):
        items = nodes()
        node = find(items, text)
        if node is not None:
            return node
        shell('input', 'swipe', '360', '1230', '360', '480', '350')
        time.sleep(0.5)
    raise AssertionError(f'Control {text!r} was not reachable by scrolling')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('phase', choices=['baseline', 'candidate'])
    parser.add_argument('apk')
    args = parser.parse_args()
    OUT.mkdir(exist_ok=True)
    for attempt in range(100):
        try:
            if shell('getprop', 'sys.boot_completed', timeout=8) == '1':
                break
        except (RuntimeError, subprocess.TimeoutExpired):
            pass
        if attempt % 6 == 0:
            print('Waiting for Android', attempt + 1, flush=True)
        time.sleep(5)
    else:
        raise RuntimeError('Android did not finish booting')
    for key in ('window_animation_scale', 'transition_animation_scale', 'animator_duration_scale'):
        shell('settings', 'put', 'global', key, '0')
    shell('input', 'keyevent', 'KEYCODE_WAKEUP')
    shell('wm', 'dismiss-keyguard')
    shell('wm', 'size', '720x1600')
    shell('wm', 'density', '320')
    shell('settings', 'put', 'system', 'font_scale', '1.0')
    before_update = prefs()['state'].text if args.phase == 'candidate' else None
    assert 'Success' in adb('install', '-r', str(Path(args.apk).resolve()), timeout=180).decode()
    if before_update is not None:
        assert prefs()['state'].text == before_update, 'APK update must retain existing player data'
        print('Update retained the existing profile and results.', flush=True)
    seed()
    open_app()
    home = nodes(f'{args.phase}-court')
    screenshot(f'{args.phase}-court')
    action_visible = find(home, 'Resume session') is not None
    tap(require(home, 'Progress'))
    nodes(f'{args.phase}-progress')
    screenshot(f'{args.phase}-progress')
    tap(require(nodes(), 'Setup'))
    settings = nodes(f'{args.phase}-setup')
    screenshot(f'{args.phase}-setup')
    if args.phase == 'candidate':
        assert any(node.get('class') == 'android.widget.EditText' and node.get('text') == 'Jordan' for node in settings)
    tap(require(nodes(), 'Court'))
    tap(scroll_to('Resume session'))
    drill_nodes = nodes(f'{args.phase}-drill')
    screenshot(f'{args.phase}-drill')
    if args.phase == 'baseline':
        (OUT / 'baseline-result.json').write_text(json.dumps({'primary_on_first_viewport': action_visible}, indent=2))
        print('Baseline recorded. Primary action initially visible:', action_visible, flush=True)
        return

    assert action_visible, 'Resume session must be in the first viewport'
    success = require(drill_nodes, 'Record successful attempt')
    for _ in range(15):
        tap(success)
    tap(require(nodes(), 'Record missed / other attempt'))
    tap(require(nodes(), '− Miss'))
    confirmation = next(node for node in nodes() if node.get('checkable') == 'true')
    tap(confirmation)
    screenshot('candidate-drill-target-met')
    tap(require(nodes(), 'Save results'))
    receipt = nodes('candidate-result')
    require(receipt, 'Session recorded.')
    screenshot('candidate-result')
    state = json.loads(prefs()['state'].text)
    assert len(state['drillResults']) == 3
    assert state['drillResults'][-1]['completed'] == state['drillResults'][-1]['attempted'] == 15
    assert state['slots'][0]['complete'] and state['totalXp'] == 150
    assert not state['drillDrafts']
    tap(require(receipt, 'View Progress'))
    nodes('candidate-progress-after-save')
    screenshot('candidate-progress-after-save')
    scroll_to('15 successful / 15 attempts')
    nodes('candidate-progress-record')
    screenshot('candidate-progress-record')
    shell('am', 'force-stop', PACKAGE)
    open_app()
    require(nodes(), 'Jordan’s court.')
    assert len(json.loads(prefs()['state'].text)['drillResults']) == 3
    shell('wm', 'size', '640x1440')
    shell('settings', 'put', 'system', 'font_scale', '1.3')
    time.sleep(2)
    nodes('candidate-compact-court')
    screenshot('candidate-compact-court')
    tap(require(nodes(), 'Setup'))
    nodes('candidate-compact-setup')
    screenshot('candidate-compact-setup')
    shell('settings', 'put', 'system', 'font_scale', '2.0')
    time.sleep(2)
    large_nodes = nodes('candidate-large-type-setup')
    if not any(node.get('class') == 'android.widget.EditText' for node in large_nodes):
        tap(require(large_nodes, 'Setup'))
        nodes('candidate-large-type-setup')
    screenshot('candidate-large-type-setup')
    assert any(node.get('text') == 'Jordan' for node in nodes())
    report = dict(update_retains_saved_state=True, primary_on_first_viewport=True, saved_name_visible=True, result_recorded_once=True,
                  successes=15, attempts=15, xp_awarded=50, relaunch_persistence=True,
                  compact_capture=True, large_type_capture=True)
    (OUT / 'candidate-result.json').write_text(json.dumps(report, indent=2))
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
