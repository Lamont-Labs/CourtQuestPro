# CourtQuest Pro — premium visual refinement campaign

## Campaign contract

Target: 8 categories × 100 accepted passes = 800 **evidence-backed** refinements.
A workflow invocation is **not** automatically a successful visual pass. A pass is accepted only after (1) source change, (2) Android build and tests, (3) rendered screenshots, (4) visual comparison, (5) regression review, and (6) recorded acceptance.

Categories: visual-identity, layout-composition, typography, glass-depth, motion-interactions, consistency, usability, final-refinement.

## Available automation

The manually dispatched `.github/workflows/premium-visual-capture.yml` captures a real first-launch screenshot from an Android emulator and uploads screenshot, test, lint, and manifest artifacts. It accepts a phase and pass identifier for traceability. It has read-only repository permissions and never changes application source or publishes an APK.

**Not yet implemented:** autonomous visual scoring, source-code editing agent, multi-screen navigation capture, persistent accepted-pass ledger, and unattended chaining across 800 iterations. No pass may be represented as completed until those components exist and the evidence passes review.

## Security and release controls

- Do not place provider API keys in source, logs, workflow input fields, or artifacts.
- Keep agent changes off `main`; use isolated development branches and reviewed merges.
- Never modify `com.courtquestpro` application ID.
- Never rotate or replace the installed-app signing identity during visual work.
- Do not publish an APK from this evidence workflow.
- Keep existing save-state serialization and onboarding compatibility intact.
- Failed builds or missing screenshots fail the acceptance gate.

## Next integration milestone

1. Confirm the screenshot workflow runs successfully on GitHub-hosted Android emulation.
2. Expand deterministic navigation capture to onboarding, dashboard, drills, match, and progress screens.
3. Connect an explicitly authorized model provider via GitHub Actions secrets and scoped permissions.
4. Implement a bounded edit/test/critique loop with durable pass receipts and cost limits.
5. Resume from checkpoints, compare candidates, and release only a verified approved APK.

The target quality of 100/100 is an aspiration, not a measured outcome.
