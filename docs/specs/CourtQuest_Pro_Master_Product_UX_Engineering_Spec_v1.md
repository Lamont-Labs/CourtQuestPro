# CourtQuest Pro — Master Product, UX & Engineering Specification v1.0

**Date:** 2026-11-11  
**Status:** Implementation-ready design proposal; not an implementation or verified release.  
**Repository:** `Lamont-Labs/CourtQuestPro`  
**Reference branch inspected:** `dev/premium-flow-toolkit`  
**Audience:** Individual basketball players, school coaches, school teams, and future clubs.  
**Product thesis:** A player can train independently and retain a personal development history; coaches can assign the same training to a team and see appropriately permissioned results.

## 0. Non-negotiable preservation contract

- Preserve the owner's selected **05 / SIGNATURE** CQ icon and CourtQuest Pro brush wordmark. The canonical fidelity reference is `CourtQuest Pro Signature Mark.png` and the design record identifies a prepared Signature brand pack. Never replace it with AI-generated or generic art, a font substitute, or a new logo.
- Preserve application ID `com.courtquestpro`, existing Android signing identity, package upgrade path, local data, saved sessions, scoring, Undo, custom drills, Drill Library, daily training and extra practice.
- Do not modify release or signing assets as part of product design. Back up and verify existing brand assets by hash before changing resource packaging. Avoid using rejected alpha drafts; the existing design record warns of extraction residue.
- Read `AGENTS.md`, `DESIGN.md`, `docs/tooling/PREMIUM_FLOW_TOOLKIT.md`, `docs/tooling/PREMIUM_FLOW_VERIFICATION.md`, and `docs/plans/2026-11-11-premium-flow-toolkit.md` at current HEAD before coding. Existing design record takes precedence over illustrative chat mockups.
- Never hardcode player names, skill scores, achievements, team counts or progress.
- Source truth and test truth must be separated from proposals and UI illustrations.

## 1. Verified reference baseline (from inspected branch documentation)

- Kotlin 2.0.21, Compose BOM 2024.12.01, AGP 8.7.3, minSdk 26, compile/target SDK 35; Java 17 Android target.
- Compatible pinned libraries: Haze 1.1.1, Lottie Compose 6.7.1, Compose Intro Showcase 2.0.2. Do not copy Haze 2.x-only APIs.
- Design colors: primary `#FF650E`, accent `#FF944F`, on-primary `#160C06`, background `#000000`, surface `#101113`, elevated `#202226`, border `#34363B`, outline `#92969E`, warm surface `#321A10`, text primary `#F8F5F0`, text secondary `#B8BBC3`.
- Functional typography: Barlow and Barlow Condensed, bundled under OFL. Signature lettering is branded art.
- Documented toolkit verification: 42 unit tests passed, build succeeded, lint 0 errors/6 warnings; Maestro dedicated-emulator CI was pending at time of inspected verification report. Recheck current status rather than claiming pass.
- UI reference sources: Compose Samples, Now in Android, Impeccable guides, UI/UX Pro Max, Product Design, Figma, Context7, GitHub. Native implementation stays Kotlin/Compose.

## 2. Product outcomes and guardrails

### Player jobs
1. Find a useful training session in 2–3 taps.
2. Understand each drill before starting.
3. Record attempts without fiddly controls.
4. Resume safely after interruption.
5. Get a credible receipt and next recommendation.
6. See real improvement over comparable training conditions.
7. Participate in teams without surrendering personal identity or history.

### Coach jobs
1. Create a team and set safe membership rules.
2. Assign an appropriate workout in under five minutes (usability target).
3. Track completion without nagging athletes individually.
4. Review individual results only where authorized.
5. Reuse and adapt assignments and practice plans.
6. Distinguish player self-report from coach-observed evidence.
7. Export permitted summaries and close a season without losing auditability.

### Non-goals for initial school pilot
- Public youth social network; adult-to-minor direct messaging; public minor leaderboards; AI grading from unvalidated video; recruiting rankings; payment-required participation; automated medical/injury decisions; attendance surveillance; automated school discipline.

## 3. Information architecture

### Player bottom navigation (5 destinations)
- **Court:** daily mission, resume, assignments, progress snapshot, recent receipt.
- **Train:** drill library, search/filter, custom drills, randomizer, programs, Mission Builder, extra practice.
- **Quests:** personal milestones, skill paths, cooperative team challenges, earned badges.
- **Progress:** history, comparable drill trends, personal bests, receipts, evidence types.
- **Me:** profile, goals, equipment, teams, settings, account and privacy.

