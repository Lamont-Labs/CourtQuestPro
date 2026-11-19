#!/usr/bin/env python3
"""Pack a transparent front/back photographic garment sheet as Android material atlases."""
import argparse
from pathlib import Path
import struct
import zlib

import numpy as np
from PIL import Image
from scipy import ndimage


def garment(sheet):
    """Isolate the largest garment and drop residual nearly transparent image noise."""
    rgba = np.array(sheet.convert("RGBA"))
    labels, count = ndimage.label(rgba[:, :, 3] > 96)
    if not count:
        raise ValueError("Missing transparent-background garment")
    sizes = np.bincount(labels.ravel())
    sizes[0] = 0
    body = labels == sizes.argmax()
    edge = ndimage.binary_dilation(body, iterations=2)
    rgba[:, :, 3] = np.where(edge, rgba[:, :, 3], 0)
    isolated = Image.fromarray(rgba)
    cropped = isolated.crop(isolated.getbbox())
    cropped.thumbnail((500, 860), Image.Resampling.LANCZOS)
    return cropped


def encode(image):
    r, g, b, alpha = np.moveaxis(np.array(image, dtype=np.int16), -1, 0)
    cream = (r - b > 12) & (g - b > 6) & (r - g < 29) & (g > 115)
    cobalt = (b - np.maximum(r, g) > 14) & (b - r > 23)
    crimson = (r - np.maximum(g, b) > 20) & (r - g > 26)
    emerald = (g - np.maximum(r, b) > 12) & (g - r > 18)
    zone = np.select((cream, cobalt, crimson, emerald), (1, 2, 3, 4)).astype(np.uint8)
    luma = r * .24 + g * .69 + b * .07
    exposure = np.empty_like(zone)
    counts = []
    for material in range(5):
        mask = zone == material
        opaque = mask & (alpha > 225)
        count = int(opaque.sum())
        if count < 350:
            raise ValueError(f"Material {material} needs 350 opaque pixels, found {count}")
        counts.append(count)
        # Put the chosen RGB at the region's actual photographic midtone.
        # Amplify the exposed accent dyes just enough to retain their stitching and seams.
        pivot = np.median(luma[opaque])
        exposure[mask] = np.clip(
            (luma[mask] - pivot) * (1.28 if material else 1.12) + 128, 0, 255
        ).round().astype(np.uint8)
    atlas = np.stack((exposure, zone, alpha.astype(np.uint8)), axis=-1)
    atlas[alpha == 0] = 0
    return struct.pack(">HH", *image.size) + zlib.compress(atlas.tobytes(), level=9), counts


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("style", choices=("classic", "fastbreak", "retro"))
    parser.add_argument("sheet", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    with Image.open(args.sheet) as sheet:
        width, height = sheet.size
        views = (("front", 0, width // 2), ("back", width // 2, width))
        for side, left, right in views:
            image = garment(sheet.crop((left, 0, right, height)))
            atlas, counts = encode(image)
            path = args.destination / f"jersey_{args.style}_{side}.bin"
            path.write_bytes(atlas)
            print(f"{path}: {image.width}x{image.height}, {len(atlas)} bytes, regions {counts}")


if __name__ == "__main__":
    main()
