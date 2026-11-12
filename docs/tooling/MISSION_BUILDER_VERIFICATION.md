# Mission Builder verification — CQ-PLAYER-002

Candidate version 8, package com.courtquestpro. Scope: immutable solo Mission definitions, durable builder, ordered manual selection, constrained seeded generation, interrupted sessions, exact-once results/completion receipts and Progress receipt reopening.

Local verification (November 11, 2026 Pacific): 61 Android unit tests, no failures/errors/skips; APK compilation passed; lint 0 errors/7 warnings. Three existing visible-selector regressions pass. Original certificate, v2 signature, manifest/launcher/DEX, ZIP and 16 KB alignment checks pass. Twenty tracked brand/resource/signing paths match baseline e4bf55b byte for byte.

New tests were observed RED before implementation. Additional regressions first failed on missing practice provenance and stale completion date, then passed after the changes. Review found an outgoing AnimatedContent route could override the last-step receipt; fallback effects now check their current route, and the native script explicitly checks final completion. The completion date and timestamp now come from one Instant and disagreeing evidence is refused. Changed generator inputs are identified as pending regeneration.

Existing legacy result/draft/slot/practice/award identities remain intact. Newly completed daily/practice drills gain additive frozen definitions; historical timestamps/provenance are not backfilled. Personal Missions use the zero-extra-XP practice policy. No team roles, sync, assignments, five-tab navigation or school readiness are claimed.

Native acceptance is pending. Required: existing library/daily/game/Maestro regressions plus the builder/reorder/remove/save/reopen/two-drill/interrupt/resume/receipt/Progress/legacy-conflict journey, normal and 320dp/130% and critical200%/keyboard evidence. Publish only the exact successful native-tested artifact, with the original signer, via the established development release. Local diagnostic APK is not the release artifact.

Physical-phone performance, owner visual acceptance, full TalkBack traversal, API26–30 fallback and alternate launcher masks remain separate, unverified checks.

## First native run retained as a failure

Run 43144393060 passed all library and daily assertions on APK SHA-256 `7bee93ac1870911008db5973c9a374f0d4eb8db6e05ed917ba0df0db144ca04d`, then stopped on the game reset assertion before the Mission script ran. Artifact 13912091037 preserves the captures, reports, APK and logcat; this is not an accepted run. The failing launch started at 03:12:35.332 UTC; its delayed first frame completed at 03:12:38.382 with 136 skipped emulator frames, immediately before the fixed-three-second state assertion. The test now waits up to 30 seconds for the complete persisted reset and records its measured wait/state; all original identity, zero-score and history assertions remain. Four readiness regressions plus three existing selector regressions pass. Mission verification runs after the library update and before the remaining daily/game suites for earlier feature feedback. No app/src change was made by this harness correction.
