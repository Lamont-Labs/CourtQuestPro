"""Capture the v47 failure and exercise its replacement on a disposable Android AVD."""

import argparse
import json
from pathlib import Path
from types import SimpleNamespace

import verify_android_daily_flow as ui
from capture_v42_solo_baseline import bounds, enter_court
from verify_phone_update import compatible, identity, upgrade

BASELINE_SHA = 'ee32c9e2f4c36b75e9e13752c9ff6cfbdb719155324a6f8dc0d6fe3ea9c2c668'
CONFIGS = [('normal', 720, 1600, '1.0'), ('compact', 640, 1200, '1.3'),
           ('large-text', 640, 1080, '2.0')]


def configure(width, height, font_scale):
    ui.shell('wm', 'size', f'{width}x{height}')
    ui.shell('wm', 'density', '320')
    ui.shell('settings', 'put', 'system', 'font_scale', font_scale)


def capture(name):
    items = ui.nodes(name)
    ui.screenshot(name)
    return items


def inspect_step(items, index):
    label, route = list(ui.NAV_ROUTES.items())[index]
    dock = ui.tagged(items, 'player-bottom-dock')
    panel = ui.tagged(items, 'navigation-tour')
    body = ui.tagged(items, 'tour-current-step')
    assert dock is not None and panel is not None and body is not None, 'Tour or dock is missing'
    assert bounds(panel)[3] < bounds(dock)[1], (label, 'overlapping dock', bounds(panel), bounds(dock))
    ui.require(list(body.iter('node')), label)
    ui.require(list(body.iter('node')), f'Step {index + 1} of 5')
    selected = [item for item in ui.NAV_ROUTES.values()
                if ui.tagged(items, 'dock-' + item).get('selected') == 'true']
    assert selected == [route], (label, 'wrong selection', selected)

    actions = {'tour-skip': 'Skip', 'tour-next': 'Finish' if index == 4 else 'Next'}
    if index:
        actions['tour-back'] = 'Back'
    else:
        assert ui.tagged(items, 'tour-back') is None
    buttons = {}
    for tag, text in actions.items():
        button = ui.tagged(items, tag)
        assert button is not None and button.get('enabled') == button.get('clickable') == 'true', tag
        ui.require(list(button.iter('node')), text)
        left, top, right, bottom = bounds(button)
        assert right - left >= 96 and bottom - top >= 96, (tag, bounds(button))
        pl, pt, pr, pb = bounds(panel)
        assert pl <= left < right <= pr and pt <= top < bottom <= pb, (tag, bounds(button), bounds(panel))
        buttons[tag] = button
    return buttons


def start_replay(items):
    ui.tap(ui.tagged(items, 'dock-me'))
    ui.tap(ui.wait_tagged('me-settings-menu', attempts=8))
    ui.tap(ui.wait_tagged('me-start-walkthrough', attempts=8))


def assert_closed(before):
    assert ui.tagged(ui.nodes(), 'navigation-tour') is None
    prefs = ui.prefs()
    assert prefs['navigation_tour_complete'].get('value') == 'true'
    assert json.loads(prefs['state'].text) == before, 'Walkthrough modified saved player data'


