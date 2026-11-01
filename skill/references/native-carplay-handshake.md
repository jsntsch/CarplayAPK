# iPhone 原生无线 CarPlay 连接全流程（sysdiagnose 实测复盘）

> 数据来源：`sysdiagnose_2026.10.25_16-01-26+0800_iPhone-OS_iPhone_21H216`（iOS 17.7.1 / 21H216 / iPhone13,2）
> 车机：别克君威 `myBuick`，BT `10:E8:A7:13:AD:5B`，主机名 `Android.local`，热点 `myBuick GS`（5G ch149 / WPA2-PSK）
> 场景：在 **设置 → 蓝牙 → myBuick** 手动发起「首次连接」，成功后车机侧断开，iPhone 自动重连一次。
>
> ⚠️ 时区说明：logarchive 里 tracev3 的挂钟时间在本机存在**时钟压缩**（1 秒归档时间 ≈ 30 秒真实时间），
> 本文所有时间为 **从日志正文内嵌时间戳（`with date …`、`addedAt=`、`… ( 十月 25, 15:58:21.221 )`）校准出的真实墙钟**。
> 仅用于理解顺序与量级，不要拿归档裸时间戳对齐。

---

## 0. 一页纸总览

```
用户动作（设置里点连接）
  └─[BT]  ACL 建链 → SDP 全查询 → SSP 配对
        └─[iAP2] Identify → 车机弹授权 → iPhone 请求 WiFi 凭据(0x5702) → 车机回 0x5703
              └─[WiFi] auto-join(trigger=bluetooth) → 关联 myBuick GS → DHCP
                    └─[Ctrl] iPhone 广播 _carplay-ctrl → 车机下发 connect 命令
                          └─[AirPlay] mDNS SRV/A → TCP 7000 PAIR_SETUP → 认证/授权 → 激活
                                └─[流] TCP 6030 屏幕 / TCP 5001 事件 / UDP 5020 保活 / AAC 音频
                                      └─[断开] 车机侧 Receiver disconnected → 自动重连（第二次 attach）
```

真实耗时（首连）：BT ACL→配对 ≈ 6 s，iAP2→凭据 ≈ 7 s，WiFi 关联 ≈ 1 s，AirPlay 建链 ≈ 0.12 s，全程 ≈ 18 s。

---

## 1. 阶段 A：蓝牙 ACL + SDP + 配对（15:58:01.9 → 15:58:16）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| 15:58:01.851 | wifid | `AUTO-JOIN: Auto-join STARTED … params=trigger=bluetooth`（凭据还没到手就先被 BT 触发了一次） |
| 15:58:03 | carkitd | 第一个 `CarConnectionEvent`（连接尝试开始）／`Creating new SessionUUID` |
| — | bluetoothd | `Device found: BDA 10:E8:A7:13:AD:5B, Nm 'myBuick'`，`Make: Buick`（从 OUI 反查厂商） |
| — | bluetoothd | 能力探测：`HandsfreeService / A2DPService / **WiAPService** / **WirelessCarplayService** supports device` |
| — | bluetoothd | `Session "com.apple.Preferences-…" is asking to connect device "myBuick"` ← **点在设置里发起** |
| — | bluetoothd | `Connecting 0xffffd7bf services to device …, **paired 0**`（未配对，走完整流程） |
| — | bluetoothd | `Running SDP` → `OI_L2CAP_Connect PSM=0x0001` → `Allocating ACL connection` → `ACL connected result:0` |
| — | bluetoothd | `Executing full SDP` → `Service search response with **9 records**` |
| — | bluetoothd | 记录：`AudioSink pv103`、`AV_RemoteControl pv106`、`Message_Notification_Server`；`Setting **WIAP version to 0**`、`A2DP Sink version 259` |
| — | bluetoothd | `Secure-Simple-Pairing has started` → `Notifying 'pairing started' for **self-initiated pairing**, Auth type 2` |

**要点**

- 车机没配对过时，iPhone 会**自己发起 SSP**，不等车机。Auth type 2 = Numeric Comparison（需要车机屏确认）。
- `WiAPService` = Wireless iAP（iAP2 over Bluetooth）服务类；`WirelessCarplayService` 是 CarPlay 专属 SDP 类。
  两者同时在，才算「支持无线 CarPlay」的车机。
