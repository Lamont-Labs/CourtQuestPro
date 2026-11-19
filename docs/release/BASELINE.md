# CourtQuest Pro release baseline

Audited November 18, 2026 (Pacific). Default branch `main` was at `02ce5d4c9c6f9c979f23dbbed6814ebd420e47dc`. Development uses `dev/jersey-reference-studio` at `e809110eaeab57b62f0d203b15d2dc9f7681ad82`, also tagged `courtquest-phone-update-v54`. The separate `dev/full-overhaul-jersey` tip was `76b3635fe952bcbfaaf5602e2c7e2903d389d3ab`. The candidate branch starts directly from v54.

| Item | Baseline evidence |
| --- | --- |
| Published phone build | GitHub prerelease v54, `com.courtquestpro`, versionName `1.0`, versionCode 54, minSdk 26, targetSdk 35. Downloaded APK SHA-256 `dd497368af32dc5c5096a77878128c26480b522484bbdc1e26cc3770ca72405e`. |
| Baseline automation | [v54 phone release](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44436967130), [development checks](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44436967021), [dock capture](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44435210543) passed. Release evidence records v53→v54 install-over, saved preferences, Studio, walkthrough, and training. |
| Signing | v53 and v54 use SHA-256 `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`, the repository's public development certificate. The baseline Gradle configuration has no separately configured release signer, and there is no public Play bundle in the v54 release. |
| Studio | Arena has front/back material atlases. Classic, Fastbreak, and Retro use procedural vector artwork. The dock layers a white text control over the jersey, independent of the selected number ink and garment placement. |
| Product scope | Active five-tab player app includes onboarding, Court, Train/Library, drills, Mission Builder, Progress, Quests, Me, and local scorekeeping. The owner-locked release scope excludes team, coach, and school administration. See `docs/MASTER_SPEC_IMPLEMENTATION.md`. |
| Data | Player state is stored in app-private SharedPreferences. App backup is disabled. Drills can launch a browser or external video app. No account, cloud sync, ads, billing, or telemetry is exposed in active source. This is a baseline source observation, pending merged-manifest and dependency review. |
| Design references | `AGENTS.md`, `DESIGN.md`, and the shipped Arena material are available. Linked Figma metadata returned a Starter-plan rate limit. |
| Immediate blockers | API 36 for Play, production signer and transition for debug-signed testers, three photographic garment pairs, release bundle validation, candidate device verification, store materials, and owner Play Console declarations. |

Google's [target API policy](https://support.google.com/googleplay/android-developer/answer/11926878) requires handset submissions to target Android 16/API 36 from August 31, 2026. [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756) distinguishes the upload key from the signature Google uses for distributed installs. Test APK continuity and Play signing require independent verification.
