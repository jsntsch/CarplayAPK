# CarPlay 配对与车机启动自动重连：固件 vs APK 流程对比

> 2026-11-07 静态对比。对象：Buick Info3.6 (`V167.3.1-GA`) `libNmeCarPlay.so` + 车机 HMI odex
> ↔ 本项目 `android-project` (V1.8.24 / 361)。**结论为静态逆向，未做真机 A/B 验证**。

## 0. 结论先行

两侧对「谁发起连接」的理解是**相反的**，这是「必须手机打开自动连接 + 车机点 CarPlay 才能连上」
以及「车机认得上次那台手机、却建不起连接」的共同根因：

- **车机设计**：车机是**连接发起方**。点火后从持久化恢复已配对手机表 → 蓝牙自动识别 →
  browse `_carplay-ctrl._tcp` → 向手机 ctrl 端口发 `GET /ctrl-int/1/connect` → 手机应答 → 会话登记。
  手机只需要**常驻广播 + 应答**。
- **本项目 APK**：手机是**唯一连接发起方**。除 `MainActivity` 的 UI 点击外没有任何启动源；
  车机主动发来的 connect 邀请只被记一行日志，不会拉起服务或新建会话。

结果：车机单方面发起时手机没有进程/广播可被回连；手机单方面发起时车机的 `SendConnect`
又因客户端表未登记而不发 connect。只有两边**同时被人工触发**才碰巧对齐。

## 1. 证据与等级

| 等级 | 来源 | 说明 |
| --- | --- | --- |
| A（字节码） | `firmware/Buick Info3.6/system/lib64/libNmeCarPlay.so` | x86-64 ELF，首 LOAD 段 vaddr=0 ⇒ **VA = 文件偏移**；capstone 14.4 万条指令 |
| A（字节码） | 本项目 265 个 Java 文件 | 行号/方法签名可复核 |
| B（字符串） | `GMSmartConnect.odex`(10MB, @0x5986ac 等)、`GMCarPlay.odex`(@0xcad4)、`MediaManagerService.strings.txt` | **未经反编译**，只证明存在该符号/常量，不证明调用时序 |
| C（历史日志） | `logs/` 全量扫描 | `CTRL_INVITATION_ACCEPTED` / `CTRL_IDENTITY_BOUND` 全历史 0；`V160_CTRL_TCP_ACCEPT` 仅 2 次且在广播开始前 54s |

## 2. 车机侧（Buick Info3.6）

### 2.1 组件分层（固件 README §三 + B 级字符串）

```
GMCarPlay.apk (UI)  ──AIDL──>  CarPlayService / gm.media.MediaManagerService (native)
   └ PROMPT_CARPLAY_CONNECTED / PROMPT_ABRUPT_DISCONNECTION / CARPLAY_PAIRING_INITIATED
GMSmartConnect.apk (设备/蓝牙/配对 UX)  ← carplay_devid / isBtPaired / getPairedPhonesDetail
MediaManagerService: startProjectionAuth / OOBBTPairingLinkKeyInformation / startSync
   └ dlopen → libNmeCarPlay.so (AirPlay server + iAP2 + HID + CarPlayControlClient)
```

### 2.2 首次配对（B 级为主）

| 步 | 证据 |
| --- | --- |
| 用户点 CarPlay / 蓝牙配对 | `android.intent.action.CARPLAY_PAIRING_INITIATED` (GMCarPlay.odex @0xcad4)；`progress_pairing` / `pair_success` / `pair_failure` / `already_paired` / `setPairingConfirmation` |
| 蓝牙 OOB 交换 link key / BT MAC | `MediaAppleInterface::OOBBTPairingLinkKeyInformation()`、`m_deviceType=%s, mEnableOOBBTPairing=%d, mEnableWIFIComponent=%d`、`CINEMO_METANAME_IAP_BT_MAC_ADDRESS`、`bluetoothIDs` |
| Apple EAP 鉴权 | `MMS: MediaCarPlayAuthentication() - startProjectionAuth(): Entry`、`Received EAPSessionInitiated callback`；失败即整体失败 |
| 记录设备 | `carplay_devid` / `carplay_phone_name` / `carplay_pp_capability` / `carplay_icon`（GMSmartConnect.odex 字符串表相邻 @0x5986ac）；`CARPLAY_STATE_NOT_CONNECTED` ↔ `CARPLAY_STATE_SESSION_INPROGRESS` (@0x382cf5) |
| 登记 CarPlay 客户端 | **`AddCarPlayClient()` @0x178d0**（见 2.4） |
| 会话开始 | `SessionStarted()` @0x18727（回调槽 `[obj+0x828]`）、`OnSessionDisableBluetooth()` @0x171ef（槽 `[obj+0x840]`，会话转到 WiFi 后关蓝牙）、`Wireless Carplay Device connected` |

