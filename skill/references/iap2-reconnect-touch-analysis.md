# iAP2 重连流程对比（固件 vs APK）与两种连车机 WiFi 场景的触摸无反应根因

> 2026-11-09 深入研究。对象：Buick Info3.6 `libNmeCarPlay.so` + `MediaManagerService`（A 级字节码/字符串）
> ↔ 本项目 `android-project`（**V1.8.33 / 370**，P1-1 诊断包已装 OPPO）。
> 承接 `pairing-reconnect-compare.md` §9–§17 的结论（触摸门 `[+0x466]` 大概率已开、`AddCarPlayClient`/`SendConnect`
> 未触发、`iap_channel=false`、P0-1/2/3 已证手机侧无缺口），本文**聚焦 iAP2 重连这一层**，
> 把「触摸无反应」的真正缺口收敛到两条独立门控上。

## 0. 结论先行（两层角色倒置）

固件与 APK 在 iAP2 连接的**发起方向**上，存在**两层相反的角色倒置**。两层任一未闭合，
车机→手机的触摸（car→phone `hidSendReport`，走 iAP2 HID）就没有通道，触摸必死：

| 层 | 规范要求 / 固件设计 | 本 APK 实际 | 后果 |
|---|---|---|---|
| **① 会话发起** | 车机是发起方：`browse _carplay-ctrl` → `GET /ctrl-int/1/connect`（`SendConnect`@0x17e10 命中 `AddCarPlayClient` 的 `[+0x9e0]` 表才发） | 手机主动 `connect 7000` 绕过该握手（`WirelessCarPlayService.c2→P1`，`CarPlayAutoReconnectService` 兜底主动连） | 车机 `AddCarPlayClient` 不触发 → `+0x9e0` 列表空 → `SendConnect` 返回 0xb → **Accessory RTSP 控制通道（车→手机）从未建立** |
| **② iAP2-over-WiFi 发起** | 手机是 iAP2 host，会话建立后**主动**向车机 `NmeWifiTransportServer`（`GetIAPWifiUrl`=`iap://wifi://`）发起 iAP2-over-Network，置 `iap_channel=true` | APK 的 iAP2-over-WiFi（`AirPlaySession.this.j`=`Iap2WirelessSetup`）**只在收到车机首条 CONTROL_POST iAP2 时被动创建**（`AirPlaySession.T()` @1424–1478），从不主动发起 | `iap_channel=false` → 车机 iAP2 HID 传输未起 → `SendHIDReport()` 无通道 → **触摸字节零到达** |

> 关键判据（§14.1 真机）：`HID_INPUT_MODE_ACCEPTED` 3/3 均 200（手机→车机 `/command` 走的是 Controller 通道，正常），
> 但 `HID_REPORT_ARRIVED=0`/`TOUCH_FORWARDED=0`/`CARPLAY_INBOUND_ANY=0`。即**手机→车机方向通，车机→手机方向全断**——
> 这正好对应「层①的 Accessory 通道 + 层②的 iAP2 HID 通道」两条反向（车→手机）链路都未建立。

## 1. 固件侧 iAP2 重连流程（A 级证据）

### 1.1 两条 iAP2 传输，角色分明
- **BT iAP2（RFCOMM）**：仅用于下发 WiFi 凭据（`0x4301`/`0x5703`）+ 手机 CarPlay 身份（`0x4300` 可用性）。
  会话建立后由 `OnSessionDisableBluetooth()` @0x171ef **关闭蓝牙 iAP2**（规范 §23.2.7：BT iAP2 仅传凭据，会话建立后必须断开）。
- **iAP2-over-WiFi（专用 transport）**：`NmeWifiTransportServer::Create`（`_ZN22NmeWifiTransportServer6CreateEPvP12WifiCallbackRK9NmeString`）
  + `on_iap_message`（`_ZN22NmeWifiTransportServer14on_iap_messageEPKhj`），URL 由 `GetIAPWifiUrl` 给出，字符串即 `iap://wifi://`。
  这是会话期间的**唯一** iAP2 通道，承载 iAP2 HID（触摸报告 `SendHIDReport()`）与反向控制。
  `carplay_wifi_transport_name`（`INmeOptions27carplay_wifi_transport_nameEv`）标定其名称；`Wifi transport already existent` 表明车机跟踪该 transport 是否已建。

