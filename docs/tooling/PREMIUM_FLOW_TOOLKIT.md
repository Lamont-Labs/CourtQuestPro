# CourtQuest Pro premium flow toolkit

Owner approved the complete toolkit on November 11, 2026. Development branch: `dev/premium-flow-toolkit`. Baseline: `b397805f5e328f1b7dd9a2ba66e5447a3b80fcea` from the verified Drill Library build-6 release lineage.

## Tools and roles

| Resource | Added at | Use |
| --- | --- | --- |
| Product Design | Existing connected plugin; verified installed | Evidence-based journey review and visual exploration |
| Figma | Existing connected plugin; verified installed | Editable flow boards, screen states and component specifications |
| Context7 | Existing connected plugin; verified installed | Check APIs against the app's actual dependency versions |
| GitHub | Existing connected plugin; verified installed | Source, CI evidence, review and authorized APK delivery |
| Compose Samples | Immutable upstream commit in references.lock.json | Jetsnack, Jetcaster and Reply native pattern references |
| Now in Android | Immutable upstream commit in references.lock.json | App organization, navigation/state patterns and screenshot tests |
| Impeccable | Four local guides plus LICENSE and NOTICE | Shape journeys, critique actual captures, clarify copy and improve onboarding |
| Maestro | Version/checksum-pinned installer, local CLI, smoke flow and CI | Fresh setup, four navigation destinations and profile continuity on a dedicated emulator |
| Compose Intro Showcase | Android dependency 2.0.2 | Contextual first-use feature guidance when a later UI change calls for it |
| Lottie Compose | Android dependency 6.7.1 | Purposeful save/completion feedback when a later UI change calls for it |
| Haze | Android dependency 1.1.1 | Compatible backdrop-blur foundation for black-glass work |

Pins: [references.lock.json](../../tools/premium-flow/references.lock.json). Local review guides: [shape](../../tools/premium-flow/impeccable/shape.md), [critique](../../tools/premium-flow/impeccable/critique.md), [clarify](../../tools/premium-flow/impeccable/clarify.md), [onboard](../../tools/premium-flow/impeccable/onboard.md). The copied Impeccable guides retain the upstream license and NOTICE. The sample apps are source references, not bundled apps or runtime dependencies. Version checks still apply when adapting their code.

## Native compatibility decision

The app retains Kotlin **2.0.21**, Compose BOM **2024.12.01**, AGP **8.7.3**, minSdk **26**, and compile/target SDK **35**.

Maven Central POM checks:
- Lottie Compose 6.7.1 declares Kotlin 1.9.22 and an older Compose BOM, compatible in principle with the app's newer fixed BOM; CI resolves and builds the real graph.
- Intro Showcase 2.0.2 declares Kotlin 1.9.10 and an older Compose BOM; CI resolves it against the existing app stack.
- Haze Android 1.1.1 declares Kotlin 2.0.21 and Compose Android 1.7.5. This fits the current toolchain.
- Haze 2.1.0 declares Kotlin 2.4.21 and Compose 1.12.2. Its newer `hazeGlass` API is not part of Haze 1.1.1 and must not be copied into this build.

Glass work can combine the compatible blur foundation with native tint, crisp edge highlights, reflections and depth. A future toolchain/Haze upgrade needs a separate compatibility decision. Adding the libraries does not mean their visual features are already used by the app.

## Flow work sequence

1. Review the real Court → training → saved receipt → Progress journey.
2. Map screen/state transitions in Figma, including fresh, resumed, interrupted and empty states.
3. Read the local shape/critique/clarify/onboard guides at the relevant stage and apply their principles to native Android.
4. Implement shared components and navigation behavior using the pinned app stack.
5. Add contextual hints or a short completion animation only when it helps the player.
6. Run the existing native daily, game and library verification scripts plus the complementary Maestro smoke flow.
7. Review actual Android captures before calling a visual improvement accepted.

The owner-selected Signature identity, deep black, brighter orange and polished glass direction are in DESIGN.md. Keep the logo/artwork fidelity anchor and pending transparency work visible when integrating brand assets.

## Maestro installation and execution

The installer uses the official **cli-2.12.1** release ZIP and its GitHub-provided SHA-256:
`54dbb0716425626604973cbb374c585ca30272375d9ce15177a1152765ddd9d7`.

Local requirements: Java 21, curl, unzip, sha256sum. Android execution also needs the SDK's adb and a running disposable AVD named **CourtQuest_PremiumQA**.

```bash
bash scripts/install_maestro.sh
bash scripts/run_premium_flow_checks.sh emulator-5554 app/build/outputs/apk/debug/app-debug.apk
```

The runner rejects physical-device serials and any emulator whose AVD name is different before installing the APK or running the clear-state flow. The smoke flow creates synthetic **CourtQuest QA** data; it must never be run against the owner's phone. Its reports and screenshots go under `artifacts/premium-flow-toolkit/`.

The pinned CLI is stored in ignored `.tooling/`; no binary or downloaded ZIP is committed. The workflow [premium-flow-toolkit.yml](../../.github/workflows/premium-flow-toolkit.yml) builds/tests/lints first, installs Java 21 and Maestro, and runs the flow on Android 35.

This complements the existing deeper daily/game/library scripts. The smoke flow alone is not evidence that recording, Undo, all saved results or app updates work.

## Readiness and verification

- All four plugin installations confirmed.
- Seven upstream repositories pinned.
- Four Impeccable guides and their license/notice copied into the repository.
- Three runtime libraries declared with fixed versions.
- Maestro installer and emulator-only runner added.
- CI build, Android tests, lint and Maestro execution: **pending**.
- No app source, save schema, package ID, signing identity, release APK or version code changed by this toolkit pass.

Exact test evidence and remaining work will be recorded here after the CI run.
