# 车机 CarPlay「设备表」与识别条件分析（Buick Info3.6）——修正版

> **本文修正上一版的错误结论**。上一版依据 `MediaManagerService` 的 `startProjectionAuth/extractDeviceName(path)`
> 判定「必须 USB 有线首次认证」，这是错的：那条链是**有线 CarPlay 分支**，与无线无关。
> 用户实测确认：**本车机无线 CarPlay 可直连，不需要先插 USB**。
> 本版以 iOS sysdiagnose（278 万行）为准，重建无线直连的真实链路。

---

## 0. 一句话结论

车机**有设备表**，但它不是"白名单"，而是**「本次 AirPlay 会话对应的 CarPlay 控制器」表**：
车机在 **AirPlay 会话建立后**才去 browse `_carplay-ctrl`，把解析到的 `(service_name, 6字节MAC)`
塞进 `libNmeCarPlay +0x9e0`，再用 `SendConnect` 做**双串匹配**，命中才发 `GET /ctrl-int/1/connect`。

**⇒ 手机不投屏时，车机根本不会 browse。** 我们之前"纯广播观察态下车机零探测"的结论是**无效实验**。

---

## 1. 真 iPhone 无线直连全过程（sysdiagnose A 级证据）

| # | 日志 | 含义 |
|---|---|---|
| 1 | `WiFi Network changed to: myBuick GS, **IsCarPlay: Y, CarPlay UUID: F72E8BA5-6918-4836-9B85-E852CB77EA38**` | 连上该 SSID 后 iOS 立刻把网络标记为 CarPlay 网络并绑定 UUID |
| 2 | `(P2P/IFEF_DIRECTLINK/IFEF_AWDL/**IsCarPlaySSID**)` | WiFi join flags 里带 `IsCarPlaySSID` |
| 3 | `Registering Bonjour **_carplay-ctrl._tcp port 51841**`（mediaremoted）→ `Registered Bonjour **iPhone_SCH._carplay-ctrl._tcp.local.**`（audiomxd） | 手机注册 ctrl 服务，端口是**动态高端口**，实例名 = `iPhone_SCH` |
| 4 | `Creating shared connection to browse for **_airplay._tcp.**` / `Starting browse for _airplay._tcp. on if en0, flagsAlt 0x2000000, flags 0x3000000000000000 < NoAWDLThreshold Traffic >` | 手机**同时** browse 车机的 AirPlay 服务 |
| 5 | `Created **APEndpointCarPlay** [0xB4A5] with name '**CarPlay**' and id '**10:E8:A7:13:AD:5B**'` | 车机 `_airplay._tcp` 的实例名就是 `CarPlay`，`deviceid` 是**车机 BT MAC** |
| 6 | `Added new WiFi endpoint 0xB4A5 (**CarPlayControlSupported: Y**)` | 该端点被判定为支持 CarPlay 控制 |
| 7 | `Connecting to **192.168.5.1:7000**` → `**192.168.5.1:5001**`（→ 5020/eventPort） | **手机主动连车机**，与我们 APK 行为一致 |
| 8 | `[Session/WiFi/USB] WiFi eligible: N (**UUID Mismatch**, Session Start Timeout, ...)` | UUID 不匹配**并不阻断**连接，仅是"eligible"判据 |

**关键**：iPhone 也是"手机主动连 7000"，所以我们连 7000 的方向是对的。
差别不在连接方向，而在**车机是否愿意在这条会话上做 CarPlay 控制**。

---

## 2. 车机侧机制（固件 A 级）

```
_CarPlayControlClientEnsureStarted@0x52fc0
   └─ browse _carplay-ctrl._tcp                          (0x5303b)
        └─ BrowseCallback@0x536e0  (event 1=Add / 2=Remove / 3=Stop / 4=Restarted)
             └─ ControlClientCallback@0x53c00
                  └─ IosControlClientCallback@0xe1a0     ( cmp ebp,2 → AddOrUpdateController )
                       └─ 0x52ac0 取 name  +  0x52b90 取 6字节 MAC
                            └─ AddCarPlayClient@0x178b0   → 填对象 +0x9e0 列表
SendConnect@0x17e10 (唯一调用者 @0x14017，日志 device_id=%s, service_name=%s)
   └─ 用 device_id + service_name **双串匹配** +0x9e0；未命中返回 0xb，**不发 connect**
```

