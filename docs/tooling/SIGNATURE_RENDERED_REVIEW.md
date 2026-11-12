# Signature journey: Android rendered review

Review method: inspect actual Android captures before accepting a visual claim; apply the repository's Impeccable hierarchy, clarity, interaction and accessibility checks. Captures contain synthetic QA profiles. They are not design mockups.

## Reviewed first candidate

Source `be0ced284dbc6af1b2ca32b5e0a691a152bf7fac`, APK `24fbea22eccf195f10fe33a6e7ac1d2843ab1cb3d737901fea655599ef1aa73a`, [Android evidence run 43104484828](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43104484828), artifact 13895011426. Daily/library/game checks passed; the complete run failed before the complementary Maestro command. That failure is retained in the implementation tracker.

| Step and capture | Observed result | Decision |
| --- | --- | --- |
| Court: `baseline-court` → `candidate-court` | Signature letterforms and CQ replace the generic header; black canvas and smoked surfaces replace the dominant warm-brown panel; resume is visible in the first normal viewport. | Retain shared identity, priority and action placement. |
| Active drill: `candidate-drill`, `candidate-drill-target-met` | Target, actual counts, successful/missed inputs and save eligibility are distinct. Save remains fixed; recording text is sharp. No backdrop blur is applied to attempt counters or their action panel. | Retain. Check enlarged recording labels separately. |
| Receipt: `candidate-result`, `candidate-receipt-next` | Saved 15/15, actual +50 XP, workout count, named next drill and fixed Continue/Progress actions agree with persisted data. | Retain. Add explicit self-recorded/local-save provenance under the master specification. |
| Progress: `candidate-progress-after-save`, `candidate-progress-record` | The dated 15/15 record appears beneath an activity summary; XP is separated from measured attempts. | Retain. Comparable Mission evidence is a Phase 2 extension. |
| Compact Court: `candidate-compact-court` | 320 dp at 130% keeps the complete mark, whole navigation words and reachable Start action. | Retain. |
| Enlarged recording: `candidate-large-type-drill` | At 320 dp/200%, the attempt label splits inside a word; verbose save guidance consumes excessive height. Recording inputs remain scrollable, but the typography is not accepted. | Fix paired metrics to reflow vertically and shorten footer copy; recapture. |
| Editor/keyboard: `candidate-library-large-type-keyboard` | At 200%, the field retains its entered name and the Save drill footer is above the keyboard. | Retain original editor/insets behavior. |
| Game/Undo: `candidate-large-type-scorekeeper`, game receipt/history captures | Point controls and Undo are reachable; dynamic players, final score and actual XP are preserved. | Retain existing game implementation. |

The first candidate's normal flow is visually coherent. The enlarged-label defect was fixed rather than counted as accepted. Owner visual acceptance remains separate from this engineering review.

## Corrected candidate acceptance

Current candidate adds the receipt evidence label, vertically reflowing paired metrics, shorter confirmation/save guidance and explicit enlarged receipt/clock captures. Final current-source Android results and exact-binary publication are recorded in the implementation tracker and release evidence after they finish.

## Evidence limits

- Native accessibility semantics and control discovery are exercised; full TalkBack traversal is not established by screenshots.
- System animation scale zero is exercised, including a static save acknowledgement. Physical-device frame-time, battery and enabled-animation performance require further measurement.
- Haze uses its version-supported fallback; Android 26–30 rendering is not proven by the Android 35 run.
- Launcher foreground uses the approved contour render with a 0.65 safe-area scale. Alternate launcher/theme masks require additional device review.
- The early local software-emulator black frames were rejected. They are not included as evidence of the application.
