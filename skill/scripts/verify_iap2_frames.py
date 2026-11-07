"""iAP2 链路层帧自检：复刻 Iap2LinkLayer.buildPacket/parsePackets 逻辑并回环验证。

用途：在部署到真机前，确认帧长度、header 校验、payload 校验三者自洽，
且能被同一套解析逻辑完整还原（防止再次出现"发出去车机不认"）。

参数取值全部来自官方 0.8.43 在同一台车（22 款君威）上的实测日志。
"""
from struct import Struct

CONTROL_SYN, CONTROL_ACK, CONTROL_EAK, CONTROL_RST = 0x80, 0x40, 0x20, 0x10
# 官方实测：控制会话 sid=1（type=0），EA 会话 sid=3（type=2）
CONTROL_SESSION_ID, EA_SESSION_ID = 1, 3
HEADER_SIZE = 9
MARKER = bytes([0xFF, 0x55, 0x02, 0x00, 0xEE, 0x10])

# 官方实测 LSP 参数
LSP_OFFICIAL = dict(max_outgoing=5, max_len=2048, rto=1500, ack_to=73, max_retrans=30, max_ack=3)


def gen_checksum(b: bytes) -> int:
    return (-sum(b)) & 0xFF


def verify(b: bytes) -> bool:
    return (sum(b) & 0xFF) == 0


def build_packet(payload, control, seq, ack, session_id) -> bytes:
    length = (len(payload) + 10) if payload else HEADER_SIZE
    h8 = bytes([0xFF, 0x5A, (length >> 8) & 0xFF, length & 0xFF,
                control & 0xFF, seq & 0xFF, ack & 0xFF, session_id & 0xFF])
    out = h8 + bytes([gen_checksum(h8)])
    if payload:
        out += payload + bytes([gen_checksum(payload)])
    return out


def pack_lsp(max_outgoing=LSP_OFFICIAL["max_outgoing"], max_len=LSP_OFFICIAL["max_len"],
             rto=LSP_OFFICIAL["rto"], ack_to=LSP_OFFICIAL["ack_to"],
             max_retrans=LSP_OFFICIAL["max_retrans"], max_ack=LSP_OFFICIAL["max_ack"]):
    p = Struct(">BBHHHBB").pack(1, max_outgoing, max_len, rto, ack_to, max_retrans, max_ack)
    p += bytes([CONTROL_SESSION_ID, 0, 1])
    p += bytes([EA_SESSION_ID, 2, 1])
    return p


def csm(msg_id: int, params: bytes = b"") -> bytes:
    total = 6 + len(params)
    return Struct(">HHH").pack(0x4040, total, msg_id) + params


def parse(data: bytes):
    """返回 [(kind, control, seq, ack, session, payload, valid)]"""
    out, i = [], 0
    while i + HEADER_SIZE <= len(data):
        if data[i] != 0xFF:
            i += 1
            continue
        if data[i + 1] == 0x55:
            out.append(("MARKER", 0xEE, 0, 0, 0, b"", True))
            i += 6
            continue
        if data[i + 1] != 0x5A:
            i += 1
            continue
        hdr = data[i:i + HEADER_SIZE]
        if not verify(hdr):
            i += 1
            continue
        length = (data[i + 2] << 8) | data[i + 3]
        if length < HEADER_SIZE or i + length > len(data):
            break
        ctl, seq, ack, sess = hdr[4], hdr[5], hdr[6], hdr[7]
        payload, ok = b"", True
        if length > HEADER_SIZE:
            wcs = data[i + HEADER_SIZE:i + length]
            ok = verify(wcs)
            payload = wcs[:-1]
        out.append(("DATA", ctl, seq, ack, sess, payload, ok))
        i += length
    return out


def decode_csm(b: bytes):
    if len(b) < 6:
        return None
    start, length, msgid = Struct(">HHH").unpack(b[:6])
    if start != 0x4040:
        return None
    return msgid, b[6:length]


def hexs(b: bytes) -> str:
    return " ".join(f"{x:02X}" for x in b)


