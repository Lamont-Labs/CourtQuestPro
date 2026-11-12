# Player navigation and Me implementation plan

> Execute inline with superpowers:executing-plans. Existing authority: owner-approved master specification and CQ-PLAYER-003 in the Player Mode sequence.

**Goal:** Provide Court, Train, Quests, Progress and Me as useful native destinations, with recoverable navigation and existing training preserved.
**Architecture:** Keep the pinned Compose stack and existing screen routes; add a small pure navigation reducer with a saved route history. Use a SaveableStateHolder for destination-local filters, editing and scroll state. Train owns the existing library/builder; Quests exposes actual daily participation and saved Missions without issuing new achievements; Me owns profile/settings.
**Tech Stack:** Kotlin 2.0.21, Compose BOM 2024.12.01, Material 3, Gson, minSdk 26.
**Spec:** docs/specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md sections 4, 5 and 16; docs/plans/2026-11-11-player-mode-sequence.md CQ-PLAYER-003.

## Global constraints

- Preserve com.courtquestpro, original signing identity, Signature vectors, existing save schema and history.
- Use true black, vivid orange, shared smoked glass and Barlow type.
- No coach authority, school backend, invented history, new XP/award rules or purchase prompts.
- CQ-PLAYER-004 owns versioned achievements; this issue makes Quests useful through current training evidence and routes.

## Review focus

1. Child Back must return to its opener; receipt Back must not reopen a completed recorder.
2. Tab switching and Activity recreation must preserve filters/name draft; training data remains durable independently.
3. Daily rollover must not display yesterday's completion as today's quest or restore expired scoring.
4. 320dp/130% and 200% labels, Me editing and keyboard must stay usable.
5. Existing build-5 upgrade, Mission/practice recovery, scoring/Undo and exact-once receipts must remain green.

## Task 1: navigation contract

- [x] Add behavioral tests for root Back, builder/editor parents, receipt replacement, repeated tab selection, invalid restored route and serialized route history.
- [x] Watch tests fail, implement PlayerNavigation reducer, run full Android unit suite.

## Task 2: native destinations

- [x] Integrate saved route history, five labeled semantic tab controls, SaveableStateHolder, appropriate child Back actions.
- [x] Add Quests daily checklist/real Mission receipts with actionable training/Progress routes and honest empty state; add Me profile summary and existing settings route.
- [x] Keep settings name draft across tab switching; preserve library detail/filter state and core saves.

## Task 3: native acceptance and delivery

- [x] Update native/Maestro selectors for Train and Me→Edit profile, retaining old Setup in baseline build-5 checks.
- [x] Add dedicated navigation/Me/Quests flow, saved-state/receipt Back, all five labels at compact/enlarged settings and keyboard captures.
- [x] Advance versionCode to 9; run Python regressions, compile, Android units, lint, all native flows and Maestro.
- [x] Review actual matching captures; request independent source review; resolve blocking findings.
- [x] Checkpoint on active development branch; publish only the exact accepted native APK using existing release workflow, independently verify public download.

## Decisions

Keep existing route names for legacy flows to reduce migration risk. Use the working library as Train's landing screen; do not insert another menu before its usable controls. Navigation history stores only short route strings; no training data is copied into Android instance-state bundles. Back from an unsaved recorder preserves its existing durable draft; Mission exit also pauses it.

## Execution ledger

- Contract checkpoint: 3c75025. New navigation tests fail on the pre-implementation source with missing PlayerNavigation/PlayerTab, observed in development run 43240534679.
- Initial implementation: 850aa3a. Development run 43241579376 passes unit/build/lint. Independent source review identified state-retention and date issues before release.
- Correction checkpoint: 04fd021. Restricts retained registries to roots/settings, hoists selected drill, preserves Progress filters, refreshes local date on resume/midnight and strengthens native tests. Seven Python selector/state checks and changed Python syntax pass locally. Final Android/native/publication checks pending.
- Minor deferred: recording-screen Back wording can still name Court even when the actual opener differs. Correct routing is implemented; copy polish is saved for the next issue.

- Final native review and publication: run 43251979732 succeeds with explicit retained-run lineage; run 43253825911 publishes identical APK and verifies unauthenticated public download. Artifact manifests and public API asset digest checked independently. Source/app tree, original signer, package, version 9, 72 tests, seven Python checks, all six native reports and Maestro verified. Rendered normal/compact/200%/full keyboard control reviewed. No additional runtime changes after 04fd021.
- Final review: independent source review completed; all five Important findings addressed and verified. Minor deferred: contextual recorder Back wording. Physical-phone/owner acceptance and full TalkBack remain separate. Next issue CQ-PLAYER-004.
