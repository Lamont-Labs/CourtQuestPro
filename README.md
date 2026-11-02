# CourtQuest Pro

Native Android basketball training prototype built with Kotlin and Jetpack Compose.

## Download and install

Open [the latest Android release](https://github.com/Lamont-Labs/CourtQuestPro/releases/latest), expand **Assets**, and download **CourtQuestPro-1.0.apk**. Use your GitHub account with access to this private repository.

On an Android 8.0 or newer phone, open the APK and choose **Install**. Android may ask you to allow installation from the app opening the file.

If a release is still building, open [Android APK builds](https://github.com/Lamont-Labs/CourtQuestPro/actions/workflows/android-apk.yml). The workflow publishes a release only after compilation, Android lint, and APK verification succeed. Each successful run also keeps the APK and its evidence as a workflow artifact for 90 days.

## Application

- Dashboard with XP, rank, streak, and six daily drill slots.
- Six basketball skill categories and drill detail screens.
- YouTube search links for drill demonstrations.
- Five difficulty choices saved locally.
- Championship setup with player and opponent names.
- Manual one-on-one scorekeeper, first to 11.
- Sudden death at 10–10.
- Match recaps and local career history.
- Local progress storage through SharedPreferences.

The basketball application logic is the original supplied prototype. This repository adds a reproducible Android build and GitHub download delivery.

## Build

| Setting | Value |
| --- | --- |
| Package | `com.courtquestpro` |
| App version | 1.0 / version code 1 |
| Minimum Android | Android 8.0 / API 26 |
| Target / compile SDK | API 35 |
| Build tools | 35.0.1 |
| Java | JDK 17 |
| Gradle | 8.9, wrapper distribution SHA-256 pinned |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |

With JDK 17 and the Android SDK installed:

```bash
chmod +x gradlew
./gradlew --no-daemon :app:assembleDebug :app:lintDebug
python3 scripts/verify_apk.py \
  --apk app/build/outputs/apk/debug/app-debug.apk \
  --build-tools "$ANDROID_HOME/build-tools/35.0.1" \
  --output dist
```

Windows compilation:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

Android Studio can open the repository root directly.

## GitHub builds and downloads

The **Android APK** workflow runs after code is pushed to `main`. It can also be started from **Actions → Android APK → Run workflow**.

Each successful build publishes:

- The installable APK.
- SHA-256 checksum and machine-readable package evidence.
- Signature, ZIP alignment, and Android package inspection results.
- Android lint reports and the Gradle build log.

Release tags use `android-v1.0-buildN`. Releases are attached to the exact source commit. The workflow uses pinned GitHub Action commit SHAs and GitHub's temporary workflow token; no personal access token is needed.

## Development signing

`signing/debug.keystore` is the original **development-only** Android debug key. It is intentionally included in this private source repository so GitHub builds retain the original test APK's signing identity. It uses the standard debug alias `androiddebugkey` and standard debug passwords `android`.

Certificate SHA-256:

```text
25f8007f0a8503b01ded3f4856d02e830c64c823aa2c816da4d347ed5015f27b
```

The ignore rules permit this exact test key and exclude other keystores. This is not a production signing setup. A production release must use a separate protected release key. Increase `versionCode` and `versionName` for future application versions.

## Verification and remaining work

`build-evidence/` preserves the original November 1, 2026 build records. New GitHub builds produce fresh evidence attached to their own releases.

The original APK compiled, packaged, and signed successfully. GitHub repeats the build and packaging checks before publishing. Neither the original session nor this delivery has established successful emulator or physical-phone operation.

Known prototype limitations retained from the supplied source:

- Drill videos open external YouTube search pages.
- Saved difficulty does not filter daily drill selection.
- Undo removes a point based on the higher score instead of the actual last scorer.
- Daily slot generation, match XP display, streak behavior, and navigation still need functional testing.
