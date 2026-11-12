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
    raise AssertionError('Compact dock not implemented yet')

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('phase',choices=['baseline','candidate']);p.add_argument('apk')
    a=p.parse_args();ui.OUT.mkdir(parents=True,exist_ok=True)
    (baseline if a.phase=='baseline' else candidate)(Path(a.apk))
