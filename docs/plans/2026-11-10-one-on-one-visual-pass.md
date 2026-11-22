# CourtQuest Pro: one-on-one visual pass

Base: `6e92997265c862b67c3d388fd30f1bbb88695db9` (published visual build 4).
Candidate: `dev/premium-game-flow`. Delivered: published visual build 5.

## Authorized continuation

Continue the owner's app-wide premium native Android work under root DESIGN.md.
Extend the established dark/orange, bundled Barlow system through game setup,
live scoring, the recorded game receipt and Progress. Implementation and the
existing development APK update were already authorized. Physical-device and
owner visual acceptance remain separate from automated checks.

## Design and boundaries

Use a compact game header, one unified scoreboard, consistent player lanes and
immediate native point/Undo actions. Keep the displayed score and player names
legible with enlarged type. Setup places the start action above the keyboard;
the receipt shows the actual saved names, date, score and earned XP. Reuse that
receipt anatomy in dated game history. Keep utility copy short and readable.

The existing score and scoring order persist, but Court has no route back into
that match: starting again clears them. Add an explicit Resume 1-on-1 entry for
an unfinished same-day match, preserving the saved score and Undo order. Do not
add a new schema, change first-to-11/sudden-death or XP rules, or invent metrics.

Alternatives considered: restyling each existing card preserves the crowded
score/action duplication; a new navigation or scoring model expands the scope.
Use existing state and native controls with a focused game presentation.

Preserve `com.courtquestpro`, the existing signer, offline assets, player
identity, saved drills, scorekeeper correction, history, and onboarding. `main`
has independent changes and is not this delivery's integration path; leave that
branch and its work intact. No imported assets or dependencies are needed.

## Plan and verification ledger

- [x] Capture build-4 setup/scoring in a current native run. Demonstrate the
  missing resume path with a behavioral Android regression check.
- [x] Implement shared native setup, scoreboard, game receipt and game history.
  Add the resume route using existing same-day saved match state.
- [x] Run Android unit tests, build, lint and APK identity/signature checks.
- [x] Verify score/Undo, exit/resume, process restart, 10–10 sudden death,
  save-once, correct XP, recap and dated history. Preserve the daily drill flow.
- [x] Review matched Android renders at 360 dp, 320 dp/130%, critical 200%
  controls, landscape, tablet and disabled system animations. Fix visible issues.
- [x] Publish the exact tested APK as build 5 through the existing GitHub URL;
  download it and verify hash, version, package and original certificate.

## Applied references

UI/UX Pro Max: Fitness/Gym App matched the product and reasoning profile without
fallback; Sports/Fitness retained Barlow Condensed/Barlow. The landing-page and
hover suggestions are web-specific and are not applied to this native flow.
The focused touch query returned Mobile Touch Target Size (Android 48 dp) and
Active States (immediate feedback). The Compose query returned semantics,
theme colors and shallow layouts; its 1.11.4 target is newer than this project's
BOM 2024.12.01, so only already-supported primitives are used. Official Android
guidance verified BoxWithConstraints, stateDescription and polite live regions.
Apply the saved Impeccable review questions to real captures: hierarchy,
unnecessary surfaces, clear first action, stable controls, text/outline contrast,
semantics, enlarged text, keyboard and system insets. No generated image is
needed for this functional native game flow.

## Behavioral regression confirmed

[Native run 42954008362](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42954008362) built unmodified build-4 application code. Daily-flow assertions passed. The game check scored 5–3, corrected it to 5–2 with the expected saved Undo order, returned to Court and failed because Resume 1-on-1 was unreachable. Artifact 13825849017 retains the current-run baseline and failure evidence. This is the RED result for the resume fix.

Receipt correction: preserve the existing per-day award keys and store the actual XP increase for each new game. Repeated games can record 0 XP. Determine a win from the scores, including when both players enter the same display name.

## Consolidated review corrections