配套字符串（`libNmeCarPlay.strings.txt` / `MediaManagerService`）：
- 车机 EA 协议：`com.buick.vip`（index 1）、`com.buick.noprotocol`（index 3）、`com.gm.noprotocol`、`com.gm.vip`
- 均带 `ACCExternalAccessorySessionUsesSocketInterfaceKey = 1`（走 **socket**，不是 USB）
- 车机 accessory UUID（iOS 侧视角）：`7EF80867-5F54-4900-A878-BFF3D34AFBCE`
- 无线 iAP2 传输：`iap://wifi://`（`CINEMO_OPTION_CARPLAY_WIFI_TRANSPORT_NAME`）
- iPhone 蓝牙 SDP 服务表：`HFP PhoneBook AVRCP A2DP HID **WirelessiAP** NetSharing MAP Gaming NetworkConsumer Braille LEA **CarPlay** AACP GATT`

---

## 3. 我们到底卡在哪一步（真机数据反推）

| 版本 | 是否投屏 | 是否有探针 | 车机 browse 次数 |
|---|---|---|---|
| V1.8.35 / 36 / 37 | 是 | **无**（探针 V1.8.38 才加） | 未知（没测） |
| **V1.8.39** | 是 | 有 | **1 次**：`19:33:53.279 from=fe80::10e8:a7ff:fe93:ad59 _carplay-ctrl._tcp.local/12`，**会话开始后 +8.1s** |
| V1.8.40（观察态） | **否** | 有 | **0 次** ← 没有会话，车机没有理由 browse |

**⇒ 上一版"车机从不 browse"是被 V1.8.40 的无效实验误导。**

V1.8.39 那次 browse 之后为什么没 connect？日志给出了原因：

```
19:33:23.401 CTRL_SERVICE_NAME_CONFLICT expected=OPPO_X8U actual=OPPO_X8U (2) reason=NSD_AUTO_RENAME
19:33:32.375 CTRL_REG_BEGIN instance=1 name=OPPO_X8U port=37267
19:33:32.997 CTRL_REG_BEGIN instance=2 name=OPPO_X8U port=37769     ← 同一名字注册了两次
19:33:45.157 CTRL_REG_BEGIN instance=3 name=OPPO_X8U port=37769
19:33:45.141 会话开始 → 19:33:47.718 RECORD_ACKNOWLEDGED
19:33:53.279 车机 PTR query  ← 车机来查了
19:33:57.121 CTRL_SERVICE_NAME_CONFLICT actual=OPPO_X8U (2)          ← 4 秒后被 NSD 改名
```

车机来查时服务名还是 `OPPO_X8U`，但 4 秒后就被 `NsdManager` 自动改名成 `OPPO_X8U (2)`。
**车机一旦在 resolve 阶段拿到 `OPPO_X8U (2)`，`SendConnect` 的双串匹配就必然打不中** → 不发 connect → 触摸永远没通道。

V1.8.40 的 `HasActiveInstance` 复用 + 端口复用已经修掉了改名（19:41:31 `CTRL_REG_LIVE actual=OPPO_X8U`，无冲突），
但**那一轮没有投屏，所以车机没来查**，修复效果从未被验证。

---

## 4. APK 必须发送/保证的信息（按车机实际消费顺序）

| 序 | 项 | 车机侧消费点 | 本 APK 现状 | 要求 |
|---|---|---|---|---|
| 1 | **AirPlay 会话成功**（7000 + SETUP + RECORD） | `_CarPlayControlClientEnsureStarted` 的触发起点 | ✅ 正常（19.9fps） | 必须保持 |
| 2 | **`_carplay-ctrl._tcp` 注册且名字稳定** | `BrowseCallback` 的 PTR/SRV 结果 | ⚠️ 曾被 NSD 改名成 `(2)` | **名字全程不得变**，端口固定 |
| 3 | **实例名 `service_name`** | `0x52ac0` 取出 → `+0x9e0` → `SendConnect` 双串匹配 | `OPPO_X8U`（手工值） | 必须与车机记录一致；建议直接对齐真 iPhone 的 `iPhone_SCH` 做 A/B |
| 4 | **6 字节 MAC** | `0x52b90` 取出 → `SendConnect` | TXT `deviceid`/`btaddr` = 手工值 | 与 3 成对，来源须一致 |
| 5 | TXT 其余键 | 车机 `AddCarPlayClient` 不读 TXT，但 iOS 侧 `_carplay-ctrl` 与 `_airplay` 的 `deviceid` 对齐有用 | 已填 10 键 | 保持 |
| 6 | **iAP2 EA 会话（`com.buick.vip`）** | `addEAPMetaEntries: External Accessory Protocol supports carplay` | ❌ 未实现 | 次优先；若 2~4 修好仍不 connect，再做 |

