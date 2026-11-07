# APK ↔ Info3.6 连接流程与 iAP2 协商 / Connection Flow & iAP2 Negotiation

适用：`android-project/app/src/main/java/io/carplay/app/secured/MfiCertificateVerifier.java`（iAP2 认证/识别/CarPlay 触发）
与 `Iap2Csm.java`（iAP2 链路层 + CSM 消息编解码，含 `0x4301` StartSession WiFi 凭证解析）。

本文件是把 **2026-11-06 社区抓包研究 + Info3.6 固件逆向交叉验证** 的成果落盘：
补齐「iAP2 识别 → MFi 认证 → CarPlayStartSession → AirPlay 会话」这条此前最模糊的链路。
修复实现见 **V1.7.73（EA 列表，code 310）** 与 **V1.7.74（0x4301 解析加固，code 311）**。

> 角色铁律（与 `SKILL.md §0` 一致，但此处必须再强调一次）：
> **手机是 iAP2 device/iPhone 角色，车机是 accessory 角色。**
> 通信方向推反了就会去改错的一侧。下面所有「手机→车机」「车机→手机」都以此为准。

---

## 1. 完整连接流程 / Full flow

| 阶段 | 消息 ID | 方向 | 作用 | 代码落点 |
| --- | --- | --- | --- | --- |
| 链路层 | `FF55` SYN / `FF5A` 帧 | 双向 | iAP2 链路建链、序号/ACK、会话（control sid=1 / EA sid=3） | `Iap2Csm` 帧解析 |
| 识别启动 | `0x1D00` StartIdentification | **手机→车机** | 手机声明 EA 能力集（index6=to-send / index7=to-receive） | `MfiCertificateVerifier.x(7424, …)` ← **V1.7.73 填 EA 列表** |
| 识别信息 | `0x1D01` (7425) IdentificationInformation | 车机→手机 | 车机能力 + `Bluetooth(17)` / `WirelessCarPlay(24)` 传输组件 + 车机 EA 收发列表 | `a0()` 已解析 index6/7 |
| 识别确认 | `0x1D02` (7426) IdentificationAccepted | 手机→车机 | 确认兼容 | `a0()` 内 `x(7426, …)` |
| MFi 认证 | `0xAA00–AA05` | 双向 | 证书 + ECDSA 挑战应答 | `MfiCertificateVerifier`（已验证通过） |
| 可用通知 | `0x4300` (17152) | 手机→车机 | CarPlay 可用 | `y()` 内 `z(17152, …)` |
| **StartSession** | **`0x4301` (17153)** | **车机→手机** | **携带 WiFi 凭证（SSID/Passphrase/Security）** | `Iap2Csm.y()` ← **V1.7.74 加固** |
| 会话 | AirPlay `SETUP` / `GET` / `/info` | 双向 | 在 WiFi 上建 CarPlay 会话 | `AirPlaySession.t0()` |

> 关键纠正（早期误判已推翻）：之前把日志里 `direction=SEND count=0` 当成「手机广播空表」是错的。
> 那是**车机**在 `0x1D01` 声明它要发的列表为空 —— 因为手机 `0x1D00` 没声明接收，车机也就不声明发送。
> 真病因在手机侧 `0x1D00` 的空 payload，方向是「手机→车机」。

`17152/17153 = 0x4300/0x4301` 是**无线 CarPlay 专用新协议**（`0x4300` 手机→车 = CarPlay 可用，
`0x4301` 车→手机 = StartSession 带 WiFi 凭证），与旧有线协议的 `0x5702/0x5703` WiFi-config 路径并存（手机侧 `a0()` 仍处理 `0x5703`=22275）。

---

### 1.1 旧协议 vs 新协议（社区抓包）

| 方向 | 旧协议 | 新协议 |
| --- | --- | --- |
| 手机→车机 可用通知 | `0x4E0D` (19949) | `0x4300` (17152) |
| 手机→车机 传输通道 ID | `0x4E0E` (19950) | 并入 `0x4300` |
| 车机→手机 AP/网络信息 | `0x5703` (22275) | `0x4301` (17153) |

- `0x4E0E` 是「设备传输通道 ID」，用于无线/有线切换。Info3.6 每次收到手机发来的 `4E0E` 都回
  `IAP2_PEER_RESTARTED`（`last_ack=4E0D`、4E0E 未被 ACK），说明该报文的**载荷或发送时序**不被接受。
- Info3.6 的 `0x1D01` RECEIVE 列表实测为 `4E09–4E0E, EA00, EA01`（旧协议），**不含 `0x4300`**，
  因此新协议可用通知会被 `IAP2_TX_SKIPPED_NOT_ADVERTISED id=4300` 跳过。
- 车机 `0x1D01` 的 SEND 列表实测为空（`count=0 ids=NONE`），所以它不会主动发 `0x5703`/`0x4301`；
  触发条件只能从它接收的旧协议消息里找。

### 1.2 Identification 参数表（社区文档）

