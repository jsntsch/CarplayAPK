#!/usr/bin/env bash
# 蓝牙「车机反复敲门」量化诊断
#
# 用法:
#   bash bt_flap_diagnose.sh              # 观察 30 秒
#   bash bt_flap_diagnose.sh 60           # 观察 60 秒
#   bash bt_flap_diagnose.sh --reset 30   # 先强制复位蓝牙(disable/enable)再观察
#
# 结论只有三种：
#   A) 只有 SDP、没有 RFCOMM  → 手机没在广播 iAP2 服务记录（SDP 记录已过期）
#   B) 有 RFCOMM、链路稳定     → 蓝牙段正常，问题在别处
#   C) 有 RFCOMM 但反复断      → 连错 SCN 或对端策略
#
# 判据依据见 references/troubleshooting.md 第 7 节。

set -u

ADB="${ADB:-adb}"
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

RESET=0
WINDOW=30
for arg in "$@"; do
    case "$arg" in
        --reset) RESET=1 ;;
        ''|*[!0-9]*) echo "忽略参数: $arg" >&2 ;;
        *) WINDOW="$arg" ;;
    esac
done

hr()   { printf '%s\n' "------------------------------------------------------------"; }
ok()   { printf '  [OK]   %s\n' "$1"; }
warn() { printf '  [WARN] %s\n' "$1"; }
bad()  { printf '  [FAIL] %s\n' "$1"; }

TMP="$(mktemp 2>/dev/null || echo /tmp/bt_flap.log)"
trap 'rm -f "$TMP"' EXIT

hr
echo "0) 设备与蓝牙状态"
hr
DEV="$("$ADB" devices | awk 'NR>1 && $2=="device"{print $1; exit}')"
if [ -z "$DEV" ]; then
    bad "未找到已授权的 adb 设备"; exit 1
fi
ok "设备 $DEV"

if [ "$RESET" = "1" ]; then
    echo "     强制复位蓝牙（svc 在 ColorOS 上会被秒拉回，这里用 bluetooth_manager）"
    "$ADB" shell "cmd bluetooth_manager disable" >/dev/null 2>&1
    sleep 10
    "$ADB" shell "cmd bluetooth_manager enable"  >/dev/null 2>&1
    sleep 12
    ok "复位完成，bluetooth_on=$("$ADB" shell 'settings get global bluetooth_on' | tr -d '\r')"
fi

hr
echo "1) 抓取 ${WINDOW}s 系统日志"
hr
"$ADB" shell "logcat -c" >/dev/null 2>&1
# 注意: 不要用 logcat -f 落盘再 pull，实测会漏写；直接拉全缓冲更可靠
sleep "$WINDOW"
"$ADB" shell "logcat -d -v time" > "$TMP" 2>/dev/null
TOTAL=$(wc -l < "$TMP" | tr -d ' ')
ok "抓取 $TOTAL 行"

hr
echo "2) ACL 打环指标"
hr
KNOCK=$(grep -c "on_incoming_connection" "$TMP" || true)
CONN=$(grep -c "BREDR Connected"        "$TMP" || true)
DISC=$(grep -c "BREDR Disconnected"     "$TMP" || true)
BYE=$(grep -c "OnClassicLinkDisconnected" "$TMP" || true)
printf '     车机敲门(on_incoming_connection) : %s\n' "${KNOCK:-0}"
printf '     ACL 建立(BREDR Connected)        : %s\n' "${CONN:-0}"
printf '     ACL 断开(BREDR Disconnected)     : %s\n' "${DISC:-0}"
printf '     经典链路断开回调                 : %s\n' "${BYE:-0}"

if [ "${KNOCK:-0}" -ge 10 ] 2>/dev/null; then
    bad "确认打环：${WINDOW}s 内敲门 ${KNOCK} 次（约 $(( WINDOW * 10 / (KNOCK>0?KNOCK:1) ))00ms 一次）"
else
    ok "无明显打环（敲门 ${KNOCK:-0} 次）"
fi

