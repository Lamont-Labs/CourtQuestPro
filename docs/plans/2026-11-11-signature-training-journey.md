# Signature training journey implementation and verification

**Goal:** Deliver the owner-approved native Court → start/resume → record → save once → receipt/next drill → Progress journey, including reusable black-glass components, Signature artwork, shared header/navigation and an in-place development APK update.

**Authority:** Explicit owner request to proceed autonomously with the approved direction and existing GitHub delivery path. No additional design-selection gate. Start from current `dev/premium-flow-toolkit` HEAD `f8089ac614250472c6190ce3fcbd3f67c7455ff1`, verified against GitHub. Main and unrelated branches remain separate.

**Architecture:** Keep the existing native Compose screens and persistence. Share a smoked-glass surface, primary action, Signature header, navigation, metrics and receipt treatment. Retain save/XP guards and all daily/library/game behavior. Make next-drill routing deterministic (resume a current saved draft, otherwise first unfinished slot), so the receipt can name the actual next drill; explicit protected shuffle remains in the library.

**Tech stack:** Kotlin 2.0.21, Compose BOM 2024.12.01, AGP 8.7.3, SDK 35/minSdk 26, Haze 1.1.1, Lottie 6.7.1, Intro Showcase 2.0.2.

**Spec:** Owner request; AGENTS.md; DESIGN.md; docs/tooling/PREMIUM_FLOW_TOOLKIT.md; docs/tooling/PREMIUM_FLOW_VERIFICATION.md; original approved Signature brand pack.

## Constraints and implementation calls

- Preserve `com.courtquestpro`, certificate `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`, local saves and dynamic names.
- Preserve custom drill revisions, suggestions, favorites, protected shuffle/swaps, extra practice, daily results/XP, scoring, Undo, history and first-install onboarding.
- Use dominant black `#000000`, vivid orange `#FF650E`, cream/white foregrounds and neutral smoked surfaces. Render highlights/reflections behind sharp foreground content.
- Keep supported Haze 1.x APIs; use a tinted fallback on API 26–30. No compiler/Compose/Haze upgrade.
- Keep counters and controls immediate. No decorative animation during recording. System animation scale must disable completion motion.
- Retain 48 dp targets, native semantics, wrapping labels, safe insets and keyboard-aware editing.
- Advance build 6 to build 7 after checking current release metadata. Publish only the exact verified APK and independently verify downloaded bytes.
- Figma flow-board creation was attempted; it requires a team/organization selection. The source-grounded specification remains here, and native work continues without that optional board.
- First generated transparent extraction attempts have visible residue and are rejected. Production assets must use clean, fidelity-preserving artwork; no residue drafts enter app resources.

## Review focus

- Repeated save taps, recreation and relaunch must not duplicate a result or XP.
- A resumable draft must win over an untouched slot; a receipt's next drill must agree with the actual route.
- Blank/long names and long custom drill titles must wrap without pushing critical controls out of reach.
- 320 dp/130% and critical controls at 200% must retain complete labels, touch targets and reachable scrolling/footer.
- Shared material/navigation changes must preserve daily, game/Undo and custom-drill journeys, including keyboard and reduced-motion settings.

## Tasks

### 1. Baseline and assets

- [x] Read required documents and actual screen/state/save code.
- [x] Inspect original Signature reference, wordmark and launcher.
- [x] Check previous run 43096106238: still queued with no assigned job at task start.
- [x] Complete dedicated-emulator baseline captures and guarded Maestro execution, locally or in GitHub.
- [x] Prepare clean Signature native assets and document source fidelity.

### 2. Shared components and complete journey

- [x] Add `TrainingJourney.kt` and meaningful unit tests for deterministic draft/next-slot routing before implementation.
- [x] Update `CourtQuestDesign.kt`, `CourtQuestPremiumSurface.kt`, `CourtQuestPrimaryButton.kt`, `AppHeader.kt`, theme and navigation.
- [x] Apply the components to Court, drill recording/timer, receipt with named next drill, and Progress with dated history.
- [x] Keep persistence, scoring and custom-drill logic intact; exercise actual saved data.
- [x] Use Context7 and pinned-source APIs, and record applicable sample/review guidance in DESIGN.md.

### 3. Android verification and rendered review

- [x] Run Android unit tests, assemble and lint; inspect results separately.
- [x] Exercise existing daily/library/game scripts and guarded Maestro; strengthen only selectors/setup that legitimately change.
- [x] Review real normal/compact/large-text captures, fresh/existing/blank/long profiles, empty/completed states, keyboard/insets and motion settings.
- [x] Fix observed issues; retain exact candidate evidence and independent code review.

### 4. Exact-binary delivery and checkpoint

- [x] Advance version, verify package/signer/ZIP/alignment, and update existing development release through its authorized path.
- [x] Download the public APK and verify its hash/version/package/certificate against the tested binary.
- [x] Commit source, evidence ledger, completed checks and remaining limitations; provide the direct GitHub APK URL.

## Execution ledger

This document records implementation decisions under the owner's autonomy instruction. Device, build and owner visual acceptance are separate claims. Update this ledger with observed results before handoff.

## Final native acceptance

Source `375a607fd7be33bf30ad56c2337cf9acf3ae2253`, app/src `839a30e17949d0b7d9dbe9284999637e2117aa34`: [full native run 43136062719](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062719) succeeded. 48 Android unit tests, 3 selector regressions, 0 lint errors/11 warnings, all library/daily/game checks and guarded Maestro (1/1) passed. [Exact native evidence](../tooling/evidence/signature-build7-native.json). Device captures confirm whole recording/correction labels, readable clock/Start/Reset at 200%, reachable result actions and an editor footer above the keyboard. The 200% recording capture is scrolled to controls; it does not show the entire metric panel in one frame. Publication and independent public downloaded-byte verification passed in the delivery below.

## Published delivery

Build 7 is published by `a431a69254b9d896d2a76640f5af363530f0dfa6` in [release run 43138905997](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43138905997). The public 20,988,666-byte APK matches the native artifact byte for byte, SHA-256 `1e0c5b24bc6f1087733014a1acaaba6b72e04435a758f9631b8f3e51b7b58767`. Independent public download passed the original certificate, package, version 7, v2 signature, ZIP and 16 KB alignment checks. [Public-download evidence](../tooling/evidence/signature-build7-public-download.json). [Direct APK](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-dev-2026-11-05/CourtQuestPro-Android-debug.apk?build=7).

Main remains separate. The next scoped master-spec issue is CQ-PLAYER-002; physical-phone/owner acceptance, full TalkBack, API 26–30 fallback and alternate launcher masks remain explicit limits. A legacy release palette sentinel was updated to match the approved vivid orange; its follow-up restored-UI run 43139207885 and development run 43139206837 passed. This is a tooling-only change, not a new APK.