| param | 含义 | 手机侧应有 |
| --- | --- | --- |
| 0 | Name | 蓝牙名 |
| 1 | Model | 机型 |
| 2 | Manufacturer | `Apple Inc.` |
| 3 | Serial Number | 稳定序列号 |
| 4 | Firmware Version | iOS 版本 |
| 5 | Hardware Version | 硬件型号 |
| 6 | Messages Sent | EA to-send |
| 7 | Messages Received | EA to-receive |
| 8 | Preferred Language | `zh` |
| 9 | Supported EA Protocols | `com.apple.carplay` 等 |
| 14 | Transport Components | Bluetooth（含 MAC）/ WirelessCarPlay |

- V1.7.73 只补了 6/7；**V1.7.76 补齐 0–5、8**（`bEaPayload()`）。
- 车机是否在 `0x1D01` 的 param 9 声明 `com.apple.carplay`，是判断「它是否愿意走 iAP2 无线 CarPlay」的关键；
  V1.7.76 增加 `IAP2_PEER_EA_PROTOCOLS` 与 `IAP2_PEER_1D01_PARAMS` 转储。
- `4E0x` 的发送载荷与调用栈由 `IAP2_TX_4E0X_PROBE` 记录（V1.7.76），用于定位车机为何在 `4E0E` 后重启。
- **重复 TLV 必须容忍**：补齐身份字段后，Info3.6 的 `0x1D01` 会变成 368 字节并出现**重复 param 10**（语言列表）。
- ⚠️ **两个取值函数都要容错，改错位置等于没改**：`Iap2Csm` 有 `q()`（526 行，缺失返回 null）与
  `v()`（617 行，缺失抛 `Missing iAP2 field`）两个 TLV getter，**两者都有重复即抛的旧逻辑**。
  0x1D01 解析走的是 `v()`，所以：
  - **V1.7.76 回归**：新增的 `IAP2_PEER_1D01_PARAMS` 诊断遍历 `for(k=0..20) Iap2Csm.v(cVar.b, k)`
    自己撞上重复 param 10 → 抛 `Duplicate iAP2 field 10` → 引导在 `waiting=IDENTIFICATION` 终止。
    症状是「诊断标记自己全不打印」（`IAP2_PEER_1D01_PARAMS`/`IAP2_PEER_CAPABILITIES` 均 0 次），
    因为抛点在打印之前。
  - **V1.7.77 修错对象**：只改了 `q()`（仅 1034 行嵌套解析在用），0x1D01 仍走 `v()`，故障照旧。
  - **V1.7.78 正确修法**：`v()` 与 `q()` 的重复分支统一改为「保留首值 + `IAP2_TLV_DUPLICATE_FIELD_KEEP_FIRST`
    告警（`getter=v` / `getter=q` 区分来源）继续」；**诊断遍历一律用 `q()` 并判 null**，探针绝不能抛异常。
  - 判据：`IAP2_TLV_DUPLICATE_FIELD_KEEP_FIRST ... getter=v first_bytes=2 dup_bytes=2` 出现后，
    应紧跟 `IAP2_PEER_1D01_PARAMS 0=.. 1=.. 10=..x2 ...` 与 `IAP2_PEER_CAPABILITIES`。

## 1.5 ⚠️ 官方流程（Apple CarPlay 规范 §23.2.7，2026-11-06 核实）

**先看这一节再看 §2** —— 它纠正了此前对 `0x4E0x` 的整体误判。

规范原文（`carplay.pdf`，106 页，源：Gitee `beautea101/WirelessCarPlay`）：

> For CarPlay over wireless, at a minimum, the iAP2 client must support:
> `AccessoryAuthentication` · `AccessoryIdentification` with **BluetoothTransportComponent,
> VehicleInformationComponent, and WirelessCarPlayTransportComponent** ·
> **`RequestAccessoryWiFiConfigurationInformation`** and **`AccessoryWiFiConfigurationInformation`**.
> **The iAP2 client over Bluetooth is used only to provide Wi-Fi credentials and must be disconnected
> once the CarPlay session is established.**

由此得到三条硬结论：

1. **蓝牙 iAP2 的唯一职责是传 WiFi 凭证** —— 即 `0x5702`（请求，`RequestAccessoryWiFiConfigurationInformation`）
   → `0x5703`（下发，`AccessoryWiFiConfigurationInformation`），拿到凭证后**必须断开蓝牙**。
2. **`0x4E09..0x4E0E` 不在无线 CarPlay 的必需流程里**。此前把它当成"CarPlay 握手必经"是误判，
   攻 `4E0E` 的五个版本（V1.7.73~83）都属于走错方向。
3. **Accessory 必须声明 `WirelessCarPlayTransportComponent`（iAP2 param 24）** 才算支持无线 CarPlay。

**Info3.6 实机对照**：`0x1D01` 只有 param 17（`CinemoBTComponent`），**param 24 缺失**、param 6（SEND）为空
⇒ 按官方定义这台车机不具备无线 CarPlay 能力，也不会主动下发 `0x5703`。这解释了全部现象：
投屏能起（普通 AirPlay 镜像）、触摸不可用（未登记为 CarPlay）、`4E0E` 一律被拒。

