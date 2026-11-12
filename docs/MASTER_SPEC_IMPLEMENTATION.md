# Master specification implementation tracker

The owner authorized implementation of the saved v1 master specification on November 11, 2026. The exact contract is [Master Product, UX & Engineering Specification v1](specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md), copied from the current saved document `libfile_23352bc73e84819196bde99e72d137f5`. The source specification remains an implementation proposal; this tracker records observed implementation status separately.

## Current issue: CQ-PREMIUM-001

The specification's recommended first engineering issue is the native premium training journey. It builds on `be0ced284dbc6af1b2ca32b5e0a691a152bf7fac`, which already implements the Signature header, shared smoked-glass components, navigation, deterministic resumption/next drill and saved-result receipt. Main remains separate.

| Phase | Scope | Current evidence |
| --- | --- | --- |
| 0 | Baseline and brand lock | Required source read; original artwork preserved; [preservation hashes](tooling/evidence/master-spec-preservation.json); real baseline/candidate Android captures from run 43104484828 |
| 1 | Canonical native premium journey | Implemented. 48 unit tests passed on the first candidate. Real daily/library/game journeys passed. Final Maestro, revised 200% capture review and exact-binary development delivery are in progress. |
| 2 | Player Mode | Next: versioned Mission model/builder, five player destinations, Quests, comparable evidence and earned milestones. Existing drill/session data must migrate without invented history. |
| 3 | Team/coach foundation | Planned: verified roles, team lifecycle, consent-aware membership, server authorization and scoped roster access. No local role toggle grants access. |
| 4 | Connected assignments | Planned: immutable Mission versions, explicit recipients, evidence-derived completion and offline reconciliation. |
| 5 | Pilot polish | Planned: approved notifications, cooperative quests, exports, security/privacy/accessibility review. |
| 6 | Controlled school pilot | Not launched. Requires the specification's consent, security, privacy and organization gates. |

The player and school paths share the future Mission, session and receipt contracts. No team records, coach privileges, accounts, sync states or achievements are fabricated to populate the interface.

## First-candidate verification and corrections

- Native source: `be0ced284dbc6af1b2ca32b5e0a691a152bf7fac`; app/src tree `6e9080d75b650dceac1ce47419468ee92a18b150`.
- Development check [43104484729](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43104484729) succeeded. Native run [43104484828](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43104484828) exercised all daily, custom-drill, extra-practice, game and Undo assertions successfully on APK SHA-256 `24fbea22eccf195f10fe33a6e7ac1d2843ab1cb3d737901fea655599ef1aa73a`. The overall run failed before Maestro because platform-tools was absent from PATH. That is a tooling failure, not a complete native acceptance claim.
- Toolkit run [43104484952](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43104484952) completed onboarding and reached Court, then failed because Maestro 2.12.1 restricts screenshots to its run output folder. Screenshot names now stay relative to that folder.
- The runner now resolves platform-tools through the declared Android SDK if adb is absent from PATH. Physical-device and wrong-AVD refusal still run before installation/reset; [guard checks](tooling/evidence/master-guard-verification.json) verify both.
- Reviewed actual normal Court, recording, receipt and Progress screens, plus 320 dp/130%, critical 200%, keyboard/editor and game captures. Signature lettering is clean at phone size. The 200% recording capture exposed a split attempt label and an unnecessarily tall footer. Paired metrics now reflow vertically, and the footer copy is shorter. Final captures must confirm the correction.
- Receipts explicitly label manual evidence as self-recorded and saved on this device. No connected sync or independent verification is claimed.
- Independent code review found no blocking production defect. Its receipt-title assertion gap was corrected before the first native run; that run checked the expected title on both the receipt and next drill.

## Remaining acceptance gates

Final current-source unit/build/lint, guarded Maestro execution, revised enlarged-text/clock/receipt captures, APK identity/signer/alignment, downloaded release verification and precise final checkpoint. Physical-phone performance, owner visual acceptance, full TalkBack traversal and API 26–30 fallback remain separate checks.

The larger roadmap does not redefine Phase 1 as a school-ready or production-ready application. Complete one scoped issue and its verification before advancing the tracker.


### Corrected-source checkpoint

`fe8634eb5cbd1793a10d39321c165781ced5e753` passes 48 unit tests and compilation after disambiguating the Kotlin reflow comparison. Its current app/src tree is `839a30e17949d0b7d9dbe9284999637e2117aa34`. Maestro then exposed a selector issue: merged native navigation exposes the visible tab label, while the old flow searched a child icon description. The failure hierarchy contains `Drills`; the flow now selects exact visible Court/Drills/Progress/Setup labels. Source `a4d5ccd89770e8861ff1c97acab9974c25191642` preserves that same app/src tree and reruns the complete native gate.

Current verification runs: [full native 43135148929](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43135148929), [toolkit smoke 43135148897](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43135148897), [development checks 43135148850](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43135148850). The app is not published from a failed overall run. Remaining evidence is recorded after these executions finish.

Next issue sequence: [Player Mode implementation order](plans/2026-11-11-player-mode-sequence.md). Actual first-candidate visual findings: [Android rendered review](tooling/SIGNATURE_RENDERED_REVIEW.md).
