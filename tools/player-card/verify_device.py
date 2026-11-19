"""Exercise native card combinations and save/restart/jersey sync on a guarded disposable AVD."""
import argparse
import hashlib
import io
import json
import re
import sys
import tarfile
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path[:0] = [str(ROOT / "scripts"), str(ROOT / "tools/jersey")]
import verify_android_daily_flow as ui
from verify_android_library_flow import seed_state, state
import verify_device as jersey_verifier


def collect(folder, location="player-card-proof"):
    payload = ui.adb("exec-out", "run-as", ui.PACKAGE, "tar", "-cf", "-", "-C", f"files/{location}", ".")
    with tarfile.open(fileobj=io.BytesIO(payload)) as archive:
        for entry in archive:
            if entry.isfile() and re.fullmatch(r"(?:\./)?[a-z0-9-]+\.(?:png|json)", entry.name):
                (folder / Path(entry.name).name).write_bytes(archive.extractfile(entry).read())


def run(class_name, method, folder):
    output = ui.shell("am", "instrument", "-w", "-e", "class", f"{ui.PACKAGE}.{class_name}" + (f"#{method}" if method else ""),
        f"{ui.PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner", timeout=900)
    (folder / f"{class_name}-{method or 'suite'}.txt").write_text(output)
    if not re.search(r"OK \(\d+ tests?\)", output) or "FAILURES" in output:
        (folder / "failure.png").write_bytes(ui.adb("exec-out","screencap","-p"))
        try: collect(folder)
        except Exception: pass
        raise AssertionError(output)


def enter():
    ui.open_app()
    ui.tap(ui.wait_tagged("welcome-get-started",attempts=20))
    ui.wait_tagged("dock-me",attempts=20)
    items=ui.nodes()
    if ui.tagged(items,"tour-skip") is not None: ui.tap(ui.tagged(items,"tour-skip"))
    ui.tap(ui.wait_tagged("dock-me"))
    ui.wait_tagged("me-player-card",attempts=20)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    for arg in ("previous","candidate","baseline","instrumentation","out"):
        parser.add_argument("--"+arg,type=Path,required=True)
    args=parser.parse_args()
    for _ in range(120):
        try:
            if ui.shell("getprop","sys.boot_completed",timeout=8)=="1": break
        except Exception: pass
        time.sleep(3)
    else: raise AssertionError("Emulator boot timed out")
    assert ui.shell("getprop","ro.kernel.qemu")=="1"
    assert ui.adb("emu","avd","name").decode().splitlines()[0].strip()=="CourtQuest_PremiumQA"
    ui.shell("input","keyevent","KEYCODE_WAKEUP"); ui.shell("wm","dismiss-keyguard")
    ui.shell("settings","put","system","accelerometer_rotation","0")
    ui.shell("settings","put","system","user_rotation","0")
    ui.shell("wm","density","320"); ui.shell("wm","size","720x1600")
    ui.shell("settings","put","system","font_scale","1.0")
    args.out.mkdir(parents=True,exist_ok=True)
    jersey_verifier.upgrade(args.previous.resolve(),args.candidate.resolve(),args.out)
    assert "Success" in ui.adb("install","-r",str(args.instrumentation.resolve())).decode()
    regression=args.out/"jersey-regression"; regression.mkdir(exist_ok=True)
    run("JerseyStudioTest","customizationRestoresAcrossRecreationAndReturnsToCourt",regression)
    collect(regression,"jersey-proof")
    ui.shell("am","force-stop",ui.PACKAGE)
    run("JerseyStudioTest","coldStartRestoresPreviouslySavedJersey",regression)
    # Compare the exact verified v57 baseline with the candidate using one saved fixture.
    assert "Success" in ui.adb("install","-r",str(args.baseline.resolve())).decode()
    ui.seed("visual-review")
    fixture=state()
    fixture.update(playerName="CourtQuest QA",jerseyNumber=8,jerseyNumberText="08",
        jerseyDesign=dict(templateId="RETRO",fabric=-14526053,secondary=-987862,side=-41984,
            collar=-41984,armhole=-41984,numberInk=-1,nameInk=-1,chestBrand="COURTQUEST"))
    seed_state(fixture)
    ui.OUT=args.out
    enter(); ui.nodes("baseline-me");ui.screenshot("baseline-me")
    ui.shell("am","force-stop",ui.PACKAGE)
    saved=ui.adb("exec-out","run-as",ui.PACKAGE,"cat","shared_prefs/court_quest_pro.xml")
    assert "Success" in ui.adb("install","-r",str(args.candidate.resolve())).decode()
    assert saved==ui.adb("exec-out","run-as",ui.PACKAGE,"cat","shared_prefs/court_quest_pro.xml")
    for label,size,font in [("normal","720x1600","1.0"),("compact","640x1280","1.3"),("large-text","640x1280","2.0")]:
        folder=args.out/label;folder.mkdir(exist_ok=True)
        ui.shell("wm","size",size); ui.shell("settings","put","system","font_scale",font)
        run("PlayerCardStudioTest","allEditionsPosesAndFacesAreRealAndSaveAsProfile",folder)
        ui.shell("am","force-stop",ui.PACKAGE)
        run("PlayerCardStudioTest","coldStartUsesSavedCardAndJerseyEditsSynchronizeWithoutCardResave",folder)
        collect(folder)
        assert len(list(folder.glob("*-*-*.png")))>=27
        (folder/"configuration.json").write_text(json.dumps(dict(size=size,font_scale=font)))
    ui.shell("wm","size","720x1600");ui.shell("settings","put","system","font_scale","1.0")
    run("NavigationTourTest",None,args.out)
    run("TrainingEntryTest",None,args.out)
    report=dict(candidate_sha256=hashlib.sha256(args.candidate.read_bytes()).hexdigest(),
        baseline_sha256=hashlib.sha256(args.baseline.read_bytes()).hexdigest(),
        combinations_per_configuration=27,configurations=3,real_android_captures=True,
        original_jersey_journey=True,restart_and_live_uniform_sync=True,upgrade_preserved_preferences=True)
    (args.out/"player-card-verification.json").write_text(json.dumps(report,indent=2)+"\n")


if __name__=="__main__": main()
