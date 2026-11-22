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

- [x] Domain and compatibility: `TrainingLibrary.kt`, optional model fields in `MainActivity.kt`, target overloads in `DrillTargets.kt`; meaningful tests in `TrainingLibraryTest.kt` cover validation, archival/revisions, legacy JSON, favorite migration, safe replacements and practice idempotency.
- [x] Native library and editor: `CourtDrillLibraryScreen.kt` and `CourtCustomDrillEditor.kt`, shared tokens and semantic native controls, stable keyed lists, keyboard footer, clear empty/results states.
- [x] Integrate: Court launch/shuffle/swap/resume, Drills navigation, shared practice recording, real custom targets, historical titles/measurement definitions and honest practice receipt/XP.
- [x] Review and verify: full existing unit suite, build, lint; real Android build-5 update plus old daily/game regressions and library create/edit/favorite/suggestion/swap/shuffle/protected-state/practice/history/relaunch cases. Inspect normal, compact, keyboard and 200% captures. Review the complete branch once independently.
- [x] Release exact tested APK as build 6, verify downloaded identity/hash/certificate, update DESIGN.md/README and this ledger; keep main intact.

## Review focus

Incomplete/invalid custom inputs must not write definitions. Edits/archives must preserve old results and queued targets. Shuffling must not create fresh award IDs or erase drafts. A practice save must not duplicate after taps/relaunch. Old JSON and the existing one-on-one flow must remain usable.

## Execution ledger

RED: run 43049795517 on test/spec commit 1f7b1c99 failed at the missing DrillInput/TrainingLibrary/model interfaces before implementation. New optional saved fields and the pure library/practice domain now implement those contracts. The native editor/library, daily selection and extra-practice recording are wired for the first compiled candidate. Official Android documentation confirmed testTagsAsResourceId support since Compose 1.2; semantic resource tags make native form checks stable without changing visible copy.

First compiled implementation `f97f022c` passed Android development checks in run 43051364479, including the existing tests and all new library/practice tests. Native acceptance now checks real custom creation/validation, favorites, safe daily swaps/shuffling, revision preservation while practice is active, relaunch/resume, actual custom goals, practice/daily receipts, archival and compact/enlarged keyboard use.

Independent review: no Critical finding; one Important finding was route loss on Activity recreation. Root route IDs and editor arguments now use saved instance state, selected daily slot and result receipt identity are retained, and game setup inputs survive configuration changes. The native acceptance verifies unsaved new input through font-scale recreation and edited input through rotation, with no premature definition write. Daily recording returns to Court if the daily rollover invalidates its old slot. No other blocking source finding was reported.

Native run 43052379477 passed daily/game assertions and exercised custom creation, favorites, daily swap/shuffle protection, edit revision and saved practice resumption. Its receipt assertion failed: AnimatedContent briefly recomposed the outgoing practice after the session cleared, and the missing-session redirect overrode the receipt. The redirect now runs only while the current route is still practice. This failure is the RED reproduction of the transition fix. The library scenario now runs first after a verified build-5 in-place update, followed by the unchanged daily/game journeys; all three exact-binary reports remain mandatory for publication.

Local tooling: `git diff --check` and Python bytecode compilation passed for the daily/game/library native-flow drivers and APK verifier. This Android branch has no root `tests/` directory; the earlier historical Python repository suite belongs to a separate source tree. The Android unit suite and real-emulator workflows are the current acceptance gates.

## Owner walkthrough

1. Open **Drills → Create custom drill**. Set the name, skill, difficulty, planned minutes, successful-action target and instructions, then save.
2. Open the saved drill to favorite it, start extra practice, or use it in a suitable untouched daily slot. The Court screen also offers **Swap** beside eligible slots and **Shuffle remaining**.
3. Leave a practice and relaunch the app. **Resume extra practice** restores attempts and the training clock. Finish the target, confirm it and save; Progress labels the result **Extra practice**.
4. Edit a custom drill while its earlier version is queued. The queued workout and saved practice keep the earlier name, instructions and goal; the edited version is available for future selection.
5. Archive a custom drill when finished with it. Its results and queued work remain available. Existing daily XP and game rewards retain their previous rules.