> 判据：`IAP2_TRANSPORT_CHANNEL_ID_SKIPPED reason=not_required_by_carplay_spec`（V1.7.84 起停发 4E0E）
> 之后若仍无 `IAP2_RX_5703`，即确认该车机无无线 CarPlay 能力，应转向有线（USB）或放弃 CarPlay 登记。

## 2. 识别阶段 EA 列表协商（核心门控）/ Identification EA negotiation — the gate

### 2.1 字段编码
`0x1D00` / `0x1D01` 的 TVL 子项里：
- **index 6 = to-send**：本端**将要发给**对端的 EA 消息 ID 列表
- **index 7 = to-receive**：本端**希望接收**的 EA 消息 ID 列表

每个 ID 占 2 字节（大端）。写入由 `Iap2Csm.B(int index, byte[] ids)` + `Iap2Csm.f(...)` 拼接。

### 2.2 车机侧门控 `R()`（关键判据）
`MfiCertificateVerifier.R()`（约 406 行）的成立条件：

```java
!this.b && this.m.contains(17153) && this.l.contains(17152) && !this.l.contains(22274)
```

- `this.m` = 车机 `0x1D01` 的 **index6（车机 SEND）** —— 车机声明「我要发 17153 给你」
- `this.l` = 车机 `0x1D01` 的 **index7（车机 RECEIVE）** —— 车机声明「我要收 17152 等」
- 车机是否在 SEND 里放 17153，取决于**手机**在 `0x1D00` 的 to-receive（index7）有没有声明想要 17153。

### 2.3 根因（V1.7.73 前）
三处 `x(7424, new byte[0])`（782 / 1401 / 1424 行）—— 手机 `0x1D00` payload 恒为空
→ 车机在 `0x1D01` 的 SEND 列表也为空 → `this.m.contains(17153)` 永远 false →
`R()` 恒 false → **车机永不发 `0x4301`** → 手机永久 `waiting=CARPLAY_START_SESSION`，
`AirPlaySession.t0()`（SETUP/GET /info）从未执行。

### 2.4 修复（V1.7.73 起；**ID 在 V1.7.79 更正**）

```java
// ⚠️ 必须按十六进制换算，禁止手算十进制：
//   0x4E09..0x4E0E = 19977..19982（旧协议 CarPlay 握手）
//   0x5702 = 22274（手机请求 AP 信息）、0x5703 = 22275（车机下发 AP 信息）
//   0xEA00/0xEA01 = 59904/59905
int[] toSend    = {19977,19978,19979,19980,19981,19982, 22274, 59904,59905};
int[] toReceive = {17153,17152, 22275, 19977,19978,19979,19980,19981,19982, 59904,59905};
// index6 = toSend（手机能发的），index7 = toReceive（手机想收的，决定车机 SEND 列表）
```

三处 `x(7424, ...)` 都用 `bEaPayload()`：
- 782 行：MFI 挑战应答后
- 1412 行：`IDENTIFICATION_FIRST` 分支
- 1438 行：`AUTHENTICATION_FIRST` 分支（`this.C0 ? 43520 : 7424` —— 仅 `7424` 填 EA 列表，
  **`43520` 是独立 auth 消息，不塞 EA 列表**）

### 2.5 ID 换算铁律（V1.7.79 血泪教训）
- `19945..19950` 是 **`0x4DE9..0x4DEE`**，不是 `0x4E09..0x4E0E`，两者差 `0x20`。
- 后果：手机在 to-send 里声明了一组车机不认识的 ID，而实际发出的是 `0x4E09..0x4E0E`，
  等于**发了未协商的 EA 消息**。车机对 `4E0E`（设备传输通道 ID）不 ACK，随即
  `IAP2_PEER_RESTARTED`（`last_ack=4E0D`，`4E0E` 重试 2 次后放弃）。
- 换算一律用命令，不靠心算：`printf "%d\n" 0x4E0E` → `19982`。
- 判断「车机收了哪些」看 `IAP2_PEER_MESSAGE_IDS`（车机 param 7），
  判断「车机能发哪些」看同一行的 `direction=SEND`（车机 param 6）。

### 2.6 双向协商语义（想收什么，必须自己先说想收）
iAP2 的 EA 集合是**交集**：A 的 to-send ∩ B 的 to-receive。
- 想让车机发 `0x5703`（旧协议 AP 信息，无线 CarPlay 的 WiFi 凭证来源），
  手机**必须先在 to-receive 声明 22275**，否则车机不会把它放进 SEND。
- 手机要发 `0x5702` 时，`z(int, byte[])` 会检查车机 RECEIVE 是否含 22274，
  不含则打 `IAP2_TX_SKIPPED_NOT_ADVERTISED id=5702` 并跳过（不是错误，是协商未成立）。
- 同理 `IAP2_TX_SKIPPED_NOT_ADVERTISED id=4300` = 车机不支持新协议 `0x4300`，
  只能走旧协议 `0x4E0D/0x4E0E`。

