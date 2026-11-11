"""Exercise the real one-on-one UI; baseline captures precede candidate checks."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import time

import verify_android_daily_flow as ui


def state():
    return json.loads(ui.prefs()['state'].text)


def reachable(text, limit=12):
    try:
        # Use actual scrollable bounds, including short landscape viewports.
        return ui.scroll_to(text, limit)
    except AssertionError as error:
        visible = ui.nodes('candidate-game-failure-state')
        ui.screenshot('candidate-game-failure-state')
        raise AssertionError(f'Control {text!r} is unreachable; visible: '
                             f'{[n.get("text") or n.get("content-desc") for n in visible]}') from error


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
    label = 'Start game' if ui.find(ui.nodes(), 'Start game') is not None else 'ENTER THE CHAMPIONSHIP'
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
        if ui.find(ui.nodes(), 'Court') is not None:
            ui.tap(reachable('Resume 1-on-1'))
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
    if ui.find(ui.nodes(), 'Court') is not None:
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
    # Existing per-day awards must not be presented as new XP on a repeat game.
    # Identical display names must not turn an opponent win into a player win.
    ui.tap(reachable('Play 1-on-1'))
    fields = [n for n in ui.nodes() if n.get('class') == 'android.widget.EditText']
    ui.tap(fields[1])
    if fields[1].get('text'):
        ui.shell('input', 'keyevent', 'KEYCODE_MOVE_END')
        ui.shell('input', 'keyevent', *(['KEYCODE_DEL'] * len(fields[1].get('text'))))
    ui.shell('input', 'text', 'Jordan')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    ui.tap(reachable('Start game'))
    for _ in range(11):
        buttons = [n for n in ui.nodes() if n.get('content-desc') == 'Record point for Jordan']
        assert len(buttons) == 2
        ui.tap(buttons[1])
    repeated = state()
    assert len(repeated['matches']) == 2
    assert repeated['matches'][0]['playerScore'] == 0 and repeated['matches'][0]['opponentScore'] == 11
    assert repeated['totalXp'] == 200 and repeated['matches'][0]['xp'] == 0
    snap('candidate-game-repeat-receipt')
    for variant in ('previous-unfinished-game', 'previous-completed-game'):
        ui.seed(variant)
        ui.open_app()
        rolled = state()
        assert rolled['matchPlayer'] == rolled['matchOpponent'] == '', 'Daily reset must clear stale match identity'
        assert rolled['playerScore'] == rolled['opponentScore'] == 0
        assert rolled['pointHistory'] == []
        ui.tap(reachable('Play 1-on-1'))
        assert len([n for n in ui.nodes() if n.get('class') == 'android.widget.EditText']) == 2
    snap('candidate-game-new-day-setup')
    ui.seed('game-history')
    ui.open_app()
    ui.tap(ui.require(ui.nodes(), 'Progress'))
    ui.tap(ui.require(ui.nodes(), 'Games'))
    ui.tap(reachable('View all 8 games'))
    ui.require(ui.nodes(), '8 recorded games')
    reachable('Guest 7')
    snap('candidate-game-full-history')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    reachable('View all 8 games')
    report = dict(input_apk_sha256=hashlib.sha256(Path(args.apk).read_bytes()).hexdigest(),
                  score_and_undo=True, resume_saved_match=True, resume_after_process_restart=True,
                  sudden_death=True, opponent_win_saved_once=True, actual_match_xp=True,
                  game_history_navigation=True, completed_match_not_resumable=True,
                  compact_controls=True, large_type_controls=True, landscape_controls=True,
                  tablet_controls=True, reduced_motion=True, repeat_game_actual_xp=True,
                  identical_names_preserve_winner_side=True, previous_day_matches_do_not_resume=True,
                  full_game_history_navigation=True)
    (ui.OUT / 'game-result.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
