# CourtQuest Pro: daily flow visual pass

Base: `d07dcd51e86f3a537711bce2064b06922155bd2f`. Candidate branch: `dev/premium-daily-flow`. Owner authorized implementation and replacement of the existing development APK. Target version code: 3.

## Scope and constraints

Rework Court → start/resume drill → save → receipt → Progress, plus Setup and the shared theme. Preserve Kotlin/Compose, `com.courtquestpro`, the existing signing certificate, saved schema, scoring/award rules, dynamic player identity, and offline use. Follow root DESIGN.md and the directly searched native UI/UX Pro Max guidance. Preserve the owner's warm orange and dark palette.

## Implementation

- Complete: shared dark Material 3 theme, bundled Barlow/Barlow Condensed with OFL license, restrained native surfaces and display hierarchy.
- Complete: daily session leads Court; supporting XP, actual drill lineup, weekly editor, and one-on-one follow.
- Complete: focused drill counters and stable Save results footer; existing draft, timer, target, and correction rules retained.
- Complete: saved result receipt; actual dated records and same-drill comparisons in Progress; current name and level in Setup.
- Complete: source review of the entire changed branch. No Critical findings. Important finding addressed: exact saved sessions must be visible in Progress; added dated session records with counts and full-history disclosure.
- Complete: local Android unit tests 25/25, repository tests 53/53, build, lint (0 errors, 6 warnings), package/version, v2 signature and signing continuity, ZIP and 16KB alignment checks.
- Pending: matched actual Android captures and runtime assertions; final GitHub release update and downloaded-asset verification.

## Decisions and review record

- Ruling: apply Pro Max's Fitness/Gym App match to the native basketball flow and retain CourtQuest colors. The broad basketball search returned fallback guidance; the refined match is recorded in DESIGN.md. Browser component libraries remain references for compatible ideas; implementation uses Compose.
- Ruling: use licensed bundled fonts instead of network fonts so the actual APK has consistent typography offline. Phone-sized and enlarged-text review remains a release verification task.
- Ruling: refresh must publish a new observable save after mutable updates. Switched its equality policy to reference equality so same-screen preference and score changes render immediately. This preserves the persisted schema; the runtime walkthrough checks first-tap selection and score/Undo feedback.
- Ruling: the shared typography requires score actions to grow with enlarged text. Replaced fixed action heights with minimum heights and added explicit player score-action labels.
- Final review: source-confirmed save-once guard, draft pruning, award keys, and saved schema remain intact. One Important finding (dated result visibility) fixed in the native Progress screen. Runtime proof is separate from this source review.
- Runtime limitation encountered: local software emulator hit an Android system startup watchdog before app launch. This is an environment startup issue; added a hardware-accelerated GitHub emulator workflow for reproducible verification. No application-runtime result is inferred from a successful build.
- Existing lint warnings: court vector size, two available dependency updates, Android data extraction configuration, packaged font license marked unused, missing launcher icon. Dependency upgrades and launcher artwork are outside this slice.

## Verification commands

`./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`

`python3 -m unittest discover -s tests -q`

`COURTQUEST_VERSION_CODE=3 python3 scripts/verify_apk.py --apk app/build/outputs/apk/debug/app-debug.apk --build-tools "$ANDROID_HOME/build-tools/35.0.1" --output artifacts/apk-verification`

GitHub workflow `verify-native-daily-flow.yml` runs the preserved build-2 baseline, updates in place to the candidate, captures Court/Drill/Progress/Setup, records and corrects attempts, saves once, verifies XP and preferences, relaunches, and captures compact/enlarged text. Its test profile is synthetic. Owner visual acceptance and physical-phone testing remain separate.