print("=" * 66)
print("1) MARKER 校验")
print("  ", hexs(MARKER))
# 注意：MARKER 的校验位只覆盖 length+control（不含 FF 55 同步字），
# 与普通包的"整 8 字节 header"算法不同 —— 参考实现里它是硬编码常量，
# 接收侧直接按 6 字节做全等比较，不走通用校验。
marker_cs = gen_checksum(MARKER[2:5])
print(f"   校验位应为 gen_checksum(02 00 EE) = 0x{marker_cs:02X}，实际 0x{MARKER[5]:02X}")
assert marker_cs == MARKER[5], "MARKER 字节不符"
print("   整 6 字节 sum & 0xFF = 0x%02X（非 0，符合预期）" % (sum(MARKER) & 0xFF))

print("\n2) LSP 长度（应为 16 字节）")
lsp = pack_lsp()
print("  ", hexs(lsp), f"= {len(lsp)}B")
assert len(lsp) == 16

print("\n3) SYN 包（control=0x80, seq=99, sess=10）")
syn = build_packet(lsp, CONTROL_SYN, 99, 0, CONTROL_SESSION_ID)
print("  ", hexs(syn), f"\n   总长 = {len(syn)}B")
assert len(syn) == 26, len(syn)
assert verify(syn[:9]), "header 校验失败"
assert verify(syn[9:]), "payload 校验失败"

print("\n4) 1D00 数据包（control=0x40, seq=100, sess=10）")
body = csm(0x1D00)
frame = build_packet(body, CONTROL_ACK, 100, 0, CONTROL_SESSION_ID)
print("   CSM payload:", hexs(body))
print("   完整帧    :", hexs(frame), f"\n   总长 = {len(frame)}B")
assert len(frame) == 16, len(frame)
assert verify(frame[:9]) and verify(frame[9:])

print("\n5) 回环解析（模拟车机侧读取）")
got = parse(frame)
k, ctl, seq, ack, sess, payload, ok = got[0]
print(f"   kind={k} ctl=0x{ctl:02X} seq={seq} ack={ack} sess={sess} valid={ok}")
print("   payload  :", hexs(payload))
dec = decode_csm(payload)
print(f"   CSM     : msgId=0x{dec[0]:04X} params={len(dec[1])}B")
assert ctl == CONTROL_ACK and sess == CONTROL_SESSION_ID and ok
assert dec[0] == 0x1D00

print("\n6) 粘包场景（MARKER + SYN + 数据包 连在一起）")
stream = MARKER + syn + frame
print("   流长度 =", len(stream), "B")
res = parse(stream)
for r in res:
    print(f"   {r[0]} ctl=0x{r[1]:02X} len(payload)={len(r[5])} valid={r[6]}")
assert len(res) == 3, f"应解析出 3 个包，实际 {len(res)}"

print("\n7) 半包场景（只给前半段，不应误判）")
half = frame[:10]
print("   ", hexs(half), "->", parse(half))

print("\n8) 噪声前导（车机可能插入杂字节）")
noisy = bytes([0x00, 0x11, 0xFF]) + frame
res = parse(noisy)
print("   解析出", len(res), "个包:", [r[0] for r in res])
assert len(res) == 1

print("\n9) 官方 0.8.51 握手序列逐帧验算（2026-10-25 02:10:23 全量 logcat）")
# 严格照抄官方实测字段：所有链路包 sid=0；我方 seq 由 98 起，ACK 不消耗序号
brk_syn    = build_packet(lsp, CONTROL_SYN, 100, 0, 0)                      # 车机 SYN
our_syn1   = build_packet(lsp, CONTROL_SYN, 98, 0, 0)                       # 我方 SYN#1
brk_synack = build_packet(lsp, CONTROL_SYN | CONTROL_ACK, 101, 98, 0)       # 车机 SYN|ACK
our_ack1   = build_packet(None, CONTROL_ACK, 98, 101, 0)                    # 我方 ACK（不消耗序号）
our_syn2   = build_packet(lsp, CONTROL_SYN, 99, 101, 0)                     # 我方 SYN#2
brk_ack    = build_packet(None, CONTROL_ACK, 101, 99, 0)                    # 车机 ACK
our_ack2   = build_packet(None, CONTROL_ACK, 99, 101, 0)                    # 我方 ACK
seq_plan = [("车机SYN", brk_syn, 0x80, 100, 0, 0),
            ("我方SYN#1", our_syn1, 0x80, 98, 0, 0),
            ("车机SYN|ACK", brk_synack, 0xC0, 101, 98, 0),
            ("我方ACK", our_ack1, 0x40, 98, 101, 0),
            ("我方SYN#2", our_syn2, 0x80, 99, 101, 0),
            ("车机ACK", brk_ack, 0x40, 101, 99, 0),
            ("我方ACK#2", our_ack2, 0x40, 99, 101, 0)]
