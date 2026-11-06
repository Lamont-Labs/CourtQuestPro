# CourtQuest Pro — Android device acceptance checklist

Status: PENDING PHYSICAL DEVICE TEST. GitHub Actions compiles the APK but does not launch the application on a device.

## Installation
1. Download the debug APK from the latest successful `Android development checks` artifact.
2. Install on an Android device running Android 8.0 (API 26) or newer.
3. Launch the app and verify Home, Career, and Settings open without crashes.

## Championship scoring
1. Open championship setup and enter two different names.
2. Start a match. Confirm the score begins 0–0.
3. Score Player, Opponent, Player. Expect 2–1.
4. Tap Undo once. Expect 1–1, not 2–0.
5. Tap Undo again. Expect 1–0.
6. Tap Undo again. Expect 0–0. Repeated Undo must not produce negative scores.
7. Score to 10–10; score the next point for either side. Expect the winner's score to be 11 and the recap to open.
8. Confirm the result appears once in Career match history.

## Persistence and compatibility
1. Start a match, enter alternating points, return Home, and navigate back to the scorekeeper if supported by the UI.
2. Force-stop and relaunch the app. Check that scores and match data remain intact in saved state.
3. With a legacy save from before pointHistory existed, verify prior scores remain unchanged; Undo must not guess an older scorer.
4. After scoring a new point on a legacy match, Undo should reverse only that new point.

## Exit criteria
- Record device model, Android version, APK run ID, date, and results for each scenario.
- Mark PASS only after physical-device execution; unit tests and APK compilation alone are insufficient.
- Do not merge the draft PR or publish a release until acceptance is reviewed.

## Known behavior to review separately
- A winning point immediately transitions to recap; Undo is not offered from recap.
- Current UI may not offer an explicit resume-match entry point after navigating Home or relaunching. If so, mark the resume scenario BLOCKED and file a separate usability task rather than claiming it passed.
