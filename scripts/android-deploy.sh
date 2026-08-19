#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STATE_DIR="$ROOT_DIR/.gradle/android-deploy"
LAST_PHYSICAL_DEVICE_FILE="$STATE_DIR/last-physical-device"
PACKAGE_NAME="fun.lifeupapp.calmanager"
LAUNCHER_ACTIVITY="$PACKAGE_NAME/.MainActivity"

variant="Debug"
apk_path=""
device=""
select_physical_device_prompt=0
skip_build=0
launch_app=1
fresh_install=0
install_flags=("-r" "-g")

usage() {
  cat <<'EOF'
Usage: scripts/android-deploy.sh [options]

Build and install Calendar Account Manager to a selected adb device.

Options:
  --variant <Debug|Release>    Gradle variant. Default: Debug
  --apk <path>                 Install this APK instead of auto-detecting build output.
  --device <serial>            adb device serial.
  --last-physical-device       Reuse the last successful physical device.
  --list-devices               List adb devices and exit.
  --select-physical-device     Select an online physical device, then install.
                               Auto-picks when exactly one physical device is online.
  --no-build                   Skip Gradle and install an existing APK.
  --launch                     Launch after install. Enabled by default.
  --no-launch                  Install only.
  --fresh                      Uninstall the existing package first (signature mismatch).
  --downgrade                  Allow versionCode downgrade (-d).
  -h, --help                   Show this help.
EOF
}

die() {
  printf 'error: %s\n' "$*" >&2
  exit 1
}

info() {
  printf '==> %s\n' "$*"
}

use_last_physical_device=0
list_devices=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --variant)
      [[ $# -ge 2 ]] || die "--variant requires a value"
      variant="$2"
      shift 2
      ;;
    --apk)
      [[ $# -ge 2 ]] || die "--apk requires a value"
      apk_path="$2"
      shift 2
      ;;
    --device)
      [[ $# -ge 2 ]] || die "--device requires a value"
      device="$2"
      shift 2
      ;;
    --last-physical-device)
      use_last_physical_device=1
      shift
      ;;
    --list-devices)
      list_devices=1
      shift
      ;;
    --select-physical-device)
      select_physical_device_prompt=1
      shift
      ;;
    --no-build)
      skip_build=1
      shift
      ;;
    --launch)
      launch_app=1
      shift
      ;;
    --no-launch)
      launch_app=0
      shift
      ;;
    --fresh)
      fresh_install=1
      shift
      ;;
    --downgrade)
      install_flags+=("-d")
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      die "unknown option: $1"
      ;;
  esac
done

find_adb() {
  local candidates=()
  [[ -n "${ANDROID_HOME:-}" ]] && candidates+=("$ANDROID_HOME/platform-tools/adb")
  [[ -n "${ANDROID_SDK_ROOT:-}" ]] && candidates+=("$ANDROID_SDK_ROOT/platform-tools/adb")
  candidates+=("$HOME/Library/Android/sdk/platform-tools/adb")
  local path
  for path in "${candidates[@]}"; do
    if [[ -x "$path" ]]; then
      printf '%s\n' "$path"
      return 0
    fi
  done
  command -v adb
}

ADB="$(find_adb)" || die "adb not found. Set ANDROID_HOME or ANDROID_SDK_ROOT."

list_adb_devices() {
  "$ADB" devices -l | awk 'NR > 1 && NF > 0 {print}'
}

print_devices() {
  info "adb: $ADB"
  local rows
  rows="$(list_adb_devices || true)"
  if [[ -z "$rows" ]]; then
    printf 'No adb devices found.\n'
    return
  fi
  printf '%s\n' "$rows" | while IFS= read -r row; do
    local serial state kind
    serial="$(awk '{print $1}' <<<"$row")"
    state="$(awk '{print $2}' <<<"$row")"
    if [[ "$serial" == emulator-* ]]; then
      kind="emulator"
    else
      kind="physical"
    fi
    printf '%-22s %-12s %s %s\n' "$serial" "$state" "$kind" "$(cut -d' ' -f3- <<<"$row")"
  done
}

online_physical_devices() {
  list_adb_devices | awk '$2 == "device" && $1 !~ /^emulator-/ {print $1}'
}