- SDP 是**全量查询**（full SDP，9 条记录），不是只查一个 UUID。这也解释了为什么安卓侧按 UUID 直连容易拿到错通道：
  iPhone 这里是先全量 SDP 再按 PSM 连 L2CAP/RFCOMM。

---

## 2. 阶段 B：iAP2 identify + 授权 + 下发 WiFi 凭据（15:58:16 → 15:58:16.5）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| 15:58:16 | carkitd | `Vehicle <private> is not paired` → `Ignoring WiFi credentials for vehicle`（首连时凭据还不可信） |
| 15:58:16 | carkitd | `Recording CarConnectionEvent: **CARCarKitAttemptingConnectionEvent**` + `CARCarKitTransportEvent` |
| — | carkitd | **`approval alert accepted`** ← 车机屏点了「允许/配对」 |
| — | carkitd | `sending device transport identifiers **BT: … USB: …**` |
| — | carkitd | `Updated a keychain item` → `posting pairings changed` → `Saved vehicle` |
| — | carkitd | **`requesting WiFi credentials`** |
| — | carkitd | **`received WiFi credentials for connectionID 49886636`** ← iAP2 `0x5702` 请求 / `0x5703` 应答 |
| — | carkitd | `adding network <private>` → `saved WiFi credentials` |
| — | carkitd | `connected BT Classic addresses` / `BT connected vehicles` |
| — | carkitd | `Sending iAP in-car wifi notification` → `setting WiFiManager to in-car` |
| — | carkitd | **`starting CarPlay Control advertising for Wi-Fi UUID`** |
| — | carkitd | `attempting connection on transport **Wireless**` |
| 15:58:17 / 15:58:19 | carkitd | 追加 `CARCarKitTransportEvent` |
| 15:58:22 | carkitd | `CARCarKitVehicleInformationEvent`（车型信息入库） |

**要点**

- 凭据方向是 **iPhone 主动请求（0x5702）→ 车机应答（0x5703）**，与我们在 `SKILL.md` 步骤 4 的结论完全一致；
  首连时凭据第一次到达还会被 `Ignoring WiFi credentials` 丢弃一次（因为此时 vehicle 尚未配对待确认），**必须允许重发**。
- 凭据内容（后续 `received network credentials` 复述）：`channel: 149, securityType: 2`。
  `securityType 2 = WPA2-PSK`，直接映射到 Android 的 `WPA2-PSK`，不需要猜 WPA3。
- 顺序铁律：**先存凭据 → 再开 `_carplay-ctrl` 广播 → 再发 `attempting connection on transport Wireless`**。
  `_carplay-ctrl` 是给车机「反向下发命令」用的回连通道，早了没有 UUID、晚了车机找不到人。

---

## 3. 阶段 C：WiFi 关联（15:58:16.4 → 15:58:17）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| 15:58:11.114 | wifid | `Auto-join started … trigger=app_state`（普通前台自动加入） |
| 15:58:13.533 | wifid | `Auto-join triggered … trigger=registration` |
| 15:58:16.468 | wifid | `Auto-join triggered … **trigger=known_network_added**` ← 凭据落库 |
| — | wifid | `AUTO-JOIN: Updated retry schedule (**fg --> carplay**)` ← 调度优先级直接切成 carplay |
| — | wifid | `Cancelled pending auto-join request` ×3 → `Scheduling auto-join retry attempt in **0 ms** (schedule=carplay)` |
| — | wifid | `AUTO-JOIN: -- ssid=… security=wpa2-personal, auths={ psk }` |
| — | wifid | 关联成功，扫到 `SSID='myBuick GS' BSSID='12:e8:a7:83:ad:59'`，`channel 149 / 5 GHz` |
| — | udhcpc | 手机拿到 `192.168.5.26/24`，网关（车机）`192.168.5.1` |

**要点**

