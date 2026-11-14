"""Real Android before/after captures; never runs against a physical/user device."""
import argparse
import hashlib
import json
import re
import subprocess
import time
from pathlib import Path

import verify_android_daily_flow as ui
from verify_phone_update import saved_values_preserved

BASELINE_SHA256 = '9a5fb589fb59e21bec2c590701379a70264998702f7d7208c0157d6f52363555'
CONFIGS = [('normal', 720, 1600, '1.0'), ('compact', 640, 1440, '1.3'),
           ('large', 640, 1440, '2.0')]
SCREENS = [('court', 'Court'), ('library', 'Train'), ('progress', 'Progress'),
           ('quests', 'Quests'), ('me', 'Me')]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def bounds(node):
    return tuple(map(int, re.findall(r'-?\d+', node.get('bounds', ''))))


def require_qa():
    assert ui.shell('getprop', 'ro.kernel.qemu') == '1', 'Physical devices are forbidden'
    assert ui.adb('emu', 'avd', 'name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA', \
        'Only the dedicated disposable AVD may be seeded'


def await_boot():
    for _ in range(120):
        try:
            if ui.shell('getprop', 'sys.boot_completed', timeout=8) == '1':
                require_qa()
                ui.shell('input', 'keyevent', 'KEYCODE_WAKEUP')
                ui.shell('wm', 'dismiss-keyguard')
                return
        except (RuntimeError, subprocess.TimeoutExpired):
            pass
        time.sleep(3)
    raise RuntimeError('Disposable Android emulator did not boot')


def audit_dock():
    items = ui.nodes()
    dock = ui.tagged(items, 'player-bottom-dock')
    assert dock is not None, 'New bottom dock is missing'
    left, top, right, bottom = bounds(dock)
    assert bottom - top <= 112, 'Bottom dock grew beyond 56dp'
    targets = []
    for label, route in ui.NAV_ROUTES.items():
        node = ui.tagged(items, 'dock-' + route)
        assert node is not None, f'Dock destination missing: {label}'
        l, t, r, b = bounds(node)
        assert r - l >= 96 and b - t >= 96, f'{label}: target smaller than 48dp at density 320'
        assert left <= l < r <= right and top <= t < b <= bottom, f'{label}: clipped dock target'
        assert node.get('enabled') == 'true', f'{label}: disabled destination'
        assert any(label == n.get('content-desc') or label == n.get('text') for n in node.iter('node')), \
            f'{label}: accessible name missing'
        targets.append(dict(label=label, bounds=[l, t, r, b]))
    for i, a in enumerate(targets):
        for b in targets[i + 1:]:
            al, at, ar, ab = a['bounds']
            bl, bt, br, bb = b['bounds']
            assert min(ar, br) <= max(al, bl) or min(ab, bb) <= max(at, bt), 'Dock touch targets overlap'
    return targets


def capture(name):
    ui.nodes(name)
    ui.screenshot(name)


def training_action(normal):
    for _ in range(1 if normal else 12):
        items = ui.nodes()
        for label in ['Start training', 'Resume session', 'Resume Mission']:
            target = ui.find(items, label)
            if target is not None:
                return target
        ui.shell('input', 'swipe', '360', '1180', '360', '460', '250')
    raise AssertionError('Training action is not visible in the first normal viewport or reachable with enlarged text')


def capture_phase(phase, output):
    measurements = []
    for config, width, height, font in CONFIGS:
        ui.OUT = output / phase / config
        ui.OUT.mkdir(parents=True, exist_ok=True)
        ui.shell('wm', 'size', f'{width}x{height}')
        ui.shell('wm', 'density', '320')
        ui.shell('settings', 'put', 'system', 'font_scale', font)
        # Both APKs receive the same existing synthetic fixture in the guarded AVD.
        ui.seed('game-history')
        time.sleep(2)
        ui.nav('Court')
        for name, label in SCREENS:
            if phase == 'after':
                # Exercise actual destination changes through the new dock.
                ui.tap(ui.wait_tagged('dock-' + ui.NAV_ROUTES[label]))
                ui.wait_tagged('nav-open-handle')
                assert ui.wait_tagged('dock-' + ui.NAV_ROUTES[label]).get('selected') == 'true'
                measurements.append(dict(configuration=config, screen=name, targets=audit_dock()))
            else:
                ui.nav(label)
                ui.wait_tagged('nav-open-handle')
            capture(name)
        ui.nav('Court')
        ui.tap(ui.wait_tagged('nav-open-handle'))
        ui.wait_tagged('player-navigation-drawer')
        capture('navigation')
        ui.tap(ui.wait_tagged('nav-close-handle'))
        ui.wait_tagged('nav-open-handle')
        ui.tap(training_action(config == 'normal'))
        time.sleep(1)
        ui.require(ui.nodes(), 'Five-Spot Form Shooting')
        capture('training')
        ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    return measurements


def compare(output):
    from PIL import Image, ImageChops, ImageDraw, ImageStat
    metrics = []
    for config, _, _, _ in CONFIGS:
        for screen in [name for name, _ in SCREENS] + ['navigation', 'training']:
            before = Image.open(output / 'before' / config / f'{screen}.png').convert('RGB')
            after = Image.open(output / 'after' / config / f'{screen}.png').convert('RGB')
            assert before.size == after.size, 'Before/after device settings differ'
            width, height = before.size
            # Exclude status bar, most of the header and the dock: detect body changes, not just navigation/clock.
            region = (0, int(height * .18), width, int(height * .85))
            diff = ImageChops.difference(before.crop(region), after.crop(region))
            histogram = diff.convert('L').histogram()
            changed = sum(histogram[16:]) / sum(histogram)
            mean = sum(ImageStat.Stat(diff).mean) / 3
            metrics.append(dict(configuration=config, screen=screen, body_changed_fraction=round(changed, 4),
                                body_mean_rgb_difference=round(mean, 3)))
            assert changed >= .08 and mean >= 3, f'{config}/{screen}: insufficient body-level visual difference'
            if config == 'normal':
                sheet = Image.new('RGB', (width * 2, height + 64), '#08090B')
                draw = ImageDraw.Draw(sheet)
                draw.text((24, 20), 'BEFORE: released v21', fill='white')
                draw.text((width + 24, 20), 'AFTER: native premium redesign', fill='#FF650E')
                sheet.paste(before, (0, 64))
                sheet.paste(after, (width, 64))
                sheet.save(output / f'comparison-{screen}.jpg', quality=94)
    return metrics


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--candidate', type=Path, required=True)
    parser.add_argument('--output', type=Path, default=Path('artifacts/premium-redesign'))
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    await_boot()
    assert sha(args.baseline) == BASELINE_SHA256, 'The baseline is not the verified released v21'
    assert sha(args.candidate) != BASELINE_SHA256, 'Candidate is still the unchanged v21 APK'
    assert 'Success' in ui.adb('install', str(args.baseline.resolve()), timeout=180).decode()
    capture_phase('before', args.output)
    ui.shell('am', 'force-stop', ui.PACKAGE)
    prefs_path = 'shared_prefs/court_quest_pro.xml'
    original = ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat', prefs_path)
    before = json.loads(ui.prefs()['state'].text)
    assert 'Success' in ui.adb('install', '-r', str(args.candidate.resolve()), timeout=180).decode()
    assert original == ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat', prefs_path), 'Upgrade changed existing preferences'
    ui.open_app()
    assert saved_values_preserved(before, json.loads(ui.prefs()['state'].text)), 'Redesign changed existing player data'
    targets = capture_phase('after', args.output)
    report = dict(baseline_apk_sha256=BASELINE_SHA256, candidate_apk_sha256=sha(args.candidate),
                  baseline_commit='de7127db7f9193630222cde505aa15d7ece99f2b',
                  candidate_commit=subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip(),
                  real_android_emulator=True, saved_data_preserved=True, dock_measurements=targets,
                  body_comparisons=compare(args.output), published_apk=False,
                  visual_owner_acceptance='PENDING')
    (args.output / 'visual-verification.json').write_text(json.dumps(report, indent=2) + '\n')
    print('Real before/after Android captures and dock measurements passed; no release published', flush=True)


if __name__ == '__main__':
    main()
