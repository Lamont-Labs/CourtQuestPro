# CourtQuest Pro v42 — WP01 Rendered Baseline Audit
Date: 2026-11-17
Status: SOURCE-VERIFIED BASELINE AUDIT; DEVICE CAPTURES NOT YET EXECUTED
Scope: SOLO PLAYER ONLY. No school, coach, roster, assignment, or Team screen acceptance.
Branch: dev/premium-flow-toolkit
Stable phone baseline: courtquest-phone-update-v41 (versionCode 41), source release SHA eb5dfc5d95a4aa8a33acec5304cb1abae12da9a8.
Proposed specification: docs/plans/2026-11-17-v42-solo-player-redesign.md
Unreleased walkthrough correction: fb63df89 (do not describe as installed v41).

## Evidence inspected
- .github/workflows/verify-premium-visuals.yml: manual workflow_dispatch, candidate compile/unit/lint, dedicated emulator, artifact upload.
- scripts/verify_premium_visuals.py: actual AVD screenshot/UI hierarchy capture, font-scale configs, dock/primary action checks.
- .agents/memory/native-visual-acceptance.md: compare rendered before/after, not merely APK metadata or pixel diff.
- Recent Actions runs: 44167181771 v41 release success; 44169400383 and 44169400341 post-release walkthrough commit success; 44172208294 on spec commit was in_progress at inspection, NOT a pass.

## Verified harness gaps
1. Baseline APK currently loaded from historical artifact 14123347359 labeled verified-native-v21.apk, NOT released v41. A v42 comparison against this is not a valid incremental v41->v42 acceptance test.
2. Existing harness explicitly requires Court 'Team' shortcut, visits Team via Court/Me, checks 'NO TEAM CONFIGURED', and captures team screen. This conflicts with current owner directive to exclude school/coach/team features completely.
3. Existing main SCREENS only includes court/library/progress/quests/me. Onboarding and a training screen are also captured, but drill detail, receipt, and Mission completion need dedicated captures and assertions.
4. Existing configurations are normal 720x1600 density320 font1.0, compact 640x1440 density320 font1.3, large 640x1440 density320 font2.0. Keep equivalent 360dp/320dp widths and test at least 1.0,1.3,2.0.
5. Existing UI checks are partly coupled to the prior cinematic design, e.g. six Court shortcut tiles and Today Focus below them. Refactor assertions to user jobs and accessible controls, not frozen layout composition.
6. The script clears data only on guarded AVD and seeds synthetic fixtures. Maintain guard; never clear user/physical-device data.
7. Visual approval requires manual critique of actual screenshots, not just compilation, UI hierarchy or pixel differences.

## WP01 execution plan
A. Pin exact v41 release APK asset and verify checksum, signer and application ID before baseline capture.
B. Introduce a separate v42 SOLO visual workflow/script or parameterized solo mode. Do not weaken or silently rewrite historical verification claims.
C. Remove Team from solo screenshots and UI requirements; retain valid existing checks for scorekeeping, Court, Train, Quests, Progress, Me.
D. Capture consistent v41 states: onboarding, Court, Train My Plan, Library, Custom, drill detail, active recording, receipt, Mission Builder, Mission completion, Quests, Progress, Me, scorekeeper, navigation walkthrough. Include no-history and populated-history fixtures.
E. Capture the same states in candidate build at matched size/density/font scale, using identical synthetic data and deterministic clock/date where feasible.
F. Record measurable: first actionable training control in viewport, number of controls before first drill, segment wrapping, touch target size, clipped labels, primary recording controls visible, scroll distance, onboarding first-action visibility, screenshot paths.
G. Preserve existing CI tests and install-upgrade checks; verify interruption recovery, no duplicate save, accessibility, no corrupted local records.
H. Review screenshots manually and produce prioritized issue register with observed vs inferred labels.
I. Do not release v42 until signed upgrade, appId, signer, versionCode and owner review all pass.

## Initial issue register (not yet reproduced by new emulator run)
- UX-001 Walkthrough card obstructs Train content on owner's v41 screenshot. Post-release fb63df89 hotfix verified in CI but not released.
- UX-002 Train My Plan/Library/Custom selector appears as stacked full-width rows in owner's v41 screenshot. Need font-scale and viewport reproduction.
- UX-003 Large Train hero and saved-plan content displace drill list in owner's screenshot and Compose layout.
- UX-004 Onboarding hero uses height scaled with font size; potential first-viewport obstruction, requires rendered proof.
- UX-005 Team surfaces still appear in legacy source/harness; scope conflict confirmed, no destructive cleanup yet.
- UX-006 Existing screenshot harness validates historical design layout instead of core player tasks; confirmed in script.
- UX-007 New v42 candidate has no screenshot evidence yet.

## Stop conditions
Do not claim a visual pass if v41 baseline unavailable, capture failed, canceled/skipped workflows, signer mismatch, synthetic data differs between captures, accessibility control clipped, or critical recording action offscreen. Preserve v41 phone installation.
