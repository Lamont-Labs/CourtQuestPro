# Versioned Mission Builder Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** A solo player builds, saves, reopens and resumes an ordered Mission, records each drill once, and finds version-linked receipts in Progress.

**Architecture:** Add immutable Mission versions, a durable builder draft, sessions and completion receipts to the current Gson save. Reuse the native drill recorder; freeze instructions, targets and measurement vocabulary at publication. Keep daily/practice identifiers and awards unchanged and annotate new legacy results with exact definitions.

**Tech Stack:** Existing Kotlin 2.0.21 / Compose BOM 2024.12.01 / Gson 2.11.0 / SDK35, min26; no new dependency.

**Spec:** docs/specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md and docs/plans/2026-11-11-player-mode-sequence.md (CQ-PLAYER-002).

## Global Constraints

- Preserve com.courtquestpro, original signer and Signature assets; version 8 for the next development candidate.
- Preserve daily slots/drafts, custom revisions, practice IDs, results, XP, award keys, matches and Undo.
- Use nullable additive fields for older Gson saves. Never invent timestamps for old evidence.
- Builder/session persistence is separate. Switching sessions requires an explicit resume/finish/discard choice.
- Solo Missions award no additional XP; existing daily and extra-practice policies remain.
- No coach, team, sync, authenticated roles or new five-tab navigation in this issue.

## Review Focus

- An interrupted/recreated drill must keep counters and cannot save twice.
- A custom drill revision/archive must not change a published Mission or queued work.
- An impossible generation constraint must preserve builder selections and explain recovery.
- A second session must not overwrite daily/practice/Mission attempts.
- Large text/keyboard must leave move/remove/save/start actions reachable.

### Task 1: Versioned definitions, deterministic generator and migration

**Files:** Create Mission.kt, MissionBuilder.kt; modify SaveState in MainActivity.kt; test MissionBuilderTest.kt.
**Interfaces:** MissionStep snapshots Drill, minimumSuccesses and measurement vocabulary. MissionVersion holds version identity, source, ordered steps, generation seed/input/candidate snapshots and createdAt. MissionBuilder publishes new immutable versions and separately persists its draft.

- [x] Add tests for older-save roundtrip (slots/drafts/custom revisions/practice/awards/results), immutable edits/archive, saved/reopened builder and revision creation.
- [x] Add tests for reproducible generation, skill/level/time/equipment/space limits, mandatory steps and actionable impossible constraints.
- [x] Observe RED; implement immutable snapshots and deterministic generation, duration and add/remove/move rules; run the suite.

### Task 2: Durable sessions and exact-once evidence

**Files:** Create MissionTraining.kt; modify DrillResult/TrainingLibrary/MainActivity; test MissionTrainingTest.kt.
**Interfaces:** MissionTraining.start/update/pause/resume/finishStep/discard; session ID + step ID idempotency, one completion receipt per session, result links to version/measurement. Legacy results gain additive provenance only when newly saved.

- [x] Add tests for process/Gson recreation, pause/resume, same/midnight dates, duplicate finish, invalid counters/targets, protected unfinished daily/practice, discard after partial saves and unchanged XP.
- [x] Observe RED; implement transitions and result/receipt links. Preserve saved partial evidence on discard.
- [x] Run all Android unit tests and build/lint.

### Task 3: Native builder, recorder and receipts

**Files:** Create CourtMissionBuilderScreen.kt, CourtMissionReceiptScreen.kt; modify MainActivity, Home/Library/Drill/Progress screens and DESIGN.md.
**Interfaces:** Builder edits persist immediately; publish and start use domain methods; Mission recorder uses frozen step vocabulary and target. Court/Drills expose builder and active resume. Progress opens immutable completion receipts.

- [x] Add a dedicated-emulator builder-to-receipt journey before implementation: select/reorder/remove, generate/lock, save/reopen/revise, interrupt/resume, repeated save, receipts/Progress and legacy conflict.
- [x] Implement shared Signature components, scrollable keyboard-safe forms, labeled 48dp move buttons, one clear primary action and concise durable-save feedback.
- [x] Verify normal/320dp-130%/critical200%/keyboard/reduced-motion captures; run existing library/daily/game/Maestro regressions on the exact candidate.

### Task 4: Review, exact-binary delivery and checkpoint

**Files:** workflows/native verification and authorized publication guard; master tracker, evidence ledger and next-chat handoff.

- [x] Review whole diff against migration/snapshot/idempotency contract and fix blocking issues.
- [x] Confirm unit/build/lint, all native journeys and APK package/original certificate/version8/alignment.
- [ ] Publish only the successful native-tested APK through existing development release; independently download/check bytes.
- [ ] Record exact source/run/APK evidence and any unverified device/accessibility limits. Keep main intact.