### Coach bottom navigation (5 destinations)
- **Home:** team selector, assignment overview, actions, upcoming schedule.
- **Roster:** members, groups, invitations, authorized player summaries.
- **Assign:** templates, Mission Builder, scheduled/published assignments, review.
- **Insights:** participation, trends, assessments, exports.
- **More:** staff, team policies, privacy, support, switch to Player mode.

Role switcher appears in account area and optionally the top-level header when both roles exist. It must never silently grant privileges.

## 4. Onboarding flows

### Player
`Welcome → role selection → age/consent route → goals → experience/skill preference → equipment/environment → suggested first Mission → first receipt → optional account sync/team join`.
- Minimize required data; prefer age-band only when legally appropriate.
- A guest can complete local solo training if feasible; team membership and multi-device sync require an authenticated, authorized account.
- Explain why each permission or profile field is needed.
- Onboarding can be resumed and must not erase existing training records.

### Coach
`Welcome → role selection → authentication → coach verification/team eligibility → create or join team → age group/goals → starter Mission → review assignment → publish → controlled invite`.
- A claimed coach role is not proof of authorization to view minors' data.
- Separate coach role verification from team membership and organization authorization.
- Preview the assignment and recipients before publishing.
- No bulk invite until consent and school requirements are satisfied.

### Team join
`Enter code or open invite → show team identity and requested data sharing → consent/guardian/school checks where applicable → pending approval → active membership → assignment access`.
- Codes must be expiring, revocable, scoped, and rate-limited; never treated as sufficient authorization alone.
- Player remains owner of personal training records. School-linked data access is governed by documented policies.

## 5. Screen specifications

### P01 Player Court
**Above fold:** branded shared header, concise greeting, prominent `Start training` or `Resume session`, next Mission, assignment indicator.  
**Below fold:** skill progress, recent receipt, optional challenge, rest-day recommendation.  
**States:** guest/new, populated, active session, overdue assignment, offline, sync pending, error.  
**Rule:** start/resume visible in first viewport at 360dp default font size; at large font sizes reachable with ordinary scrolling or stable action footer.

### P02 Train
Search with recent searches, skill chips, duration, equipment, location and difficulty filters; curated and custom drill tabs; one-tap randomizer with constraints; drill preview; `Build Mission` and `Quick practice` actions. Empty custom-drill state offers `Create drill`.

### P03 Drill detail
Title, purpose, skill, equipment, difficulty, instructions, safety/space note, measurement type, examples/demonstration where licensed, recent comparable personal result, `Start` action. All instructions available offline for downloaded drills.

### P04 Active training
Large score/attempt controls; single focused drill at a time; pause, Undo, rest timer, progress through Mission; accessible haptics; screen-awake policy only during active recording; clear exit/resume behavior. No glass blur behind critical score controls.

### P05 Drill receipt
Exact drill, completed attempts, measurement, date/time, origin, source class, save/sync state, personal-best indicator only if comparable. `Next drill` primary, `Review` secondary. Saving is idempotent.

### P06 Mission completion
Summary of drills, total duration, results, milestones, coach assignment status, next recommendation. Celebration animation <= 2 seconds, reduced-motion alternative, dismissible; no invented scores.

### P07 Quests
Skill paths, optional personal challenges, cooperative team goals, completed badges with criteria and supporting records. Avoid coercive streak-loss design.

### P08 Progress
Date ranges, filters by skill/drill/team, training volume, attempts, success rates with denominators, personal bests, evidence class, session receipt drill-down. Comparisons only across compatible definitions.

### P09 Me
Profile, teams, goals, equipment, notification controls, accessibility, export/delete requests, account state and role switching.

### C01 Coach Home
Current team selector, draft/published assignments, completion totals with denominators, items requiring review, quick `Create assignment`, team schedule. Avoid displaying unverified skill ratings as fact.

### C02 Roster
Members, pending invites, roles, groups, permissioned player details. Membership statuses: invited, pending consent, active, suspended, left, removed. No sensitive data in invitation previews.

### C03 Assignment Builder
Pick Mission template or create; add/reorder drills; set duration, reps/attempts, equipment, difficulty, accommodations, deadline, recipients/groups; review exact target roster; publish, save draft, duplicate, archive. Edits after publication create versioned revisions, not silent history rewrites.

### C04 Assignment Detail
Published version, recipients, completion breakdown, self-recorded/coach-observed status, late/missing with neutral wording, notes and review history. Coaches cannot edit historical player attempts.

### C05 Insights
Team participation, assignments completed, aggregate drill trends, baseline vs follow-up where valid, filters and privacy-safe exports. Small groups must not be exposed through easily identifying aggregate slices.