def verify_tour(config, width, height, font):
    configure(width, height, font)
    ui.seed('visual-review')
    ui.open_app()
    enter_court()
    ui.wait_tagged('tour-next', attempts=8)
    before = json.loads(ui.prefs()['state'].text)
    for index, label in enumerate(ui.NAV_ROUTES):
        buttons = inspect_step(capture(f'{index + 1:02d}-{label.lower()}'), index)
        if index == 1:
            ui.tap(buttons['tour-back'])
            ui.tap(inspect_step(ui.nodes(), 0)['tour-next'])
            buttons = inspect_step(ui.nodes(), 1)
        ui.tap(buttons['tour-next'])
    assert_closed(before)

    if config == 'normal':
        start_replay(ui.nodes())
        items = capture('replay-court')
        inspect_step(items, 0)
        ui.tap(ui.tagged(items, 'dock-career'))
        inspect_step(ui.nodes(), 3)
        ui.shell('input', 'keyevent', 'KEYCODE_BACK')
        buttons = inspect_step(ui.nodes(), 2)
        ui.tap(buttons['tour-skip'])
        assert_closed(before)
        ui.shell('am', 'force-stop', ui.PACKAGE)
        ui.open_app()
        enter_court()
        assert_closed(before)
    print(f'{config}: five tabs, control bounds, Back, Finish and saved data verified', flush=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--previous', type=Path, required=True)
    parser.add_argument('--candidate', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--instrumentation', type=Path, default=ui.ROOT /
                        'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')
    args = parser.parse_args()
    assert ui.shell('getprop', 'ro.kernel.qemu') == '1', 'Physical devices are excluded'
    assert ui.adb('emu', 'avd', 'name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA'
    tools = ui.SDK / 'build-tools/35.0.1'
    args.output.mkdir(parents=True, exist_ok=True)
    previous = identity(args.previous, tools, args.output, 'v47')
    assert previous['apk_sha256'] == BASELINE_SHA, 'Expected the exact published v47 APK'

    ui.OUT = args.output / 'v47'
    ui.OUT.mkdir(exist_ok=True)
    configure(*CONFIGS[0][1:])
    assert 'Success' in ui.adb('install', '-r', str(args.previous.resolve()), timeout=180).decode()
    ui.seed('visual-review')
    ui.open_app()
    enter_court()
    baseline = capture('01-court')
    panel = ui.tagged(baseline, 'navigation-tour')
    dock = ui.tagged(baseline, 'player-bottom-dock')
    next_button = ui.tagged(baseline, 'tour-next')
    assert panel is not None and dock is not None
    assert next_button is None or bounds(next_button)[3] > bounds(dock)[1], 'Expected the v47 Next overlap'
    print(f'v47 defect reproduced. Dock {bounds(dock)}. Next {bounds(next_button) if next_button is not None else "not visible"}.',
          flush=True)
    if args.candidate is None:
        return

    candidate_version = ui.expected_version_code()
    candidate = identity(args.candidate, tools, args.output, f'v{candidate_version}')
    compatible(previous, candidate, 47, candidate_version)
    upgrade(SimpleNamespace(previous=args.previous, candidate=args.candidate, previous_version=47,
                            candidate_version=candidate_version, output=args.output))
    for config in CONFIGS:
        ui.OUT = args.output / f'v{candidate_version}' / config[0]
        ui.OUT.mkdir(parents=True, exist_ok=True)
        if config[0] == 'large-text':
            ui.shell('settings', 'put', 'global', 'animator_duration_scale', '0')
        verify_tour(*config)

    compose_result = 'NOT_RUN'
    if args.instrumentation.is_file():
        assert 'Success' in ui.adb('install', '-r', str(args.instrumentation.resolve()),
                                   timeout=180).decode()
        results = ui.shell('am', 'instrument', '-w', '-e', 'class',
                           f'{ui.PACKAGE}.NavigationTourTest',
                           f'{ui.PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner',
                           timeout=240)
        (args.output / 'compose-ui-test.txt').write_text(results + '\n')
        assert 'OK (1 test)' in results and 'INSTRUMENTATION_STATUS_CODE: -4' not in results, results
        compose_result = 'PASS'

    report = dict(previous=previous, candidate=candidate, v47_next_overlap_reproduced=True,
                  verified_configurations=[c[0] for c in CONFIGS], actions=['Back', 'Next', 'Skip', 'Finish'],
                  settings_replay=True, direct_dock_selection=True, system_back=True,
                  upgrade_evidence='install-upgrade.json', compose_ui_test=compose_result, visual_review='PENDING')
    (args.output / 'walkthrough-verification.json').write_text(json.dumps(report, indent=2) + '\n')


if __name__ == '__main__':
    main()
