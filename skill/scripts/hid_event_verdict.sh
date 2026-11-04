#!/usr/bin/env bash
# hid_event_verdict.sh -- 抓 HID 通道判定日志并直接给结论
#
# 用途：验证「车机是否接受在事件通道上收 POST /command」，
#       判读表见 references/verify-links.md 第 5 节「触摸」。
#
# 用法：
#   ./hid_event_verdict.sh            # 抓 90 秒，边抓边判
#   ./hid_event_verdict.sh 60         # 抓 60 秒
#   ./hid_event_verdict.sh --tail     # 只看已存在的缓冲，不重新抓
set -uo pipefail

WIN="${1:-90}"
ADB="${ADB:-adb}"

if ! "${ADB}" get-state >/dev/null 2>&1; then
    echo "[!] 没有 adb 设备。先插 USB 并确认 'adb devices'。" >&2
    exit 1
fi

LOG=$(mktemp -t hid_event.XXXXXX.log)
trap 'rm -f "$LOG"' EXIT

if [ "$WIN" = "--tail" ]; then
    echo "==> 从现有缓冲抓取"
    "${ADB}" logcat -d > "$LOG" 2>/dev/null
else
    echo "==> 清缓冲，抓取 ${WIN}s（期间请在车机上点几下屏幕）"
    "${ADB}" logcat -c 2>/dev/null
    "${ADB}" logcat -v time > "$LOG" 2>/dev/null &
    LPID=$!
    sleep "$WIN"
    kill "$LPID" 2>/dev/null
    wait "$LPID" 2>/dev/null
fi

# 注意：不要用 -s TAG:I，会把 Log.d 整条滤掉（铁律 12）
grep -aE "HID_INPUT_MODE_|HID_EVENT_|HID_RETRY|HID_REPORT|CARPLAY_EVENT|CARPLAY_CAPS|HID_CAPABILITIES|HID_TOUCH" "$LOG" \
    | sed -E 's/^[0-9-]+ [0-9:.]+ +[0-9]+ +[0-9]+ //' > "$LOG.f"

echo
echo "================ 关键行 ================"
if [ -s "$LOG.f" ]; then
    cat "$LOG.f"
else
    echo "(无匹配行 —— 应用没打过插桩，或这一版不是 V1.3.0)"
fi

echo
echo "================ 结论 ================"
SENT_EV=$(grep -c "HID_INPUT_MODE_SENT.*carrier=EVENT" "$LOG.f" 2>/dev/null || echo 0)
SENT_CT=$(grep -c "HID_INPUT_MODE_SENT.*carrier=CONTROL" "$LOG.f" 2>/dev/null || echo 0)
ACC_EV=$(grep -c "HID_INPUT_MODE_ACCEPTED.*carrier=EVENT" "$LOG.f" 2>/dev/null || echo 0)
ACC_CT=$(grep -c "HID_INPUT_MODE_ACCEPTED.*carrier=CONTROL" "$LOG.f" 2>/dev/null || echo 0)
UNC_EV=$(grep -c "HID_INPUT_MODE_UNCONFIRMED.*carrier=EVENT" "$LOG.f" 2>/dev/null || echo 0)
REJECT=$(grep -c "HID_EVENT_REJECTED" "$LOG.f" 2>/dev/null || echo 0)
LOCK=$(grep -c "HID_EVENT_ACCEPTED" "$LOG.f" 2>/dev/null || echo 0)
REPORT=$(grep -c "HID_REPORT_RECEIVED" "$LOG.f" 2>/dev/null || echo 0)

echo "SENT   event=$SENT_EV  control=$SENT_CT"
echo "ACCEPT event=$ACC_EV  control=$ACC_CT"
echo "EVENT fail=$UNC_EV  rejected_gate=$REJECT  locked=$LOCK"
echo "HID_REPORT_RECEIVED=$REPORT"
echo

if [ "$LOCK" -gt 0 ]; then
    if [ "$REPORT" -gt 0 ]; then
        echo "结论 A: 事件通道已锁定且车机在推 HID report —— 触摸链路通了。"
    else
        echo "结论 B: 车机接受事件通道上的 POST /command（200），但一个 HID report 都没推。"
        echo "        通道选择已排除，转查会话完整性：audio stream 是否 SETUP、"
        echo "        AUDIO_MAIN_MODES_CONFIRMED 是否出现（见 references/verify-links.md §4）。"
    fi
elif [ "$REJECT" -gt 0 ]; then
    echo "结论 C: 车机不接受事件通道上的 POST /command（连续 2 次失败已自动回退控制通道）。"
    echo "        这说明 HID 命令必须留在控制通道，触摸根因不在通道选择上。"
    echo "        下一步查：车机为何不推 /command hidSendReport（audio stream / 会话完整性）。"
elif [ "$SENT_EV" -gt 0 ] && [ "$ACC_EV" -eq 0 ]; then
    echo "结论 D: 事件通道上发出去了但一直在等响应（无 ACCEPTED / 无 UNCONFIRMED）。"
    echo "        检查 y5 的 z() 是否已调用（门禁 t==true，铁律 15），以及 5s 超时是否触发。"
elif [ "$SENT_EV" -eq 0 ]; then
    echo "结论 E: 一次都没在事件通道上发出。"
    echo "        可能性：重发补丁没生效（版本不对）、P0 心跳没跑、或 EVENT_OK 一开始就被置 false。"
    echo "        先确认：adb shell dumpsys package io.carplay.app | grep versionName"
else
    echo "结论 F: 数据不足以判断，延长抓取窗口，并在车机上点几下屏幕再试。"
fi
