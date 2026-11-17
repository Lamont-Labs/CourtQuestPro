"""Capture released v41 solo-player baseline on the guarded disposable Android AVD.

No candidate comparisons, no Team/coach flows, no physical-device operations.
"""
import argparse
import hashlib
import json
from pathlib import Path
import verify_android_daily_flow as ui

CONFIGS = [('normal',720,1600,'1.0'),('compact',640,1440,'1.3'),('large',640,1440,'2.0')]
DESTINATIONS = ['Court','Train','Quests','Progress','Me']

def capture(name):
    ui.nodes(name)
    ui.screenshot(name)

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--apk',required=True,type=Path)
    parser.add_argument('--output',default='artifacts/v42-v41-baseline',type=Path)
    args=parser.parse_args()
    assert args.apk.is_file() and args.apk.stat().st_size > 1000000, 'Missing real release APK'
    # Safety: seed() and pm clear operate only on the disposable AVD.
    assert ui.shell('getprop','ro.kernel.qemu') == '1', 'Never run against a physical device'
    assert ui.adb('emu','avd','name').decode().splitlines()[0].strip() == 'CourtQuest_PremiumQA'
    apk_sha=hashlib.sha256(args.apk.read_bytes()).hexdigest()
    assert 'Success' in ui.adb('install',str(args.apk.resolve()),timeout=180).decode()
    records=[]
    for label,width,height,font in CONFIGS:
        ui.OUT=args.output/label
        ui.OUT.mkdir(parents=True,exist_ok=True)
        ui.shell('wm','size',f'{width}x{height}')
        ui.shell('wm','density','320')
        ui.shell('settings','put','system','font_scale',font)
        assert 'Success' in ui.shell('pm','clear',ui.PACKAGE)
        ui.open_app()
        capture('onboarding')
        ui.seed('visual-review')
        ui.open_app()
        capture('seeded-first-frame')
        for destination in DESTINATIONS:
            try:
                ui.navigate(destination)
            except AssertionError:
                # Preserve hierarchy and screenshot when a release's navigation differs.
                capture('navigation-failure-'+destination.lower())
                raise
            capture(destination.lower())
            records.append({'configuration':label,'screen':destination,'capture':str(ui.OUT/(destination.lower()+'.png'))})
        ui.navigate('Court')
        capture('court-return')
        # Do not mutate practice history in this visual-only baseline pass.
    report={'release_tag':'courtquest-phone-update-v41','apk_sha256':apk_sha,
            'reference':'released v41, not unreleased walkthrough fix',
            'guarded_emulator':True,'candidate_captured':False,
            'owner_visual_approval':'PENDING','screens':records,
            'omitted':'Team/coach/school; deeper drill/receipt journeys pending dedicated fixture'}
    args.output.mkdir(parents=True,exist_ok=True)
    (args.output/'baseline-manifest.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report),flush=True)

if __name__=='__main__':
    main()