# 发起方判定
if grep -q "is local inistated: false" "$TMP"; then
    echo "     发起方: 远端(车机) —— 手机侧没有主动发起"
elif grep -q "initiator:remote" "$TMP"; then
    echo "     发起方: 远端(车机)"
fi

hr
echo "3) 断开原因"
hr
grep -oE "reason:[A-Z_]+\(0x[0-9a-fA-F]+\)" "$TMP" | sort | uniq -c | sort -rn | head -5 | sed 's/^/     /'
grep -q "REMOTE_USER_TERMINATED_CONNECTION" "$TMP" \
    && echo "     → 0x13 = 对端主动断开。结合敲门频率，通常是「连上了但拿不到服务」" \
    || true

hr
echo "4) L2CAP PSM 分布（决定性判据）"
hr
PSM_SDP=$(grep -c "psm: BT_PSM_SDP"     "$TMP" || true)
PSM_RFC=$(grep -c "psm: BT_PSM_RFCOMM"  "$TMP" || true)
printf '     BT_PSM_SDP    : %s\n' "${PSM_SDP:-0}"
printf '     BT_PSM_RFCOMM : %s\n' "${PSM_RFC:-0}"
grep -oE "psm: [A-Za-z_0-9]+" "$TMP" | sort | uniq -c | sort -rn | head -6 | sed 's/^/         /'

hr
echo "5) 服务与链路健康度"
hr
SRVC=$(grep -c "bta_dm_conn_srvcs.count=0" "$TMP" || true)
RFB=$(grep -c "RFCOMM_BufDataInd" "$TMP" || true)
HFP=$(grep -c "oplus_btif_hf: acl_disconnected: Invalid index" "$TMP" || true)
printf '     bta_dm_conn_srvcs.count=0 : %s  (一个 profile 都没起来)\n' "${SRVC:-0}"
printf '     RFCOMM_BufDataInd          : %s  (iAP2 通道有数据 = 链路真的通了)\n' "${RFB:-0}"
printf '     HFP 无效句柄               : %s\n' "${HFP:-0}"

hr
echo "6) 结论"
hr
if [ "${KNOCK:-0}" -ge 10 ] 2>/dev/null && [ "${PSM_RFC:-0}" -eq 0 ] 2>/dev/null; then
    bad "A) 手机没有在广播 iAP2 服务记录"
    echo "     车机做了 ${PSM_SDP:-0} 次 SDP 查询，但一次 RFCOMM 都没尝试 ——"
    echo "     它在 SDP 里查不到 00000000-deca-fade-deca-deafdecacafe 就挂断了。"
    echo
    echo "     检查 listenUsingRfcommWithServiceRecord 的监听窗口："
    echo "       常见写法是 90000 ms（90 秒），分布在三处 ——"
    echo "         1) accept 循环截止时间"
    echo "         2) postDelayed(关闭监听, V)"
    echo "         3) 截止时间戳 = elapsedRealtime() + V"
    echo "       三处必须一起改成 0x7fffffff，只改一处等于没改。"
    echo
    echo "     同时确认：应用进程活着（监听随进程存在），且已选中目标车机。"
elif [ "${RFB:-0}" -gt 0 ] 2>/dev/null && [ "${DISC:-0}" -le 2 ] 2>/dev/null; then
    ok "B) 蓝牙链路健康：RFCOMM 有数据、几乎无断开"
    echo "     → 蓝牙段不是瓶颈。转向 WiFi / AirPlay / 事件通道。"
elif [ "${PSM_RFC:-0}" -gt 0 ] 2>/dev/null; then
    warn "C) 有 RFCOMM 尝试但链路反复断"
    echo "     → 可能连错 SCN（isConnected=true 不代表连对服务，见铁律 5）"
    echo "     → 或车机对上次连接有残留状态，先 --reset 再来一次"
else
    warn "信息不足"
    echo "     → 确认车机处于「无线 CarPlay 待机」界面后，加大观察窗口重跑"
    echo "     → bash bt_flap_diagnose.sh --reset 120"
fi
hr
