# Development progress — 2026-11-05

## CQ-001 — Correct scorekeeper Undo

Status: IMPLEMENTED; Android compilation and device verification pending.

Branch: `dev/scorekeeper-undo-history`
Implementation commit: `2fa10f66304ba40efd4f42ea2f5c6bcdee4c694a`

### Source evidence
- `app/src/main/java/com/courtquestpro/MainActivity.kt`: prior Undo decremented the leading score rather than the actual most recent scorer.
- `README.md`: independently documents the defect.
- `.github/workflows/android-apk.yml`: Android build and lint only trigger automatically for pushes to `main`, not this development branch.

### Changes
- Persist the ordered scoring history in `SaveState.pointHistory` (nullable for Gson compatibility with old saves).
- Record the scorer for every accepted point.
- Undo only the last recorded scoring event.
- Reset history when a match starts or the daily session resets.
- For legacy in-progress matches without scoring history, disable unsafe score guessing; existing saved scores remain intact.

### Verification
- GitHub update_file accepted the source change and returned a commit SHA.
- Android compile/lint, device behavior and regression tests have NOT been verified.
- No public release, production deployment, or main-branch merge was performed.

### Next task
CQ-002: Add isolated scorekeeper logic with automated unit tests covering alternating scorers, tied scores, repeated undo, old saved state, and win boundaries; enable safe branch build verification without publishing a release.

## CQ-002 — Automated scorekeeper verification

Status: SOURCE IMPLEMENTED; CI execution not yet confirmed.

Branch: `dev/scorekeeper-undo-history`

### Commits
- `29a8e246b9171250727eaa625b14c0e49e084d04` — pure scorekeeper transitions
- `513ee64e2a4350e7077a6f26ce404be601761ec6` — six JUnit regression cases
- `af716fd79b712662f210457d8bc5268af7acee01` — wire UI to transitions
- `f41ccbecaedda6dad570741dbe8cd05e00ca182f` — JUnit dependency
- `e803dcb29454ee95c0561b7235fdbeff61bc00f9` — development CI workflow

### Files
- `app/src/main/java/com/courtquestpro/Scorekeeper.kt`
- `app/src/test/java/com/courtquestpro/ScorekeeperTest.kt`
- `app/src/main/java/com/courtquestpro/MainActivity.kt`
- `app/build.gradle.kts`
- `.github/workflows/android-development.yml`

### Verification limitations
Tests are authored, not yet demonstrated passing. Branch workflows newly added outside the default branch may not trigger until GitHub recognizes the workflow. No emulator/device run. No production merge or release.

### Next task
CQ-003: Verify actual GitHub Actions execution and inspect failing tests/build/lint, then fix issues; consider a test-only PR check if the branch workflow cannot start.

## CQ-003 — CI execution enablement

Status: DRAFT PR CREATED; checks not yet reported.

- CI workflow amended to include `pull_request` events targeting `main`.
- Workflow-change commit: `379662d6a27e708b79892f6d56f9302d17ae5681`.
- Draft PR #1: https://github.com/Lamont-Labs/CourtQuestPro/pull/1
- No merge or release authorized/performed.
- At initial inspection, PR-associated workflow runs and commit status lists were empty. Do not infer success.
- Follow-up: inspect PR checks after GitHub processes the event; if none appear, investigate repository Actions settings/workflow registration. Verify Gradle tests, lint, APK, and behavior before merging.

## CQ-004 — Live CI execution diagnosis

Status: IN PROGRESS, not yet verified as passing.

- GitHub Actions run #6 (ID `42094156820`) started on draft PR #1.
- Job `verify` ID `127479706204`: checkout, JDK 17 setup, and Android SDK install succeeded; Gradle unit tests/build/lint running at inspection.
- Six standalone Kotlin assertions passed locally against the pure scorekeeper logic (Kotlin 1.9/JRE 21); these are independent smoke checks, not a substitute for the project's JUnit/Android CI.
- Final CI outcome, APK artifact and Android device behavior remain pending.
- Next: inspect the completed job, fix any failing Gradle test/lint/build, and preserve evidence before merging.

## CQ-006 — Android CI evidence confirmed

Status: CI PASSED; physical-device gameplay testing pending.

- GitHub Actions run #6 (`42094156820`): verify job success; unit tests, debug APK build, lint, artifact upload successful.
- GitHub Actions run #8 (`42094492183`): verify job success; same checks successful.
- Run #8 artifact: `courtquest-dev-verification` (ID `13447127081`, 16,125,930 bytes), SHA-256 digest `cb30395c7f18e96ddf52a442823d732200a74db3563cd99c079b26b1eed5eec9`.
- Artifact retained until 2026-11-19 19:36 UTC.
- Draft PR #1 remains unmerged; main untouched. No release published.
- Next task: verify device installation and interactive scorekeeper behavior; review PR before any merge.

## CQ-007 — Persistence regression and device acceptance preparation

Status: SOURCE COMMITTED; physical-device execution pending.

- Added `app/src/test/java/com/courtquestpro/ScorekeeperPersistenceTest.kt`: three Gson persistence/legacy compatibility regression tests.
- Added `docs/ANDROID_DEVICE_ACCEPTANCE.md`: stepwise Android installation, scorekeeping, championship, persistence and acceptance criteria.
- Commits: `cff0cafb5d9f533864b2c354610725a28ec7987b`, `207d563edae9521b9b132b2928b2c455ce61d40a`.
- Cannot claim physical-device testing: no device control was available.
- The new JUnit tests need GitHub CI execution on the new head commit.
- Next task: inspect new CI result, address failures, and verify resume-match UX or document limitation.

## CQ-009 — Vincent-first visual foundation

Status: SOURCE COMMITTED; Android CI and device acceptance pending.

- Commit: `3928cfc3418a05826178e56898677d76680b9e92`.
- Changed `MainActivity.kt` design tokens to a charcoal / warm-orange court palette and dark Material surfaces, revised dashboard and navigation copy for Vincent-first training.
- Retained existing player state, daily drill completion, scorekeeper, history, and persistence.
- Personalization fallback uses Vincent only when no player name has been saved; stored player name takes precedence, allowing future profiles.
- This is a first-pass visual foundation, not a full design overhaul. Remaining: guided training sessions, actual performance metrics, reusable component extraction, responsive UI polish, accessibility, and device review.
- No device run or successful CI check claimed for this commit.
- Next: verify Android CI and address theme/contrast and build issues before expanding training UX.
