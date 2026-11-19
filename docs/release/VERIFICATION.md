# CourtQuest Pro v55 verification

The verified v55 candidate was built at [f53edba6cdb168001f9bc148527c9ed13e8993d0](https://github.com/Lamont-Labs/CourtQuestPro/commit/f53edba6cdb168001f9bc148527c9ed13e8993d0). The staged package remains `com.courtquestpro`, version name `1.0`, version code `55`, target SDK 36, and minimum SDK 26. The source and release documentation are in [draft pull request #8](https://github.com/Lamont-Labs/CourtQuestPro/pull/8).

| Check | Verified evidence |
| --- | --- |
| Application compilation and native test compilation | Debug APK, Android instrumentation APK, and release bundle built. |
| Android JVM tests | 129 passed, zero failures, errors, and skips. |
| Source and packaged artwork | 103 Python checks passed, all eight jersey atlases and 18 offline drill plates verified. |
| Release lint | Zero errors and 20 warnings. |
| Dependency inventory | 140 resolved Maven coordinates. OSV query returned no matches. This is an advisory database check. |
| AAB | 18,985,817 bytes. Bundletool, ZIP integrity, manifest, release debug flag and signature absence, four native ABIs and 16 KB ELF/package alignment passed. It is unsigned. |
| Exact phone APK | 25,879,822 bytes. Android v2 development certificate `25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b`. |
| Android 16 upgrade | The published v54 APK was installed with synthetic saved profile, history, active drill, jersey, and custom and Mission records. Android installed v55 in place, preserved preferences byte for byte during installation, and retained preexisting values after launch and relaunch. |
| Compact dock | Reviewed the Android 16 install-over screenshot with legacy number 42 and the normal, 320dp/130% and 320dp/200% captures with number 00. The saved print is integrated into the jersey and the five destinations fit. |
| Native instrumentation | Twelve Android 16 tests passed. The suite covers a fresh named profile and recreation, offline drill recording and exact saved result, Progress and Quests, all eight garment views, and Jersey Studio persistence and all walkthrough steps at normal, 130% and 200% type. |
| Visual review | Reviewed the exact Android 16 Court, Train, Progress and Quests captures, four jersey constructions on both sides, and three dock captures. Some Studio evidence frames are scrolled. Store selection uses reviewed shots with full garment silhouettes. |
| Performance samples | Three cold starts on the Android 16 emulator were 1,548ms, 1,217ms and 1,289ms. One process snapshot measured 104,748 KiB total PSS. The captured app crash buffer was empty. These measurements describe the emulator. |

The [successful Android 16 run](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44455082702) and [exact artifact archive](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44455082702/artifacts/14498180510) include the APK, unsigned AAB, native screenshots, certificate, test XML, lint, audit and persistence receipts. An earlier run completed the upgrade and stopped at instrumentation due to its runner PATH. The corrected workflow passed all configured tests.

- APK SHA-256 `482b0ae00be62f78ece3051c20a31d88e3e6aaa1d87af0d02108ed5c861f597b`.
- AAB SHA-256 `5fa87ebcb4fc62f333d7de397ac43e1a66f0b4949600897af07589bf4840fe7c`.
- Store launch still requires an approved Play upload signer, Play App Signing confirmation, a decision for existing development-signed installs, policy answers and any account-specific testing.

The [Android 8 minimum-SDK run](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44455315142) passed with the same verified APK SHA-256. Its [evidence archive](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44455315142/artifacts/14497716520) contains a fresh install, relaunch, all five destinations, and a Court capture at a tablet-sized viewport. The enlarged viewport was letterboxed in the phone emulator framebuffer. A dedicated tablet capture set remains open for store use.

Physical phone testing, full TalkBack and switch/keyboard traversal, low-memory recovery, an exact Play-signed install, a 16 KB runtime device, and dedicated tablet Play listing shots remain open. The isolated solo scorekeeping and Mission interfaces retain their prior unit and historical device coverage.

## Published downloads

[Download the tested v55 phone APK](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-phone-update-v55/CourtQuestPro-Update-v55.apk) · [Download the Play listing package](https://github.com/Lamont-Labs/CourtQuestPro/releases/download/courtquest-phone-update-v55/CourtQuestPro-Play-Listing-v55.zip)

The [publication workflow](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44456793867) verified the Android 8 and Android 16 receipts, tagged the matching app and Gradle trees, and redownloaded the published APK for byte comparison. An independent redownload also matched the tested APK. The published listing ZIP contains six reviewed 1080 × 1920 RGB phone screenshots, the approved branding assets, copy, and policy worksheets. Its SHA-256 is `659eafbb4605fffb4e0851a7d935548e6ca96977989fbdd261ed49d4ad33eee6`.

The [release variant runtime run](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44456612982) used the validated unsigned bundle to produce an emulator-only install set with the existing test certificate. [Runtime evidence](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44456612982/artifacts/14497502778) confirms an Android 16 offline Library, background return, Jersey Studio, and a process relaunch. Play upload signing and Play-distributed APK verification require the authorized account and signatures.

The Figma Starter rate limit blocked a fresh design-file read. Jersey visual review used the owner reference, the approved Arena baseline, and actual Android captures. Automatic browser review rejected Play Console navigation on an unexpected origin. The repository work proceeded using Google’s public requirements.
