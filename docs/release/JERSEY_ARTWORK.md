# Photographic Jersey Studio artwork

| Style | Photographic construction | Candidate artwork source SHA-256 |
| --- | --- | --- |
| Arena | Existing dimensional mesh with contrast piping. Reuses the original v54 atlases byte for byte. | Existing release resources under `app/src/main/res/raw`. |
| Classic | Scoop collar, generous shoulders, and straight varsity side piping. | `d41e92773cb756d3bff50cdc2142dc533c1056a4e8c64bb989b266b14fe0f915` |
| Fastbreak | Angular V-neck, tapered mesh, and shaped performance panels. | `aee0dea646a44f3ec4c1b8d41e08fe34d3a27d8feb2975a1b04897dcbab09381` |
| Retro | Square neck, broad rib bindings, and stepped heritage trim. | `f2a70172aecd5c39854868f040ba23ab87a5c7f9a2949ccd6d31b675623c75df` |

The three original garments were made with ImageGen as transparent front/back sheets. The prompt specified separate graphite, cream, cobalt, crimson, and emerald textile regions, ghost-mannequin basketball tailoring, realistic mesh and clear fabric geometry, and blank chest/back for app-rendered typography. Fastbreak was regenerated as two jerseys without shorts. No third-party marks are present. The source sheets are authoring inputs, while the versioned material atlases are runtime artwork. `scripts/pack_jersey_materials.py` packs new sheets and reports per-region pixel coverage.

The Android renderer decodes a garment matte, zone index, and luminance. It applies the five chosen fabric inks, draws separately colored names and numbers, and uses anti-aliased contrasting outlines where the selected inks are close. The renderer uses a smaller raster for thumbnails and the dock. It preserves saved styles and color fields without a data migration. Each atlas is checked for bounds, integrity, transparency, five editable textile roles, and a distinct silhouette.

`python3 scripts/verify_jersey_materials.py` verifies the eight bundled photomaterial resources.
