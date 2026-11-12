"""Prove the actual enlarged Save name control is fully visible above the IME."""
from pathlib import Path
import json,hashlib,re,sys,time
import verify_android_daily_flow as ui
from verify_android_mission_flow import click
from verify_android_library_flow import state

def bounds(node):
    return list(map(int,re.findall('[0-9]+',node.get('bounds',''))))

def main():
    apk=Path(sys.argv[1])
    ui.shell('wm','size','640x1440');ui.shell('wm','density','320')
    ui.shell('settings','put','system','font_scale','2.0')
    ui.seed('empty');ui.open_app()
    click('Me');click('Edit profile')
    ui.tap(ui.scroll_to('Jordan',30))
    ui.shell('input','keyevent','KEYCODE_MOVE_END')
    ui.shell('input','text','%sQA')
    button=None
    for _ in range(30):
        items=ui.nodes()
        candidate=ui.find(items,'Save name')
        regions=[n for n in items if n.get('scrollable')=='true' and n.get('package')==ui.PACKAGE]
        if candidate is not None:
            parents=[n for n in items if n.get('clickable')=='true' and any(x is candidate for x in n.iter('node'))]
            if parents and regions:
                parent=min(parents,key=lambda n:(bounds(n)[2]-bounds(n)[0])*(bounds(n)[3]-bounds(n)[1]))
                viewport=max(regions,key=lambda n:(bounds(n)[2]-bounds(n)[0])*(bounds(n)[3]-bounds(n)[1]))
                left,top,right,bottom=bounds(parent);vl,vt,vr,vb=bounds(viewport)
                if bottom-top>=96 and top>=vt and bottom<=vb and parent.get('enabled')=='true':
                    button=parent;break
        assert regions,'Profile must be scrollable above the keyboard'
        region=max(regions,key=lambda n:(bounds(n)[2]-bounds(n)[0])*(bounds(n)[3]-bounds(n)[1]))
        left,top,right,bottom=bounds(region)
        ui.shell('input','swipe',str((left+right)//2),str(top+(bottom-top)*4//5),
                 str((left+right)//2),str(top+(bottom-top)//4),'250')
    assert button is not None,'Full 48dp Save name target must be visible with keyboard open at 200% text'
    ui.nodes('candidate-player-keyboard-full-save');ui.screenshot('candidate-player-keyboard-full-save')
    ui.tap(button)
    assert state()['playerName']=='Jordan QA'
    ui.shell('wm','size','720x1600')
    ui.shell('settings','put','system','font_scale','1.0')
    report=dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),large_type_keyboard_full_save_target=True,
                explicit_keyboard_open_name_save=True)
    (ui.OUT/'keyboard-result.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)

if __name__=='__main__':main()
