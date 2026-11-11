# CourtQuest Pro

Native Android basketball training and one-on-one scorekeeping, built with Kotlin and Jetpack Compose.

## Install the current development build

[Download CourtQuestPro-Android-debug.apk — build 6](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-dev-2026-11-05/CourtQuestPro-Android-debug.apk?build=6)

Open the APK on an Android 8.0 or newer phone and choose Install/Update over the existing app. Version 1.0, version code **6**. The package and original signing identity are preserved. The Android build-5 update test retained its player profile and local history.

## Drill Library build 6

The new **Drills** destination includes:

- Custom drills with a name, skill, difficulty, planned minutes, successful-action target and instructions.
- Search, skill filters, My drills and Favorites.
- Suggestions based on the selected level, weekly focus, favorites and recent training, with a short explanation.
- Daily-workout swaps and **Shuffle remaining**, preserving completed drills, saved drafts, slot identities and existing award rules.
- Independent extra practice with saved attempts, timer resumption, a recorded-result receipt and labeled Progress history.

Open **Drills → Create custom drill** to add one. Targets accept 1–500 successful actions; planned minutes accept 1–60 and remain the training clock. Completion requires the saved action target and confirmation. Use a suitable drill in an untouched daily slot, or start extra practice. An unfinished practice must be resumed, completed or explicitly discarded before another replaces it.

Editing creates a new definition while queued workouts and earlier results retain their original instructions and targets. Favorites follow the edited version. Archiving removes a drill from future selection while preserving saved history and queued work. Extra practice records activity in Progress; daily XP and streak rules retain their existing behavior.

The approved dark/orange Barlow visual system continues across Court, recording, receipts, Progress, Setup and one-on-one play. Names and results come from saved state. Same-day games resume with scores and Undo order. Restoring an expired score Activity returns to Court, and score writes reject expired matches.

The active design standard is [DESIGN.md](DESIGN.md); [AGENTS.md](AGENTS.md) directs future sessions to it. Implementation, review, native regressions and the owner walkthrough are recorded in [the Drill Library ledger](docs/plans/2026-11-11-drill-library.md).

## Verification

| Check | Published build 6 result |
| --- | --- |
| Android unit tests | 42 passed; no failures, errors or skips |
| APK build | Passed |
| Android lint | 0 errors; 10 warnings retained in the verification artifact |
| Exact-binary Android journeys | Library, existing daily flow and full one-on-one flow passed in [native run 43060242201](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43060242201) |
| Existing saved data | Preserved by the in-place build-5 → build-6 update |
| Activity/process restoration | Same-day 2–1 game resumed; expired saved score route returned to Court without a match/XP write |
| Rendered screens | Normal, 320 dp/130%, critical 200% and keyboard/footer captures reviewed |
| Package, version, v2 signature, original signer, ZIP and 16KB alignment | Passed on the downloaded release |
| Public download | Independently downloaded; byte for byte identical to the Android-tested APK |
| Physical phone, owner visual acceptance for build 6 and full TalkBack traversal | Pending |

Native checks use synthetic profiles. Library coverage includes validation, favorites, swaps/shuffling, locked drafts/completions, editor recreation, revision preservation, practice relaunch and recording once, real custom goals, original target in the queued daily drill, archival and history. Existing daily checks cover onboarding, names, attempts, XP, preferences, history, Back and relaunch. Game checks cover scoring/Undo, resumption, sudden death, repeated-game XP, identical names, rollover, history, compact/large-text/landscape/tablet controls and reduced motion.

## Build

JDK 17, Android SDK 35, build-tools 35.0.1, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01. Application ID: `com.courtquestpro`; minimum SDK 26.

```bash
./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
python3 scripts/verify_apk.py \
  --apk app/build/outputs/apk/debug/app-debug.apk \
  --build-tools "$ANDROID_HOME/build-tools/35.0.1" \
  --output artifacts/apk-verification
```

Application work is checkpointed in `dev/drill-library`. The authorized release branch remains `dev/scorekeeper-undo-history`; `main` remains independent. Publishing uses the exact successful native artifact, without rebuilding it, and checks that its app source tree matches the release source.

## Saved data and assets

Player profile, plans, XP, custom definitions, favorites, practice sessions, drill drafts/results, matches and scoring corrections are stored locally. Existing saved JSON remains readable. Drill demonstrations open external YouTube searches. Bundled Barlow fonts retain their SIL Open Font License in `app/src/main/res/raw/barlow_ofl.txt`.

The preserved development signing certificate has SHA-256:

`25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`

`signing/debug.keystore` is the existing development identity. Production signing is a separate release task. Do not regenerate the development key or change the package during an APK update.

The tested APK was published and downloaded again in [release run 43063480763](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43063480763). Actual build-6 Library, Editor, Custom Drill, Practice Result and enlarged-text PNGs are assets on the same release. Native source: `0ff5e6c5d78ba8b958deef01478475fe5c78f4a0`; release commit: `e28a28680e397474584c80cd68bb991221cbda8d`.

Published APK: 17,047,484 bytes. SHA-256:

`00bcd7d1296ac2a71efadad8beb42834e3f9897ff7b5af668c020c731507a3d4`
