# CourtQuest Pro repository instructions

## Active development line

The owner-approved premium flow toolkit and current build-6 source continue on [`dev/premium-flow-toolkit`](https://github.com/Lamont-Labs/CourtQuestPro/tree/dev/premium-flow-toolkit). Read that branch's `AGENTS.md`, `DESIGN.md`, [toolkit guide](https://github.com/Lamont-Labs/CourtQuestPro/blob/dev/premium-flow-toolkit/docs/tooling/PREMIUM_FLOW_TOOLKIT.md) and [verification record](https://github.com/Lamont-Labs/CourtQuestPro/blob/dev/premium-flow-toolkit/docs/tooling/PREMIUM_FLOW_VERIFICATION.md) before new UI work. This original documentation branch retains older app source; preserve the newer Drill Library, daily-flow and game-flow work when continuing. Follow a newer owner-specified branch when supplied.

## Start here for UI work

- Read [DESIGN.md](DESIGN.md) before designing, implementing, reviewing, or publishing any CourtQuest Pro UI change. It is the active design standard, tool guide, and visual acceptance checklist.
- Ground each pass in the latest owner feedback, the exact source revision, and the affected Android flow. Start the daily-use redesign with Court → start or resume training → drill result → Progress.
- For a material UI pass, load the installed `ui-ux-pro-max` guidance and run the relevant design-system, UX, and detected `jetpack-compose` searches. Check category, platform, and version fit before applying the output. Save the selected guidance and the reason for any adaptation in `DESIGN.md`.
- Use the named design and review sources at the stage where they help. Apply the Impeccable-based review in `DESIGN.md` to actual Android captures for each visually reviewed candidate. Record the tools used, evidence gathered, and remaining gaps.
- Build production UI in Kotlin and Jetpack Compose. Reuse existing Android components, shared design tokens, and state. Keep player names, scores, drill history, and progress dynamic and based on saved data.
- Verify compilation, unit tests, lint, and the affected user journey separately. Call out blocked runtime or visual checks. Use the existing GitHub development APK update path when release is authorized, and verify the downloaded APK's version, application ID, signature, and checksum.
