"""CQ-PLAYER-002 on the disposable CourtQuest_PremiumQA emulator only."""
from pathlib import Path
import hashlib,json,sys,time
import verify_android_daily_flow as ui
from verify_android_library_flow import state,seed_state,snap,confirm_target
import re


def top():
    items=ui.nodes()
    regions=[n for n in items if n.get('scrollable')=='true']
    if not regions:return
    def bounds(n):return list(map(int,re.findall(r'\d+',n.get('bounds'))))
    n=max(regions,key=lambda n:(bounds(n)[2]-bounds(n)[0])*(bounds(n)[3]-bounds(n)[1]))
    left,t,right,b=bounds(n);x=(left+right)//2
    for _ in range(16):ui.shell('input','swipe',str(x),str(t+(b-t)//4),str(x),str(t+(b-t)*4//5),'100')

def click(text):
    if text in ui.NAV_ROUTES:
        ui.navigate(text)
        return
    found=ui.find(ui.nodes(),text)
    if found is None:
        top();found=ui.scroll_to(text,40)
    ui.tap(found)

def tagged(name):
    top()
    for _ in range(35):
        items=ui.nodes()
        for n in items:
            if n.get('resource-id','').endswith(name):
                bounds=list(map(int,re.findall(r'\d+',n.get('bounds',''))))
                if len(bounds)==4 and bounds[2]>bounds[0] and bounds[3]>bounds[1]:return n
        ui.shell('input','swipe','320','1100','320','300','200')
    raise AssertionError(name)

def main():
    apk=Path(sys.argv[1]).resolve()
    assert ui.shell('getprop','ro.kernel.qemu')=='1'
    avd=ui.adb('emu','avd','name').decode().splitlines()[0].strip()
    assert avd=='CourtQuest_PremiumQA', avd
    ui.OUT.mkdir(parents=True,exist_ok=True)
    ui.seed('empty')
    fixture=state();fixture['drillDrafts']=[];seed_state(fixture)
    click('Build a Mission');snap('mission-builder-empty')
    click('Add Defensive Slide Intervals')
    click('Add Stationary Crossover Control')
    click('Move Stationary Crossover Control up')
    assert [s['drill']['id'] for s in state()['missionDraft']['steps']]==['b1','d1']
    click('Move Stationary Crossover Control down')
    click('Remove Stationary Crossover Control')
    click('Add Mirror Footwork')
    click('Save Mission')
    version=state()['missionVersions'][-1]
    assert [s['drill']['id'] for s in version['steps']]==['d1','d3']
    snap('mission-saved-definition')
    ui.shell('am','force-stop',ui.PACKAGE);ui.open_app()
    click('Build a Mission');assert state()['missionDraft']['steps']==version['steps']
    click('Start Mission')
    click('Record successful attempt');click('Record successful attempt')
    ui.shell('am','force-stop',ui.PACKAGE);ui.open_app()
    click('Resume Mission')
    assert state()['missionSessions'][-1]['drafts'][0]['successful']==2
    active_fixture=state()
    snap('mission-resumed-recording')
    for _ in range(2):click('Record successful attempt')
    click('Record missed / other attempt');confirm_target();click('Save results')
    snap('mission-drill-receipt')
    assert len(state()['drillResults'])==1
    click('Next Mission drill');ui.require(ui.nodes(),'Mirror Footwork')
    for _ in range(4):click('Record successful attempt')
    confirm_target();click('Save results')
    click('Mission summary');ui.require(ui.nodes(),'Mission complete.');snap('mission-completion')
    saved=state();assert len(saved['missionReceipts'])==1 and len(saved['drillResults'])==2 and saved['totalXp']==0
    assert saved['drillResults'][0]['missionVersionId']==version['id']
    ui.shell('am','force-stop',ui.PACKAGE);ui.open_app()
    click('Progress');click('History');click('View Mission receipt');snap('mission-progress-receipt')
    ui.shell('input','keyevent','KEYCODE_BACK');top();ui.require(ui.nodes(),'Your work, recorded.')
    click('View Mission receipt')
    assert len(state()['drillResults'])==2 and len(state()['missionReceipts'])==1
    click('Back to Court');click('Build a Mission')
    click('Add Stationary Crossover Control');click('Save Mission')
    assert len(state()['missionVersions'])==2
    assert state()['missionVersions'][0]==version
    # A legacy practice cannot be overwritten by Start Mission.
    fixture=state();fixture['practiceSession']=dict(id='protected-practice',drillId='s1',date=fixture['activeDate'],draft=dict(date=fixture['activeDate'],slotId='protected-practice',drillId='s1',successful=2,attempted=3,remainingSeconds=350,targetConfirmed=False));seed_state(fixture)
    click('Build a Mission');click('Start Mission');ui.require(ui.nodes(),'Resume extra practice')
    assert state()['practiceSession']['draft']['successful']==2
    click('Keep building')
    # Normal, compact/130%, critical/200% and keyboard states.
    for scale,label in [('1.0','normal'),('1.3','compact'),('2.0','large-type')]:
        ui.shell('wm','size','640x1440' if scale!='1.0' else '720x1600')
        ui.shell('settings','put','system','font_scale',scale);time.sleep(2)
        snap('mission-'+label)
        click('Save Mission');click('Saved on this device')
        ui.tap(tagged('mission-title'));snap('mission-'+label+'-keyboard')
        ui.shell('input','keyevent','KEYCODE_BACK')
    seed_state(active_fixture);click('Resume Mission');snap('mission-large-type-recorder')
    click('Record successful attempt');assert state()['missionSessions'][-1]['drafts'][0]['successful']==3
    ui.shell('wm','size','720x1600');ui.shell('settings','put','system','font_scale','1.0')
    report=dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),builder_order_remove=True,multi_step_next_drill=True,saved_reopen=True,immutable_revision=True,interrupted_resume=True,exact_once_results=True,version_linked_receipt=True,progress_receipt=True,legacy_practice_protected=True,solo_xp_unchanged=True,normal_compact_large_keyboard_captured=True)
    (ui.OUT/'mission-result.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)

if __name__=='__main__':main()
