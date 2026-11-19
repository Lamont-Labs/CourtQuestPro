# CourtQuest Pro

Native Android basketball training and one-on-one scorekeeping, built with Kotlin and Jetpack Compose.

## Android phone testing and Play preparation

The latest published phone-test baseline is [CourtQuest Pro v54](https://github.com/Lamont-Labs/CourtQuestPro/releases/tag/courtquest-phone-update-v54). It installs on Android 8.0 or newer as `com.courtquestpro` using the established development certificate. The published release contains a checksum and the v53-to-v54 emulator upgrade receipt.

The API 36 candidate uses version code 55. Its Android verification and upload-key workflow is in [Android Play candidate](.github/workflows/android-play-candidate.yml). Read the [engineering audit](docs/release/ENGINEERING_AUDIT.md), [store listing package](docs/release/play-store/LISTING.md) and [Play Console worksheet](docs/release/play-store/PLAY_CONSOLE.md). CourtQuest Pro stores player records locally and currently ships the solo-player experience.

## Signature training journey

Court → start/resume → record attempts/corrections → save once → receipt/actual next drill → Progress now shares the clean Signature identity, deep black canvas, vivid orange actions and smoked-glass surfaces. The header and native navigation are reusable. Foreground counters and controls remain sharp; a saved-result acknowledgement respects disabled system animation.

Receipts show actual successful/attempted counts, awarded XP, the named next drill and explicit self-recorded/local-save provenance. Continue uses the same deterministic recovery route as its preview. Existing saves, dynamic names, daily XP, one-on-one scoring, Undo and history remain intact.

The **Drills** destination retains custom drills, search/skill filters, favorites, explained suggestions, protected swaps/shuffling and independent extra practice. Custom targets accept 1–500 successful actions; planned minutes accept 1–60 and remain the training clock. Editing creates a new definition; queued work and history preserve the original target/instructions. Archival removes future selection while retaining saved work. An unfinished practice must be resumed, finished or explicitly discarded before replacement.

## Historical verification — Signature build 7

| Check | Published build 7 |
| --- | --- |
| Android unit tests | 48 passed; no failures/errors/skips |
| Selector regression tests | 3 passed |
| Compilation and lint | Passed; 0 lint errors/11 warnings |
| Exact-binary native journeys | Library, daily training and game/Undo passed in [run 43136062719](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062719) |
| Guarded Maestro | 1/1 passed on the dedicated QA emulator; [toolkit run](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062726) also passed |
| Saved data/restoration | Build-5 in-place update, custom revisions, practice, same-day/expired Activity and process restart checked |
| Actual rendered review | Normal 360 dp, 320 dp/130%, critical 200%, keyboard/insets and game captures reviewed |
| Package/version/signature/signer/ZIP/16 KB alignment | Passed on the independently downloaded public APK |
| Exact public bytes | Byte for byte identical to the native-tested artifact |
| Release-branch checks | [Restored UI 43139207885](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43139207885) and [development 43139206837](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43139206837) passed |

Native checks use synthetic profiles. Physical-phone operation/performance, owner visual acceptance, full TalkBack, API 26–30 blur fallback and alternate launcher masks remain unverified. Enlarged controls/clock/receipt captures are scrolled viewports; they do not show every metric at once. [Rendered review](docs/tooling/SIGNATURE_RENDERED_REVIEW.md) records the actual scope.

Native source: `375a607fd7be33bf30ad56c2337cf9acf3ae2253`; app/src tree: `839a30e17949d0b7d9dbe9284999637e2117aa34`. Release commit: `a431a69254b9d896d2a76640f5af363530f0dfa6`; [publication/download verification](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43138905997) passed. [Native ledger](docs/tooling/evidence/signature-build7-native.json), [public-download ledger](docs/tooling/evidence/signature-build7-public-download.json), [implementation plan](docs/plans/2026-11-11-signature-training-journey.md).

APK SHA-256:

`1e0c5b24bc6f1087733014a1acaaba6b72e04435a758f9631b8f3e51b7b58767`

## Master specification

CQ-PREMIUM-001, the first native journey, is delivered. Next is the versioned Mission foundation/builder, followed by Player navigation, evidence-backed Quests and comparable Progress. Team/coach authorization and connected assignments are later phases. [Master specification](docs/specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md), [implementation tracker](docs/MASTER_SPEC_IMPLEMENTATION.md), [next issue/migration contract](docs/plans/2026-11-11-player-mode-sequence.md).

## Build and preservation

JDK 17, SDK 36/build-tools 35.0.1, Gradle 8.11.1, AGP 8.10.1, Kotlin 2.0.21, Compose BOM 2024.12.01, minimum SDK 26. Haze 1.1.1 and Lottie Compose 6.7.1 remain pinned.

```bash
./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
python3 scripts/verify_apk.py \
  --apk app/build/outputs/apk/debug/app-debug.apk \
  --build-tools "$ANDROID_HOME/build-tools/35.0.1" \
  --output artifacts/apk-verification
```

Active release engineering continues on `dev/play-production-candidate` from the v54 release branch. The phone-test APK and the Play App Bundle use separate signing configurations.

Profiles, plans, XP, custom definitions/favorites, practice, drafts/results, matches and corrections are stored locally. No connected sync or independently verified skill is implied. Drill demonstrations open external YouTube searches. Barlow fonts retain their SIL Open Font License in `app/src/main/res/raw/barlow_ofl.txt`. Clean Signature contours/renders and their source hashes are in `tools/branding/`.

The preserved development certificate SHA-256 is:

`25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`

Do not regenerate the existing development identity or change the package during an update. Production signing is a separate release task. [DESIGN.md](DESIGN.md) and [AGENTS.md](AGENTS.md) guide future work.
