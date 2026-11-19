# CourtQuest Pro — Android release engineering audit

The candidate is version code 55 on `dev/play-production-candidate`, based on the published v54 phone release. See [the baseline](BASELINE.md) for verified inputs and [the Play worksheet](play-store/PLAY_CONSOLE.md) for account requirements.

## Active player journey

| Surface | Implementation reviewed | Evidence required for release |
| --- | --- | --- |
| Welcome and setup | Signature welcome keeps guest and returning entry. Get Started restores a four-step setup draft and creates the named local player. | Named completion, backwards navigation, recreation, returning entry, compact and enlarged layout. |
| Court | Real saved next drill and training state, compact personalized jersey. | Actual start/resume, plain/crowded preferences, reviewed native shot. |
| Train and Drill Library | Search and random drill filtered by current query; offline 18-drill imagery, technique guides, and custom drills. | Library and live result device journey, per-drill art integrity, custom and mission paths on release candidate. |
| Training and scorekeeping | Saved attempt draft, eligibility confirmation, idempotent result route, local score history and Undo. The drill clock stops on Android backgrounding. | Offline recording, activity and process relaunch, rapid taps, saved receipt verification, score and Undo, restored results. |
| Progress and Quests | Snapshot results feed metrics and objective completion. Awarded keys prevent replay from granting rewards twice. | Record one drill and see Progress/Quests update. Empty-state and date-boundary checks. |
| Me and Jersey Studio | Named colors, double-zero handling, draft and saved design, local Privacy policy. Eight garment material atlases, 58dp dock. | Four front/back native previews and saved font-scale dock. Test name/number validation, persistent colors, low-contrast safeguards. |
| Team, coaches and schools | Explicitly deferred in the owner-approved solo scope. Existing internal team models remain disconnected. | Keep team, cloud and administration promises out of the listing and no exposed route. |

## Security, release composition and performance

- App package remains `com.courtquestpro`. Min SDK 26. Candidate raises compile and target SDK to 36 with AGP 8.10.1 and the checksummed Gradle 8.11.1 wrapper.
- The candidate removes the unused WebKit and showcase dependencies and direct Internet permission. Practice video links use an external Android activity with a handoff label and unavailable-handler feedback. Custom drills use an allowlisted basketball category in the search and never put a saved custom title in the URL. The release manifest strips the AndroidX profile command receiver while retaining automatic profile installation. Review the exact merged bundle receipt for unexpected exported components, permissions or debug flags.
- Player and training data use Android private SharedPreferences. There are no runtime credentials in active app code. Android app backup is disabled. Device extraction rules explicitly exclude app preferences from cloud backup and Android device transfer. A malformed stored profile now displays a recovery screen and retains the original stored bytes. The app does not request access to a camera, microphone, location, contacts, storage, or advertising IDs in its own source.
- Verify platform-provided permissions and components from the release manifest. A transitive graph is captured from Gradle and queried against the OSV Maven data set. A failed query remains visible as an unavailable audit. Source review alone is insufficient to conclude that the final graph has no applicable vulnerabilities.
- The public repository's development key is an existing required dependency for the phone channel. It is not a production secret or production trust root. CI provisions an owner-configured production upload key only on explicit dispatch, validates the public upload fingerprint, and keeps the material outside the workspace. Play app signing distribution keys are a distinct identity.
- The v54 APK contains AndroidX Graphics Path native libraries in four ABIs. All four downloaded v54 libraries aligned ELF LOAD segments to `0x4000`. The candidate gate independently tests each packaged AAB library and `PAGE_ALIGNMENT_16K`.
- The garment renderer versions the three new constructions with matched front/back and region-aware relighting. Full-size and small raster caches are memory-bounded; draw work uses a cancellable background coroutine. Actual startup and process memory are measured on the Android 16 candidate emulator. Emulated samples are useful for regressions and are not a substitute for phone profiling.
- R8, resource shrinking, and an app-specific Baseline Profile are pending measured validation. Gson persists field names and model types used by legacy saves. A separate obfuscation pass needs stable schema/keep rules and an upgrade test before enablement. No minification claim is based on the existing APK.

## Known issues and release gates

| Gate | Status and action |
| --- | --- |
| Production trust and upgrade | The public Android Debug signer cannot be used for Google Play production. A new or authorized Play identity will differ from the installed test APK. Owner must authorize the signing route and the way existing testers move across channels. A tested transfer path or a decision to retain debug testers on their current channel is necessary. |
| Account and policy | Confirm actual Play account type, app record, support email, policy hosting, age targeting, content rating, and declarations. Assess optional external YouTube results before targeting child age groups. Do not use school/student accounts in this release. |
| Compatibility beyond smoke | Check TalkBack, switch/keyboard, magnification, motion preference, tablet/landscape rotation, Android minimum SDK and a physical phone. Measure 16 KB runtime separately from ELF and bundle validation. |
| Store screenshots | Use only reviewed native app captures of the exact candidate with fictional profile data. Phone and tablet are separate presentation gates. |
| Device-side deletion | There is no CourtQuest account. Android Settings clear-storage and uninstall remove private local data. Release validation must never clear real tester profiles. |
| Production go-live | A verified build and an internal test draft do not establish Play approval or production access. |
