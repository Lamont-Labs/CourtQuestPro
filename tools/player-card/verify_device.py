"""Exercise native card combinations and save/restart/jersey sync on a guarded disposable AVD."""
import argparse
import hashlib
import io
import json
import re
import sys
import tarfile
import time
from xml.etree import ElementTree as ET
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


def run(class_name, method, folder,baseline=False):
    output = ui.shell("am", "instrument", "-w", "-e", "courtquestBaseline",str(baseline).lower(),"-e", "class", f"{ui.PACKAGE}.{class_name}" + (f"#{method}" if method else ""),
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
    for label,size,font in [("normal","720x1600","1.0"),("compact","640x1280","1.3"),("large-text","640x1280","2.0"),("landscape","1280x720","1.0")]:
        folder=args.out/"before"/label;folder.mkdir(parents=True,exist_ok=True)
        ui.shell("am","force-stop",ui.PACKAGE)
        seed_state(fixture)
        ui.shell("run-as",ui.PACKAGE,"rm","-rf","files/app-quality-proof")
        ui.shell("wm","size",size);ui.shell("settings","put","system","font_scale",font)
        run("PremiumJourneyAuditTest","everySoloDestinationAndSettingsPageIsReachable",folder,baseline=True)
        collect(folder,"app-quality-proof")
    ui.shell("am","force-stop",ui.PACKAGE)
    legacy=ET.Element("map")
    legacy_values={"selected_style_v1":"CLASSIC","selected_pose_v2":"DRIBBLING","selected_face_v2":"BLANK",
        "avatar_hair_v3":"BRAIDS","avatar_headwear_v3":"WHITE","avatar_build_v3":"LEAN",
        "avatar_skin_v3":"DEEP","avatar_shoes_v3":"ORANGE","avatar_gear_v3":"BOTH"}
    for key,value in legacy_values.items():ET.SubElement(legacy,"string",name=key).text=value
    legacy_bytes=ET.tostring(legacy,encoding="utf-8")
    ui.shell(f'run-as {ui.PACKAGE} sh -c "cat > shared_prefs/courtquest_player_card.xml"',data=legacy_bytes)
    saved=ui.adb("exec-out","run-as",ui.PACKAGE,"cat","shared_prefs/court_quest_pro.xml")
    assert "Success" in ui.adb("install","-r",str(args.candidate.resolve())).decode()
    assert saved==ui.adb("exec-out","run-as",ui.PACKAGE,"cat","shared_prefs/court_quest_pro.xml")
    assert legacy_bytes==ui.adb("exec-out","run-as",ui.PACKAGE,"cat","shared_prefs/courtquest_player_card.xml")
    migration=args.out/"cosmetic-migration";migration.mkdir(exist_ok=True)
    ui.shell("wm","size","720x1600");ui.shell("settings","put","system","font_scale","1.0")
    run("PlayerCardStudioTest","legacyV3CosmeticsUpgradeWithoutChangingTraining",migration)
    ui.shell("am","force-stop",ui.PACKAGE)
    run("PlayerCardStudioTest","legacyV3CosmeticsSurviveColdStart",migration)
    collect(migration)
    for label,size,font in [("normal","720x1600","1.0"),("compact","640x1280","1.3"),("large-text","640x1280","2.0"),("landscape","1280x720","1.0")]:
        folder=args.out/label;folder.mkdir(exist_ok=True)
        ui.shell("am","force-stop",ui.PACKAGE)
        # Disposable-AVD visual fixture only, after install-over preservation assertions.
        # Reset cosmetic QA selections so paired app captures use the same saved identity.
        seed_state(fixture)
        ui.shell("run-as",ui.PACKAGE,"rm","-f","shared_prefs/courtquest_player_card.xml")
        ui.shell("run-as",ui.PACKAGE,"rm","-rf","files/player-card-proof")
        ui.shell("wm","size",size); ui.shell("settings","put","system","font_scale",font)
        ui.shell("run-as",ui.PACKAGE,"rm","-rf","files/app-quality-proof")
        run("PremiumJourneyAuditTest","everySoloDestinationAndSettingsPageIsReachable",folder)
        collect(folder,"app-quality-proof")
        run("PlayerCardStudioTest","allEditionsPosesAndFacesAreRealAndSaveAsProfile",folder)
        ui.shell("am","force-stop",ui.PACKAGE)
        run("PlayerCardStudioTest","coldStartUsesSavedCardAndJerseyEditsSynchronizeWithoutCardResave",folder)
        run("PlayerCardStudioTest","avatarOptionsRenderOnEveryPoseAndPersistWithoutTouchingTraining",folder)
        ui.shell("am","force-stop",ui.PACKAGE)
        run("PlayerCardStudioTest","customizedAvatarSurvivesColdStartAndUnsavedAvatarDraftCanBeDiscarded",folder)
        collect(folder)
        gallery=json.loads((folder/"verified.json").read_text())
        assert len(gallery["gallery_files"])==gallery["combinations"]==80
        assert all((folder/name).is_file() for name in gallery["gallery_files"])
        (folder/"configuration.json").write_text(json.dumps(dict(size=size,font_scale=font)))
    ui.shell("wm","size","720x1600");ui.shell("settings","put","system","font_scale","1.0")
    ui.seed("visual-review")
    run("NavigationTourTest",None,args.out)
    ui.seed("visual-review")
    run("TrainingEntryTest",None,args.out)
    # Existing deep receipt/progression/extra-practice and game/Undo checks, same guarded AVD.
    import capture_v42_solo_baseline as solo
    for label in ("normal","large"):
        ui.OUT=args.out/"deep-journeys"/label;ui.OUT.mkdir(parents=True,exist_ok=True)
        ui.shell("wm","size","640x1440" if label=="large" else "720x1600")
        ui.shell("settings","put","system","font_scale","2.0" if label=="large" else "1.0")
        ui.seed("visual-review");ui.open_app();solo.enter_court();solo.set_tour_complete(False)
        solo.verify_candidate(label)
        solo.verify_drill_batch(label)
        solo.verify_progression_screens(label)
    import verify_android_game_flow as games
    ui.OUT=args.out/"game-journey";ui.OUT.mkdir(parents=True,exist_ok=True)
    sys.argv=[sys.argv[0],"candidate",str(args.candidate.resolve())]
    games.main()
    census=json.loads((args.out/"normal"/"avatar-rendered-differences.json").read_text())
    dimensions={key.split("-",1)[1]:len(value) for key,value in census.items() if key.startswith("STANDING-")}
    assert len(dimensions)==7 and sum(dimensions.values())==43
    assert len(census)==5*len(dimensions)
    assert all(len(hashes)==len(set(hashes.values())) for hashes in census.values())
    for pose in {key.split("-",1)[0] for key in census}:
        assert {key.split("-",1)[1]:len(value) for key,value in census.items() if key.startswith(pose+"-")}==dimensions
    report=dict(candidate_sha256=hashlib.sha256(args.candidate.read_bytes()).hexdigest(),
        baseline_sha256=hashlib.sha256(args.baseline.read_bytes()).hexdigest(),
        combinations_per_configuration=80,configurations=4,real_android_captures=True,
        original_jersey_journey=True,restart_and_live_uniform_sync=True,upgrade_preserved_preferences=True,
        avatar_dimensions=7,avatar_options=43,avatar_all_poses=True,avatar_restart_and_discard=True,
        total_choices_including_card_pose_face=56,physical_footwear_models=3,
        isolated_artwork_pixel_checks=True,landscape=True,deep_drill_receipt_progression_game_regressions=True)
    report["legacy_cosmetic_upgrade_and_cold_start"]=True
    report["avatar_option_counts"]=dimensions
    (args.out/"player-card-verification.json").write_text(json.dumps(report,indent=2)+"\n")


if __name__=="__main__": main()
