"""Build and validate the offline illustration manifest for built-in drills."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG_PATH = ROOT / "app/src/main/java/com/courtquestpro/MainActivity.kt"
COACHING_PATH = ROOT / "app/src/main/java/com/courtquestpro/DrillCoachingLibrary.kt"
ASSET_DIR = ROOT / "app/src/main/assets/drill_art"
MANIFEST_PATH = ASSET_DIR / "manifest.json"
REPORT_PATH = ROOT / "docs/drills/DRILL_ART_COVERAGE.md"

DRILL_PATTERN = re.compile(
    r'Drill\("([^"]+)","([^"]+)","([^"]+)","([^"]+)",\d+,'
    r'"([^"]*)","([^"]*)","([^"]*)"\)'
)
COACHING_PATTERN = re.compile(r'^\s*"([^"]+)"\s+to\s+DrillCoaching\(', re.MULTILINE)

# Phase labels are grounded in MainActivity.drills and DrillCoachingLibrary.
# Comparison images are omitted when the generated panel would misstate a
# valid variation or normalize unsafe contact.
VISUALS = {
    "s1": {
        "summary": "Five positions, balanced release, held follow-through",
        "sequence": ["Reset feet at a comfortable spot", "Release under control", "Hold the follow-through at the spot"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Balanced finish versus drifting off the spot and dropping the follow-through."},
    },
    "s2": {
        "summary": "Receive, set, shoot, retrieve safely",
        "sequence": ["Prepare hands and receive", "Set balance and square", "Release, land balanced, then retrieve"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Balanced catch-and-set versus a rushed, off-balance catch."},
    },
    "s3": {
        "summary": "Wing change-of-direction into a balanced jumper",
        "sequence": ["Start at the wing", "Change direction and stop", "Shoot from a balanced base"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Controlled stop versus sideways drift or crossed feet."},
    },
    "b1": {
        "summary": "Low stationary crossover with eyes up",
        "sequence": ["Set an athletic stance", "Cross below the waist", "Reset and switch direction"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Low controlled dribble versus a high, exposed crossover with eyes down."},
    },
    "b2": {
        "summary": "Slow baseline dribble, short burst, controlled deceleration",
        "sequence": ["Start slowly at the baseline", "Accelerate for a few steps", "Decelerate without losing control"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Eyes-up control versus an upright, high loose dribble."},
    },
    "b3": {
        "summary": "Two protected moves into an open driving lane",
        "sequence": ["Choose two controlled moves", "Protect the ball through the combo", "Accelerate into open space"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Ball kept close versus unnecessarily exposed outside the body."},
    },
    "f1": {
        "summary": "Right-hand and left-hand layup approaches",
        "sequence": ["Approach at a comfortable speed", "Use the matching hand-side footwork", "Finish gently off the backboard"],
        "comparison": {"included": False, "reason": "The plate contrasts right- and left-hand layup patterns; it does not show a reliable common-mistake comparison."},
    },
    "f2": {
        "summary": "Controlled direction change into a far-hand finish",
        "sequence": ["Approach the cone or line", "Change direction without slipping", "Finish with the hand farthest from the imaginary defender"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Far-hand protection versus exposing the ball with the near hand."},
    },
    "f3": {
        "summary": "Protected path under the basket to a far-side finish",
        "sequence": ["Choose the baseline path", "Protect the ball under the hoop", "Finish gently on the far side"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Far-side finish versus stopping on the near side."},
    },
    "d1": {
        "summary": "Balanced lateral slides with reset intervals",
        "sequence": ["Set a balanced stance", "Slide without crossing feet", "Rest and reset before repeating"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Feet apart during the slide versus crossed feet."},
    },
    "d2": {
        "summary": "Controlled closeout with a visible no-contact buffer",
        "sequence": ["Approach under control", "Shorten steps near the shooter", "Raise hands and stop without contact"],
        "comparison": {"included": False, "reason": "A wrong closeout image could normalize contact or entering a shooter's landing space; the plate shows safe spacing instead."},
    },
    "d3": {
        "summary": "Partner mirror steps at safe distance",
        "sequence": ["Start with safe spacing", "Mirror lateral movement", "Stop and reset if balance is lost"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Safe spacing versus crowding and crossing feet, without contact."},
    },
    "r1": {
        "summary": "Find the ball, establish legal position, locate the rebound",
        "sequence": ["Locate the ball", "Turn into legal position without pushing", "Find the rebound after holding position"],
        "comparison": {"included": False, "reason": "The generated lower inset depicted forearm contact and was cropped before bundling; the safe sequence remains."},
    },
    "r2": {
        "summary": "Safe two-hand rebound, balanced landing, controlled outlet",
        "sequence": ["Jump only when space is safe", "Secure with two hands and land balanced", "Pivot, then make a controlled outlet"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Balanced two-hand control versus an unstable one-hand recovery."},
    },
    "r3": {
        "summary": "Read a partner shot and rebound outside the landing area",
        "sequence": ["Partner shoots comfortably", "Read the bounce and move safely", "Secure the rebound and alternate roles"],
        "comparison": {"included": False, "reason": "A negative example could imply entering the shooter's landing area; the plate shows safe spacing only."},
    },
    "p1": {
        "summary": "Chest-pass and bounce-pass target points",
        "sequence": ["Set a clear partner target", "Use a chest or bounce pass", "Send the bounce to the floor point that reaches waist height"],
        "comparison": {"included": False, "reason": "The plate compares two valid pass types, not a common mistake."},
    },
    "p2": {
        "summary": "Pass, cut into space, show hands, receive",
        "sequence": ["Pass to the partner", "Cut decisively into open space", "Show hands and receive under control"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Cutting after the pass versus standing still."},
    },
    "p3": {
        "summary": "Secure, scan, pivot legally, and outlet to an open lane",
        "sequence": ["Secure the simulated rebound", "Scan and pivot without traveling", "Choose a clear lane and pass"],
        "comparison": {"included": True, "leftLabel": "GOOD REP", "rightLabel": "COMMON MISS", "note": "Planted pivot foot versus lifting the pivot foot."},
    },
}

FALLBACK = {
    "customDrills": "No drill-specific artwork is inferred; show the saved instructions and generic court geometry.",
    "missingBuiltInArtwork": "Show the existing schematic court and written coaching cues with an explicit offline-artwork notice.",
}


def parse_catalog() -> list[dict[str, str]]:
    source = CATALOG_PATH.read_text(encoding="utf-8")
    block = source.split("internal val drills = listOf(", 1)[1].split("\n)", 1)[0]
    drills = []
    for match in DRILL_PATTERN.finditer(block):
        drill_id, category, title, level, instructions, target, video_query = match.groups()
        drills.append({
            "id": drill_id,
            "category": category,
            "title": title,
            "level": level,
            "instructions": instructions,
            "target": target,
            "videoQuery": video_query,
        })
    if not drills:
        raise ValueError("The built-in drill catalog could not be parsed.")
    return drills


def webp_dimensions(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if len(data) < 30 or data[:4] != b"RIFF" or data[8:12] != b"WEBP":
        raise ValueError(f"{path.name} is not a complete RIFF WebP file.")
    declared_size = struct.unpack_from("<I", data, 4)[0] + 8
    if declared_size != len(data):
        raise ValueError(f"{path.name} has a truncated or trailing RIFF payload.")
    if data[12:16] != b"VP8 ":
        raise ValueError(f"{path.name} uses an unsupported WebP encoding; expected optimized lossy VP8.")
    frame = data[20:]
    if frame[3:6] != b"\x9d\x01\x2a":
        raise ValueError(f"{path.name} has an invalid VP8 frame header.")
    width = struct.unpack_from("<H", frame, 6)[0] & 0x3FFF
    height = struct.unpack_from("<H", frame, 8)[0] & 0x3FFF
    if not (1 <= width <= 4096 and 1 <= height <= 4096):
        raise ValueError(f"{path.name} has invalid dimensions {width}x{height}.")
    return width, height


def build_manifest() -> dict:
    catalog = parse_catalog()
    ids = [drill["id"] for drill in catalog]
    if len(ids) != len(set(ids)):
        raise ValueError("The built-in catalog contains duplicate drill IDs.")
    if set(ids) != set(VISUALS):
        raise ValueError(f"Visual plan mismatch; catalog-only={sorted(set(ids)-set(VISUALS))}, plan-only={sorted(set(VISUALS)-set(ids))}.")

    coaching_source = COACHING_PATH.read_text(encoding="utf-8")
    coaching_ids = set(COACHING_PATTERN.findall(coaching_source))
    if set(ids) != coaching_ids:
        raise ValueError(f"Coaching coverage mismatch; catalog-only={sorted(set(ids)-coaching_ids)}, coaching-only={sorted(coaching_ids-set(ids))}.")

    entries = []
    for drill in catalog:
        drill_id = drill["id"]
        visual = VISUALS[drill_id]
        asset_path = f"drill_art/{drill_id}.webp"
        file_path = ROOT / "app/src/main/assets" / asset_path
        if not file_path.is_file():
            raise FileNotFoundError(f"Missing illustration for {drill_id}: {file_path}")
        width, height = webp_dimensions(file_path)
        size = file_path.stat().st_size
        if size > 512_000:
            raise ValueError(f"{asset_path} exceeds the 512 KB offline-asset budget ({size} bytes).")
        comparison = visual["comparison"]
        if comparison.get("included"):
            if not comparison.get("leftLabel") or not comparison.get("rightLabel") or not comparison.get("note"):
                raise ValueError(f"{drill_id} has an incomplete visual-comparison description.")
        elif not comparison.get("reason"):
            raise ValueError(f"{drill_id} must explain why its comparison panel is omitted.")
        if len(visual["sequence"]) < 2:
            raise ValueError(f"{drill_id} is missing a multi-phase visual sequence.")

        entries.append({
            **drill,
            "assetPath": asset_path,
            "sha256": hashlib.sha256(file_path.read_bytes()).hexdigest(),
            "width": width,
            "height": height,
            "sizeBytes": size,
            "visualSummary": visual["summary"],
            "sequence": visual["sequence"],
            "comparison": comparison,
        })

    return {
        "schemaVersion": 1,
        "fallback": FALLBACK,
        "drills": entries,
    }


def render_coverage(manifest: dict) -> str:
    rows = []
    for entry in manifest["drills"]:
        comparison = entry["comparison"]
        comparison_status = (
            f"Yes — {comparison['leftLabel']} / {comparison['rightLabel']}"
            if comparison["included"]
            else f"Not shown — {comparison['reason']}"
        )
        rows.append(
            f"| `{entry['id']}` | {entry['title']} | `{entry['assetPath']}` "
            f"| {entry['width']} × {entry['height']} ({entry['sizeBytes'] // 1024} KiB) "
            f"| {len(entry['sequence'])} phases | {comparison_status} | PASS |"
        )
    return "\n".join([
        "# CourtQuest Pro Drill Illustration Coverage",
        "",
        f"Verified built-in drills: **{len(manifest['drills'])}/{len(manifest['drills'])}**.",
        "Every plate is bundled offline as optimized WebP under `app/src/main/assets/drill_art/`.",
        "The manifest is generated from the current Kotlin drill catalog and records exact source instructions, asset dimensions, byte size, and SHA-256.",
        "The upper panel provides the drill-specific movement sequence. A comparison panel is included only when it can be shown safely and accurately; exclusions are explained per drill.",
        "",
        "Custom drills have no inferred drill-specific illustration. They retain their saved instructions and use generic court geometry. Missing built-in artwork displays an explicit offline fallback.",
        "",
        "| ID | Built-in drill | Asset | Dimensions / size | Sequence | Form comparison | Verification |",
        "| --- | --- | --- | --- | --- | --- | --- |",
        *rows,
        "",
        "All assets passed RIFF/WebP integrity, dimension, SHA-256, filename mapping, and 512 KiB per-image budget checks. Android decode and display are verified separately by the native library workflow.",
        "",
    ])


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="Write the deterministic manifest and coverage report.")
    parser.add_argument("--check", action="store_true", help="Verify the committed manifest and report against source and assets.")
    args = parser.parse_args()

    manifest = build_manifest()
    manifest_text = json.dumps(manifest, indent=2, ensure_ascii=False) + "\n"
    report_text = render_coverage(manifest)
    if args.write:
        MANIFEST_PATH.write_text(manifest_text, encoding="utf-8")
        REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
        REPORT_PATH.write_text(report_text, encoding="utf-8")
    elif args.check:
        if not MANIFEST_PATH.is_file() or MANIFEST_PATH.read_text(encoding="utf-8") != manifest_text:
            raise SystemExit("The committed drill artwork manifest is missing or stale; run with --write.")
        if not REPORT_PATH.is_file() or REPORT_PATH.read_text(encoding="utf-8") != report_text:
            raise SystemExit("The committed drill artwork coverage report is missing or stale; run with --write.")
    else:
        parser.error("Choose --write or --check.")

    print(f"Verified {len(manifest['drills'])} built-in drill illustrations.")
    print(f"Total bundled artwork: {sum(entry['sizeBytes'] for entry in manifest['drills']):,} bytes.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
