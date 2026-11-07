#!/usr/bin/env python3
"""Capture first launch only after the actual app UI is visible; fail closed."""
import pathlib
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path("visual-evidence")
OUT.mkdir(exist_ok=True)
PKG = "com.courtquestpro"

def adb(*args, timeout=50):
    return subprocess.run(["adb", *args], capture_output=True, timeout=timeout, check=True).stdout

def dump():
    adb("shell", "uiautomator", "dump", "/data/local/tmp/cq-capture.xml", timeout=75)
    raw = adb("exec-out", "cat", "/data/local/tmp/cq-capture.xml")
    (OUT / "ui-hierarchy.xml").write_bytes(raw)
    start = raw.find(b"<?xml")
    if start < 0:
        start = raw.find(b"<hierarchy")
    if start < 0:
        raise RuntimeError("Android UI dump returned no XML; diagnostic saved to ui-hierarchy.xml")
    return ET.fromstring(raw[start:])

def center(bounds):
    import re
    m = re.fullmatch(r"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]", bounds)
    if not m:
        return None
    a,b,c,d = map(int, m.groups())
    return ((a+c)//2, (b+d)//2)

adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk", timeout=180)
adb("shell", "am", "start", "-n", PKG + "/.MainActivity")
for attempt in range(1, 5):
    time.sleep(12 if attempt == 1 else 8)
    try:
        root = dump()
    except (ET.ParseError, RuntimeError, subprocess.CalledProcessError, subprocess.TimeoutExpired) as exc:
        (OUT / "ui-dump-error.txt").write_text(f"attempt={attempt}: {type(exc).__name__}: {exc}\\n")
        if attempt == 4:
            raise RuntimeError("Android UI hierarchy unavailable after bounded retries") from exc
        continue
    nodes = list(root.iter("node"))
    dialogs = [n for n in nodes if "isn't responding" in n.get("text", "") or "isn’t responding" in n.get("text", "")]
    if dialogs:
        if attempt == 4:
            raise RuntimeError("Android System UI ANR persists after bounded recovery")
        wait = next((n for n in nodes if n.get("text") == "Wait"), None)
        coords = center(wait.get("bounds", "")) if wait is not None else None
        if coords:
            adb("shell", "input", "tap", str(coords[0]), str(coords[1]))
        continue
    app_nodes = [n for n in nodes if n.get("package") == PKG]
    if app_nodes:
        data = adb("exec-out", "screencap", "-p", timeout=90)
        (OUT / "first-launch.png").write_bytes(data)
        if len(data) < 10000 or not data.startswith(bytes.fromhex("89504e470d0a1a0a")):
            raise RuntimeError("Invalid app screenshot")
        print("CourtQuest UI captured after", attempt, "checks")
        break
    if attempt == 4:
        raise RuntimeError("CourtQuest UI not visible after bounded startup checks")
    adb("shell", "am", "start", "-n", PKG + "/.MainActivity")
