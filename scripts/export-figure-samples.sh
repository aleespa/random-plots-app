#!/usr/bin/env bash
# Renders every figure on the connected device and pulls 1000x1000 WebP Browse samples into
# build/figure-samples/<key>.webp. Copy the ones you want into app/src/main/res/drawable/.
#
# Usage: scripts/export-figure-samples.sh [adb-serial]
# Needs a device/emulator with OpenGL ES 3.1 (use a recent system image with a host GPU).
#
# Runs the test with `am instrument` instead of connectedDebugAndroidTest, because Gradle
# uninstalls the app afterwards and that deletes the exported files.
set -euo pipefail
cd "$(dirname "$0")/.."
# Git Bash on Windows rewrites /sdcard/... arguments into Windows paths; keep them as-is.
export MSYS_NO_PATHCONV=1

ADB=(adb)
if [[ $# -ge 1 ]]; then ADB=(adb -s "$1"); fi

PKG=com.aleespa.randomsquare
REMOTE=/sdcard/Android/data/$PKG/files/figure-samples
OUT=build/figure-samples

./gradlew installDebug installDebugAndroidTest --console=plain -q
"${ADB[@]}" shell rm -rf "$REMOTE"
"${ADB[@]}" shell am instrument -w \
    -e class $PKG.FigureRenderTest \
    -e exportSamples true \
    $PKG.test/androidx.test.runner.AndroidJUnitRunner

rm -rf "$OUT" && mkdir -p "$OUT"
"${ADB[@]}" pull "$REMOTE/." "$OUT"
echo "Samples written to $OUT:"
ls "$OUT"
