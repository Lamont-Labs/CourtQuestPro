# CourtQuest Pro

Native Android basketball training and one-on-one scorekeeping, built with Kotlin and Jetpack Compose.

## Install the current development build

[Download CourtQuestPro-Android-debug.apk](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-dev-2026-11-05/CourtQuestPro-Android-debug.apk?build=5)

Open the APK on an Android 8.0 or newer phone and choose Install/Update. The development release is updated in place. The package and signing identity are preserved so Android can retain the existing player profile and local history.

## One-on-one visual build 5

Version 1.0, version code **5**. The daily-use redesign includes a training-first Court, bundled Barlow typography, focused attempt recording, a saved-result receipt, dated training history and same-drill comparisons in Progress, and a current-profile Setup screen. The one-on-one setup, adaptive scoreboard, saved game receipt and dated game history now share that system. Unfinished same-day games resume with their saved score and Undo order. Repeat games show actual earned XP, and expired games return to setup. Player names and all results come from saved state.

The active design standard is [DESIGN.md](DESIGN.md); [AGENTS.md](AGENTS.md) tells future development sessions to use it. The implementation and verification ledger is [the one-on-one pass](docs/plans/2026-11-10-one-on-one-visual-pass.md).

## Verification

| Check | Published build 5 result |
| --- | --- |
| Android unit tests | 31 passed |
| APK build | Passed |
| Android lint | 0 errors; 10 warnings retained in the verification artifact |
| Package, version, v2 signature, signer continuity, ZIP and 16KB alignment | Passed on the downloaded release; its SHA-256 matches the Android-tested APK |
| Android journey and rendered screens | Passed in [native run 43043445045](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43043445045) |
| Physical phone and owner visual acceptance | Pending for this visual candidate |

The native walkthrough uses synthetic test profiles. It compares the preserved build-4 UI with the candidate, installs the candidate in place, records/corrects attempts, saves once, verifies XP and history, checks relaunch and preference feedback, and captures normal/compact/enlarged-text screens. It also exercises blank/long names, empty history, completed workouts, scorekeeping/Undo, Back, and fresh onboarding. Game checks cover saved-score resumption after restart, 10–10 sudden death, correct winner-side handling for identical names, actual XP on repeated games, daily rollover and the full game-history list.

## Build

JDK 17, Android SDK 35, build-tools 35.0.1, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01. Application ID: `com.courtquestpro`; minimum SDK 26.

```bash
export COURTQUEST_VERSION_CODE=5
./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
python3 scripts/verify_apk.py \
  --apk app/build/outputs/apk/debug/app-debug.apk \
  --build-tools "$ANDROID_HOME/build-tools/35.0.1" \
  --output artifacts/apk-verification
```

The authorized development release workflow runs from `dev/scorekeeper-undo-history` when its release-request file changes. Application work is checkpointed in `dev/premium-game-flow`; `main` remains separate.

## Saved data and assets

Player profile, plans, XP, drill drafts/results, matches and scoring corrections are stored locally. Drill demonstrations open external YouTube searches. Bundled Barlow fonts include their SIL Open Font License in `app/src/main/res/raw/barlow_ofl.txt`.

The preserved development signing certificate has SHA-256:

`25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`

`signing/debug.keystore` is the existing development identity. Production signing is a separate release task. Do not regenerate the development key or change the package during an APK update.

The published APK was independently downloaded and verified in [release run 43045952485](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43045952485). Actual Android screenshots are saved as PNG assets on the same release. The emulator profiles in these previews are synthetic.

SHA-256: `d2e6eb32d3d90d93a3be3cbbc08684e5090202746294e2ea0bf3583b5817339b`.
