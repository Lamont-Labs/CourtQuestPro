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

BASELINE_SHA256 = '5125f01e191d26e03fc779ebd96affa3e9dbd5db2900dcedbc8f8660ac9f63bd'
REFERENCE_FIDELITY_STATUS = 'OWNER_REFERENCE_AVAILABLE; AUTOMATED_PIXEL_MATCH_NOT_CLAIMED'
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

def wait_text(text, attempts=8):
    """am start can return before a cold activity exports its first real controls."""
    for attempt in range(attempts):
        target = ui.find(ui.nodes(), text)
        if target is not None:
            return target
        if attempt + 1 < attempts:
            time.sleep(0.2)
    raise AssertionError(f'Visible onboarding control {text!r} did not become ready')

def audit_live_primary():
    """Normal launch must expose actual recording, not only a clock/save gate."""
    items = ui.nodes()
    viewport = next(n for n in items if n.get('scrollable') == 'true')
    vl, vt, vr, vb = bounds(viewport)
    for tag in ['live-drill-stats', 'live-record-success']:
        node = ui.tagged(items, tag)
        assert node is not None, f'Live first viewport is missing {tag}'
        l, t, r, b = bounds(node)
        assert vl <= l < r <= vr and vt <= t < b <= vb, f'Live first viewport clips {tag}'
        if tag == 'live-record-success':
            assert r - l >= 96 and b - t >= 96, 'Live recording target is smaller than 48dp'
            assert node.get('enabled') == 'true', 'Live recording is disabled before any attempts'

def audit_me_avatar():
    items = ui.nodes()
    jersey = ui.tagged(items, 'me-jersey-hero')
    avatar = ui.tagged(items, 'me-neutral-avatar')
    assert jersey is not None and avatar is not None, 'Profile identity artwork is missing'
    left, top, right, bottom = bounds(jersey)
    cx, cy = (left + right) / 2, (top + bottom) / 2
    radius = min(right - left, bottom - top) / 2
    l, t, r, b = bounds(avatar)
    assert r > l and b > t
    assert all((x - cx) ** 2 + (y - cy) ** 2 < radius ** 2
               for x in (l, r) for y in (t, b)), 'Jersey circle clips the neutral avatar badge'

def audit_unrecorded_save():
    # TextView children may remain enabled inside a disabled Compose button.
    # The tagged control owns the actionable disabled semantics.
    button = ui.tagged(ui.nodes(), 'live-save-results')
    assert button is not None, 'Save results button is missing'
    assert button.get('enabled') == 'false', \
        'Save became enabled without the required attempts and explicit confirmation'

def audit_large_heading(tag):
    node = ui.wait_tagged(tag)
    l, t, r, b = bounds(node)
    assert r - l >= 400 and b > t, f'Large-text heading is confined to a narrow column: {tag}'

def audit_home_shortcuts():
    shortcuts = []
    for label in ['Train', 'Drills', 'Progress', 'Quests', 'Games', 'Team']:
        node = ui.wait_tagged(f'court-shortcut-{label.lower()}')
        assert node.get('clickable') == 'true' and node.get('enabled') == 'true', \
            f'{label}: shortcut is not actionable'
        # Compose may export the accessible name on a semantic child rather than
        # on the tagged, clickable wrapper. Keep the name within this target.
        assert any(label == child.get('content-desc') or label == child.get('text')
                   for child in node.iter('node')), f'{label}: shortcut label is not accessible'
        l, t, r, b = bounds(node)
        assert r - l >= 96 and b - t >= 96, f'{label}: shortcut target is smaller than 48dp'
        shortcuts.append((l, t, r, b))
    focus = ui.tagged(ui.nodes(), 'court-todays-focus')
    assert focus is not None, 'Today’s Focus action is missing'
    _, focus_top, _, focus_bottom = bounds(focus)
    assert focus_top > max(box[3] for box in shortcuts), \
        'Today’s Focus action must follow the six reference shortcut tiles'
    viewports = [n for n in ui.nodes() if n.get('scrollable') == 'true']
    assert viewports, 'Court must expose a scrollable content viewport'
    viewport = max(viewports, key=lambda n: (bounds(n)[2] - bounds(n)[0]) *
                   (bounds(n)[3] - bounds(n)[1]))
    assert focus_bottom <= bounds(viewport)[3], \
        'Today’s Focus action must be visible in the initial Court viewport'
    ui.tap(ui.wait_tagged('court-shortcut-drills'))
    assert ui.wait_tagged('train-library-content') is not None, \
        'The Drills shortcut must open the drill catalog, not only My Plan'
    ui.navigate('Court')
    train_route = ui.NAV_ROUTES['Train']
    ui.tap(ui.wait_tagged(f'dock-{train_route}'))
    assert ui.wait_tagged('train-plan-content') is not None, \
        'The Train destination must open My Plan after visiting Drills'


