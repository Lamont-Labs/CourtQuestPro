# Card Studio v60 recovery and candidate

The input is development commit `0b3ff087d0153a06d7bec694f266b25f453e0fea`, versionCode 58. The last release tag at discovery was v56. Action 44797671170 supplies the signed v58 APK and 44631807988 supplies v57. All three use the established phone-update signer with SHA-256 `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`.

| v58 observation | v59 implementation |
|---|---|
| Replit commit 899f1db supplied athlete art. Four material plates and five poses reached Compose. | The same athletes, textile maps, folds and material plates drive the profile and editor. |
| Replit commit 387a3a6 supplied four texture masters outside the Android assets. v58 used separate authored pose/head crops. | Brushed crop, box-braid bun, twist knot and sculpted waves register the four masters to the posed crowns. |
| Head plates contained both hair and warm skin and painted over the body complexion. A footwear change could select a second base with the old head. | One canonical athlete supports every shoe and hairstyle. Skin, hair, footwear and uniform are independently registered and painted. |
| The v58 skin filter preserved input luminance for every swatch. Original shoe cutouts omitted bright rubber and portions of the toe. | The selected complexion changes exposure while preserving local relief. Shoe registration follows both ankle seams and the full visible foot, including the sole. |
| Source and device tests covered selections but had no statistical reverse, saved side or flip. | A single two-sided native card is used from Me and the editor. A separate readable record supports large text and reduced motion. |

## v60 reconstruction

The v59 source at branch head `9b43402a076579bf1731e3c8712338ab3fcc62f1` is the recovery baseline. Its package job produced a versionCode 59 APK in run 44809896559, but native verification failed: `DRIBBLING/gear/WRISTS produced duplicate artwork`. The selected wrist coordinate `(340,553)` was outside the dribbling pose's skin mask, so the `DST_IN` matte erased the tape.

The latest published phone-test release at discovery was v56. Its published manifest identifies `com.courtquestpro`, versionCode 56, certificate SHA-256 `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`, and APK SHA-256 `788164fe1fd9e1e38557d16aedd5cb641e763e85017dd12fa7d0164c72858037`; the downloaded release APK matched its checksum.

- Keeps the v59 athlete source, registered skin/hair/shoe layers, live Jersey Studio uniform, saved card preferences, actual player record and front/back flip.
- Corrects all five arm accessory anchors against their packaged skin mattes; dribbling elbow/wrist anchors now follow the exposed forearm, and tape angle follows the transformed arm for each body build.
- Adds 14 face-anonymous, transparent 128×128 haircut thumbnails derived from the approved standing athlete, with hash/dimension checks. The `BLANK` face choice uses a softly lit, featureless face layer in each pose bundle.
- Rebuilds the controls as compact image-led horizontal selectors for all four editions, five poses, four face treatments, hair, physique, skin, shoes and accessories. The four fronts have separate hero placement, light cues and nameplate geometry; reverse panels distinguish partial saved records from empty history.
- `CardStudioRebuildTest` captures four fronts and backs, checks the four rendered designs differ, and captures all face treatments on each pose. Existing option tests still compare actual Android pixels for 280 avatar-choice/pose combinations.
- VersionCode is 60. The Android workflow checks package/certificate continuity against the actual released v56 APK and the v58/v59 candidates, then performs install-over while checking exact preference preservation and cold relaunch. It uploads an Actions APK candidate only; it does not create a public release.

## Recorded statistics

The app reads its existing save. Saved drills count valid completed result records, including repeat practice. Attempts and successful reps aggregate all supported measurements. Shooting accuracy uses made baskets over attempted shots, with frozen vocabulary first. Controlled-release drills, finishes, passing and other repetitions cannot enter that denominator. Completed missions count distinct sessions with verified completion receipts. Older one-drill records have no inferred mission receipt. Games and win/loss records use the final player and opponent scores. Ties appear explicitly if present. Rank uses the same XP function as Progress. The longest recorded streak is labeled “Best streak.” An em dash on the card means no record.

## Reproduction and coverage

`tools/player-card/build_anatomy.py` rebuilds the five compact registered bundles from the existing assets. Authoring requires Pillow, NumPy and OpenCV. The Android app uses only its existing native APIs. The stored bundles allow a normal offline Android build without those authoring dependencies. Hair color is an additive preference, leaving all earlier preference keys, their meanings and the training save intact.

The automated native gallery selects 14 hairstyles, six hair colors, five headwear options, ten complexions, four builds, four shoe models, seven shoe colors and six accessory configurations on all five poses. All 280 outputs are isolated athlete screenshots; 20 more compare each face treatment across all poses. Eight additional captures compare the front and reverse of all four editions. The v60 workflow verifies released v56 → v60 plus v58 → v60 and v59 → v60 installs, frozen selection and player-preference hashes, restart, discard, uniform changes, compact/large-text/landscape layouts and reduced-motion reading. `check_card_pixels.py` compares Android output with registered artwork masks. Full-size screenshots and numeric results are artifacts of each run; a failed run retains its diagnostics.
