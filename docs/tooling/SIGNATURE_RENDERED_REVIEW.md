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

Final native source `375a607fd7be33bf30ad56c2337cf9acf3ae2253`, app/src `839a30e17949d0b7d9dbe9284999637e2117aa34`, [successful run 43136062719](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062719), artifact `13908407333`. Exact APK SHA-256 `1e0c5b24bc6f1087733014a1acaaba6b72e04435a758f9631b8f3e51b7b58767`.

Reviewed the final Court, drill, receipt, next drill, Progress and compact Court captures. Normal 360 dp presents the resume action in the first viewport, readable target/counts, 15/15 with actual +50 XP and the expected next drill. At 320 dp/130%, Signature lettering and navigation words remain whole and the primary action is reachable. At 200%, the recording/correction labels wrap at word boundaries, all save guidance and the disabled Save action remain readable, the clock/Start/Reset fit their scrolled panel, and both fixed receipt actions remain complete. The editor footer is above the open keyboard. The final enlarged game capture retains point controls and Undo with actual names/scores.

The enlarged recording capture is scrolled to the attempt controls; its upper metrics are outside that viewport. It does not visually prove every metric at once. Paired metrics now reflow in code, and persisted successful/attempted counts were asserted after actual taps. This distinction is retained rather than claiming an uncaptured full-panel review.

The current exact APK passes every native suite and guarded Maestro. Publication and independent public-download verification passed in run 43138905997 and the public-download ledger. Owner visual acceptance remains separate.

## Evidence limits

- Native accessibility semantics and control discovery are exercised; full TalkBack traversal is not established by screenshots.
- System animation scale zero is exercised, including a static save acknowledgement. Physical-device frame-time, battery and enabled-animation performance require further measurement.
- Haze uses its version-supported fallback; Android 26–30 rendering is not proven by the Android 35 run.
- Launcher foreground uses the approved contour render with a 0.65 safe-area scale. Alternate launcher/theme masks require additional device review.
- The early local software-emulator black frames were rejected. They are not included as evidence of the application.
