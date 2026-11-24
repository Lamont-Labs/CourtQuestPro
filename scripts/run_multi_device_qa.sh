#!/usr/bin/env bash
set -euo pipefail
mkdir -p qa-evidence
pkg=com.courtquestpro
[[ "$(adb get-serialno)" =~ ^emulator-[0-9]+$ ]]
[[ "$(adb shell getprop ro.kernel.qemu | tr -d '\r')" == 1 ]]
case "$QA_SUITE" in NavigationTourTest|TrainingEntryTest|CardStudioRebuildTest|WelcomeAnimationVisualTest#swipeShotCapturesLaunchApexRimNetAndExitOnAndroid|WelcomeAnimationVisualTest#reducedMotionTapSkipsFlightAndStillEntersTheApp) ;; *) exit 2;; esac
case "$QA_SCALE" in 1.0|2.0) ;; *) exit 2;; esac
collect() {
  adb exec-out screencap -p > qa-evidence/final.png || true
  adb logcat -d -b crash > qa-evidence/crashes.txt || true
  adb shell uiautomator dump /sdcard/qa.xml >/dev/null 2>&1 && adb pull /sdcard/qa.xml qa-evidence/hierarchy.xml >/dev/null || true
  for folder in player-card-proof app-quality-proof; do
    adb exec-out run-as "$pkg" tar -cf - "files/$folder" > "qa-evidence/$folder.tar" 2>/dev/null || rm -f "qa-evidence/$folder.tar"
  done
}
trap collect EXIT
adb install app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell settings put system font_scale "$QA_SCALE"
adb shell cmd uimode night yes
if [[ "$QA_ID" == welcome-motion ]]; then
  adb shell settings put global animator_duration_scale 1
  adb shell settings put global transition_animation_scale 1
  adb shell settings put global window_animation_scale 1
elif [[ "$QA_ID" == welcome-reduced ]]; then
  adb shell settings put global animator_duration_scale 0
fi
adb shell settings get global animator_duration_scale > qa-evidence/animator-scale.txt
adb logcat -c
printf 'profile=%s\nfont_scale=%s\nsuite=%s\ncommit=%s\n' "$QA_ID" "$QA_SCALE" "$QA_SUITE" "$GITHUB_SHA" > qa-evidence/device.txt
adb shell wm size >> qa-evidence/device.txt
adb shell wm density >> qa-evidence/device.txt
adb shell getprop ro.build.version.sdk >> qa-evidence/device.txt
runner=$(adb shell pm list instrumentation | tr -d '\r' | awk '/target=com.courtquestpro\)/ {sub(/^instrumentation:/,"",$1); print $1; exit}')
test -n "$runner"
# Each device starts with a fresh synthetic app installation.
adb shell am instrument -w -r -e class "$pkg.$QA_SUITE" "$runner" | tee qa-evidence/instrumentation.txt
case "$QA_SUITE" in
  CardStudioRebuildTest) expected=5 ;;
  *) expected=1 ;;
esac
if [[ "$expected" == 1 ]]; then
  grep -Fxq 'OK (1 test)' qa-evidence/instrumentation.txt
else
  grep -Fxq "OK ($expected tests)" qa-evidence/instrumentation.txt
fi
grep -Eq '^INSTRUMENTATION_CODE: -1[[:space:]]*$' qa-evidence/instrumentation.txt
if grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED' qa-evidence/instrumentation.txt; then
  exit 1
fi