---

## 5. 固件门控判定树（V1.8.40 后新增，libNmeCarPlay.so 反汇编）

> 目标函数链全部在 `libNmeCarPlay.so`（Cinemo，x86-64，.text 0xcd30-0x9b833）。
> A 级 = 反汇编逐条确认；B 级 = 由相邻证据推断，尚未定位填充点。

### 5.1 HID 发送的硬门控（A 级，本次最关键发现）

```
NmeCarPlay::SendHIDReport  wrapper @0x13ce0
   └─ impl @0x16690
        lock [+0x968]
        pool = this+0x998
        if pool.empty()            → return（不发送）     ← ★ 门 ①
        obj = *pool.begin()
        if obj == null             → return
        call 0xf7d0(obj, type, data, len)

NmeCarPlay::SendHIDReportTo(session) @0x19010
        pool = this+0x998
        cur  = pool.empty() ? null : *pool.begin()
        if (cur != session)        → log + return 0x0d    ← ★ 门 ②
        call 0xf720(session, data, len, this)
```

**结论：车机内部必须先有「HID sink」登记在 `NmeCarPlay+0x998` 池里，且发起方必须是池里的第一个会话，
否则 `SendHIDReport` 静默返回、一个字节都不发。**

`+0x998` 池的全部引用只有 6 处：构造 `0x14fa0`、析构 `0x15270`、清空 `0x15ce0`（构造 @0x151b7、`Reset()` @0x15925）、
查询 `0x165c0`(empty) / `0x165f0`(begin)。**代码里找不到 push_back**（B 级：填充由池对象自身的成员函数完成）。

`Reset()` @0x158b6-0x15931 会把 `[+0x8cc..+0x8e8]` 8 个状态字全部置 1、`[+0xa5c] = -1`、并**清空 `+0x998`**。
⇒ 一次 Reset 之后，HID 池归零；若会话重建没重新登记，触摸必然全程静默。

### 5.2 上层判定顺序（A 级，按调用先后）

| # | 层 | 固件位置 | 判定内容 | 我们状态 |
|---|---|---|---|---|
| 0 | 标定 | `MediaManagerService` `getWirelessCarplayCalibration()` / `Apple_CarPlay_enableWireless` | 无线 CarPlay 是否启用 | ✅（真 iPhone 可连 ⇒ 已开） |
| 1 | iAP2 EA 设备登记 | `MediaManagerService` `MediaWirelessCarPlay` / `Wireless Carplay Device connected` / `CINEMO_METANAME_IAP_EAP_APP_SUPPORTS_CARPLAY` | EA 会话里声明 `com.buick.vip` + supportsCarplay | ❌ **断点** |
| 2 | AirPlay 会话 | 7000 + SETUP + RECORD | 视频/音频流 | ✅ 19.9fps |
| 3 | mDNS 发现 | `_CarPlayControlClientEnsureStarted`@0x52fc0 → `DNSServiceBrowse("_carplay-ctrl._tcp", flags=0x0800000000000000)` | 车机 browse（flags 高 32 位 = `kDNSServiceFlagsIncludeAWDL`） | ⚠️ V1.8.39 仅 1 次 |
| 4 | 设备表登记 | `AddCarPlayClient`@0x178b0 → 填 `NmeCarPlay+0x9e0` | 遍历 `+0x9e0` 比对 **(name, MAC)**；MAC 由 `0x586b0` 从 service 字典取 key **`deviceID`** 再 `0x8e190` 解析成 6 字节 | ⚠️ 未验证 |
| 5 | 通知 Android | `+0x898` 回调 = `fnCarplayServiceUpdate`，**唯一触发点就在 `AddCarPlayClient` 尾部**（0x17ba7/0x17c25） | 参数 `(event 0=Add/1=Remove, arg, macStr, name)` | — |
| 6 | connect | 虚函数槽 `0xd6920`=SendConnect@0x13f30 → `SendConnect`@0x17e10 → `_CarPlayControlClientSendCommand`@0x54860 | 再用 (deviceID, serviceName) 匹配 `+0x9e0`，命中才发 `GET /ctrl-int/1/<cmd>`（cmd 常量 `"connect"` @0xa415c） | ⚠️ |
| 7 | `/command` 握手 | 车机侧 `/command` 路由 @0x412ab；`type` 取值 @0x421cf，`params` @0x421f3 | 车机→手机发 modesChanged 等；`params` 为空即 422（0x422b5 `mov ebp,0x1a6`） | ✅ V1.8.20 已修 |

