"""Native geometry regression for the owner-reported oversized player navigation."""
from pathlib import Path
import argparse,json,re,time,hashlib
import verify_android_daily_flow as ui

LABELS=['Court','Train','Quests','Progress','Me']
ROUTES=['home','library','quests','career','me']

def rect(node):
    return list(map(int,re.findall('[0-9]+',node.get('bounds',''))))

def state():
    return json.loads(ui.prefs()['state'].text)

def tagged(items,tag):
    return next((n for n in items if n.get('resource-id','').endswith(tag) and len(rect(n))==4 and rect(n)[2]>rect(n)[0] and rect(n)[3]>rect(n)[1]),None)

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
    # Preserve the baseline's actual stored history/profile across Android install -r.
    before=state()
    before_raw=ui.prefs()['state'].text
    assert 'Success' in ui.adb('install','-r',str(apk.resolve()),timeout=180).decode()
    assert ui.prefs()['state'].text==before_raw,'Installing the dock update changed saved data'
    ui.open_app()
    assert state()==before,'Opening the dock update changed saved data'
    configurations=[('normal',720,1600,'1.0'),('owner-large',720,1600,'1.5'),
                    ('compact',640,1440,'1.3'),('compact-200',640,1440,'2.0'),
                    ('landscape',1280,720,'1.0')]
    measurements=[]
    for name,width,height,font in configurations:
        ui.shell('wm','size',f'{width}x{height}')
        ui.shell('settings','put','system','font_scale',font);time.sleep(2)
        for label,route in zip(LABELS,ROUTES):
            items=ui.nodes()
            button=tagged(items,'nav-'+route)
            assert button is not None,(name,label,'missing tab target')
            ui.tap(button)
            items=ui.nodes()
            tabs=[tagged(items,'nav-'+r) for r in ROUTES]
            assert all(tabs),(name,'five accessible targets required')
            boxes=[rect(n) for n in tabs]
            same_row(boxes)
            for box,n,expected in zip(boxes,tabs,LABELS):
                left,top,right,bottom=box
                assert right-left>=95 and bottom-top>=95,(name,expected,'target smaller than 48dp',box)
                assert 0<=left<right<=width and 0<=top<bottom<=height,(name,expected,'offscreen',box)
                assert n.get('content-desc')==expected,(name,expected,'missing accessible name',n.attrib)
            for left,right in zip(boxes,boxes[1:]):
                assert left[2]<=right[0],(name,'overlapping tab targets')
            chosen=[n for n in tabs if n.get('selected')=='true']
            assert len(chosen)==1 and chosen[0].get('content-desc')==label,(name,label,'selected semantics incorrect')
            dock=tagged(items,'player-navigation-dock')
            assert dock is not None,'Dock measurement tag required'
            db=rect(dock)
            assert db[3]-db[1]<=114,(name,'dock exceeds 56dp',db)
            measurements.append(dict(configuration=name,selected=label,dock_bounds=db,tab_bounds=dict(zip(LABELS,boxes))))
        # Last selected tab is Me; each root has been tapped at each text size.
        ui.nodes('dock-'+name);ui.screenshot('dock-'+name)
        ui.shell('input','keyevent','KEYCODE_BACK');time.sleep(0.5)
        ui.require(ui.nodes(),'Start training' if not before.get('drillDrafts') else 'Resume session')
    # Explicit worst-case label and the owner's screenshot destination.
    for font,label,route,name in [('1.5','Quests','quests','owner-quests'),('2.0','Progress','career','progress-200')]:
        ui.shell('wm','size','640x1440')
        ui.shell('settings','put','system','font_scale',font);time.sleep(2)
        ui.tap(tagged(ui.nodes(),'nav-'+route));ui.nodes('dock-'+name);ui.screenshot('dock-'+name)
    ui.shell('wm','size','720x1600');ui.shell('settings','put','system','font_scale','1.0');time.sleep(2)
    ui.tap(tagged(ui.nodes(),'nav-home'))
    train=tagged(ui.nodes(),'nav-library');l,t,r,b=rect(train);x=(l+r)//2;y=(t+b)//2
    ui.shell('input','swipe',str(x),str(y),str(x),str(y),'900')
    items=ui.nodes('dock-tooltip')
    assert any(n.get('text')=='Train' for n in items),'Long-press must show the full section name'
    assert tagged(items,'nav-home').get('selected')=='true','Long-press must not navigate'
    ui.screenshot('dock-tooltip')
    ui.shell('input','tap','40','200');time.sleep(1)
    ui.tap(tagged(ui.nodes(),'nav-library'))
    ui.require(ui.nodes(),'Create custom drill')
    ui.tap(tagged(ui.nodes(),'nav-home'))
    assert state()==before,'Tab browsing altered saved player/training data'
    report=dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),upgrade_from_build9=True,
                saved_data_unchanged=True,all_five_destinations=True,single_row_all_text_sizes=True,
                dock_height_56dp=True,touch_targets_48dp=True,no_overlap_or_offscreen_targets=True,
                selected_tab_semantics=True,root_back_to_court=True,long_press_names=True,
                configurations=configurations,measurements=measurements)
    (ui.OUT/'dock-result.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('phase',choices=['baseline','candidate']);p.add_argument('apk')
    a=p.parse_args();ui.OUT.mkdir(parents=True,exist_ok=True)
    (baseline if a.phase=='baseline' else candidate)(Path(a.apk))
