"""CQ-PLAYER-003: five destinations, local state and predictable Back on the guarded QA emulator."""
from pathlib import Path
import hashlib, json, re, sys, time
import verify_android_daily_flow as ui
from verify_android_library_flow import state, snap
from verify_android_mission_flow import click, top


def back():
    ui.shell('input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(1)


def text_field():
    return next(n for n in ui.nodes() if n.get('class') == 'android.widget.EditText')


def replace_text(value):
    node = text_field()
    ui.tap(node)
    ui.shell('input', 'keyevent', 'KEYCODE_MOVE_END')
    ui.shell('input', 'keyevent', *(['KEYCODE_DEL'] * max(1, len(node.get('text', '')))))
    ui.shell('input', 'text', value.replace(' ', '%s'))
    back()  # IME, not destination


def main():
    apk = Path(sys.argv[1])
    ui.OUT.mkdir(parents=True, exist_ok=True)
    ui.shell('wm', 'size', '720x1600')
    ui.shell('settings', 'put', 'system', 'font_scale', '1.0')
    ui.seed('empty'); ui.open_app()
    original = state()
    for tab, heading in [('Train', 'Train your way.'), ('Quests', "TODAY'S DAILY QUEST"),
                         ('Progress', 'Your work, recorded.'), ('Me', 'Edit profile')]:
        click(tab); ui.require(ui.nodes(), heading); snap('navigation-' + tab.lower())
        back(); ui.require(ui.nodes(), 'Start training')
    assert state() == original, 'Browsing must not award XP or create evidence'
    click('Train'); replace_text('Mirror')
    click('Me'); click('Train')
    assert text_field().get('text') == 'Mirror', 'Library search must survive a tab round trip'
    replace_text('')
    click('Build a Mission'); back(); ui.require(ui.nodes(), 'Train your way.')
    click('Me'); click('Edit profile'); replace_text('Draft Player')
    assert state()['playerName'] == original['playerName'], 'Draft typing must not save identity'
    click('Quests'); click('Me'); click('Edit profile')
    assert text_field().get('text') == 'Draft Player'
    # Android's saved instance state, rather than force-stop, must retain route and text.
    ui.shell('input', 'keyevent', 'KEYCODE_HOME'); time.sleep(2)
    ui.shell('am', 'kill', ui.PACKAGE); time.sleep(1)
    ui.open_app()
    assert text_field().get('text') == 'Draft Player'
    click('Save name'); assert state()['playerName'] == 'Draft Player'
    back(); ui.require(ui.nodes(), 'Draft Player')
    assert state()['totalXp'] == original['totalXp']
    for scale, label, size in [('1.0','normal','720x1600'), ('1.3','compact','640x1440'), ('2.0','large-type','640x1440')]:
        ui.shell('wm', 'size', size)
        ui.shell('settings', 'put', 'system', 'font_scale', scale); time.sleep(2)
        for tab in ['Court','Train','Quests','Progress','Me']:
            click(tab)
            for visible_tab in ['Court','Train','Quests','Progress','Me']:
                ui.require(ui.nodes(), visible_tab)
            snap('navigation-' + label + '-' + tab.lower())
        click('Edit profile')
        ui.tap(ui.scroll_to('Draft Player', 25)); snap('navigation-' + label + '-keyboard')
        back(); back()
    ui.shell('wm', 'size', '720x1600')
    ui.shell('settings', 'put', 'system', 'font_scale', '1.0')
    report = dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),
        five_usable_destinations=True, root_back_to_court=True, builder_back_to_train=True,
        library_tab_state=True, profile_draft_tab_state=True, profile_draft_process_restore=True,
        name_save_explicit=True, navigation_does_not_award_xp=True,
        normal_compact_large_keyboard_captured=True)
    (ui.OUT/'navigation-result.json').write_text(json.dumps(report, indent=2)+'\n')
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