- **`fg --> carplay` 调度切换**是原生实现的关键动作：CarPlay 凭据落库后，WiFi 自动加入队列被**提升为 carplay 专用调度、0 ms 立即重试**。
  纯 `WifiNetworkSuggestion` 在 Android 上做不到这种抢占，这正是我们踩过的「关联慢/被其它热点抢走」的根因。
- 关联判据不是 SSID，而是「SSID + 是否 CarPlay 网络 + 是否拿到 Local IP」，见下阶段。

---

## 4. 阶段 D：CarPlay Control + Bonjour（15:58:16.5 → 15:58:21.2）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| — | audiomxd | `APBrowser: Setting Presence browse mode` → `BTLE discoverer activation complete. Started AirPlayTarget scanning`（BLE 兜底发现） |
| — | audiomxd | `APBrowserCarSessionHelper: StartBonjourForWiFi: <wifiUUID>` |
| — | audiomxd | `WiFi Network changed to: **myBuick GS, IsCarPlay: Y**, CarPlay UUID: F72E8BA5-…` |
| — | audiomxd | `WiFi eligible: **N (No Local IP)**` → 随后 `WiFi eligible: **Y**` |
| — | audiomxd | `APBrowserCarBonjour: [WiFi] Starting Bonjour browsing` + `CarPlayControlServer: Server start` |
| — | carkitd | `wantsCarPlayControlAdvertisingForWiFiUUID: yes` ← **carkitd 授权**广播，audiomxd 才去注册 |
| — | audiomxd | `Registering Bonjour **_carplay-ctrl._tcp port 51841**` → `Registered Bonjour **iPhone_SCH._carplay-ctrl._tcp.local.**` |
| — | audiomxd | `Added new WiFi endpoint 0xB4A5 (**CarPlayControlSupported: Y**)` |
| — | audiomxd | `Bonjour device added/updated` |
| 15:58:21.221 | audiomxd | **`CarPlayControlConnectCommandReceived : 814607901.221012 ( 十月 25, 15:58:21.221 )`** ← 车机下发 `connect` |
| — | audiomxd | `HandleControlServerEvent command 'connect' received for deviceID: 10:E8:A7:13:AD:5B` |
| — | audiomxd | `Setting pending autoconnect deviceID … index = WiFi` |
| — | audiomxd | **`Created APEndpointCarPlay with name 'CarPlay' and id '10:E8:A7:13:AD:5B'`** |

**要点（与我们之前实测完全对得上）**

- 车机 `_airplay._tcp` 服务名 = **`CarPlay`**，端口 **7000**；`txt/deviceID` 用 BT 地址。
- **`_carplay-ctrl` 的端口是动态的，不要写死。** 实测首会话注册在 **51841**、重连会话注册在 **51846**，
  每会话递增/变化；服务实例名是 `iPhone_<设备名>`（本机为 `iPhone_SCH`），不是 `CarPlay`。
  车机必须是**从 mDNS SRV 读端口**来建反向连接，任何一端写死 45697/固定值都会在第二会话失联。
- **广播顺序有前置条件**：先 `carkitd wantsCarPlayControlAdvertisingForWiFiUUID: yes`，
  audiomxd 才 `Registering Bonjour`；会话一结束就 `Deregistered Bonjour _carplay-ctrl._tcp`。
- **触发建链的是车机**：车机通过 iPhone 广播的 CarPlay Control 通道反向下发 `connect`，iPhone 才去连 7000。
  也就是说 iPhone 是「被叫」，不是「主叫」。我们的实现若只做主动连 7000，会缺少这一环（能连通但车机可能不认）。
- WiFi 可用性有 4 个判据（缺一即 `N`）：`No Network SSID` / `UUID Mismatch` / `Session Start Timeout` / `No Remote IP` / `No Local IP`。
  Android 侧对应要做：SSID 匹配、拿到 IPv4、能 ping 到网关、在超时窗口内完成。

---

