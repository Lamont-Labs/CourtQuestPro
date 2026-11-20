"""Create exact-coordinate shoe plates from pose-specific athlete edits."""
from __future__ import annotations

import argparse
import hashlib
import io
import json
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
POSES = ("standing", "dribbling", "action", "ready", "followthrough")
MODELS = ("low", "high", "strap")
SOURCES = ROOT / "attached_assets/generated_images/player_shoe_edits"
SOURCE_MANIFEST = SOURCES / "manifest.json"
EDIT_DIR = ROOT / "generated-artifacts/player-card-v3/authoring-shoes"
ASSETS = ROOT / "app/src/main/assets/player_cards"

# The high-collar model gets a small, pose-specific extension above the source
# shoe matte. Each polygon follows the visible upper-to-sock boundary; it is not
# a placement rectangle. All other shoe pixels use the approved foot silhouette.
HIGH_COLLARS = {
    "standing": [
        [(350, 885), (352, 873), (360, 861), (394, 859), (410, 867), (414, 879), (398, 886), (374, 886)],
    ],
    "dribbling": [
        [(228, 850), (236, 832), (250, 816), (275, 814), (289, 826), (290, 840), (274, 849), (251, 853)],
        [(658, 857), (663, 841), (675, 827), (696, 827), (706, 838), (704, 850), (690, 858), (672, 860)],
    ],
    "action": [
        [(323, 876), (327, 861), (339, 849), (371, 848), (386, 858), (390, 871), (375, 878), (347, 879)],
        [(594, 653), (599, 635), (613, 620), (643, 618), (661, 630), (666, 646), (649, 654), (620, 656)],
    ],
    "ready": [],
    "followthrough": [],
}


def read_cqp(path: Path, expected_count: int) -> list[Image.Image]:
    import struct

    with path.open("rb") as stream:
        magic, width, height, count = struct.unpack(">4sHHB", stream.read(9))
        if (magic, width, height, count) != (b"CQP2", 1024, 1024, expected_count):
            raise ValueError(f"Unexpected card plate: {path}")
        layers = []
        for _ in range(count):
            size = struct.unpack(">I", stream.read(4))[0]
            layers.append(Image.open(io.BytesIO(stream.read(size))).convert("RGBA"))
        if stream.read(1):
            raise ValueError(f"Trailing data in {path}")
    return layers


def foreground_alpha(image: Image.Image) -> Image.Image:
    rgb = np.asarray(image.convert("RGB"))
    light = (rgb.min(axis=2) > 215) & ((rgb.max(axis=2) - rgb.min(axis=2)) < 34)
    flood = Image.fromarray((light * 255).astype("uint8"), "L").filter(ImageFilter.MaxFilter(3))
    ImageDraw.floodfill(flood, (0, 0), 128)
    return Image.fromarray((np.asarray(flood) != 128).astype("uint8") * 255, "L")


def extract(edit_dir: Path) -> int:
    SOURCES.mkdir(parents=True, exist_ok=True)
    manifest={}
    for pose in POSES:
        base = read_cqp(ASSETS / f"{pose}-avatar.pcmat", 2)
        shoe_mask = base[1].getchannel("A").filter(ImageFilter.MaxFilter(3))
        for model in MODELS:
            source = edit_dir / f"{pose}-{model}-edit.png"
            if not source.is_file():
                raise FileNotFoundError(source)
            edited = Image.open(source).convert("RGBA")
            if edited.size != (1024, 1024):
                raise ValueError(f"Expected 1024px pose plate: {source}")
            alpha = shoe_mask.copy()
            if model == "high":
                extension = Image.new("L", (1024, 1024))
                draw = ImageDraw.Draw(extension)
                for polygon in HIGH_COLLARS[pose]:
                    draw.polygon(polygon, fill=255)
                rgb=np.asarray(edited.convert("RGB"))
                dark_shoe=(rgb.max(axis=2)<210)&(np.asarray(foreground_alpha(edited))>0)
                high_pixels=(np.asarray(extension)>0)&dark_shoe
                alpha = Image.fromarray(np.maximum(np.asarray(alpha), high_pixels.astype("uint8")*255), "L")
            shoe = edited.copy()
            shoe.putalpha(alpha)
            target = SOURCES / f"{pose}-{model}.png"
            box=alpha.getbbox()
            if box is None:
                raise ValueError(f"Empty shoe plate: {source}")
            shoe.crop(box).save(target, optimize=True)
            manifest[f"{pose}/{model}"]={
                "file":target.name,"sha256":hashlib.sha256(target.read_bytes()).hexdigest(),
                "origin":[box[0],box[1]],"dimensions":[box[2]-box[0],box[3]-box[1]],
                "source_pose":pose,"model":model,
                "high_collar_polygons":HIGH_COLLARS[pose] if model=="high" else [],
                "attachment":"authored pose coordinate cutout; no per-user scale or rotation",
            }
    SOURCE_MANIFEST.write_text(json.dumps(manifest,indent=2)+"\n")
    return len(POSES) * len(MODELS)


def check() -> None:
    manifest=json.loads(SOURCE_MANIFEST.read_text())
    if len(manifest)!=len(POSES)*len(MODELS):
        raise AssertionError("Footwear source matrix is incomplete")
    for pose in POSES:
        for model in MODELS:
            item=manifest[f"{pose}/{model}"]
            path=SOURCES/item["file"]
            if hashlib.sha256(path.read_bytes()).hexdigest()!=item["sha256"]:
                raise ValueError(f"Footwear source hash mismatch: {path}")
            with Image.open(path) as image:
                image.load()
                if image.mode != "RGBA" or image.size != tuple(item["dimensions"]):
                    raise ValueError(f"Invalid footwear layer: {path}")
                alpha = image.getchannel("A")
                if alpha.getextrema() != (0, 255) or not alpha.getbbox():
                    raise ValueError(f"Footwear layer needs a real transparent matte: {path}")
                if alpha.getbbox() != (0,0,*image.size):
                    raise ValueError(f"Footwear art must be tightly cropped: {path}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--extract-edits", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.extract_edits:
        print(f"Extracted {extract(args.extract_edits)} pose-matched shoe plates.")
    if args.check:
        check()
        print("Verified all 15 transparent, pose-matched shoe plates.")
    if not (args.extract_edits or args.check):
        parser.error("Choose --extract-edits and/or --check")


if __name__ == "__main__":
    main()
