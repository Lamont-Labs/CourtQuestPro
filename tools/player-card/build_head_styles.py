"""Extract pose-matched hair/head plates from approved athlete edit passes.

The AI edit is used only inside a traced head/neck region. The original body,
uniform, ball, limbs, lighting outside that region, and player artwork remain
the source of truth. The native app bundles transparent head-only WebP plates.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
POSES = ("standing", "dribbling", "action", "ready", "followthrough")
STYLES = ("bald", "fade", "curls", "locs", "braids", "afro", "topknot", "waves")
ASSET_ROOT = ROOT / "app/src/main/assets/player_cards/heads"
SOURCE_ROOT = ROOT / "attached_assets/generated_images/player_head_edits"
MANIFEST = ROOT / "app/src/main/assets/player_cards/head-style-manifest.json"

# Curved head-and-neck silhouettes in source-plate coordinates, not rectangular
# wig boxes. Crop bounds are derived from these points.
REGIONS = {
    "standing": [(430, 0), (585, 0), (583, 75), (565, 112), (550, 145),
                 (540, 168), (482, 168), (472, 145), (450, 112), (434, 75)],
    "dribbling": [(452, 0), (620, 0), (617, 72), (601, 108), (580, 137),
                  (568, 172), (555, 193), (492, 193), (484, 170), (468, 137),
                  (454, 108), (449, 72)],
    "action": [(382, 0), (520, 0), (516, 60), (502, 91), (483, 119),
               (475, 145), (462, 161), (421, 161), (411, 143), (399, 117),
               (386, 85)],
    "ready": [(438, 10), (586, 10), (583, 70), (565, 105), (552, 131),
              (545, 160), (481, 160), (472, 136), (455, 108), (441, 76)],
    # The source follow-through arms pass close to either side of the head.
    # Keep their pixels in the approved base plate instead of accidentally
    # copying an arm/background fragment into the hairstyle head layer.
    "followthrough": [(478, 130), (560, 130), (560, 170), (550, 198),
                      (548, 222), (545, 269), (512, 269), (505, 243),
                      (492, 220), (480, 194), (474, 165)],
}

EDIT_NAMES = {
    ("standing", "locs"): "standing-fullbody-longloc-edit.png",
    **{(pose, "waves"): f"{pose}-waves-full-edit.png" for pose in POSES},
    **{(pose, style): f"{pose}-{style}-full-edit.png"
       for pose in POSES for style in STYLES if (pose, style) != ("standing", "locs")},
}


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def foreground_alpha(image: Image.Image) -> Image.Image:
    """Remove the image editor's baked checker/white background by border flood.

    The thresholded light matte is dilated first so checker tiles that touch
    diagonally join. Only the light component connected to the image border is
    removed; bright uniform pixels enclosed by the athlete remain foreground.
    """
    rgb = np.asarray(image.convert("RGB"))
    light = (rgb.min(axis=2) > 215) & ((rgb.max(axis=2) - rgb.min(axis=2)) < 34)
    flood = Image.fromarray((light * 255).astype("uint8"), "L").filter(ImageFilter.MaxFilter(3))
    ImageDraw.floodfill(flood, (0, 0), 128)
    is_background = np.asarray(flood) == 128
    alpha = Image.fromarray((~is_background * 255).astype("uint8"), "L")
    return alpha.filter(ImageFilter.GaussianBlur(0.35))


def edit_path(edit_dir: Path, pose: str, style: str) -> Path:
    return edit_dir / EDIT_NAMES[(pose, style)]


def extract(edit_dir: Path) -> dict:
    SOURCE_ROOT.mkdir(parents=True, exist_ok=True)
    result = {}
    for pose in POSES:
        polygon = REGIONS[pose]
        xs, ys = zip(*polygon)
        crop_box = (min(xs), min(ys), max(xs) + 1, max(ys) + 1)
        region = Image.new("L", (1024, 1024))
        ImageDraw.Draw(region).polygon(polygon, fill=255)
        region = region.filter(ImageFilter.GaussianBlur(0.9))
        for style in STYLES:
            edited_path = edit_path(edit_dir, pose, style)
            if not edited_path.is_file():
                raise FileNotFoundError(f"Missing approved edit: {edited_path}")
            edited = Image.open(edited_path).convert("RGBA")
            if edited.size != (1024, 1024):
                raise ValueError(f"{edited_path} is {edited.size}; expected 1024x1024")
            alpha = ImageChops.multiply(foreground_alpha(edited), region)
            patch = edited.copy()
            patch.putalpha(alpha)
            head = patch.crop(crop_box)
            if head.getchannel("A").getbbox() is None:
                raise ValueError(f"Empty extracted head for {pose}/{style}")
            target = SOURCE_ROOT / f"{pose}-{style}.png"
            head.save(target, optimize=True)
            result[f"{pose}/{style}"] = {
                "source_edit": edited_path.name,
                "source_sha256": sha(edited_path),
                "source_head": f"player_head_edits/{target.name}",
                "head_sha256": sha(target),
                "crop": list(crop_box),
                "polygon": polygon,
                "dimensions": list(head.size),
            }
    return result


def build() -> dict:
    sources = {}
    assets = {}
    clean_bases={}
    ASSET_ROOT.mkdir(parents=True, exist_ok=True)
    for pose in POSES:
        source_body=Image.open(ROOT/f"attached_assets/generated_images/player_card_{pose}.png").convert("RGBA")
        region=Image.new("L",source_body.size)
        ImageDraw.Draw(region).polygon(REGIONS[pose],fill=255)
        remove=ImageChops.multiply(source_body.getchannel("A"),region)
        clean=source_body.copy()
        clean.putalpha(ImageChops.subtract(source_body.getchannel("A"),remove))
        clean_path=ASSET_ROOT/f"cleared-{pose}.webp"
        clean.save(clean_path,"WEBP",lossless=True,quality=100,method=6)
        clean_bases[pose]={
            "file":f"heads/{clean_path.name}",
            "sha256":sha(clean_path),
            "dimensions":list(clean.size),
            "source_sha256":sha(ROOT/f"attached_assets/generated_images/player_card_{pose}.png"),
            "cleared_region":REGIONS[pose],
        }
        for style in STYLES:
            key = f"{pose}/{style}"
            source = SOURCE_ROOT / f"{pose}-{style}.png"
            if not source.is_file():
                raise FileNotFoundError(f"Missing transparent head art: {source}")
            with Image.open(source) as image:
                image = image.convert("RGBA")
                if image.getchannel("A").getextrema()[0] != 0:
                    raise ValueError(f"Head art must retain true transparency: {source}")
                if image.getchannel("A").getbbox() is None:
                    raise ValueError(f"Empty head art: {source}")
                target = ASSET_ROOT / f"{pose}-{style}.webp"
                image.save(target, "WEBP", lossless=True, quality=100, method=6)
                assets[key] = {
                    "file": f"heads/{target.name}",
                    "sha256": sha(target),
                    "dimensions": list(image.size),
                    "source_sha256": sha(source),
                }
            sources[key] = {
                "source_head": f"player_head_edits/{source.name}",
                "source_sha256": sha(source),
            }
    return {"format": 1, "poses": list(POSES), "styles": list(STYLES),
            "purpose": "Pose-matched transparent head plates; original body and live jersey remain native layers.",
            "cleanBases":clean_bases,"assets": assets}


def check() -> None:
    manifest = json.loads(MANIFEST.read_text())
    assert manifest["poses"] == list(POSES) and manifest["styles"] == list(STYLES)
    assert len(manifest["assets"]) == len(POSES) * len(STYLES)
    assert set(manifest["cleanBases"])==set(POSES)
    for pose,item in manifest["cleanBases"].items():
        path=ROOT/"app/src/main/assets/player_cards"/item["file"]
        source=ROOT/f"attached_assets/generated_images/player_card_{pose}.png"
        assert source.is_file() and sha(source)==item["source_sha256"],source
        assert path.is_file() and sha(path)==item["sha256"],path
        with Image.open(path) as image:
            assert image.mode=="RGBA" and image.size==(1024,1024)
    for pose in POSES:
        for style in STYLES:
            item = manifest["assets"][f"{pose}/{style}"]
            path = ROOT / "app/src/main/assets/player_cards" / item["file"]
            source=SOURCE_ROOT/f"{pose}-{style}.png"
            assert source.is_file() and sha(source)==item["source_sha256"],source
            assert path.is_file() and sha(path) == item["sha256"], path
            with Image.open(path) as image:
                assert image.mode == "RGBA" and image.size == tuple(item["dimensions"])
                assert image.getchannel("A").getextrema()[0] == 0
                assert image.getchannel("A").getbbox()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--extract-edits", type=Path)
    parser.add_argument("--build", action="store_true")
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.extract_edits:
        extracted = extract(args.extract_edits)
        print(f"Extracted {len(extracted)} pose-matched head plates.")
    if args.build:
        MANIFEST.write_text(json.dumps(build(), indent=2) + "\n")
        print("Built optimized hairstyle plates and manifest.")
    if args.check:
        check()
        print(f"Verified {len(POSES) * len(STYLES)} transparent, hashed head plates.")
    if not (args.extract_edits or args.build or args.check):
        parser.error("Choose --extract-edits, --build, and/or --check")


if __name__ == "__main__":
    main()