### 1.2 车机重连 / 登记状态机（§6.2）
```
上电 → 读蓝牙配对表(isBtPaired/isBTIdDevice/getPairedPhonesDetail)
 → 蓝牙自动回连 + EVENT_PHONE_PAIRED_DEVICE_CHANGED        [B 级]
 → AddCarPlayClient(event=0, BT_MAC, 设备标识) 恢复 [+0x9e0]   [A, @0x178d0]
 → _CarPlayControlClientEnsureStarted @0x52fc0 → browse `_carplay-ctrl._tcp`
 → SendConnect @0x17e10：双串匹配 [+0x9e0] 表，命中才发 `GET /ctrl-int/1/connect`
 → 手机回 200 → SessionStarted → 车机侧 CarPlay 会话登记完成
```
- `AddCarPlayClient` 由 `IosControlClientCallback(AddOrUpdateController)` @0xe272/0xe338 触发（mDNS `_carplay-ctrl` 发现的回调）。
- `iap_channel` 来自 `/info` 的 `CAPS`：仅当 iAP2-over-WiFi（`NmeWifiTransportServer`）真正建链才为 true。
- 车机侧有独立 beacon/keepalive（`_IdleStateKeepAlive*` / `ReconfirmDevice` / `RetryTimerFired`）做重发现，不依赖 App。

### 1.3 触摸报告的真实承载
README §1（A 级）：**「触摸报告走 iAP2 HID，输入模式协商走 HTTP `/command`」**。
- `hidSetInputMode`（手机→车机）：RTSP `POST /command`，`OnSessionSetInputMode` 回调槽 `[obj+0x848]` @0x17332 只转发不解释 → 200 仅代表转发成功，**不代表 HMI 启用触摸**。
- `hidSendReport`（车机→手机）：`SendHIDReport()` @0x4ab62 → `call 0x45100`，统一门 `[+0x466]`（startSession 成功后置 1）。**承载于 iAP2 HID（iAP2-over-WiFi）**，
  不是 RTSP 通道。故 `iap_channel=false` 时，车机即使想发触摸也无传输。

## 2. APK 侧 iAP2 重连流程（A 级源码）

### 2.1 两条 iAP2 传输也存在，但建立方式相反
- **BT iAP2（RFCOMM）**：`WirelessCarPlayService.c2()` @2660 → `new PairingSession` @2674 → `pairingSession.P1()` @2719
  → `k1()`/`I1()` → `MfiCertificateVerifier`（含 `0x4300` 可用性、`0x4301` WiFi 凭据解析 `Iap2Csm.y()`）。
  这条在**两种连车机 WiFi 场景都会跑**（见 §3）。
- **iAP2-over-WiFi（network iAP2）**：`AirPlaySession.this.j`/`r0` = `Iap2WirelessSetup`。
  创建点**唯一**在 `AirPlaySession.T()` @1424–1478：
  ```java
  // T() 是 INBOUND CONTROL_POST 的 iAP2 数据处理器（S0→T）
  if (!this.o.get() && this.r0 == null) {            // r0 全代码未赋值，恒为 null → 该门恒开
      if (this.j == null) {
          this.j = Iap2WirelessSetup.n(str2, ...);    // ← 仅当收到车机首条 iAP2 时才建
          NETWORK_IAP_ROUTE_SELECTED transport=CONTROL_POST trigger=AUTHENTICATED_PEER
      }
      return !this.j.s(bArr) ? J1("IAP_NOT_ACCEPTED") : new byte[0];  // 把车机数据喂进 network iAP2
  }
  ```
  - `Iap2WirelessSetup` 是 iAP2-over-Network 实现（`secured/Iap2WirelessSetup.java`）：`MfiCertificateVerifier.u()` 跑无线 iAP2 握手；
    `a.l()` 回调返回 `ControlChannel.LocalBluetoothIdentifier()`（V1.7.67+ 补的本机蓝牙标识）。
  - `x1()` @3650 返回 `new Iap2PostResponseOwner(this, this.j)` —— 即把 network iAP2 的**出站**字节写回 CONTROL_POST。
    **但 `x1()` 要求 `this.j` 已存在**，而 `this.j` 只在收到车机首条 iAP2 时才有。
  - `close()` @4168–4181 同时关 `r0`/`j`，**WiFi 重连时不会重建**（只在整个会话销毁时关）。

