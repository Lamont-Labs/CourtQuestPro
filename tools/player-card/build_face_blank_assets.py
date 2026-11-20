"""Add or verify featureless, source-lit face layers in the offline pose bundles."""
import argparse
import hashlib
import io
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageChops, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets/player_cards"
SOURCES = ROOT / "attached_assets/generated_images"


def anonymous_face(source: Image.Image, face_mask: Image.Image) -> Image.Image:
    softened = source.filter(ImageFilter.GaussianBlur(12))
    mask=face_mask.convert("L")
    softened.putalpha(ImageChops.multiply(softened.getchannel("A"),mask))
    visible=mask.point(lambda value:255 if value else 0)
    red,green,blue,alpha=softened.split()
    return Image.merge("RGBA",(
        ImageChops.multiply(red,visible),ImageChops.multiply(green,visible),
        ImageChops.multiply(blue,visible),alpha
    ))


def encode(image: Image.Image) -> bytes:
    output = io.BytesIO()
    image.save(output, "WEBP", lossless=True, method=6, exact=False)
    return output.getvalue()


def create(pose: str, source_file: str, archive_path: Path) -> bytes:
    with zipfile.ZipFile(archive_path) as archive:
        order = [entry.filename for entry in archive.infolist()]
        files = {name: archive.read(name) for name in order}
    bounds = json.loads(files["bounds.json"])
    face = Image.open(io.BytesIO(files["face.webp"])).convert("RGBA")
    face_bounds = bounds["face"]
    source = Image.open(SOURCES / source_file).convert("RGBA")
    if source.size != (1024, 1024):
        raise AssertionError(f"{pose}: source is not a 1024px approved athlete")
    face_mask = Image.new("L", source.size)
    face_mask.paste(face.getchannel("A"), (face_bounds[0], face_bounds[1]))
    softened = anonymous_face(source, face_mask)
    crop = softened.crop((face_bounds[0], face_bounds[1], face_bounds[2], face_bounds[3]))
    if crop.getchannel("A").getbbox() is None:
        raise AssertionError(f"{pose}: blank-face layer is empty")
    encoded = encode(crop)
    bounds["face-blank"] = face_bounds
    files["face-blank.webp"] = encoded
    if "face-blank.webp" not in order:
        order.insert(order.index("bounds.json"), "face-blank.webp")
    files["bounds.json"] = json.dumps(bounds, separators=(",", ":")).encode()
    temp = archive_path.with_suffix(".zip.tmp")
    with zipfile.ZipFile(temp, "w", compression=zipfile.ZIP_STORED) as archive:
        for name in order:
            info = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            archive.writestr(info, files[name])
    temp.replace(archive_path)
    return encoded


def check(archive_path: Path, source_file: str) -> str:
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None:
            raise AssertionError(f"{archive_path.name}: invalid ZIP entry")
        bounds = json.loads(archive.read("bounds.json"))
        if "face-blank" not in bounds:
            raise AssertionError(f"{archive_path.name}: missing featureless face layer")
        embedded = archive.read("face-blank.webp")
        image = Image.open(io.BytesIO(embedded)).convert("RGBA")
        left, top, right, bottom = bounds["face-blank"]
        if image.size != (right - left, bottom - top):
            raise AssertionError(f"{archive_path.name}: blank-face image and matte bounds differ")
        if image.getchannel("A").getbbox() is None:
            raise AssertionError(f"{archive_path.name}: blank-face image has no visible pixels")
        face = Image.open(io.BytesIO(archive.read("face.webp"))).convert("RGBA")
        source = Image.open(SOURCES / source_file).convert("RGBA")
        mask = Image.new("L", source.size)
        mask.paste(face.getchannel("A"), (left, top))
        expected = encode(anonymous_face(source, mask).crop((left, top, right, bottom)))
        if hashlib.sha256(embedded).digest() != hashlib.sha256(expected).digest():
            raise AssertionError(f"{archive_path.name}: anonymous face layer is stale or not source-derived")
        return hashlib.sha256(embedded).hexdigest()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    hashes = {}
    for pose, detail in {
        "standing": "player_card_standing.png",
        "dribbling": "player_card_dribbling.png",
        "action": "player_card_action.png",
        "ready": "player_card_ready.png",
        "followthrough": "player_card_followthrough.png",
    }.items():
        path = ASSETS / "anatomy" / f"{pose}.zip"
        if args.check:
            hashes[pose] = check(path,detail)
        else:
            create(pose, detail, path)
            hashes[pose] = check(path,detail)
    print(f"Verified anonymous face layers for {len(hashes)} poses.")


if __name__ == "__main__":
    main()