## 5. 阶段 E：AirPlay 连接 + 认证 + 激活（15:58:21.2 → 15:58:21.9）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| — | audiomxd | `DNS = 'CarPlay._airplay._tcp.local.%14'` |
| — | audiomxd | `SRV resolved CarPlay._airplay._tcp.local. -> **Android.local.** port 7000, If 14, TTL 120` |
| — | audiomxd | `Resolved DNS Android.local. -> fe80::14bf:2ff:fe8b:3686%en0` / `-> **192.168.5.1**` |
| — | audiomxd | `Connect over **Infra** with "CarPlay" ( preferredConnection = Infra, isInfraAvailable = 1, isAWDLAvailable = 0, isNANAvailable = 0 )` |
| — | audiomxd | 建 `CarPlay-ctrl` / `CarPlay-rctl` 两条 HTTP 连接 |
| — | audiomxd | `Connected to 192.168.5.1:7000 (Reach=0.00 ms, **SRV=1.05 ms, DNS=3.15 ms, Connect=2.72 ms, Total=117.99 ms**)` |
| — | audiomxd | `HTTPClientCore Request written: Header **225 B**, Body **37 B**` → `Response: Header **116 B**, Body **159 B**, Status **200**`（PAIR_SETUP/VERIFY） |
| — | audiomxd | `coreUtilsPairing_getPairedPeerFromVerificationPairingSession … error -6727 kNotFoundErr` → 首次配对，正常 |
| — | audiomxd | `Created APKeyHolderCoreUtils` |
| — | audiomxd | `Endpoint connected state changed: isConnected = true` |
| — | audiomxd | `APSNetworkClockNTP: Started APSNetworkClock NTP`（时钟同步） |
| — | audiomxd | `APTransportConnectionUDP 'CarPlay-kplv' … Remote port: **5020**`（低功耗保活） |
| — | audiomxd | `APTransportConnectionHTTP 'CarPlay-evnt'` → `192.168.5.1:**5001**` |
| — | audiomxd | `ScreenPixelSize: **1280 x 768**`，`usage mode CarPlay`，`AirPlay HDR supported` |
| — | audiomxd | `Endpoint_**AuthenticationSucceeded**` → `Endpoint_**AuthorizationSucceeded**` → `Endpoint_**EndpointActivated**`（ActivationSeed 1, Features 3） |
| — | audiomxd | **`Disable bluetooth for endpoint 'CarPlay' with device address '54:09:10:68:51:0b'`** ← WiFi 接管后 iPhone 主动压制 BT |
| — | audiomxd | `HALCarAudioDevice: AudioMain → **AAC 48000 Hz Stereo 1024 samples/packet**；AudioAlt → ELD 48000` |
| — | audiomxd | `TCPUnbuffered connected to 192.168.5.1:**6030** in 13.714 ms` → `screen stream resumed` |

**端口总表（车机侧）**

| 端口 | 协议 | 用途 | 备注 |
|---|---|---|---|
| 7000 | TCP | AirPlay RTSP 控制（`CarPlay-ctrl` / `CarPlay-rctl`） | 唯一的建链入口，PAIR_SETUP 在这里 |
| 5001 | TCP | 事件流（`CarPlay-evnt`） | HTTP，双向事件：born / modes / vehicleInfo / HID |
| 5020 | UDP | 低功耗保活（`CarPlay-kplv`） | keepAliveType 4 LowPowerOnIdle |
| 6030 | TCP | 屏幕码流（`CarPlay-00000001`） | 非缓冲裸 TCP，`Set buffering mode to 0` |

**mDNS 关键坑（我们踩过）**

- 服务实例名 `CarPlay._airplay._tcp.local.` 的 **SRV target 是 `Android.local.`，不是 IP**。
  必须做 **SRV → 主机名 → A/AAAA 二次解析**，直接拿服务名当主机名会解析失败。
- AAAA 会返回链路本地地址 `fe80::…%en0`；若先试 IPv6 再回落 IPv4，会白白丢 100~200 ms。
  原生实现是**先 SRV，再同时解 A/AAAA，取先连上的**，日志里 Total 仅 118 ms。

---

