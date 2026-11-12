# Master specification implementation tracker

## Current issue — CQ-PLAYER-002

The versioned solo Mission Builder has passed native acceptance on source `f6db205d3b8d7a10621acd3b39bc6fda0f576ecc`, app/src `826dc7c4f6eecbd0147b04e2cbc50a107ed4d089`. [Native run 43147187632](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43147187632), [development 43147187607](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43147187607) and [toolkit/Maestro 43147187655](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43147187655) succeeded. Results: 61 Android unit tests, 7 Python checks, lint 0 errors/11 warnings, all library/daily/game/Mission journeys and guarded Maestro. The exact 21,103,354-byte APK is version 1.0 (8), SHA-256 `7bee93ac1870911008db5973c9a374f0d4eb8db6e05ed917ba0df0db144ca04d`, with the original signer. [Native evidence](tooling/evidence/mission-build8-native.json), [rendered review](tooling/MISSION_BUILDER_RENDERED_REVIEW.md). Exact-binary publication is pending; build 7 remains public until that delivery gate finishes.

Implemented: separate durable builder, ordered selection/reorder/remove, saved immutable revisions, frozen target/vocabulary, seeded constrained generation with stored inputs/candidates, protected required steps, start/pause/resume/discard, exact-once drill results, one completion receipt and receipt reopening in Progress. New legacy daily/practice evidence is additive; old history, awards and session identities are preserved. [Current verification](tooling/MISSION_BUILDER_VERIFICATION.md). Do not move to CQ-PLAYER-003 until current native acceptance and higher-version APK delivery finish.


The owner authorized implementation of the saved v1 master specification on November 11, 2026. The exact contract is [Master Product, UX & Engineering Specification v1](specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md), copied from the current saved document `libfile_23352bc73e84819196bde99e72d137f5`. This tracker distinguishes implemented features from the proposal.

## CQ-PREMIUM-001 — native acceptance passed

The recommended first engineering issue now implements Court → start/resume → attempts/corrections → save once → receipt/actual next drill → Progress, using the shared Signature header, navigation and smoked-glass components. Receipts identify manual evidence as self-recorded and stored locally. The next-drill preview and actual Continue route share deterministic recovery logic. No connected sync, verified skill, coach privileges or invented player history are claimed.

Native source: `375a607fd7be33bf30ad56c2337cf9acf3ae2253`; app/src tree: `839a30e17949d0b7d9dbe9284999637e2117aa34`.

| Phase | Scope | Observed status |
| --- | --- | --- |
| 0 | Baseline and brand lock | Complete for this pass: original Signature contours/letterforms retained; real Android baseline; signer/package/save schema preserved. [Brand hashes](tooling/evidence/master-spec-preservation.json), [core preservation](tooling/evidence/master-core-preservation.json). |
| 1 | Canonical premium journey | Complete and published as development build 7; current-source native acceptance and independent public-download verification passed. |
| 2 | Player Mode | CQ-PLAYER-002 native acceptance passed; build-8 delivery pending. Five usable destinations, evidence-backed Quests, comparable Progress and resumable setup follow. [Scoped sequence and migration contract](plans/2026-11-11-player-mode-sequence.md). |
| 3 | Team/coach foundation | Planned: verified roles, consent-aware membership, server authorization and scoped roster access. |
| 4 | Connected assignments | Planned: immutable Mission versions, explicit recipients and evidence-derived completion/offline reconciliation. |
| 5 | Pilot polish | Planned: approved notifications, cooperative quests, exports and security/privacy/accessibility review. |
| 6 | Controlled school pilot | Not launched. Requires the master specification's organization and consent/security gates. |

## Completed verification

- [Native run 43136062719](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062719): SUCCESS; artifact `13908407333`. The exact APK passed library, daily and game regressions and guarded Maestro (1/1). 48 Android unit tests and 3 selector regressions passed; lint reports 0 errors/11 warnings.
- [Development checks 43136062758](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062758) and [toolkit/Maestro 43136062726](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062726): SUCCESS.
- APK version 1.0 (7), package `com.courtquestpro`, original certificate, v2 signature, ZIP integrity and 16 KB alignment passed independently after artifact download. APK SHA-256: `1e0c5b24bc6f1087733014a1acaaba6b72e04435a758f9631b8f3e51b7b58767` (20,988,666 bytes).
- Real Android captures reviewed at normal 360 dp, 320 dp/130%, critical 200%, keyboard/insets and reduced-motion settings. Recording/correction labels, clock controls, fixed result actions and keyboard-aware editing remain reachable. [Rendered review](tooling/SIGNATURE_RENDERED_REVIEW.md).
- Same-day/expired Activity restoration, process restart, custom revisions/favorites/archive, protected shuffle, extra practice, XP, repeated saves, dynamic names, Back, long history and game/Undo checks passed. [Machine-readable native ledger](tooling/evidence/signature-build7-native.json).
- Independent code review found no blocking production defect. Its receipt-title assertion gap was strengthened to check the named next drill on both receipt and destination.

## Corrections and retained history