### 2.2 由此推出的死锁（决定性）
- iAP2 协议：手机 = iAP2 host，**应先发**识别/`0x4300` 可用性；车机 `NmeWifiTransportServer` 等待手机发起。
- APK：`this.j` 仅在**车机先发** iAP2-over-Network 时才建；手机自身从不主动发起。
- 结果：车机等手机先发、手机等车机先发 → **两端都不发** → iAP2-over-WiFi 永不建立 → `iap_channel=false` → 触摸零到达。
- 与真机一致：`CAPS iap_channel=false`、`CARPLAY_INBOUND_ANY=0`、`HID_REPORT_ARRIVED=0`、`TOUCH_FORWARDED=0`。

## 3. 两种「连车机 WiFi」场景的触摸无反应根因

两种场景**只是手机进入车机 WiFi 的触发方式不同**，一旦 `CarPlayAutoReconnectService` 起 `WirelessCarPlayService.V0`（→ `c2` → `P1`），
后续 BT iAP2 + AirPlay + network iAP2 隧道**走的是同一条代码路径**，因此**触摸缺口完全相同**，均被 §0 的两层倒置卡住：

| 场景 | 含义 | WiFi 进入方式 | BT iAP2 是否跑 | 触摸结果 | 说明 |
|---|---|---|---|---|---|
| **A：手机连车机 WiFi** | 手机作为 WiFi client 主动连车机 AP（基础设施模式） | App 驱动（`CarPlayWifiConnector`/`WifiNetworkSpecifier` 或手动） | ✅ 跑（`c2→P1`） | ❌ 无触摸 | 车机拿到手机 BT iAP2 身份，但①`AddCarPlayClient` 未触发（绕过 ctrl 握手）、②network iAP2 未主动发起（`iap_channel=false`）。会话建立后车机 `OnSessionDisableBluetooth` 关 BT iAP2，仅剩的 iAP2-over-WiFi 又没建 → 触摸断 |
| **B：自动连接车机蓝牙 WiFi** | 系统自动回连已保存车机 WiFi（可由蓝牙 proximity 触发） | OS 自动加入已存网络 → 后台 `WifiNetworkMonitor`/`CarPlayAutoReconnectService` 检测到后起会话 | ✅ 仍跑（同一 `V0→c2→P1`） | ❌ 无触摸 | 与 A 走同一 `c2→P1` 路径，缺口一致。区别仅在「连接由 OS 而非 App 发起」，不改变 iAP2 层结论 |

> **关键纠偏**：两场景触摸失败**不是场景差异导致**，而是**同一个 APK 缺口**在两种进入方式下都暴露。
> 不存在「某场景能摸、某场景不能摸」的分叉——因为卡点在 iAP2-over-WiFi 建立与 `AddCarPlayClient` 登记，二者都与「谁发起 WiFi 连接」无关。

### 3.1 为什么「视频正常、触摸为零」在此两场景下自洽
- 视频/音频走 **AirPlay RTSP/UDP**（手机→车机单向推流），只需手机主动连 7000 + RECORD 成功，与 iAP2 通道无关 → 正常。
- 触摸是**车机→手机**反向命令，必须经由：①`AddCarPlayClient` 建立的 Accessory RTSP 通道 + ②`iap_channel` 起的 iAP2 HID 传输。两条反向链路在 APK 当前实现里都没建立 → 视频正常、触摸死。

## 4. 与既有结论的衔接（不重复造轮子）
- §14.2 已证 `startSession`/`[+0x466]` 大概率已开（RECORD 200），故「固件反向门没开」**不再是主线**，本文不重复。
- §15.5 已证「会话发起方向 / `AddCarPlayClient` 登记」是第一缺口（层①）。本文补上**层② iAP2-over-WiFi 主动发起**这一被忽视的第二缺口，
  并说明两者**共同**造成「车机→手机反向通道全断」。
- P1-1（V1.8.33）只诊断 BT iAP2 握手是否让 `AddCarPlayClient` 登记成功（层①的 A/B），**未触及层②**；
  即便 P1-1 让 `V160_CTRL_TCP_ACCEPT` 变正，若 `iap_channel` 仍 false，触摸依旧为零——需在 P1-1 之后补 **P1-2（主动发起 iAP2-over-WiFi）**。

## 5. 修复方向（按层，均可开关 / A/B）

