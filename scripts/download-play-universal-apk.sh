#!/usr/bin/env bash
set -euo pipefail

APP="${GPC_APP:-fun.lifeupapp.calmanager}"
VERSION_CODE="${1:-}"
OUTPUT="${2:-}"
MAX_ATTEMPTS="${PLAY_APK_MAX_ATTEMPTS:-10}"
SLEEP_SECONDS="${PLAY_APK_RETRY_SECONDS:-30}"

die() {
  printf 'error: %s\n' "$*" >&2
  exit 1
}

if [[ -z "$VERSION_CODE" ]]; then
  VERSION_CODE="$(gpc --app "$APP" --output json releases status | jq -r '
    (map(select(.track == "production" and (.versionCodes | length) > 0)) | .[0].versionCodes[0])
    // (map(.versionCodes[]?) | map(tonumber) | max | tostring)
  ')"
fi

[[ "$VERSION_CODE" =~ ^[1-9][0-9]*$ ]] || die "could not resolve a Play versionCode"

if [[ -z "$OUTPUT" ]]; then
  OUTPUT="$PWD/calendar-account-manager-${VERSION_CODE}-play-signed-universal.apk"
fi

attempt=1
download_id=""
while (( attempt <= MAX_ATTEMPTS )); do
  listing="$(gpc --app "$APP" --output json generated-apks list "$VERSION_CODE")"
  download_id="$(jq -r '.[0].generatedUniversalApk.downloadId // empty' <<<"$listing")"
  if [[ -n "$download_id" ]]; then
    break
  fi
  if (( attempt == MAX_ATTEMPTS )); then
    die "universal APK not ready for versionCode $VERSION_CODE"
  fi
  echo "Play has not generated the universal APK yet; retrying in ${SLEEP_SECONDS}s ($attempt/${MAX_ATTEMPTS})"
  sleep "$SLEEP_SECONDS"
  attempt=$((attempt + 1))
done

# ponytail: gpc generated-apks download writes 0 bytes; Play media download needs alt=media
encoded_id="$(python3 -c 'import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1], safe=""))' "$download_id")"
url="https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${APP}/generatedApks/${VERSION_CODE}/downloads/${encoded_id}:download?alt=media"
token="$(gpc auth token)"
curl -sS -L --fail -o "$OUTPUT" -H "Authorization: Bearer ${token}" "$url"
test -s "$OUTPUT" || die "downloaded APK is empty: $OUTPUT"
printf '%s\n' "$OUTPUT"
