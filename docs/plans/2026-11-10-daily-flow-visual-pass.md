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
- Complete: build 3 published to the existing development APK URL, downloaded and verified independently (package, version code 3, preserved signer, ZIP and 16KB alignment, matching published checksum).
- Pending: matched actual Android captures and runtime assertions. Build 3 release commit `da37913ca060604fdd599adefb2d10481703d7cd`. The application source tree is identical to candidate `3847cd883e12d69afef876a008273386cbb94d33` (`app/src` tree `c19a4983d639a12918a1f0e6ecbbf99aea2ac567`).

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

## Published build 3 evidence

- Release commit: `da37913ca060604fdd599adefb2d10481703d7cd`.
- Release workflow: [42945044187](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42945044187), successful.
- Development checks: [42945044267](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42945044267), successful.
- Restored UI checks: [42945044170](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42945044170), successful. The old literal-color guard now checks the shared native token and its orange value.
- Downloaded APK SHA-256: `1056c917720dd7b5a2265e4a0069db6d0a3f8c78befde19d94b143090981bbda`. Size: 16,914,940 bytes.
- Package: `com.courtquestpro`. Version: 1.0 (3). Minimum/target SDK: 26/35.
- Certificate SHA-256: `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`.
- The local and clean CI APK ZIP payloads match except the generated D8 class-checksum metadata in `classes3.dex`; their binary hashes differ. The release checksum above was validated against the actual downloaded GitHub asset. Source trees for app/src, root build, settings, properties and Gradle wrapper match the candidate.
- Actual Android runtime/visual acceptance is recorded separately below; the successful release job does not establish it.

## Actual Android review and build 4 refinements

Method: design review only (native platform; browser detector does not apply). Applied the directly read Impeccable native audit/critique guidance to matching Android Emulator captures from [run 42946204172](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42946204172), artifact `13823540747`. Taste's web guidance was considered only for hierarchy, consistent edges and actual product evidence; its website recipes are not a native implementation requirement.

Observed: build 2's training action was below the first viewport; build 3's Resume session was visible. Updating build 2 in place retained the existing profile/results. Setup's saved name and first-tap level selection were verified. The drill recorded 15 successes and one miss. The test needed to scroll inside the actual list bounds to reach its correction action; the first harness stopped before saving, so that run does not prove the full flow.

- P1 fixed in candidate 4: Progress's selected Training chip inherited a purple Material container. Explicit warm secondary/tertiary roles now keep selection and tonal controls in CourtQuest's palette.
- P2 fixed: the drill had a generic Training display title plus a second large drill title, pushing correction actions down. The drill name is now the single display title. Removed the duplicated plus symbol on the success action.
- P2 fixed: dated records followed another activity/streak section. They now lead the Training content, ahead of streak summaries. Full history uses individual LazyColumn items with a native Back path.
- Input/control outlines now use #8B7A71 (4.53:1 against the canvas, 4.19:1 against the utility surface and 3.16:1 against the warm surface). Secondary text on the warm surface is 5.84:1.
- Ruling: include an original native adaptive basketball launcher mark in this delivery. The baseline had no launcher icon; the new mark uses the existing brand colors and Android masks. This expands the original slice slightly and remains reversible.
- Owner visual acceptance, TalkBack traversal, physical-phone operation, landscape and tablet layouts remain separate checks. The next native run checks save-once, dated record visibility, relaunch, history Back, fresh onboarding, and compact/200% controls before publication of build 4.