device_state() {
  local serial="$1"
  list_adb_devices | awk -v serial="$serial" '$1 == serial {print $2; exit}'
}

select_device() {
  if [[ -n "$device" ]]; then
    printf '%s\n' "$device"
    return
  fi
  if [[ "$use_last_physical_device" -eq 1 ]]; then
    [[ -f "$LAST_PHYSICAL_DEVICE_FILE" ]] || die "no last physical device recorded"
    tr -d '[:space:]' < "$LAST_PHYSICAL_DEVICE_FILE"
    return
  fi

  local devices=()
  while IFS= read -r line; do
    [[ -n "$line" ]] || continue
    devices+=("$line")
  done < <(online_physical_devices)

  case "${#devices[@]}" in
    0)
      print_devices
      die "no online physical device found"
      ;;
    1)
      printf '%s\n' "${devices[0]}"
      ;;
    *)
      local i=1 serial
      info "multiple physical devices online"
      for serial in "${devices[@]}"; do
        printf '%2d) %s\n' "$i" "$serial"
        ((i++))
      done
      printf '\nSelect device number: ' >&2
      local index
      read -r index
      [[ "$index" =~ ^[0-9]+$ ]] || die "invalid selection: $index"
      (( index >= 1 && index <= ${#devices[@]} )) || die "selection out of range: $index"
      printf '%s\n' "${devices[$((index - 1))]}"
      ;;
  esac
}

resolve_apk() {
  if [[ -n "$apk_path" ]]; then
    [[ "$apk_path" == /* ]] || apk_path="$ROOT_DIR/$apk_path"
    [[ -f "$apk_path" ]] || die "APK not found: $apk_path"
    printf '%s\n' "$apk_path"
    return
  fi
  local build_type
  build_type="$(tr '[:upper:]' '[:lower:]' <<<"${variant:0:1}")${variant:1}"
  local apk_dir="$ROOT_DIR/app/build/outputs/apk/$build_type"
  local found
  found="$(find "$apk_dir" -maxdepth 1 -type f -name '*.apk' ! -name '*androidTest*.apk' -print0 \
    | xargs -0 ls -t 2>/dev/null \
    | head -n 1 || true)"
  [[ -n "$found" ]] || die "no APK found in $apk_dir"
  printf '%s\n' "$found"
}

if [[ "$list_devices" -eq 1 && "$select_physical_device_prompt" -eq 0 ]]; then
  print_devices
  exit 0
fi

if [[ "$select_physical_device_prompt" -eq 0 && -z "$device" && "$use_last_physical_device" -eq 0 ]]; then
  select_physical_device_prompt=1
fi

selected_device="$(select_device)"
state="$(device_state "$selected_device")"
[[ "$state" == "device" ]] || die "device '$selected_device' is not online; current state: ${state:-missing}"
info "selected device: $selected_device"

if [[ "$skip_build" -eq 0 && -z "$apk_path" ]]; then
  info "building :app:assemble${variant}"
  (
    cd "$ROOT_DIR"
    export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || true)}"
    ./gradlew ":app:assemble${variant}" --no-daemon
  )
fi

resolved_apk="$(resolve_apk)"
info "installing $resolved_apk"

if [[ "$fresh_install" -eq 1 ]]; then
  info "uninstalling $PACKAGE_NAME"
  "$ADB" -s "$selected_device" uninstall "$PACKAGE_NAME" >/dev/null || true
fi

if ! "$ADB" -s "$selected_device" install "${install_flags[@]}" "$resolved_apk"; then
  die "install failed. If the device has a Play Store build, rerun with --fresh"
fi

mkdir -p "$STATE_DIR"
printf '%s\n' "$selected_device" > "$LAST_PHYSICAL_DEVICE_FILE"

if [[ "$launch_app" -eq 1 ]]; then
  info "launching $LAUNCHER_ACTIVITY"
  "$ADB" -s "$selected_device" shell am start \
    -a android.intent.action.MAIN \
    -c android.intent.category.LAUNCHER \
    -n "$LAUNCHER_ACTIVITY" >/dev/null
fi

info "done"
