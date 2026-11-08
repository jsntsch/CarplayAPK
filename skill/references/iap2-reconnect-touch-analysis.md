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
