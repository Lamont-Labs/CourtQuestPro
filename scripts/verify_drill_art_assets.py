"""Fail-fast integrity checks for CourtQuest drill illustrations."""
from pathlib import Path
import hashlib
import json
import struct

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
MANIFEST = ASSETS / "drill_art/manifest.json"


def webp_dimensions(data: bytes):
    if len(data) < 30 or data[:4] != b"RIFF" or data[8:12] != b"WEBP":
        raise ValueError("Not a WebP RIFF file")
    if int.from_bytes(data[4:8], "little") + 8 != len(data):
        raise ValueError("Invalid WebP RIFF length")
    kind = data[12:16]
    if kind == b"VP8X":
        return (1 + int.from_bytes(data[24:27], "little"),
                1 + int.from_bytes(data[27:30], "little"))
    if kind == b"VP8L":
        if data[20] != 0x2f:
            raise ValueError("Invalid lossless WebP header")
        bits = int.from_bytes(data[21:25], "little")
        return (1 + (bits & 0x3fff), 1 + ((bits >> 14) & 0x3fff))
    if kind == b"VP8 ":
        if data[23:26] != b"\x9d\x01\x2a":
            raise ValueError("Invalid lossy WebP frame header")
        return (int.from_bytes(data[26:28], "little") & 0x3fff,
                int.from_bytes(data[28:30], "little") & 0x3fff)
    raise ValueError(f"Unsupported WebP chunk {kind!r}")


def main():
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    assert manifest["schemaVersion"] == 1
    entries = manifest["drills"]
    assert entries, "No drill illustrations declared"
    ids, paths = set(), set()
    errors = []
    for entry in entries:
        name = entry.get("id", "?")
        try:
            assert name not in ids, "Duplicate drill id"
            ids.add(name)
            assert entry["title"].strip(), "Missing title"
            relative = Path(entry["assetPath"])
            assert not relative.is_absolute() and ".." not in relative.parts, "Unsafe asset path"
            assert relative.as_posix().startswith("drill_art/"), "Artwork must stay in drill_art"
            assert relative.suffix.lower() == ".webp", "Artwork must be WebP"
            assert relative.as_posix() not in paths, "Duplicate artwork path"
            paths.add(relative.as_posix())
            asset = ASSETS / relative
            data = asset.read_bytes()
            assert len(data) == entry["sizeBytes"], "Size mismatch"
            assert hashlib.sha256(data).hexdigest() == entry["sha256"], "SHA-256 mismatch"
            assert webp_dimensions(data) == (entry["width"], entry["height"]), "Dimension mismatch"
            assert entry["width"] > 0 and entry["height"] > 0, "Invalid dimensions"
            assert entry["visualSummary"].strip(), "Missing visual summary"
            assert entry["sequence"] and all(str(step).strip() for step in entry["sequence"]), "Missing coaching sequence"
            comparison = entry["comparison"]
            assert isinstance(comparison["included"], bool), "Invalid comparison flag"
            if comparison["included"]:
                assert comparison["leftLabel"].strip() and comparison["rightLabel"].strip(), "Missing comparison labels"
                assert comparison["note"].strip(), "Missing comparison guidance"
        except (AssertionError, KeyError, OSError, ValueError, TypeError) as exc:
            errors.append(f"{name}: {exc}")
    actual = {p.relative_to(ASSETS).as_posix() for p in (ASSETS / "drill_art").glob("*.webp")}
    for extra in sorted(actual - paths):
        errors.append(f"Unlisted artwork: {extra}")
    if errors:
        raise SystemExit("Drill artwork integrity FAILED:\n" + "\n".join(errors))
    print(f"Drill artwork integrity PASS: {len(entries)} plates, hashes, dimensions, metadata")


if __name__ == "__main__":
    main()