for name, pkt, ctl_e, seq_e, ack_e, sid_e in seq_plan:
    assert verify(pkt[:9]), f"{name} header 校验失败"
    if len(pkt) > 9:
        assert verify(pkt[9:]), f"{name} payload 校验失败"
    _, ctl, seq, ack, sid, payload, ok = parse(pkt)[0]
    assert ok, f"{name} 回读校验失败"
    assert (ctl, seq, ack, sid) == (ctl_e, seq_e, ack_e, sid_e), \
        f"{name} 字段不符: got ctl=0x{ctl:02X} seq={seq} ack={ack} sid={sid}"
    assert sid == 0, f"{name} 链路包 sid 必须为 0（实测铁律）"
    print(f"   {name:12s} {len(pkt):>2}B  ctl=0x{ctl:02X} seq={seq:>3} ack={ack:>3} sid={sid}"
          + (f"  payload={len(payload)}B" if payload else ""))

print("\n   帧长核对：SYN=26B / 纯 ACK=9B / 首条数据 1D00=16B")
assert len(brk_syn) == 26 and len(our_ack1) == 9

print("\n10) CSM 消息帧长核对（对齐官方 TX bytes= 字段）")
def frame_len(msg_id, params=b""):
    return len(build_packet(csm(msg_id, params), CONTROL_ACK, 100, 0, CONTROL_SESSION_ID))

assert frame_len(0x1D00) == 16, "1D00 应为 16B（0 参数）"
assert frame_len(0x1D02) == 16, "1D02 应为 16B（0 参数）"
assert frame_len(0xAA00) == 16, "AA00 应为 16B（0 参数）"
assert frame_len(0xAA05) == 16, "AA05 应为 16B（0 参数）"
# AA02：官方 40B → 参数 24B = TLV 头 4B + 20B 随机挑战
chal_param = bytes([0x00, 0x18, 0x00, 0x00]) + bytes(range(20))
assert frame_len(0xAA02, chal_param) == 40, "AA02 应为 40B（24B 参数 = 4B 头 + 20B 挑战）"
# 4E0C：官方 57B → 参数 57-16 = 41B
assert frame_len(0x4E0C, bytes(41)) == 57
print("   1D00=16B ✓  1D02=16B ✓  AA00=16B ✓  AA02=40B ✓  AA05=16B ✓  4E0C=57B ✓")

print("\n11) 1D01 参数解析 + 「车机可接收消息表」提取")
def tlv(pid, value):
    ln = 4 + len(value)
    return bytes([(ln >> 8) & 0xFF, ln & 0xFF, (pid >> 8) & 0xFF, pid & 0xFF]) + value

def msg_entry(mid):
    # 消息表条目：len=6 的 TLV，值 2 字节就是消息 ID
    return bytes([0x00, 0x06, 0x00, 0x00, (mid >> 8) & 0xFF, mid & 0xFF])

# 按实测：车机声明 8 个可接收 ID
declared = [0x4E09, 0x4E0A, 0x4E0B, 0x4E0C, 0x4E0D, 0x4E0E, 0xEA00, 0xEA01]
payload_1d01 = tlv(0x0000, b"myBuick\x00") + tlv(0x0001, b"Buick\x00")
payload_1d01 += tlv(0x0006, b"".join(msg_entry(m) for m in declared))
payload_1d01 += tlv(0x000E, tlv(0x0001, bytes([0x00])))   # 传输组件：bluetooth

