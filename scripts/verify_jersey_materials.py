"""Check the eight checked-in garments, lossless masks, and zone coverage."""
from collections import Counter
from itertools import product
from pathlib import Path
import struct
import zlib


def main():
    root = Path(__file__).resolve().parents[1] / "app/src/main/res/raw"
    silhouettes = {}
    for style, side in product(("arena", "classic", "fastbreak", "retro"), ("front", "back")):
        filename = root / f"jersey_{style}_{side}.bin"
        raw = filename.read_bytes()
        width, height = struct.unpack(">HH", raw[:4])
        assert 300 <= width <= 550 and 550 <= height <= 900, (filename, width, height)
        decoded = zlib.decompress(raw[4:])
        assert len(decoded) == width * height * 3, filename
        zones = decoded[1::3]
        alpha = decoded[2::3]
        assert max(zones) == 4 and min(alpha) == 0 and max(alpha) == 255, filename
        counts = Counter(zone for zone, opacity in zip(zones, alpha) if opacity > 225)
        assert all(counts[zone] >= 350 for zone in range(5)), (filename, counts)
        shape = alpha.translate(bytes(0 if x < 128 else 1 for x in range(256)))
        assert shape not in silhouettes, (filename, silhouettes.get(shape))
        silhouettes[shape] = filename.name
        print(f"{filename.name}: {width}x{height}, {len(raw)} bytes, "
              f"solid zones {[counts[zone] for zone in range(5)]}")


if __name__ == "__main__":
    main()
