from pathlib import Path
from datetime import date, timedelta
from xml.etree import ElementTree as ET
import argparse
import hashlib
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
    return next((node for node in items if any(text.casefold() in [line.strip().casefold() for line in value.splitlines()]
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


def seed(variant="normal"):
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
    if variant == 'empty':
        state.update(totalXp=0, currentStreak=0, longestStreak=0, drillResults=[], drillDrafts=[])
    elif variant == 'blank':
        state['playerName'] = ''
    elif variant == 'long':
        state['playerName'] = 'Alexandria Montgomery-Washington'
    elif variant == 'history':
        state['drillResults'] = [dict(date=today, slotId=f'history-{i}', drillId='s1', completed=10 + i % 3, attempted=20) for i in range(150)]
    elif variant == 'complete':
        for slot in state['slots']:
            slot['complete'] = True
        state['drillDrafts'] = []
    if variant == 'game-history':
        state['matches'] = [dict(date='2025-03-10', player='Jordan', opponent=f'Guest {i}',
                                 playerScore=11, opponentScore=i, winner='Jordan', xp=0) for i in range(8)]
    if variant in ('previous-unfinished-game', 'previous-completed-game'):
        state.update(activeDate=(date.fromisoformat(today) - timedelta(days=1)).isoformat(),
                     matchPlayer='Jordan', matchOpponent='Casey', playerScore=5, opponentScore=2,
                     pointHistory=[True] * 5 + [False] * 2)
        if variant == 'previous-completed-game':
            state.update(championshipDone=True, playerScore=11, opponentScore=2)
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
        scrollables = [node for node in items if node.get('scrollable') == 'true']
        assert scrollables, f'No scrollable content can reach {text!r}'
        def bounds(node):
            return list(map(int, re.findall(r'\d+', node.attrib['bounds'])))
        scrollable = max(scrollables, key=lambda node: (bounds(node)[2] - bounds(node)[0]) * (bounds(node)[3] - bounds(node)[1]))
        left, top, right, bottom = bounds(scrollable)
        x = (left + right) // 2
        height = bottom - top
        shell('input', 'swipe', str(x), str(top + height * 4 // 5), str(x), str(top + height // 4), '350')
        time.sleep(0.5)
    raise AssertionError(f'Control {text!r} was not reachable by scrolling')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('phase', choices=['baseline', 'candidate'])
    parser.add_argument('apk')
    args = parser.parse_args()
    OUT.mkdir(exist_ok=True)
    for attempt in range(100):
        emulator_pid = os.environ.get('COURTQUEST_EMULATOR_PID')
        if emulator_pid:
            try:
                os.kill(int(emulator_pid), 0)
            except ProcessLookupError as error:
                raise RuntimeError('Android Emulator exited before boot; inspect emulator.log') from error
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
    packaged_version = int(re.search(r'versionCode=(\d+)', shell('dumpsys', 'package', PACKAGE)).group(1))
    if args.phase == 'candidate':
        assert packaged_version == int(os.environ.get('COURTQUEST_VERSION_CODE', '4'))
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
        tap(require(settings, 'Beginner'))
        changed_settings = nodes('candidate-preference-selected')
        label = require(changed_settings, 'Beginner')
        left, top, right, bottom = map(int, re.findall(r'\d+', label.attrib['bounds']))
        selected = False
        for node in changed_settings:
            if node.get('checked') != 'true' and node.get('selected') != 'true':
                continue
            bounds = list(map(int, re.findall(r'\d+', node.attrib['bounds'])))
            if bounds[0] <= left and bounds[1] <= top and bounds[2] >= right and bounds[3] >= bottom:
                selected = True
        assert selected, 'The chosen level must visibly select on the first tap'
        assert json.loads(prefs()['state'].text)['difficulty'] == 'Beginner'
    tap(require(nodes(), 'Court'))
    tap(scroll_to('Resume session'))
    drill_nodes = nodes(f'{args.phase}-drill')
    screenshot(f'{args.phase}-drill')
    if args.phase == 'baseline':
        (OUT / 'baseline-result.json').write_text(json.dumps({'primary_on_first_viewport': action_visible}, indent=2))
        print('Baseline recorded. Primary action initially visible:', action_visible, flush=True)
        return

    assert action_visible, 'Resume session must be in the first viewport'
    success = scroll_to('Record successful attempt')
    for _ in range(15):
        tap(success)
    tap(scroll_to('Record missed / other attempt'))
    tap(scroll_to('− Miss'))
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
    scroll_to('UP NEXT')
    scroll_to('Stationary Crossover Control')
    require(nodes(), 'Stationary Crossover Control')
    screenshot('candidate-receipt-next')
    tap(require(nodes(), 'Continue training'))
    next_drill = nodes('candidate-next-drill')
    require(next_drill, 'Stationary Crossover Control')
    screenshot('candidate-next-drill')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    tap(require(nodes(), 'Progress'))
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
    scroll_to('Jordan')
    nodes('candidate-large-type-profile')
    screenshot('candidate-large-type-profile')
    shell('wm', 'size', '720x1600')
    shell('settings', 'put', 'system', 'font_scale', '1.0')
    seed('empty')
    open_app()
    tap(require(nodes(), 'Progress'))
    require(nodes('candidate-empty-progress'), 'Start your baseline.')
    screenshot('candidate-empty-progress')
    seed('history')
    open_app()
    tap(require(nodes(), 'Progress'))
    tap(scroll_to('View all 150 sessions'))
    require(nodes('candidate-full-history'), 'Training history')
    screenshot('candidate-full-history')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    require(nodes(), 'Your work, recorded.')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    require(nodes(), 'Jordan’s court.')
    shell('wm', 'size', '640x1440')
    shell('settings', 'put', 'system', 'font_scale', '2.0')
    seed()
    open_app()
    tap(scroll_to('Resume session'))
    tap(scroll_to('Record successful attempt'))
    tap(scroll_to('Record missed / other attempt'))
    draft_state = json.loads(prefs()['state'].text)
    assert draft_state['drillDrafts'][0]['successful'] == 1 and draft_state['drillDrafts'][0]['attempted'] == 2
    require(nodes('candidate-large-type-drill'), 'Save results')
    screenshot('candidate-large-type-drill')
    shell('settings', 'put', 'system', 'font_scale', '1.0')
    shell('wm', 'size', '720x1600')
    seed('blank')
    open_app()
    require(nodes('candidate-blank-profile'), 'Your court.')
    screenshot('candidate-blank-profile')
    seed('long')
    open_app()
    require(nodes('candidate-long-profile'), 'Alexandria Montgomery-Washington’s court.')
    scroll_to('Resume session')
    screenshot('candidate-long-profile')
    seed('complete')
    open_app()
    complete_home = nodes('candidate-complete-court')
    require(complete_home, 'Work put in.')
    screenshot('candidate-complete-court')
    tap(require(complete_home, 'Play 1-on-1'))
    fields = [node for node in nodes() if node.get('class') == 'android.widget.EditText']
    assert len(fields) == 2
    tap(fields[1])
    shell('input', 'text', 'Casey')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    tap(scroll_to('Start game'))
    score_nodes = nodes('candidate-scorekeeper')
    screenshot('candidate-scorekeeper')
    tap(require(score_nodes, 'Record point for Jordan'))
    assert json.loads(prefs()['state'].text)['playerScore'] == 1
    require(nodes(), '1')
    tap(require(nodes(), 'Record point for Casey'))
    game = json.loads(prefs()['state'].text)
    assert game['playerScore'] == game['opponentScore'] == 1
    tap(require(nodes(), 'UNDO LAST POINT'))
    game = json.loads(prefs()['state'].text)
    assert game['playerScore'] == 1 and game['opponentScore'] == 0
    require(nodes('candidate-scorekeeper-undo'), '0')
    screenshot('candidate-scorekeeper-undo')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    require(nodes(), 'Jordan’s court.')
    shell('wm', 'size', '640x1440')
    shell('settings', 'put', 'system', 'font_scale', '2.0')
    seed('complete')
    open_app()
    tap(scroll_to('Play 1-on-1'))
    tap(scroll_to("Opponent's name (required)"))
    shell('input', 'text', 'Casey')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    tap(scroll_to('Start game'))
    tap(scroll_to('Record point for Jordan'))
    assert json.loads(prefs()['state'].text)['playerScore'] == 1
    tap(scroll_to('UNDO LAST POINT'))
    assert json.loads(prefs()['state'].text)['playerScore'] == 0
    nodes('candidate-large-type-scorekeeper')
    screenshot('candidate-large-type-scorekeeper')
    shell('settings', 'put', 'system', 'font_scale', '1.0')
    shell('wm', 'size', '720x1600')
    assert 'Success' in shell('pm', 'clear', PACKAGE)
    open_app()
    require(nodes('candidate-fresh-onboarding'), 'CONTINUE')
    screenshot('candidate-fresh-onboarding')
    tap(require(nodes(), 'CONTINUE'))
    identity = next(node for node in nodes() if node.get('class') == 'android.widget.EditText')
    tap(identity)
    shell('input', 'text', 'Avery')
    shell('input', 'keyevent', 'KEYCODE_BACK')
    for _ in range(3):
        tap(require(nodes(), 'CONTINUE'))
    nodes('candidate-fresh-week-plan')
    screenshot('candidate-fresh-week-plan')
    tap(require(nodes(), 'ENTER MY COURT'))
    require(nodes('candidate-fresh-court'), 'Avery’s court.')
    screenshot('candidate-fresh-court')
    fresh_state = json.loads(prefs()['state'].text)
    assert fresh_state['playerName'] == 'Avery' and len(fresh_state['slots']) == 6
    report = dict(input_apk_sha256=hashlib.sha256(Path(args.apk).read_bytes()).hexdigest(),
                  version_code=packaged_version, full_history_navigation=True, large_type_drill=True, large_type_scorekeeping=True, fresh_onboarding=True, empty_progress=True, blank_name=True, long_name=True,
                  completed_workout=True, scorekeeping_and_undo=True, system_back=True,
                  receipt_next_drill_matches_continue=True,
                  update_retains_saved_state=True, primary_on_first_viewport=True, saved_name_visible=True, immediate_preference_selection=True, result_recorded_once=True,
                  successes=15, attempts=15, xp_awarded=50, relaunch_persistence=True,
                  compact_capture=True, large_type_capture=True)
    (OUT / 'candidate-result.json').write_text(json.dumps(report, indent=2))
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