### C06 Team Settings
Staff roles, invite policy, age group, retention and consent policy, team archive, approved exports, access audit, support.

## 6. Mission model and training algorithm

A **Mission** is a versioned ordered collection of drill references with measurement rules, time estimate, equipment requirements, and source (`PERSONAL`, `RECOMMENDED`, `COACH_ASSIGNED`, `CHALLENGE`). It can be instantiated as a `MissionSession`. An `Assignment` references a published immutable Mission version and an explicit recipient set.

Mission Builder inputs: skill, time budget (5/15/30 minutes and custom), equipment, available space, experience, physical constraints supplied voluntarily, preferred intensity, assignment restrictions.

Deterministic selection pipeline:
1. Exclude drills that violate equipment, location, accessibility, or age-appropriate constraints.
2. Match chosen skill and objective; preserve coach-required drills for assignments.
3. Balance warmup, primary practice, and cooldown as appropriate.
4. Fit total estimated time; allow honest overflow warnings.
5. Avoid recent identical repetition unless deliberate practice is selected.
6. Use player history only when sufficient comparable evidence exists.
7. Explain recommendation in plain language (`Matches your 15-minute window and ball-only setup`).
8. Log algorithm version and input snapshot for reproducibility.

Randomizer: seed stored with generated Mission, hard constraints enforced, no duplicates unless explicitly allowed, reroll preserves user selections. Never randomize away mandatory coach requirements.

## 7. Data model (conceptual, migration subject to source inspection)

- `Account(account_id, auth_provider, created_at, status)`
- `PlayerProfile(player_id, account_id, display_name, age_band, preferences, created_at)`
- `CoachProfile(coach_id, account_id, verification_status, created_at)`
- `Organization(org_id, type, display_name, policy_version)`
- `Team(team_id, org_id?, season_id?, age_group, status)`
- `TeamMembership(membership_id, team_id, player_id, role, status, effective_from, effective_to)`
- `CoachTeamGrant(grant_id, coach_id, team_id, role, scope, status)`
- `ConsentRecord(consent_id, subject_id, guardian_or_authority_id?, scope, version, effective_at, revoked_at)`
- `Drill(drill_id, owner_scope, title, measurement_schema_id, equipment, version, status)`
- `Mission(mission_id, owner_scope, source, status, created_at)`
- `MissionVersion(mission_version_id, mission_id, version, immutable_definition, created_at)`
- `Assignment(assignment_id, team_id, mission_version_id, published_by, due_at, status, revision)`
- `AssignmentRecipient(assignment_id, player_id, eligibility_status)`
- `MissionSession(session_id, player_id, mission_version_id, assignment_id?, state, started_at, completed_at?)`
- `DrillAttempt(attempt_id, session_id, drill_id, measurement_schema_id, values, occurred_at, evidence_class, client_event_id)`
- `SessionReceipt(receipt_id, session_id, result_hash, saved_at, sync_status)`
- `AchievementAward(award_id, player_id, rule_id, rule_version, supporting_receipt_ids, awarded_at)`
- `AuditEvent(event_id, actor_id, action, subject_type, subject_id, timestamp, correlation_id, metadata)`
- `SyncOutbox(event_id, device_id, idempotency_key, payload_hash, state, retry_count)`

Use opaque IDs, immutable receipt references, server-generated authority decisions, and client-generated idempotency keys. Define retention per data category and organization. No invented school ID or external school directory dependency.

## 8. Training session state machine

`NOT_STARTED → ACTIVE ↔ PAUSED → COMPLETING → SAVED_LOCAL → SYNC_PENDING → SYNCED`.

Exceptional states: `INTERRUPTED`, `SYNC_FAILED_RETRYABLE`, `CONFLICT_REQUIRES_REVIEW`, `ABANDONED`, `RECOVERY_REQUIRED`.

Invariants:
- A session ID has at most one canonical completion receipt.
- `Save` is idempotent across repeated taps and network retries.
- Undo creates an auditable correction or reverses an unsaved local input; it does not silently rewrite a synced receipt.
- A paused/interrupted session can resume without duplicate attempts.
- Assignment completion is derived from valid saved session evidence, not button presses.
- Historical measurement definitions remain attached to results after drill edits.
- App process death and device restart preserve recoverable state.

## 9. Access-control matrix

