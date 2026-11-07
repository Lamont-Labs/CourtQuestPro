#!/usr/bin/env python3
"""Best-effort onboarding evidence collection; never modifies app source."""
import json
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = pathlib.Path("visual-evidence/onboarding")
OUT.mkdir(parents=True, exist_ok=True)
results = []

def adb(*args, timeout=90):
    return subprocess.run(["adb", *args], capture_output=True, timeout=timeout, check=True)

def hierarchy():
    adb("shell", "uiautomator", "dump", "/data/local/tmp/courtquest-nav.xml", timeout=120)
    raw = adb("exec-out", "cat", "/data/local/tmp/courtquest-nav.xml").stdout
    return ET.fromstring(raw)

def tap_label(root, labels):
    for label in labels:
        for node in root.iter("node"):
            text = (node.get("text", "") + " " + node.get("content-desc", "")).strip()
            bounds = node.get("bounds", "")
            if label.lower() not in text.lower():
                continue
            m = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
                return text
    return None

for step in range(5):
    item = {"step": step + 1}
    try:
        root = hierarchy()
        xml_path = OUT / f"step-{step+1}.xml"
        ET.ElementTree(root).write(xml_path, encoding="utf-8", xml_declaration=True)
        png_path = f"/data/local/tmp/courtquest-onboarding-{step+1}.png"
        adb("shell", "screencap", "-p", png_path, timeout=120)
        adb("pull", png_path, str(OUT / f"step-{step+1}.png"))
        adb("shell", "rm", "-f", png_path)
        item["captured"] = True
        if step < 4:
            pressed = tap_label(root, ["CONTINUE"])
            for attempt in range(3):
                if pressed is not None:
                    break
                # Scroll to reveal the action below the viewport.
                adb("shell", "input", "swipe", "540", "1750", "540", "650", "450")
                time.sleep(1)
                root = hierarchy()
                pressed = tap_label(root, ["CONTINUE"])
            if pressed is None:
                item["navigation"] = "No matching accessible next control; stopped safely"
                results.append(item)
                break
            item["navigation"] = pressed
            time.sleep(2)
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError) as exc:
        item["error"] = type(exc).__name__
        results.append(item)
        break
    results.append(item)
(OUT / "capture-report.json").write_text(json.dumps(results, indent=2) + "\n")
print(json.dumps(results))
