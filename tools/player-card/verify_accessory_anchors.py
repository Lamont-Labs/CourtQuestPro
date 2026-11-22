"""Require each pose's wrist/sleeve control points to land on its registered skin matte."""
import io
import json
import re
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
LAYOUT = ROOT / "app/src/main/java/com/courtquestpro/CardPoseLayout.kt"
ANATOMY = ROOT / "app/src/main/assets/player_cards/anatomy"


def pose_arm_points(source: str, pose: str) -> list[float]:
    start = source.index(f"PlayerCardPose.{pose.upper()}->CardPoseLayout")
    end = source.find("PlayerCardPose.", start + 1)
    block = source[start:end if end >= 0 else len(source)]
    lists = re.findall(r"listOf\(([^)]*)\)", block)
    if len(lists) < 3:
        raise AssertionError(f"{pose}: arm coordinates are missing from CardPoseLayout")
    values = [float(value.strip().removesuffix("f")) for value in lists[2].split(",")]
    if len(values) != 6:
        raise AssertionError(f"{pose}: expected shoulder, elbow and wrist points")
    return values


def verify() -> list[str]:
    source = LAYOUT.read_text()
    checked = []
    for pose in ("standing", "dribbling", "action", "ready", "followthrough"):
        points = pose_arm_points(source, pose)
        with zipfile.ZipFile(ANATOMY / f"{pose}.zip") as bundle:
            manifest = json.loads(bundle.read("bounds.json"))
            bounds = manifest["skin"]
            skin = Image.open(io.BytesIO(bundle.read("skin.webp"))).convert("RGBA").getchannel("A")
            blank = Image.open(io.BytesIO(bundle.read("face-blank.webp"))).convert("RGBA")
            blank_bounds = manifest["face-blank"]
            if blank.size != (blank_bounds[2] - blank_bounds[0], blank_bounds[3] - blank_bounds[1]):
                raise AssertionError(f"{pose}: featureless face layer does not match its registration")
            if blank.getchannel("A").getbbox() is None:
                raise AssertionError(f"{pose}: featureless face layer is empty")
        left, top, right, bottom = bounds
        # The sleeve intentionally begins under the jersey at the shoulder;
        # elbow and wrist must be on exposed skin so the band cannot be erased.
        for name, x, y in zip(("elbow", "wrist"), points[2::2], points[3::2]):
            if not (left <= x < right and top <= y < bottom):
                raise AssertionError(f"{pose}/{name} ({x},{y}) is outside the skin matte")
            alpha = skin.getpixel((round(x) - left, round(y) - top))
            if alpha < 160:
                raise AssertionError(f"{pose}/{name} ({x},{y}) misses skin matte (alpha={alpha})")
        checked.append(pose)
    return checked


if __name__ == "__main__":
    print("Verified pose-registered arm accessory anchors: " + ", ".join(verify()))