| 层 | 优先级 | 动作 | 代码锚点 | 验证判据 |
|---|---|---|---|---|
| ① | P1（继续 P1-1） | 让车机 `AddCarPlayClient` 登记：响应 `GET /ctrl-int/1/connect` 并等 `onCtrlInvitation` 再连 7000（V1.8.32 已落地，真机待验 `V160_CTRL_TCP_ACCEPT`） | `CarPlayCtrlAdvertiser.StartEarly`/`onCtrlInvitation`、`WirelessCarPlayService.c2` | `V160_CTRL_TCP_ACCEPT≥1`、`CTRL_INVITATION_ACCEPTED≥1` |
| ② | **P1-2（新，本文核心）** | **主动发起 iAP2-over-WiFi**：`AirPlaySession` 在 RECORD 成功后**不等车机先发**，主动 `Iap2WirelessSetup.n(...)` 建 `this.j` 并发送手机识别/`0x4300` 可用性到车机 iAP2 端点（CONTROL_POST 隧道或直连 `GetIAPWifiUrl`），使车机 `NmeWifiTransportServer.on_iap_message` 收到、置 `iap_channel=true` | `AirPlaySession.T()` @1424（去掉「仅入站建链」约束）、`x1()` @3650、`Iap2WirelessSetup` | `CARPLAY_IAP_DIAG_J=exists` 在 RECORD 后即刻出现、`CAPS iap_channel=true`（车机 `/info`）、`HID_REPORT_ARRIVED≥1` |
| ② | P2 | `this.j`/`r0` 在 WiFi 重连时重建（当前只在 `close()` 销毁），对齐车机 beacon 周期 | `AirPlaySession.close()` @4168、`WifiNetworkMonitor` | 断 WiFi 重连后触摸恢复，不需重启 App |

**判据顺序（最终）**：`V160_CTRL_TCP_ACCEPT`（层①）→ `iap_channel=true`（层②）→ `CARPLAY_INBOUND_ANY` → `HID_REPORT_ARRIVED` → `TOUCH_FORWARDED`。
前两级任一为 0，任何 HID 侧改动都无效。

## 6. 边界（保持诚实）
- `NmeWifiTransportServer` 究竟是「专用 TCP 端点 `iap://wifi://`」还是「CONTROL_POST iAP2 隧道的接收端」，字节码未完全拆分；
  **但功能结论不变**：车机需要手机先发 iAP2-over-Network 标识才能置 `iap_channel`，而 APK 从不先发。无论传输细节如何，缺口在「发起方向」。
- `+0x9e0` 列表在真机某次会话是否为空、`[+0x468]` 是否 NULL、HMI `SetCallbacks` 槽是否注册，静态字节码无法证明，需整车 pcap / 车机侧日志（受 §15.7 边界约束）。
- 本分析**只做研究与落盘**，未改业务代码、未出包；修复项待用户确认后实施。

## 7. V1.8.35 真机验证结果（2026-11-09 08:44，myBuick GS）—— **修正 §2 的因果判断**

**结论先说**：P1-2「主动发起 iAP2-over-WiFi」**执行成功但无效**；真正的阻断点不在「手机不主动发 iAP2」，
而在**保留 WLAN 重连路径整段跳过 BT iAP2 引导**（`X2()` 的 `this.j != null` 分支），
导致车机从未收到手机的身份/可用性消息，`AddOrCarPlayClient` 永不登记。

### 7.1 证据（logcat `CarPlayAPK` / `CarPlusWireless`，原始日志 `logs/v1.8.35/`）

