#!/usr/bin/env bash
# capture_session.sh -- 一键准备 + 流式抓取 CarPlayAPK 会话日志
#
# 用法:
#   bash skill/scripts/capture_session.sh <版本目录名> [apk路径]
#     例: bash skill/scripts/capture_session.sh v1.8.41 android-project/dist/Carplay-V1.8.41-debug.apk
#   bash skill/scripts/capture_session.sh --pull <版本目录名>     # 只把应用内连接日志拉回来
#
# 说明:
#   - 本脚本前台运行 logcat（不 nohup），由调用方用后台任务托管；终止任务即停止抓取。
#   - 日志落到 logs/<版本>/recapture.log，prefs 快照落 logs/<版本>/prefs-start.txt。
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PKG="io.carplay.app"

pull_only=0
if [ "${1:-}" = "--pull" ]; then
  pull_only=1; shift
fi

VER="${1:?用法: capture_session.sh <版本目录名> [apk路径]}"
APK="${2:-}"
OUT="$ROOT/logs/$VER"
mkdir -p "$OUT"

adb wait-for-device
echo "[capture] device: $(adb devices | sed -n '2p')"

if [ "$pull_only" = "1" ]; then
  adb shell "run-as $PKG ls files/" > "$OUT/app-files.txt" 2>&1
  for f in $(adb shell "run-as $PKG ls files/" 2>/dev/null | tr -d '\r' | grep -E '^connection-log'); do
    adb exec-out "run-as $PKG cat files/$f" > "$OUT/$f" 2>/dev/null
    echo "[capture] pulled $f -> $(wc -c < "$OUT/$f") bytes"
  done
  exit 0
fi

if [ -n "$APK" ] && [ -f "$APK" ]; then
  echo "[capture] installing $(basename "$APK")"
  adb install -r -d "$APK" 2>&1 | tail -2
fi

echo "[capture] version: $(adb shell dumpsys package $PKG | grep -m1 versionName | tr -d '\r')"

# 清空前把 prefs 存一份，便于确认各项开关状态
{
  echo "== carplayapk-debug =="
  adb shell "run-as $PKG cat shared_prefs/carplayapk-debug.xml" 2>&1
  echo
  echo "== carplayapk-local-transport =="
  adb shell "run-as $PKG cat shared_prefs/carplayapk-local-transport.xml" 2>&1
} | tr -d '\r' > "$OUT/prefs-start.txt"
echo "[capture] prefs -> prefs-start.txt"

# 应用内连接日志清空，保证本轮从零开始
adb shell "run-as $PKG sh -c 'rm -f files/connection-log-*.txt'" 2>/dev/null

adb logcat -G 64M 2>&1 | tail -1
adb logcat -c
echo "[capture] logcat buffer cleared, streaming -> logs/$VER/recapture.log"
echo "[capture] ---- 现在可以操作手机/车机 ----"

adb logcat -v threadtime
