# Development progress — 2026-11-05

## CQ-001 — Correct scorekeeper Undo

Status: IMPLEMENTED; Android compilation and device verification pending.

Branch: `dev/scorekeeper-undo-history`
Implementation commit: `2fa10f66304ba40efd4f42ea2f5c6bcdee4c694a`

### Source evidence
- `app/src/main/java/com/courtquestpro/MainActivity.kt`: prior Undo decremented the leading score rather than the actual most recent scorer.
- `README.md`: independently documents the defect.
- `.github/workflows/android-apk.yml`: Android build and lint only trigger automatically for pushes to `main`, not this development branch.

### Changes
- Persist the ordered scoring history in `SaveState.pointHistory` (nullable for Gson compatibility with old saves).
- Record the scorer for every accepted point.
- Undo only the last recorded scoring event.
- Reset history when a match starts or the daily session resets.
- For legacy in-progress matches without scoring history, disable unsafe score guessing; existing saved scores remain intact.

### Verification
- GitHub update_file accepted the source change and returned a commit SHA.
- Android compile/lint, device behavior and regression tests have NOT been verified.
- No public release, production deployment, or main-branch merge was performed.

### Next task
CQ-002: Add isolated scorekeeper logic with automated unit tests covering alternating scorers, tied scores, repeated undo, old saved state, and win boundaries; enable safe branch build verification without publishing a release.