| 环节 | 实测 | 说明 |
|---|---|---|
| 会话进入方式 | `CURRENT_WIFI_REUSE_BY_SSID peer_name=myBuick`、`verified_previous_transport=true` | 走**保留 WLAN 重连**，不是全新配网 |
| AirPlay | `CARPLAY_PAIR_VERIFIED` → `CARPLAY_INITIAL_SETUP` → `CARPLAY_SETUP_RESP_KEYS n=3 keys=[keepAlivePort, eventPort, timingPort]` → `EVTCP connect 192.168.5.1:5001` | 车机 SETUP 响应**不含任何 iAP2 bootstrap 字段** |
| 车机 /info | 22 keys，`bluetoothIDs=[10:E8:A7:13:AD:5B]`，`features=0x6104040280`，`hidDevices` 含触摸屏 | 车机能力正常，`CAPS ... iap_channel=false`（=features 未含 `iAPChannel`） |
| P1-2 | `P12_PROACTIVE_TRIGGERED j==null` → `P12_NETWORK_IAP_ROUTE_BUILT proactive=true` → `CARPLAY_IAP2_OUTPUT_THREAD_STARTED` | **主动建链确实执行** |
| 手机→车机 iAP2 | 7× `CONTROL_COMMAND_SEND path=/command type=iAPSendMessage`（1×70B + 6×79B 重试，payload 首 2 字节 `FF5A` = iAP2 链路控制） | 全部 HTTP 200 |
| 车机→手机 | `INBOUND_REQ=0`、`CARPLAY_COMMAND_RECEIVED=0`、`HID_REPORT_ARRIVED=0`、`TOUCH_FORWARDED=0` | **零入站**，iAP2 无任何应答 |
| HID | `HID_GATE_OPEN cnt=1 mode=0 devices=0`（过早，SKIP）→ `HID_SWEEP mode=1` → 3× `hidSetInputMode` → 3×200 → `HID_MODE_REPORTED=true` | HID 协商本身正常，之后仅等车机发报告 |
| ctrl | `CTRL_SERVICE_REGISTERED network_scoped=true`、`CTRL_ANNOUNCE name=OPPO_X8U port=34213`，但 `CTRL_INVITATION_ACCEPTED=0` / `V160_CTRL_TCP_ACCEPT=0` | 车机**从未回连**手机 `_carplay-ctrl` |
| **BT iAP2** | `P1_START=0`、`RFCOMM_SELECTED_PEER_*=0`、`IAP2_*=0`；仅 `PAIR_VERIFY_FIRST_OK`（属 AirPlay pair-verify，非 BT iAP2） | **BT iAP2 整段没跑** |

### 7.2 源码根因（A 级）

```java
// WirelessCarPlayService.onStartCommand（保留 WLAN 分支）
this.j = networkFromNetworkHandle;   // 4064/4081：保留 wifi 时非 null
...
W2();                                // 4097 → X2(-1L)

private void X2(long j2) {
    if (this.j != null) {            // ← 保留 WLAN 重连
        Y2(j2);                      // e7 → e2()：只起 AirPlay 保留会话
        return;                      // ← 直接 return，永远不会走 BT iAP2
    }
    ...
    h7 → c2(jB, jC, j2) → new PairingSession(...) → P1(...)   // 仅 this.j==null 才到这里
}
```

- `this.j` 是 `private Network j`（保留的 WifiNetworkHandle），**不是** AirPlay 会话对象。
- 因此「手机连车机 WiFi / 系统自动回连」两种场景只要命中 `CURRENT_WIFI_REUSE`，BT iAP2 就永远不会启动。
- 这与 §2.2 的「死锁」判断并不冲突，但**顺序要修正**：不是「车机等手机发 iAP2」，
  而是「手机连 BT iAP2 身份都没送出去」，车机侧 `AddCarPlayClient` 根本没被触发，
  `iap_channel` 自然为 false，HID 报告自然为零。
- 也解释了为什么 `V160_CTRL_TCP_ACCEPT` 在 V1.8.29~V1.8.35 始终为 0、P1-1 探针全程沉默。

### 7.3 修复方向（V1.8.36，可开关）

1. `X2()` 保留分支：`Y2(j2)` 之后**补一次 BT iAP2 引导**（新分配 `J.b()/J.c()` → `h7` → `c2()`），
   开关 `p1_1_bt_iap2_retained`（默认 **false**，先 A/B）。
2. 埋点：`X2_ENTER j_present=`、`X2_PATH=retained|bootstrap`、`C2_ENTER`、`C2_GATE g1= h1=`、`P1_CALLED`，
   让下一轮抓包能直接看出是哪道门挡住。
3. 判据：`P1_START` → `RFCOMM_SELECTED_PEER_CONNECTED` → `IAP2_AUTHENTICATED_WAITING_CARPLAY_START`
   → `V160_CTRL_TCP_ACCEPT≥1` → `INBOUND_REQ>0` → `HID_REPORT_ARRIVED≥1`。

> 边界：BT iAP2 与保留 AirPlay 会话并发是否会互相干扰（RFCOMM 抢占、`this.B` 被覆盖）尚未验证，
> 因此必须开关化、失败只记日志不抛异常。

## 8. V1.8.36/37 证伪 BT iAP2 路线 + V1.8.38 转向「车机 Bonjour 登记」（2026-11-09 18:xx）

