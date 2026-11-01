#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
DiPlay / AirPlay 接收端 —— pair-setup + pair-verify 探测脚本

用途
----
在 PC 上把「手机 → CarPlay 接收端」的配对握手单独跑通，不用每次改 Kotlin 重编 APK。
默认目标是同网段 Android 上运行的 DiPlay（`com.shihab.diplay`）。

规格来源
--------
反编译 DiPlay 0.2.20（com.shilapi.xcertplay.airplay.*）：
  Srp6a / Srp6aSession / PairSetup / PairVerify / AirPlayCrypto
并与 iPhone 原生 CarPlay 的 sysdiagnose 核对一致。

⚠️ 这里的 SRP 是 **Apple 变体**，与 RFC 5054 不同：
  · M1 proof 里 H(N) 与 H(g) **逐字节 XOR 后再拼**（同 RFC 5054，但 K 的算法不同）
  · K = SHA512(toBytes(S))，**不**做定长 pad（RFC 用 PAD(S)）
  · u = SHA512(PAD(A) || PAD(B))

用法
----
  python diplay_pair_probe.py 192.168.1.51
  python diplay_pair_probe.py 192.168.1.51 --pin 3939 --port 7000
"""
import argparse
import hashlib
import os
import socket
import struct
import sys
import uuid

from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
from cryptography.hazmat.primitives.asymmetric.x25519 import X25519PrivateKey, X25519PublicKey
from cryptography.hazmat.primitives import serialization as _ser

Raw = _ser.Encoding.Raw

# ---------------------------------------------------------------- 大整数 / SRP

# RFC 3526 §2 —— 3072 位 MODP 组（DiPlay Srp6a.n 逐字符一致）
N_HEX = (
    "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"
    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"
    "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
    "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"
    "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
    "83655D23DCA3AD961C62F356208552BB9ED529077096966D"
    "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
    "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9"
    "DE2BCBF6955817183995497CEA956AE515D2261898FA0510"
    "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64"
    "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7"
    "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B"
    "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C"
    "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31"
    "43DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF"
)
N = int(N_HEX, 16)
G = 5
N_BYTES = 384
COLON = b":"
IDENTIFIER = b"Pair-Setup"


def sha512(*parts: bytes) -> bytes:
    h = hashlib.sha512()
    for p in parts:
        h.update(p)
    return h.digest()


def to_bytes(x: int) -> bytes:
    """最小大端编码（去掉前导零）——对应 DiPlay toBytes。"""
    if x == 0:
        return b"\x00"
    return x.to_bytes((x.bit_length() + 7) // 8, "big")


def pad(x: int, size: int = N_BYTES) -> bytes:
    """左补零到固定长度——对应 DiPlay pad。"""
    b = to_bytes(x)
    return b"\x00" * (size - len(b)) + b if len(b) < size else b[-size:]


K_MULT = int.from_bytes(sha512(to_bytes(N), pad(G)), "big")


def srp_client(pin: str, variant: int = 0):
    """生成 (A, 计算 M1 的函数)。

    variant 用于扫差异（用错误码当 oracle）：
      0 = x=H(salt|H(I:P))  K=H(toBytes(S))  XOR  ← 基准
      1 = x=H(salt|H(P:I))
      2 = x=H(H(I:P)|salt)
      3 = K=H(pad(S))
      4 = u=H(A|B) 不 pad
      5 = M1 不做 XOR（直接 H(N)||H(g)）
      6 = x 里不含 salt
    """
    a = int.from_bytes(os.urandom(32), "big")
    A = pow(G, a, N)

    def finish(salt: bytes, B: int):
        inner = sha512(IDENTIFIER, COLON, pin.encode())
        if variant == 1:
            inner = sha512(pin.encode(), COLON, IDENTIFIER)
        if variant == 2:
            xb = sha512(inner, salt)
        elif variant == 6:
            xb = sha512(inner)
        else:
            xb = sha512(salt, inner)
        u = (int.from_bytes(sha512(pad(A), pad(B)), "big") if variant != 4
             else int.from_bytes(sha512(to_bytes(A), to_bytes(B)), "big"))
        x = int.from_bytes(xb, "big")
        S = pow((B - K_MULT * pow(G, x, N)) % N, a + u * x, N)
        K = sha512(pad(S) if variant == 3 else to_bytes(S))
        hn, hg = sha512(to_bytes(N)), sha512(to_bytes(G))
        head = hn + hg if variant == 5 else bytes(x ^ y for x, y in zip(hn, hg))
        M1 = sha512(head, sha512(IDENTIFIER), salt, pad(A), pad(B), K)
        M2 = sha512(pad(A), M1, K)
        return M1, M2, K

    return A, finish


# ---------------------------------------------------------------- TLV8

def tlv(*pairs) -> bytes:
    out = b""
    for t, v in pairs:
        if isinstance(v, int):
            v = bytes([v])
        # 超过 255 字节要按 255 切块，重复 type（HAP 规则）
        while True:
            chunk, v = v[:255], v[255:]
            out += bytes([t, len(chunk)]) + chunk
            if not v:
                break
    return out


def tlv_decode(data: bytes) -> dict:
    out, i = {}, 0
    while i + 2 <= len(data):
        t, ln = data[i], data[i + 1]
        out[t] = out.get(t, b"") + data[i + 2:i + 2 + ln]
        i += 2 + ln
    return out


T_STATE, T_SALT, T_PUB, T_PROOF = 0x06, 0x02, 0x03, 0x04
T_ENC, T_ERR, T_ID, T_SIG = 0x05, 0x07, 0x01, 0x0A


# ---------------------------------------------------------------- RTSP

class Rtsp:
    def __init__(self, host, port, timeout=6):
        self.s = socket.create_connection((host, port), timeout)
        self.s.settimeout(timeout)
        self.cseq = 0
        self.buf = b""

    def req(self, method, body=b"", ctype="application/octet-stream"):
        self.cseq += 1
        head = f"{method} RTSP/1.0\r\nCSeq: {self.cseq}\r\n"
        head += "User-Agent: AirPlay/950.7.1\r\n"
        if body:
            head += f"Content-Type: {ctype}\r\nContent-Length: {len(body)}\r\n"
        self.s.sendall(head.encode() + b"\r\n" + body)
        return self.read()

    def read(self):
        while b"\r\n\r\n" not in self.buf:
            d = self.s.recv(65536)
            if not d:
                raise ConnectionError("对端关闭")
            self.buf += d
        head, self.buf = self.buf.split(b"\r\n\r\n", 1)
        lines = head.decode("latin1").split("\r\n")
        status = lines[0]
        hdrs = {}
        for ln in lines[1:]:
            if ":" in ln:
                k, v = ln.split(":", 1)
                hdrs[k.strip().lower()] = v.strip()
        n = int(hdrs.get("content-length", 0))
        while len(self.buf) < n:
            d = self.s.recv(65536)
            if not d:
                break
            self.buf += d
        body, self.buf = self.buf[:n], self.buf[n:]
        return status, hdrs, body


# ---------------------------------------------------------------- Chacha

def chacha(key: bytes, nonce: bytes, data: bytes, aad: bytes = b"", encrypt=True):
    """ChaCha20-Poly1305；返回 ct||tag（加密）或明文（解密，tag 不符返回 None）。"""
    try:
        from cryptography.hazmat.primitives.ciphers.aead import ChaCha20Poly1305
    except ImportError:
        return None
    c = ChaCha20Poly1305(key)
    if encrypt:
        return c.encrypt(nonce, data, aad)
    try:
        return c.decrypt(nonce, data, aad)
    except Exception:
        return None


def hkdf_sha512(ikm: bytes, salt: bytes, info: bytes, length: int) -> bytes:
    try:
        from cryptography.hazmat.primitives.kdf.hkdf import HKDF
        from cryptography.hazmat.primitives import hashes
        return HKDF(algorithm=hashes.SHA512(), length=length, salt=salt, info=info).derive(ikm)
    except ImportError:
        # 纯 Python 兜底
        import hmac
        prk = hmac.new(salt or b"\x00" * 64, ikm, hashlib.sha512).digest()
        out, t, i = b"", b"", 1
        while len(out) < length:
            t = hmac.new(prk, t + info + bytes([i]), hashlib.sha512).digest()
            out += t
            i += 1
        return out[:length]


def nonce_label(label: str) -> bytes:
    b = label.encode("ascii")[:8]
    return b"\x00" * 4 + b



# ---------------------------------------------------------------- 身份

ID_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".diplay_identity.json")


def load_identity():
    """持久化本端 Ed25519 长期身份 + pairingId（pair-verify 要与注册时一致）。"""
    if os.path.exists(ID_FILE):
        import json
        j = json.load(open(ID_FILE))
        priv = Ed25519PrivateKey.from_private_bytes(bytes.fromhex(j["priv"]))
        return priv, bytes.fromhex(j["pub"]), j["pairing_id"].encode()
    priv = Ed25519PrivateKey.generate()
    pub = priv.public_key().public_bytes(_ser.Encoding.Raw, _ser.PublicFormat.Raw)
    pid = str(uuid.uuid4()).upper().encode()
    import json
    json.dump({"priv": priv.private_bytes(_ser.Encoding.Raw, _ser.PrivateFormat.Raw,
                                          _ser.NoEncryption()).hex(),
               "pub": pub.hex(), "pairing_id": pid.decode()},
              open(ID_FILE, "w"))
    return priv, pub, pid


def verify_sig(ltpk: bytes, data: bytes, sig: bytes) -> bool:
    try:
        from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PublicKey
        Ed25519PublicKey.from_public_bytes(ltpk).verify(sig, data)
        return True
    except Exception:
        return False


# ---------------------------------------------------------------- main

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("host")
    ap.add_argument("--port", type=int, default=7000)
    ap.add_argument("--pin", default="3939")
    ap.add_argument("--variant", type=int, default=0)
    args = ap.parse_args()

    print(f"== 目标 {args.host}:{args.port} ==")
    r = Rtsp(args.host, args.port)

    st, hd, _ = r.req("OPTIONS *")
    print("OPTIONS     ->", st, "|", hd.get("public", "")[:80])

    st, hd, body = r.req("GET /info", ctype="text/x-apple-plist+xml")
    print(f"GET /info   -> {st} ({len(body)}B)")
    if any(c in st for c in (" 455", " 403", " 470")):
        print("  ↳ 需要先配对，继续 pair-setup")

    # ---------------- pair-setup ----------------
    A, finish = srp_client(args.pin, args.variant)
    st, hd, body = r.req("POST /pair-setup", tlv((T_STATE, 1), (0x00, 0)))
    print(f"setup M1    -> {st} ({len(body)}B)")
    if " 200 " not in st:
        print("  ✗ M1 被拒"); return
    d = tlv_decode(body)
    if T_ERR in d:
        print("  ✗ M2 ERROR =", d[T_ERR].hex()); return
    salt, B = d[T_SALT], int.from_bytes(d[T_PUB], "big")
    print(f"  M2: salt={len(salt)}B B={len(d[T_PUB])}B")
    if len(d[T_PUB]) != N_BYTES:
        print(f"  ⚠ B 不是 {N_BYTES}B —— 组大小可能不一致")

    M1, M2, K = finish(salt, B)
    # M3 必须同时带本端公钥 A 和 proof —— 少了 A，接收端算不出 u，必回 ERROR=2
    st, hd, body = r.req("POST /pair-setup",
                         tlv((T_STATE, 3), (T_PUB, pad(A)), (T_PROOF, M1)))
    print(f"setup M3    -> {st} ({len(body)}B)")
    d = tlv_decode(body)
    if T_ERR in d:
        print("  ✗ M4 ERROR =", d[T_ERR].hex(), "（PIN 或 SRP 公式不对）"); return
    if d.get(T_PROOF) == M2:
        print("  ✓ M4 proof 校验通过 —— SRP 正确")
    else:
        print("  ⚠ M4 proof 不匹配")

    # ---------------- M5 / M6：交换长期身份，注册配对 ----------------
    priv, ltpk, pid = load_identity()
    key_ps = hkdf_sha512(K, b"Pair-Setup-Encrypt-Salt", b"Pair-Setup-Encrypt-Info", 32)
    xc = hkdf_sha512(K, b"Pair-Setup-Controller-Sign-Salt",
                     b"Pair-Setup-Controller-Sign-Info", 32)
    sig_c = priv.sign(xc + pid + ltpk)
    sub5 = tlv((T_ID, pid), (T_PUB, ltpk), (T_SIG, sig_c))
    enc5 = chacha(key_ps, nonce_label("PS-Msg05"), sub5)
    st, hd, body = r.req("POST /pair-setup", tlv((T_STATE, 5), (T_ENC, enc5)))
    print(f"setup M5    -> {st} ({len(body)}B)")
    d = tlv_decode(body)
    if T_ERR in d:
        print("  ✗ M6 ERROR =", d[T_ERR].hex(), "（M5 签名/加密不对）"); return
    if T_ENC not in d:
        print("  ⚠ M6 无 EncryptedData"); return
    sub6 = chacha(key_ps, nonce_label("PS-Msg06"), d[T_ENC], encrypt=False)
    if sub6 is None:
        print("  ✗ M6 解密失败"); return
    d6 = tlv_decode(sub6)
    acc_id, acc_pk, acc_sig = d6.get(T_ID, b""), d6.get(T_PUB, b""), d6.get(T_SIG, b"")
    xa = hkdf_sha512(K, b"Pair-Setup-Accessory-Sign-Salt",
                     b"Pair-Setup-Accessory-Sign-Info", 32)
    ok6 = verify_sig(acc_pk, xa + acc_id + acc_pk, acc_sig)
    print(f"setup M6    -> 对端 identifier={acc_id.decode(errors='replace')} "
          f"公钥={len(acc_pk)}B 签名校验={'✓' if ok6 else '✗'}")
    print("  ✓✓ pair-setup 全流程完成，配对已注册")

    # ---------------- pair-verify ----------------
    eph_priv = X25519PrivateKey.generate()
    eph_pub = eph_priv.public_key().public_bytes(_ser.Encoding.Raw, _ser.PublicFormat.Raw)
    st, hd, body = r.req("POST /pair-verify", tlv((T_STATE, 1), (T_PUB, eph_pub)))
    print(f"verify M1   -> {st} ({len(body)}B)")
    d = tlv_decode(body)
    if T_ERR in d:
        print("  ✗ verify M2 ERROR =", d[T_ERR].hex()); return
    acc_eph = d.get(T_PUB, b"")
    shared = eph_priv.exchange(X25519PublicKey.from_public_bytes(acc_eph))
    vkey = hkdf_sha512(shared, b"Pair-Verify-Encrypt-Salt",
                       b"Pair-Verify-Encrypt-Info", 32)
    pv2 = chacha(vkey, nonce_label("PV-Msg02"), d.get(T_ENC, b""), encrypt=False)
    print(f"verify M2   -> 对端临时公钥={len(acc_eph)}B 解密={'✓' if pv2 is not None else '✗'}"
          f"（明文 {len(pv2) if pv2 else 0}B）")
    if pv2 is None:
        return
    sig_v = priv.sign(acc_eph + pid + eph_pub)
    sub3 = tlv((T_ID, pid), (T_SIG, sig_v))
    enc3 = chacha(vkey, nonce_label("PV-Msg03"), sub3)
    st, hd, body = r.req("POST /pair-verify", tlv((T_STATE, 3), (T_ENC, enc3)))
    d = tlv_decode(body)
    print(f"verify M3   -> {st} ({len(body)}B)")
    if d.get(T_STATE) != b"\x04":
        print("  ✗ M4 =", d, "（pairingId 未注册或签名内容不符）")
        return
    print("  ✓✓✓ pair-verify 成功 —— 整条认证链路打通")

    # ---------------- 配对后的能力探测 ----------------
    st, hd, body = r.req("GET /info", ctype="text/x-apple-plist+xml")
    print(f"配对后 /info -> {st} ({len(body)}B)")
    if body:
        txt = body.decode("utf-8", "replace")
        import re as _re
        for k in ("width", "height", "features", "model", "name"):
            mt = _re.search(r"<key>" + k + r"</key>\s*<[^>]+>([^<]{0,40})", txt)
            if mt:
                print(f"    {k} = {mt.group(1)}")

    # ---------------- stream-setup 试探 ----------------
    plist = (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        '<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" '
        '"http://www.apple.com/DTDs/PropertyList-1.0.dtd">\n'
        '<plist version="1.0"><dict>'
        '<key>streams</key><array><dict>'
        '<key>type</key><integer>110</integer>'
        '<key>dataPort</key><integer>6030</integer>'
        '</dict></array></dict></plist>'
    ).encode()
    st, hd, body = r.req("POST /stream-setup", plist, ctype="application/x-apple-binary-plist")
    print(f"POST /stream-setup -> {st} ({len(body)}B)")
    if body:
        print("    body:", body[:160])


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print("✗", type(e).__name__, e)
        sys.exit(1)