def audit_large_progress_picker():
    picker = ui.wait_tagged('progress-section-picker')
    l, t, r, b = bounds(picker)
    assert r - l >= 400 and b - t >= 96, 'Large-text Progress picker is cramped'
    ui.tap(picker)
    items = ui.nodes()
    for label in ['overview', 'skills', 'history']:
        node = ui.tagged(items, 'segment-' + label)
        assert node is not None and node.get('enabled') == 'true', f'Progress option missing: {label}'
        l, t, r, b = bounds(node)
        assert r - l >= 400 and b - t >= 96 and 0 <= t < b <= 1440, \
            f'Progress option is clipped or below 48dp: {label}'
    capture('progress-view-menu-large')
    ui.tap(ui.tagged(items, 'segment-overview'))


def audit_large_library():
    # At 200% text, use full-width controls rather than splitting single words.
    for label in ['today', 'library', 'custom']:
        audit_large_controls(['segment-' + label], f'library-{label}-large')
    # The new default is My Plan. Discovery actions belong to Library.
    audit_large_controls(['segment-library'], 'library-selected-control-large')
    ui.tap(ui.wait_tagged('segment-library'))
    audit_large_controls(['library-create', 'library-build-mission'], 'library-actions-large', 104)


def audit_large_controls(tags, screenshot_name, minimum_height=96):
    for attempt in range(16):
        items = ui.nodes()
        controls = [ui.tagged(items, tag) for tag in tags]
        scrollables = [node for node in items if node.get('scrollable') == 'true']
        assert scrollables, 'Large-text controls need a scrollable native content area'
        viewport = bounds(max(scrollables, key=lambda n: (bounds(n)[2] - bounds(n)[0]) *
                              (bounds(n)[3] - bounds(n)[1])))
        if all(node is not None and bounds(node)[2] - bounds(node)[0] >= 400
               and bounds(node)[3] - bounds(node)[1] >= minimum_height
               and viewport[1] + 8 < bounds(node)[1] < bounds(node)[3] < viewport[3] - 8
               for node in controls):
            capture(screenshot_name)
            return
        start, end = ('1100', '700') if attempt < 8 else ('400', '950')
        ui.shell('input', 'swipe', '320', start, '320', end, '300')
    raise AssertionError(f'Full-width large-text controls were not reachable without clipped targets: {tags}')


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
        # Only the named disposable AVD can be cleared. No real phone is accessed.
        require_qa()
        assert 'Success' in ui.shell('pm', 'clear', ui.PACKAGE)
        ui.open_app()
        wait_text('Get Started' if phase == 'after' else 'CONTINUE')
        if phase == 'after' and config == 'large':
            audit_large_heading('onboarding-brand-name')
        capture('onboarding')
        # Both APKs receive the same existing synthetic fixture in the guarded AVD.
        ui.seed('visual-review')
        # Seeding deliberately force-stops the package; launch before querying its UI.
        ui.open_app()
        ui.wait_tagged('player-bottom-dock' if phase == 'after' else 'nav-open-handle')
        ui.navigate('Court')
        for name, label in SCREENS:
            if phase == 'after':
                # Exercise actual destination changes through the new dock.
                ui.tap(ui.wait_tagged('dock-' + ui.NAV_ROUTES[label]))
                ui.wait_tagged('player-bottom-dock')
                assert ui.wait_tagged('dock-' + ui.NAV_ROUTES[label]).get('selected') == 'true'
                measurements.append(dict(configuration=config, screen=name, targets=audit_dock()))
            else:
                ui.navigate(label)
                ui.wait_tagged('nav-open-handle')
            capture(name)
            if phase == 'after' and config == 'normal' and name == 'court':
                audit_home_shortcuts()
                ui.navigate('Court')
            if phase == 'after' and config == 'large' and name == 'court':
                audit_large_controls(['court-athlete-details'], 'court-player-details-large', 1)
            if phase == 'after' and name == 'me':
                audit_me_avatar()
            if phase == 'after' and config == 'large' and name == 'library':
                audit_large_library()
            if phase == 'after' and config == 'large' and name == 'progress':
                audit_large_progress_picker()
                audit_large_controls(['progress-activity-heading'], 'progress-activity-heading-large', 1)
                audit_large_controls(['progress-record-sessions'], 'progress-record-large')
        ui.navigate('Court')
        if phase == 'after':
            ui.wait_tagged('player-bottom-dock')
            assert ui.tagged(ui.nodes(), 'nav-open-handle') is None, 'Retired side handle is visible'
            capture('navigation')
        else:
            ui.tap(ui.wait_tagged('nav-open-handle'))
            ui.wait_tagged('player-navigation-drawer')
            capture('navigation')
            ui.tap(ui.wait_tagged('nav-close-handle'))
            ui.wait_tagged('nav-open-handle')
        ui.tap(training_action(config == 'normal'))
        time.sleep(1)
        ui.require(ui.nodes(), 'Five-Spot Form Shooting')
        capture('training')
        if phase == 'after':
            if config == 'normal':
                audit_live_primary()
            ui.scroll_to('Record successful attempt')
            capture('training-actions')
            ui.scroll_to('Save results')
            audit_unrecorded_save()
        ui.shell('input', 'keyevent', 'KEYCODE_BACK')
        # Team did not exist on the baseline; label its actual Me entry point.
        if phase == 'after':
            ui.navigate('Court')
            ui.tap(ui.scroll_to('Team'))
            ui.scroll_to('NO TEAM CONFIGURED')
            assert ui.tagged(ui.nodes(), 'team-screen') is not None
            team_before = json.loads(ui.prefs()['state'].text)
            capture('team')
            assert team_before == json.loads(ui.prefs()['state'].text), 'Viewing empty Team mutated player data'
            ui.shell('input', 'keyevent', 'KEYCODE_BACK')
            ui.navigate('Me')
            ui.tap(ui.wait_tagged('me-settings-menu'))
            capture('me-menu')
            ui.tap(ui.wait_tagged('me-team'))
            ui.scroll_to('NO TEAM CONFIGURED')
            assert ui.tagged(ui.nodes(), 'team-screen') is not None, 'Me menu did not open Team'
        else:
            ui.navigate('Me')
            capture('team')
    return measurements