**结论先说**：§7.3 的「补 BT iAP2 就能让车机登记」被真机**证伪**。
`GET /ctrl-int/1/connect` 的缺口不在 iAP2，而在**车机侧 Bonjour browser 是否把手机服务上报为
Add（`IosControlClientCallback(AddOrUpdateController)`）**；BT iAP2 不是这条状态机的触发源，
强补它还会让车机反复重协商（15~20s 重建）造成**投屏黑屏**。

### 8.1 三份真机抓包的同一结论（C 级，`logs/v1.8.35|36|37`）

| 判据 | v1.8.35（retained，无 BT iAP2） | v1.8.36（补 BT iAP2） | v1.8.37（+强发 0x4300） |
|---|---|---|---|
| `CTRL_ANNOUNCE` | 2 | 9 | 19 |
| `V160_CTRL_TCP_ACCEPT` | **0** | **0** | **0** |
| `CTRL_INVITATION_ACCEPTED` | **0** | **0** | **0** |
| `CTRL_EARLY_PEER_CONNECTED` | **0** | **0** | **0** |
| `CARPLAY_INBOUND_ANY` | **0** | **0** | **0** |
| `HID_REPORT_ARRIVED` | **0** | **0** | **0** |
| `P1_START` / BT iAP2 | 0 | 7（认证全通过） | 11 |
| 视频 | 20~27fps 正常 | 会话 15~20s 重建 | 会话 15~20s 重建，**黑屏** |

⇒ BT iAP2 跑通（`IAP2_IDENTIFIED` / `MFI_KEY_POSSESSION_VERIFIED`）也不改变
`V160_CTRL_TCP_ACCEPT=0`；车机 EA 侧 `PEER_MESSAGE_IDS SEND count=0 ids=NONE`、
`PEER_EA_PROTOCOLS` 为空，证明**车机在这条链路上根本不宣告 CarPlay**。
V1.8.37 强发 0x4300 只得到 `IAP2_ACK_TIMEOUT_SYN`。

### 8.2 固件门控链复核（A 级 capstone，VA=文件偏移）

```
_CarPlayControlClientEnsureStarted @0x52fc0
  └─ 建 "_carplay-ctrl._tcp" browse（0x5303b，服务类型串 @0xa3d0f）
_CarPlayControlClientBrowseCallback @0x536e0
  └─ event 名表：1=Add / 2=Remove / 3=Stop / 4=Restarted（@0x53727..0x5374f）
  └─ 从 Bonjour 字典取 "services"（键池 @0xb1d09）→ 逐项 0x5ba30 遍历 → ControlClientCallback @0x53c00（xref 3 处：0x5391f/0x539ec/0x53a71）
IosControlClientCallback @0xe1a0
  └─ 0xe1cd cmp ebp,2 ⇒ event<2 即 AddOrUpdateController
  └─ 0xe1f0 call 0x52ac0(dict,&out)   → 取 "name"（实例名，键池 @0xb1cfc）
  └─ 0xe20a call 0x681c0(name,...)     → 字符串转码/拷贝（非 MAC 解析）
  └─ 0xe217 call 0x52b90(dict,&6B)     → 取 6 字节标识（蓝牙 MAC）
  └─ 0xe272 call 0x178b0 = AddCarPlayClient(event,name,mac6,dict)
SendConnect @0x17e10（唯一调用者 @0x14017，日志 'device_id=%s, service_name=%s' @0x13fa8）
  └─ "SendConnect we have %zu entries" @0x9d519
  └─ 0x17f38/0x17f47 双串比较；未命中 ebp=0xb 不发
  └─ 命中 "SendConnect sending: %s, %s" @0x9d54f → call 0xe400 → GET /ctrl-int/1/connect
```

**关键更正**：`AddCarPlayClient` 的数据源是**mDNS 服务字典的 `name` + 6 字节 MAC**，
不是蓝牙配对表（README §6.2 的旧表述按此收窄）。`services` / `ifname` / `removedServices`
都是 **Bonjour browser 返回字典的键**，不是 TXT 键 —— 「补 TXT `services`」的思路作废。

### 8.3 手机侧对应能力全部就绪（A 级源码 + 真机自证）

- 注册 `_carplay-ctrl._tcp`，实例名=`ServiceName()`（=`OPPO_X8U`），端口=`localPort`，
  TXT 10 键（`id`/`deviceid`/`btaddr`=手填 MAC、`srcvers=27.2.0`、`features=0x445F8A00,0x1C340` 等），
  见 `ControlChannel.FillTxt()`、`CarPlayCtrlAdvertiser.q():752-755`。