### 2.7 V1.7.78 实机观测（Info3.6 / myBuick GS）
`authenticated=true`，已走到 `4E0E`：`last_tx=4E0E last_ack=4E0D waiting=CARPLAY_START_SESSION`；
车机 `0x1D01` 关键字段（`IAP2_PEER_1D01_PARAMS`）：

| 字段 | 观测 | 含义 |
| --- | --- | --- |
| param 6 | 空/缺失 | 车机不声明自己要发任何 EA 消息 ⇒ 不会主动发 `0x5703` |
| param 7 | 16B = 8 项 `4E09..4E0E,EA00,EA01` | 车机只收旧协议集 |
| param 10 | `32x4`（128B） | 4 条 32 字节语言串（UTF-16），重复属正常，first-wins 即可 |
| param 24 | 缺失 ⇒ `wireless_carplay=0` | **不支持新协议 `0x4301`**，只能走旧协议 |
| param 17 | 42B | 蓝牙传输组件（含 MAC） |

### 2.8 旧协议 `0x4E0x` 实测帧结构（V1.7.79 探针，Info3.6 实测）
手机在识别完成后按 `4E0C → 4E09 → 4E0A → 4E0D → 4E0E` 顺序发 5 条，前 4 条被 ACK，**只有 `4E0E` 不被 ACK**：

| 消息 | 帧长 | param 0 内容 | 车机反应 |
| --- | --- | --- | --- |
| `0x4E0C` | 57B | UUID 字符串 `0f5046a7-c8c1-40e5-a0c1-380686ae807f` | ACK |
| `0x4E09` | 35B | `"CarPlay PKJ110"`（设备名） | ACK |
| `0x4E0A` | 23B | `"zh"` | ACK |
| `0x4E0D` | 21B | **1 字节 `01`**（CarPlay 可用状态） | ACK |
| `0x4E0E` | 38B | 旧实现：`"E0:42:6D:B6:D5:78"`（18B MAC 字符串） | **不 ACK** |

`4E0E` 原始帧（V1.7.79）：
```
FF5A0026 40 56 B2 01 38 | 4040 001C 4E0E 0016 0000 45303A34323A36443A42363A44353A3738 00 1B
                          └ CSM  └len=28 └ID   └pLen=22 └p0    └ "E0:42:6D:B6:D5:78" + NUL
```

**触发重启的确凿时间线**（ms 级）：`4E0D`(seq=85) 被 `ack=85` 确认 → `4E0E`(seq=86) 发出后车机
**沉默 1506ms**（始终不回 `ack=86`）→ 手机 `retry=1` → 20ms 后车机发 SYN 重启链路
（`IAP2_PEER_RESTARTED`，`last_ack` 停在 `4E0D`）。

**载荷黑名单（都已实测，别再试）**：

| 尝试 | 载荷 | 结果 |
| --- | --- | --- |
| V1.7.73~79 | `TLV(0, "E0:42:6D:B6:D5:78")`（18B MAC 串） | 不 ACK + 1.5s 后 SYN 重启 |
| V1.7.80 | 空 payload（帧 16B，`4040 0006 4E0E`） | 同样不 ACK + 重启 |
| V1.7.81 | `TLV(0, uint16 ComponentID)`（取自车机 param 17） | 待实测 |

### 2.9 车机 `param 17` 传输组件结构（Info3.6 实测，42B）

`0x1D01` 的 param 15/16/17/24 值本身是**嵌套 TLV**（4 字节头：len(2) + id(2)），长度完全闭合：

```
00 06 00 00 00 01                                   len=6,  id=0, ComponentID = 1
00 16 00 01 "CinemoBTComponent" 00                  len=22, id=1, ComponentName
00 04 00 02                                         len=4,  id=2, 空
00 0A 00 03 "E0:42" 00                              len=10, id=3, 蓝牙地址（车机侧被截断）
```

要点：
- **ComponentID = 1**（子 param 0，uint16 大端），`0x4E0E` 要用它回填。
- 组件名是 `CinemoBTComponent` —— Cinemo 是车机的 AirPlay 栈，说明车机把手机当成一个 Cinemo 传输组件。
- 车机自己已带手机 MAC 前缀 `E0:42`，所以「发 MAC 让车机认识手机」这个前提从一开始就不成立。
- 解析实现见 `MfiCertificateVerifier` 的 `bleCompId` 字段与 `IAP2_PEER_TRANSPORT_COMPONENT_ID` 诊断。

> 判据（V1.7.82）：`IAP2_IDENTIFICATION_EA ... transport17_bytes=42 transport17_id=1`，
> 随后 `4E0E` 应被 ACK（`IAP2_TX_ACK id=4E0E`，`last_ack` 前进），不再有 `IAP2_PEER_RESTARTED`。

### 2.10 `4E0E` 三种载荷全部失败的教训（V1.7.73~81 实测）

| 版本 | `4E0E` 载荷 | 帧长 | 结果 |
| --- | --- | --- | --- |
| V1.7.73~79 | `TLV(0, "E0:42:6D:B6:D5:78")` | 38B | 不 ACK + 1.5s 后 SYN 重启 |
| V1.7.80 | 空 payload | 16B | 同上 |
| V1.7.81 | `TLV(0, uint16 ComponentID=1)` | 22B | 同上 |

