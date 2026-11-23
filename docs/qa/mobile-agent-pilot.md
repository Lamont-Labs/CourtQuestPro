# CourtQuest Pro exploratory AI testing pilot

Upstream: https://github.com/pierridotite/mobile-agent-testers
Reviewed revision: 72616df6004805de1b854f64507023ad0bc14c87
Baseline: courtquest-update-v61; package com.courtquestpro.

## Pilot missions
- Compact phone: onboarding, walkthrough, navigation, reachable controls.
- Standard phone: complete solo training, save results, inspect Progress.
- 200% font: check labels, controls, keyboard, Card and Uniform Studio.
- Large display: customize uniform and card, flip card, save, relaunch, verify persistence.

Use synthetic player names and data only. Never use real accounts, signing keys or production credentials. Build a disposable debug APK, never publish it. Report actual observed behavior, reproduction steps, screenshots, device settings and severity; distinguish hypotheses from confirmed defects.

## Integration gate
Upstream currently requires Claude Code authentication (ANTHROPIC_API_KEY or CLAUDE_CODE_OAUTH_TOKEN), or a supported Anthropic-compatible provider; ChatGPT subscription is not an API credential. Its workflow also requires results_public.pem and ARTIFACT_PASSPHRASE. Do not enable paid runs until credentials and spending limits are explicitly configured. Keep artifacts private, review upstream scripts and pin dependencies before execution. Start with four Android profiles, maximum two concurrent jobs, no iOS. Do not replace existing deterministic tests or modify release workflows. AI reports are advisory and require reproduction before fixes.
