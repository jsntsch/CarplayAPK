#!/usr/bin/env python3
"""手工 mDNS/ DNS-SD 查询，用来确认车机是否真的在广播 CarPlay 服务。

绕过 App 与 NsdManager，直接从一台连在车机热点上的机器（PC/手机）发组播查询，
从而区分「车机没广播」和「App 侧发现实现有问题」。

用法:
    python mdns_probe.py                      # 默认查 _airplay._tcp / _carplay-ctrl._tcp / _carplay._tcp
    python mdns_probe.py _raop._tcp           # 只查指定类型
    python mdns_probe.py --timeout 6          # 延长等待
    python mdns_probe.py --iface 192.168.5.101  # 指定出口网卡地址

退出码: 0 = 至少发现一个含 "carplay" 或 "airplay" 的服务；1 = 没发现。
"""

import argparse
import socket
import struct
import sys
import time

MDNS_ADDR = "224.0.0.251"
MDNS_PORT = 5353

DEFAULT_TYPES = [
    "_airplay._tcp.local",
    "_carplay-ctrl._tcp.local",
    "_carplay._tcp.local",
]

TYPE_NAMES = {1: "A", 2: "NS", 5: "CNAME", 6: "SOA", 12: "PTR",
              16: "TXT", 28: "AAAA", 33: "SRV"}


def encode_name(name: str) -> bytes:
    out = b""
    for part in name.rstrip(".").split("."):
        if part:
            out += bytes([len(part)]) + part.encode("utf-8")
    return out + b"\x00"


def build_query(name: str, qtype: int = 12, qclass: int = 1) -> bytes:
    # 标准查询，QDCOUNT=1。mDNS 的 class 最高位（QU bit）置 0 表示希望组播应答。
    header = struct.pack("!HHHHHH", 0, 0, 1, 0, 0, 0)
    return header + encode_name(name) + struct.pack("!HH", qtype, qclass)


def read_name(data: bytes, off: int):
    """解析 DNS 名字，支持压缩指针。返回 (名字, 下一个偏移)。"""
    labels = []
    next_off = off
    jumped = False
    guard = 0
    while guard < 128:
        guard += 1
        if off >= len(data):
            break
        length = data[off]
        if length == 0:
            off += 1
            if not jumped:
                next_off = off
            break
        if length & 0xC0 == 0xC0:  # 压缩指针
            ptr = ((length & 0x3F) << 8) | data[off + 1]
            if not jumped:
                next_off = off + 2
                jumped = True
            off = ptr
            continue
        off += 1
        labels.append(data[off:off + length].decode("utf-8", "replace"))
        off += length
        if not jumped:
            next_off = off
    return ".".join(labels), next_off


def parse_response(data: bytes):
    """解析 mDNS 响应，产出 (section, 名字, 类型, 值)。"""
    if len(data) < 12:
        return
    _, flags, qd, an, ns, ar = struct.unpack("!HHHHHH", data[:12])
    if not (flags & 0x8000):  # 不是响应
        return
    off = 12
    for _ in range(qd):
        _, off = read_name(data, off)
        off += 4
    for section, count in (("answer", an), ("authority", ns), ("additional", ar)):
        for _ in range(count):
            name, off = read_name(data, off)
            if off + 10 > len(data):
                return
            rtype, rclass, ttl, rdlen = struct.unpack("!HHIH", data[off:off + 10])
            off += 10
            rdata_off = off
            off += rdlen
            if off > len(data):
                return
            tname = TYPE_NAMES.get(rtype, "type%d" % rtype)
            if rtype == 12:                       # PTR
                target, _ = read_name(data, rdata_off)
                yield section, name, tname, target, ttl
            elif rtype == 33:                     # SRV
                prio, weight, port = struct.unpack("!HHH", data[rdata_off:rdata_off + 6])
                target, _ = read_name(data, rdata_off + 6)
                yield section, name, tname, "%s:%d (prio=%d w=%d)" % (target, port, prio, weight), ttl
            elif rtype == 16:                     # TXT
                txt, p = [], rdata_off
                end = rdata_off + rdlen
                while p < end:
                    ln = data[p]
                    p += 1
                    if ln == 0:
                        continue
                    txt.append(data[p:p + ln].decode("utf-8", "replace"))
                    p += ln
                yield section, name, tname, " | ".join(txt), ttl
            elif rtype in (1, 28):                # A / AAAA
                if rtype == 1 and rdlen == 4:
                    val = ".".join(str(b) for b in data[rdata_off:rdata_off + 4])
                elif rtype == 28 and rdlen == 16:
                    val = socket.inet_ntop(socket.AF_INET6, data[rdata_off:rdata_off + 16])
                else:
                    continue
                yield section, name, tname, val, ttl


def main() -> int:
    ap = argparse.ArgumentParser(description="手工 mDNS 查询 CarPlay/AirPlay 服务")
    ap.add_argument("types", nargs="*", default=None,
                    help="服务类型，如 _airplay._tcp（可省略 .local）")
    ap.add_argument("--timeout", type=float, default=4.0, help="等待秒数，默认 4")
    ap.add_argument("--iface", default=None, help="出口网卡地址（本机在车机网段上的 IP）")
    args = ap.parse_args()

    types = args.types or DEFAULT_TYPES
    types = [t if t.endswith(".local") else t + ".local" for t in types]

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
    except (AttributeError, OSError):
        pass
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 255)
    sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_LOOP, 1)

    if args.iface:
        try:
            sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_IF,
                            socket.inet_aton(args.iface))
            print("出口网卡: %s" % args.iface)
        except OSError as e:
            print("设置出口网卡失败: %s（继续用系统默认）" % e)

    print("查询类型: %s" % ", ".join(types))
    print("发送到 %s:%d，等待 %.1fs\n" % (MDNS_ADDR, MDNS_PORT, args.timeout))

    for t in types:
        try:
            sock.sendto(build_query(t), (MDNS_ADDR, MDNS_PORT))
        except OSError as e:
            print("发送失败 (%s): %s" % (t, e))

    sock.settimeout(0.5)
    deadline = time.time() + args.timeout
    seen = []
    seen_keys = set()
    while time.time() < deadline:
        try:
            data, addr = sock.recvfrom(9000)
        except socket.timeout:
            continue
        except OSError:
            break
        for section, name, rtype, value, ttl in parse_response(data):
            key = (name, rtype, value)
            if key in seen_keys:
                continue
            seen_keys.add(key)
            seen.append(key)
            print("[%s] %s  %-4s  %s   (ttl=%ds, from %s)"
                  % (section, name, rtype, value, ttl, addr[0]))

    print()
    if not seen:
        print("未收到任何应答。可能原因：")
        print("  1. 本机不在车机热点网段（先确认 IP 是 192.168.x.x 且网关是车机）")
        print("  2. 车机未进入 CarPlay 接收态，AirPlay 服务未启动")
        print("  3. AP 开启了客户端隔离，组播到不了本机")
        print("  4. Windows 防火墙拦了 UDP 5353")
        return 1

    hits = [s for s in seen if "carplay" in s[2].lower() or "airplay" in s[2].lower()]
    if hits:
        print("发现 CarPlay/AirPlay 相关服务 %d 条 —— 车机侧广播正常。" % len(hits))
        print("若 App 侧仍发现不到，问题在 App 的 NsdManager 实现或网络绑定。")
        return 0
    print("收到应答但无 CarPlay 相关服务，检查是否查错了服务类型。")
    return 1


if __name__ == "__main__":
    sys.exit(main())
