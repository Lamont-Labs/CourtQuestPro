"""Native geometry regression for the owner-reported oversized player navigation."""
from pathlib import Path
import argparse,json,re,time,hashlib
import verify_android_daily_flow as ui

LABELS=['Court','Train','Quests','Progress','Me']
ROUTES=['home','library','quests','career','me']

def rect(node):
    return list(map(int,re.findall(r'-?\d+',node.get('bounds',''))))

def state():
    return json.loads(ui.prefs()['state'].text)

def tagged(items,tag):
    return ui.tagged(items,tag)

def same_row(boxes):
    centers=[(r[1]+r[3])/2 for r in boxes]
    assert max(centers)-min(centers)<=4,'Player destinations wrapped into more than one row'

def baseline(apk):
    ui.seed();ui.open_app()
    ui.shell('wm','size','720x1600');ui.shell('wm','density','320')
    ui.shell('settings','put','system','font_scale','1.5');time.sleep(2)
    ui.tap(ui.require(ui.nodes(),'Quests'))
    items=ui.nodes('dock-baseline-owner-font');ui.screenshot('dock-baseline-owner-font')
    boxes=[rect(ui.require(items,label)) for label in LABELS]
    try:
        same_row(boxes)
    except AssertionError as e:
        report=dict(baseline_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),old_bar_reproduces_two_rows=True,assertion=str(e),label_bounds=dict(zip(LABELS,boxes)))
        (ui.OUT/'dock-baseline-result.json').write_text(json.dumps(report,indent=2)+'\n')
        print(json.dumps(report),flush=True)
        return
    raise AssertionError('Published baseline did not reproduce the owner-reported two-row navigation')

def candidate(apk):
    # Verify the approved compact edge drawer, not the retired horizontal dock.
    before=state()
    before_raw=ui.prefs()['state'].text
    assert 'Success' in ui.adb('install','-r',str(apk.resolve()),timeout=180).decode()
    assert ui.prefs()['state'].text==before_raw,'Installing the navigation update changed saved data'
    ui.open_app()
    assert state()==before,'Opening the navigation update changed saved data'
    configurations=[('normal',720,1600,'1.0'),('owner-large',720,1600,'1.5'),
                    ('compact',640,1440,'1.3'),('compact-200',640,1440,'2.0'),
                    ('landscape',1280,720,'1.0')]
    measurements=[]
    for name,width,height,font in configurations:
        ui.shell('wm','size',f'{width}x{height}')
        ui.shell('wm','density','320')
        ui.shell('settings','put','system','font_scale',font)
        time.sleep(2)
        for label,route in zip(LABELS,ROUTES):
            # A LazyColumn need not compose all five entries at once in landscape.
            # Every labeled destination must still be reachable and at least 48dp.
            target=ui.drawer_target(label,minimum_px=96)
            left,top,right,bottom=rect(target)
            assert right-left>=96 and bottom-top>=96,(name,label,'48dp target required',target.attrib)
            ui.nodes(f'edge-{name}-{route}-drawer')
            ui.tap(target)
            ui.wait_tagged('nav-open-handle')
            selected=ui.drawer_target(label,minimum_px=96)
            assert selected.get('selected')=='true',(name,label,'selected destination not exposed',selected.attrib)
            closer=ui.wait_tagged('nav-close-handle')
            ui.tap(closer)
            ui.wait_tagged('nav-open-handle')
            measurements.append(dict(configuration=name,selected=label,target_bounds=[left,top,right,bottom]))
        ui.nodes('edge-'+name)
        ui.screenshot('edge-'+name)
    ui.shell('wm','size','720x1600')
    ui.shell('settings','put','system','font_scale','1.0')
    assert state()==before,'Tab browsing altered saved player/training data'
    report=dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),
                upgrade_from_build9=True,saved_data_unchanged=True,
                five_accessible_drawer_destinations=True,drawer_closes_on_selection=True,
                configurations=configurations,measurements=measurements)
    (ui.OUT/'dock-result.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('phase',choices=['baseline','candidate']);p.add_argument('apk')
    a=p.parse_args();ui.OUT.mkdir(parents=True,exist_ok=True)
    try:
        (baseline if a.phase=='baseline' else candidate)(Path(a.apk))
    except Exception:
        ui.nodes('dock-failure')
        ui.screenshot('dock-failure')
        raise
    finally:
        ui.shell('wm','size','720x1600')
        ui.shell('wm','density','320')
        ui.shell('settings','put','system','font_scale','1.0')