def compare(output):
    from PIL import Image, ImageChops, ImageDraw, ImageStat
    metrics = []
    for config, _, _, _ in CONFIGS:
        for screen in [name for name, _ in SCREENS] + ['navigation', 'training', 'onboarding', 'team']:
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
                baseline_label = 'BEFORE: Me (Team did not exist)' if screen == 'team' else 'BEFORE: previous verified native UI'
                draw.text((24, 20), baseline_label, fill='white')
                draw.text((width + 24, 20), 'AFTER: cinematic native redesign', fill='#FF650E')
                sheet.paste(before, (0, 64))
                sheet.paste(after, (width, 64))
                sheet.save(output / f'comparison-{screen}.jpg', quality=94)
    return metrics


def capture_checked_phase(phase, output):
    try:
        return capture_phase(phase, output)
    except Exception:
        try:
            # Do not collect a screen if the disposable-device guard failed.
            require_qa()
            ui.OUT.mkdir(parents=True, exist_ok=True)
            ui.nodes('visual-failure')
            ui.screenshot('visual-failure')
        except Exception as evidence_error:
            print(f'Validated QA failure evidence unavailable: {evidence_error}', flush=True)
        raise


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--candidate', type=Path, required=True)
    parser.add_argument('--output', type=Path, default=Path('artifacts/premium-redesign'))
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    await_boot()
    assert sha(args.baseline) == BASELINE_SHA256, 'The baseline is not the previous verified native APK'
    assert sha(args.candidate) != BASELINE_SHA256, 'Candidate is still the unchanged previous native APK'
    assert 'Success' in ui.adb('install', str(args.baseline.resolve()), timeout=180).decode()
    capture_checked_phase('before', args.output)
    ui.shell('am', 'force-stop', ui.PACKAGE)
    prefs_path = 'shared_prefs/court_quest_pro.xml'
    original = ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat', prefs_path)
    before = json.loads(ui.prefs()['state'].text)
    assert 'Success' in ui.adb('install', '-r', str(args.candidate.resolve()), timeout=180).decode()
    assert original == ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat', prefs_path), 'Upgrade changed existing preferences'
    ui.open_app()
    assert saved_values_preserved(before, json.loads(ui.prefs()['state'].text)), 'Redesign changed existing player data'
    targets = capture_checked_phase('after', args.output)
    report = dict(baseline_apk_sha256=BASELINE_SHA256, candidate_apk_sha256=sha(args.candidate),
                  baseline_commit='c30876b3ac4bea45967a7a254030df6de084c5d7',
                  candidate_commit=subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip(),
                  real_android_emulator=True, saved_data_preserved=True, dock_measurements=targets,
                  body_comparisons=compare(args.output), published_apk=False,
                  visual_owner_acceptance='PENDING',
                  reference_image_fidelity=REFERENCE_FIDELITY_STATUS,
                  team_baseline='No Team screen existed; captured the real Me entry point',
                  team_empty_state_preserves_save=True)
    (args.output / 'visual-verification.json').write_text(json.dumps(report, indent=2) + '\n')
    print('Real before/after Android captures and dock measurements passed; no release published', flush=True)


if __name__ == '__main__':
    main()
