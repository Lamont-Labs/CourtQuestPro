# Mission Builder verification — CQ-PLAYER-002

Published development version 8, package com.courtquestpro; native acceptance and independent public-download verification passed. Scope: immutable solo Mission definitions, durable builder, ordered manual selection, constrained seeded generation, interrupted sessions, exact-once results/completion receipts and Progress receipt reopening.

Local verification (November 11, 2026 Pacific): 61 Android unit tests, no failures/errors/skips; APK compilation passed; lint 0 errors/7 warnings. Three existing visible-selector regressions pass. Original certificate, v2 signature, manifest/launcher/DEX, ZIP and 16 KB alignment checks pass. Twenty tracked brand/resource/signing paths match baseline e4bf55b byte for byte.

New tests were observed RED before implementation. Additional regressions first failed on missing practice provenance and stale completion date, then passed after the changes. Review found an outgoing AnimatedContent route could override the last-step receipt; fallback effects now check their current route, and the native script explicitly checks final completion. The completion date and timestamp now come from one Instant and disagreeing evidence is refused. Changed generator inputs are identified as pending regeneration.

Existing legacy result/draft/slot/practice/award identities remain intact. Newly completed daily/practice drills gain additive frozen definitions; historical timestamps/provenance are not backfilled. Personal Missions use the zero-extra-XP practice policy. No team roles, sync, assignments, five-tab navigation or school readiness are claimed.

Native acceptance passed in [run 43147187632](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43147187632), artifact 13913082538. All 61 Android unit tests and seven Python verification checks pass; lint is 0 errors/11 warnings. The exact APK passed the existing library/daily/game suites, the complete two-drill Mission journey and guarded Maestro. Normal/320dp-130%/critical200%/keyboard captures were reviewed; see [rendered review](MISSION_BUILDER_RENDERED_REVIEW.md) for the precise scope and limits. Independent artifact download matches the declared SHA-256 and passes original signer, v2 signature, ZIP, manifest and 16KB alignment checks. [Machine-readable evidence](evidence/mission-build8-native.json). The exact tested artifact was published without rebuilding it in [run 43233289181](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43233289181).

Physical-phone performance, owner visual acceptance, full TalkBack traversal, API26–30 fallback and alternate launcher masks remain separate, unverified checks.

## First native run retained as a failure

Run 43144393060 passed all library and daily assertions on APK SHA-256 `7bee93ac1870911008db5973c9a374f0d4eb8db6e05ed917ba0df0db144ca04d`, then stopped on the game reset assertion before the Mission script ran. Artifact 13912091037 preserves the captures, reports, APK and logcat; this is not an accepted run. The failing launch started at 03:12:35.332 UTC; its delayed first frame completed at 03:12:38.382 with 136 skipped emulator frames, immediately before the fixed-three-second state assertion. The test now waits up to 30 seconds for the complete persisted reset and records its measured wait/state; all original identity, zero-score and history assertions remain. Four readiness regressions plus three existing selector regressions pass. Mission verification runs after the library update and before the remaining daily/game suites for earlier feature feedback. No app/src change was made by this harness correction.


The repeated native run used the same APK bytes as the first run. Both previous-day game fixtures completed all strict date/identity/score/history checks; the measured readiness waits after launch were 0.024 and 0.014 seconds. No application source change was required by the readiness correction.

## Exact-binary delivery passed

Release commit `cea3890f7958998f5c1afa885893aff13a669053`; publication run `43233289181` SUCCESS, evidence artifact `13949898896`. The fixed development release now serves version1.0(8), 21,103,354 bytes, SHA-256 `7bee93ac1870911008db5973c9a374f0d4eb8db6e05ed917ba0df0db144ca04d`. Both the workflow and an independent public download matched the accepted native APK byte for byte. Package, original certificate, v2 signature, ZIP/DEX/manifest/launcher and 16KB alignment checks passed independently. Sixteen build-8 Android captures accompany the release. [Public-download ledger](evidence/mission-build8-public-download.json).
