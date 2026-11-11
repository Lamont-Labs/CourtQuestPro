#!/usr/bin/env bash
set -euo pipefail
export MAESTRO_CLI_NO_ANALYTICS=1
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true

qa_serial=${1:-}
qa_apk=${2:-}
if [[ ! "$qa_serial" =~ ^emulator-[0-9]+$ ]]; then
    printf '%s\n' 'Refusing to reset app data: pass a dedicated CourtQuest_PremiumQA emulator serial.' >&2
    exit 2
fi
if [[ ! -f "$qa_apk" ]]; then
    printf '%s\n' 'Pass the candidate APK as the second argument.' >&2
    exit 2
fi
command -v adb >/dev/null
qa_avd=$(adb -s "$qa_serial" emu avd name | tr -d '\r' | head -n 1)
if [[ "$qa_avd" != CourtQuest_PremiumQA ]]; then
    printf '%s\n' 'Refusing to reset app data: this emulator is not CourtQuest_PremiumQA.' >&2
    exit 2
fi

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(dirname -- "$script_dir")
qa_apk=$(realpath -- "$qa_apk")
bash "$script_dir/install_maestro.sh"
qa_maestro="$project_root/.tooling/maestro/2.12.1/bin/maestro"
qa_evidence="$project_root/artifacts/premium-flow-toolkit"
mkdir -p "$qa_evidence"
adb -s "$qa_serial" install -r "$qa_apk"
cd -- "$project_root"
exec "$qa_maestro" --device "$qa_serial" test \
    --format junit --output "$qa_evidence/report.xml" \
    --test-output-dir "$qa_evidence" \
    -e EVIDENCE_DIR="$qa_evidence" .maestro/premium-smoke.yaml
