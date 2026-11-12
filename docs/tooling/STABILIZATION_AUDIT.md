# Native Android stabilization audit

## Source and boundaries

The import initially checked out `main` at
`1bf6159` (Replit configuration over `02ce5d4`). No work was reset or
overwritten. The owner-specified development branch was fetched and confirmed
at `c06fb3e0c084cf72e05a007eea3b8e9f84b6a0c7`.
Repairs are isolated on `repair/premium-flow-stabilization`.

`AGENTS.md`, `DESIGN.md`, all ten Android workflow files on that development
branch, the Maestro smoke flow, and the native UI verification scripts were
inspected before editing. The older main-branch automation was not imported
over the intended development source.

All existing application code/resources and artwork remain unchanged from
source tree `3d9e038cbf9677c95b832839849482ec05a6afdf`, with one additive repair:
the missing original `courtquest_navigation_atlas.png` was recovered byte for
byte from repository history. No Kotlin code, Gradle configuration, package ID,
version, signing configuration, save behavior, navigation implementation, or
design standard was changed.
No test was deleted and no lint suppression or failure bypass was added.
No release workflow was triggered and no APK or replacement web app was
published.

## Confirmed failures and repairs

| Evidence | Finding | Disposition |
| --- | --- | --- |
| [Compact verification 43319429873](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43319429873) | Exact `content-desc` equality on the tagged wrapper rejected Court's accessible label. `all(tabs)` also depended on XML Element child-count truthiness, not presence. The test assumed every lazy drawer destination was visible simultaneously, impossible on short landscape screens. | Validate the exact label within its route's semantic subtree, an enabled clickable target, visible geometry, all five destinations by scrolling the drawer, selected state and automatic closure. Keep minimum 48dp touch assertions at the explicitly configured density. Capture a hierarchy and screenshot on failure. |
| [First repair emulator run 43321302118](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43321302118) | The initial read did not yet expose the drawer; the failure capture showed it open. Its actual hierarchy confirmed labels on child nodes and the active Court tab exported as selected/focusable but non-clickable. | Wait for observable open/closed resources with bounded retries. Accept the platform's selected/focusable tab semantics only when enabled; still tap the current tab and assert automatic dismissal. Unselected non-clickable and disabled targets continue to fail. |
| Same run's real Android screenshot and repository history | The loader requests `courtquest_navigation_atlas.png`, deleted after its introduction. Later two tiny differently named `.png` files are not valid PNG images and cannot satisfy the loader. The caught load error silently left every navigation picture blank. | Restore the exact historical atlas, Git blob `84ec96618a850b2b33d623f0023e252974f86bfa`, SHA-256 `c7c28958c9d051b1b0ef0f6444672ad1b78e54b6383ea31086a9a61687158620`. Keep both pre-existing files untouched. Add an integrity test for the loader path, original hash, dimensions, PNG chunk CRCs and decompressed pixel stream. No artwork was regenerated or redesigned. |
| [Library/navigation 43316142939](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43316142939) | Library, Mission and navigation reports passed, then the daily script compared the installed version 10 with the obsolete workflow expectation 9. | Compare the installed candidate with the explicit version in the checked-out Android build source. Fail on absent or ambiguous version declarations; do not change the app version. Remove obsolete version overrides in the library, daily and game workflows. Frozen build-9 verification remains pinned. |
| Same run, later previously unreachable checks | Daily, game, library, Mission and keyboard helpers still had paths that tapped retired always-visible horizontal tabs. | Share explicit drawer navigation for current candidates. Preserve the old horizontal-tab selectors in the historical daily baseline path. Keep exact result, XP, saved-data, history and scoring assertions. |
| [Earlier toolkit 43312688287](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43312688287) | Maestro could not find `^Train$` while the drawer was collapsed. | Already repaired upstream. The owner baseline's [toolkit run 43319430422](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43319430422) passed. Preserve that smoke flow. |
| [Earlier development 43310629442](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43310629442) and [illustrated-navigation 43299699219](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43299699219) | `ProduceStateDoesNotAssignValue` lint failure and an invalid `drawImage` import. | Already repaired before the supplied baseline. Its [development run 43319429868](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43319429868) passed. Do not rewrite the working native artwork loader. |

The compact verification budget increased from 20 to 35 minutes to include the
five configurations, scroll reachability, selected-state and close-control
checks, plus the unchanged Maestro and APK checks.

## Repository-wide historical failures

The latest run of each registered workflow was also inspected. Some registered
workflows exist only on older branches and are not present on the intended
development branch:

- **Android APK, main:** [42261440034](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42261440034)
  is failed, but GitHub returns `log not found` and no failed-step detail.
  Its root cause cannot be established from retained evidence. Do not claim it
  was repaired or rerun it: the workflow publishes releases.
- **Headless AI proposal, main:** [42428442499](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42428442499)
  explicitly rejected all three model attempts because they returned unchanged
  source / whitespace only. This was the proposal validator enforcing its
  contract, not an Android application failure. Do not weaken that validator
  or restart an obsolete refinement campaign.
- **Premium visual baseline, main:** [42425915617](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/42425915617)
  encountered emulator-offline messages and then an XML parse error in its old
  capture script. That script/workflow is absent from the owner-specified
  development branch. The current native harness uses UI dump files with parse
  retries, and the current toolkit emulator journey passed.
- The frozen build-9 verification, authorized APK publication, daily/game
  historical verification, restored-UI checks and repository tooling checks
  most recently passed before this repair. Their immutable source/binary
  assertions and historical baselines were retained, not relaxed.

## Verification

Local commands and environment constraints are in [replit.md](../../replit.md).
The local suite covers visible selectors, persisted rollover readiness,
drawer labels/actionability/closure, lazy scrolling and source version
expectations plus artwork integrity (28 tests, all passed). Python compilation, shell syntax, JSON parsing, five Android XML
resource/manifest parses, and the existing smoke runner's early refusal guards
were also checked. No native device is attached and this container has no
Android SDK or accelerated emulator.

GitHub Actions is authoritative for Android unit tests, compilation, lint,
emulator screenshots/journeys, package/signature verification and APK alignment.
A successful old-source run is not evidence that the repaired verification
scripts passed; use the repair-branch runs and their exact commits.

The first repair commit `caac371` passed
[Android development run 43321302037](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43321302037):
72 Android unit tests across 15 suites, zero failures/errors/skips, successful
APK compilation, and lint with zero errors / 11 existing warnings. The artifact's
JUnit and lint XML were inspected independently. Its emulator checks are
[compact drawer](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43321302118)
and [library/training](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43321301963).
The compact run failed and supplied the real hierarchy/screenshot above; it is
not claimed as a pass. Subsequent changes scroll partially clipped drawer
controls into a minimum 48dp hit area before tapping, handle the observed
selected-tab semantics/readiness, and restore the original artwork. Their
exact-commit Android runs must finish separately. Superseded in-progress runs
on the intermediate commit were cancelled to avoid duplicate emulator work.

Physical-phone testing, full TalkBack traversal, and supported old-API rendering
remain separate from this narrowly scoped test-harness stabilization.