### 2.3 点火 / 启动后自动重连（A 级链路）

```
车机上电
 → GMSmartConnect 读蓝牙配对表 (isBtPaired / isBtConnected / isBTIdDevice /
    getPairedPhonesDetail / BLUETOOTH_DEVICE_AUTO_IDENTIFICATION_CalID)     [B]
 → 蓝牙自动回连上次手机 + EVENT_PHONE_PAIRED_DEVICE_CHANGED                [B]
 → AddCarPlayClient(event=0, ..., BT_MAC, 设备标识) 恢复 [obj+0x9e0] 客户端表 [A]
 → _CarPlayControlClientEnsureStarted() @0x52fc0
    · [obj+0x30] 已启动则跳过，否则 0x5303b 建立 "_carplay-ctrl._tcp" browse
 → _CarPlayControlClientBrowseCallback() @0x536e0
    · event=1 "Add" → 0x53860 → 0x53c00 ControlClientCallback 读
      "removedServices"/"services" → 0x537d0 激活服务
    · event=2 "Remove" → 0x5379f；event=4 "Restarted" → 0x53a30
 → SendConnect() @0x17e30：在客户端表内做双字符串匹配；命中才发命令
 → _CarPlayControlClientSendCommand() @0x54900：
      GET /ctrl-int/1/connect HTTP/1.1
      Host: <...>  User-Agent: AirPlay/320.17.3
      AirPlay-Receiver-Device-ID: %llu        (车机自己的 receiver id)
 → 手机回 200 → SessionStarted → fnCarplayServiceUpdate 回调 [obj+0x898] 通知 HMI
```

**失败自动恢复（车机侧自带）**：`_BonjourBrowser_ReconfirmDevice` @0x581f2、
`_BonjourBrowser_RetryTimerFired` @0x57e03 / 0x57e8a / 0x5b61b（browse 失败重试）、
`_IdleStateKeepAliveStart` @0x4b693 / 0x4be23、`_IdleStateKeepAliveStop` @0x48d61 等、
`_IdleStateKeepAliveReceiveBeacon` @0x4c18e、`_ControlIdleStateTransition` @0x475ca。
即车机有**独立于 App 的常驻保活与重发现机制**。

### 2.4 两个关键门控（A 级，本次新定位）

**`AddCarPlayClient()` @0x178d0–0x17ca1**
- `0x178f7` 把 6 字节入参格式化成 `%02X:%02X:...`（蓝牙 MAC 字符串，存 `[rsp+0x80]`）；
  第二个标识串在 `[rsp+0x40]`。
- 客户端表 `[obj+0x9e0]`，条目步长 `0x30`。`0x179a0 cmp ebp,1`：
  **event=0 添加**（`0x17aad` 构造条目、`[+0x68]=1`、`0x17d20` 入表）、**event=1 删除**（`0x17b00` 匹配后 `0x17d90` erase）。
- 添加前用 `0xbce0` / `0xbcf0` 做**双字符串比对** `AddCarPlayClient() %s == %s && %s == %s`（@0x17a66）去重。
- 结尾 `0x17ba7` 判 `[obj+0x898]` 非空后调 `fnCarplayServiceUpdate` 回调（@0x17c25）。

**`SendConnect()` @0x17e30–0x1806a**
- 遍历同一张表，`0x17f31`/`0x17f41` 两次 `0xbcf0` 比较 `[entry+0x10]` 与 `[entry+0]` 两串。
- `0x17f72 cmp rbx, rax; je 0x18040` ⇒ **遍历完仍未命中 ⇒ `ebp=0xb` 直接返回，不发任何 connect**；
  只有命中才 `0x17fdf → call 0xe400` 发送。
- ⇒ 车机**只向客户端表里已登记的 (MAC, 设备标识) 二元组**发 connect。这就是「认得上次手机」的判据。

