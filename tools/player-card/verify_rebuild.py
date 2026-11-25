"""Capture v56, v58, v59 and v60 on one disposable Android AVD; prove direct upgrades."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import sys
import subprocess
import time
from xml.etree import ElementTree as ET

ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/"scripts"))
import verify_android_daily_flow as ui
from verify_android_library_flow import state,seed_state
from verify_phone_update import saved_values_preserved

def native(folder,method,cls="CardStudioRebuildTest",pose=None):
    folder.mkdir(parents=True,exist_ok=True)
    ui.shell("run-as",ui.PACKAGE,"rm","-rf","files/player-card-proof")
    args=["am","instrument","-w","-r","-e","class",f"{ui.PACKAGE}.{cls}#{method}"]
    if pose:args.extend(["-e","pose",pose.upper()])
    print(f"Android: {cls}#{method}" + (f" ({pose})" if pose else ""),flush=True)
    try:
        output=ui.shell(*args,f"{ui.PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner",timeout=120)
    except subprocess.TimeoutExpired as exc:
        # Preserve failure evidence instead of burning 700 seconds and losing the hang context.
        (folder/"instrumentation-timeout.txt").write_text(
            f"test={cls}#{method} pose={pose} timeout=120s\\n"
            f"stdout={exc.stdout!r}\\nstderr={exc.stderr!r}\\n")
        diagnostics=[
            ("window.txt",("shell","dumpsys","window")),
            ("activity.txt",("shell","dumpsys","activity","activities")),
            ("processes.txt",("shell","ps","-A")),
            ("logcat.txt",("logcat","-d","-t","1800")),
            ("hierarchy.xml",("shell","uiautomator","dump","/dev/tty")),
        ]
        for filename,command in diagnostics:
            try:
                (folder/filename).write_bytes(ui.adb(*command,timeout=12))
            except Exception as diagnostic_error:
                (folder/(filename+".error.txt")).write_text(str(diagnostic_error))
        try:
            (folder/"timeout-screen.png").write_bytes(ui.adb("exec-out","screencap","-p",timeout=12))
        except Exception as screenshot_error:
            (folder/"timeout-screen.error.txt").write_text(str(screenshot_error))
        # Surface the last test checkpoint in the Actions console, not only in ZIP evidence.
        log_file=folder/"logcat.txt"
        if log_file.exists():
            checkpoints=[line for line in log_file.read_text(errors="replace").splitlines()
                         if "CQ_ACCESS_STAGE" in line or "CQ_CARD_RESTORE_STAGE" in line]
            print("Last Android test checkpoints:",flush=True)
            print("\\n".join(checkpoints[-25:]) or "(none recorded)",flush=True)
        raise AssertionError(f"Android instrumentation stalled: {cls}#{method}; diagnostics in {folder}") from exc
    (folder/f"{method}.txt").write_text(output)
    from tarfile import open as taropen
    import io
    if ui.shell("run-as",ui.PACKAGE,"ls","files").find("player-card-proof")>=0:
        content=ui.adb("exec-out","run-as",ui.PACKAGE,"tar","-cf","-","-C","files/player-card-proof",".",timeout=100)
        with taropen(fileobj=io.BytesIO(content)) as archive:
            for entry in archive:
                if entry.isfile() and re.fullmatch(r"(?:\./)?[a-z0-9-]+\.(png|json|txt)",entry.name):
                    (folder/Path(entry.name).name).write_bytes(archive.extractfile(entry).read())
    if not re.search(r"OK \(\d+ tests?\)",output) or "FAILURES" in output:
        print(output[-9000:],flush=True)
        (folder/"failure.png").write_bytes(ui.adb("exec-out","screencap","-p"))
        raise AssertionError(f"{cls}#{method} failed on the running AVD")

def install(file,code,replace=False):
    assert "Success" in ui.adb("install",*(["-r"] if replace else []),str(file.resolve()),timeout=180).decode()
    if code:assert re.search(rf"versionCode={code}\b",ui.shell("dumpsys","package",ui.PACKAGE))

def enter_welcome_if_present():
    """Handle the current tap-to-enter welcome and older released welcome buttons."""
    for attempt in range(12):
        items=ui.nodes()
        for tag in ("welcome-enter-ball","welcome-swipe-ball","welcome-get-started"):
            target=ui.tagged(items,tag)
            if target is not None:
                ui.tap(target)
                return tag
        if attempt<11:time.sleep(.2)
    return None

def dismiss_tour_if_present():
    for attempt in range(10):
        items=ui.nodes()
        skip=ui.tagged(items,"tour-skip")
        if skip is not None:
            ui.tap(skip)
            return
        if attempt>=3 and ui.tagged(items,"dock-me") is not None:
            return
        if attempt<9:time.sleep(.2)

def ready():
    for _ in range(100):
        if ui.shell("getprop","sys.boot_completed",timeout=10)=="1":break
        time.sleep(2)
    else:raise RuntimeError("Android did not boot")
    assert ui.shell("getprop","ro.kernel.qemu")=="1"
    assert ui.adb("emu","avd","name").decode().splitlines()[0].strip()=="CourtQuest_PremiumQA"
    ui.shell("input","keyevent","KEYCODE_WAKEUP");ui.shell("wm","dismiss-keyguard")
    ui.shell("settings","put","system","accelerometer_rotation","0")
    ui.shell("settings","put","system","user_rotation","0")
    ui.shell("wm","density","320");ui.shell("wm","size","720x1600")
    ui.shell("settings","put","system","font_scale","1.0")
    for key in ("window_animation_scale","transition_animation_scale","animator_duration_scale"):
        ui.shell("settings","put","global",key,"1")

def seed():
    ui.seed("game-history"); ui.open_app()
    val=state();today=ui.shell("date","+%F")
    val.update(playerName="Avery Rivers",jerseyNumber=8,jerseyNumberText="08",totalXp=425,longestStreak=3,
               drillResults=[dict(slotId="makes",date=today,drillId="s1",attempted=10,completed=6),
                            dict(slotId="control",date=today,drillId="s2",attempted=8,completed=2)],
               matches=[dict(date=today,player="Avery Rivers",opponent="Guest",playerScore=11,opponentScore=7,winner="Avery Rivers",xp=10)])
    seed_state(val)
    # Allow the old version to normalize and persist its own state before hashing.
    ui.open_app()
    enter_welcome_if_present()
    ui.wait_tagged("dock-me",attempts=12)
    ui.shell("am","force-stop",ui.PACKAGE)
    root=ET.Element("map")
    for key,value in {"selected_style_v1":"HOLOGRAPHIC","selected_pose_v2":"STANDING","selected_face_v2":"BLANK",
        "avatar_hair_v3":"STRAIGHT","avatar_skin_v3":"COOL","avatar_footwear_v3":"HIGH"}.items():
        ET.SubElement(root,"string",name=key).text=value
    ui.shell(f'run-as {ui.PACKAGE} sh -c "cat > shared_prefs/courtquest_player_card.xml"',data=ET.tostring(root))
    return state()

def before_screen(out):
    ui.OUT=out;ui.open_app()
    enter_welcome_if_present()
    dismiss_tour_if_present()
    ui.tap(ui.wait_tagged("dock-me",attempts=12));ui.tap(ui.wait_tagged("me-player-card-studio",attempts=12))
    ui.wait_tagged("player-card-studio-screen",attempts=12);time.sleep(1)
    ui.screenshot("before-studio")

def main():
    parser=argparse.ArgumentParser()
    for arg in ("v56","v58","v59","candidate","instrumentation","out"):parser.add_argument("--"+arg,type=Path,required=True)
    args=parser.parse_args();out=args.out.resolve();out.mkdir(parents=True,exist_ok=True);ui.OUT=out
    ready();upgrades={}
    def digest(data):return hashlib.sha256(data).hexdigest()
    def pref(file):return ui.adb("exec-out","run-as",ui.PACKAGE,"cat","shared_prefs/"+file+".xml")
    for code,apk in [(56,args.v56),(58,args.v58),(59,args.v59)]:
        if code!=56:assert "Success" in ui.adb("uninstall",ui.PACKAGE).decode()
        install(apk,code);saved=seed()
        if code==59:before_screen(out)
        ui.shell("am","force-stop",ui.PACKAGE);files=["court_quest_pro","courtquest_player_card"]
        before={k:pref(k) for k in files};install(args.candidate,60,True)
        assert before=={k:pref(k) for k in files}
        ui.open_app();ui.wait_tagged("welcome-enter-ball",attempts=12)
        now=state()
        for key in ("playerName","jerseyNumber","matches","drillResults","totalXp","longestStreak"):
            assert key in saved and key in now, f"Missing required upgrade state: {key}"
            assert saved_values_preserved(saved[key],now[key]),key
        # Legacy releases may not serialize the optional formatted jersey number.
        # Verify it when present, without indexing a key absent in the source APK.
        if "jerseyNumberText" in saved:
            assert "jerseyNumberText" in now, "Formatted jersey number lost during upgrade"
            assert saved_values_preserved(saved["jerseyNumberText"],now["jerseyNumberText"]),"jerseyNumberText"
        assert pref("courtquest_player_card")==before["courtquest_player_card"]
        upgrades[str(code)]={"input_apk_sha256":digest(apk.read_bytes()),"candidate_sha256":digest(args.candidate.read_bytes()),
            "previous_version":code,"installed_version":60,"preferences_byte_identical":True,
            "after_cold_launch":True,"preference_hashes":{k:digest(v) for k,v in before.items()}}
    (out/"upgrades.json").write_text(json.dumps(upgrades,indent=2))
    install(args.instrumentation,None,True)
    native(out/"normal","twoSidesShowLiveJerseyRealStatsAndPersistSelectedEdition")
    ui.shell("am","force-stop",ui.PACKAGE)
    native(out/"normal","restoredCardUniformAndDiscard")
    native(out/"uniforms","savedUniformTemplatesUpdateCollectibleArtworkWithoutChangingTraining","PlayerCardStudioTest")
    for pose in ("standing","dribbling","action","ready","followthrough"):
        native(out/"poses"/pose,"everyOptionAtEachPose",pose=pose)
    native(out/"welcome","swipeShotCapturesLaunchApexRimNetAndExitOnAndroid","WelcomeAnimationVisualTest")
    # Saved avatar preferences remain intact; the card editor only changes edition and uniform.
    for mode,size,font in [("compact","640x1280","1.3"),("large-text","640x1280","2.0"),("landscape","1280x720","1.0")]:
        ui.shell("wm","size",size);ui.shell("settings","put","system","font_scale",font)
        ui.shell("settings","put","global","animator_duration_scale","0")
        native(out/mode,"accessibleLayoutAndNoMotionRecord")
        if mode=="large-text":
            ui.shell("am","force-stop",ui.PACKAGE)
            native(out/"reduced-motion","reducedMotionTapSkipsFlightAndStillEntersTheApp","WelcomeAnimationVisualTest")
        if mode=="landscape":
            ui.shell("settings","put","global","animator_duration_scale","1")
            ui.shell("am","force-stop",ui.PACKAGE)
            native(out/"welcome-landscape","swipeShotCapturesLaunchApexRimNetAndExitOnAndroid","WelcomeAnimationVisualTest")
            ui.shell("settings","put","global","animator_duration_scale","0")
    ui.shell("wm","size","720x1600");ui.shell("settings","put","system","font_scale","1.0")
    ui.shell("settings","put","global","animator_duration_scale","1")
    ui.seed("visual-review")
    native(out/"navigation","everyGuideActionFitsAboveTheDockAndContinuesToTheCorrectTab","NavigationTourTest")
    (out/"result.json").write_text(json.dumps({"commit":os.environ["GITHUB_SHA"],"candidate_sha256":digest(args.candidate.read_bytes()),
        "version_code":60,"card_design":"five-owner-supplied-paired-editions","card_front_back_screenshots":10,
        "pose_edition_front_screenshots":25,"welcome_entry_screenshots_portrait":2,
        "welcome_entry_screenshots_landscape":2,
        "reduced_motion_entry":True,"direct_upgrades":[56,58,59],"status":"passed"},indent=2))
if __name__=="__main__":main()
