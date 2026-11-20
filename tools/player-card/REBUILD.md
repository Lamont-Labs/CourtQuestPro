# Card Studio v59

The input is development commit `0b3ff087d0153a06d7bec694f266b25f453e0fea`, versionCode 58. The last release tag at discovery was v56. Action 44797671170 supplies the signed v58 APK and 44631807988 supplies v57. All three use the established phone-update signer with SHA-256 `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`.

| v58 observation | v59 implementation |
|---|---|
| Replit commit 899f1db supplied athlete art. Four material plates and five poses reached Compose. | The same athletes, textile maps, folds and material plates drive the profile and editor. |
| Replit commit 387a3a6 supplied four texture masters outside the Android assets. v58 used separate authored pose/head crops. | Brushed crop, box-braid bun, twist knot and sculpted waves register the four masters to the posed crowns. |
| Head plates contained both hair and warm skin and painted over the body complexion. A footwear change could select a second base with the old head. | One canonical athlete supports every shoe and hairstyle. Skin, hair, footwear and uniform are independently registered and painted. |
| The v58 skin filter preserved input luminance for every swatch. Original shoe cutouts omitted bright rubber and portions of the toe. | The selected complexion changes exposure while preserving local relief. Shoe registration follows both ankle seams and the full visible foot, including the sole. |
| Source and device tests covered selections but had no statistical reverse, saved side or flip. | A single two-sided native card is used from Me and the editor. A separate readable record supports large text and reduced motion. |

## Recorded statistics

The app reads its existing save. Saved drills count valid completed result records, including repeat practice. Attempts and successful reps aggregate all supported measurements. Shooting accuracy uses made baskets over attempted shots, with frozen vocabulary first. Controlled-release drills, finishes, passing and other repetitions cannot enter that denominator. Completed missions count distinct sessions with verified completion receipts. Older one-drill records have no inferred mission receipt. Games and win/loss records use the final player and opponent scores. Ties appear explicitly if present. Rank uses the same XP function as Progress. The longest recorded streak is labeled “Best streak.” An em dash on the card means no record.

## Reproduction and coverage

`tools/player-card/build_anatomy.py` rebuilds the five compact registered bundles from the existing assets. Authoring requires Pillow, NumPy and OpenCV. The Android app uses only its existing native APIs. The stored bundles allow a normal offline Android build without those authoring dependencies. Hair color is an additive preference, leaving all earlier preference keys, their meanings and the training save intact.

The automated native gallery selects 14 hairstyles, six hair colors, five headwear options, ten complexions, four builds, four shoe models, seven shoe colors and six accessory configurations on all five poses. All 280 outputs are isolated athlete screenshots. Eight additional captures compare the front and reverse of all four editions. The job records real v57 → v59 and v58 → v59 installations, frozen selection and player preference hashes, restart, discard, uniform changes, four display configurations and frame samples during the turn. `check_card_pixels.py` compares Android output with registered artwork masks. Full-size screenshots and numeric results are artifacts of each run; a failed run retains its diagnostics.
