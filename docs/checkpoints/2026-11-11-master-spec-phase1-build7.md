# Master specification Phase 1 — Signature build 7

Owner authority: implement the saved master specification and deliver the premium native journey autonomously, preserving the existing app/data/signing and GitHub delivery path. Work began at `f8089ac614250472c6190ce3fcbd3f67c7455ff1`. Continue from the current HEAD of `dev/premium-flow-toolkit`, never from the older main app source.

## Delivered

CQ-PREMIUM-001: Court → start/resume → attempts/corrections → save once → receipt/actual next drill → Progress. Shared clean Signature header, native navigation, black/orange smoked-glass surfaces, accessible reflow, fixed action areas and reduced-motion save acknowledgement. The original cream CQ/underline/basketball contours and handwritten wordmark are retained. Existing saved schema, dynamic profiles, custom drills/revisions, suggestions/favorites, protected shuffle/swaps, extra practice, XP, game scoring/Undo and history are preserved.

The exact master specification is committed at `docs/specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md`; its SHA-256 is `600028b9d4697965bd293c1fad359c868cb8ec82cb356df2eaa4e23ac5309fd7`. This milestone does not implement the whole master roadmap or establish school readiness.

## Exact source and binary

| Item | Value |
| --- | --- |
| Native-tested source | `375a607fd7be33bf30ad56c2337cf9acf3ae2253` |
| Native-tested app/src tree | `839a30e17949d0b7d9dbe9284999637e2117aa34` |
| Native run/artifact | `43136062719` / `13908407333` |
| Native artifact ZIP SHA-256 | `2a8872afc151a5d53e66db2aa7ec695b5565e9fd49562d141924cafa2dfe4c5c` |
| Publication commit/run | `a431a69254b9d896d2a76640f5af363530f0dfa6` / `43138905997` |
| Version/package | `1.0 (7)` / `com.courtquestpro` |
| APK size/SHA-256 | `20,988,666` bytes / `1e0c5b24bc6f1087733014a1acaaba6b72e04435a758f9631b8f3e51b7b58767` |
| Original certificate SHA-256 | `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b` |
| GitHub release/asset | `courtquest-dev-2026-11-05` / `737690893` |

[Direct development APK](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-dev-2026-11-05/CourtQuestPro-Android-debug.apk?build=7). Install/update over the existing app. The publisher retrieved the successful native artifact, checked matching app source and test reports, uploaded without rebuilding, downloaded again and reverified it. An additional public download matched it byte for byte and passed the identity/signature/ZIP/16 KB alignment checks.

## Completed checks

- 48 Android unit tests; no failures/errors/skips. Six new deterministic routing cases were observed failing before implementation.
- 3 visible-selector regressions; offscreen zero-area semantics cannot receive taps.
- Compilation and lint: 0 errors/11 warnings. The fixed stack is retained; six structural/resource warnings and five availability advisories are documented.
- Exact-binary library, daily and game native journeys: PASS. Build-5 update retention, custom revisions, protected shuffle, extra practice, repeated-save guards, XP, process/Activity restoration, date rollover, scores/Undo and history passed.
- Guarded Maestro: 1/1 PASS. Dedicated QA emulator only; physical-device and wrong-AVD refusal checks passed before reset/install.
- Normal 360 dp, 320 dp/130%, critical 200%, keyboard/insets and reduced-motion captures reviewed. Whole recording/correction labels, clock controls, fixed result actions and the keyboard-aware editor footer were observed.
- Development `43136062758`, toolkit `43136062726`, full native `43136062719`, publication `43138905997`, and release follow-up restored UI `43139207885` / development `43139206837`: SUCCESS.
- A legacy palette sentinel failed before compilation in `43138905972`; the approved orange literal was restored in tooling-only commits `7cdf542f46fa523e45a093b8809b65a9ced29b6b` / `8cd8657cc6ea7e7b158d3b1cf916934d21780b88`, then both follow-up checks passed. App/src and the public binary are unchanged.

The old toolkit handoff runs 43096106238/43096106289 remained queued at the latest explicit check. Fresh successful executions supersede them; they are not counted as successful.

## Limits

Physical-phone operation/performance and owner visual acceptance, full TalkBack traversal, API 26–30 Haze fallback rendering and alternate launcher/theme masks remain unverified. Enlarged recording captures are scrolled to controls; they do not frame every metric at once. Synthetic QA data is not shipped as player history. No connected sync, verified skill, coach access or pilot readiness is claimed.

## Resume next

Implement CQ-PLAYER-002, the versioned Mission foundation and builder. Follow `docs/plans/2026-11-11-player-mode-sequence.md`, including its current-code migration and acceptance contract. Preserve legacy nullable JSON, slot/session/award identities and archived drill snapshots. Do not invent timestamps for date-only historical results. Build one complete solo Mission save/reopen/start/resume/receipt/Progress journey, then verify existing flows and the exact higher-version development APK before continuing to five-tab Player navigation and Quests.

Kotlin 2.0.21 / Compose BOM 2024.12.01 / AGP 8.7.3 / SDK35/min26 / Haze1.1.1 / Lottie6.7.1 / Intro Showcase2.0.2 remain pinned. Use the shared native components and original Signature assets. Keep main separate. The authorized publisher branch remains dev/scorekeeper-undo-history. Further machine-readable proof is in `docs/tooling/evidence/signature-build7-native.json` and `signature-build7-public-download.json`.
