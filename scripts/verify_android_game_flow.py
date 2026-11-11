"""Exercise the real one-on-one UI; baseline captures precede candidate checks."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import time

import verify_android_daily_flow as ui


def state():
    return json.loads(ui.prefs()['state'].text)


def reachable(text, limit=12):
    for _ in range(limit):
        items = ui.nodes()
        node = ui.find(items, text)
        if node is not None:
            return node
        sizes = re.findall(r'(\d+)x(\d+)', ui.shell('wm', 'size'))
        width, height = map(int, sizes[-1])
        ui.shell('input', 'swipe', str(width // 2), str(height - 220),
                 str(width // 2), str(max(100, height // 3)), '300')
        time.sleep(0.4)
    raise AssertionError(f'Control {text!r} is unreachable')


def snap(label):
    ui.nodes(label)
    ui.screenshot(label)


def start_game(phase):
    ui.seed('complete')
    ui.open_app()
    ui.tap(ui.require(ui.nodes(), 'Play 1-on-1'))
    snap(f'{phase}-game-setup')
    fields = [n for n in ui.nodes() if n.get('class') == 'android.widget.EditText']
    assert len(fields) == 2
    ui.tap(fields[1])
    ui.shell('input', 'text', 'Casey')
    snap(f'{phase}-game-keyboard')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    label = 'ENTER THE CHAMPIONSHIP' if phase == 'baseline' else 'Start game'
    # During the RED run, the existing build still has the previous label.
    if phase == 'candidate' and ui.find(ui.nodes(), 'Start game') is None:
        label = 'ENTER THE CHAMPIONSHIP'
    ui.tap(reachable(label))
    snap(f'{phase}-game-score')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('phase', choices=['baseline', 'candidate'])
    parser.add_argument('apk')
    args = parser.parse_args()
    ui.OUT.mkdir(parents=True, exist_ok=True)
    ui.shell('wm', 'size', '720x1600')
    ui.shell('wm', 'density', '320')
    ui.shell('settings', 'put', 'system', 'font_scale', '1.0')
    start_game(args.phase)
    if args.phase == 'baseline':
        ui.shell('input', 'keyevent', 'KEYCODE_BACK')
        return

    for _ in range(5):
        ui.tap(ui.require(ui.nodes(), 'Record point for Jordan'))
    for _ in range(3):
        ui.tap(ui.require(ui.nodes(), 'Record point for Casey'))
    ui.tap(reachable('Undo last point'))
    game = state()
    assert (game['playerScore'], game['opponentScore']) == (5, 2)
    assert game['pointHistory'] == [True] * 5 + [False] * 2
    snap('candidate-game-corrected')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    # Regression: build 4 saves the score but exposes no resume action.
    resume = reachable('Resume 1-on-1')
    snap('candidate-game-resume-court')
    ui.tap(resume)
    assert state()['playerScore'] == 5 and state()['opponentScore'] == 2
    ui.require(ui.nodes(), 'Record point for Jordan')
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.open_app()
    ui.tap(reachable('Resume 1-on-1'))
    assert state()['pointHistory'] == [True] * 5 + [False] * 2
    snap('candidate-game-relaunched')

    for width, height, font, label in [
        (640, 1440, '1.3', 'compact'),
        (640, 1440, '2.0', 'large-type'),
        (1600, 720, '1.0', 'landscape'),
        (1600, 2560, '1.0', 'tablet'),
    ]:
        ui.shell('wm', 'size', f'{width}x{height}')
        ui.shell('settings', 'put', 'system', 'font_scale', font)
        time.sleep(2)
        ui.tap(reachable('Resume 1-on-1')) if ui.find(ui.nodes(), 'Court') else None
        ui.require([reachable('Record point for Jordan')], 'Record point for Jordan')
        reachable('Record point for Casey')
        reachable('Undo last point')
        snap(f'candidate-game-{label}')
    ui.shell('wm', 'size', '720x1600')
    ui.shell('settings', 'put', 'system', 'font_scale', '1.0')
    ui.shell('settings', 'put', 'global', 'animator_duration_scale', '0')
    ui.shell('settings', 'put', 'global', 'transition_animation_scale', '0')
    ui.shell('settings', 'put', 'global', 'window_animation_scale', '0')
    time.sleep(2)
    if ui.find(ui.nodes(), 'Court'):
        ui.tap(reachable('Resume 1-on-1'))
    for _ in range(5):
        ui.tap(reachable('Record point for Jordan'))
    for _ in range(8):
        ui.tap(reachable('Record point for Casey'))
    assert (state()['playerScore'], state()['opponentScore']) == (10, 10)
    reachable('Sudden death')
    snap('candidate-game-sudden-death')
    ui.tap(reachable('Record point for Casey'))
    game = state()
    assert game['championshipDone'] is True
    assert len(game['matches']) == 1
    match = game['matches'][0]
    assert (match['playerScore'], match['opponentScore'], match['winner']) == (10, 11, 'Casey')
    assert game['totalXp'] == 200
    assert match['xp'] == 100
    snap('candidate-game-receipt')
    reachable('View game history')
    ui.tap(reachable('View game history'))
    ui.require(ui.nodes(), 'Game history')
    reachable('Casey won')
    snap('candidate-game-history')
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.open_app()
    game = state()
    assert len(game['matches']) == 1 and game['totalXp'] == 200
    assert ui.find(ui.nodes(), 'Resume 1-on-1') is None
    report = dict(input_apk_sha256=hashlib.sha256(Path(args.apk).read_bytes()).hexdigest(),
                  score_and_undo=True, resume_saved_match=True, resume_after_process_restart=True,
                  sudden_death=True, opponent_win_saved_once=True, actual_match_xp=True,
                  game_history_navigation=True, completed_match_not_resumable=True,
                  compact_controls=True, large_type_controls=True, landscape_controls=True,
                  tablet_controls=True, reduced_motion=True)
    (ui.OUT / 'game-result.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