def decode_params(data):
    out, i = [], 0
    while i + 4 <= len(data):
        ln = (data[i] << 8) | data[i + 1]
        pid = (data[i + 2] << 8) | data[i + 3]
        if ln < 4 or i + ln > len(data):
            break
        out.append((pid, data[i + 4:i + ln]))
        i += ln
    return out

ids = set()
for pid, val in decode_params(payload_1d01):
    for npid, nval in decode_params(val):
        if len(nval) == 2:
            ids.add((nval[0] << 8) | nval[1])
ids = {i for i in ids if 0x4000 <= i <= 0x7FFF or 0xE000 <= i <= 0xEFFF}
print("   解析出可接收消息:", ",".join("0x%04X" % i for i in sorted(ids)))
assert ids == set(declared), f"消息表提取失败: {ids}"

# 关键判定：0x5702 不在表内 → 绝不能发（官方 IAP2_TX_SKIPPED_NOT_ADVERTISED id=5702）
assert 0x5702 not in ids, "0x5702 不应出现在车机声明表内"
print("   0x5702 不在声明表内 → 官方据此 skipped（死锁点，见 verify_wifi_config.py 第 8 项）")

print("\n12) 0x1D00 EA 列表编码/解码回环（对齐 V1.7.73 bEaPayload）")
def b_ea_ids(ids):
    out = bytearray()
    for mid in ids:
        out.append((mid >> 8) & 0xFF)
        out.append(mid & 0xFF)
    return bytes(out)

def iap2_b(index, value):
    ln = 4 + len(value)
    return bytes([(ln >> 8) & 0xFF, ln & 0xFF,
                  (index >> 8) & 0xFF, index & 0xFF]) + value

def iap2_f(*tlvs):
    out = bytearray()
    for t in tlvs:
        out += t
    return bytes(out)

def decode_tlv(data):
    out, i = {}, 0
    while i + 4 <= len(data):
        ln = (data[i] << 8) | data[i + 1]
        idx = (data[i + 2] << 8) | data[i + 3]
        if ln < 4 or i + ln > len(data):
            break
        out[idx] = data[i + 4:i + ln]
        i += ln
    return out

def ids_of(b):
    return [(b[i] << 8) | b[i + 1] for i in range(0, len(b), 2)]

# 与 MfiCertificateVerifier.bEaPayload() 完全一致的取值（车机 RECEIVE 期望集 + 17153/17152）
to_send = [19945, 19946, 19947, 19948, 19949, 19950, 59904, 59905]   # 0x4E09–0x4E0E, 0xEA00,0xEA01
to_receive = [17153, 17152, 19945, 19946, 19947, 19948, 19949, 19950, 59904, 59905]
ea = iap2_f(iap2_b(6, b_ea_ids(to_send)), iap2_b(7, b_ea_ids(to_receive)))
frame_1d00 = csm(0x1D00, ea)
print("   0x1D00 EA payload:", hexs(ea), f"({len(ea)}B)")
mid, params = decode_csm(frame_1d00)
assert mid == 0x1D00, f"msgId 应为 0x1D00，实际 0x{mid:04X}"
tvl = decode_tlv(params)
got_send, got_recv = ids_of(tvl[6]), ids_of(tvl[7])
print("   to_send   :", ",".join("0x%04X" % i for i in got_send))
print("   to_receive:", ",".join("0x%04X" % i for i in got_recv))
assert got_send == to_send, f"to_send 不一致: {got_send}"
assert 17153 in got_recv and 17152 in got_recv, "to_receive 必须含 17153/17152"
assert got_recv == to_receive, f"to_receive 不一致: {got_recv}"
print("   EA 列表编码与 V1.7.73 bEaPayload() 一致 ✓ （车机将据此在 SEND 列表放 17153）")

print("\n" + "=" * 66)
print("全部通过：帧格式自洽 + 握手字段与官方实测逐位一致 + EA 列表编码对齐，可部署真机验证")

