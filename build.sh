#!/bin/bash
set -euo pipefail

ADDR="192.168.1.136:5555"
APK="app/build/outputs/apk/debug/app-debug.apk"

echo "==> Building ShowPic..."
./gradlew assembleDebug

if [ "${1:-}" = "--install" ]; then
    echo "==> Connecting to $ADDR..."
    adb connect "$ADDR" 2>/dev/null || true
    echo "==> Installing..."
    adb -s "$ADDR" install -r "$APK"
    echo "==> Launching..."
    adb -s "$ADDR" shell am start -n com.jocala.showpic/.MainActivity
fi