## 6. 阶段 F：首次会话断开（15:58:28 → 15:58:32）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| 15:58:28.425 | audiomxd | 命令缓存里最后一条 `iAPSendMessage, Body 97, RTT: **5.031 ms**` |
| 15:58:32.430 | audiomxd | 最后一条收到 `iAPSendMessage, Body 247` |
| — | audiomxd | **`APTransportConnectionTCPUnbuffered: Receiver disconnected`**（6030 被车机侧关掉） |
| — | audiomxd | `Endpoint_**Failed** { FailureType: **StartupFailed**, Status: **-16613**, ActivationSeed: 1 }` |
| — | audiomxd | `stream 0xA862 disconnected with reason **-72320 / 0xFFFEE580**`、`Session started = true` |
| — | audiomxd | `CarPlay Session Breakdown: **Connection Time 3.484 s, Total Session Time 00:00:14**` |
| — | carkitd | `disconnected session` → `CarPlay session ended; disabling Driving` → `lastSession` 13 事件 |
| — | carkitd | 重连耗时分解：**Bluetooth 5.815 s / AirPlay 0.663 s / CarKit 5.547 s / Wi-Fi 0.598 s / Total 17.934 s** |
| — | audiomxd | `Endpoint_Dissociated` → `CarPlayControlServer: Server stop` |
| — | audiomxd | `Network signature changed: IPv4.Router=192.168.5.1; RouterHardwareAddress=12:e8:a7:83:ad:59` |

**要点**

- 断开是**车机侧主动**：6030 屏幕流被对端关闭 → iPhone 报 `StartupFailed -16613`。
- iPhone 的「连接成功」判定不止 7000 的 200，还要看 6030 是否稳住。这一点我们自己实现时最容易漏。

### 6.1 断流根因：屏流其实跑通了，是车机关掉了接收端

对 6030 建流 → 断流这一段做完整取证后，结论比「协商不认可」更具体：**码流是正常工作的**。

```
[0xFA66] connected to 192.168.5.1:6030 ... in 13.714 ms
<<<< FigVirtualDisplaySession >>>> sessionResume
<<<< FigVirtualDisplayProcessor >>>> fvdp_resume → fvdp_startTimers
backboard  Display 2 hot plug 1
backboard  preferred_mode_with_criteria: resolution [0 x 0], target hdr (Unspecified),
           user hdr (Unspecified), rate (0) --> 1280x768@30 RGB_8bit
backboard  Display 2 current mode (1280 x 768) [0 0]
audiomxd   fvdp_ensureEncoderAndPools: codec 'ftvc', size 1280 x 768
audiomxd   fvdp_submitPixelBuffer: first source frame received
audiomxd   fvdp_updateGeometryForFrame: source rect, dest rect and image size changes are out of sync!  ← 告警
audiomxd   fvdp_encoderControlCreateFrameProperties: adding ForceKeyFrame
audiomxd   fvdp_statsReport: FrameSubmits/sec 25, FrameEncodes/sec 27,
           EncoderDrops/sec 0, SinkOverflowDrops/sec 0, BytesSent/sec 887090
...（稳定编码约 14 s，25fps / ≈0.85 MB/s）
audiomxd   Receiver disconnected          ← 车机关闭 6030 接收端
audiomxd   Endpoint_Failed { StartupFailed, -16613 } / { StartupFailed, -72320 }
```

关键判读：

| 证据 | 含义 |
|---|---|
| `FrameEncodes/sec 27`、`EncoderDrops/sec 0`、`SinkOverflowDrops/sec 0` | 编码侧**零丢帧、零溢出**，手机没有跟不上 |
| `BytesSent/sec 887090`（第二会话 1047027） | 码流**持续在发**，网络没断 |
| 稳定跑了 **14 s**（第二会话 33 s）才断 | 不是握手即挂，是**会话中途**被掐 |
| **两次会话（首连 + 自动重连）死法完全一样** | 与「首次配对」无关，是可复现的对端行为 |

所以 -16613 不是「连不上」，而是「**对端主动结束了已建立的接收端**」。往两个方向查：

1. **车机侧（最可能）**：这颗「车机」主机名是 `Android.local`、热点 `myBuick GS`，
   即**安卓模拟主机**。它自己的 CarPlay 接收实现主动 `close()` 了 6030，
   常见原因是 media pipeline 起不来、H.264 解码器初始化失败、或上层 UI 被抢焦点。