三次结构都合法、checksum 正确，车机一律沉默 1.5s 后重启 ⇒ **问题不在编码，而在「引用的目标不存在」**。

**根因**：手机在 `0x1D00` 里只发了 param 0–8，**从未声明自己的传输组件**（param 15/16/17/24 全缺）。
`0x4E0E` 的语义是「引用某传输组件 ID」，引用一个手机从未声明过的组件，车机只能判非法。

**V1.7.82 修法**：把手机自己的蓝牙传输组件写进 `0x1D00` 的 param 17，结构完全照车机实测的那份：

```java
byte[] btComponent = Iap2Csm.f(
        Iap2Csm.B(0, new byte[]{0x00, 0x01}),   // 子0 ComponentID = 1（与 4E0E 引用值一致）
        Iap2Csm.B(1, Iap2Csm.A(name)),         // 子1 ComponentName
        Iap2Csm.B(2, new byte[0]),             // 子2 空（车机原样如此）
        Iap2Csm.B(3, Iap2Csm.A(serial)));      // 子3 蓝牙地址
// ...
Iap2Csm.B(17, btComponent)
```

### 2.11 车机侧 `4E0x` 实现位置（固件结论）

- `system/bin/MediaManagerService`（1.1MB，Cinemo 媒体服务）是唯一含 `CinemoBTComponent` 的可执行体，
  也是唯一有 `0x4E0x` 立即数的文件：**72 处** `mov dword ptr [rip+X], 0x4e0x`。
- 它们构成一张「消息注册表」，形态 `{id, 4, slot}`：
  `{0x4c01,4,2}` `{0x4c04,4,3}` `{0x4e09,4,5}` `{0x4e0a,4,6}` `{0x4e0e,4,0x3f(63)}` `{0x5001,4,7}`。
  **只登记 ID 与槽位，不含 payload 结构**，所以从这张表推不出消息体。
- `libNmeCarPlay.so`（注意是 **x86-64**，不是 ARM64）、`libNmeSDK.so` 等 30+ 个 .so 均为传输/媒体层，无 `4E0x` 处理。
- `GMCarPlay.apk` 只有资源、无 dex；`GMCarPlay.odex` 是 HMI 层（`CarPlayServiceManager`/`ICarPlayListener`）。

`bEaPayload()` 内打印 `IAP2_IDENTIFICATION_EA to_send=… to_receive=…`，复现时一眼确认非空。

### 2.12 会话生命周期与配对（V1.7.84 实测，Info3.6）

**停发 `0x4E0E` 的效果**：该轮 iAP2 会话 `IAP2_PEER_RESTARTED=0`，`4E0C/4E09/4E0A/4E0D` 全部 ACK —— iAP2 侧不再被重启。
但车机仍不发 `0x5703`、仍不连 `_carplay-ctrl`（`CTRL_INVITATION_ACCEPTED=0`、`CTRL_TCP_ACCEPT=0`）。

**投屏会话实测寿命只有 18/33/36/41 秒**，结束方式必须分类看，否则会误判成同一个 bug：

| 结束标记 | 归属 | 含义 |
| --- | --- | --- |
| `USER_NOTICE 已断开连接` + `Stopped by user` + `first_channel_end=NONE` | 本地 | `MainActivity.n1()` 主动停（用户点断开） |
| `SESSION_FAILURE detail=EVENT_CHANNEL_SocketException message=Connection reset` | 车机 | 车机 reset 事件通道（`read_calls=1 raw_bytes=0`） |
| `NATIVE_CHANNEL_FAIL channel=VIDEO error=IO` + 全部媒体通道同时 IO 失败 | 车机 | 车机侧关闭媒体连接 |
| `NATIVE_SOURCE_FINAL first_channel_end=NONE` | 本地 | 本地 close 但无通道报错 |

**`/info` features 位域解码**（Info3.6 实测 `416679199360 = 0x6104040280`，位表按 UXPlay / openairplay）：

| bit | 名称 | 本机 |
| --- | --- | --- |
| 7 | Screen (镜像) | ✅ |
| 9 | Audio | ✅ |
| 18 | AudioFormat1 | ✅ |
| 26 | HasUnifiedAdvertiserInfo | ✅ |
| **32** | **isCarPlay** | ✅ 车机自认 CarPlay |
| 37 / 38 | CarPlayVoice / UnifiedMediaControl(CoreUtils) | ✅ |

⇒ 车机**在 AirPlay 侧声明了 isCarPlay**，同时 `CARPLAY_INFO_KEYS` 含 `hidDevices`/`displays`，
`HID_GATE_OPEN … devices=2` 说明它声明了 **2 个 HID 设备**。所以「没触摸」不是车机没声明 HID，
而是**会话没被登记成 CarPlay**（`HID_INPUT_MODE_ACCEPTED` 极少、`HID_REPORT_ARRIVED=0`、`INBOUND_REQ=0`）。

