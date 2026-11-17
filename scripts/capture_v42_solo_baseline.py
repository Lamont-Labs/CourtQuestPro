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
    ui.wait_tagged('player-bottom-dock', attempts=20)

def dismiss_hint():
    skip = ui.tagged(ui.nodes(), 'tour-skip')
    if skip is not None:
        ui.tap(skip)

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
    if config != 'large':
        rail = [bounds(ui.wait_tagged('segment-'+key)) for key in ['today','library','custom']]
        assert len({b[1] for b in rail}) == 1, 'Train selector unexpectedly stacked'
    ui.tap(ui.wait_tagged('segment-library'))
    ui.wait_tagged('library-search')
    capture('drill-discovery')
    # All selectors and real built-in detail/artwork remain reachable.
    ui.tap(ui.scroll_to('View Five-Spot Form Shooting', 18))
    ui.wait_tagged('drill-art-s1', attempts=10) if config == 'normal' else None
    capture('drill-detail')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    ui.navigate('Court')
    # Opening and leaving the current workout must preserve attempts and history.
    ui.tap(ui.scroll_to('Resume session', 18))
    ui.require(ui.nodes(), 'Record successful attempt')
    ui.tap(ui.scroll_to('Record successful attempt', 18))
    state = json.loads(ui.prefs()['state'].text)
    assert any(d['successful'] >= 1 for d in state.get('drillDrafts', []))
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
    ui.tap(ui.scroll_to('Mission Builder', 18))
    capture('mission-builder')
    ui.shell('input','keyevent','KEYCODE_BACK')
    # Restore the identical fixture before the matched screen captures.
    ui.seed('visual-review')
    ui.open_app()
    enter_court()
    dismiss_hint()

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--apk',required=True,type=Path)
    parser.add_argument('--output',default='artifacts/v42-v41-baseline',type=Path)
    parser.add_argument('--candidate-apk',type=Path)
    args=parser.parse_args()
    assert args.apk.is_file() and args.apk.stat().st_size > 1000000, 'Missing real release APK'
    # Safety: seed() and pm clear operate only on the disposable AVD.
    assert ui.shell('getprop','ro.kernel.qemu') == '1', 'Never run against a physical device'
    assert ui.adb('emu','avd','name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA'
    apk_sha=hashlib.sha256(args.apk.read_bytes()).hexdigest()
    assert 'Success' in ui.adb('install',str(args.apk.resolve()),timeout=180).decode()
    records=[]
    phases = [('before', args.apk)]
    if args.candidate_apk:
        phases.append(('after', args.candidate_apk))
    for phase, apk in phases:
      if phase == 'after':
        saved = ui.prefs()['state'].text
        assert 'Success' in ui.adb('install','-r',str(apk.resolve()),timeout=180).decode()
        assert ui.prefs()['state'].text == saved, 'Update changed saved player state'
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
        if phase == 'after':
            verify_candidate(label)
        else:
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
        ui.navigate('Court')
        capture('court-return')
        # Do not mutate practice history in this visual-only baseline pass.
    report={'release_tag':'courtquest-phone-update-v41','apk_sha256':apk_sha,
            'reference':'released v41, not unreleased walkthrough fix',
            'guarded_emulator':True,'candidate_captured':bool(args.candidate_apk),
            'owner_visual_approval':'PENDING','screens':records,
            'omitted':'Team/coach/school; deeper drill/receipt journeys pending dedicated fixture'}
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