2. **几何不同步告警**：`fvdp_updateGeometryForFrame: … out of sync!` **两次会话都出现**，
   且都紧跟在首帧之后。原生侧 `preferred_mode_with_criteria` 拿到的请求是
   `resolution [0 x 0] / rate (0)`，属于**车机没上报 preferred mode**，回落到 `1280x768@30 RGB_8bit`。
   若对端按另一个分辨率/帧率建解码器，首帧几何一改就会触发重建，重建失败即关流。

**对我们的直接指导**：不要再去优化「怎么连上 7000/6030」——那部分原生和我们都通了。
该查的是**我们作为接收端为什么把 6030 关掉**，以及**建解码器时用的宽高/帧率/色彩格式是否与
`fvdp_displaySetMode` 下发的 1280x768@30 RGB_8bit 一致**。

---

## 7. 阶段 G：自动重连（15:58:50 → 15:59:07）

| 真实时间 | 日志主体 | 事件 |
|---|---|---|
| 15:58:50 | carkitd | **`CARCarKitAttemptingConnectionEvent`** + `CARCarKitTransportEvent`（第二次 attach，全自动，无用户动作） |
| 15:58:51 / 15:58:54 | carkitd | `Accessory does not support CarPlay`（瞬时态，忽略）→ `Handling CarPlay attach` → `received network credentials … channel 149, securityType 2` |
| — | carkitd | `network already exists, checking if updates are required` → **`Ignoring wireless reconnect start; already started`**（去重保护） |
| — | carkitd | `supportedAirPlayFeatures: uiContext, viewAreas, cornerMasks, focusTransfer, h.264Level5.1, mainBuffered, altScreen, enhanced…` |
| — | carkitd | `supportedCarPlayFeatures: **7**`；`vehicle doesn't require assets` |
| — | audiomxd | 新 endpoint `0xC36A`：`Posting Notification Endpoint_AuthenticationSucceeded` → `AuthorizationSucceeded` |
| — | audiomxd | `Modes changed: screen **iOS** (permanent: iOS), mainAudio **Car** (permanent: Car)` |
| — | audiomxd | `remote screen port: **6030**`；`keepAliveType → 2 Standard` |
| 15:59:04.291 | audiomxd | `modesChanged, Body 232` |
| 15:59:07.797 | audiomxd | `hidSendReport, Body 96` ← 触摸/旋钮事件通道已工作 |

**要点**

- 重连是**同一条链路重走，但跳过了 BT 配对和 WiFi 关联**：凭据已在，WiFi 已在，直接 7000 + 认证。
  所以「自动重连」应该实现成：**只重跑 AirPlay 段**，不要重跑蓝牙。
- `Ignoring wireless reconnect start; already started` 说明原生实现有幂等保护，我们并发重连时要照做，否则会出现双会话互踢。
- 第二次 attach 的 `screen iOS / mainAudio Car` 与第一次 `screen Car / mainAudio Car` 不同——
  车机在会话里夺回/让出主屏取决于当时车机 UI 状态，属正常。

---

## 8. 时间轴（真实墙钟，单行版）

