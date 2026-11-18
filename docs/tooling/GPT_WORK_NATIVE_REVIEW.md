# CourtQuest Pro native quality review

## Baseline and verification

The rollback point is the published [v47 release](https://github.com/Lamont-Labs/CourtQuestPro/releases/tag/courtquest-phone-update-v47)
at `cdeb75dbd064a6689173e52b8a969cf992824b60`. The downloaded APK
verified as `com.courtquestpro` versionCode 47 with the existing certificate
`25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`.
The fresh walkthrough candidate is `921373c70b937e7d35186282caf146735944ae9a`.
The walkthrough verification workflow builds the test APK, checks the
real release-to-candidate upgrade on a disposable AVD, and retains screenshots.
[Native walkthrough workflow](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44370335831)

| Capability | Applied work |
| --- | --- |
| GitHub | Recorded the development commit, kept the immutable v47 release as the rollback point, and ran both Android development and dedicated walkthrough checks. |
| Figma | Authored an editable five-stop walkthrough and an enlarged-text layout. [Walkthrough layout draft](https://www.figma.com/design/JKwMaV5jiJp7qSEmbvZSKa) |
| Context7 and Android Compose docs | Checked the Scaffold content padding contract and how to apply and consume it for each sibling. |
| Product Design review method and UI/UX Pro Max | Reviewed the tour sequence, exit controls, touch targets, and the difference between design drawings and Android evidence. |
| Android SDK, Compose UI testing, Python and ADB | Built the production application and test APK. The dedicated test measures dock clearance, taps all five destinations, tests replay and verifies persisted state. |
| Open-source references | Reviewed Compose Samples, Now in Android, Compose Intro Showcase, Haze and Lottie. Native Compose already provides the required state, clipping, animation, and hit geometry. The existing toolkit documents the pinned versions. |

The discovery search confirmed installed GitHub, Figma, Product Design,
Context7, Replit and Superpowers, along with image and video generation
capabilities. GitHub writes, Figma edits and Context7 queries were exercised.
The native milestones use the app's approved assets. The available Firebase,
Canva and MagicPath connections are optional for this work. The Android and
design workflows for these passes use existing connections.

## Screen-by-screen source review

| Surface | What the current app does | Next native acceptance check |
| --- | --- | --- |
| Welcome | Signature wordmark and separate first-launch and returning-player entry. Content sits in a fixed viewport with weighted spacers. | Show both initial actions at 320 dp and 200% text, including short landscape. |
| Court | Arena header, next session panel, drill shortcuts, workout and schedule. This branch aligns the preview with the training route and uses the selected weekly schedule. | Capture fresh, saved-draft, active Mission and completed day states. Verify the first action and the weekly goal with changed training days. |
| Dock | One 58 dp row with approved artwork, a customizable jersey and accessible selected tabs. | Check the five tap bounds, target names and selection at compact and enlarged text. Verify the guide clears the bar and system navigation. |
| Drill Library and discovery | Today, Library and Custom segments with search, filters, categories, favorites and quick practice. | Check search-to-result distance, filter reset, and minimum action size on 320 dp devices. |
| Drill art and coaching | Bundled plates, diagrams and written guides, plus a zoomable reference viewer. | Compare signed APK screenshots for plates and text. Exercise zoom, collapse and return to the drill. |
| Daily training | Recorder separates attempt actions, corrections, target confirmation and timing. | Check the first viewport, both attempt actions, Undo, pause and safe resume. |
| Custom drills | A scrollable editor sits above a keyboard-aware save action. | Save, reopen and edit a named drill with keyboard and 200% text. Verify old revisions remain usable. |
| Results and receipts | One saved result with counts, success rate, target evidence and follow-up actions. | Save once, relaunch from the same saved data, verify the next drill matches Court, and reopen from Progress. |
| Quests and rewards | Active, Completed and Achievements sections use stored training evidence. A branded hero uses a fixed height. | Check hero and tab copy at 200% text and repeat an eligible completion to confirm one award. |
| Progress | Time range filters with lifetime XP, activity, comparable bests, history and receipts. | Compare empty and returning profiles, verify chart labels against the selected period, and review the first screen on a short phone. |
| Profile and jersey | Approved jersey artwork with the saved number, profile settings and achievements. | Test two-digit and blank number states, long names, editing, relaunch and back navigation. |
| Teams and schools | Solo navigation has no active team or school destination. The dormant team view and data remain in source. | Review a future team prototype against the role and consent requirements in the master specification before enabling entry. |
| Settings | Me settings hub links to player preferences, help, replay and notices. Editing responds to the keyboard. | Check the full Me-to-settings loop, keyboard Save reachability, Help and repeatable guide entry. |

## Court dashboard work in progress

The Court preview uses the same deterministic next-slot selection as training
and the result receipt. The Resume label requires a validated saved draft.
A Mission shows its own actual drill position. The weekly metric uses selected
training days, and the two shortcut actions stack at enlarged text.
The initial Court build, unit tests and lint passed. Device review of these
dashboard changes is pending.
