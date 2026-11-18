# CourtQuest Pro — v42 Solo Player Redesign Specification
Date: 2026-11-17
Status: FIRST COURT/TRAIN/NAVIGATION/WALKTHROUGH BATCH AUTHORIZED FOR IMPLEMENTATION; NO APK RELEASE AUTHORIZED
Repository: Lamont-Labs/CourtQuestPro
Baseline: released v41 (versionCode 41), with separately verified but unreleased walkthrough compactness correction fb63df89.
Source basis: AGENTS.md; DESIGN.md; docs/specs/CourtQuest_Pro_Master_Product_UX_Engineering_Spec_v1.md; v41 Kotlin/Compose source review.
Owner scope change (2026-11-17): school and coach functions are OUT COMPLETELY for now.

## 1. Product definition
CourtQuest Pro is a premium, offline-first, individual basketball training app. The main loop is OPEN -> CHOOSE -> PRACTICE -> SAVE -> IMPROVE. Prioritize player utility over decoration and data-backed progression over invented performance ratings.

## 2. Hard preservation gates
- Preserve com.courtquestpro, permanent signing certificate, install-in-place update path, existing local data, and versionCode monotonicity.
- Preserve approved 05/SIGNATURE logo, wordmark, jersey identity, black/orange tokens, bundled 18 drill-specific illustrations, and player-customizable identity.
- Preserve scoring, Undo, custom drills, daily training, extra practice, Mission Builder, saved Mission receipts, and unfinished-session recovery.
- No fake achievements, scores, player identities, school membership, camera-grade skill claims, or invented data.
- Existing Team and coach implementation is excluded from active navigation/roadmap; any future removal must be non-destructive and migration-tested. Do not delete historical data solely to hide a feature.
- Preserve explicit self-recorded provenance; do not call local hashes independent proof of athletic performance.
- v41 remains the stable user-installed baseline until v42 verification and owner-authorized APK release.

## 3. Information architecture
Five tabs: COURT, TRAIN, QUESTS, PROGRESS, ME. Five targets, single row, 48dp minimum interactive targets, compact ~56-58dp dock, no overlaps, responsive at 320dp/360dp and font scale 1.0/1.3/2.0.
COURT: dominant Resume/Start action; current training summary; next drill; compact latest result; secondary shortcuts.
TRAIN: My Plan | Library | Custom; one-row selector at normal font size; intentional vertical adaptation only at accessibility font sizes; search/filter close to results; compact drill rows with illustration, category, target, quick practice; drill detail and Mission Builder.
QUESTS: current personal challenge, earned evidence-backed badges, completed personal Missions, progress to next objective. No team challenges.
PROGRESS: summary -> skill trends -> comparable drill trends -> session history -> detailed receipt. Never substitute XP for skill.
ME: customizable jersey/profile, goals, schedule, equipment preferences, settings, accessibility, local-data explanation, future export/restore.
Keep scorekeeper reachable from Court. No coach/school UI or promises of working team connections.

## 4. Screen behavior and hierarchy
COURT: above fold show next action and status; optional cinematic hero only if primary action remains visible. Avoid duplicate buttons with same destination.
TRAIN: remove needless 190dp hero from browsing path or collapse to compact header. Ensure My Plan/Library/Custom never accidentally become three full-width rows at standard font size. My Plan shows six drills as compact actionable list. Search and filters should not bury the first result.
DRILL DETAIL: purpose, setup, equipment/space, measurement, visual sequence, top 3 technique cues, common mistakes, relevant history, sticky Start action where appropriate. Detailed guidance collapsible and accessible.
LIVE RECORDING: prominent Success/Miss with generous hit targets, undo, compact timer, visible target and progress, pause/resume; no distracting hero or glass blur behind controls. Protect unsaved attempts.
RECEIPT: saved confirmation, exact numerator/denominator and rate, target status, timestamp when available, self-recorded label, comparable prior result only if valid; next drill primary action.
QUESTS: explicit criteria and supporting record IDs for any achievement; rest-friendly consistency instead of coercive daily-streak penalty.
PROGRESS: charts with zero baselines where appropriate, visible denominators, no trend claim when sample is insufficient, same drill/measurement only.
ONBOARDING: immersive Signature branding without 300dp*fontScale forcing essential controls below viewport; personal goals, difficulty, training days, first workout; avoid school/coach role selector.

## 5. Training intelligence v1
Deterministic and offline-first. Existing daily plan selects one drill per category, using level and day-of-year, not performance. New algorithm must:
1. Filter by valid catalog, selected difficulty, equipment/space when collected, and player overrides.
2. Preserve unfinished drafts and active Mission precedence.
3. Use compatible recent evidence only; never compare unlike measurement definitions.
4. Prefer the player's chosen focus, add reasonable variety, and propose a targeted progression only with adequate history.
5. Include an explanation referencing actual data or user preferences.
6. Store algorithm version, decision inputs, eligible candidates, and chosen drill/seed for reproducibility.
7. Fail to simple deterministic level-based selection when evidence is absent or insufficient.
8. Avoid inferred fatigue, injury, shot mechanics, or hand balance without explicit measurements.