## 3. 手机侧（本项目 APK，A 级）

### 3.1 首次配对 / 每次连接

```
MainActivity.d2()/e2() → F0(selectedBT) :353   ← 唯一发起点
 → startForegroundService(WirelessCarPlayService.V0 + selectedBtAddress)
 → onStartCommand :3939 → W2() :4061 → X2(-1) → c2() :2644
 → PairingSession (蓝牙 RFCOMM "CarPlus iAP2 Phone" :2423)
 → iAP2 0x1D00/01/02 + MFi 证书 + WiFi 凭证（详见 iap2-carplay-flow.md）
 → z1(Network) :3655 → new AirPlaySession :3672
 → PairingTlv8.h() :4692 → /pair-verify 或 /pair-setup → /fp-setup → SETUP → RECORD
```

持久化（全量 `getSharedPreferences`）：

| prefs 文件 | 键 | 语义 |
| --- | --- | --- |
| `wireless-certificate-trust` | `approved_leaf_<MAC>` | **事实上的"已配对"硬门**，需用户弹窗确认 |
| `carplay-own-identity` | `identifier` / `ed25519_seed` / `ed25519_public` | 本机长期密钥，稳定 |
| `carplay-authenticated-peers` | `SHA-256(车机 pairing identifier)` → 公钥 | 键来自**车机返回值** |
| `wireless-ui` | `selected_address` | 上次选的车机蓝牙 MAC |
| `wireless-session-status` | `running` / `state` / `connection_route` | UI 状态镜像 |

**没有** `carplay_devid` / `last_paired_phone` / `auto_connect` 之类键，也没有 `isPaired()` 方法。

### 3.2 "自动连接"与重连的真相

| 项 | 实证 |
| --- | --- |
| 静态广播接收器 | `AndroidManifest.xml` **0 个 receiver**，无 `RECEIVE_BOOT_COMPLETED`；全工程无 Alarm/Job/Work |
| 服务存活 | `onStartCommand` 返回 `2` = `START_NOT_STICKY`；`:3971` `"Session already running; no automatic second connection"` |
| 进程重启 | `CarPlusApplication.onCreate():52-59` 把 `running` 置 false 并记 `PREVIOUS_SESSION_PROCESS_EXIT` |
| UI 开关 | `MainActivity.onCreate():1814` 的「自动连接」是 **RadioButton**，只存 `onSaveInstanceState` 的 `home_manual_route`，**不落 prefs** |
| `_carplay-ctrl` 广播 | `StartEarly()` 唯一调用点 `MainActivity.onResume():2021`；需 `DeviceMac()` 手填非空、需已连 WiFi，否则 `CTRL_EARLY_SKIPPED` |
| 广播注销 | `StopEarly()` 唯一调用点 `PairingSession.Q1():1826`；`close():483` `await(400ms)` 后再 `new ...q()` ⇒ 空窗 + `(2)` 改名风险（V1.8.24 已加 latch） |
| **收到车机 connect 邀请** | `CarPlayCtrlAdvertiser.l():242-340` 会 `ControlRequestParser.d()` 解析并回 `e(200)`，`MarkCtrlInvitation()` + `CTRL_INVITATION_ACCEPTED` + `this.g.b()`；但 **`StartEarly()` 的 `b()` 回调只打 `CTRL_EARLY_PEER_CONNECTED` 日志**（:555-564），不会启动服务。只有已存在 `PairingSession` 时的 `e.b()`(:454-463) 才会触发发现 |
| 请求格式匹配 | 固件发 `GET /ctrl-int/1/connect` + `AirPlay-Receiver-Device-ID: <数字>`；`ControlRequestParser.c()` 正好只认这个 GET（非 GET→405，`Content-Length` 非 0→拒绝）。**格式本身是对得上的** |
| 硬门 | `WirelessCarPlayService.z1():3647` `certPin==null && this.j==null → throw`；证书未确认时 `:563` 明写 `"no trust saved and no automatic reconnect"` |
| 配对复用 | `PairingTlv8.h():389` 先试 `/pair-verify` 复用，`j():293` 用车机返回的 identifier 查表，查不到抛 `PAIR_RESUME_STORED_PEER_REQUIRED` → 回退 `/pair-setup`（实测重连每次均重做） |
| 重连预算 | `Iap2Bootstrap.f():154-182` 最多 3 次、退避 1/3/8s、30s+5s 窗口重置；`Q2():1876-1949` 冷启动复用**任意** WiFi（SSID 仅作诊断） |

