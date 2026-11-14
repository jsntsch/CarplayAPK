#!/system/bin/sh
# eir_inject.sh — OPPO 经典蓝牙 EIR 注入 128-bit CarPlay UUID（PoC / 裸 HCI）
#
# 目的：向控制器写入 Extended Inquiry Response，使其在被车机 inquiry 时
#       广播 128-bit Service UUID 2D8D2466-E14D-451C-88BC-7301ABEA291A，
#       验证 v1861 根因（车机靠 EIR 的 0x07 段甄别 CarPlay 设备）。
#
# 前置：
#   1) 设备已 root（OPPO 基本需要 BL 解锁 + Magisk）。
#   2) 存在 hcitool（BlueZ 工具；OPPO 原厂通常没有，需 busybox/toybox 或自编译）。
#   3) 手机置为可发现（discoverable）——见下方 auto_piscan。
#   4) App 的 BtDiscovery RFCOMM 三条 SDP 记录已就绪（否则车机即便看到 EIR 也连不上 iAP2）。
#
# 重要：AOSP 蓝牙栈会在可发现模式变化/服务变更时重写 EIR，覆盖本次写入。
#       因此必须"循环重发"以覆盖车机搜索窗口（默认 200 次 × 0.15s ≈ 30s）。
#       —— 这是 PoC 而非生产方案；生产见 Magisk 模块（hook BTM_WriteEIR）。
#
# 用法：
#   sh eir_inject.sh            # 默认循环 200 次，间隔 0.15s
#   sh eir_inject.sh 400 0.1    # 循环 400 次，间隔 0.1s
#   sh eir_inject.sh once       # 只发一次（用于抓包验证 EIR 是否生效）

set -u

# ---- 目标 UUID 的 EIR 编码（little-endian，规范串整体反转） ----
# 规范串 : 2D8D2466-E14D-451C-88BC-7301ABEA291A
# EIR LE : 1a 29 ea ab 01 73 bc 88 1c 45 4d e1 66 24 8d 2d
UUID_LE="1a 29 ea ab 01 73 bc 88 1c 45 4d e1 66 24 8d 2d"

# EIR TLV：Length=17(0x11) + Type=0x07(128-bit complete) + 16 字节 UUID
TLV="11 07 ${UUID_LE}"

# FEC_Required = 0x00，其后跟 240 字节 EIR（TLV 打头，其余 0 填充）
EIR_LEN=240
TLV_BYTES=18   # 1(len)+1(type)+16(uuid)

build_eir_payload() {
  # 输出：00 + TLV + 0 填充到 240 字节
  printf "00 "
  printf "%s " ${TLV}
  # 剩余填充 0x00
  remain=$((EIR_LEN - TLV_BYTES))
  i=0
  while [ $i -lt $remain ]; do printf "00 "; i=$((i+1)); done
}

EIR_PAYLOAD=$(build_eir_payload)

# ---- 命令构造：hcitool cmd 0x03 0x0052 <241 字节参数> ----
CMD="hcitool cmd 0x03 0x0052 ${EIR_PAYLOAD}"

do_inject_once() {
  # 静默执行；hcitool 无输出即成功（可用 btmon 验证）
  eval "${CMD}" >/dev/null 2>&1
  return $?
}

auto_piscan() {
  # 置可发现（可连接 + 可发现）。失败不致命（可能已被 App/系统设置）。
  hciconfig hci0 piscan >/dev/null 2>&1
}

verify_tool() {
  if ! command -v hcitool >/dev/null 2>&1; then
    echo "ERROR: hcitool 不可用。OPPO 原厂无此工具，需 busybox/toybox 或自编译 BlueZ hcitool。"
    echo "       也可改用原生 HCI socket 注入（见 oppo-eir-128bit-uuid-rewrite.md §6.1）。"
    exit 2
  fi
}

case "${1:-loop}" in
  once)
    verify_tool
    auto_piscan
    do_inject_once
    echo "injected once. 用 'btmon' 或另一台 bluetoothctl scan 抓本机 EIR 验证 0x07 段。"
    ;;
  loop|*)
    COUNT="${1:-200}"
    GAP="${2:-0.15}"
    verify_tool
    auto_piscan
    echo "循环注入 ${COUNT} 次，间隔 ${GAP}s（覆盖车机搜索窗口）。Ctrl-C 可中断。"
    i=0
    while [ $i -lt "$COUNT" ]; do
      do_inject_once
      i=$((i+1))
      sleep "$GAP" 2>/dev/null || sleep 0.15
    done
    echo "done. 若车机在窗口内搜到本机并登记为 CarPlay 设备 => v1861 根因坐实。"
    ;;
esac
