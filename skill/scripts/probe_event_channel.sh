#!/usr/bin/env bash
# 事件通道取证：车机到底有没有往我们这边写字节？
#
# 用法:
#   bash probe_event_channel.sh               # 自动找车机 IP，探 5001
#   bash probe_event_channel.sh 192.168.5.1   # 指定车机 IP
#   bash probe_event_channel.sh 192.168.5.1 5001
#
# 做两件事:
#   1) 列出手机到车机的全部 TCP 连接，含状态与 tx:rx 队列深度
#      tx:rx 长期 0:0 = 双向都没在传（车机一个字节都没写）
#   2) 另开一条到 eventPort 的裸连接，看对端是否主动推数据
#      0 字节 = 与客户端实现无关，是对端策略/会话状态问题
#
# 背景见 references/verify-links.md

set -u

ADB="${ADB:-adb}"
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

CAR="${1:-}"
PORT="${2:-5001}"

hr()   { printf '%s\n' "------------------------------------------------------------"; }
ok()   { printf '  [OK]   %s\n' "$1"; }
warn() { printf '  [WARN] %s\n' "$1"; }
bad()  { printf '  [FAIL] %s\n' "$1"; }

# 192.168.5.1 -> 0105A8C0（/proc/net/tcp 用大端值的字节倒序表示）
ip_to_hex() {
    local a b c d
    IFS=. read -r a b c d <<< "$1"
    printf '%02X%02X%02X%02X' "$d" "$c" "$b" "$a"
}

state_name() {
    case "$1" in
        01) echo "ESTABLISHED" ;;
        02) echo "SYN_SENT"    ;;
        03) echo "SYN_RECV"    ;;
        04) echo "FIN_WAIT1"   ;;
        05) echo "FIN_WAIT2"   ;;
        06) echo "TIME_WAIT"   ;;
        07) echo "CLOSE"       ;;
        08) echo "CLOSE_WAIT"  ;;
        09) echo "LAST_ACK"    ;;
        0A) echo "LISTEN"      ;;
        0B) echo "CLOSING"     ;;
        *)  echo "st=$1"       ;;
    esac
}

hr
echo "0) 设备与网络"
hr
DEV="$("$ADB" devices | awk 'NR>1 && $2=="device"{print $1; exit}')"
if [ -z "$DEV" ]; then bad "未找到已授权的 adb 设备"; exit 1; fi
ok "设备 $DEV"

WLAN="$("$ADB" shell "ip -4 -o addr show wlan0" 2>/dev/null | awk '{print $4}')"
if [ -n "$WLAN" ]; then
    ok "wlan0 = $WLAN"
else
    warn "wlan0 无 IPv4 地址 —— 车机热点可能已关闭（会话结束时车机常把热点一起收掉）"
    echo "         → 需要先在车机上重新触发无线投屏"
fi

if [ -z "$CAR" ]; then
    CAR="$("$ADB" shell "ip route" 2>/dev/null | awk '/^192\.168\./{print $3; exit}')"
    [ -z "$CAR" ] && CAR="192.168.5.1"
fi
HEX="$(ip_to_hex "$CAR")"
ok "车机 $CAR  →  /proc/net/tcp 表示: $HEX"

hr
echo "1) 到车机的 TCP 连接（含 tx:rx 队列）"
hr
RAW="$("$ADB" shell "cat /proc/net/tcp /proc/net/tcp6" 2>/dev/null | grep -i "$HEX" || true)"
if [ -z "$RAW" ]; then
    bad "没有任何到 $CAR 的连接"
    echo "     → 会话未建立，或已完全关闭（连 TIME_WAIT 都没有）"
else
    printf '     %-14s %-24s %-24s %s\n' "状态" "本地" "远端" "tx:rx"
    printf '%s\n' "$RAW" | while read -r _sl loc rem st txrx _rest; do
        printf '     %-14s %-24s %-24s %s\n' "$(state_name "$st")" "$loc" "$rem" "$txrx"
    done
    echo
    echo "     端口参考: 5001(hex 1389)=事件  6030(178E)=屏幕流  7000(1B58)=RTSP"
    echo "     tx:rx 是滞留字节数，不是累计量；长期 0:0 = 双向都没在传。"
fi

hr
echo "2) 裸探测 eventPort $CAR:$PORT"
hr
echo "     另开一条独立连接，看对端是否会主动推字节（不依赖被测 App）"
BYTES="$("$ADB" shell "cd /data/local/tmp; rm -f evprobe.bin; timeout 12 nc -w 8 $CAR $PORT > evprobe.bin 2>/dev/null; wc -c < evprobe.bin" 2>/dev/null | tr -d ' \r')"
BYTES="${BYTES:-0}"
printf '     收到字节数: %s\n' "$BYTES"

if [ "$BYTES" -gt 0 ] 2>/dev/null; then
    ok "对端会主动推送数据 —— 事件通道本身是活的"
    "$ADB" shell "cd /data/local/tmp; od -An -tx1 evprobe.bin | head -4" 2>/dev/null | sed 's/^/         /'
    echo "         → 若被测 App 仍 raw_bytes=0，问题在 App 侧："
    echo "           · 连的可能不是这条 socket"
    echo "           · 或读循环卡在别的阶段（看 stage= 字段）"
else
    warn "8 秒内 0 字节"
    echo "         → 排除「我们这条 socket 特殊」：车机对该端口任何新连接都不主动发。"
    echo "         → 这是对端策略或会话状态问题，不是客户端实现问题。"
    echo "         → 接着查：会话是否完整（audio stream 是否 SETUP）、"
    echo "           或事件通道上是否从未成功发出过任何一条命令。"
fi

hr
echo "3) App 侧旁证"
hr
PID="$("$ADB" shell "pidof io.carplay.app" 2>/dev/null | tr -d '\r')"
if [ -n "$PID" ]; then
    ok "App 进程 $PID"
    FDS="$("$ADB" shell "ls -l /proc/$PID/fd 2>/dev/null | grep -c socket")"
    echo "     socket fd 数: ${FDS:-?}"
else
    warn "App 未运行（换包名自行调整本脚本第 3 段）"
fi

hr
echo "取证完成。判断顺序："
echo "  1) 无任何连接        → 会话没建立，先回 SKILL.md 步骤 1"
echo "  2) 有连接但 tx:rx=0:0 → 车机没写字节，看第 2 段裸探测结果"
echo "  3) 裸露探测也有数据   → 问题在 App 的读路径，不在对端"
hr