**配对是另一个独立缺陷**：`CARPLAY_PAIR_VERIFY_RESUMED_STORED_PEER` 恒为 0，
设备 `shared_prefs/carplay-authenticated-peers.xml` 有记录但长期不更新
⇒ 我们每次连接都做**全新 PairSetup**，车机每次都当成新设备弹「重新配对」界面。
WWDC 2017《Developing Wireless CarPlay Systems》明确：配对只做一次，之后进车自动重连、不应再要求确认。
V1.7.85 起改为「有存储 peer 时先 `/pair-verify`，失败回退 `/pair-setup`」，打点
`PAIR_VERIFY_FIRST_OK` / `PAIR_VERIFY_FIRST_FAILED … fallback=pair_setup` / `PAIR_SETUP_PEER id_matched=`。

> 判据：`PAIR_VERIFY_FIRST_OK` 命中 ⇒ 车机不再弹配对界面；`PAIR_SETUP_PEER id_matched=false` 持续出现
> ⇒ 车机每次给新的 pairing identifier（那才是「每次弹配对」的根因，需要另修）。

### 2.13 触摸无反应的规范级根因（CarPlay HID / Modes，V1.7.86）

**方向先说清**（容易搞反）：CarPlay 的 **HID 设备在车机（Accessory）侧**。
`hidSendReport` 的 **Sender = Accessory**（车机发触摸给手机），`hidSetInputMode` 的 **Sender = Apple device**（手机设输入模式）。
手机端要做的是：发 `hidSetInputMode`、接收 `/command` 上的 `hidSendReport`，再注入本机输入系统（`TOUCH_FORWARDED`）。

**决定性门控条款**（§23.3.5.1.3 与 §23.3.5.2 两处一致）：

> **When the Apple device does not own the screen, the accessory must not send these HID usages.**

⇒ 车机是否发触摸，**只取决于它认为 MainScreen 归谁**。资源归属靠 `modesChanged`（Apple device → accessory）告知。

**`modesChanged` 的规范结构（Table 23-71 / 23-73）**

```xml
<dict>
  <key>type</key><string>modesChanged</string>
  <key>resources</key><array>
    <dict><key>resourceID</key><integer>1</integer>   <!-- 1=MainScreen 2=MainAudio -->
          <key>entity</key><integer>1</integer></dict> <!-- 1=Controller(Appledevice) 2=Accessory -->
  </array>
</dict>
```

**两个坑（都实测踩过）**：

| 坑 | 错误写法 | 后果 |
| --- | --- | --- |
| 参数包在 `params` 里 | `{"type":"modesChanged","params":{...}}` | 车机读不到顶层 `resources`，**静默**返回 `status=0`，归属不变 |
| 用 `transferType` 而非 `entity` | `{resourceID:1,transferType:1,...}` | `transferType/transferPriority/takeConstraint` 是 **changeModes（车机→手机）** 的字段，modesChanged 不认 |

⇒ 两条同时错 = 车机始终认为自己持有屏幕 = 依规范**永不发送触摸**。这与实测完全一致：
`CARPLAY_SCREEN_MODES_SENT screen_phone=true` 52 次，但 `INBOUND_REQ=0`、`CARPLAY_COMMAND_RECEIVED=0`、
`HID_REPORT_ARRIVED=0`、`TOUCH_FORWARDED=0`。

**同类坑**：`hidSetInputMode`（Table 23-75）要求顶层 `hidInputMode` + `uuid`；`setUIContext` 同理顶层 `uiContext`。
判据：§23.3.7 的 `hidSendReport` 示例（page 481）——`type/hidReport/uuid` **都是顶层键**，没有 `params` 包装。
`params` 只出现在 **changeModes 的响应**（Table 23-70）里。

> 已知有效标记：`CARPLAY_SCREEN_MODES_PAYLOAD keys=[type, resources] body={resources=[{resourceID=1, entity=1}], type=modesChanged}`
> 之后应看到车机回 `CARPLAY_COMMAND_RECEIVED type=hidSendReport` → `HID_REPORT_ARRIVED` → `TOUCH_FORWARDED` 递增。

**历史日志比对法（判定「触摸链路是否开始过」）**：合并 `logs/capture-*/diag-*/logcat.txt` 后统计

```bash
cat logs/capture-*/diag-*/logcat.txt | grep -ac "INBOUND_REQ"        # 车机→手机 请求总数
cat logs/capture-*/diag-*/logcat.txt | grep -ac "HID_REPORT_ARRIVED" # 车机→手机 触摸报告
cat logs/capture-*/diag-*/logcat.txt | grep -ac "TOUCH_FORWARDED"    # 注入本机
```

v1.7.72~1.7.84 共 12 份抓包：三项**全为 0**，而 `V153_EVENT_READY=true` 有 74 次 ⇒ 通道通、车机不发。
这是「触摸无反应」的最简判定：**先看车机有没有发，别先查手机侧注入**。

**输入模式枚举只有 0..4**（Table 23-76）。历史 `HID_GATE_OPEN mode=5 devices=2` 出现 66 次属无效值
（`HID_INPUT_MODE_ACCEPTED` 从不出现在 mode 2/5），V1.7.87 已把扫掠收敛到 0..4。

