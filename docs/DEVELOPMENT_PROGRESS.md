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

## CQ-010 — Legacy scoring compatibility and guided drill clock

Status: SOURCE COMMITTED; CI and physical device verification pending.

- Run #22 (`42102160658`) failed with 9 tests, 1 failure: `ScorekeeperPersistenceTest.olderSavedJsonWithoutHistoryDoesNotGuessScorer`.
- Root cause: `SaveState.pointHistory` defaulted to an empty list, allowing Gson to initialize a missing field as an empty list. Changed the default to null so legacy saves remain distinguishable. New match and daily reset explicitly initialize an empty history.
- Fix commit: `d91527363a8c0e6db115d8b0f61c334b8072ff41`.
- Added a per-drill six-minute (drill-configured) countdown with Start/Pause/Reset, visual progress, and an explicit reminder that time elapsed does not automatically count a drill as completed.
- Training timer commit: `8d75e56fd026b77281b730e5878ef192d8ae5da5`.
- The timer is ephemeral UI state; score and completion persistence are unchanged. Do not claim background or process-death timer continuity.
- Next: verify CI green; introduce actual recorded drill outcomes (attempts/makes or reps) rather than XP-only completion.

## CQ-011 — Measurable drill results

Status: SOURCE COMMITTED; new CI and physical device verification pending.

- Confirmed CQ-010 workflow run #28 (`42102728158`) passed Android unit tests, APK compilation, and lint.
- Added persisted `DrillResult` records with date, slot ID, drill ID, successful attempts and total attempts. Nullable `SaveState.drillResults` preserves Gson compatibility with older saves.
- Drill screen now records attempts and successful reps, blocks successful count exceeding attempts, requires at least one attempt before completion, and shows the previous highest successful count for that drill.
- Results are saved atomically with the existing completion/XP update. Incomplete in-progress counters are not yet persisted; no claims of advanced analytics or calibrated player skill ratings.
- Implementation commit: `b2494c93a96e0124ea6d7233ad81616c25e84c10`.
- Three new JUnit tests for persistence, legacy compatibility, and personal-best selection: `bafdd0c885e4cb6a7fe4efde35c21f074a1a23b8`.
- Next: confirm CI; add drill-specific measurement schemas and aggregate player development views.

## CQ-012 — Player development dashboard

Status: SOURCE COMMITTED; CQ-012 CI and physical device verification pending.

- CQ-011 Android workflow run #34 (`42103282591`) succeeded.
- Implementation commit `ef016389a704193e8fbc118e461a3b5ef96519d5` adds recorded drill/session and attempt counts to the Progress/Career screen, category breakdowns, success percentages, and highest successful-count record.
- Displays explicit caution: cross-drill success rates are descriptive, not calibrated skill ratings or demonstrated improvement.
- Handles empty and legacy results safely via `drillResults.orEmpty()`.
- Updated setup option backgrounds for dark theme contrast.
- Next: confirm Android CI; compare identical drills over time, separate shooting accuracy from repetition counts, and consider resumable training sessions.

## CQ-013 — Same-drill performance trends

Status: SOURCE COMMITTED; new CI/device verification pending.

- CQ-012 GitHub Actions run #38 (`42103779012`) succeeded.
- Added pure deterministic `DrillTrends.calculate`: compares earliest and latest valid recorded results by date for identical drill IDs, excludes zero-attempt/invalid records, requires at least two sessions.
- Added same-drill trend cards to Progress: first/latest success rate, direction, percentage-point difference, session count and insufficient-data state.
- Three unit tests cover insufficient samples, out-of-order records and invalid data.
- Commits: `f417a740a3c69e43ddaf4f3de3b2ae98e0efaceb`, `66eddec50d41a4a70da93f87d1af8a8884f4c5e5`, `932cbb53d37734c6c39522243150a280b8264be9`.
- Limitation: compares earliest and latest, not statistical significance or a calibrated player rating. Distinct drill IDs may still have varying conditions.
- Next: confirm CI, then refine drill-specific measurement definitions and training flow.

## CQ-014 — Basketball-specific measurement vocabulary

Status: SOURCE COMMITTED; CI and device verification pending.

- CQ-013 GitHub Actions run #46 (`42105820959`) passed.
- Added `DrillMeasurement.kt` with distinct result labels and coaching instructions for shooting, finishing, passing, ball handling, defense and rebounding.
- Updated drill recorder to display category-appropriate success/attempt labels and buttons; no existing saved result schema changed.
- Added two unit tests for category coverage and fallback behavior.
- Commits: `3dd76cdd967ff17540bdc37ba6f6e5073b1117c6`, `071f77250454137f7bfc905ad95e4aff1e5ecc1d`, `5932c4ebeae9cd7ef1c82e25facbdafb4329a644`.
- Scope limit: this is category-specific vocabulary, not yet per-drill measurement units, timed interval metrics, or detailed coaching analytics.
- Next: verify CI, then implement drill-specific targets and outcome validation.

