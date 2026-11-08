# CourtQuest Pro — Premium Native UI Design Contract v1

Status: design baseline, not visual acceptance. This contract governs subsequent one-change-at-a-time implementation.

## Product identity
A basketball performance studio, not a generic scorekeeper. Preserve existing dark/brown/orange identity while elevating hierarchy, rhythm, depth, and motion. Keep user-created player names and training data dynamic. Do not hardcode Vincent or any other person.

## Source audit (2026-11-07)
- Native Kotlin Jetpack Compose, application ID `com.courtquestpro`, minSdk 26, targetSdk 35.
- Current Compose BOM `2024.12.01`; Material3 already present.
- `AppHeader.kt` contains warm gradient, basketball mark, PLAYER LAB chip, title and subtitle. This is the first bounded visual component.
- No third-party UI dependency is necessary for initial work.

## Visual language
- Background: deep near-black `#121315`; elevated surface `#252127`; warm secondary `#4B281D`.
- Primary text `#F7F3EF`; secondary `#B7ACA6`; primary orange `#FFA35C`; vivid orange `#FF783D`.
- Spacing scale (dp): 4, 8, 12, 16, 24, 32, 40.
- Corners: 12dp controls, 16dp standard cards, 24dp hero surfaces; pill for chips.
- Typography: high-contrast editorial heading; compact uppercase eyebrow labels; readable secondary content. Do not globally replace fonts without device proof.
- Motion: purposeful and short, never decorative in active scorekeeping; honor reduced-motion settings.
- Accessibility: touch targets at least 48dp where feasible, semantic labels, scalable text, legible contrast, no color-only meaning.

## Screen priorities
1. Onboarding: clear first-run player setup, skill level and goals; skip/revisit; persist selections.
2. Home: a single clear next action, training summary, current progress.
3. Drills: selection, instructions, active state, completion, result.
4. Scorekeeper: fast and reliable input, unmistakable current score, undo/correction.
5. Career/progress: meaningful statistics and milestone history.
6. Settings: transparent preferences and data controls.

## Open-source evaluation
- `android/compose-samples` (Apache-2.0): reference architecture and custom design system patterns, especially Jetsnack. Do not wholesale copy app screens.
- `rainxchzed/RikkaUi`: interesting Compose Foundation component library; actively developing. Audit LICENSE, API compatibility and dependencies before importing.
- `afiksoco/compose-uikit` (MIT): small motion components; very young project. Consider adapting individual patterns only after license and compatibility checks.
- No external UI package added in this baseline. Do not execute remote install scripts in CI.

## Implementation and acceptance gates
- Implement one screen or component at a time in an isolated candidate.
- Preserve package ID, signing, existing behavior, persistence and player data.
- Every candidate must pass Kotlin compilation, unit tests and lint before technical checkpoint.
- A technical checkpoint is not a premium UI pass. Visual acceptance requires review of an actual screen on a device or validated screenshot baseline.
- Retain source hashes, patch, build/test/lint results and review decision.
- The 800-pass ledger remains 0 accepted until the first visually reviewed improvement is approved.

## First execution target
Refactor `AppHeader.kt` to use shared CourtQuest design tokens without changing the rendered colors, dimensions or public API. Then incrementally refine layout with before/after evidence and human approval. This avoids relying on a 1.5B model to rewrite an entire Kotlin file.
