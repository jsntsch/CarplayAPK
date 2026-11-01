#!/usr/bin/env bash
# 无线 CarPlay 链路一键体检
# 用法: bash diagnose_adb.sh [车机热点关键词，默认 buick]
#
# 检查顺序与排障顺序一致：车机待机 → WiFi → 蓝牙 ACL → RFCOMM 通道 → 已保存网络。
# 全部通过后再谈协议调试，否则一定白做。

set -u

KEYWORD="${1:-buick}"
ADB="${ADB:-adb}"

# Windows Git Bash 下 adb shell 传 /sdcard 路径会被转成 Windows 路径
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL='*'

hr() { printf '%s\n' "------------------------------------------------------------"; }
ok() { printf '  [OK]   %s\n' "$1"; }
warn() { printf '  [WARN] %s\n' "$1"; }
bad() { printf '  [FAIL] %s\n' "$1"; }

hr
echo "1) 设备连接"
hr
DEV="$("$ADB" devices | awk 'NR>1 && $2=="device"{print $1; exit}')"
if [ -z "$DEV" ]; then
    bad "未找到已授权的 adb 设备"
    echo "     先执行: adb devices 并在手机上确认授权"
    exit 1
fi
ok "设备 $DEV"

hr
echo "2) 车机是否处于「无线 CarPlay 待机」"
hr
SCAN="$("$ADB" shell "cmd wifi list-scan-results" 2>/dev/null)"
HIT="$(printf '%s\n' "$SCAN" | grep -i "$KEYWORD" || true)"
if [ -n "$HIT" ]; then
    ok "扫到车机热点"
    printf '%s\n' "$HIT" | sed -n '1,4p' | sed 's/^/         /'
    echo "     → 同一热点出现多个 BSSID 属正常（2.4G/5G 双频）"
else
    bad "扫不到含「$KEYWORD」的热点"
    echo "     车机未进入 CarPlay / 手机互联界面。此时车机仍会接受 RFCOMM，"
    echo "     但 iAP2 状态机没启动，表现为 0 字节 + 1s 后 Broken pipe。"
    echo "     → 先去车机上把界面切到 Apple CarPlay，再回来重跑本脚本"
fi

hr
echo "3) WiFi 状态与地址"
hr
"$ADB" shell "cmd wifi status" 2>/dev/null | grep -iE "Wifi is|WifiInfo" | sed 's/^/     /' | head -3
GW="$("$ADB" shell "ip route" 2>/dev/null | awk '/192\.168\.|10\.0\.|172\./ {print}' | head -5)"
echo "     路由:"; printf '%s\n' "$GW" | sed 's/^/         /'
if printf '%s\n' "$GW" | grep -q "rndis0\|vgate0"; then
    warn "存在 rndis0/vgate0 路由（USB 共享或虚拟网卡）"
    echo "         → adb shell 的 ping/nc 可能走错接口，得出「车机不可达」的假结论"
    echo "         → 连通性必须由 App 在目标网络内自己探，不要用 adb shell 判定"
fi

hr
echo "4) 蓝牙 ACL"
hr
ACL="$("$ADB" shell "dumpsys bluetooth_manager" 2>/dev/null | grep -c "ACL BR/EDR:Y")"
if [ "${ACL:-0}" -gt 0 ] 2>/dev/null; then
    ok "存在已建立的 ACL 链路（车机可能主动回连）"
else
    warn "无 ACL 链路 —— 车机可能仍认为上一条连接活着"
    echo "         → 重置: adb shell svc bluetooth disable; sleep 20; adb shell svc bluetooth enable"
fi

hr
echo "5) RFCOMM 通道"
hr
SCN="$("$ADB" shell "dumpsys bluetooth_manager" 2>/dev/null | grep -iE "SCN|scn=" | head -8)"
if [ -n "$SCN" ]; then
    printf '%s\n' "$SCN" | sed 's/^/     /'
    if printf '%s\n' "$SCN" | grep -q "STATE_DISCONNECTED scn=0"; then
        bad "SDP 查不到通道号（scn=0）"
        echo "         → OPPO/ColorOS 会清 SDP 缓存，现场查询被 CONTROLLER_BUSY 打断"
        echo "         → 先 fetchUuidsWithSdp() 预热，再反射 getServiceChannel() 直连"
    fi
else
    warn "dumpsys 无通道信息（可能从未建立过 RFCOMM）"
fi
echo "     注意: SCN 动态分配，出现过 5/6/7；isConnected=true 不代表连对服务，"
echo "           唯一判据是连上后 2~3s 内收到 FF 55 开头的帧。"

hr
echo "6) 已保存的网络（判断是否需要密码）"
hr
"$ADB" shell "cmd wifi list-networks" 2>/dev/null | head -6 | sed 's/^/     /'
echo "     ephemeral 网络（Suggestion 创建）不会出现在上面的列表里，属正常。"

hr
echo "体检完成。2) 不通过时不要继续后续协议调试。"
hr