## CQ-015 — Drill completion target gates

Status: SOURCE COMMITTED; CI and device verification pending.

- CQ-014 Android workflow #54 (`42106730888`) passed.
- Added explicit minimum successful-action counts for all 18 existing drills (`DrillTargets.kt`).
- Drill completion button now requires meeting the minimum recorded successful actions AND checking an acknowledgment that the full written target was completed (including sides, timed rounds, rest and technique).
- No automatic inference that counts prove technique or side balance. Interval drills count completed rounds; the user confirms durations.
- Added 3 unit tests for target coverage, thresholds and invalid data.
- Commits: `d04aefd3cba78b33d2fc071ba5baa990b1617ba1`, `1941565997659e589e66b42e4edbfc585b05d163`, `cdb6fdc8a928f47115af8772b66e126e22cf0ffe`.
- Next: verify CI; consider persisting in-progress sessions and richer per-side/interval metrics.

## CQ-016 — Resumable training drafts

Status: SOURCE COMMITTED; new CI and physical device verification pending.

- CQ-015 GitHub Actions run #62 (`42107549654`) passed.
- Added nullable `SaveState.drillDrafts` and `DrillDraft` containing date, slot ID, drill ID, successes, attempts, remaining seconds and target confirmation.
- Counts and confirmation persist immediately when changed; timer remaining seconds persist as it ticks and on reset/restart. Draft is restored when reopening the same date/slot/drill.
- Completing a drill removes its draft in the same saved-state update as the result and XP award. Drafts do not award XP.
- Added two Gson tests for roundtrip and legacy-save compatibility.
- Commits: `75d10432c6b1859f702256dc142e8b9e8ba19818`, `4c60f044efdf2d328a6c3bc34d92cd5646fe0c01`, `b6399d0c2d4039becb49dbaf60a62e2cbe9a6612`.
- Timer resumes paused rather than continuing while app is closed. Pending: CI/device acceptance, draft cleanup policy, state validation.

## CQ-017 — Training recovery hardening

Status: SOURCE COMMITTED; new CI and device verification pending.

- CQ-016 Android workflow #70 (`42108323195`) passed.
- Added `DrillDraftRecovery` validation for date, slot ID, drill ID, incomplete status, nonnegative counters, successful <= attempted and timer within drill duration.
- Daily rollover and newly generated slots discard old drafts; current-day startup prunes invalid, stale and duplicate records.
- In-progress writes are accepted only for the active, matching incomplete slot; recovery ignores invalid drafts.
- Added three JUnit tests for recovery, pruning and duplicate selection.
- Commits: `38df2f89307908b9aa6d412c2613b41435fcc987`, `fc962c593904a116676bc25751e225460af32e44`, `de2e2429ae6d3b7f76f9e2cdffd710df6dbf345a`.
- Next: confirm CI and evaluate a direct APK release pipeline; physical device testing remains outstanding.

## CQ-018 APK release checkpoint

Previous Android CI run 78 passed. Release workflow now checks the APK with Android aapt and apksigner rather than calling a missing Python script. The authorized release-request marker was updated to trigger publication. Release asset availability is not yet verified. Implementation commits: a65d062 and 3ece651. Next: inspect publishing workflow result and verify direct APK asset.

## CQ-019 Published Android APK verification

Verified GitHub prerelease tag: courtquest-dev-2026-11-05. Target commit: 3ece6517196472cbb516d13404e52299c13e8a89. Published 2026-11-05T21:13:53Z. GitHub confirms uploaded APK asset CourtQuestPro-Android-debug.apk (16,621,617 bytes), SHA-256 b735413e0ae7c48a52d700accbb232aaea38f8771c5a0d559bc2b68abc422191. A separate .sha256 asset is uploaded. Direct URL: https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-dev-2026-11-05/CourtQuestPro-Android-debug.apk . Verified release metadata and asset listing, not physical installation. The release points to the authorized trigger commit, not later documentation commits. Next: device acceptance and release hardening. Main branch unchanged by this task.

## CQ-020 Premium visual overhaul, stage 1

Approved broader product upgrade includes app shell, training 2.0, player development, plans and challenges. First implementation committed: 8c78a579c6a6723002aff5ed62036536e30ab257. Added a branded basketball header with warm dark gradient, CourtQuest mark, Player Lab identity, bold typography and numbered dashboard sections for workout, game day and skill development. Updated workout surface and key progress typography. Existing save schema, navigation and training logic preserved. Automated CI for this change and physical device review remain pending. Next: verify CI, then expand reusable visual components and overhaul training interactions in small tested changes. Do not mark all roadmap stages complete.