## 4. 差异表

| # | 维度 | 车机 Buick Info3.6 | 本项目 APK | 影响 |
| --- | --- | --- | --- | --- |
| 1 | **连接发起方** | 车机主动：`browse → GET /ctrl-int/1/connect` (`0x5303b`/`0x549dd`)，手机是 responder | 手机主动：`MainActivity.F0()` 是唯一入口；车机邀请只记日志 | **最根本**。车机单方面回连时手机无进程；必须两边都人工触发 |
| 2 | 后台常驻 | 系统服务 + `_IdleStateKeepAlive*` / `ReconfirmDevice` / `RetryTimerFired` 常驻保活 | 0 receiver、`START_NOT_STICKY`、广播只在 `onResume` | 熄火/杀进程/退后台后无法被唤醒 |
| 3 | 配对持久化 | `carplay_devid` + `carplay_phone_name` + 蓝牙配对表 + `AddCarPlayClient` 的 (MAC, ID) 二元组表 | `approved_leaf_<MAC>` + `carplay-own-identity` + `carplay-authenticated-peers`；**无 devid 概念** | 车机要匹配的二元组，手机侧没有等价物可稳定提供 |
| 4 | 配对状态机 | `CARPLAY_STATE_NOT_CONNECTED` ↔ `SESSION_INPROGRESS`、`already_paired`/`setPairingConfirmation`、点火后恢复 | 无 `isPaired()`；每次连接都要证书 pin 硬门 + 用户确认 | 每次都被当新设备 → 车机提示「连接新电话」 |
| 5 | pair-verify 复用 | 固件有 `AirPlay Pairing Identity` / `_PairingSessionGetOrCreateIdentityKeychain` / `_PairingSessionSavePeerKeychain` / `Pair-verify server unknown peer` | `PairingTlv8` 的 peer 键取自**车机返回的 identifier**；该值变化就退回 pair-setup | 双方向缓存键不一致，重连永远重新配对 |
| 6 | 蓝牙↔WiFi 切换 | `OnSessionDisableBluetooth()` 会话建立后关蓝牙；iAP2 走独立 `CreateNmeIAPWifiTransport` / `iap://wifi://` | `Q2()` 复用任意当前 WiFi，不验证是否车机热点 | 复用错网 → 解析失败 → 进入 3 次 Recover → 22~50s 断连 |
| 7 | 服务发现身份 | ctrl 客户端读 `name`(`0x52ae7`)、`srcvers`(`0x52c2c`)、`services`(`0x52cd9`)、`ifname`(`0x52d18`) | TXT 10 键无 `services`；实例名可能被 NsdManager 改成 `OPPO_X8U (2)` | 二元组匹配可能失败 → `SendConnect` 返回 0xb（不发 connect） |
| 8 | 失败恢复 | browse 重试 + beacon 保活，无次数上限 | 会话内 3 次 Recover，进程死即终止 | 断连后无法自愈 |

## 5. 差异如何解释实测现象

| 现象 | 解释 |
| --- | --- |
| 「车机识别到之前连接的手机，但无法建立连接」 | 车机恢复客户端表并 browse，但手机未开 App ⇒ 无 `_carplay-ctrl` 广播 / 无 TCP 监听；`SendConnect` 即便发出也拿不到 200 |
| 「手机上打开自动连接后，车机点 CarPlay 才成功」 | 手机侧 `F0()` 主动建会话（AirPlay 方向打通），车机侧人工点图标触发 HMI/配对登记；两条**各自手动**的链才碰巧对齐 |
| 「车机点 CarPlay 提示连接新电话」 | 差异 4/5：手机无 devid 状态、pair-verify 复用失败每次重做 pair-setup，车机按新设备处理 |
| 「22s / ~50s 后断连并 Recovering 1/3」 | 差异 6/8：`Q2()` 复用错网 + 3 次预算 + 车机侧 beacon 超时 |
| 「触摸无反应」 | 车机 `SendConnect` 未成功 ⇒ `AddCarPlayClient` 会话登记未完成 ⇒ HMI 不桥接触摸（`hidSetInputMode` 回 200 只代表固件转发成功）。历史 `CTRL_INVITATION_ACCEPTED` / `CTRL_IDENTITY_BOUND` 全 0 与此一致 |