```
15:58:01.9  wifid    AUTO-JOIN started (trigger=bluetooth)
15:58:03    carkitd  CarConnectionEvent #1, new SessionUUID
15:58:03~16 bluetoothd ACL → full SDP(9 records) → SSP self-initiated (Auth type 2)
15:58:16    carkitd  未配对 → 丢弃一次凭据 → CARCarKitAttemptingConnectionEvent
15:58:16.x  carkitd  approval alert accepted → device transport identifiers → 请求凭据
15:58:16.x  carkitd  收到 0x5703 凭据 (ch149/securityType2) → adding network → 存库
15:58:16.5  carkitd  _carplay-ctrl 开始广播 → attempting connection on transport Wireless
15:58:16.5  wifid    fg → carplay 调度，0ms 重试，关联 myBuick GS
15:58:17    wifid    DHCP: 192.168.5.26/24, gw 192.168.5.1
15:58:17~21 audiomxd Bonjour browsing + CarPlayControlServer start + APEndpointCarPlay 建立
15:58:21.2  audiomxd 车机下发 connect 命令 ← 关键触发点
15:58:21.3  audiomxd SRV→Android.local→192.168.5.1，TCP 7000 建链 118ms
15:58:21.4  audiomxd PAIR_SETUP 200 → APKeyHolder → isConnected=true
15:58:21.5  audiomxd UDP 5020 保活 / TCP 5001 事件 / 屏幕 1280x768
15:58:21.8  audiomxd Authentication → Authorization → Activated (seed 1)
15:58:21.9  audiomxd 压制 BT → AAC 音频 → TCP 6030 屏幕流
15:58:28.4  audiomxd iAP 消息往返 RTT 5ms（会话已活跃）
15:58:32    audiomxd 车机关 6030 → Endpoint_Failed StartupFailed -16613 → 会话结束(存活 14s)
15:58:50    carkitd  CARCarKitAttemptingConnectionEvent #2（自动重连，跳过 BT/WiFi）
15:58:54    carkitd  凭据已在 → 直接 attach
15:59:04    audiomxd modesChanged 生效
15:59:07    audiomxd hidSendReport（触摸通道通）
```

---

## 9. 对我们（Android 模拟主机）的 8 条硬结论

1. **触发方向反了**：原生是「车机通过 `_carplay-ctrl` 下发 `connect`」后再连 7000。
   我们的实现是主动撞 7000，能连通但缺少车机认可的握手起点 —— 应补上 ctrl 通道的监听与 `connect` 处理。
2. **`_carplay-ctrl` 必须在凭据落库之后、发起 Wireless 连接之前广播**，且 TXT 要带 Wi-Fi UUID。
3. **凭据首次会被丢弃一次**，`0x5702` 必须允许重发；`securityType=2` 可直接映射 WPA2-PSK。
4. **WiFi 关联要做「carplay 优先级 + 0 ms 重试」的抢占**，否则会被已保存的其它热点抢走（Android 上只能靠
   `WifiNetworkSuggestion` + 主动 `disconnect` 其它网络 + 高频轮询近似实现）。
5. **mDNS 必须做 SRV → hostname → A/AAAA 两跳**，不能把 `CarPlay` 当主机名；先连 IPv4 更快。
6. **「连上」的判据是 6030 稳住**，不是 7000 返回 200。要监控屏流 TCP 的生命周期。
   且要区分两类失败：**建不起流**（连接/参数问题）vs **建起来了被对端掐**（接收端解码问题）。
   本次实测属后者 —— 见 6.1，别把力气花错地方。
7. **自动重连只重跑 AirPlay 段**，并加 `already started` 幂等保护；不要重跑 BT/WiFi。
8. **WiFi 起来后要压制 BT ACL**（原生直接 disable BT for endpoint），否则车机可能把音频留在 BT 通道。

---

## 10. 复用方法：从任意 iOS sysdiagnose 复现本表

```bash
# 依赖
pip install UnifiedLog lz4 biplist

# 抽取 CarPlay 时间线（自动做 dsc v2 / 时钟校准 / 容错）
python scripts/carplay_sysdiag_timeline.py \
    "/path/to/sysdiagnose_2026.10.25_16-01-26+0800_iPhone-OS_iPhone_21H216" \
    -o carplay_timeline.txt
```

脚本会输出：阶段分组的事件序列、内嵌真实时间戳、端口/分辨率/耗时汇总。
`references/troubleshooting.md` 里有「症状 → 该看哪一段日志」的对照表。

---

## 11. 2026-10-31 复核：同一份 sysdiagnose 的原文补全与勘误

这一段是用**新方法**重扫同一份归档（`sysdiagnose_2026.10.25_16-01-26+0800`）的结果，用来补第 1~10 节
里靠推断的部分。第 1~10 节的时序仍然有效，但涉及「TXT 内容」「NTP」「触摸协商」的表述以下面为准。

### 11.1 为什么要换方法

`UnifiedLog 0.3` 对这份归档解析结果是 **0 行**：`0x600d` 数据块里的条目改成了 `0x6004` 类型
（proc 名与消息**内联**，不再走 catalog 引用），旧库遇到未知 tag 后对齐错乱，整块丢弃；异常被
`Parse()` 内部吞掉，返回值仍是 True，所以看不出失败。