**注意 `CAPS ... touch=0` 是日志统计项自身的 bug**（`map8` 存 displayUUID 出现次数，被当成显示索引比较），
与 `HID_CAPABILITIES devices=N touch=M`（这行才是真实解析结果）矛盾时，**以后者为准**。

### 2.16 显示 UUID 对照（V1.7.88 新增）

规范 §23.3.5.1.3 / §23.3.5.2：**Apple device 不持有屏幕时，Accessory 不得发送 HID 用途**。
判定「持有」的唯一依据是显示 UUID：车机 `hidDevices[i].displayUUID` 必须等于我们声明的主显示 UUID。
这两值过去从未打印，导致「屏幕归属没谈成」和「车机压根不发 HID」无法区分。V1.7.88 补齐：

| 标记 | 字段 | 判读 |
| --- | --- | --- |
| `CARPLAY_DISPLAY_UUIDS count=N main=<uuid> main_stream=110 main_rate=60 [i]uuid=…,type=…` | `/info` 的全部 display | `main` 就是 `arrayList.get(0)` 选中的那一个 |
| `HID_TOUCH_DESCRIPTOR … dev_uuid= display_uuid= main_uuid= display_match_main=true\|false` | 每个触摸设备 | `display_uuid=none` ⇒ 车机没给它绑显示；`false` ⇒ 绑到了别的显示 |
| `HID_CAPABILITIES devices=N touch=M back=K consumer=J main_uuid=…` | 汇总 | 与上面逐条对照 |

**根因判据（按优先级）**：

1. `display_match_main=true` 但 `HID_REPORT_ARRIVED=0` ⇒ 不是显示绑定问题，查 `modesChanged` 是否真正生效（`CARPLAY_SCREEN_MODES_PAYLOAD`）。
2. `display_match_main=false`（尤其 `display_uuid` 指向 `count>1` 时的另一块显示）⇒ **主显示选错了**，
   应改 `arrayList.get(0)` 的选择策略（车机主屏通常是 `type=110` 且分辨率最大的那块）。
3. `display_uuid=none` ⇒ 车机没把 HID 关联到任何显示，需在 `/info` 响应的 `displays` 声明上排查（我们回给车机的显示 UUID 集合）。

`main_uuid` 与 `display_uuid` 若都是数字串（非标准 UUID 格式），说明车机用整数 ID 而非 UUID，
此时 `ProtoFieldAccessor.c()` 按字符串比较仍然有效，但不能当作 Apple 侧 UUID 使用。

---

## 3. CarPlayStartSession `0x4301` (17153) WiFi 凭证解析 / StartSession WiFi parsing

### 3.1 标准编码（嵌套布局，MFi 实机互通结构）
`Iap2Csm.y()`（640 行）解析：取 message **field 1**，把它**重新当作 TLV** 解析，再取子字段：
- sub 0 = SSID
- sub 1 = Passphrase
- sub 4 = Security type

这是从真机 CarPlay 逆向得到的结构，MFi 认证车机（含 Info3.6）几乎肯定用此布局。

### 3.2 社区文档描述的变体（扁平布局）
部分 CarPlay 抓包文档（CSDN 无线连接避坑指南）描述车机直接把以下字段编码为 `0x4301` 的**顶层**字段：
- 顶层 field 1 = SSID
- 顶层 field 2 = Passphrase
- 顶层 field 3 = Security type

### 3.3 修复（V1.7.74）
`Iap2Csm.y()` 内新增三处加固（均零风险：标准嵌套布局正常工作时完全不触发兜底）：
1. **诊断日志** `CARPLAY_START_WIFI_TOPLEVEL_FIELDS`：dump 车机 `0x4301` 顶层 field ID，复现即知真实编码布局。
2. **扁平布局兜底**：嵌套布局 sub0/sub1/sub4 不全时，自动回退到顶层 field 1/2/3（`CARPLAY_START_WIFI_LAYOUT flat`），
   避免车机用变体编码时 WiFi 凭证取不到、永不连 AP。
3. **identifier NPE 防护**：`0x4301` field 3 的 identifier 字段在非 null 结尾字符串时不再崩溃，按空处理
   （`CARPLAY_START_IDENTIFIER_INVALID_TREATED_EMPTY`），避免变体布局下端点被拒。

---

## 4. 诊断标记速查 / Diagnostic markers

| 标记 | 含义 | 出现即说明 |
| --- | --- | --- |
| `IAP2_IDENTIFICATION_EA to_send=… to_receive=…` | V1.7.73 EA 列表已填 | 手机发了非空 `0x1D00`（修复生效） |
| `IAP2_PEER_MESSAGE_IDS direction=SEND … 17153` | 车机 SEND 列表含 17153 | `R()` 将转 true，车机即将发 StartSession |
| `waiting=CARPLAY_START_SESSION` | 手机在等车机发 17153 | 该计数**归零**即门控通过 |
| `CARPLAY_START_WIFI_TOPLEVEL_FIELDS <ids>` | 0x4301 顶层字段 | 看车机实际编码布局 |
| `CARPLAY_START_WIFI_LAYOUT nested_…` / `… flat` | 嵌套/扁平布局 presence | `flat` 表示 Info3.6 用扁平编码，需据 §3.2 对齐字段 |
| `IAP2_AUTHENTICATED_START_HINT` | 认证后启动提示 | 与连 AP 动作一起出现即 StartSession 被消费 |
| `CTRL_INVITATION_ACCEPTED` | 车机回连控制通道 | 会话真正建立 |

