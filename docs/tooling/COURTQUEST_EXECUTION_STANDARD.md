# CourtQuest Pro — Development Execution Standard
Effective: 2026-11-17. Scope: native Android solo-player product. Owner operates no GitHub workflows manually.

## Authority
Canonical repository: Lamont-Labs/CourtQuestPro; working branch dev/premium-flow-toolkit. Read AGENTS.md, DESIGN.md, docs/tooling/PREMIUM_FLOW_TOOLKIT.md and current owner direction first. School/coach/team work is excluded. Preserve approved branding, existing data, package ID and signing identity. Do not overwrite newer commits with a stale Replit workspace.

## Tool-selection gate (required before every substantial implementation batch)
1. Source truth: inspect current branch HEAD, changed files, tests and last CI results through GitHub.
2. Reuse: search existing app components and pinned local/open-source references before implementing a duplicate. Prefer Jetpack Compose native examples; check source commit, license, API/toolchain compatibility, maintenance and accessibility. Do not bulk-import repositories.
3. Native code: choose a tool that can edit the complete connected codebase. Replit is an available CourtQuestPro project, but first verify its current branch/commit and synchronization with canonical GitHub. If not verifiable, do not delegate destructive edits.
4. Design: use installed Product Design for user-journey audit, Figma for editable screens/components where useful, and existing UI-UX Pro Max/Impeccable guidance. Do not convert the native app into a web prototype.
5. API correctness: use Context7 for version-sensitive library details. Treat library installation as distinct from actual use.
6. Verify: automated compile/unit/lint + actual Android emulator screenshot/interaction evidence at normal/compact/200% text; measure controls and preserve synthetic fixture integrity.
7. Release: only after owner authorization and verification of signer, package ID, version code, upgrade, rollback and saved data.

## Work cadence
Six major feature-area work packages rather than hundreds of cosmetic tasks. Each package must produce: source diff, commit SHA, test results or explicit blockers, screenshot evidence when visual, and a clear next action. Make related fixes together. Do not create documentation-only tasks when implementation can safely proceed. Never represent queued/canceled/failed CI as passing.

## Automation
Workflows should trigger on relevant source changes or be started by available connected tools. Never require the owner to operate GitHub. Avoid cancel-in-progress settings that repeatedly discard useful checks. Capture failure diagnostics as artifacts. Diagnose actual logs before editing.

## Performance and quality
Track time spent in CI queue/build vs implementation, failure root causes, repeated regressions, first-viewport space, drill start taps, recording action visibility, font scale 1.0/1.3/2.0, saved-data persistence and APK upgrade continuity. Favor reliable native improvements over dependency count or novelty.

## Immediate state
Existing Replit CourtQuestPro app confirmed; its GitHub synchronization has NOT yet been verified. GitHub screenshot baseline run 44173344482 failed due to a missing expected navigation tag after fixture seed; diagnostic capture change ce4c355d committed. v41 remains the installed stable release. Do not claim screenshot baseline success or v42 implementation completion.
