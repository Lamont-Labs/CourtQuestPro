# Premium Flow Toolkit Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task by task.

**Goal:** Add every owner-approved flow/design resource, compatible Android libraries, and repeatable Maestro tooling to the current CourtQuest Pro development line.

**Architecture:** Start from the verified build-6 release branch, preserve the app's native logic, and add development resources at their proper layer. Pin example repositories as references, vendor selected licensed Impeccable review guides, add compatible runtime libraries, and install Maestro through a checksum-pinned local bootstrap. Keep app visual implementation separate from tooling readiness.

**Tech Stack:** Kotlin 2.0.21, Compose BOM 2024.12.01, AGP 8.7.3, Java 17 for Android builds, Java 21 for Maestro CLI, Android SDK 35.

**Spec:** DESIGN.md; owner instructions in this conversation to add the full recommended toolkit and retain Signature branding, deep black, brighter orange and polished glass.

## Global Constraints

- Preserve application ID `com.courtquestpro`, signer, local saves, custom drills, scoring, Undo and history.
- Base source: `b397805f5e328f1b7dd9a2ba66e5447a3b80fcea`, containing verified Drill Library build 6.
- Preserve the current Kotlin, Compose and Android SDK versions.
- Product Design, Figma, Context7 and GitHub connections were confirmed installed on November 11, 2026.
- Source example apps are development references, not Android runtime dependencies.
- Maven compatibility check selects Lottie Compose 6.7.1, Intro Showcase 2.0.2 and Haze 1.1.1.
- Haze 1.1.1 matches Kotlin 2.0.21 / Compose 1.7.x. Haze 2.1.0 declares Kotlin 2.4.21 and Compose 1.12.2 dependencies; its newer glass APIs are not adopted by this setup pass.
- Run destructive fresh-profile smoke tests only on a dedicated Android emulator.
- This task does not publish an APK or claim visual acceptance.

## Review Focus

- Tool installation verifies the official release SHA-256 before extraction.
- Emulator-only test runner refuses physical devices before clearing app state.
- Existing build-6 feature/source files remain unchanged.
- Native dependencies resolve without an unplanned compiler or Compose upgrade.
- Reference pins and copied licenses remain traceable, and docs distinguish prepared tools from rendered UI.

## Tasks

### Task 1: Connect the approved references and Android libraries

- [ ] Pin Compose Samples, Now in Android, Impeccable, Maestro, Intro Showcase, Lottie and Haze in `tools/premium-flow/references.lock.json`.
- [ ] Add the selected Impeccable review guides with the upstream license and notice.
- [ ] Add the three version-pinned Android libraries to `app/build.gradle.kts`.
- [ ] Carry the newer owner branding/palette/glass decisions into the build-6 DESIGN.md without replacing its newer implementation evidence.
- [ ] Link the toolkit from AGENTS.md and document each tool's actual role and readiness.
- [ ] Verify JSON, copied-source hashes and Gradle resolution/build through CI.

### Task 2: Install and exercise Maestro tooling

- [ ] Add the official checksum-pinned Maestro 2.12.1 installer and ignored local cache.
- [ ] Add a dedicated-emulator smoke flow covering fresh onboarding, the four navigation destinations and preserved profile after relaunch.
- [ ] Add an emulator-only runner and a manually runnable / branch-triggered CI workflow.
- [ ] Verify installer syntax and CLI version; run the native build, unit tests, lint and smoke flow in CI.
- [ ] Record exact evidence, any blocked checks, and a next-chat handoff.

## Execution ledger

- Worktree: fresh checkout, branch `dev/premium-flow-toolkit`, starting at the build-6 release checkpoint.
- Ruling: use the build-6 development/release lineage instead of the older `main` or original design branch, because the latter omit recent drill-library and flow fixes. Cost if wrong: the toolkit targets the build the owner most recently received; no shared branch is replaced.
- Ruling: configuration-only additions are checked through actual dependency resolution/build and tooling execution; no unit tests that merely mirror the dependency declarations.
- Baseline repository-test command: `python3 -m unittest discover -s tests -v` reports no importable `tests` directory on this current lineage. Its actual regression suite is the Android unit-test suite, with existing native journey scripts.