---

## 5. 验收流程（现场复现）/ Acceptance

1. 手机开 **CarPlayAPK** → 选 `myBuick` → 点 **「开始连接」**（升级后须手动点，否则不广播 `_carplay-ctrl`）。
2. 按车机提示接入无线 CarPlay，等会话尝试建立/失败。
3. 跑 `logs/capture-20261106-174/capture.sh`（或对应版本）抓取。
4. 判定：
   - `IAP2_IDENTIFICATION_EA` 出现 → 非空 Identification 已发；
   - `IAP2_PEER_MESSAGE_IDS direction=SEND` **含 17153** → `R()` 转 true、`waiting` 归零；
   - `CARPLAY_START_WIFI_TOPLEVEL_FIELDS` / `LAYOUT` 出现且 SSID 取到 → `0x4301` 解析成功；
   - `IAP2_AUTHENTICATED_START_HINT` + 连 AP 动作 + `CTRL_INVITATION_ACCEPTED` + `t0()`（SETUP/GET /info）跑通。

---

## 6. 边界与待验证 / Boundaries — unverified

- 上述编码与门控逻辑来自**社区抓包 + 固件静态逆向**，**未在真车（myBuick GS）端到端验证**。
  真车实装的是 V1.7.74 诊断包，待用户复现抓取后比对 §4 标记确认。
- 若复现日志 `CARPLAY_START_WIFI_LAYOUT flat`：说明 Info3.6 用扁平编码，需据 §3.2 把
  port / address / identifier 的字段位置也精确对齐（届时 `CARPLAY_START_WIFI_TOPLEVEL_FIELDS`
  会给出真实 field ID，据此改 `Iap2Csm.y()` 的 `q(cVar.b, n)` 取值）。
- `0x4301` 的 WiFi 凭证取全后，还须确认手机真去连车机 AP 并跑通 DHCP/会话，否则链路仍停在 §1 最后一行。

## 6.5 ⚠️ 日志打点陷阱（必读）/ Logging pitfall — read this

`SyntheticRefs.c(String)` **不是日志函数**，它的实现是：

```java
public static void c(String str) throws IOException {
    throw new IOException(str);   // 抛异常，不是打印
}
```

- 在 `bEaPayload()` 里用它「打点」→ 抛出的 IOException 被同方法的 `catch (IOException) { return new byte[0]; }`
  吞掉 ⇒ **EA 列表恒为空，修复等于没做**（V1.7.73 的真实情况）。
- 在 `Iap2Csm.y()` 里用它「打点」→ 17153 一进来就抛异常，解析直接失败（V1.7.74 的真实情况）。
- 正确入口：
  - `ControlChannel.Log(String)`：`public static`，写 logcat tag `CarPlayAPK`，**static 上下文可用**。
  - `MfiCertificateVerifier.c0(String)`：private 实例方法，写应用内文件日志（`this.a.h`）。
- 教训：**加诊断日志前先确认该函数是否真的打印**；`throws` 声明是强信号。
  Before adding diagnostics, verify the helper actually logs — a `throws` clause is a red flag.

## 7. 参考来源 / Sources

- wiomoc/iap2 深度解析（Identification 消息结构、EA 协议消息 ID 列表方向）
- CSDN《无线 CarPlay 连接避坑指南》（`0x4300/0x4301` 含义、`0x4301` 扁平字段布局、车机 RECEIVE 列表语义）
- Apple WWDC17-717（无线 CarPlay 架构：车机发 StartSession 带 WiFi 凭证；**配对只做一次、之后自动重连**）
- mib2q-carplay 固件逆向（EA session 消息 ID 取值对照）
- 本仓库 `firmware/Buick Info3.6/` 静态逆向（`libNmeCarPlay.so` 的 `startSession` 串、车机 RECEIVE 期望集 `4E09–4E0E,EA00,EA01`）
- **Apple 官方规范 PDF（已入库，勿再联网找）**：
  - `docs/Apple-CarPlay-Specification.pdf`（106 页；§23.2.7 无线 CarPlay 的 iAP2 必需组件与「蓝牙只传 WiFi 凭证」条款）
  - `docs/iAP2-Accessory-Interface-Spec.pdf`（iAP2 概述，10 页，无逐消息定义）
  - 来源：Gitee 镜像 `beautea101/WirelessCarPlay`（GitHub 原仓库已 404）
- UXPlay / openairplay feature 位表（`/info` 与 mDNS `features` 位域解码，注意两者编号不同：mDNS bit32 是 SupportsVolume，
  `/info` bit32 是 isCarPlay）