## 6. Measurement and data contract
Existing: SaveState JSON in SharedPreferences; DrillResult has date, slotId, drillId, successes/completed, attempted, practice flag, optional drillSnapshot, Mission identifiers, measurementSnapshot, recordedAt; DrillDraft stores attempts, successes, remainingSeconds, targetConfirmed. Mission receipts use SHA-256 hashes to check consistency of stored local results, not independent verification.
Proposed additive schema: schemaVersion, measurementDefinitionId/version, optional actualElapsedSeconds, optional context fields (hand, distance zone, perceived difficulty), recommendationVersion and provenance. Optional fields default null; never fabricate legacy values.
Trend acceptance: >=3 compatible sessions AND category-appropriate minimum attempts (threshold must be researched/tested), report aggregate made/attempted with denominator, recent-vs-baseline and confidence caveats; no unsupported 'skill rating'.
Persist migration should be additive and backward compatible. Evaluate Room only behind reversible, tested migration with full v41 data preservation and malformed-state recovery. Do not migrate merely for UI work.
Protect historical results against accidental mutation and duplicate saves; keep Mission receipt validity.

## 7. Gamification
Keep XP explicitly labeled participation XP. Existing thresholds Rookie/Bronze/Silver/Gold/All-Star/MVP/Legend are participation ranks, never certified ability. Existing fixed awards include 50 XP daily drill and 100 XP all-six bonus. Do not retroactively rewrite awards without a migration and disclosure.
Add personal-best badges only from compatible valid evidence. New challenge definitions must include stable ID, rules version, evidence IDs, issued timestamp, and no duplicate issuance.
Scheduled training consistency should allow rest days and user-selected training days.

## 8. Work packages (proposed; no implementation yet)
WP01: Source and rendered baseline, screenshot fixtures, screen-density and accessibility audit.
WP02: Compact walkthrough and segment rail fix; first viewport recovery.
WP03: Court and Train hierarchy redesign, no regressions in custom drill, Mission Builder, daily practice.
WP04: Drill detail and live recording ergonomics, real-device hit targets, resume and Undo tests.
WP05: Receipt and Progress clarity, comparable data rules.
WP06: Quest mechanics and solo-only onboarding/Me cleanup.
WP07: Additive measurement/provenance model, integrity and migration tests.
WP08: Deterministic recommendation engine with reproducibility and fallback tests.
WP09: Comprehensive Android device, performance, accessibility, and offline validation.
WP10: Owner review, signed in-place v42 APK release, rollback/readiness documentation.
Do not treat each WP as a single small 'improvement'; split implementation into reviewable commits and group APK updates only after ten verified improvements per owner preference.

## 9. Release acceptance
- Full compilation, unit tests, lint, native UI tests, and GitHub Actions green; inspect canceled/failed workflows, never equate skipped checks with pass.
- Real Android emulator screenshots for Court, Train, drill detail, active practice, receipt, Quests, Progress, Me, onboarding; 320dp/360dp and font scale 1.0/1.3/2.0, no clipped primary controls.
- Validate one-handed attempt recording, accessible 48dp targets, reduced motion, no lost drafts on interruption, no duplicate result or award on retry.
- Test no-history, many-history, custom drill archived/edited, six daily slots, Mission pause/resume, app upgrade v41->v42, offline mode, malformed data.
- Verify APK appId com.courtquestpro, versionCode >41, same signer, checksum, clean install and upgrade, preserved saved profile and records.

## Progression batch implementation notes

- Progress summaries use valid dated saved drill results, verified Mission receipts, and the existing participation XP/rank. Filters are 7 days, 30 days, and all time; all-time activity is graphed as monthly buckets for the most recent 12 months.
- New daily check-in and weekly three-distinct-days rewards use stable versioned keys. A single additive quest activation timestamp prevents upgrade-time retroactive XP; saved-result writes and app restart reconcile rewards idempotently. Existing per-drill XP and the six-drill daily bonus are unchanged.
- Achievements are deterministic projections of saved results, frozen measurement snapshots, and verifiable Mission receipts. Personal bests require matching drill definition, schema, and attempts. Unverifiable receipts remain visible as saved receipts but do not unlock the Mission achievement.
- Progress, Quests, and Me remain native Compose screens, use the existing black/orange system and artwork, and expose no active team/school/coach route. Team data is not removed. No new network services or dependencies are introduced.
- The v42 APK/signing/release gates in this plan are deliberately not run for this batch; the request explicitly excludes publishing an APK. Build, lint, unit and guarded emulator evidence is recorded in the branch’s GitHub Actions runs.
- No claim of visual acceptance from CI alone. Owner confirms installed build before next release batch.

## 10. Audit limits
Prior phases were primarily source review with limited owner-provided v41 screenshot evidence. Competitive references were initial research, not independent product benchmark tests. All proposed thresholds, UX improvements and technologies require verification before implementation.
