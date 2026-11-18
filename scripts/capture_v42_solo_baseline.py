"""Matched v41/candidate solo-player captures and focused behavior verification."""
import argparse
import hashlib
import json
import re
import time
from pathlib import Path
from xml.etree import ElementTree as ET
import verify_android_daily_flow as ui

CONFIGS = [('normal',720,1600,'1.0'),('compact',640,1440,'1.3'),('large',640,1440,'2.0')]
DESTINATIONS = ['Court','Train','Quests','Progress','Me']

def capture(name):
    ui.nodes(name)
    ui.screenshot(name)

def bounds(node):
    return list(map(int, re.findall(r'-?\d+', node.get('bounds', ''))))

def enter_court():
    # v41 opens a welcome on every cold launch, even after fixture seeding.
    # The released APK predates the separate guest CTA in canonical source.
    target = ui.tagged(ui.nodes(), 'welcome-get-started')
    ui.tap(target if target is not None else ui.scroll_to('ENTER COURT', 18))
    try:
        return ui.wait_tagged('player-bottom-dock', attempts=4)
    except AssertionError:
        items = ui.nodes()
        if ui.tagged(items, 'player-bottom-dock') is not None:
            return ui.tagged(items, 'player-bottom-dock')
        retry = ui.tagged(items, 'welcome-get-started')
        assert retry is not None and retry.get('clickable') == 'true' and retry.get('enabled') == 'true', (
            'Welcome changed without opening the player dock; refusing to replay navigation'
        )
        # Retry only while a fresh hierarchy confirms the same transition CTA
        # remains visible and actionable. This is safe and does not replay data.
        ui.tap(retry)
        return ui.wait_tagged('player-bottom-dock', attempts=20)

def dismiss_hint():
    skip = ui.tagged(ui.nodes(), 'tour-skip')
    if skip is not None:
        ui.tap(skip)

def tap_once_then_wait(node, ready_tag):
    """Issue one tap, then wait for the destination; never replay at its new coordinates."""
    ui.tap(node)
    return ui.wait_tagged(ready_tag, attempts=12)

def wait_until(read, predicate, label, timeout=8.0):
    """Wait for persisted state to reach an expected value, with a real deadline."""
    deadline = time.monotonic() + timeout
    latest = read()
    while not predicate(latest) and time.monotonic() < deadline:
        time.sleep(0.1)
        latest = read()
    assert predicate(latest), f'{label} did not persist before timeout: {latest!r}'
    return latest

def without_progression_migration(raw_state):
    state = json.loads(raw_state) if isinstance(raw_state, str) else dict(raw_state)
    state.pop('questSystemActivatedAt', None)
    # v41's DrillResult schema predates this field. Its effective Kotlin
    # default is false, so a v42 Gson rewrite may add the explicit default
    # without changing the saved result's meaning. Keep every other result
    # field and the list order in the preservation comparison.
    for result in state.get('drillResults') or []:
        if isinstance(result, dict):
            result.setdefault('practice', False)
    return state

