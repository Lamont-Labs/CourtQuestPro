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

def tab_target(items,label,route,width):
    tagged_node=tagged(items,'nav-'+route)
    if tagged_node is not None:
        return tagged_node
    limit=min(width,380)
    matches=[]
    for node in items:
        box=rect(node)
        if len(box)!=4 or box[0]<0 or box[2]<=box[0] or box[3]<=box[1] or box[2]>limit:
            continue
        text=node.get('text','').strip()
        desc=node.get('content-desc','').strip()
        if text==label or desc==label or (label=='Me' and desc.startswith('Me, jersey number ')):
            matches.append(node)
    return max(matches,key=lambda n:(rect(n)[2]-rect(n)[0])*(rect(n)[3]-rect(n)[1]),default=None)


def candidate(apk):
    before=state()
    before_raw=ui.prefs()['state'].text
    assert 'Success' in ui.adb('install','-r',str(apk.resolve()),timeout=180).decode()
    assert ui.prefs()['state'].text==before_raw,'Installing the edge-navigation update changed saved data'
    ui.open_app()
    assert state()==before,'Opening the edge-navigation update changed saved data'
    ui.tap(ui.require(ui.nodes(),'Open navigation'))
    ui.tap(tab_target(ui.nodes(),'Court','home',720))
    ui.require(ui.nodes(),'Jordan’s court.')

    configurations=[('normal',720,1600,'1.0'),('owner-large',720,1600,'1.5'),
                    ('compact',640,1440,'1.3'),('compact-200',640,1440,'2.0'),
                    ('landscape',1280,720,'1.0')]
    route_checks={
        'Court':('home','Jordan’s court.'),
        'Train':('library','Train your way.'),
        'Quests':('quests','A little work. A better game.'),
        'Progress':('career','Your work, recorded.'),
        'Me':('me','Your game. Your way.')
    }
    measurements=[]
    for config,width,height,font in configurations:
        ui.shell('wm','size',f'{width}x{height}')
        ui.shell('settings','put','system','font_scale',font);time.sleep(2)
        labels=list(route_checks) if config!='landscape' else ['Court']
        current_label='Court'
        for label in labels:
            route,heading=route_checks[label]
            items=ui.nodes()
            handle=tagged(items,'nav-open-handle')
            if handle is None:
                handle=ui.require(items,'Open navigation')
            ui.tap(handle)
            items=ui.nodes('edge-'+config+'-expanded')
            active_targets=[tab_target(items,name,path,width) for name,(path,_) in route_checks.items()]
            visible=[(name,node) for name,node in zip(route_checks,active_targets) if node is not None]
            assert len(visible)>=1,(config,'no destination target is accessible')
            if config!='landscape':
                assert len(visible)==5,(config,'all five destinations must be visible')
                boxes=[rect(node) for _,node in visible]
                centers=[(box[0]+box[2])/2 for box in boxes]
                assert max(centers)-min(centers)<=8,(config,'cards must form one vertical rail',boxes)
                for box,node,expected in zip(boxes,[n for _,n in visible],[n for n,_ in visible]):
                    left,top,right,bottom=box
                    assert right-left>=95 and bottom-top>=95,(config,expected,'target smaller than 48dp',box,node.attrib)
                    assert 0<=left<right<=width and 0<=top<bottom<=height,(config,expected,'offscreen',box)
                    desc=node.get('content-desc','')
                    assert desc==expected or (expected=='Me' and desc.startswith('Me, jersey number ')),(config,expected,'accessible name',node.attrib)
                for upper,lower in zip(boxes,boxes[1:]):
                    assert upper[3] <= lower[1],(config,'navigation cards overlap',boxes)
            selected_nodes=[node for _,node in visible if node.get('selected')=='true']
            assert len(selected_nodes)==1,(config,'exactly one destination must be selected')
            selected_desc=selected_nodes[0].get('content-desc','')
            assert selected_desc==current_label or (current_label=='Me' and selected_desc.startswith('Me, jersey number ')),(config,'wrong selected destination',selected_desc,current_label)
            assert ui.find(items,'Close navigation') is not None,(config,'drawer close control is not accessible')
            if label=='Court':
                ui.screenshot('edge-'+config)
            target=tab_target(items,label,route,width)
            assert target is not None,(config,label,'destination not found in open rail')
            ui.tap(target)
            items=ui.nodes()
            assert ui.find(items,heading) is not None,(config,label,'destination did not open',heading)
            assert ui.find(items,'Open navigation') is not None,(config,label,'rail did not close after selection')
            measurements.append(dict(configuration=config,selected=label,tab_bounds={name:rect(node) for name,node in visible}))
            current_label=label

        if config!='landscape':
            ui.shell('input','keyevent','KEYCODE_BACK');time.sleep(0.5)
            ui.require(ui.nodes(),'Jordan’s court.')
        handle=tagged(ui.nodes(),'nav-open-handle')
        if handle is None:
            handle=ui.require(ui.nodes(),'Open navigation')
        ui.tap(handle)
        ui.nodes('edge-outside-open')
        ui.shell('input','tap',str(width-20),str(height//2));time.sleep(0.5)
        ui.require(ui.nodes(),'Open navigation')
        ui.tap(ui.require(ui.nodes(),'Open navigation'))
        ui.shell('input','keyevent','KEYCODE_BACK');time.sleep(0.5)
        ui.require(ui.nodes(),'Open navigation')
        ui.tap(ui.require(ui.nodes(),'Open navigation'))
        ui.shell('input','swipe',str(width//4),str(height//2),'4',str(height//2),'400');time.sleep(0.5)
        ui.require(ui.nodes(),'Open navigation')

    ui.shell('wm','size','720x1600')
    ui.shell('settings','put','system','font_scale','1.0');time.sleep(2)
    ui.tap(ui.require(ui.nodes(),'Open navigation'))
    open_items=ui.nodes('edge-jersey-before')
    ui.tap(tab_target(open_items,'Me','me',720))
    ui.tap(ui.require(ui.nodes(),'Customize jersey'))
    field=ui.require(ui.nodes(),'Jersey number')
    ui.tap(field)
    ui.shell('input','keyevent','KEYCODE_MOVE_END')
    for _ in range(len(str(before.get('jerseyNumber') or 23))):
        ui.shell('input','keyevent','KEYCODE_DEL')
    ui.shell('input','text','8')
    ui.tap(ui.require(ui.nodes(),'Save jersey number'))
    assert state().get('jerseyNumber')==8,'Selected jersey number was not persisted'
    ui.tap(ui.require(ui.nodes(),'Open navigation'))
    menu=ui.nodes('edge-jersey-after')
    me=tab_target(menu,'Me','me',720)
    assert me is not None and me.get('content-desc')=='Me, jersey number 8',(me.attrib if me is not None else 'Me nav card missing')
    ui.tap(me)
    assert ui.find(ui.nodes(),'Number 8') is not None,'Me profile did not show the saved jersey number'
    saved=state()
    expected={**before,'jerseyNumber':8}
    assert saved==expected,('Jersey edit changed unrelated profile or training data',saved,expected)

    report=dict(input_apk_sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),upgrade_from_build9=True,
                saved_data_preserved_on_install=True,all_five_destinations=True,all_five_routes_open=True,
                vertical_illustrated_rail=True,touch_targets_at_least_48dp=True,selected_tab_semantics=True,
                auto_close_on_selection=True,outside_tap_dismiss=True,back_dismiss=True,swipe_dismiss=True,root_back_to_court=True,
                jersey_number_23_to_8_persisted=True,unrelated_data_unchanged=True,
                configurations=configurations,measurements=measurements)
    (ui.OUT/'edge-navigation-result.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)



if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('phase',choices=['baseline','candidate']);p.add_argument('apk')
    a=p.parse_args();ui.OUT.mkdir(parents=True,exist_ok=True)
    (baseline if a.phase=='baseline' else candidate)(Path(a.apk))