## 6. 修复方向（按优先级；**V1.8.25 已实施，实现与判据见 §8**）

| 优先级 | 动作 | 代码锚点 |
| --- | --- | --- |
| **P0** | **让手机能被车机回连唤醒**：加 `RECEIVE_BOOT_COMPLETED` + 蓝牙 ACL/WiFi/网络变化 receiver；把 `_carplay-ctrl` 广播与 TCP accept 移到常驻前台服务，`START_STICKY`，去掉 `onStartCommand` 的 "no automatic second connection" 早退（改为幂等） | `AndroidManifest.xml`、`CarPlayCtrlAdvertiser.StartEarly/StopEarly`、`WirelessCarPlayService:3971` |
| **P0** | **车机 connect 邀请直接起会话**：`StartEarly` 的 `b()` 回调不再只打日志，改为用持久化的 `selected_address` 发 `V0` intent 启动服务；`PairingSession.Q1()` 不再注销早广播（避免空窗与 `(2)`） | `CarPlayCtrlAdvertiser:555-564`、`PairingSession:454-463/1826` |
| **P1** | **建立 devid 等价物**：把 `ControlRequestParser.j()` 的 `AirPlay-Receiver-Device-ID` 落盘，作为"已配对车机"主键；SETUP `deviceID` / TXT `deviceid` / 服务实例名与之保持稳定一致，供车机 `(MAC, ID)` 二元组匹配 | `ControlRequestParser.a.j()`、`AirPlaySession:4721`、`ControlChannel.FillTxt()` |
| **P1** | **稳定 pair-verify 复用键**：本机 peer 缓存改按车机蓝牙 MAC 索引，不依赖车机每次返回的 identifier；避免每次退回 pair-setup | `PairingTlv8.j():293`、`WirelessCarPlayService:1141-1183` |
| **P1** | 补 `services` 等固件读取键的 A/B 验证（先在 `CTRL_SERVICE_TXT` 看清现状，再逐个加，不要一次补 22 键 —— V1.7.70 有回归先例） | `ControlChannel.FillTxt()`、`txt-rules.md` |
| **P2** | `Q2()` 增加「当前 WiFi 确为车机热点」判据；放宽 `Iap2Bootstrap` 3 次上限并对齐车机 beacon 周期 | `WirelessCarPlayService:1876-1949`、`Iap2Bootstrap.f():154-182` |

## 7. 结论边界 / 待验证

- 车机 HMI 的 `carplay_devid` 读取时机、蓝牙自动回连的确切触发条件是 **B 级字符串证据**
  （odex 未反编译），不能写成「已证实车机上电必然读该键」。
- `AddCarPlayClient` 二元组里第二个字符串的具体含义（deviceID / name / uuid）尚未从字节码确认，
  只知它与格式化后的 MAC 一起比对。
- 固件 `ControlClientCallback` 读的 `services` 究竟来自 TXTRecord 还是 Bonjour info 字典，需
  进一步跟 `0x5b7a0` 的返回类型确认；**不要据此直接补 TXT 键**。
- 设备当前离线，`V1.8.25` 已出包但**尚未装机**。以上修复方向都需真机 A/B：先看
  `V160_CTRL_TCP_ACCEPT` / `CTRL_INVITATION_ACCEPTED` 是否首次非 0，再看 `HID_REPORT_ARRIVED`。
  完整验证清单见 §8.5。

## 8. V1.8.25 实现（2026-11-07）

产物 `android-project/dist/Carplay-V1.8.25-debug.apk`（**1.8.25 / 362**），
`compileDebugJavaWithJavac` 与 `exportApk` 均 BUILD SUCCESSFUL，`verify_built_apk.sh` **PASS**
（debuggable / 签名 / dex 锚点 `AUTO_WIFI_MONITOR_READY`）。设备离线，**未装机验证**。

### 8.1 新增文件（`prototype` 包）