**注意第 4 层的大小写**：车机读的是 **`deviceID`**（0xb1e4b，大写 ID），
我们 TXT 填的是 **`deviceid`**（`ControlChannel.java:413`，全小写）。
iOS 原生 `_carplay-ctrl` TXT 键集是 `deviceid`（小写，dsc 0x39d84a4），所以车机内部字典应做了映射；
但**这一映射尚未被证实**，属可 A/B 项（见 §6 第 2 条）。

### 5.3 日志侧对账（V1.8.39 / `logs/v1.8.39/recapture.log`）

| 事件 | 计数 | 说明 |
|---|---|---|
| 车机 PTR 查询 `_carplay-ctrl._tcp.local`（`fe80::10e8:a7ff:fe93:ad59`） | **1** | `19:33:53.279`，RECORD_ACK +8.1s |
| `CTRL_TCP_ACCEPT` / `V160_CTRL_TCP_ACCEPT` | **0** | 车机从未回连 ctrl 端口（37769） |
| 车机→手机 `hidSendReport` | **0** | 一次都没有 |
| `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED` | **0** | — |
| 我们→车机 `POST /command` | 24 次（CSeq 6/8/14/18） | **全是单方面探针**：`requestUI`、`modesChanged`、`hidSetInputMode`，车机回 200/422 |
| `HID_SEND_ENTER size=3 generation=2 pool=HID_DEDICATED` | 有 | `19:33:41.435` 我们主动发 `hidSetInputMode` |

⇒ **V1.8.39 的 `/command` 全是我们自说自话，车机没有对我们发起任何一次 CarPlay 控制交互。**
「车机 browse 了 1 次」是它唯一主动动作，之后 resolve/connect 都没发生。

---

## 6. 下一步（按性价比排序，都可 A/B）

1. **（不改代码）补一条判据**：现有历史日志已足够证明「有 browse、无 accept、无 hidSendReport」，
   不必重跑实验；把 V1.8.39 + Buick 时段两份日志的 `CTRL_TCP_ACCEPT` / `hidSendReport` 计数固定为基线即可。
2. **TXT 键名 A/B（改代码，极小）**：`_carplay-ctrl` 的 TXT 同时写 **`deviceid` 与 `deviceID`**（同值），
   并在 `CTRL_SERVICE_TXT` 日志里打印实际键集。用于验证 `0x586b0` 取的 `deviceID` 到底来自 TXT 还是内部映射。
   风险：多一个键不影响 mDNS 合法性，回滚只需删一行。
3. **打开第 1 层门（改代码，主攻）**：iAP2 EA 会话声明 `com.buick.vip` + `supportsCarplay`。
   现有 P1-1 已能跑到 `IAP2_IDENTIFIED` / `MFI_KEY_POSSESSION_VERIFIED`，但车机 `PEER_MESSAGE_IDS SEND count=0`、
   `PEER_EA_PROTOCOLS` 为空 ⇒ 车机完全不认这条 iAP2 链路。
   首个可疑点：`19982 (0x4E0E) BluetoothIdentifier` 发出后 1.35s 车机即 `IAP2_PEER_RESTARTED last_tx=4E0E`
   —— 建议 A/B 关闭 19982 主动发送，观察车机是否继续引导并发消息。
4. **（可选）`AirPlaySession` SETUP 补 `hidDevices`**：若第 1 层通了仍无 HID，再验证 SETUP 是否需带
   `hidDevices`/`hidDescriptor`/`hidVendorID`（固件键池 0xb1100-0xb1720 内有这些键），使车机建立 HID sink 填入 `+0x998`。

---

## 7. V1.8.41 实施记录（2026-11-09，已出包）

按用户要求补齐两项，`dist/Carplay-V1.8.41-debug.apk`（1.8.41 / 378，verify PASS）。

### 7.1 TXT 补 `deviceID`（大写 ID）

`ControlChannel.FillTxt()` 在原有 10 键基础上新增 **`deviceID`**，与 `deviceid` 同值同源（同一变量 `str2`，不会漂移）：

```
id / deviceid / deviceID / btaddr / model / manufacturer / srcvers / features / osvers / protovers / flags  = 11 键
```

日志改为 `V170_TXT_FILLED n=11 ...`。依据：固件 `0x586b0` 取 MAC 的 key 是 `deviceID`（0xb1e4b），
`0x52c00` 另读 `srcvers`（0xa0b0b）。iOS 原生只广播小写 `deviceid`，车机内部应有映射，但未证实 ⇒ 两键并存覆盖两条取值路径。

