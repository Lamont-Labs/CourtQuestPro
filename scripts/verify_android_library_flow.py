"""Native custom drill, protected workout and independent-practice acceptance."""
from pathlib import Path
from xml.etree import ElementTree as ET
import hashlib
import json
import re
import time
import verify_android_daily_flow as ui


def state():
    return json.loads(ui.prefs()['state'].text)


def seed_state(value):
    root = ET.Element('map')
    ET.SubElement(root, 'boolean', name='onboarding_complete', value='true')
    ET.SubElement(root, 'string', name='state').text = json.dumps(value)
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.shell(f'run-as {ui.PACKAGE} sh -c "cat > shared_prefs/court_quest_pro.xml"', data=ET.tostring(root, encoding='utf-8'))
    ui.open_app()


def snap(name):
    ui.nodes('candidate-library-' + name)
    ui.screenshot('candidate-library-' + name)


def tagged(name):
    for _ in range(16):
        items = ui.nodes()
        found = next((n for n in items if n.get('resource-id', '').endswith(name)), None)
        if found is not None:
            return next((n for n in found.iter("node") if n.get("class", "").endswith("EditText")), found)
        regions = [n for n in items if n.get('scrollable') == 'true']
        assert regions, f'No scroll region for {name}'
        def bounds(n): return list(map(int, re.findall(r'\d+', n.get('bounds'))))
        region = max(regions, key=lambda n: (bounds(n)[2]-bounds(n)[0])*(bounds(n)[3]-bounds(n)[1]))
        left, top, right, bottom = bounds(region)
        ui.shell('input', 'swipe', str((left+right)//2), str(top+(bottom-top)*4//5),
                 str((left+right)//2), str(top+(bottom-top)//4), '350')
        time.sleep(0.4)
    snap('missing-control')
    raise AssertionError(f'Missing native resource {name}')


def fill(name, text):
    node = tagged(name)
    ui.tap(node)
    ui.shell('input', 'keyevent', 'KEYCODE_MOVE_END')
    ui.shell('input', 'keyevent', *(['KEYCODE_DEL'] * max(1, len(node.get('text', '')))))
    ui.shell('input', 'text', text.replace(' ', '%s'))
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')


def click(text):
    ui.tap(ui.scroll_to(text, 18))


def confirm_target():
    check = next(n for n in ui.nodes() if n.get('checkable') == 'true')
    ui.tap(check)


def main():
    import sys
    apk = Path(sys.argv[1])
    ui.shell('wm', 'size', '720x1600')
    ui.shell('wm', 'density', '320')
    ui.shell('settings', 'put', 'system', 'font_scale', '1.0')
    ui.seed('empty')
    ui.open_app()
    click('Drills')
    snap('home')
    click('Create custom drill')
    click('Save drill')
    ui.require(ui.nodes(), 'Give your drill a name of 1–60 characters.')
    assert not state().get('customDrills')
    fill('custom-title', 'Corner Touch')
    fill('custom-minutes', '4')
    fill('custom-goal', '5')
    fill('custom-instructions', 'Set your feet and make controlled shots from the corner.')
    snap('editor')
    click('Save drill')
    created = state()['customDrills'][-1]['drill']
    old_id = created['id']
    assert created['title'] == 'Corner Touch' and created['minimumSuccesses'] == 5
    snap('detail')
    click('Save to favorites')
    assert old_id in state()['favoriteDrillIds']
    click('Use in daily workout')
    before = state()
    assert before['slots'][0]['id'] == 'slot-0' and before['slots'][0]['drillId'] == old_id

    # Establish one completed and one started slot, then exercise real UI shuffle.
    before['slots'][1]['complete'] = True
    before['drillDrafts'] = [dict(date=before['activeDate'], slotId='slot-2', drillId='f1',
                                successful=1, attempted=1, remainingSeconds=300, targetConfirmed=False)]
    seed_state(before)
    click('Shuffle remaining')
    shuffled = state()
    assert [s['id'] for s in shuffled['slots']] == [s['id'] for s in before['slots']]
    assert shuffled['slots'][1] == before['slots'][1] and shuffled['slots'][2] == before['slots'][2]
    assert shuffled['drillDrafts'] == before['drillDrafts']
    assert any(a['drillId'] != b['drillId'] for a, b in zip(before['slots'], shuffled['slots']))
    snap('shuffle')
    click('Drills')
    fill('library-search', 'Corner Touch')
    click('View Corner Touch')
    click('Use in daily workout')
    assert state()['slots'][0]['drillId'] == old_id
    click('Drills')
    fill('library-search', 'Corner Touch')
    click('View Corner Touch')
    click('Start extra practice')
    ui.require(ui.nodes(), 'Extra practice · Shooting')
    click('Record successful attempt')
    click('Record missed / other attempt')
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    practice_id = state()['practiceSession']['id']
    assert state()['practiceSession']['draft']['successful'] == 1
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.open_app()
    click('Drills')
    fill('library-search', 'Corner Touch')
    click('View Corner Touch')
    click('Edit custom drill')
    fill('custom-title', 'Corner Touch Plus')
    fill('custom-goal', '8')
    click('Save drill')
    revised = state()['customDrills'][-1]['drill']
    assert revised['id'] != old_id and revised['minimumSuccesses'] == 8
    assert state()['slots'][0]['drillId'] == old_id
    assert state()['practiceSession']['drillId'] == old_id
    assert revised['id'] in state()['favoriteDrillIds'] and old_id not in state()['favoriteDrillIds']
    click('Resume current practice')
    for _ in range(4):
        click('Record successful attempt')
    confirm_target()
    snap('practice')
    click('Save results')
    ui.require(ui.nodes(), 'Practice recorded.')
    snap('receipt')
    saved = state()
    result = saved['drillResults'][-1]
    assert result['practice'] is True and result['completed'] == 5 and result['attempted'] == 6
    assert result['slotId'] == practice_id and result['drillSnapshot']['title'] == 'Corner Touch'
    assert saved['totalXp'] == 0 and saved.get('practiceSession') is None
    assert len(saved['drillResults']) == 1
    click('View Progress')
    click('5 successful / 6 attempts')
    snap('progress')

    # Queued old version still uses its original five-action target after editing.
    for slot in saved['slots']:
        slot['complete'] = slot['id'] != 'slot-0'
    saved['drillDrafts'] = []
    seed_state(saved)
    click('Start training')
    ui.require(ui.nodes(), 'Corner Touch')
    for _ in range(5):
        click('Record successful attempt')
    confirm_target()
    click('Save results')
    completed = state()
    assert len(completed['drillResults']) == 2
    assert completed['drillResults'][-1].get('practice', False) is False
    assert completed['drillResults'][-1]['drillSnapshot']['minimumSuccesses'] == 5
    assert completed['totalXp'] == 150 and completed['slots'][0]['complete']
    click('Back to Court')
    click('Drills')
    fill('library-search', 'Corner Touch Plus')
    click('View Corner Touch Plus')
    click('Archive drill')
    click('Archive drill')
    assert all(d['archived'] for d in state()['customDrills'])
    assert len(state()['drillResults']) == 2
    ui.shell('am', 'force-stop', ui.PACKAGE)
    ui.open_app()
    assert len(state()['drillResults']) == 2 and state()['totalXp'] == 150

    for font, label in [('1.3', 'compact'), ('2.0', 'large-type')]:
        ui.shell('wm', 'size', '640x1440')
        ui.shell('settings', 'put', 'system', 'font_scale', font)
        time.sleep(2)
        click('Drills')
        snap(label)
        click('Create custom drill')
        fill('custom-title', 'Keyboard Check')
        ui.tap(tagged('custom-title'))
        ui.require(ui.nodes(), 'Save drill')
        snap(label + '-keyboard')
        ui.shell('input', 'keyevent', 'KEYCODE_BACK')
        ui.shell('input', 'keyevent', 'KEYCODE_BACK')
        click('Discard changes')
    ui.shell('wm', 'size', '720x1600')
    ui.shell('settings', 'put', 'system', 'font_scale', '1.0')
    report = dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(), custom_creation=True,
                  form_validation=True, favorites=True, custom_workout_swap=True, actual_shuffle=True,
                  completed_and_started_protected=True, slot_identity_preserved=True, practice_resume=True,
                  practice_recorded_once=True, custom_target_works=True, revision_preserves_queued_target=True,
                  original_definition_in_history=True, favorite_revision_migration=True, extra_practice_xp_unchanged=True,
                  daily_awards_preserved=True, archival_preserves_history=True, relaunch_persistence=True,
                  compact_capture=True, large_type_capture=True, keyboard_footer_reachable=True)
    (ui.OUT/'library-result.json').write_text(json.dumps(report, indent=2)+'\n')
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
