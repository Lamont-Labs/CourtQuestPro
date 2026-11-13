# COURTQUEST PRO — PERFORMANCE-FIRST UX RESTRUCTURE v1
Status: APPROVED DIRECTION / SPECIFICATION ONLY
Date: 2026-11-13
Repository: Lamont-Labs/CourtQuestPro
Implementation baseline: repair/premium-flow-stabilization
Scope: Navigation and information architecture, not a rebuild.

## Product identity
CourtQuest Pro is a serious basketball development tool that makes improvement enjoyable. Individual players and school-team players are equal first-class users. Preserve the existing deep black, vivid orange, glass accents, approved illustrated logo/icons, functional training flows, and saved player data. Reduce card stacking, scrolling, ornamental copy, and duplicated actions. Make primary daily action accessible without scrolling on a typical phone.

## Nonnegotiable navigation
Five approved side-drawer illustrated icons, unchanged in art and order: Court, Train, Quests, Progress, Me. Retain compact floating cards, transparent backing, orange edge handle, and overlay behavior: opening the drawer MUST NOT resize, shift, or reflow screen content. Close on destination selection, back, or outside tap. Keep jersey artwork black with orange/white trim and player-selectable number 0–99; NO jersey-color controls or overlays. Do not add a competing large bottom bar. Use internal tabs only for subordinate sections.

## Screen map
### Court — daily command center
Above fold: concise page title/date; one primary session card with session name, completion count, duration if known, and Start/Resume CTA; compact three-metric row (only metrics supported by existing data); one row of shortcuts to Drills, Plan, Quests, Stats. Below fold: next recommended action and recent activity, maximum two short rows. Empty state has a clear first-session action. Never fabricate accuracy or readiness.
### Train — workbench
Compact segmented selector Today | Library | Custom. Today shows ordered session drill rows with completion and one Start/Resume action. Library has search/filter, drill list and detail-on-demand. Custom exposes create/edit saved drills without navigating through the entire library. Drill recording retains existing receipt/save-once behavior; no silent duplicate saves. Advanced instruction expands on request, not on initial page.
### Quests — motivation
Tabs Active | Completed. First view shows up to three actionable quests and concise progress; rewards/XP remain secondary to training. Preserve current quest rules and awards.
### Progress — evidence
Tabs Overview | Skills | History. Overview shows truthful tracked metrics and time windows; Skills shows per-skill trends only if data supports them; History lists sessions, games, and receipts with detail on demand. Distinguish attempted/completed and practice/game. Do not synthesize performance statistics from unrelated values.
### Me — player and team identity
Above fold: player name, black jersey icon with selected number, team/individual context if available, and compact profile summary. Sections Profile | Settings | Team (Team only when implemented/available). Settings keep name, jersey number, difficulty and current preferences. Do not hardcode Vincent or any player name.

## Shared UI rules
- One dominant CTA per screen; secondary actions smaller and clearly labeled.
- Dense but touch-safe: target at least 48dp interactive surfaces.
- Fixed page header and local tab selector where appropriate; avoid nested scroll conflicts.
- Avoid horizontal carousel as sole access path; no hiding core tools.
- Primary action and status should fit in the initial viewport on common Android phones; small screens can scroll lower-priority details.
- Use concise, athletic language: Start session, Resume, Record attempts, View results, Next drill. No cartoonish hype.
- Dark surfaces layered subtly, bright orange reserved for active/primary/important states, crisp typography and high contrast.
- Accessibility: readable labels, selected states, content descriptions, font scaling, TalkBack, sufficient contrast, back navigation.
- Use existing Compose architecture and tokens. No dependency/plugin additions without demonstrated need.

## Implementation sequence (one isolated verified task at a time)
0. Verify latest signed rollback APK release, version/signing/CI. Do not modify production assets.
1. Inventory current routes, existing components and data bindings. Capture current Court layout and scroll burden; record baselines and tests.
2. Court first: compact above-fold command center. Reuse real session state and actions. Preserve navigation overlay. Compile and device-verify.
3. Train: Today/Library/Custom tabs, drill workflow and save-once receipt regression tests.
4. Progress: Overview/Skills/History tabs; evidence-backed metrics and receipt history.
5. Quests: Active/Completed; retain reward logic.
6. Me: compact player identity and settings; black jersey only.
7. Cross-screen typography, spacing, performance and accessibility polish.
8. Owner phone visual acceptance before new signed release; preserve package ID and signing identity.

## Acceptance gates
- All five approved icons visibly unchanged; side drawer floats above rather than shifts content.
- No jersey color selector or colored patch anywhere.
- First screen presents Start/Resume without scroll and with real session state.
- Existing custom drills, daily training, game scorekeeper, quests, onboarding, and data persistence unaffected.
- No hardcoded player identity.
- Navigation/back behavior tested for each top-level route and local tab.
- Android compile, unit tests, lint, device smoke and signing continuity green before APK release.
- Avoid broad simultaneous refactors; preserve known-good checkpoints and record each commit.