| Action | Player | Coach (authorized team) | Team admin | Guardian (when authorized) |
|---|---|---|---|---|
| Record own training | Yes | Only own player profile | Only own player profile | No by default |
| View own history | Yes | Own only | Own only | Approved dependent scope |
| View assigned completion | Own | Team members in scope | Team members in scope | Approved dependent scope |
| View private unshared workouts | Yes | No | No | Only consented/legal scope |
| Create team assignments | No | Yes | Yes | No |
| Manage coach grants | No | No unless delegated | Yes | No |
| Change past player attempts | No silent edits | No | No | No |
| Export team data | No | Scoped approval | Scoped approval | No |

All permissions enforced server-side. Team membership alone does not authorize viewing every personal training detail. Revoke access promptly when membership or grant ends. School and guardian rights need jurisdiction- and contract-specific legal review.

## 10. Offline/synchronization

- Local encrypted storage for sensitive account and student data as appropriate to threat model; never assume device encryption alone meets every requirement.
- Durable local event outbox with stable UUID/idempotency key and payload hash.
- Retry with bounded exponential backoff; avoid duplicate writes.
- Server verifies authentication, authorization, event schema, session lineage, and idempotency.
- Use append-only corrections for synced records; no last-write-wins for evidence or authority grants.
- UI status: `Saved on this device`, `Waiting to sync`, `Synced`, `Needs attention`.
- Revoked team membership must stop further team data access; offline cache expiration and deletion policies required.
- Offline coach publication must be queued as draft, not represented as published until server authorization confirms.

## 11. Gamification and measurement

- XP for legitimate session participation and milestone completion, capped against farming; XP must never masquerade as skill proficiency.
- Skill paths: Shooter, Playmaker, Finisher, Defender, Footwork. Names and badge art require final brand review.
- Personal-best comparisons require same drill version/measurement rules and compatible attempt counts; display denominators.
- Streaks are optional and non-punitive; rest days and injury-recovery accommodations must not be penalized.
- Team quests are cooperative by default; no public youth rankings at launch.
- Evidence classes: `SELF_RECORDED`, `COACH_OBSERVED`, `INDEPENDENTLY_VERIFIED`. Only issue `INDEPENDENTLY_VERIFIED` when an actual verification protocol exists.
- Every achievement stores rule version and supporting receipts; revoke or mark invalid if source evidence is invalidated.

## 12. Blackout visual design system

- Use canonical repository tokens rather than chat-mockup approximations.
- Deep true-black base, polished smoked-black glass, orange primary actions, crisp ivory text, refined highlights.
- Haze 1.1.1 can support compatible backdrop effects; verify actual device performance and contrast. Do not use Haze 2.x API examples.
- Signature artwork preserved; use prepared brand assets only after fidelity and alpha/background-edge review.
- Barlow Condensed ExtraBold for high-impact display; Barlow for controls/body. Avoid overuse of uppercase in instructional copy.
- Spacing: existing 4/8/12/16/24/32/40 dp scale; touch targets >=48dp where possible; avoid tiny score controls.
- Glass reserved for hero, header, navigation and overlays; active drill controls use stable high-contrast surfaces.
- Animation: brief tactile transitions; success celebration <=2s; support reduced motion, battery/performance limits.
- Accessibility: TalkBack semantics, font scaling, focus order, high contrast, error text beyond color, screen reader announcements for saved state, large tap targets.
- Responsive: 360dp baseline, tall/short phones, system bars, IME, tablets as enhancement; landscape interruption must preserve state.

## 13. Notifications and communication

- Local reminders opt-in; coach assignments can notify through approved, consent-aware channels.
- No direct adult-to-minor chat at launch.
- Quiet hours and school policies override engagement prompts.
- No public disclosure of minor profile, location, training schedule, or team membership.
- No ad targeting of minors or sale of training data.

## 14. School readiness and compliance workstream

- Determine applicable COPPA, FERPA, state student privacy, school contracting, accessibility, and retention obligations with qualified counsel before real student pilot.
- Document data controller/processor roles, school-approved purposes, data inventory, subprocessors, incident response, export/deletion, consent withdrawal, retention and security review.
- No assumption that school consent automatically covers unrelated commercial processing.
- Verify coach identity and organization authority before student data access.
- Provide guardian/school workflows where legally required; do not collect more minor information than necessary.
- Perform threat modeling, penetration testing, privacy review, and accessibility review before real-school launch.

## 15. Instrumentation and adoption metrics

Track privacy-minimized product events with opt-in/consent as required:
- `onboarding_started`, `onboarding_completed`, `first_mission_started`, `first_mission_saved`, `mission_resumed`, `mission_abandoned`
- `assignment_draft_created`, `assignment_published`, `invite_accepted`, `assignment_completed`, `coach_report_viewed`
- `sync_failed`, `sync_recovered`, `receipt_opened`, `progress_viewed`

