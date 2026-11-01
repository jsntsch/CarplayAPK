# AirPlay / CarPlay 配对与加密 —— 实现级规格

> 来源：反编译 **DiPlay 0.2.20**（`com.shilapi.xcertplay.airplay.*`，一个能正常工作的
> CarPlay 接收端），并与 iPhone 原生 CarPlay 的 sysdiagnose 逐字节核对。
> 2026-10-25 整理。**这份是照实现抄的，不是从文档猜的。**

## 0. 先认清一件事

`/pair-setup` 和 `/pair-verify` 是两条不同的路：

| | pair-setup | pair-verify |
|---|---|---|
| 作用 | **注册配对**（交换长期身份） | 用已注册身份**认证本次会话** |
| 密码学 | SRP-6a（PIN）+ Ed25519 | Curve25519 + ChaCha20-Poly1305 |
| 需要前置 | 无（但有 PIN） | **必须有已注册的配对记录** |
| 何时用 | 首次配对 | 每次连接（含自动重连） |

iPhone 原生连车机时**只做 pair-verify**——因为它的配对记录是在 **iAP2/蓝牙**阶段
建立的，不走 RTSP。所以拿我们自己的实现去怼 `/pair-setup` 会一直收
`470 Connection Authorization Required`，那是**方向错了**，不是参数错。

## 1. pair-verify 逐字节

```
M1: TLV8{ 6:State=1,           3:Controller ephemeral pubkey (32B) }
M2: TLV8{ 6:State=2,           3:Accessory ephemeral pubkey (32B),
          5:EncryptedData (120B) }
M3: TLV8{ 6:State=3,           5:EncryptedData (120B) }
M4: TLV8{ 6:State=4 }
```

派生与加解密：

```
shared = X25519(local_eph_priv, peer_eph_pub)
key    = HKDF-SHA512(shared,
                     salt = "Pair-Verify-Encrypt-Salt",
                     info = "Pair-Verify-Encrypt-Info",
                     len  = 32)
nonce(M2) = 12B = 00000000 || "PV-Msg02"     ← label 拷到偏移 4，最多 8 字节
nonce(M3) = 12B = 00000000 || "PV-Msg03"
aad    = 空
布局   = ciphertext || tag(16)               ← BC ChaCha20Poly1305 原生顺序
```

明文结构（**M2 与 M3 完全对称**）：

```
plaintext = TLV8{ 1:pairingId (UTF-8, UUID 36B),
                 10:Ed25519 signature (64B) }
signature = Ed25519Sign(identity_priv,
                        peer_eph_pub || pairingId || local_eph_pub)
```

**长度自证**：36 + 64 + 2 个 TLV 头 = **104B**，+16B tag = **120B**。
这与 iPhone 原生实测的 M2/M3 `EncryptedData` 长度（都是 120B）完全一致，
也与 `M2 = State(2) + PublicKey(32) + EncryptedData(120)` 的 TLV 形态一致
（注意：车机把对方公钥放在**独立 TLV**，不是塞进 EncryptedData 前 32 字节）。

### 接收端校验逻辑（DiPlay `PairVerify.m4`）

1. `enc = tlvs[5]`，用 M2 同一把 key、nonce 换 `PV-Msg03` 解密；
2. 解出 `identifier = tlvs[1]`、`signature = tlvs[10]`；
3. `pubkey = PairingStore.get(identifier)` —— **查不到就 `err(4)`**；
4. `Ed25519Verify(pubkey, peerPub||identifier||localPub, signature)`；
5. 通过 → `verified = true`，派生控制密钥，返回 `TLV8{6:[4]}`。

### 控制通道密钥（pair-verify 之后）

```
writeKey = HKDF-SHA512(shared, "Control-Salt", "Control-Write-Encryption-Key", 32)
readKey  = HKDF-SHA512(shared, "Control-Salt", "Control-Read-Encryption-Key",  32)
```

同类还有（AirPlay 各数据面各自一套）：
`Events-Read/Write-Encryption-Key`、`DataStream-Input/Output-Encryption-Key`。

## 2. pair-setup（注册配对用）

```
M1: TLV8{ 6:State=1, 0:Method=0 }
M2: TLV8{ 6:State=2, 2:Salt(16B), 3:Accessory SRP public B (384B) }
M3: TLV8{ 6:State=3, 4:Proof }
M4: TLV8{ 6:State=4, 4:Proof }
M5: TLV8{ 6:State=5, 5:EncryptedData }   ← Controller 发身份
M6: TLV8{ 6:State=6, 5:EncryptedData }   ← Accessory 回身份
```

SRP 参数（DiPlay `Srp6a` 反编译，逐项确认）：

| 项 | 值 |
|---|---|
| 组 | **RFC 3526 §2，3072 位 MODP**（不是 2048！） |
| g | 5 |
| k（multiplier） | `BigInteger(SHA512( toBytes(N) \|\| pad(g) ))` |
| 哈希 | **SHA-512** |
| 用户名 | `Pair-Setup` |
| PIN | `3939`（`SETUP_CODE`） |
| 分隔符 | `":"`（0x3A） |
| pad() | 左补零到与 N 同长（384B） |

