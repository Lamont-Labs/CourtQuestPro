"""Pack an original zone-keyed garment photograph into Android's three lighting plates."""
import argparse
import io
import struct
import zlib
from pathlib import Path

import numpy as np
from PIL import Image
from scipy import ndimage


def catalog_photo(image, side):
    w, h = image.size
    half = image.crop((0 if side == 'front' else w // 2, 0, w // 2 if side == 'front' else w, h))
    rgba = np.array(half.convert('RGBA'))
    alpha = rgba[..., 3]
    components, count = ndimage.label(alpha > 90)
    if count == 0:
        raise ValueError('Transparent photo has no garment')
    body = components == np.bincount(components.ravel())[1:].argmax() + 1
    body = ndimage.binary_dilation(body, iterations=2)
    alpha = np.where(body, np.clip((alpha.astype(float) - 3) * 255 / 250, 0, 255), 0).astype('uint8')
    y, x = np.where(alpha > 0)
    ys, xs = slice(y.min(), y.max() + 1), slice(x.min(), x.max() + 1)
    rgb, alpha = rgba[ys, xs, :3].astype(float), alpha[ys, xs]
    r, g, b = np.moveaxis(rgb, -1, 0)
    zones = np.select([(r > g * 1.30) & (r > b * 1.25), (g > r * 1.22) & (g > b * 1.15),
                       (b > r * 1.22) & (b > g * 1.12), (r > b * 1.09) & (g > b * 1.045)],
                      [3, 4, 2, 1], 0)
    # Isolated chroma noise in graphite mesh isn't stitched accent yarn.
    zones = ndimage.median_filter(zones.astype('uint8'), size=3)
    brightness = rgb.max(axis=2)
    light = np.zeros_like(alpha)
    for zone in range(5):
        region = zones == zone
        values = brightness[region & (alpha > 225)]
        if len(values) < 250:
            raise ValueError(f'Missing textile zone {zone} on {side}')
        exposure = brightness[region] / np.percentile(values, 53)
        light[region] = np.rint(np.where(exposure <= 1, 128 * np.minimum(exposure, 1) ** 1.50,
                               128 + 127 * (1 - np.exp(-.78 * np.maximum(0, exposure - 1))))).astype('uint8')
    return light, zones, alpha, [128] * 5


def arena_photo(path):
    data = path.read_bytes()
    w, h = struct.unpack('>HH', data[:4]); samples = np.frombuffer(zlib.decompress(data[4:]), np.uint8).reshape(h, w, 3)
    return *np.moveaxis(samples, -1, 0), [34, 225, 128, 142, 127]


def pack(path, light, zones, alpha, midtones):
    height, width = light.shape
    pivot = np.take(midtones, zones)
    diffuse = np.rint(255 * np.minimum(light / pivot, 1)).astype('uint8')
    specular = np.rint(np.maximum(0, light - pivot) / (255 - pivot) * alpha).astype('uint8')
    plates = []
    for members in [(0, 1, 2), (3, 4)]:
        rgba = np.zeros((height, width, 4), np.uint8)
        for channel, zone in enumerate(members):
            rgba[..., channel] = np.where(zones == zone, diffuse, 0)
        rgba[..., 3] = np.where(np.isin(zones, members), alpha, 0)
        plates.append(Image.fromarray(rgba))
    highlights = Image.new('RGBA', (width, height), 'white'); highlights.putalpha(Image.fromarray(specular))
    plates.append(highlights)
    with path.open('wb') as out:
        out.write(struct.pack('>4sHH', b'CQJ1', width, height))
        for layer in plates:
            x, y, right, bottom = layer.getchannel('A').getbbox()
            cropped = layer.crop((x, y, right, bottom)); encoded = io.BytesIO()
            cropped.save(encoded, 'WEBP', lossless=True, method=6)
            data = encoded.getvalue()
            out.write(struct.pack('>HHHHI', x, y, cropped.width, cropped.height, len(data))); out.write(data)
    print(path.name, width, height, path.stat().st_size)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('style', choices=['arena', 'classic', 'fastbreak', 'retro']); parser.add_argument('source', type=Path)
    parser.add_argument('--output', type=Path, required=True); args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    for side in ('front', 'back'):
        samples = (arena_photo(args.source / f'jersey_arena_{side}.bin') if args.style == 'arena'
                   else catalog_photo(Image.open(args.source), side))
        pack(args.output / f'{args.style}_{side}.jmat', *samples)
