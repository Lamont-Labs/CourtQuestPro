#!/usr/bin/env python3
"""Compare two CourtQuest APKs and exercise first run on an isolated CI emulator."""
import hashlib
import json
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = "com.courtquestpro"
OUT = Path("visual-evidence")
APK_DIR = Path("apk-input")
OUT.mkdir(exist_ok=True)
REMOTE_XML = "/data/local/tmp/courtquest-first-run.xml"
BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def adb(*args, timeout=45):
    return subprocess.run(
        ["adb", *args], capture_output=True, check=True, timeout=timeout
    ).stdout


def values(node):
    return tuple(
        " ".join(node.get(key, "").split()).casefold()
        for key in ("text", "content-desc")
    )


def hierarchy():
    adb("shell", "uiautomator", "dump", REMOTE_XML, timeout=25)
    raw = adb("exec-out", "cat", REMOTE_XML)
    (OUT / "last-ui.xml").write_bytes(raw)
    return ET.fromstring(raw)


def wait_for(predicate, description, seconds=55):
    deadline = time.monotonic() + seconds
    last_error = None
    while time.monotonic() < deadline:
        try:
            root = hierarchy()
            if predicate(root):
                return root
        except (subprocess.SubprocessError, ET.ParseError) as error:
            last_error = error
        time.sleep(0.6)
    raise RuntimeError(f"Expected {description}; inspect last-ui.xml ({last_error})")


def contains(root, label):
    expected = " ".join(label.split()).casefold()
    return any(expected in value for node in root.iter("node") for value in values(node))


def screen(label):
    return wait_for(lambda root: contains(root, label), label)


def home_screen():
    return wait_for(
        lambda root: any(
            "court" in values(node) and node.get("package") == PACKAGE
            for node in root.iter("node")
        ),
        "the visible Court navigation tab",
    )


def tap_node(node):
    match = BOUNDS.fullmatch(node.get("bounds", ""))
    if not match:
        raise RuntimeError(f"Target has no valid bounds: {node.attrib}")
    left, top, right, bottom = map(int, match.groups())
    if left >= right or top >= bottom:
        raise RuntimeError(f"Target has empty bounds: {node.attrib}")
    adb("shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))


def tap(label, allow_scroll=False):
    expected = label.casefold()
    for _ in range(7 if allow_scroll else 1):
        root = hierarchy()
        node = next(
            (item for item in root.iter("node") if expected in values(item)), None
        )
        if node is not None:
            tap_node(node)
            return
        if allow_scroll:
            dims = re.findall(rb"(\d+)x(\d+)", adb("shell", "wm", "size"))
            width, height = map(int, dims[-1])
            x = str(width // 2)
            adb("shell", "input", "swipe", x, str(height * 4 // 5), x, str(height // 3), "370")
    raise RuntimeError(f"Could not reach {label}")


def capture(folder, name):
    folder.mkdir(parents=True, exist_ok=True)
    png = adb("exec-out", "screencap", "-p", timeout=40)
    if len(png) < 10000 or not png.startswith(b"\x89PNG\r\n\x1a\n"):
        raise RuntimeError(f"Invalid screenshot for {folder.name}/{name}")
    (folder / f"{name}.png").write_bytes(png)
    (folder / f"{name}.xml").write_bytes((OUT / "last-ui.xml").read_bytes())


def apk(label):
    files = list((APK_DIR / label).rglob("*.apk"))
    if len(files) != 1:
        raise RuntimeError(f"Expected one APK for {label}, got {files}")
    return files[0]


def launch():
    adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")


def fresh():
    adb("shell", "pm", "clear", PACKAGE)
    launch()
    screen("STEP 1 OF 5")


def wizard(folder, exercise_back=False):
    for step in range(1, 6):
        root = screen(f"STEP {step} OF 5")
        capture(folder, f"step-{step}")
        if step == 2 and exercise_back:
            field = next(
                node for node in root.iter("node")
                if node.get("class") == "android.widget.EditText"
            )
            tap_node(field)
            adb("shell", "input", "text", "Jordan")
            wait_for(
                lambda ui: contains(ui, "Jordan")
                and b"mInputShown=true" in adb("shell", "dumpsys", "input_method"),
                "the player name and open keyboard",
            )
            capture(folder, "name-with-keyboard")
        tap("ENTER MY COURT" if step == 5 else "CONTINUE")
        if step == 2 and exercise_back:
            screen("STEP 3 OF 5")
            adb("shell", "input", "keyevent", "4")
            root = screen("STEP 2 OF 5")
            if not contains(root, "Jordan"):
                raise RuntimeError("Player name disappeared on Android Back")
            tap("CONTINUE")
    root = home_screen()
    if exercise_back and not contains(root, "Jordan’s court."):
        raise RuntimeError("Home did not show the entered player name")
    capture(folder, "home")


baseline = apk("baseline")
candidate = apk("candidate")
adb("shell", "settings", "put", "system", "font_scale", "1.0")
adb("shell", "settings", "put", "secure", "show_ime_with_hard_keyboard", "1")
adb("install", "-r", str(baseline), timeout=180)
fresh()
screen("Your court starts here.")
capture(OUT / "before", "welcome")

# Replace the baseline APK on the emulator to verify signing compatibility.
adb("install", "-r", str(candidate), timeout=180)
fresh()
screen("Build your game.")
wizard(OUT / "after", exercise_back=True)

adb("shell", "am", "force-stop", PACKAGE)
launch()
if not contains(home_screen(), "Jordan’s court."):
    raise RuntimeError("The completed player profile did not survive relaunch")
tap("START TRAINING", allow_scroll=True)
wait_for(
    lambda root: any(
        re.fullmatch(r"drill [0-9]{2} / 06", value)
        for node in root.iter("node") for value in values(node)
    ),
    "the first training drill",
)
capture(OUT / "after", "first-drill")

adb("shell", "wm", "size", "840x1600")
adb("shell", "settings", "put", "system", "font_scale", "1.3")
fresh()
wizard(OUT / "after-large-text")
report = {
    "baseline_apk_sha256": hashlib.sha256(baseline.read_bytes()).hexdigest(),
    "candidate_apk_sha256": hashlib.sha256(candidate.read_bytes()).hexdigest(),
    "update_install": "pass",
    "five_step_onboarding": "pass",
    "continue_with_keyboard": "pass",
    "android_back_retains_player_name": "pass",
    "relaunch_after_setup_retains_player_name": "pass",
    "home_to_first_drill": "pass",
    "five_steps_narrower_display_font_scale_1_3": "pass",
}
(OUT / "result.json").write_text(json.dumps(report, indent=2) + "\n")
print(json.dumps(report, indent=2))