| 文件 | 职责 |
| --- | --- |
| `CarPlayLinkStore.java` | 持久化 `carplay-auto-link`：总开关、车机 BT MAC、iAP2 下发的 WiFi SSID/密码/安全类型、`AirPlay-Receiver-Device-ID`、按 MAC 索引的最后 pairing identifier、60s WiFi 请求冷却。**凭据不写日志**，`allowBackup=false`；passphrase 明文存于应用私有 prefs（未引入加密依赖，见 §8.6） |
| `CarPlayWifiConnector.java` | `WifiNetworkSpecifier`（隐藏 SSID、移除 `NET_CAPABILITY_INTERNET`）+ `ConnectivityManager.requestNetwork(..., 45000)`；处理 `onAvailable/onUnavailable/onLost`，保存 `readyNetwork`；权限缺失时明确报 `NEARBY_WIFI_DEVICES_NOT_GRANTED` |
| `CarPlayBootReceiver.java` | 静态 receiver：`BOOT_COMPLETED` / `MY_PACKAGE_REPLACED` / `QUICKBOOT_POWERON` → `ensure()` 拉起常驻服务 |
| `CarPlayAutoReconnectService.java` | 常驻 FGS（`START_STICKY`、`connectedDevice`、通知渠道 `carplay-auto-link`）；动态注册蓝牙 `ACL_CONNECTED`、注册 WiFi 可用监听；触发连热点 / 重启 ctrl 广播 / 起会话 |

### 8.2 修改文件

| 文件 | 改动 |
| --- | --- |
| `AndroidManifest.xml` | 加 `RECEIVE_BOOT_COMPLETED`；声明 `CarPlayAutoReconnectService`（FGS connectedDevice）与 `CarPlayBootReceiver`（exported=true） |
| `CarPlayCtrlAdvertiser.java` | `StartEarly()` 的 `b()` 回调不再只打日志：车机 `GET /ctrl-int/1/connect` 到达时调 `CarPlayAutoReconnectService.onCtrlInvitation()` 自动起会话 |
| `MainActivity.java` | `onResume` 增加 `CarPlayAutoReconnectService.ensure(this, "APP_FOREGROUND")` |
| `WirelessCarPlayService.java` | ① `t()` 把 iAP2 下发的 WiFi 凭证落盘（原来只在静态字段 `l1`）；② `onStartCommand` 的 `null intent` 由 STOP 改为自动重连、`selectedBtAddress` 从 prefs 恢复、`b0` 早退改幂等并返回 sticky、成功路径 `START_STICKY`；③ 修掉两处早退不复位 `b0/f1` 的状态泄漏；④ `Q2()` 仅当保存过真实 SSID 且不匹配时拒绝复用（P2） |
| `ControlChannel.java` | `MarkCtrlInvitation()` 落盘 receiver id（P1 的 devid 等价物） |
| `PairingTlv8.java` | 记录按 MAC 索引的 pairing identifier、`PAIR_VERIFY_PEER_KEY_LOOKUP` 诊断、复用失败计数（P1，**不伪造身份、不删既有配对**） |
| `Iap2Bootstrap.java` | 重连预算 3 → 6 次（P2），退避 1/3/8s 与 30s 稳定窗口不变 |
| `app/build.gradle.kts` | 版本 1.8.24/361 → 1.8.25/362 |

### 8.3 完整触发链（本次核心诉求）

```
手机开机 / 打开 App
 → CarPlayBootReceiver / MainActivity → CarPlayAutoReconnectService.ensure()
 → 常驻 FGS：动态监听蓝牙 ACL_CONNECTED + WiFi 可用
用户连上车机蓝牙（ACL_CONNECTED，MAC == selected_address）
 → AUTO_BT_HEAD_UNIT_CONNECTED，记录 5 分钟窗口
 → 路径 A（有 iAP2 保存的热点凭证）：
      CarPlayWifiConnector.connect() → requestNetwork（首次可能弹系统同意框）
      → onAvailable → 重新注册 _carplay-ctrl → startSession(network)
 → 路径 B（Info3.6 不下发凭证，但系统已保存车机热点）：
      系统自己回连 WiFi → WiFi Monitor onAvailable（在蓝牙窗口内）
      → 重新注册 _carplay-ctrl → startSession(network)
 → 路径 C（车机先发起）：
      车机 browse 到 ctrl 服务 → GET /ctrl-int/1/connect
      → StartEarly.b() → onCtrlInvitation() → startSession()
 → WirelessCarPlayService.V0 + selectedBtAddress + useCurrentWifi/handle
 → 既有 AirPlay 握手链路（不改动）
```