Earlier failures remain recorded rather than treated as successful overall runs. The first native candidate passed the library/daily/game journeys but failed at the Maestro handoff because adb was absent from PATH. The toolkit's absolute screenshot path was incompatible with Maestro 2.12.1. Later runs exposed merged-label navigation selectors, offscreen zero-bounds semantics and a missing timer scroll in the test harness. The current runner uses SDK platform-tools, relative screenshot names, exact visible tabs and visible-area selectors, with regression tests. [Physical-device and wrong-AVD refusal checks](tooling/evidence/master-guard-verification.json) still pass before installation/reset.

Actual 200% captures exposed an overly tall save footer and split metric wording. The current layout reflows paired metrics and uses shorter guidance. Foreground controls remain outside the blur. Current enlarged captures show the scrolled recording controls and clock; they do not frame the entire metric panel at once.

The original handoff runs 43096106238/43096106289 remain queued at the latest check. Their pending status is superseded for acceptance by the successful current-source runs above; they are not reported as passed.

## Remaining limits and next issue

Physical-phone operation/performance, owner visual acceptance, full TalkBack traversal, API 26–30 fallback rendering and alternate launcher/theme masks remain unverified. Do not redefine Phase 1 as a school-ready release or claim the whole master specification is implemented. Finish build-8 exact-binary delivery, then proceed with CQ-PLAYER-003 under the saved Player Mode sequence.

## Exact-binary delivery

Release commit `a431a69254b9d896d2a76640f5af363530f0dfa6`; [publication run 43138905997](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43138905997): SUCCESS. The workflow verified source/app tree, tests, all native flow reports, Maestro and original signer before publishing the tested APK without rebuilding it. It downloaded and reverified the published file. An additional independent public download matched every byte and passed version/package/v2 signature/ZIP/16 KB alignment checks. [Public-download ledger](tooling/evidence/signature-build7-public-download.json).

The legacy release-branch restored-UI guard expected the previous orange literal and failed before compilation in run 43138905972. Its sentinel is updated to the approved `accentVivid` value `0xFFFF650E`; the current source passes that shell guard locally. This tooling-only change preserves the native-tested app/src and published APK. The follow-up [restored-UI run 43139207885](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43139207885) passed its source guard, compilation, lint, unit tests and package check on release commit `8cd8657cc6ea7e7b158d3b1cf916934d21780b88`. [Release-branch development run 43139206837](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43139206837) also passed. No APK rebuild/replacement was published for that tooling-only correction.

## Future release track — Google Play and sustainable tiers

**Roadmap decision (2026-11-11):** CourtQuest Pro is intended for eventual Google Play distribution and may offer free and paid tiers. These are future requirements, not claims of current compliance or approved monetization. Do not block the current Player Mode implementation on publishing or billing work, but avoid architecture that makes compliance or entitlements difficult.

### Google Play readiness (mandatory before store publication)

- Recheck the then-current Google Play Developer Program Policies, Developer Distribution Agreement, Android target API deadlines, SDK policies and Play Console requirements at submission time. Requirements can change.
- Prepare a Play-ready Android App Bundle (AAB), release signing/Play App Signing and upgrade continuity; retain existing `com.courtquestpro` application identity and signing history. Test internal/closed tracks before production.
- Provide accurate store listing, screenshots, content rating, app access instructions, privacy policy, Data safety declarations, permissions disclosures and account/data deletion workflows where applicable.
- Verify Google Play Families/children's policies and age/consent handling if targeting or admitting minors; obtain qualified privacy/legal review for school/student data, including applicable COPPA/FERPA and state requirements. Avoid behavioral advertising and inappropriate tracking of minors.
- Audit third-party SDKs, analytics, crash reporting, security, accessibility, content/IP licenses, device compatibility, performance, and Play pre-launch reports. Maintain a documented compliance checklist and submission evidence.
- Treat compliance as a recurring release gate, not a one-time certification; review changes to Play policies before each production update.

### Possible free / paid offering (decision deferred)

- Preserve a genuinely useful free player experience: onboarding, core solo training, drill recording, saved results and basic Progress. Do not paywall access to a school-required assignment or essential privacy/accessibility controls.
- Evaluate optional premium enhancements such as advanced analytics, expanded programs, sophisticated Mission Builder recommendations and convenience features; separately evaluate a school/team license with administrator-authorized purchasing.
- Keep account identity, locally saved training evidence, team permissions and entitlement state separate. A subscription expiration must not erase a player's training history, invalidate receipts or remove required school access.
- If selling digital features/subscriptions in a Play-distributed app, implement the applicable Google Play Billing requirements and current purchase/subscription disclosure, acknowledgment, restoration, cancellation and refund policies; check available exceptions rather than assuming them.
- Model entitlements server-verified where appropriate, with offline grace behavior, purchase restore, refund/revocation handling and tests against forged client-side premium flags. Do not add billing SDKs or purchase prompts until pricing, tiers, audience and policy route are approved.
- Future pricing, exact feature gates, subscription versus one-time purchase and school licensing remain **undecided**. Avoid advertising a paid tier as already available.

**Scheduling:** add a dedicated Play readiness and monetization architecture review before public beta; execute store submission verification and billing acceptance only after the native player and authorized team foundations are stable. School pilots require their own privacy/security gates regardless of Play publication.