- 手机自己的 discover 探针能发现该服务：`CTRL_PROBE_FOUND type=_carplay-ctrl._tcp name=OPPO_X8U`
  → mDNS responder 工作正常（本机回环，不等于车机收得到）。
- ctrl TCP ServerSocket 全地址 LISTEN；`ControlRequestParser` 对 `GET /ctrl-int/1/connect`
  回 200 并置 invitation（`ControlRequestParser:168-169`）；`/proc/net/tcp` 证明该端口零入站。
- `CTRL_SERVICE_TXT ... n=0` 是 `onServiceRegistered` 回调不带 attributes 的 Android 行为，
  **不代表车机收到空 TXT**（诊断盲点，不是缺陷）。

### 8.4 定案（替换 §7.3 的修复方向）

缺口收敛为**车机侧一级门控**：`_CarPlayControlClientEnsureStarted` 的 browser 是否启动、
以及它是否把手机服务上报成 Add。两种可能：

| 可能 | 判据 | 手机侧对策 |
|---|---|---|
| (a) 车机 browser 根本没启动（状态机未进 CarPlay / 未认定已配对设备） | 手机侧收不到任何来自车机的 `_carplay-ctrl` PTR query | 无解，需车机侧（蓝牙配对、HMI 重进 CarPlay、重启车机） |
| (b) browser 在跑，但收不到手机应答（车机热点隔离客户端 multicast / 单播未回） | 有 query，但 `V160_CTRL_TCP_ACCEPT` 仍 0 | 向车机网关 **单播 mDNS 通告**（V1.8.38） |

### 8.5 V1.8.38 落地（默认关、零视频回归）

- 新类 `prototype/CarPlayCtrlMdnsHelper`：
  - **探针**：`MulticastSocket(5353)` + `SO_REUSEADDR` + `joinGroup(224.0.0.251)` + `MulticastLock`，
    只记录**非本机**来源的 DNS query，打 `CTRL_MDNS_QUERY from=… q=… carplay_ctrl=true/false`；
    周期性发 `_airplay._tcp.local` PTR query 打 `CTRL_MDNS_PROBE_QUERY`，用来看双向 multicast 是否通。
  - **单播通告**：每 5s 构造 PTR+SRV+TXT+A 四条 Answer 的 mDNS 应答，**同时**发给
    224.0.0.251:5353 与车机网关:5353，打 `CTRL_MDNS_ANNOUNCE n=… unicast=… to=…`。
- 开关 `ctrl_mdns_enhance`（默认 **false**）：`ControlChannel.CtrlMdnsEnhanceEnabled()`、
  设置 → 连接「车机发现增强（mDNS）」、adb `DEBUG_SET_CTRL_MDNS --ez enabled true`。
- 挂钩：`CarPlayCtrlAdvertiser.q()` 注册成功后 `start(...)`，`close()` 里 `stop()`；
  所有异常只记 `CTRL_MDNS_START_FAILED`，绝不影响注册主流程。
- **明确不碰**：AirPlay 会话、RECORD、HID、BT iAP2、0x4300。P1-1/P1-3 保持默认关，
  并标注「已知导致车机重协商与投屏黑屏，勿开」。

### 8.6 判据（按序）

1. `CTRL_MDNS_STARTED` 出现（说明探针起来，端口未被占用）。
2. 车机是否发 `_carplay-ctrl` query → `CTRL_MDNS_QUERY … carplay_ctrl=true`。
3. 是否有来自 `192.168.5.1` 的任意 mDNS 报文 → `CTRL_MDNS_QUERY from=192.168.5.1`（下行连通性）。
4. `V160_CTRL_TCP_ACCEPT≥1` / `CTRL_INVITATION_ACCEPTED≥1`。
5. `CARPLAY_INBOUND_ANY≥1` → `HID_REPORT_ARRIVED≥1` → `TOUCH_FORWARDED≥1`。

### 8.7 边界

- 单播 mDNS 应答能否被车机 Bonjour browser 接受**未验证**；若车机只接受 multicast 且热点隔离，
  该手段无效。
- 车机 browser 启动条件仍属静态字节码无法证明的部分（需车机侧日志/整车抓包）。
- 本轮不改业务协议、不动已验证视频链路；V1.8.38 只加发现层能力与诊断。