Funnel metrics: first-session completion, weekly active players, week-four retention, sessions/player/week, assignment completion, coach repeat assignment, time-to-first-assignment, successful sync, crash-free sessions. Report cohorts separately for independent and team-joined players. Avoid collecting sensitive unnecessary identifiers.

Pilot proposal: 3 teams, 30–45 players, six weeks, subject to consent/approvals. Planning targets (not benchmarks): 80% onboarding, 70% first-session completion, 50% week-four active retention, 75% coaches publishing a second assignment, 60% reporting time saved. Conduct qualitative player/coach interviews.

## 16. Release plan and gates

### Phase 0 — baseline and brand lock
- Inspect current HEAD, asset inventory, source state, CI, release and signing lineage; create preservation manifest with hashes.
- Recheck pending Maestro device run and any newer flow changes.
- Capture real Android baseline screens and document UX gaps.

### Phase 1 — native premium journey
- Implement shared header and navigation; Court → start/resume → record attempts → idempotent save → receipt/next drill → Progress.
- Tests: unit, state machine, process death, Undo, offline, screenshot, emulator Maestro.

### Phase 2 — Player Mode
- Onboarding, Mission Builder, Quests, Progress comparisons, achievements, accessibility.
- Verify no fake player data in production.

### Phase 3 — team/coach foundation
- Role model, team lifecycle, invitations, verification/consent, roster, coach dashboard.
- Server authorization and security tests required before real data.

### Phase 4 — connected assignments
- Publish versioned Mission, recipient mapping, player completion, coach reporting, offline reconciliation.

### Phase 5 — pilot polish
- Notifications, cooperative quests, exports, visual refinement, security/accessibility and student privacy reviews.

### Phase 6 — controlled school pilot
- Recruit approved schools/teams, collect consent, test with pilot accounts, measure outcomes and iterate.

Each phase: checkpoint → implement minimal vertical slice → unit/integration tests → device smoke → actual screenshot review → preserve signer/package ID → signed development APK where authorized → owner review. No production release on incomplete device/security/privacy gates.

## 17. Test matrix and acceptance criteria

**Critical journeys:**
1. Fresh player installs, completes onboarding, trains, saves once, relaunches, finds receipt.
2. Existing build-6 user updates APK; all saved history/custom drills remain intact.
3. Interrupted session resumes with no duplicate attempts.
4. Repeated Save and delayed sync produce exactly one canonical receipt.
5. Offline session persists and syncs once after reconnect.
6. Coach creates team, invites authorized player, publishes Mission, player completes, coach sees authorized completion.
7. Revoked coach cannot view former team members' records even through stale deep links.
8. Player leaves team, personal history remains; school data visibility follows policy.
9. Coach cannot edit player attempt history; correction is traceable.
10. Signature icon/wordmark match reference and launcher masks at common densities.
11. 360dp viewport, enlarged text, TalkBack and reduced-motion flows remain usable.
12. Existing game scorekeeping and Undo regressions pass.

**Definition of done per feature:**
- User-visible primary and exceptional states implemented; no dead ends.
- Data provenance and permissions defined and tested.
- Unit/integration tests green, native compile and lint reviewed.
- Maestro or equivalent dedicated-emulator flow passed where applicable.
- Real screenshots reviewed against `DESIGN.md` and Signature brand reference.
- Existing application ID, signing continuity, local history, drill library and scoring preserved.
- Git checkpoint and evidence ledger recorded; unresolved gaps explicitly marked.

## 18. Recommended first engineering issue

**Title:** `CQ-PREMIUM-001 — Canonical Player Training Journey + Signature Preservation`

**Scope:** Inspect current source and latest CI; lock hashes for Signature brand assets; complete Court → start/resume → attempt recording → idempotent save → receipt → next drill → Progress with shared navigation/header; verify current custom drills, daily training, extra practice, game scoring and Undo; collect actual emulator screenshots and sign-off evidence. No school backend or unrelated refactor in this issue.

**Acceptance:** Existing users can update without data loss; signature and package ID unchanged; no duplicate save; session recoverable after process death; progress matches receipt; high-contrast controls at 360dp; no placeholder player values; unit/lint/build/device checks documented; brand not replaced.

## 19. Handoff instructions

Start from the current HEAD of `dev/premium-flow-toolkit` or its explicitly designated successor. Read the repository instructions and latest implementation state before touching code. Treat this specification as a product contract and proposed implementation roadmap, not proof that its features exist. Do one scoped task at a time, save a checkpoint, preserve all approved branding and signing, and report exact evidence and blockers.
