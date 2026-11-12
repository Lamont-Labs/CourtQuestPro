# Player Mode: next implementation sequence

Authority: the owner-approved master specification. This is the next scoped sequence after CQ-PREMIUM-001 passes its current-source device and development-delivery gates. It does not claim these features are implemented.

1. **CQ-PLAYER-002 — Versioned Mission foundation and builder.** Add immutable ordered drill snapshots, explicit source and measurement rules, stable Mission/session identity, stored random seed and algorithm/input provenance. Adapt current daily training and extra practice without discarding legacy slots, results, drafts, custom revisions or award keys. Let a solo player build, save and resume a real Mission. Preserve the same Mission definition for future authorized team assignments.
2. **CQ-PLAYER-003 — Player navigation and Me.** Introduce Court, Train, Quests, Progress and Me with usable destinations and predictable Back behavior. Migrate the current Drills and Setup entry points. Do not add an empty coach dashboard or a role switch that grants authority.
3. **CQ-PLAYER-004 — Evidence-backed Quests.** Implement initial deterministic participation/milestone rules with rule version and supporting receipts. Keep XP distinct from proficiency, avoid coercive streak loss, and show honest unearned states.
4. **CQ-PLAYER-005 — Comparable Progress.** Filter and compare identical drill/measurement versions and compatible denominators; retain historical definitions, receipt drill-down and truthful empty/insufficient-evidence states.
5. **CQ-PLAYER-006 — Resumable player setup.** Extend goals, experience, equipment and environment using minimal local inputs. Preserve existing profiles and drafts; account/team/consent routes must reflect actual authenticated capabilities when those exist.

Each issue has its own migration/preservation check, meaningful unit/state tests, dedicated-emulator journey, normal/compact/large-text review and exact development-APK evidence. A displayed coach role, invite code or team membership never substitutes for server authorization. The school foundation follows the master specification's Phase 3 and its security/consent gates.

## CQ-PLAYER-002 migration and acceptance contract

Grounded in the current `SaveState`, `TrainingLibrary`, `DrillDraftRecovery` and `DrillResult` contracts:

- Introduce Mission definitions and sessions additively. Existing nullable custom-drill, draft and result collections must remain readable by Gson; normalize absent collections deliberately. Test the actual older JSON shape before switching any route.
- A published Mission version holds ordered drill snapshots and measurement rules, rather than resolving its instructions through the current mutable selection. Existing `UserDrill.familyId` links revisions; `TrainingLibrary.catalog` intentionally retains archived definitions for queued work. Preserve that behavior.
- Preserve legacy `activeDate`, slot IDs, completions, practice session IDs, drafts, `awardedKeys`, total XP and match records. Do not infer elapsed time or completion timestamps from a date-only `DrillResult`. Mark unavailable historical provenance explicitly.
- Persist a builder draft separately from a training session. Adding/reordering/recreating the builder cannot create results, spend XP, complete slots or replace a running daily/practice draft. Starting another session requires an explicit resume/finish/discard decision.
- Persist generation seed, algorithm version, selected input constraints and candidate identifiers with a generated version. Replaying those inputs must reproduce the ordered selection. Protected/mandatory steps cannot be rerolled away; an impossible constraint set returns an actionable empty result.
- Keep the existing daily and extra-practice award policies during the first Mission migration. A distinct session/result identity and existing award keys must prevent duplicate writes across repeat taps and process recreation.
- Builder UI must support a real ordered selection, remove/reorder, an estimated duration derived from steps, save/reopen, start/resume and a receipt linked to the exact version. It uses the shared Signature header, native surfaces and accessible actions. No coach assignment or connected sync is claimed.
- Acceptance requires older-save fixtures, immutable-revision and deterministic-generation tests, same-day daily/practice restoration, a complete builder-to-receipt-to-Progress Android journey, the existing library/daily/game/Maestro suites, and normal/320 dp/130%/200%/keyboard review. Publish the exact successful candidate with a higher version code and the same signer.
