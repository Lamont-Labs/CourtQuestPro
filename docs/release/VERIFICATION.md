# CourtQuest Pro v55 verification

The v55 app source built at [f12b362e5ede67cfef54894c27abb907d4ecb23d](https://github.com/Lamont-Labs/CourtQuestPro/commit/f12b362e5ede67cfef54894c27abb907d4ecb23d). The staged package remains `com.courtquestpro`, version name `1.0`, version code `55`, target SDK 36, and minimum SDK 26. The source and release documentation are in [draft pull request #8](https://github.com/Lamont-Labs/CourtQuestPro/pull/8).

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
| Compact dock | Reviewed the Android 16 install-over screenshot with saved jersey number 42 in the garment and the low-profile five-tab dock. |
| First-run and all eight views | The main instrumentation stage in the first APK/AAB run stopped before running a test because the runner shell lacked `adb` on PATH. The workflow now adds the SDK platform tools to PATH and reruns the harness. |

The [build, lint, bundle and Android 16 upgrade artifacts](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44454237293/artifacts/14497128351) include the binary, AAB, Android captures, certificate, test XML, lint, advisory report and build receipts. The workflow's overall failure belongs to the subsequent instrumentation launcher. [Run log](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44454237293)

- APK SHA-256 `482b0ae00be62f78ece3051c20a31d88e3e6aaa1d87af0d02108ed5c861f597b`.
- AAB SHA-256 `87b5229f911cc6764d4925f29f07ca38451c4a449658b165e28838e49e943b5c`.
- Current Android 16 regression [run 44455082702](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44455082702).
- Store launch still requires an approved Play upload signer, Play App Signing confirmation, a decision for existing development-signed installs, policy answers and any account-specific testing.

The [Android 8 minimum-SDK run](https://github.com/Lamont-Labs/CourtQuestPro/actions/runs/44455315142) reuses the verified APK bytes on a separate emulator and checks the five destinations on the older system, including a tablet-sized Court capture.
