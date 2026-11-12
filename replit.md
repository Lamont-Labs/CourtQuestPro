# CourtQuest Pro

This is the existing **native Android** Kotlin / Jetpack Compose application.
Do not create a replacement web app or migrate the stack to make a browser
preview appear.

## Development baseline

The owner identified `dev/premium-flow-toolkit` as the intended development
branch. The initial import was on an older `main`; it was retained, not reset.
Stabilization work is isolated on `repair/premium-flow-stabilization`.
Read `AGENTS.md` and `DESIGN.md` before changes. Preserve the approved black /
bright-orange Signature design, side drawer, artwork, package, signer and
existing user-data behavior.

## Local checks

```sh
python3 -m unittest discover -s scripts -p 'test_android_flow_*.py' -v
python3 -m compileall -q scripts
bash -n scripts/*.sh
python3 -m json.tool tools/premium-flow/references.lock.json >/dev/null
git diff --check
```

## Android checks

With JDK 17 and Android SDK platform 35 / build tools 35.0.1 installed:

```sh
./gradlew --no-daemon --console=plain \
  :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

The imported Replit container has Python but no configured Android SDK,
`adb`, or accelerated emulator. No web run workflow is required.
GitHub Actions remains authoritative for Android compilation, lint,
unit tests, emulator journeys and APK verification.

Native journey scripts seed synthetic data and sometimes clear app data.
Run them only on the dedicated disposable QA emulator, never on a user's phone.
`scripts/run_premium_flow_checks.sh` enforces the `CourtQuest_PremiumQA`
emulator name before its destructive Maestro smoke flow.

The repair-branch push runs Android development checks plus library/training
and compact-drawer verification. It does not trigger either release workflow.
Do not merge, publish an APK, deploy, or update GitHub releases without separate
authorization.
