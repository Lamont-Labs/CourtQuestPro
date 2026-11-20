"""Build anonymous, pose-registered hairstyle thumbnails from approved athlete art."""
import argparse
import hashlib
import io
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets/player_cards"
SOURCE = ROOT / "attached_assets/generated_images/player_card_standing.png"
OUT = ASSETS / "appearance-thumbnails"
MANIFEST = ASSETS / "appearance-thumbnails.json"
STYLES = (
    "original", "bald", "fade", "curls", "locs", "braids", "afro", "topknot",
    "waves", "straight", "brush", "boxbun", "twistknot", "sculpted",
)


def webp(image: Image.Image) -> bytes:
    stream = io.BytesIO()
    image.save(stream, "WEBP", lossless=True, method=6, exact=False)
    return stream.getvalue()


def render(style: str) -> bytes:
    with zipfile.ZipFile(ASSETS / "anatomy/standing.zip") as bundle:
        bounds = json.loads(bundle.read("bounds.json"))
        def patch(name: str) -> Image.Image:
            return Image.open(io.BytesIO(bundle.read(name + ".webp"))).convert("RGBA")

        canvas = Image.open(SOURCE).convert("RGBA")
        erase_bounds = bounds["erase"]
        erase = patch("erase").getchannel("A")
        current_alpha = canvas.getchannel("A")
        current_alpha.paste(ImageChops.subtract(current_alpha.crop(
            (erase_bounds[0], erase_bounds[1], erase_bounds[2], erase_bounds[3])), erase),
            (erase_bounds[0], erase_bounds[1]))
        canvas.putalpha(current_alpha)

        scalp_bounds = bounds["scalp"]
        canvas.alpha_composite(patch("scalp"), (scalp_bounds[0], scalp_bounds[1]))
        hair_key = "hair-" + style
        hair_bounds = bounds[hair_key]
        canvas.alpha_composite(patch(hair_key), (hair_bounds[0], hair_bounds[1]))

        # A lit, featureless shadow is used in the selector so thumbnails never
        # expose the source face. The live card still uses the chosen face style.
        face_bounds = bounds["face"]
        face_mask = patch("face").getchannel("A")
        shadow = Image.new("RGBA", face_mask.size, (29, 24, 23, 0))
        shadow.putalpha(face_mask.point(lambda alpha: round(alpha * 0.94)))
        canvas.alpha_composite(shadow, (face_bounds[0], face_bounds[1]))

    crop = canvas.crop((390, 0, 650, 255))
    alpha_bounds = crop.getchannel("A").getbbox()
    if alpha_bounds is None:
        raise ValueError(f"{style}: approved head crop is empty")
    crop = crop.crop(alpha_bounds)
    crop.thumbnail((128, 128), Image.Resampling.LANCZOS)
    tile = Image.new("RGBA", (128, 128))
    tile.alpha_composite(crop, ((128 - crop.width) // 2, (128 - crop.height) // 2))
    if tile.getchannel("A").getextrema() != (0, 255):
        raise ValueError(f"{style}: thumbnail lost transparent edges")
    return webp(tile)


def generate() -> dict:
    OUT.mkdir(parents=True, exist_ok=True)
    entries = {}
    for style in STYLES:
        data = render(style)
        path = OUT / f"hair-{style}.webp"
        path.write_bytes(data)
        entries[style] = {"file": f"appearance-thumbnails/hair-{style}.webp",
                          "sha256": hashlib.sha256(data).hexdigest(), "dimensions": [128, 128]}
    manifest = {"format": 1, "source": "approved standing athlete artwork; shadowed anonymous face",
                "styles": entries}
    MANIFEST.write_text(json.dumps(manifest, indent=2) + "\n")
    return manifest


def check() -> None:
    manifest = json.loads(MANIFEST.read_text())
    if set(manifest["styles"]) != set(STYLES):
        raise AssertionError("Hairstyle thumbnail manifest does not match the built-in catalog")
    for style in STYLES:
        item = manifest["styles"][style]
        path = ASSETS / item["file"]
        data = path.read_bytes()
        if hashlib.sha256(data).hexdigest() != item["sha256"]:
            raise AssertionError(f"{style}: thumbnail hash does not match manifest")
        if item["dimensions"] != [128, 128] or Image.open(io.BytesIO(data)).size != (128, 128):
            raise AssertionError(f"{style}: thumbnail dimensions are not 128 x 128")
        if Image.open(io.BytesIO(data)).convert("RGBA").getchannel("A").getextrema() != (0, 255):
            raise AssertionError(f"{style}: thumbnail must retain genuine transparency")
    print(f"Verified {len(STYLES)} transparent, face-anonymous hairstyle thumbnails.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.check:
        check()
    else:
        generate()
        check()