可用的做法：按 `bv41` 头（4B magic + u32 未压缩长度 + u32 压缩长度，压缩体从偏移 12 起）解压每个
`0x600d` 块，再扫可打印字符串。5548 个块 → 329 万条字符串，其中 CarPlay 相关 9670 条。
脚本：`scripts/sysdiag_strings_probe.py`。

### 11.2 补全的原文事实

| 项 | 原文 | 说明 |
|---|---|---|
| ctrl 端口 | `Registering Bonjour _carplay-ctrl._tcp port 51841` / `port 51846` | 两会话各一个，动态 |
| ctrl 实例名 | `Registered Bonjour iPhone_SCH._carplay-ctrl._tcp.local.` | `iPhone_` + 设备名 |
| 车机 connect #1 | `CarPlayControlConnectCommandReceived : 814607901.221012` = 15:58:21.221 | 注册后 **4.7 s** |
| 车机 connect #2 | `... : 814607933.646298` = 15:58:53.646 | 断开后 +18 s，重连会话 |
| connect 携带 | `HandleControlServerEvent command 'connect' received for deviceID: 10:E8:A7:13:AD:5B` | 带的是**车机自己**的 BT 地址，不是手机身份 |
| 事件通道 | `Connecting to 192.168.5.1:5001, interface 14` / `APTransportStream ... CarPlay-evnt` | TCP 5001 |
| 保活 | `[0x25FB] Setting keepAliveType to 2 Standard` → `to 4 LowPowerOnIdle`、（另一会话 0x4FCF 同）| `CarPlay-kplv` = UDP 5020 |
| 触摸 | `hidSendReport, Body 96` ×7（15:59:07.797 ~ 15:59:22.318） | 车机主动推，无协商 |
| 车机请求行 | `HTTP Request:  POST /command RTSP/1.0` | **RTSP/1.0** |
| iAP2 续传 | `iAPSendMessage, Body 187/192/247/97` ×13 | WiFi 接管后 iAP2 走事件通道 |
| 会话内命令全集 | 只有 `hidSendReport` + `iAPSendMessage` | 没有 `requestUI`、没有 `hidSetInputMode` |

### 11.3 勘误（上一版写过头的地方）

| 上一版表述 | 实际 | 处置 |
|---|---|---|
| 「`APSNetworkClockNTP: Started APSNetworkClock NTP`」 | 全量 329 万条字符串里 `APSNetworkClockNTP`、`NetworkClock`、`timingPort` **均 0 命中** | 该行无法复核，本文已按「未证实」处理；`keepAliveType` 的切换是真实存在的 |
| 「`_carplay-ctrl` 的 TXT 要带 Wi-Fi UUID」 | 日志**没有**打印 TXT 内容（只有 `starting CarPlay Control advertising for Wi-Fi UUID` 这句意图描述） | 降级为推断。TXT 键集要拿车机侧抓包（`dns-sd -L` / tcpdump）才能定 |
| 「触摸走 iAP2 EA 会话 HID」 | 无线 CarPlay 下是 AirPlay 事件通道的 `hidSendReport`，且**不需要** `hidSetInputMode` | 已在 `touch-input-hid.md` 第 16 节改正 |

### 11.4 对第 9 节 8 条结论的修订

- 第 1 条（触发方向）：**加强**。不只是「缺一个握手起点」，而是「抢跑会让车机把会话定性成普通 AirPlay 镜像」，
  实测差距是 4.7 s vs 1.28 s。等待窗口 **≥5 s** 才有意义。
- 第 3 条（凭据重发）：不变。
- 第 8 条（WiFi 起来后压制 BT）：不变，但注意原生还顺带把音频从 BT 切到 AirPlay（`mainAudio Car`）。
- 新增第 9 条：**`_carplay-ctrl` 注册后必须静默等车机 `connect`，不要自拨 7000。**
- 新增第 10 条：**触摸不需要任何协商**，别再把 `hidSetInputMode` 当开关。
