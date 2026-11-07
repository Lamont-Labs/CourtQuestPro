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

def describe_error(exc):
    if isinstance(exc, subprocess.CalledProcessError):
        stderr = (exc.stderr or b"").decode("utf-8", "replace")[-700:]
        stdout = (exc.stdout or b"").decode("utf-8", "replace")[-300:]
        return f"command={exc.cmd!r} exit={exc.returncode} stderr={stderr!r} stdout={stdout!r}"
    return f"{type(exc).__name__}: {str(exc)[:700]}"

def hierarchy():
    last_error = None
    for attempt in range(1, 4):
        try:
            adb("shell", "rm", "-f", "/data/local/tmp/courtquest-nav.xml")
            adb("shell", "uiautomator", "dump", "/data/local/tmp/courtquest-nav.xml", timeout=120)
            raw = adb("exec-out", "cat", "/data/local/tmp/courtquest-nav.xml").stdout
            return ET.fromstring(raw)
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError) as exc:
            last_error = exc
            print(f"UI hierarchy attempt {attempt}/3 failed: {describe_error(exc)}", flush=True)
            if attempt < 3:
                time.sleep(3 * attempt)
    raise last_error

def screen_size():
    output = adb("shell", "wm", "size").stdout.decode("utf-8", "replace")
    matches = re.findall(r"(\d+)x(\d+)", output)
    if not matches:
        raise RuntimeError(f"Cannot determine emulator dimensions: {output!r}")
    return tuple(map(int, matches[-1]))

def visible_step(root):
    strings = [node.get("text", "") + " " + node.get("content-desc", "") for node in root.iter("node")]
    for value in strings:
        match = re.search(r"STEP\s+([1-5])\s+OF\s+5", value, re.I)
        if match:
            return int(match.group(1))
    return None

def tap_continue(root):
    for node in root.iter("node"):
        value = (node.get("text", "") + " " + node.get("content-desc", "")).strip()
        if value.upper() != "CONTINUE":
            continue
        match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
        if not match:
            continue
        x1, y1, x2, y2 = map(int, match.groups())
        adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
        return True
    return False

def advance(expected):
    width, height = screen_size()
    for attempt in range(7):
        root = hierarchy()
        if visible_step(root) != expected:
            raise RuntimeError(f"Unexpected step before navigation: {visible_step(root)}; expected {expected}")
        if tap_continue(root):
            time.sleep(2)
            next_root = hierarchy()
            if visible_step(next_root) == expected + 1:
                return
            raise RuntimeError(f"Continue did not advance step {expected}; observed {visible_step(next_root)}")
        x = width // 2
        adb("shell", "input", "swipe", str(x), str(int(height * .82)),
            str(x), str(int(height * .24)), "420")
        time.sleep(1)
    raise RuntimeError(f"Continue not found on step {expected} after device-relative scrolling")

for step in range(1, 6):
    item = {"step": step}
    try:
        root = hierarchy()
        ET.ElementTree(root).write(OUT / f"step-{step}-diagnostic.xml", encoding="utf-8", xml_declaration=True)
        observed = visible_step(root)
        item["observed_step"] = observed
        if observed != step:
            raise RuntimeError(f"Expected onboarding step {step}, observed {observed}")
        ET.ElementTree(root).write(OUT / f"step-{step}.xml", encoding="utf-8", xml_declaration=True)
        remote = f"/data/local/tmp/courtquest-onboarding-{step}.png"
        adb("shell", "screencap", "-p", remote, timeout=120)
        local = OUT / f"step-{step}.png"
        adb("pull", remote, str(local))
        adb("shell", "rm", "-f", remote)
        if local.stat().st_size < 10000:
            raise RuntimeError(f"Screenshot too small: {local.stat().st_size}")
        item["captured"] = True
        if step < 5:
            advance(step)
            item["navigation"] = "verified"
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError, RuntimeError, OSError) as exc:
        item["error"] = describe_error(exc)
        results.append(item)
        break
    results.append(item)

(OUT / "capture-report.json").write_text(json.dumps(results, indent=2) + "\n")
print(json.dumps(results), flush=True)
if len(results) != 5 or not all(item.get("captured") for item in results):
    raise SystemExit("Incomplete five-screen onboarding evidence")
