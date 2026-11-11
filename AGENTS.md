# CourtQuest Pro repository instructions

## Start here for UI work

- Read [DESIGN.md](DESIGN.md) before designing, implementing, reviewing, or publishing any CourtQuest Pro UI change. It is the active design standard, tool guide, and visual acceptance checklist.
- Ground each pass in the latest owner feedback, the exact source revision, and the affected Android flow. Start the daily-use redesign with Court → start or resume training → drill result → Progress.
- For a material UI pass, load the installed `ui-ux-pro-max` guidance and run the relevant design-system, UX, and detected `jetpack-compose` searches. Check category, platform, and version fit before applying the output. Save the selected guidance and the reason for any adaptation in `DESIGN.md`.
- Use the named design and review sources at the stage where they help. Apply the Impeccable-based review in `DESIGN.md` to actual Android captures for each visually reviewed candidate. Record the tools used, evidence gathered, and remaining gaps.
- Build production UI in Kotlin and Jetpack Compose. Reuse existing Android components, shared design tokens, and state. Keep player names, scores, drill history, and progress dynamic and based on saved data.
- Verify compilation, unit tests, lint, and the affected user journey separately. Call out blocked runtime or visual checks. Use the existing GitHub development APK update path when release is authorized, and verify the downloaded APK's version, application ID, signature, and checksum.