> ⚠️ 之前用「B=255 字节」反推车机用 2048 位组，结论是错的：`/pair-setup` 在
> 这台车机上根本没走通，那个 255 字节很可能不是合法的 SRP B。**以这份 3072 为准。**

M5/M6 的加密与签名常量：

```
salt/info:  "Pair-Setup-Encrypt-Salt" / "Pair-Setup-Encrypt-Info"
nonce:      00000000 || "PS-Msg05"  /  "PS-Msg06"
签名盐/信息: "Pair-Setup-Controller-Sign-Salt" / "-Sign-Info"
             "Pair-Setup-Accessory-Sign-Salt"  / "-Sign-Info"
```

## 3. 对我们实现的要求

1. **先注册配对，再做 pair-verify。** 没有 `PairingStore` 记录时，
   `pair-verify` 的 M4 一定会回 `err(4)`。
2. **长期身份要在两次之间保持一致**（同一对 Ed25519 + 同一个 pairingId），
   自增/随机都会让签名校验失败。已落到 `ocarplay_pairing` 首选项。
3. **本机 CarPlay 走哪条路取决于配对从哪来**：
   - 走蓝牙 iAP2 建立配对 → 之后直接 pair-verify（原生 iPhone 就是这样）；
   - 只有 IP 通道 → 必须先 pair-setup（PIN）。
4. `M2/M3` 的 tag 校验是**精确 oracle**：参数对必然解开，错必然失败。
   调试时先看这一步，不要一上来就改 TLV 类型号。

## 4. 本地实验台

`DiPlay-0.2.20.apk`（`com.shihab.diplay`）可作为**局域网模拟车机**：

- 内含完整 AirPlay/CarPlay 接收端：`_airplay._tcp`、RTSP、pair-setup/verify、
  H.264（`avcC`）；
- 装到同网段的第二台 Android（或本机回环 `127.0.0.1:7000`）即可把
  「Car+ → CarPlay 投屏」这一段单独跑通，不必等真车。

⚠️ ColorOS/OPPO 会拦截 adb 安装（日志 `PackageInstaller: PC install attack detected!`
→ `OPLUS_ADB_INSTALL_CANCEL`，回 `Failure [-99]`）。绕过：把 APK 推到
`/sdcard/Download/`，用手机「文件管理」点安装（属用户安装，不走 adb 拦截）。

## 5. 实测验证（2026-10-25，DiPlay 0.2.20 真跑通）

```
OPTIONS   -> 200
/info     -> 200, 9002B        （DiPlay 不要求先配对，真车回 455）
setup M1  -> 200, 409B         salt 16B + B 384B  → 3072 位组确认
setup M3  -> 200, 69B          M4 proof 校验通过
setup M5  -> 200, 159B         M6 对端 32B 公钥 + Ed25519 签名校验 ✓
verify M1 -> 200, 159B
verify M2 -> 解密 ✓，明文 104B  ← 36(pairingId) + 64(签名) + 4(TLV 头)
verify M3 -> 200, 6B, State=4  ✓✓✓
```

工具：`scripts/diplay_pair_probe.py <接收端IP>`

### 两个必须踩过的细节

**① M3 必须带本端公钥 A**

```
M3 = TLV8{ 6:State=3, 3:A(384B, PAD 过), 4:Proof }
```
只发 `State + Proof` 时，接收端拿不到 A 就算不出 `u`，**固定回 `ERROR=02`**
（Authentication）。这是最容易误判成「PIN 错」的坑。

**② SRP 是 Apple 变体，差异全在细节**

| 项 | Apple/DiPlay | RFC 5054 |
|---|---|---|
| M1 proof | `H( H(N) XOR H(g) ‖ H(I) ‖ salt ‖ PAD(A) ‖ PAD(B) ‖ K )` | 同（XOR 存在） |
| K | `H(toBytes(S))` —— **最小大端，不 pad** | `H(PAD(S))` |
| u | `H(PAD(A) ‖ PAD(B))` | 同 |
| k | `H(toBytes(N) ‖ PAD(g))` | 同 |
| server proof | `H(PAD(A) ‖ clientProof ‖ K)` | 同 |

> ⚠️ 反编译读代码时，如果只过滤 `invoke` / `const` 指令，会**漏掉那段字节级 XOR 循环**
> （`aget-byte` + `xor-int/2addr` + `aput-byte`），从而误判成「没有 XOR」——
> 我就这么绕过一次弯路。

### 接收端启动方式（DiPlay）

只打开 `DiPlayActivity` 时 **7000 不监听**。必须进到
`CarPlayHostActivity`（界面点「连接手机」也一样），日志出现
`phase=WIRELESS stage=ConnectingBluetooth` 后端口才起来——
它虽然还在等蓝牙，但 RTSP 已可用，纯 IP 客户端可以直接连。
