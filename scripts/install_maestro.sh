#!/usr/bin/env bash
set -euo pipefail
export MAESTRO_CLI_NO_ANALYTICS=1
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(dirname -- "$script_dir")
maestro_version=2.12.1
maestro_sha256=54dbb0716425626604973cbb374c585ca30272375d9ce15177a1152765ddd9d7
maestro_archive="$project_root/.tooling/downloads/maestro-$maestro_version.zip"
maestro_target="$project_root/.tooling/maestro/$maestro_version"
maestro_binary="$maestro_target/bin/maestro"

if [[ -x "$maestro_binary" ]]; then
    "$maestro_binary" --version
    exit 0
fi

mkdir -p "$(dirname -- "$maestro_archive")" "$(dirname -- "$maestro_target")"
if [[ ! -f "$maestro_archive" ]]; then
    curl --fail --location --silent --show-error --connect-timeout 20 --retry 2 \
        "https://github.com/mobile-dev-inc/Maestro/releases/download/cli-$maestro_version/maestro.zip" \
        -o "$maestro_archive.partial"
    mv -- "$maestro_archive.partial" "$maestro_archive"
fi
printf '%s  %s\n' "$maestro_sha256" "$maestro_archive" | sha256sum --check --status

maestro_stage=$(mktemp -d "$project_root/.tooling/maestro/install.XXXXXXXX")
trap 'rm -rf -- "$maestro_stage"' EXIT
unzip -q "$maestro_archive" -d "$maestro_stage"
test -f "$maestro_stage/maestro/bin/maestro"
chmod +x "$maestro_stage/maestro/bin/maestro"
mv -- "$maestro_stage/maestro" "$maestro_target"
"$maestro_binary" --version
