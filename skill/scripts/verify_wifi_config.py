#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
iAP2 WiFi 凭据协商自检（0x5700/0x5701/0x5702/0x5703 + 0x4300）

对齐关系：
  - Kotlin: core/iap2/WifiConfig.kt        (parse / encodeForTest / Security.fromWireValue)
  - Kotlin: core/iap2/Iap2MessageType.kt   (ALWAYS_SENDABLE / maySend)
  - Kotlin: core/session/CarPlaySessionStateMachine.kt (requestWifiCredentials)
  - 参考实现: refs/wifi.py (wiomoc/iap2)

运行:
  python tools/verify_wifi_config.py
"""

import sys

# ---------------------------------------------------------------- 规范常量
MSG_DEVICE_TRANSPORT_ID = 0x4300
MSG_REQ_WIFI_INFO = 0x5700
MSG_WIFI_INFO = 0x5701
MSG_REQ_WIFI_CONFIG = 0x5702
MSG_WIFI_CONFIG = 0x5703

# 车机 1D01 实测声明的可接收表（official_capture.log）
PEER_ADVERTISED = {0x4E09, 0x4E0A, 0x4E0B, 0x4E0C, 0x4E0D, 0x4E0E, 0xEA00, 0xEA01}

ALWAYS_SENDABLE = {
    0x1D00, 0x1D01, 0x1D02, 0x1D03,
    0xAA00, 0xAA01, 0xAA02, 0xAA03, 0xAA04, 0xAA05,
    0x4300, 0x4301, 0x5700, 0x5701, 0x5702, 0x5703,
    0x4E09, 0x4E0A, 0x4E0B, 0x4E0C, 0x4E0D, 0x4E0E,
    0xEA00, 0xEA01,
}

SEC_NONE, SEC_WEP, SEC_WPA_WPA2 = 0, 1, 2

failures = []


def check(cond, label, detail=""):
    if cond:
        print(f"   ✓ {label}")
    else:
        print(f"   ✗ {label}  {detail}")
        failures.append(label)


# ---------------------------------------------------------------- TLV 编解码
def tlv(pid, value: bytes) -> bytes:
    """规范格式：[length 2B][paramId 2B][value]，length 含 4 字节头"""
    total = 4 + len(value)
    return total.to_bytes(2, "big") + pid.to_bytes(2, "big") + value


def csm(msg_id, params: bytes = b"") -> bytes:
    total = 6 + len(params)
    return (0x4040).to_bytes(2, "big") + total.to_bytes(2, "big") + msg_id.to_bytes(2, "big") + params


def decode_len_first(data: bytes):
    out, i = [], 0
    while i + 4 <= len(data):
        ln = int.from_bytes(data[i:i + 2], "big")
        pid = int.from_bytes(data[i + 2:i + 4], "big")
        if ln < 4 or i + ln > len(data):
            return None
        out.append((pid, data[i + 4:i + ln]))
        i += ln
    if i != len(data) or not out:
        return None
    return out


def decode_id_first(data: bytes):
    out, i = [], 0
    while i + 4 <= len(data):
        pid = int.from_bytes(data[i:i + 2], "big")
        ln = int.from_bytes(data[i + 2:i + 4], "big")
        if i + 4 + ln > len(data):
            return None
        out.append((pid, data[i + 4:i + 4 + ln]))
        i += 4 + ln
    if i != len(data) or not out:
        return None
    return out


def as_text(v: bytes):
    if not v:
        return None
    if 0 in v:
        v = v[:v.index(0)]
    try:
        s = v.decode("utf-8")
    except UnicodeDecodeError:
        return None
    return s if s.strip() else None


def security_from_wire(v: int) -> str:
    return {0: "Open", 1: "WEP", 2: "WPA/WPA2-PSK"}.get(v, f"UNKNOWN({v})")


def parse_wifi(payload: bytes):
    """等价于 Kotlin WifiConfig.parse()"""
    entries = decode_len_first(payload) or decode_id_first(payload)
    if entries is None:
        return None
    ssid = psk = bssid = None
    sec = None
    channel = 0
    status = None
    for pid, val in entries:
        if pid == 0x0000 and val:
            status = val[0]
        elif pid == 0x0001:
            ssid = as_text(val)
        elif pid == 0x0002:
            psk = as_text(val)
        elif pid == 0x0003 and val:
            sec = security_from_wire(val[0])
        elif pid == 0x0004 and val:
            channel = val[0]
        elif pid == 0x0005 and len(val) == 6:
            bssid = ":".join(f"{b:02X}" for b in val)
    if status not in (None, 0):
        return None
    if not ssid:
        return None
    if sec is None:
        sec = "Open" if not psk else "WPA2-PSK"
    return {"ssid": ssid, "psk": psk, "security": sec, "channel": channel, "bssid": bssid}


# ---------------------------------------------------------------- 用例
print("1) 规范编码：0x5703 参数编解码往返（myBuick GS / WPA2 / ch149）")
payload = (
    tlv(0x0001, "myBuick GS".encode() + b"\x00")
    + tlv(0x0002, "carplay2022".encode() + b"\x00")
    + tlv(0x0003, bytes([SEC_WPA_WPA2]))
    + tlv(0x0004, bytes([149]))
)
cfg = parse_wifi(payload)
check(cfg is not None, "解析成功", payload.hex())
check(cfg["ssid"] == "myBuick GS", f"ssid={cfg['ssid'] if cfg else None}")
check(cfg["psk"] == "carplay2022", f"psk={'***' if cfg and cfg['psk'] else None}")
check(cfg["security"] == "WPA/WPA2-PSK", f"security={cfg['security'] if cfg else None}")
check(cfg["channel"] == 149, f"channel={cfg['channel'] if cfg else None}")

print("\n2) 字段 ID 必须按规范（1=SSID 2=Passphrase 3=Security 4=Channel）")
ids = [pid for pid, _ in decode_len_first(payload)]
check(ids == [1, 2, 3, 4], f"参数顺序 {[hex(i) for i in ids]}")
expected_len = (4 + len("myBuick GS") + 1) + (4 + len("carplay2022") + 1) + (4 + 1) + (4 + 1)
check(len(payload) == expected_len, f"字节数 {len(payload)} == {expected_len}")

# 旧实现是「id 在前」且把 passphrase 放 3、安全类型放 4、信道放 5 → 全部错位
legacy = b""
for pid, val in [(1, b"myBuick GS"), (2, b"aa:bb:cc:dd:ee:ff"), (3, bytes([SEC_WPA_WPA2])),
                 (4, b"carplay2022"), (5, bytes([149]))]:
    legacy += pid.to_bytes(2, "big") + len(val).to_bytes(2, "big") + val
legacy_cfg = parse_wifi(legacy)
check(legacy_cfg is not None
      and legacy_cfg["psk"] != "carplay2022"
      and legacy_cfg["channel"] == ord("c"),
      "旧字段序会被错读：psk=%s / channel=%s（真实 psk=carplay2022 / ch=149）"
      % (legacy_cfg["psk"] if legacy_cfg else None,
         legacy_cfg["channel"] if legacy_cfg else None))

print("\n3) 开放网络：SecurityType=0 且无 passphrase")
open_payload = tlv(0x0001, "Buick-CarPlay".encode()) + tlv(0x0003, bytes([SEC_NONE]))
open_cfg = parse_wifi(open_payload)
check(open_cfg["security"] == "Open", f"security={open_cfg['security']}")
check(open_cfg["psk"] is None, "psk 为空")

print("\n4) 兼容变体：[id 2B][len 2B] 排列（部分车厂实现）")
alt = b""
for pid, val in [(0x0001, b"myBuick GS"), (0x0002, b"carplay2022"), (0x0003, bytes([2]))]:
    alt += pid.to_bytes(2, "big") + len(val).to_bytes(2, "big") + val
alt_cfg = parse_wifi(alt)
check(decode_len_first(alt) is None, "长度优先解析应失败（不合规）")
check(alt_cfg and alt_cfg["ssid"] == "myBuick GS", f"兜底解析 ssid={alt_cfg['ssid'] if alt_cfg else None}")

print("\n5) 0x5701 WiFiInformation（status/ssid/passphrase）同解析器可用")
info_payload = (
    tlv(0x0000, bytes([0]))
    + tlv(0x0001, "iPhone-Hotspot".encode())
    + tlv(0x0002, "12345678".encode())
)
info = parse_wifi(info_payload)
check(info and info["ssid"] == "iPhone-Hotspot", f"ssid={info['ssid'] if info else None}")
check(info and info["psk"] == "12345678", "passphrase 解析")
bad_status = tlv(0x0000, bytes([1])) + tlv(0x0001, b"x")
check(parse_wifi(bad_status) is None, "status=1（用户拒绝）应判为失败")

print("\n6) CSM 帧长（含 6 字节头）")
frame = csm(MSG_WIFI_CONFIG, payload)
check(len(frame) == 6 + len(payload), f"0x5703 帧 {len(frame)}B")
check(frame[:6] == b"\x40\x40" + len(frame).to_bytes(2, "big") + b"\x57\x03",
      f"头 {frame[:6].hex(' ')}")
req = csm(MSG_REQ_WIFI_CONFIG)          # 官方实测 16B 整帧（= 9 链路头 + 6 + 1 校验）
check(len(req) == 6, f"0x5702 无参数 CSM={len(req)}B → 链路整帧 {9 + len(req) + 1}B")
check(9 + len(req) + 1 == 16, "与官方 IAP2_TX_BEGIN id=5702 bytes=16 一致")

print("\n7) 0x4300 DeviceTransportIdentifier：16 字节 UUID")
uuid_bytes = bytes(range(16))
tid_frame = csm(MSG_DEVICE_TRANSPORT_ID, tlv(0x0000, uuid_bytes))
check(len(tid_frame) == 6 + 4 + 16, f"帧长 {len(tid_frame)}B")
tid_params = decode_len_first(tid_frame[6:])
check(tid_params and tid_params[0][1] == uuid_bytes, "参数回读一致")

print("\n8) 发送门禁：旧逻辑 vs 新逻辑")
for mid, name in [(MSG_REQ_WIFI_CONFIG, "0x5702 请求WiFi"),
                  (MSG_DEVICE_TRANSPORT_ID, "0x4300 传输标识"),
                  (MSG_REQ_WIFI_INFO, "0x5700 请求WiFi信息")]:
    old_ok = mid in PEER_ADVERTISED
    new_ok = mid in ALWAYS_SENDABLE or not PEER_ADVERTISED or mid in PEER_ADVERTISED
    check(not old_ok and new_ok, f"{name}: 旧=丢弃 新=放行（这正是官方 SKIPPED_NOT_ADVERTISED 死锁点）")

print("\n" + "=" * 66)
if failures:
    print(f"失败 {len(failures)} 项：{failures}")
    sys.exit(1)
print("全部通过：0x5702→0x5703 链路字段、TLV 顺序、门禁放行均自洽")
