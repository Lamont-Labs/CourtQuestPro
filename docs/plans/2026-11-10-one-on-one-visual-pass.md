# CourtQuest Pro: one-on-one visual pass

Base: `6e92997265c862b67c3d388fd30f1bbb88695db9` (published visual build 4).
Candidate: `dev/premium-game-flow`. Intended delivery: visual build 5.

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
- [ ] Implement shared native setup, scoreboard, game receipt and game history.
  Add the resume route using existing same-day saved match state.
- [ ] Run Android unit tests, build, lint and APK identity/signature checks.
- [ ] Verify score/Undo, exit/resume, process restart, 10–10 sudden death,
  save-once, correct XP, recap and dated history. Preserve the daily drill flow.
- [ ] Review matched Android renders at 360 dp, 320 dp/130%, critical 200%
  controls, landscape, tablet and disabled system animations. Fix visible issues.
- [ ] Publish the exact tested APK as build 5 through the existing GitHub URL;
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
