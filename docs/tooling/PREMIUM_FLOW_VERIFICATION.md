# Premium flow toolkit verification

Date: November 11, 2026; native acceptance update November 12 UTC.

## Superseding current-source verification

The historical setup record below describes the original 42-test checkpoint. Current source `375a607fd7be33bf30ad56c2337cf9acf3ae2253` passes [native run 43136062719](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062719): 48 Android unit tests, 3 selector regressions, 0 lint errors/11 warnings, library/daily/game flows and guarded Maestro (1/1). [Current toolkit run 43136062726](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43136062726) also passes its dedicated-emulator journey. Haze/Lottie and the Signature components are now integrated. APK identity/signing/alignment passed after the native artifact was downloaded independently. Exact-binary publication and independent public-download verification also passed; see [the master tracker](../MASTER_SPEC_IMPLEMENTATION.md) and [exact-binary ledger](evidence/signature-build7-native.json).

The original 43096106238/43096106289 runs remain queued; no success is attributed to them. Fresh successful executions finish the outstanding smoke verification. Physical-device and wrong-AVD guards still fail closed before reset/install. Maestro uses relative screenshot names and exact visible native navigation labels.

## Historical setup handoff

## Source and preservation

- Active branch: `dev/premium-flow-toolkit`.
- Baseline: `b397805f5e328f1b7dd9a2ba66e5447a3b80fcea`, the verified Drill Library build-6 release lineage.
- Toolkit configuration and strengthened post-relaunch profile smoke assertion were published at `41fde059c6a02efacb629f6da2bd7a48c3b589d0`.
- The existing `app/src`, signing files, saved-state schema, release-request files and version code are unchanged by this setup.
- Source/example references are pinned. Runtime/test-tool source pins match the selected release tags.

## Completed checks

| Check | Evidence |
| --- | --- |
| Plugin availability | Product Design, Figma, Context7 and GitHub confirmed installed |
| Reference catalogue | Seven unique repositories with full 40-character commit pins and immutable URLs |
| Review resources | Four local Impeccable guides with upstream LICENSE and NOTICE |
| Config parsing | JSON and both YAML documents parse successfully |
| Shell validation | Both installer and test runner pass `bash -n` |
| Tool artifact | Official Maestro 2.12.1 ZIP SHA-256 matches `54dbb0716425626604973cbb374c585ca30272375d9ce15177a1152765ddd9d7` |
| CLI installation | Installed executable reports `2.12.1` |
| Physical-device guard | Runner exits 2 before adb/tool installation for a non-emulator serial |
| Wrong-AVD guard | Runner exits 2 before APK installation or reset when the AVD is not `CourtQuest_PremiumQA` |
| Source preservation | No diff in `app/src`, `signing`, or `release-request` from the baseline |
| Independent review | No Critical/Important production issue; post-relaunch name assertion strengthened before acceptance |
| Diff hygiene | `git diff --check` passed on the implementation |

## Native verification gate

State at the original toolkit-only handoff: **local unit/build/lint and APK verification passed; Maestro device smoke pending CI runner**.

- [Premium flow toolkit CI](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43096106238): queued at the last check; no runner job has been assigned.
- [Android development checks](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/43096106289): queued at the last check.
- Local Android SDK 35/build tools 35.0.1 and checksum-verified Gradle 8.9 completed all unit/build/lint tasks successfully: **42 tests, 0 failures/errors/skips; lint 0 errors / 6 warnings**. Host JDK: 21.0.13; app Java/Kotlin bytecode target remains 17.
- All three new runtime dependencies resolved at the selected versions with the existing Compose BOM. See [resolved dependency excerpt](evidence/resolved-toolkit-dependencies.txt).
- The diagnostic APK passed package/version/launcher checks, original signing continuity, v2 signature, ZIP integrity, main-Activity presence and 16 KB alignment. It is a local diagnostic build and was not published.
- Evidence: [unit/lint/build summary](evidence/local-verification.json), [build output](evidence/android-build.txt), [APK verification](evidence/apk-manifest.json), and [byte-exact upstream guide hashes](evidence/impeccable-source-hashes.json).
- No local hardware-accelerated Android emulator is available; Maestro's actual device journey requires the dedicated emulator in CI or another supported development environment.
- The active source lineage has no repository `tests/` directory. Its regression suite is the Android unit-test suite and existing native journey scripts.

The local build confirms the added dependencies compile and the existing unit suite passes. Device journey execution remains an independent gate: a configured workflow, successful CLI installation, or static review is not evidence that the Android journey passed.

## Historical scope and next work

This setup adds dependencies and development/verification resources. The deep-black/brighter-orange Signature glass UI, contextual hints, and Lottie feedback have not been integrated into screens. Haze 1.1.1 is the compatible blur foundation; Haze 2.x glass APIs require a separate toolchain decision.

At the toolkit-only handoff, the published APK remained build 6. That setup did not publish a replacement; the superseding Signature build-7 delivery is recorded above.

Next-chat prompt: [NEXT_CHAT_PROMPT.md](../NEXT_CHAT_PROMPT.md). Finish the native verification gate before accepting or releasing subsequent UI work.