### 7.2 iAP2 EA 设备登记（四点改动，均在 `MfiCertificateVerifier`）

| # | 改动 | 位置 | 说明 |
|---|---|---|---|
| 1 | 能力表补 `0xEA02/0xEA03`（59906/59907） | `bEaPayload()` `toSendRaw`/`toReceiveRaw` | 原来只声明起停 59904/59905，没有数据通道 |
| 2 | Identification 增 **param 9** = EA 协议清单 | `bEaPayload()` | NUL 分隔：`com.buick.vip` / `com.buick.noprotocol` / `com.gm.noprotocol` / `com.gm.vip`；车机 0x1D01 的 param 9 实测为空，补声明后对照其是否回填 |
| 3 | **主动发 0xEA00** 请求 EA 会话 | `eaRequestSession()`，在 `IAP2_IDENTIFIED` 后调用 | param 0 = 协议名；拒绝则按 `EA_PROTOCOLS` 顺序换下一个，最多 4 次 |
| 4 | 入站 `0xEA00/01/02/03` 专路处理 | `a0()` 顶部 → `eaOnMessage()` | 0xEA00 双向同 ID：param 0 长度 2 = status 应答；其余 = 车机反向请求（自动回 status=0 接受） |

新增开关（`carplayapk-debug` prefs，均**默认 true**）：

| 键 | 默认 | 作用 |
|---|---|---|
| `iap2_ea_session` | true | 是否主动发起 EA 会话（关掉即退回 V1.8.40 行为） |
| `iap2_ea_proto_list` | true | 是否在 Identification 里声明 param 9 |

新增日志锚点：`IAP2_EA_SESSION_REQ` / `IAP2_EA_SESSION_RESP` / `IAP2_EA_SESSION_ESTABLISHED` /
`IAP2_EA_SESSION_EXHAUSTED` / `IAP2_EA_PEER_REQUEST` / `IAP2_EA_DATA` / `IAP2_EA_RX`。

### 7.3 构建注意

`exportApk` 增量构建出现过一次 APK 从 1.35MB 膨胀到 2.15MB（zip 内容不变，多出约 800KB 签名块填充）。
`gradlew clean` 后重建恢复正常（1,352,943 字节）。**出包后必须核对文件大小**，异常时先 clean。

### 7.4 验证判据（真机）

1. `V170_TXT_FILLED n=11` 出现，`CTRL_SERVICE_TXT` 里能看到 `deviceID`。
2. `IAP2_IDENTIFICATION_EA ... ea_proto_list=NNB/4proto` 出现且 `bytes` 比上版大。
3. `IAP2_EA_SESSION_REQ idx=0 proto=com.buick.vip` → 期待 `IAP2_EA_SESSION_RESP status=0`。
4. 车机 0x1D01 若回填 param 9，`IAP2_PEER_EA_PROTOCOLS` 不再是空。
5. 终极判据仍是：`CTRL_TCP_ACCEPT` 由 0 变正 + 出现 `hidSendReport`。

---

## 8. 证据索引

- sysdiagnose（`.workbuddy/tmp_sysdiag_carplay.txt`，2,785,693 行）：
  `2539741`（IsCarPlay:Y + carPlayUUID）、`2540457`（IsCarPlaySSID）、`2542685`（Registering ctrl port 51841）、
  `2553716`（Registered `iPhone_SCH._carplay-ctrl._tcp.local.`）、`2553723`（APEndpointCarPlay name/id）、
  `2553724`（CarPlayControlSupported:Y）、`2554118`（connect 7000）、`2555360`（connect 5001）、
  `2526216`（EA 协议 `com.buick.vip`/`com.buick.noprotocol`/`com.gm.noprotocol` + SocketInterface + PrimaryUUID）、
  `2517585`（WirelessCarplayService）、`2589785`（第二次 IsCarPlay:Y）。
- 固件：`libNmeCarPlay.strings.txt`（`iap://wifi://`）、`MediaManagerService.carplay.txt`（标定键 `Apple_CarPlay_enableWireless`、EA meta、无线会话生命周期）、
  `GMCarPlay.odex.carplay.txt`（`CarPlay is not enabled.` / `isCarPlayWirelessEnabled=` / `EnableCarplayProjection`）。
- 真机日志：`logs/v1.8.39/recapture.log`（会话 + 唯一一次车机 browse + NSD 改名）、
  `logs/v1.8.40/observe.log.gz`（观察态，无会话 → 无 browse，**实验无效**）。