Native run 43055431111 on `272759aa` passed all three exact-binary journeys with 42 Android tests and lint 0 errors/10 warnings. APK SHA-256: `b40501501f54c45cd635cc8308b50da60083cfbe074f86c9fcb1db40d484deba`. The practice receipt transition is GREEN. Actual captures confirm normal/compact library, detail, shuffle, practice receipt/history and the keyboard-aware editor. The 200% capture exposed inside-word wrapping in the brand and Progress navigation label; the next candidate makes the brand row wrap as one group and uses the already bundled condensed typeface for enlarged navigation labels without reducing text scale.

The final independent follow-up found an Important restoration edge case: saving the root route also saves a live score screen across process restoration. On the following date, the daily reset cleared the match fields but invalidated only a daily drill route. A restored score route therefore remained live for an expired match. Native regression commit `156aad8d` first checks actual background-process Activity restoration on the same day, then restores it against an expired date and requires Court with no game/XP write. This is separate from the existing force-stop/new-launch rollover checks.

RED confirmed in native run 43058994537: same-day process restoration resumed Jordan/Casey at 2–1, while next-day restoration showed the cleared 0–0 live scoreboard with blank player names. The failing assertion required Court's Start training action. The correction invalidates both daily-drill and score routes during date reset, and gates point/Undo callbacks with the existing same-day valid-match rule. The same native assertion remains the regression test.

The independent follow-up reviewed the final route, callback and enlarged-text corrections and reported no remaining Critical or Important finding. Candidate `0ff5e6c5d78ba8b958deef01478475fe5c78f4a0` (app/src tree `57fcba4679745dae0a1b85dff0bd93235b2bc757`) completed exact-binary verification in native run 43060242201. This candidate supersedes the earlier green artifact; only its completed checks and published-byte verification can authorize the build-6 release.

## Final Android acceptance

Candidate `0ff5e6c5d78ba8b958deef01478475fe5c78f4a0`, app/src tree `57fcba4679745dae0a1b85dff0bd93235b2bc757`, passed [native run 43060242201](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43060242201). Artifact `13872814689` contains the exact 17,047,484-byte APK with SHA-256 `00bcd7d1296ac2a71efadad8beb42834e3f9897ff7b5af668c020c731507a3d4`.

- 42 Android unit tests passed; zero failures, errors or skips. Lint: 0 errors, 10 warnings.
- All library, daily and one-on-one journey assertions passed against that same APK hash. Installing over the preserved build 5 retained the saved state. Existing profile, history, daily awards and match behavior remain covered.
- Real background-process restoration resumed a same-day 2–1 match; the same saved score route restored against the next date returned to Court without a match or XP write.
- Native custom creation/validation, favorites, daily swaps and protected shuffling, practice relaunch/resume, edit revision/goal preservation, exactly-once practice recording, original target in daily recording, archival and history all passed.
- Actual normal and 320 dp/130% library captures, 200% library/navigation and 200% keyboard/footer captures were reviewed. The full brand and Progress label stay whole; the editor's Save action remains above the keyboard. Practice receipt and original-definition Progress history were inspected.
- Independent review is closed with no remaining Critical or Important source finding. Physical-phone operation, owner visual acceptance for this candidate and full TalkBack traversal remain separate.

Publication uses this tested artifact without rebuilding. The release gate rechecks its source tree, all three flow reports, test/lint reports, manifest, v2 signature, original certificate and alignment, then downloads the uploaded APK again. Publication outcome and a further public-download checksum follow below after completion.

## Published checkpoint

Build 6 is published on the existing development release. Release commit `e28a28680e397474584c80cd68bb991221cbda8d` and [release run 43063480763](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43063480763) passed source/test/journey/identity verification, uploaded the exact native-tested APK and verified the downloaded release again. The original package `com.courtquestpro`, certificate `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b` and existing asset URL are preserved.

A further independent public download of `CourtQuestPro-Android-debug.apk?build=6` matched the tested APK byte for byte: 17,047,484 bytes, SHA-256 `00bcd7d1296ac2a71efadad8beb42834e3f9897ff7b5af668c020c731507a3d4`. ZIP integrity passed. The release contains five named build-6 Android captures. README and DESIGN.md now identify this as the published checkpoint. Main was not changed. Owner phone/visual acceptance and full TalkBack traversal remain open.