- Independent source review found a daily rollover regression: old names survived the existing score reset. The reset now clears match identity too; native fixtures cover yesterday’s unfinished and finished games.
- Saved game dates retain the year, so records from multiple seasons remain identifiable.
- Native game-history coverage now opens an eight-record full list, reaches an older record and uses native Back.
- Candidate `cbb8b21` compiled, passed all 31 Android tests and lint, and passed the complete daily flow plus scoring/Undo, exit/resume and process restart. Its extended native run `42955538196` stopped at a harness bug: an XML leaf element was tested for truthiness, so the activity recreation resume action was skipped. The harness now checks `is not None`. This was not an application crash or an inferred runtime pass.

## Rendered review of the first build-5 candidate

Inspected exact Android PNGs from run `42955538196`, artifact `13827875451`, against current-run build-4 reference captures from `42954008362`. The setup reduces the two-line generic header and boxed rules to a compact title, numbered rule rows and a stable Start game footer. With the keyboard open, the active opponent field and Start game are both visible; build 4 hid the action below the keyboard. The live game uses a single score surface and short instruction, with one +1 per action. At 200% text, point actions stack and remain reachable. The Court capture shows Resume 1-on-1 leading the completed-session panel. These are observed refinements, not owner visual acceptance.

The first candidate’s XML reports contain 31 Android tests, zero failures/errors and lint with zero errors / ten warnings. New native verification retains the dated receipt and full-history views and checks landscape, tablet, sudden death, repeated awards, identical names and daily rollover. Publication remains gated on those actual results.

## Final native candidate verified — November 11, 2026

[Run 43043445045](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43043445045) passed the complete daily and game journeys. Source `67e9776c7983e77ef870146aede6db28927be51b`; app/src tree `c33f5a258704f520f1e95872661134a09bc14aa3`. Artifact `13866239034` retains the exact APK, all captures, raw UI hierarchies, both behavioral reports, 31 passing Android tests, lint (0 errors / 10 warnings), package/certificate/ZIP/alignment verification, and source revisions. The downloaded artifact ZIP and APK hashes were checked locally.

APK SHA-256: `d2e6eb32d3d90d93a3be3cbbc08684e5090202746294e2ea0bf3583b5817339b`. Package `com.courtquestpro`, version 1.0 (5), 16949180 bytes; original certificate `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`. The helper manifest reports emulator testing separately; the candidate-result and game-result reports bind the passing journeys to this exact hash. The publication job combines that evidence before writing the release.

Manually inspected the final receipt, Games view, 200% scoring controls, landscape control capture, tablet scoreboard and full game list. Scores/names/date/year and saved XP are readable, and enlarged controls reflow. Landscape uses vertical scrolling; its capture is a control-reachability check, not acceptance of an optimized landscape composition. All captures use synthetic profiles. Physical-phone operation, owner visual acceptance and full TalkBack traversal remain open.

## Published delivery and continuation

Release commit: `64a86fb96b1d182e2bda2e126a5c7570e3a0a434`. [Publication run 43045952485](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43045952485) succeeded: it downloaded the successful native artifact, verified source/app identity, tests, lint and both exact-binary journey reports before publication, then downloaded and re-verified the released bytes. The actual public GitHub APK URL was independently downloaded locally; size, ZIP integrity, SHA-256 and full-byte comparison to the Android-tested APK all matched.

[Install build 5 over the existing app](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-dev-2026-11-05/CourtQuestPro-Android-debug.apk?build=5). The same release retains five build-5 native PNGs and the earlier build-4 comparison images. Application ID, version advance and the original certificate are verified; no signing identity or save schema changed.

No open Critical or Important code-review finding remains in this slice. The rollover finding was fixed and exercised on Android. The history navigation and year-identification review findings were also addressed. Both verification-script issues were corrected before the final successful run. Source/app work and release evidence are saved in GitHub; `main` remains intact. Continue from this release and root DESIGN.md. Owner phone/visual acceptance, full TalkBack traversal and a landscape composition refinement remain open.