`StartEarly`/`StopEarly` 的重启放在工作线程并 `synchronized`，避免 400ms 注销 latch 卡主线程或两条 WiFi 路径并发重注册。

### 8.4 Android 平台约束（minSdk 33 / targetSdk 37）

- **后台启动 FGS**：Android 12+ 禁止，`BOOT_COMPLETED` 是豁免；蓝牙 ACL 广播不做静态注册，改在常驻服务内动态注册。服务需先在跑（开机自启或 App 打开过一次）才能收到后续蓝牙连接事件。
- **连 WiFi**：Android 10+ `WifiManager.addNetwork/enableNetwork` 对应用无效，只能用 `WifiNetworkSpecifier`；`requestNetwork` 首次可能弹"允许连接"系统对话框，且需运行时 `NEARBY_WIFI_DEVICES`（Manifest 已声明 `neverForLocation`）。
- **前台服务类型**：API 34+ 用三参 `startForeground(..., FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)`，API 33 用两参。
- **不误触发**：WiFi 监听只在最近 5 分钟内有匹配车机的蓝牙事件时生效，连家里 WiFi 不会自动起 CarPlay。

### 8.5 真机验证判据（按序看）

1. `APP_BUILD version=1.8.25`、`AUTO_SERVICE_CREATED sticky=true`、`AUTO_BT_RECEIVER_REGISTERED`、`AUTO_WIFI_MONITOR_REGISTERED`。
2. 连车机蓝牙 → `AUTO_BT_HEAD_UNIT_CONNECTED match=true`。
3. `AUTO_WIFI_REQUEST_SUBMITTED` + `AUTO_WIFI_AVAILABLE`（路径 A），或 `AUTO_WIFI_MONITOR_READY source=SYSTEM_SAVED_NETWORK`（路径 B）。
4. `AUTO_SESSION_START_REQUESTED use_current_wifi=...` → 会话日志 → `VIDEO_PROGRESS` / `AUDIO_CAPTURE_READ`。
5. 车机侧发起时：`AUTO_CTRL_INVITATION_RECEIVED`、`CTRL_INVITATION_ACCEPTED`（历史全 0）、`V167_CTRL_IDENTITY_BOUND`。
6. 最终仍看 `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED`；`AUTO_LINK_WIFI_SAVED`（首次 iAP2 下发凭证后）与 `AUTO_LINK_RECEIVER_ID_SAVED`（收到 ctrl connect 后）。
7. 异常路径：`AUTO_WIFI_PERMISSION_MISSING`（去设置授予"附近的设备"）、`AUTO_WIFI_UNAVAILABLE`（系统同意框未确认 / 45s 超时）、`CURRENT_WIFI_REUSE_REJECTED`（连错热点）、`AUTO_WIFI_SKIPPED reason=NO_SAVED_CREDENTIALS`。

### 8.6 已知边界 / 风险（不要写成已解决）

- **未装机**：以上全部是静态实现与编译验证，真机行为未验证；设备 `adb devices` 为空。
- **WiFi 凭证来源单一**：只有 iAP2 下发过凭证（`WirelessCarPlayService.t()` 被调到）才会落盘。Info3.6 历史实测 `wifi_request=false`，此时路径 A 不可用，只能靠路径 B（系统已保存热点）或路径 C（车机发起）。若系统也没保存过车机热点，仍需用户首次在系统 WiFi 里手动连一次。
- **P1 的能力边界**：车机若每次下发新 pairing identifier，`pair-verify` 的签名校验（`bVarB.a.equals(strL)`）决定了按 MAC 缓存**不能**替代它；本次只落盘 devid + 诊断，**没有**绕过校验，也没有改 SETUP/TXT 的 `deviceID`。§4 的"每次重做 pair-setup"是否因此改善，待真机日志确认。
- **凭据存储**：passphrase 明文存应用私有 prefs（`allowBackup=false`）。量产前应改用 Android Keystore / `EncryptedSharedPreferences`。
- `carplay-auto-link` 的自动重连总开关默认开启，目前**没有 UI 开关**，只能通过 `CarPlayLinkStore.setEnabled()` 代码调用关闭。
- `PairingSession.Q1()` 仍会 `StopEarly()`（本版未改），V1.8.24 的注销 latch 已缓解 `(2)` 改名；双路径重注册的并发由 `restartCtrlAndStartSession()` 串行化。