def scroll_to_tag(tag):
    for _ in range(18):
        items = ui.nodes()
        target = ui.tagged(items, tag)
        regions = [n for n in items if n.get('scrollable') == 'true']
        if target is not None:
            box = bounds(target)
            if len(box) == 4 and box[2] > box[0] and box[3] > box[1]:
                if any((rb := bounds(region))[0] <= box[0] and rb[1] <= box[1] and
                       box[2] <= rb[2] and box[3] <= rb[3] for region in regions):
                    return target
                if not regions:
                    sizes = re.findall(r'(\d+)x(\d+)', ui.shell('wm', 'size'))
                    assert sizes, f'Cannot determine the visible screen for {tag}'
                    width, height = map(int, sizes[-1])
                    dock = ui.tagged(items, 'player-bottom-dock')
                    bottom = min(height - 96, bounds(dock)[1] if dock is not None else height - 96)
                    if box[0] >= 0 and box[2] <= width and box[1] >= 48 and box[3] <= bottom:
                        return target
                    raise AssertionError(f'Control {tag} is outside the safe screen and has no scroll viewport: {box}')
        if not regions:
            raise AssertionError(f'No visible control or scroll viewport for {tag}')
        region = max(regions, key=lambda n: (bounds(n)[2]-bounds(n)[0])*(bounds(n)[3]-bounds(n)[1]))
        left, top, right, bottom = bounds(region)
        x = (left+right)//2
        height = bottom-top
        ui.shell('input','swipe',str(x),str(top+height*4//5),str(x),str(top+height//4),'350')
        time.sleep(.5)
    last = ui.tagged(ui.nodes(), tag)
    raise AssertionError(f'Control {tag} did not fit inside its scroll viewport: '
                         f'{bounds(last) if last is not None else "not exported"}')

def scroll_to_tag_top(tag):
    """Return an above-the-fold control to view after scrolling deeper into a list."""
    for _ in range(18):
        items = ui.nodes()
        target = ui.tagged(items, tag)
        regions = [node for node in items if node.get('scrollable') == 'true']
        if target is not None and regions:
            box = bounds(target)
            if any((rb := bounds(region))[0] <= box[0] and rb[1] <= box[1] and
                   box[2] <= rb[2] and box[3] <= rb[3] for region in regions):
                return target
        if not regions:
            raise AssertionError(f'No visible scroll viewport can reach {tag}')
        region = max(regions, key=lambda node: (bounds(node)[2]-bounds(node)[0]) *
                     (bounds(node)[3]-bounds(node)[1]))
        left, top, right, bottom = bounds(region)
        x = (left + right) // 2
        height = bottom - top
        # Moving the finger downward returns the list toward its beginning.
        ui.shell('input', 'swipe', str(x), str(top + height // 4),
                 str(x), str(top + height * 3 // 4), '350')
        time.sleep(.5)
    last = ui.tagged(ui.nodes(), tag)
    raise AssertionError(f'Control {tag} did not return to its scroll viewport: '
                         f'{bounds(last) if last is not None else "not exported"}')

def visible_progress_metric_nodes(tag):
    scroll_to_tag(tag)
    return ui.nodes()

def open_progress_from_receipt():
    ui.tap(ui.scroll_to('View Progress', 18))
    ui.wait_tagged('player-bottom-dock', attempts=20)

def assert_dock():
    items = ui.nodes()
    dock = ui.wait_tagged('player-bottom-dock')
    assert bounds(dock)[3] - bounds(dock)[1] <= 120, 'Dock exceeds 60dp'
    boxes = []
    for route in ui.NAV_ROUTES.values():
        node = ui.tagged(items, 'dock-' + route)
        assert node is not None, route
        box = bounds(node)
        assert box[2]-box[0] >= 96 and box[3]-box[1] >= 96, (route, box)
        boxes.append(box)
    assert len({box[1] for box in boxes}) == 1, 'Five tabs must remain one row'
    for first, second in zip(sorted(boxes), sorted(boxes)[1:]):
        assert first[2] <= second[0], 'Dock hit targets overlap'

def set_tour_complete(value):
    root = ET.fromstring(ui.adb('exec-out', 'run-as', ui.PACKAGE, 'cat',
                               'shared_prefs/court_quest_pro.xml'))
    for node in list(root):
        if node.get('name') == 'navigation_tour_complete':
            root.remove(node)
    ET.SubElement(root, 'boolean', name='navigation_tour_complete', value=str(value).lower())
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.shell(f'run-as {ui.PACKAGE} sh -c "cat > shared_prefs/court_quest_pro.xml"',
             data=ET.tostring(root, encoding='utf-8'))
    ui.open_app()
    enter_court()

def verify_candidate(config):
    assert_dock()
    # The optional introduction reserves space; it cannot overlap the scroll viewport.
    hint = ui.wait_tagged('navigation-tour')
    items = ui.nodes()
    regions = [bounds(n) for n in items if n.get('scrollable') == 'true']
    assert regions and max(box[3] for box in regions) <= bounds(hint)[1], 'Hint overlays content'
    if config == 'normal':
        assert bounds(hint)[3] - bounds(hint)[1] <= 160, 'Hint exceeds 80dp'
    for tag in ['tour-next', 'tour-skip']:
        box = bounds(ui.wait_tagged(tag))
        assert box[2]-box[0] >= 96 and box[3]-box[1] >= 96, tag
    initial = ui.prefs()['state'].text
    # Finish all five hints and test previous navigation, without mutating saved player data.
    for index, route in enumerate(['home', 'library', 'quests', 'career', 'me']):
        assert ui.wait_tagged('dock-'+route).get('selected') == 'true'
        if index == 1:
            ui.tap(ui.wait_tagged('tour-back'))
            assert ui.wait_tagged('dock-home').get('selected') == 'true'
            ui.tap(ui.wait_tagged('tour-next'))
        ui.tap(ui.wait_tagged('tour-next'))
    assert ui.tagged(ui.nodes(), 'navigation-tour') is None
    assert ui.prefs()['navigation_tour_complete'].get('value') == 'true'
    assert ui.prefs()['state'].text == initial, 'Navigation hints mutated player data'
    ui.navigate('Court')
    if config == 'normal':
        ui.wait_tagged('court-todays-focus')
    capture('court-no-hint')
    ui.navigate('Train')
    start = ui.wait_tagged('train-start-training')
    start_box, dock_box = bounds(start), bounds(ui.wait_tagged('player-bottom-dock'))
    assert start_box[3] <= dock_box[1] and start_box[3]-start_box[1] >= 104, 'Train action is clipped'
    if config == 'large':
        summary = bounds(ui.wait_tagged('train-plan-summary'))
        assert summary[2]-summary[0] >= 480, 'Enlarged plan summary is squeezed beside difficulty'
    ui.tap(start)
    ui.scroll_to('Record successful attempt', 18)
    ui.shell('input','keyevent','KEYCODE_BACK')
    ui.wait_tagged('player-bottom-dock', attempts=10)
    ui.navigate('Train')
    if config != 'large':
        rail = [bounds(ui.wait_tagged('segment-'+key)) for key in ['today','library','custom']]
        assert len({b[1] for b in rail}) == 1, 'Train selector unexpectedly stacked'
    ui.tap(ui.wait_tagged('segment-library'))
    ui.wait_tagged('library-search')
    if config == 'normal':
        first = bounds(ui.wait_tagged('library-drill-s1'))
        dock = bounds(ui.wait_tagged('player-bottom-dock'))
        assert first[3] <= dock[1], 'First built-in drill must fit above the dock'
        assert first[3]-first[1] >= 144, 'First drill row is clipped'
    capture('drill-discovery')
    # All selectors and real built-in detail/artwork remain reachable.
    ui.tap(scroll_to_tag('library-drill-s1'))
    scroll_to_tag('drill-art-s1') if config == 'normal' else None
    capture('drill-detail')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    ui.navigate('Court')
    # Opening and leaving the current workout must preserve attempts and history.
    ui.tap(ui.scroll_to('Resume session', 18))
    prior = next(d for d in json.loads(ui.prefs()['state'].text)['drillDrafts']
                 if d['slotId'] == 'slot-0')
    ui.tap(ui.scroll_to('Record successful attempt', 18))
    state = json.loads(ui.prefs()['state'].text)
    recorded = next(d for d in state['drillDrafts'] if d['slotId'] == 'slot-0')
    assert recorded['successful'] == prior['successful'] + 1
    assert recorded['attempted'] == prior['attempted'] + 1
    capture('active-drill')
    ui.shell('input','keyevent','KEYCODE_BACK')
    ui.wait_tagged('player-bottom-dock', attempts=10)
    ui.tap(ui.scroll_to('Resume session', 18))
    assert json.loads(ui.prefs()['state'].text)['drillDrafts'] == state['drillDrafts']
    capture('resumed-drill')
    ui.shell('input','keyevent','KEYCODE_BACK')
    ui.navigate('Train')
    ui.tap(ui.wait_tagged('segment-custom'))
    ui.tap(ui.scroll_to('Create custom drill', 18))
    ui.wait_tagged('custom-title')
    capture('custom-drill-editor')
    ui.shell('input','keyevent','KEYCODE_BACK')
    ui.navigate('Train')
    ui.tap(ui.wait_tagged('segment-library'))
    ui.tap(ui.scroll_to('Build a Mission', 18))
    capture('mission-builder')
    ui.shell('input','keyevent','KEYCODE_BACK')
    # Restore the identical fixture before the matched screen captures.
    ui.seed('visual-review')
    ui.open_app()
    enter_court()
    dismiss_hint()

def verify_progression_screens(config):
    # Explicitly inspect first-time empty and returning-player states on the disposable AVD.
    ui.seed('empty')
    ui.open_app()
    enter_court()
    dismiss_hint()
    ui.navigate('Progress')
    ui.assert_empty_progress(ui.nodes(), visible_progress_metric_nodes)
    scroll_to_tag('progress-empty')
    assert ui.tagged(ui.nodes(), 'progress-empty') is not None
    capture('progress-empty')
    ui.navigate('Quests')
    assert ui.wait_tagged('quests-daily-checkin') is not None
    assert scroll_to_tag('quests-weekly-consistency') is not None
    capture('quests-empty')
    ui.navigate('Me')
    capture('me-empty')

    ui.seed('visual-review')
    ui.open_app()
    enter_court()
    dismiss_hint()
    ui.navigate('Progress')
    capture('progress-returning')
    scroll_to_tag('progress-milestone-first-saved-drill')
    capture('progress-earned-history-milestone')
    ui.navigate('Quests')
    assert ui.wait_tagged('quests-daily-checkin') is not None
    capture('quests-returning')
    ui.navigate('Me')
    scroll_to_tag('me-achievement-first-saved-drill')
    capture('me-returning')

def verify_drill_batch(config):
    # Run the exact feature journey on the guarded Android device at each text size.
    ui.seed('progression-empty')
    ui.open_app()
    enter_court()
    dismiss_hint()
    assert_dock()
    ui.navigate('Train')
    ui.tap(ui.wait_tagged('segment-library'))
    search = ui.wait_tagged('library-search')
    ui.tap(search)
    ui.shell('input', 'text', 'beginner%sfive')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    ui.tap(scroll_to_tag('library-drill-s1'))
    capture('drill-detail')
    scroll_to_tag('drill-art-s1')
    ui.tap(scroll_to_tag('drill-enlarge-art'))
    ui.wait_tagged('drill-art-viewer')
    ui.tap(ui.wait_tagged('drill-zoom-art'))
    ui.scroll_to('Fit illustration')
    capture('drill-art-zoom')
    ui.tap(ui.wait_tagged('drill-close-art'))
    scroll_to_tag('drill-rep-check-s1')
    ui.shell('input','keyevent','KEYCODE_BACK')
    ui.navigate('Court')

    def state():
        return json.loads(ui.prefs()['state'].text)

    def draft():
        return next(d for d in state()['drillDrafts'] if d['slotId'] == 'slot-0')

    def counts(expected):
        actual = wait_until(
            draft,
            lambda value: (value['successful'], value['attempted']) == expected,
            f'Persisted drill counters {expected}'
        )
        return actual

    ui.tap(ui.scroll_to('Resume session', 18))
    success = ui.wait_tagged('live-record-success')
    hit = bounds(success)
    assert hit[3]-hit[1] >= 128, 'Primary recorder is not fully visible'
    capture('drill-recorder')
    ui.tap(success)
    ui.tap(scroll_to_tag('live-record-miss'))
    counts((1, 2))
    saved_draft = draft()
    ui.shell('am','force-stop',ui.PACKAGE)
    ui.open_app()
    enter_court()
    ui.tap(ui.scroll_to('Resume session', 18))
    assert draft() == saved_draft, 'Cold resume changed the recorder draft'
    ui.tap(scroll_to_tag('live-undo-miss'))
    counts((1, 1))
    ui.tap(scroll_to_tag('live-undo-success'))
    counts((0, 0))
    ui.shell('input','keyevent','KEYCODE_BACK')
    ui.tap(ui.scroll_to('Resume session', 18))
    box = bounds(ui.wait_tagged('live-record-success'))
    x, y = (box[0]+box[2])//2, (box[1]+box[3])//2
    ui.shell('; '.join([f'input tap {x} {y}'] * 15))
    counts((15, 15))
    confirm = scroll_to_tag('live-confirm-target')
    ui.tap(confirm)
    if config == 'normal':
        ui.shell('input','keyevent','KEYCODE_BACK')
        ui.tap(ui.scroll_to('Resume session', 18))
        ui.tap(scroll_to_tag('live-undo-success'))
        counts((14, 14))
        assert not draft()['targetConfirmed']
        assert scroll_to_tag('live-save-results').get('enabled') == 'false'
        ui.shell('input','keyevent','KEYCODE_BACK')
        ui.tap(ui.scroll_to('Resume session', 18))
        ui.tap(ui.wait_tagged('live-record-success'))
        counts((15, 15))
        ui.tap(scroll_to_tag('live-confirm-target'))
    save = scroll_to_tag('live-save-results')
    assert save.get('enabled') == 'true'
    box = bounds(save)
    x, y = (box[0]+box[2])//2, (box[1]+box[3])//2
    # A duplicate physical tap after the route changes can land on the receipt's
    # Continue action. Verify save-once through persisted result count/relaunch,
    # not by sending a second tap into a different screen.
    tap_once_then_wait(save, 'drill-saved-receipt')
    capture('drill-receipt')
    final = state()
    receipts = [r for r in final['drillResults'] if r['slotId'] == 'slot-0']
    assert len(receipts) == 1
    assert (receipts[0]['completed'], receipts[0]['attempted']) == (15, 15)
    assert len(final['drillResults']) == 1 and final['totalXp'] == 75
    daily_key = f"quest_daily_first_saved_drill_v1_{final['activeDate']}"
    assert final['awardedKeys'].count(daily_key) == 1, 'Daily quest reward must be granted exactly once'
    assert final.get('questSystemActivatedAt'), 'Quest activation must persist in the saved state'
    assert final['slots'][0]['complete'] and not final['drillDrafts']
    open_progress_from_receipt()
    ui.navigate('Quests')
    assert scroll_to_tag('quests-daily-checkin-earned') is not None
    capture('quests-daily-earned')
    ui.tap(scroll_to_tag_top('quests-tab-achievements'))
    scroll_to_tag('quests-achievement-first-saved-drill')
    capture('achievement-first-saved-drill')
    ui.navigate('Me')
    scroll_to_tag('me-achievement-first-saved-drill')
    capture('me-first-saved-drill')
    ui.navigate('Progress')
    capture('progress-first-saved-drill')
    ui.shell('am','force-stop',ui.PACKAGE)
    ui.open_app()
    enter_court()
    assert state() == final, 'Relaunch changed the result or awarded duplicate XP'
    if config == 'normal':
        ui.navigate('Train')
        ui.tap(ui.wait_tagged('segment-library'))
        ui.tap(scroll_to_tag('library-drill-s1'))
        ui.tap(scroll_to_tag('drill-start-practice'))
        box = bounds(ui.wait_tagged('live-record-success'))
        x, y = (box[0]+box[2])//2, (box[1]+box[3])//2
        ui.shell('; '.join([f'input tap {x} {y}'] * 15))
        ui.tap(scroll_to_tag('live-confirm-target'))
        box = bounds(scroll_to_tag('live-save-results'))
        x, y = (box[0]+box[2])//2, (box[1]+box[3])//2
        tap_once_then_wait(scroll_to_tag('live-save-results'), 'drill-saved-receipt')
        practice = state()
        assert practice['drillResults'][:-1] == final['drillResults']
        assert practice['drillResults'][-1]['practice']
        assert practice['totalXp'] == final['totalXp'] and practice['slots'] == final['slots']
        assert practice.get('practiceSession') is None
        assert practice['awardedKeys'].count(daily_key) == 1, 'Extra practice duplicated the daily quest award'
        capture('extra-practice-receipt')
        ui.shell('am','force-stop',ui.PACKAGE)
        ui.open_app()
        enter_court()
        assert state() == practice, 'Extra-practice result or quest award changed after restart'

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--apk',required=True,type=Path)
    parser.add_argument('--output',default='artifacts/v42-v41-baseline',type=Path)
    parser.add_argument('--candidate-apk',type=Path)
    parser.add_argument('--drill-batch', action='store_true')
    parser.add_argument('--all-v42-checks', action='store_true')
    args=parser.parse_args()
    assert args.apk.is_file() and args.apk.stat().st_size > 1000000, 'Missing real release APK'
    # Safety: seed() and pm clear operate only on the disposable AVD.
    assert ui.shell('getprop','ro.kernel.qemu') == '1', 'Never run against a physical device'
    assert ui.adb('emu','avd','name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA'
    apk_sha=hashlib.sha256(args.apk.read_bytes()).hexdigest()
    assert 'Success' in ui.adb('install',str(args.apk.resolve()),timeout=180).decode()
    if args.drill_batch:
        assert args.candidate_apk and args.candidate_apk.is_file(), 'Drill batch requires a candidate APK'
        ui.seed('visual-review')
        saved = ui.prefs()['state'].text
        assert 'Success' in ui.adb('install','-r',str(args.candidate_apk.resolve()),timeout=180).decode()
        assert ui.prefs()['state'].text == saved, 'Update changed saved player state'
        for label,width,height,font in CONFIGS:
            ui.OUT = args.output/'drills'/label
            ui.OUT.mkdir(parents=True, exist_ok=True)
            ui.shell('wm','size',f'{width}x{height}')
            ui.shell('wm','density','320')
            ui.shell('settings','put','system','font_scale',font)
            verify_drill_batch(label)
        return
    records=[]
    phases = [('before', args.apk)]
    if args.candidate_apk:
        phases.append(('after', args.candidate_apk))
    for phase, apk in phases:
      if phase == 'after':
        saved = ui.prefs()['state'].text
        assert 'Success' in ui.adb('install','-r',str(apk.resolve()),timeout=180).decode()
        assert ui.prefs()['state'].text == saved, 'Update changed saved player state'
        ui.open_app()
        enter_court()
        restored = ui.prefs()['state'].text
        before_data = without_progression_migration(saved)
        after_data = without_progression_migration(restored)
        if before_data != after_data:
            changed_fields = {
                key: {'before': before_data.get(key, '<missing>'),
                      'after': after_data.get(key, '<missing>')}
                for key in sorted(set(before_data) | set(after_data))
                if before_data.get(key, '<missing>') != after_data.get(key, '<missing>')
            }
            raise AssertionError(
                'Progression migration changed existing player data: ' +
                json.dumps(changed_fields, sort_keys=True)
            )
        assert json.loads(restored).get('questSystemActivatedAt'), \
            'Additive quest activation timestamp was not saved after launch'
        ui.OUT = args.output/'upgrade'
        ui.OUT.mkdir(parents=True, exist_ok=True)
        capture('preserved-after-relaunch')
      for label,width,height,font in CONFIGS:
        ui.OUT=args.output/phase/label
        ui.OUT.mkdir(parents=True,exist_ok=True)
        ui.shell('wm','size',f'{width}x{height}')
        ui.shell('wm','density','320')
        ui.shell('settings','put','system','font_scale',font)
        assert 'Success' in ui.shell('pm','clear',ui.PACKAGE)
        ui.open_app()
        capture('onboarding')
        ui.seed('visual-review')
        ui.open_app()
        enter_court()
        capture('seeded-first-frame')
        capture('walkthrough')
        dismiss_hint()
        for destination in DESTINATIONS:
            try:
                ui.navigate(destination)
            except AssertionError:
                # Preserve hierarchy and screenshot when a release's navigation differs.
                capture('navigation-failure-'+destination.lower())
                raise
            capture(destination.lower())
            records.append({'phase':phase,'configuration':label,'screen':destination,'capture':str(ui.OUT/(destination.lower()+'.png'))})
        if phase == 'after':
            set_tour_complete(False)
            verify_candidate(label)
            verify_progression_screens(label)
            if args.all_v42_checks:
                ui.OUT = args.output/'drills'/label
                ui.OUT.mkdir(parents=True, exist_ok=True)
                verify_drill_batch(label)
        ui.navigate('Court')
        capture('court-return')
        # Do not mutate practice history in this visual-only baseline pass.
    scope = 'all' if args.all_v42_checks else ('drills' if args.drill_batch else 'solo')
    report={'release_tag':'courtquest-phone-update-v41','apk_sha256':apk_sha,
            'reference':'released v41, not unreleased walkthrough fix',
            'guarded_emulator':True,'candidate_captured':bool(args.candidate_apk),
            'verification_scope':scope,
            'owner_visual_approval':'PENDING','screens':records,
            'omitted':(
                'Team/coach/school are intentionally out of scope.'
                if args.all_v42_checks else
                'Team/coach/school are intentionally out of scope; scope-specific drill or progression checks are not run.'
            )}
    args.output.mkdir(parents=True,exist_ok=True)
    (args.output/'baseline-manifest.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)

if __name__=='__main__':
    try:
        main()
    except Exception:
        ui.OUT.mkdir(parents=True, exist_ok=True)
        try:
            capture('failure')
        except Exception as diagnostic_error:
            print(f'Could not capture Android failure: {diagnostic_error}', flush=True)
        raise
