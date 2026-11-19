# Jersey Studio material sources

`masters/` contains the source art for all four cuts. These files are outside the Android source set. The APK ships the eight `.jmat` files in `app/src/main/assets/jerseys/`.

| Style | Master | Construction |
| --- | --- | --- |
| Arena | `jersey_arena_front.bin`, `jersey_arena_back.bin` | Original photographic samples from commit `871eb4644f486ed53dd7ab11246807c7433bc05e` with their original five exposure pivots. |
| Classic | `classic.webp` | Lossless source from the approved graphite varsity pair. Scoop neck, straight piping, mesh and wide shoulders. |
| Fastbreak | `fastbreak.webp` | Original paired performance artwork generated from the approved performance-top reference. Split V, forward shoulder seam, curved panels and a split drop hem. |
| Retro | `retro.webp` | Lossless source from the approved heritage pair. Squared front neck, broad binding, straight body and stepped woven side tape. |

The three paired masters use neutral gray for the body, ivory for the secondary yarn, blue for the side, red for the collar and green for the armhole binding. The exporter reads those colors as zone identifiers. It retains the photographed variations in exposure, mesh, seams and folds. Both halves have their own silhouette and lighting.

## Rebuild the eight runtime assets

Run from the repository root with Python 3.11 or later. The verified authoring environment used Pillow 13.0.0, NumPy 2.3.5 and SciPy 1.17.0. The Android build and device do not need Python image-processing packages.

```bash
python3 -m venv /tmp/courtquest-jersey-art
/tmp/courtquest-jersey-art/bin/pip install Pillow==13.0.0 numpy==2.3.5 scipy==1.17.0
src=tools/jersey/masters
out=app/src/main/assets/jerseys
/tmp/courtquest-jersey-art/bin/python tools/jersey/pack_material.py arena "$src" --output "$out"
for style in classic fastbreak retro; do
  /tmp/courtquest-jersey-art/bin/python tools/jersey/pack_material.py "$style" "$src/$style.webp" --output "$out"
done
git diff --exit-code -- "$out"
```

## Runtime format and drawing

Each `CQJ1` file stores big-endian width and height as unsigned 16-bit integers followed by three cropped lossless WebP planes. A plane starts with its `x`, `y`, `width` and `height` as unsigned 16-bit integers, then its payload length as an unsigned 32-bit integer and its encoded bytes. Crops share the coordinate system of the original photograph.

The first RGB plane carries primary, secondary and side diffuse lighting. The second carries collar and armhole diffuse lighting. The third carries neutral specular highlights. Android applies two color matrices to the diffuse planes and draws highlights over the fitted print and fabric. The same cached plates feed the editor, style thumbnails and saved jersey icon. Palette changes update paint parameters. A mutex coalesces decode work off the UI thread. The cache budgets decoded bitmaps by allocation bytes and uses lower resolution for small controls.

## Verification

The read-only Jersey Studio candidate workflow compares the existing release signer, installs the new APK over v56 on `CourtQuest_PremiumQA`, verifies a saved Retro uniform and unrelated player history, then exercises the editor. Evidence includes full Android window screenshots, garment crops, unit-test and lint XML, upgrade receipts and Android frame timings. `comparison.py` assembles the normal-layout crops in fixed style order.
