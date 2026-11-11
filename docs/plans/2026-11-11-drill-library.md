# Drill Library implementation plan

Owner approved custom drills, suggestions, safe randomization and independent extra practice with “Do it.” Build 5 received positive owner visual feedback. Base: `7e18770f45723f9a19852716188db04492773155`. Candidate: `dev/drill-library`; intended APK version code 6.

## Specification

Keep the approved dark/orange native Barlow design. Add a Drills navigation destination with searchable All/My drills/Favorites views, skill filters, transparent level/focus/recent-activity suggestions, drill detail and a custom editor. A custom drill has a name, one of the six existing skill categories, difficulty, 1–60 planned minutes, instructions and a measurable target of 1–500 successful actions. This delivery uses the existing success/attempt measurement model; duration remains the training clock rather than an invented timed-performance metric.

Players can use a suitable drill in an untouched daily slot, swap an untouched drill, shuffle the remaining eligible choices, or start independent extra practice. Completed slots and any saved draft are locked against replacement. Slot IDs and award keys survive swaps/shuffles. Suggestions and random replacements use eligible library content at or below the selected level; suggestions explain their rule instead of claiming AI coaching or diagnosing weaknesses from incomparable drills.

Extra practice resumes after relaunch, records once in Progress and has an explicit discard confirmation. Extra practice records training activity; the existing daily-workout XP and streak rules remain unchanged. Display this before starting and identify extra-practice receipts/history clearly. An unfinished practice blocks replacement by a different practice until resumed/completed or explicitly discarded.

Custom edits create a new definition ID and retain old definitions for queued drills and historical comparisons. New results retain the actual drill definition. Archiving removes a drill from future selection while preserving history and queued work. Favorites follow the edited version. Optional saved fields keep existing JSON readable. No backend, account, paid service, new dependency, package change or signer change.

## Approaches and decisions

The approved approach is a unified library supporting both daily selection and extra practice. A simple add-name form would fail the existing unknown-target completion guard. Reusing daily slots for extra practice would overwrite workouts/awards. Use shared native recording UI with a separate persisted practice session and versioned custom definitions.

UI/UX Pro Max matched Fitness/Gym App and Sports/Fitness without fallback: retain the approved palette/fonts rather than its web landing-page recipe. The targeted UX search matched actionable empty states; web search-result guidance is adapted only to local filtering. Compose guidance matched stable LazyColumn IDs, virtualization and scoped effects; only APIs supported by the pinned BOM 2024.12.01 are used.

## Implementation tasks

- [ ] Domain and compatibility: `TrainingLibrary.kt`, optional model fields in `MainActivity.kt`, target overloads in `DrillTargets.kt`; meaningful tests in `TrainingLibraryTest.kt` cover validation, archival/revisions, legacy JSON, favorite migration, safe replacements and practice idempotency.
- [ ] Native library and editor: `CourtDrillLibraryScreen.kt` and `CourtCustomDrillEditor.kt`, shared tokens and semantic native controls, stable keyed lists, keyboard footer, clear empty/results states.
- [ ] Integrate: Court launch/shuffle/swap/resume, Drills navigation, shared practice recording, real custom targets, historical titles/measurement definitions and honest practice receipt/XP.
- [ ] Review and verify: full existing unit suite, build, lint; real Android build-5 update plus old daily/game regressions and library create/edit/favorite/suggestion/swap/shuffle/protected-state/practice/history/relaunch cases. Inspect normal, compact, keyboard and 200% captures. Review the complete branch once independently.
- [ ] Release exact tested APK as build 6, verify downloaded identity/hash/certificate, update DESIGN.md/README and this ledger; keep main intact.

## Review focus

Incomplete/invalid custom inputs must not write definitions. Edits/archives must preserve old results and queued targets. Shuffling must not create fresh award IDs or erase drafts. A practice save must not duplicate after taps/relaunch. Old JSON and the existing one-on-one flow must remain usable.
