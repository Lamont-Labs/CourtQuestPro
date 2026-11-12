# Compact player dock — implementation plan

> Execute inline with the already-authorized native development/update workflow.

**Goal:** Replace the owner-rejected two-row build-9 navigation with one low-profile row at normal and enlarged text.
**Architecture:** Change only CourtQuestNavigation.kt plus Android versionCode. Keep the five routes, callbacks, saved-state model and Signature material. A 56dp dock contains 48dp targets, four inactive icons and a selected name pill; text measurement decides whether the selected pill also has room for its icon. Full tab names and selected Tab semantics remain available, with native long-press tooltips. Scalable text is retained; the bar never switches to two rows.
**Stack:** Kotlin 2.0.21, Compose BOM 2024.12.01, existing Material3 and shared smoked-glass surface; no dependency changes.
**Authority:** Owner's November 12 phone screenshot and instruction to keep all five destinations low profile, superseding build-9's large-text fallback.

## Constraints and review focus

- Preserve package com.courtquestpro, certificate 25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b, Signature resources and all saves.
- Keep Court/Train/Quests/Progress/Me order, role/selected semantics, native ripple and Back behavior.
- All controls fit one row at 360dp/100%, 360dp/150% (owner case), 320dp/130%, 320dp/200%; inactive targets remain 48dp wide. Worst-case Progress label must fit without visible clipping.
- Preserve Android system navigation insets and Scaffold content clearance. OS gesture/three-button area is outside the dock height.
- Apply relevant UI/UX Pro Max touch/navigation/semantics guidance to the pinned native stack; official Android tooltip/semantics API documentation checked. Native captures are acceptance evidence.

## Work

- [ ] Reproduce old two-row geometry from the frozen published build 9 on the guarded emulator and save baseline screenshot.
- [ ] Implement compact single-row dock and version 10; preserve all other runtime sources.
- [ ] Run existing 72 unit tests, seven Python checks, APK compilation and lint; test every tab's bounds/selection/destination at all four text/width cases, actual long-press label, Back, upgrade-in-place and saved-data preservation. Review before/after captures; independent source review.
- [ ] Publish only the exact accepted native APK through the existing fixed development download; verify public bytes/version/package/original signer and save handoff. Current Quest award rules remain next scope.
