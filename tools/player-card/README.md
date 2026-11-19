# Avatar and collector-material revision

The avatar inspector adds six hair choices, four headwear choices, three
illustrated builds, seven skin choices, six shoe colorways and four accessory
combinations. Original choices retain the shipped art. These are cosmetic
illustration settings, not body measurements or performance claims.
Pinned Hair/Body/Gear tools keep categories reachable while scrolling. Hair and
shoe colorways have pose-aware detail views beside the full live collectible;
body and accessory choices retain the full-body view.

`build_avatar_assets.py` derives transparent skin and shoe plates from the
committed original athlete plates. Garment alpha is explicitly subtracted from
skin. Skin/shoe color matrices retain source light and shade; modest localized
bitmap meshes share their deformation with uniform lettering and accessory
anchors. Heads and distal hands remain outside the body deformation.
Hair/headband details are native pose-anchored drawings; sleeves and wrist tape
are composited through the skin matte. This is a 2D customization system, not a
3D character generator or realistic physical tailoring simulation.

`build_card_surfaces.py` optimizes original generated material artwork into
`surface-holographic.webp`, `surface-classic.webp` and `surface-battle.webp`.
They contain no athlete, branding, player name, serial number, stats or uniform.
Native foreground layers retain dynamic identity and the approved Signature logo.
`surface-manifest.json` records source names, dimensions and hashes; the avatar
manifest records pose mattes and hashes. All production plates work offline.

Run `python3 -m unittest scripts.test_player_card_artwork -v` in addition to
the original plate check. Native tests exercise every cosmetic option on every
pose at normal size, plus reachable selections at compact/200% text, save,
restart, draft recreation, discard, and unchanged training state.

# CourtQuest Player Card V2 assets

Three original generated, fictional adult athletes are bundled offline. They are
illustrations, not photographs, real-person likenesses or performance measurements.
The subject is anonymous in all three face treatments. No external image service
is called by the Android application.

## Production source

`app/src/main/assets/player_cards/{standing,dribbling,action}.pcmat` contains all
production artwork, including the complete lossless original subject layer.
Each `CQP2` file has a big-endian signature, 1024 × 1024 dimensions, an 11-layer
count, then length-prefixed lossless WebP layers:

1. Original full-body illustration and genuine alpha.
2. Pose-specific anonymous face mask.
3. Arena body textile light map (R/G/B = fabric/secondary/side).
4. Arena bindings (R/G = collar/armhole).
5–10. The same two maps for Classic, Fastbreak and Retro.
11. Cloth highlights for live lettering and fabric.

The manifest records source names, hand-authored polygons, print quads, face
coordinates, byte counts and SHA-256 digests. `build_assets.py --check` validates
every layer, image dimensions, transparency, file format and digest.
`build_assets.py` rebuilds from the generated PNGs when those working masters
are present. Those temporary generation files are not required at runtime or
for Android compilation; all actual art is present inside the versioned plates.

## Usage and limits

Athlete garment masks preserve the generated mesh lighting and folds while
dying all five cloth zones at draw time. The two ink roles supply live chest
branding and number, including `00` and leading zeros. Card names use the
saved player name. No copy of the uniform is stored in card preferences, so
Jersey Studio changes propagate immediately without saving the card again.
Pose-specific panels adapt each named template to the worn garment; these are
illustrated adaptations, not a 3D cloth simulation or an exact physical tailoring
preview. Player names are printed on the collectible identity; the front-facing
uniform retains front chest branding rather than inventing a back view.

All three editions have authored native geometry and typography: prism foil /
dimensional silver, heritage metal and engraved court texture, and angular
orange battle lighting. They are not flattened screenshots or web components.
Generated shoe trademarks were removed from the standing production plate.

`PlayerCardMaterials` uses serialized background decoding and a bounded 48 MiB
cache. Thumbnail/phone renders sample the assets; color changes never re-decode
bitmaps. Studio changes crossfade briefly through Compose (which respects the
system animation-duration scale); no perpetual animations or particles run.
Card graphics expose a full accessible description, and the settings controls
retain scalable native text and 48dp targets.

The emulator verifier reuses the guarded CourtQuest QA AVD and records actual
Android screenshots for all 27 edition/pose/face combinations at normal,
compact and 200% text. It additionally exercises save/exit, cold-start
persistence, recreation, discard, Jersey Studio synchronization, existing
jersey customization, navigation/walkthrough and training-entry tests.
Screenshots use disposable synthetic QA data, never a user's phone.
