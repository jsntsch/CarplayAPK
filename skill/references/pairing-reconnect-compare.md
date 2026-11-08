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
## 9. V1.8.28 复核与字节码补证（2026-11-08 18:11–18:40）

> 静态逆向 + 当前工作区源码（**1.8.28 / 365**，且方案 B 相关改动全部**未提交**）；未改业务代码、未做真机 A/B。
> 本节**修正 §0、§4、§5 中已过时的结论**，并补上 §7 三个待查项里最关键的触摸门控。

### 9.1 必须纠正的旧结论

| 旧结论（§0/§4/§5） | 现状 | 证据 |
|---|---|---|
| 「两侧发起方向相反是根本根因；车机邀请只记日志」 | **已部分修复**：V1.8.25 起有 `CarPlayBootReceiver` + `CarPlayAutoReconnectService`(START_STICKY FGS) + `StartEarly` 常驻 `_carplay-ctrl` 广播/accept；`CarPlayCtrlAdvertiser.b()` 收到 `GET /ctrl-int/1/connect` 调 `onCtrlInvitation()` 自动起会话 | `CarPlayCtrlAdvertiser.java:561-567`、`CarPlayAutoReconnectService.java:81-94` |
| 「pair-verify 复用恒 0，每次重新配对」 | **已过时**：真机日志 `PAIR_VERIFY_FIRST_OK` 12 条 / 4 文件、`CARPLAY_PAIR_VERIFY_RESUMED_STORED_PEER` 29 条 / 20 文件（11-06 22:04 起） | `logs/` 全量 grep |
| 「车机从不回连 ctrl」 | 极少但非零：`CTRL_INVITATION_ACCEPTED` 4 条 / 1 文件、`V160_CTRL_TCP_ACCEPT` 2 条 / 1 文件 | 同上 |
| 「0 receiver / START_NOT_STICKY」 | Manifest 已有 `CarPlayBootReceiver` + 3 个 service；`CarPlayAutoReconnectService` 显式 `START_STICKY`；但 `WirelessCarPlayService.onStartCommand` 成功路径 `return 3`(=START_REDELIVER_INTENT)，**注释自称 STICKY，数值与注释不一致** | `AndroidManifest.xml:62-86`、`WirelessCarPlayService.java:4097` |
| 规范 Table 23-71 顶层键 | Cinemo 固件按 `{type, params}` 解析；本轮字节码复核 0x421f3 取 params、0x447d0 从 params 取 `appStates`(0x4480c)/`resources`(0x44991)，params 空 → 0x422b5 422 且跳过 0x4227a 响应体 ⇒ 空 body。当前 APK 四种命令全部 `{type,params}` | `SessionStateMachine.Q()`、`AirPlaySession.z0()/t0()` |

### 9.2 固件侧新增硬证据（capstone 全量 xref）

**CFLString 键表编码**：`0xb1000..0xb1b00` 每键 = 8B magic `56 07 01 00 ff ff ff 7f` + ASCII\0；rip 引用指向 magic 起点，**字符串在 +8**（旧脚本读目标地址得乱码即因此）。共 140 键。

**车机→手机反向控制通道统一发送门（触摸真断点）**：

| 地址 | 结论 |
|---|---|
| `0x45100` / `0x45117` | `cmp byte ptr [rdi+0x466],0` / `je 0x45150` ⇒ 为 0 直接返回 `0xffffe5cb`，**不发任何命令** |
| `call 0x45100` **9 处** | 0x4a314 `changeModes`、0x4a4b2 `forceKeyFrame`、0x4a5a4 `requestSiri`、0x4a68e `requestUI`、0x4a796 `setNightMode`、0x4a896 `setLimitedUI`、0x4a9a3 `iAPSendMessage`、0x4aa94 `updateVehicleInformation`、**0x4ab62 `hidSendReport`**（顶层 `uuid`+`hidReport`，无 params；其余 8 条均带 `params`） |
| `0x484f1` | `[rbx+0x466],1` **全局唯一写入点**，在函数 `0x483b0`（唯一调用者 `0x41a7c`，位于 SETUP 的 `X-Apple-Durations` 解析段 0x41800..0x41a90） |
| 0x483f0..0x484eb 前置门 | `[+0x488]≥0 → call 0x485a0`；状态 `[+0x4a8]==0x64/0x66`、`[+0xdc0]==0x65`、`[+0x1cd8]≥0 → call 0x48c30`；最后 `call 0x3d670(...,"startSession",...)` 返回 0 |
| 回调槽 | `modesChanged`→`[r14+0x60]` @0x443fd；`requestUI`→`[r14+0x68]` @0x44491（调用 0x44518）；`hidSetInputMode`→`[r14+0x48]` @0x4454e（调用 0x44624）。槽为 NULL 时固件只转发上层、不解释 mode ⇒ **200 ≠ HMI 启用触摸**。`SetCallbacks` @0x160f0 仅 @0x132d4 调 1 次 |

⇒ **「触摸无反应」不是 HID 描述符 / inputMode / uuid / 422 载荷问题（V1.8.20~24 已逐项修完并 200）**。
~~而是车机侧 SETUP/startSession 状态机未走完 ⇒ `[+0x466]` 没开 ⇒ 9 条反向命令（含 hidSendReport）一条都不发~~
**【后半句已被 §14 修正：RECORD 回 200 意味着 `[+0x466]` 大概率已置位，缺口转向「谁产生反向命令」】**与历史 `CARPLAY_INBOUND_REQUEST`/`INBOUND_REQ`/`HID_REPORT_ARRIVED`/`TOUCH_FORWARDED` 全 0、`CARPLAY_EVENT_INPUT raw_bytes=0` 完全吻合。

**车机启动链复核**：`CarPlayControlClientStart` 串 xref 唯一 @0x52f90；`EnsureStarted` @0x52fc0 `cmp [rbx+0x30],0` → 0x5303b `lea '_carplay-ctrl._tcp'` + `'local.'`；`AddCarPlayClient` @0x178d0 格式化 6B MAC；`SendConnect` @0x17e30 双串匹配（`%s == %s && %s == %s`），未命中 @0x17f72 返回 `0xb` 不发。

**固件确有完整 AirPlay 配对**：`/pair-setup`、`/pair-verify`、Pair-setup/verify server、`_PairingSessionGetOrCreateIdentityKeychain` / `_PairingSessionSavePeerKeychain`、`AirPlay Pairing Identity`、`Pair-verify server unknown peer`。与 diplay 模拟端（`OPTIONS` 无配对端点、POST 一律 400/470）形成对照。

### 9.3 当前 APK 与固件仍存的实质差异

| # | 维度 | 固件 Info3.6 | V1.8.28 工作区 | 影响 |
|---|---|---|---|---|
| 1 | **配对层次** | 蓝牙 OOB + Apple EAP + AirPlay pair-setup/verify + `AddCarPlayClient` 会话登记（三层） | 只有 AirPlay 层 `PairingTlv8.h()`；无 OOB/EAP/devid/客户端表 | 车机按新设备处理、HMI 不登记 |
| 2 | **配对身份键** | `(BT MAC, 第二标识串)` 二元组 + keychain identifier | `SHA-256(车机返回的 identifier)` + `approved_leaf_<MAC>`；receiver id 落盘但未参与二元组匹配 | `SendConnect` 匹配不上 → 不发 connect |
| 3 | **重连恢复** | 点火读蓝牙配对表 → 恢复 `[obj+0x9e0]` 客户端表 → browse → connect；常驻 beacon + 无限重试 | BOOT / APP_FOREGROUND / 蓝牙 ACL / WiFi 可用 / ctrl 邀请 → `V0` 起会话；3→6 次 Recover + 5 分钟蓝牙窗口 | 车机单方面点火时，手机需有事件或已常驻 |
| 4 | **匹配门** | `SendConnect` 不匹配静默不发 | V1.8.28 放宽 `i2()` 优先读 `selected_address`、`v2()` 信任手动选择 | APK 无等价门，但也无可供车机匹配的稳定标识 |
| 5 | **广播身份** | 按蓝牙名/二元组匹配，读 `name/srcvers/services/ifname` | 实例名 = 手动蓝牙名，可能被 NSD 改成 `(2)`；TXT 10 键无 `services` | 二元组匹配可能失败 |
| 6 | **SETUP** | 0x41800 段解析 `X-Apple-Durations`；0x483b0 startSession 状态门 | **全工程无 `X-Apple-Durations`**；`streams` 只发 type110 视频，**音频流从未协商**（`pcmBytes=0`） | 最实质的协议差异，**疑为 `[+0x466]` 未置位原因，待 A/B** |
| 7 | 配对异常兼容 | 固件有 455 / unknown peer | 只有 455 回退；`EventChannel.b.g()` 只认 200，**无 470/400 分支** | diplay 上 470→400 直接 `CONNECT_FAILURE` |

### 9.4 下一步判据（按序）

1. 真机抓包先看**任意入站**（不只 `hidSendReport`）：`CARPLAY_INBOUND_REQUEST` / `INBOUND_REQ`。若连 `forceKeyFrame`/`setNightMode` 都没有 ⇒ `[+0x466]` 未开，问题在 SETUP/startSession，别再查 HID。
2. A/B：SETUP 补 `X-Apple-Durations` + 完整 audio stream（type 100/130），看入站是否出现、`pcmBytes` 是否 >0。
3. 身份侧：实例名严格等于车机记录的蓝牙名（消除 `(2)`），`AirPlay-Receiver-Device-ID` 与 SETUP `deviceID` / TXT `deviceid` 稳定一致，供 `SendConnect` 二元组匹配。
4. 不要再扫 `hidInputMode` / 改 HID 描述符 / 回到顶层键写法（案例 9~13 已证伪）。

### 9.5 边界

字节码能证明门的位置与条件，**真机某次会话 `[+0x466]` 是否置位无法静态得知**，需车机侧日志或整机抓包。odex 未反编译 ⇒ 点火时序仍 B 级；`AddCarPlayClient` 第二标识串含义未定。方案 B 全部为未提交工作区改动，对比结论仅对当前工作区成立。

## 10. APK 侧如何让固件打开反向控制通道门（规范 + 固件 + 源码三方对齐，2026-11-08 19:47）

> 只做分析与方案设计，**未改业务代码**。对象仍是 V1.8.28/365 工作区（方案 B 未提交）。

### 10.1 规范依据（docs/Apple-CarPlay-Specification.pdf，PDF 页）

| PDF 页 | 条款 | 结论 |
|---|---|---|
| 48–49 | §23.3 典型会话生命周期 | ①TCP 控制连接 ②MFi-SAP `/auth-setup` ③`GET /info` **带 binary plist 请求体**（可提供手机 info / qualifier）④初始 `SETUP` 配控制流 ⑤**Apple device 为每个 TCP 流主动发起连接** ⑥`RECORD` 启动车机会话 ⑦**会话建立后再发 SETUP 配 audio/screen 流** ⑧交换数据 ⑨TEARDOWN |
| 61 | Table 23-36 初始 SETUP 必填(Y) | `deviceID`/`eiv`/`ekey`/`et`/`macAddress`/`model`/`name`/`osBuildVersion`/`sessionUUID`/`sourceVersion`/`streams(*)`/`timingPort`；响应 Table 23-37 `eventPort`/`keepAlivePort`/`streams` |
| 64 | Table 23-45 StreamID | **100 MainAudio / 101 AltAudio / 102 MainHighAudio / 110 Screen** |
| 65 | Table 23-47 Channels | **Controller control channel**(device→accessory, TCP 5000) 与 **Accessory control channel**(accessory→device, dynamic TCP) 是两条独立 RTSP 通道 |
| 97 | §23.3.7 Commands | 命令走 `/command`「on the control stream」；**手机命令走 Controller channel，车机命令走 Accessory channel**；每条命令必须有 HTTP 响应 |
| 79 / 82 | §23.3.5.1.3 | Apple device 不持有屏幕时 accessory **不得**发 HID 用途 |
| 101 | §23.3.7.7 / Table 23-74 | `hidSendReport` Sender=Accessory，顶层 `hidReport`/`uuid`（`params` 只出现在 changeModes 响应 Table 23-70） |

### 10.2 固件门的新证据（capstone，VA=文件偏移）

- `0x45100` 统一发送门：`cmp byte ptr [rdi+0x466],0` → 0 直接返回 `0xffffe5cb`；9 条车机→手机命令全走它，其 payload 由 `[rdi+0x468]` 对象经 `0x6cbb0` 发出。
- **`[+0x466]` 唯一写入 `0x484f1`**，在 `0x483b0`（唯一调用者 `0x41a7c`，SETUP 的 `X-Apple-Durations` 解析段）：先按状态 `[+0x4a8]==0x64/0x66`(MainAudio/MainHighAudio)、`[+0xdc0]==0x65`(AltAudio) 调 `0xc780` 启流；`[+0x1cd8]≥0` 时调 `_ScreenStart` @0x48c30（日志 `OSStatus _ScreenStart(...)` @0x48c90）；最后 `call 0x3d670(...,"startSession",...)` 返回 0 才置 1。
- **`_GeneralAudioSetup` @0x4666d 日志 `'%s audio set up for %s on port %d, RTCP on port %d'`；紧接 `0x46687 cmp byte ptr [rbx+0x466],0 / je 0x4634a`** ⇒ **`[+0x466]` 是「会话已 started」门：门没开时音频流 SETUP 只记日志、不启流**（`0x466b8 call 0xc780` / `0x466c9 mov [rbx+0x4d0],rbp` 都在门开分支）。
- ⇒ 顺序必须是 **startSession(RECORD) → `[+0x466]=1` → audio/screen 流 SETUP 才生效 → 反向命令门才允许发送**。与规范 step 6→7 完全一致。

### 10.3 APK 侧缺口（当前源码）

| # | 规范要求 | APK 现状 | 代码锚点 |
|---|---|---|---|
| 1 | 初始 SETUP 带 Table 23-36 必填键 | 有 `name/model/deviceID/sessionUUID/sourceVersion=320.17/osBuildVersion/timingPort/timingProtocol/features`(14 能力名)/`keepAliveLowPower`；**缺 `eiv`/`ekey`/`et`/`macAddress`，且无 `streams`** | `AirPlaySession.t0():4711-4760` |
| 2 | `GET /info` 带请求 plist | `R1("GET","/info",null)` —— **空 body**（`R1` 支持 map→BinaryPlist，可直接补） | `t0():4767`、`R1():1309-1341` |
| 3 | RECORD 之后 SETUP audio/screen 流 | `l2()` 只发 **单流 type110**，且在 `g2()` 之后、**RECORD 之前**；RECORD 后**没有任何流 SETUP** | `i0():4276-4306`、`l2():2324-2350` |
| 4 | 音频流 100/101/102 协商 | 从未协商，`pcmBytes=0`；`H1()` 后只开 `p1(MEDIA,48000,2,16)` 占位 + `/feedback` 只报 type110 | `i0():4307-4340`；源码注释 4308-4326 自认「音频流从未协商，RECORD 语义是开始播放全部流」「RECORD ACK 后恒定 10.5s teardown」 |
| 5 | RECORD 应有响应 | 后台线程 `CarPlus-record` 发 RECORD，`CARPLAY_RECORD_NO_RESPONSE_TOLERATED` 吞掉无响应；不等待即置 `l0/E` 并 `H1()` | `i0():4288-4306` |
| 6 | 为每个 TCP 流主动建连接 | 只按 `eventPort` 建 `EventChannel`（`K0()`），未为 audio/screen 建 TCP | `K0():1001-1019` |

### 10.4 修复方案（APK 侧，按优先级；**门在固件，APK 只能让固件自己开**）

| 优先级 | 动作 | 锚点 / 依据 | 风险 | 验证判据 |
|---|---|---|---|---|
| **P0-1** | **把流 SETUP 移到 RECORD 之后**：RECORD 用带超时的同步请求等 200（或至少等 ACK/EOF 后确认），再发 screen(type110) + MainAudio(type100) + AltAudio(101) + MainHighAudio(102) 的 SETUP，并按响应 `dataPort` 主动建 TCP/UDP | 规范 step 6→7；固件 `0x46687` 门 | 中：改会话时序，需保留原后台 RECORD 作为兜底（Info3.6 曾 13s 不回响应）；用开关可回滚 | `AUDIO_STREAM_SETUP_* status=200`、`pcmBytes>0` |
| **P0-2** | **补齐初始 SETUP 必填键**：`eiv`/`ekey`(从 MFi-SAP 会话密钥派生)、`et`(Table 23-24，0x10 MFi-SAP)、`macAddress`；初始 SETUP 加一条控制流描述（或按规范「只设控制流可不填 streams」保持，但需显式记录决策） | Table 23-36；固件 `0x483b0` 状态门 | 中：加密键必须与 `PairingTlv8` 导出的 Control-Read/Write key 一致，不能现编 | `CARPLAY_SETUP_KEYS` 全键可见、车机不回 4xx |
| **P0-3** | **`GET /info` 带 binary plist 请求体**（至少 `qualifier` 或手机 info），让车机获得手机侧信息用于会话登记 | §23.3.2.2 / Table 23-25；`R1()` 已支持 map | 低 | 车机 `/info` 响应键不再被裁、日志出现 `qualifier` |
| **P1-1** | **按规范建立/确认 Accessory control channel**：`eventPort` 连接后主动发一次 RTSP `OPTIONS`/握手并**对每个入站请求回 HTTP 200**（`s1()` 已有分发），同时在 SETUP 响应里解析 `streams[].dataPort` 为音频流建连接 | Table 23-47 / §23.3.7；`K0()`、`s1():2675` | 中：不要新增 `/reverse`/PTTH（固件无此实现，规范也未要求） | `CARPLAY_INBOUND_REQUEST` 任意入站 ≥1 |
| **P1-2** | **会话登记身份闭环**：让 `GET /ctrl-int/1/connect` 真正发生（V1.8.25 已支持 responder），把 `AirPlay-Receiver-Device-ID` 与 SETUP `deviceID`/TXT `deviceid`/服务实例名稳定一致，供固件 `SendConnect()` 二元组匹配 | `SendConnect@0x17e30`、`AddCarPlayClient@0x178d0` | 低 | `CTRL_INVITATION_ACCEPTED` 从 4 条涨到每次会话 ≥1 |
| **P1-3** | **屏幕所有权保持 `entity=1`**：`Q()` 的 `entity=(this.d.a==2?2:1)` 在车机 `/info` 声明归自己时会发 2（维持车机持有）→ 规范禁止发 HID；应显式声明 Controller 持有 | `SessionStateMachine.Q():651-670`；§23.3.5.1.3 | 低-中：需先查真机 `/info` 的 `modes` 实际值 | `CARPLAY_SCREEN_MODES_PAYLOAD ... entity=1` 且 `status=200` |
| **P2** | **能力探测兼容模拟车机**：`OPTIONS *` 无配对端点（diplay）时跳过 `PairingTlv8.h()`；对 Buick 保留原加密链路 | 上一轮 §9；`EventChannel.b.g()` 只认 200 | 高：不得全局关配对 | diplay 不再 `CONNECT_FAILURE`；Buick 仍 `CARPLAY_PAIR_VERIFIED` |

**明确不要做**：①不要再扫 `hidInputMode` / 改 HID 描述符 / 回到顶层键（案例 9~13 已证伪）；②不要伪造 `startSession` 命令给车机（它是固件内部 `0x3d670` 的控制名，不在入站白名单）；③不要一次补 22 个 TXT 键（V1.7.70 回归史）。

### 10.5 能力边界

- `[+0x466]` 是**车机进程内状态**，APK 无 API 可直接置位；只能通过合规的 SETUP/RECORD/流序列让固件自己置位。若固件还需要 HMI 层（`SetCallbacks` @0x160f0 仅 @0x132d4 调一次，槽 `[+0x60]/[+0x48]/[+0x68]`）或蓝牙 OOB 登记，纯 APK 侧可能仍不足 —— **必须真机 A/B 才能排除**。
- 本项目有可用 Gradle 构建链（`gradlew exportApk --offline` + `verify_built_apk.sh`），可做编译与 dex 锚点核验；**真机连接、车机侧日志与整机抓包需用户配合**。
- 静态逆向无法证明真机某次会话 `[+0x466]` 是否置位；odex 未反编译，点火时序仍 B 级。

## 11. V1.8.29：P0-3 / P0-2 / P0-1 / P1-1 / P1-2 / P1-3 一次性落地（2026-11-08 20:30–21:00）

> 版本 **1.8.29 / 366**（`app/build.gradle.kts`），产物 `dist/Carplay-V1.8.29-debug.apk`。
> 编译 BUILD SUCCESSFUL，`verify_built_apk.sh` 六项 PASS，dex 锚点 `POST_RECORD_STREAM_SETUP_SEND` 命中 2 次，
> 15 个新增 `V2_*` / `CARPLAY_INBOUND_ANY` 锚点全部进 dex。**设备 USB 掉线，未安装、未做真机 A/B。**

### 11.1 统一回滚开关（最重要的运维约定）

`ControlChannel.SeqV2Enabled()` 读 `carplayapk-debug.xml` 的布尔 **`session_seq_v2`（默认 true）**。
置 false 即可**不重新出包**完整回退到 V1.8.28 会话序列（6 项改动全部被跳过或恢复旧值）：

```bash
# 写入 false（需 run-as 可读写的临时目录，见下方注意）
adb shell "run-as io.carplay.app sh -c 'printf ... > shared_prefs/carplayapk-debug.xml'"
```

新增 `ControlChannel.V2Log()`：logcat tag `CarPlayAPK` 前缀 `V2_SEQ` **+ 应用内 `files/v2-seq-log.txt` 双写**
（AppLogger 是 prototype 包私有，secured 包不能访问，故独立追加，不改动业务层注入链）。

### 11.2 六项改动与代码锚点

| 项 | 文件 / 方法 | 改动 |
|---|---|---|
| **P0-3** | `AirPlaySession.t0()` | `GET /info` 从 `R1("GET","/info",null)` 改为带 binary plist 请求体（`model`/`name`/`sourceVersion`），**故意不填 `qualifier`** 以免裁剪车机响应；被拒自动回退空 body。依据规范 PDF p.50 §23.3.2.2 |
| **P0-2** | `AirPlaySession.t0()` + 新增 `V2StreamKey()` | 补 Table 23-36 必填 `ekey`(16B data)/`eiv`(16B data)/`et`=0x10/`macAddress`。密钥按规范 p.50 `Bytes0-15 of SHA-512(label + streamConnectionID(8B BE) + master)`，`master=this.m0`（pair-verify 共享密钥），初始 SETUP 用 sid=0。**注意**：规范还要求 ekey 用 master 再加密一层，本版是最小实现、待 A/B。仅当 HTTP 非 200（`EventChannel.d`）才去掉 V2 键重发一次，超时/IO 错误不重发（避免创建第二个会话） |
| **P0-1** | `AirPlaySession.i0()` + 新增 `V2PostRecordStreamSetup()` / `V2PerStreamSetupFallback()`；`R1()`；`EventChannel.setRequestTimeoutOverride()` / `w()` | ① **RECORD 短超时**：`R1()` 对 RECORD 设 1.5s 读超时且 `tolerateReadTimeout=true`，`w()` 超时**不 close 通道**（旧逻辑占锁到 13s EOF 并 close，卡死后续所有请求）；迟到 RECORD 响应由 `staleResponseTolerance` 容忍一次，不再触发 `RTSP CSeq mismatch`。② **流 SETUP 移到 RECORD 之后**：用 `CountDownLatch recordFinished` 在 RECORD 请求结束后由独立线程 `V2PostRecordStreamSetup()` 发 `SETUP` with `streams=[110,100,101,102]`（Table 23-45），失败则逐流重发。③ 保留原 `l2()` 单流 110 在原位，视频链路不回归 |
| **P1-1** | `EventChannel.w()` / `z()`；`AirPlaySession.V2PostRecordStreamSetup()` | 入站回 `RTSP/1.0 200 OK` 本就存在，无需新增；补 `CARPLAY_INBOUND_ANY` 计数日志到**同步读循环与常驻事件读循环两条路径**（固件 `[+0x466]` 门未开时该计数应恒 0）。解析 SETUP 响应 `streams[].dataPort`，对 100/101/102 用 `new Socket()` 主动建 TCP（规范 p.48 step 5）；**刻意不用 `k0()/L0()`**，因 `L0()` 会覆盖 `this.d0` 主 socket；连接存入 `v2StreamSockets` 并在 `AirPlaySession.close()` 统一关闭。不新增 `/reverse`/PTTH（规范与固件均无） |
| **P1-2** | `AirPlaySession.t0()`；`CarPlayCtrlAdvertiser.close()` | SETUP `macAddress` 与 `deviceID` 同源（都取 `LocalBluetoothIdentifier()`），`name` 仍是 `ControlChannel.ServiceName()`，打 `SETUP_IDENTITY_UNIFIED` 一致性日志。针对 `(2)` 改名的**已定位根因**（`unregisterService()` 异步，`Q1()` 立即重注册同名），把注销等待从 400ms 提到 **1500ms** 并打 `CTRL_UNREGISTER_WAIT confirmed=` 证据 |
| **P1-3** | `SessionStateMachine.Q()` | `entity` 从 `(this.d.a==2?2:1)` 改为显式 **1（Controller 持有屏幕）**，符合 §23.3.5.1.3 的车机发 HID 前提；开关关闭时恢复旧语义 |

### 11.3 真机验证判据（按序，别先看 HID）

```bash
adb install -r android-project/dist/Carplay-V1.8.29-debug.apk
adb logcat -b main -v time CarPlayAPK:V *:S | grep V2_SEQ
adb shell "run-as io.carplay.app cat files/v2-seq-log.txt"
adb shell "run-as io.carplay.app cat files/connection-log-0.txt" | grep -E "V2_|CARPLAY_SETUP_RESP_KEYS|pcmBytes|INBOUND"
```

1. `SETUP_ENCRYPTION_KEYS` + `SETUP_IDENTITY_UNIFIED` + `INFO_REQUEST_PLIST`（P0-2/P0-3/P1-2 已生效）；
   若出现 `SETUP_V2_REJECTED` 说明车机不接受新增键，已自动回退，需据此调整 ekey/eiv 派生。
2. `RECORD_SEND_SHORT_TIMEOUT` → `RECORD_NO_RESPONSE_TOLERATED`（不再等 13s、不 close）→ `POST_RECORD_STREAM_SETUP_SEND` → `POST_RECORD_STREAM_SETUP_OK`（P0-1 序列打通）。
3. `STREAM_DATA_PORT` / `AUDIO_STREAM_CONNECTED` → **`pcmBytes>0`**（固件 `[+0x466]` 门开的最强间接证据）。
4. `CARPLAY_INBOUND_ANY count>=1`（任意入站；连 `forceKeyFrame`/`setNightMode` 都没有 ⇒ 门仍未开）→ `HID_REPORT_ARRIVED` → `TOUCH_FORWARDED`。
5. 回归：视频 ≈20fps、音频 ≈136 包/秒、`dropped_frames_total` 不增长、13s / 10.5s teardown 消失。

A/B：把 `session_seq_v2` 置 false 重跑同一场景，对比以上判据即可判定是否为本版引入的收益/回归。

### 11.4 已知风险与边界

- **ekey/eiv 是推导实现**：规范 p.61 说 ekey「Encrypted with master key from encryption setup」，本版只按 p.50 的 SHA-512 派生填明文 16B；若车机据此解密流数据，可能导致视频异常。已用「仅 4xx 时去掉 V2 键重发」做自愈，但**真机必须验**。
- `streamConnectionID` 字节序（8B BE）与初始 SETUP 取 sid=0 均为推导，规范未逐字规定。
- diplay 模拟端**没有 `/pair-setup` `/pair-verify`**（§9/§10 已定案），会话在配对阶段就 `CONNECT_FAILURE`，**走不到本版的 SETUP/RECORD 序列**，无法用来验证这 6 项；必须用 Buick 真车或实现了配对端点的模拟车机。
- `staleResponseTolerance` 只容忍 1 条迟到响应；若车机回多条乱序响应仍可能 `RTSP CSeq mismatch`。
- 静态分析无法证明真机某次会话 `[+0x466]` 是否置位；odex 未反编译，点火时序仍 B 级证据。
- 本版 6 项与之前的「方案 B」全部仍是**未提交工作区改动**。

## 12. V1.8.29 首次真机验证（2026-11-08 22:33–22:41，Buick myBuick GS 车机）—— 结论：连接失败

**环境**：OPPO PKJ110 / 1.8.29(366) / `session_seq_v2` 默认 true；手机 WiFi `myBuick GS` = `192.168.5.100/24`，
车机 `192.168.5.1:7000`（控制）/ `:5001`（事件）。车机开启后自动连接，**未出现稳定画面**。
抓包：`logs/v1.8.29-autoconnect-key.log`（明文 1.2M）/ `...-full.log.gz`（12M，全量 47.6 万行）。

### 12.1 失败形态
不是「连不上」，而是**会话每 ~10.1s 重建一次**：`RECOVERY_WAIT count=N p50_ms=10118 p95_ms=10789`
→ `SESSION_FAILURE detail=VIDEO_SEND_SocketException message=Broken pipe`
→ `EVENT_CHANNEL_SocketException Socket closed`
→ `Recovering selected receiver 1/3 … 4/3` 无限循环（`receiver_render_confirmed=false`）。

### 12.2 前半段全部成功（V2 六项确实生效并被车机接受）
| 阶段 | 证据 |
|---|---|
| 配对 | `CARPLAY_PAIR_SETUP` → `CARPLAY_RESP op=PAIR_VERIFY` → `CARPLAY_PAIR_VERIFIED`；`PAIR_SETUP_PEER id_matched=false new_pairing=true`（每次重新配对） |
| P0-2 | `CARPLAY_SETUP_KEYS n=14 keys=[…, macAddress, ekey, eiv, et]` → **`CARPLAY_SETUP_RESP_KEYS n=3 [keepAlivePort, eventPort, timingPort]`（200）**；`V2_SEQ SETUP_ENCRYPTION_KEYS` / `SETUP_IDENTITY_UNIFIED` |
| P0-3 | `CONTROL_COMMAND_SEND path=/info keys=[model,name,sourceVersion] plistLen=104` → **`CARPLAY_INFO_KEYS n=22`**（含 modes/audioFormats/hidDevices/displays/bluetoothIDs）|
| P0-1 前半 | `V2_SEQ RECORD_SEND_SHORT_TIMEOUT` → **`CARPLAY_RECORD_ACKNOWLEDGED`**（本次车机回 RECORD 响应，不再 13s EOF）|
| P1-2 | `V2_SEQ CTRL_UNREGISTER_WAIT ms=1500 confirmed=true` |

### 12.3 明确失败点一：P0-1 的「RECORD 后多流 SETUP」被车机 100% 拒绝
```
CARPLAY_RESPONSE op=STREAM_SETUP status=400 cseq=18 encrypted=true
CONTROL_RESP_STATUS status=400 op=STREAM_SETUP bodyLen=0 bodyHex=EMPTY
V2_SEQ POST_RECORD_STREAM_SETUP_FAILED ... fallback=per_stream
```
计数：`POST_RECORD_STREAM_SETUP_OK = 0`、`FAILED = 21`、`STREAM_SETUP 400` 共 **39** 次。
逐流回退：**110 屏幕 OK**、101 AltAudio 偶发 OK、**100 MainAudio / 102 MainHighAudio 恒 400**。
⇒ Buick Info3.6 不接受「一次 SETUP 带 4 条 streams」，也不接受新 `streamConnectionID` 的音频流声明。
失败后连发 5 个 SETUP（1 多流 + 4 逐流）会占用控制通道并可能覆盖已建的 110 流，**应删除逐流回退**。

### 12.4 明确失败点二：~10.1s 看门狗（疑为历史遗留）
- `AUDIO_SEND_DIAG g=100 … sentFrames=0 packets=0 gateEntered=false` —— 音频**一帧未发**，`pcmBytes` 恒 0。
- `CARPLAY_EVENT_FEEDBACK_SENT count=1 channel=EVENT body_bytes=0`；`CONTROL_FEEDBACK_SENT` 每会话仅 1 次，
  `CONTROL_FEEDBACK_FAILED = 0`（不是抛异常，更像是阻塞在控制通道锁 / 等响应）。
- 车机 `CARPLAY_UDP_KEEPALIVE advertised=true interval_ms=5000`，历史 V1.7.98 源码注释已写明
  「RECORD ACK 后恒定 10.5s teardown」「连续数个周期收不到 feedback 即判对端失效」；
  11-06 历史日志亦有 `RECOVERY_WAIT p95_ms=13043`。
⇒ ~~10s 级断连高度可能是历史遗留看门狗，不是 V2 新引入~~ **【本推断已被 §13 的 A/B 证伪：10.1s 断连就是 V2 引入的，关闭 V2 后完全消失】**

### 12.5 触摸与 ctrl 回连（仍未解决）
- `CARPLAY_INBOUND_ANY = 0`、`HID_REPORT_ARRIVED = 0`、`TOUCH_FORWARDED = 0` ⇒ 固件反向控制通道门 `[+0x466]` 仍未开，§9 的结论依然成立。
- `CTRL_INVITATION_ACCEPTED = 0`、`CTRL_TCP_ACCEPT = 0`；`CTRL_ANNOUNCE name=CarPlayAPK … manual=`（**用户未填「手机蓝牙标识」**），
  `CTRL_SERVICE_LIVE name=CarPlayAPK (2) port=0` + `CTRL_SERVICE_TXT n=0`（APP 侧 `onServiceRegistered` 拿到的 NsdServiceInfo 是空对象；
  但系统 `serviceDiscovery[MdnsAdvertiser]` 显示 10 键 TXT 与 port=37669 实际广播正常）。
  `confirmed=true` 仍出现 `(2)` ⇒ 1500ms 等待**没能消除**改名，冲突源不止注销时序。
- `AUTO_WIFI_SKIPPED reason=NO_SAVED_CREDENTIALS` 反复（未选手动 WLAN），但双方同在 `192.168.5.0/24`，**不是网络不通**。

### 12.6 下一步（按优先级）
1. **A/B 定性**：`carplayapk-debug.xml` 置 `session_seq_v2=false`，重启 APP 抓 30s；若 10.1s 断连仍恒定 ⇒ 与 V2 无关，专攻 §12.4。
2. **P0-1 改造**：多流 SETUP 失败**立即停止**（删逐流回退）；只补 `type=100`；**复用 `l2()` 的 `streamConnectionID`**（不新 `E1()`）；不再重复声明已建的 110。
3. **feedback 排查**：查 10s 内为何只发 1 次（控制通道锁 / `R1` 阻塞 / `this.c0` 判定），这是看门狗最直接的对手。
4. **P1-2**：用户在 APP 填「手机蓝牙标识」使实例名与车机记录一致；修 `onServiceRegistered` 读空 NsdServiceInfo 的问题；`(2)` 冲突需另找根因（可能 StartEarly 与正式注册并发）。

## 13. A/B 定案 + V1.8.30 麦克风去除（2026-11-08 22:44–23:00）

### 13.1 A/B 方法
同一台 OPPO PKJ110 + 同一台 Buick myBuick GS 车机，仅把 `carplayapk-debug.xml` 的
`session_seq_v2` 从「缺省(true)」改为 `false`，`am force-stop` + `am start` 触发自动连接，
`adb logcat -G 16M` + `-c` + `-d` 抓 **40 秒**窗口（`logs/v1.8.29-ab-v2off.log`，3.8 万行）。
其余偏好保持 `debug_mode=true / auto_connect_launch=true / log_enabled=true`。

### 13.2 A/B 结果（**纠正 §12.4 的误判**）

| 判据（40s 窗口） | V2 开（§12，8 分钟折算） | **V2 关** |
|---|---|---|
| `CARPLAY_SETUP_KEYS` | n=14（含 ekey/eiv/et/macAddress） | **n=10**（回退旧集合） |
| `V2_SEQ` | 190 | **1**（仅 `RECORD_ACKNOWLEDGED`，该埋点无条件打印） |
| `POST_RECORD_STREAM_SETUP` | 83 | **0** |
| `status=400 op=STREAM_SETUP` | 47 | **0** |
| `SESSION_FAILURE` / `Broken pipe` | 20 / 每 ~10.1s | **0 / 0** |
| `RECOVERY_WAIT` / `Recovering N/3` | 持续循环 | **0** |
| `CONTROL_FEEDBACK_SENT` | 每会话仅 1 次 | **8 次 / 40s**，另有 `CARPLAY_EVENT_FEEDBACK_SENT` 6 次、`CARPLAY_UDP_KEEPALIVE_SENT` 6 次 |
| 用户观感 | 连接失败、画面反复中断 | **投屏恢复** |

⇒ **10.1s 断连是 V2 引入的**（P0-1 的多流 SETUP 400 + 逐流回退连发 5 个 SETUP 占控制通道，
feedback 被挤到每会话 1 次，触发车机 5s keepalive 看门狗）。§12.4 把它归为「历史遗留看门狗」**是错的**：
历史确有 10.5s teardown 记录，但本次对照证明关掉 V2 后同一车机、同一网络下 40 秒零断连。

### 13.3 新问题与修复（用户反馈「车机出现手机麦克风采集的声音」）
**根因（真机日志坐实）**：
```
AUDIO_CAPTURE_ENTER started=false perm=0
AUDIO_CAPTURE_STARTED source=MIC rate=44100 channels=2 projection=false
AUDIO_CAPTURE_READ last=1920 frames=... output_ready=true
```
`WirelessCarPlayService.startPcmCapture()` 的 V1.7.99 注释已自述「先接麦克风源，用于验证
PCM→编码→车机出声链路，待验证通过后替换音源」。没有 `MediaProjection` 授权时回退到
`MediaRecorder.AudioSource.MIC`，采集到的环境声经 `AirPlaySession.t1(MEDIA, pcm, frames)`
作为 **MEDIA 主流**发给车机，车机当主音频播放 ⇒ 用户听到回声/环境声。

**修复（V1.8.30 / 367）**：
1. `WirelessCarPlayService.startPcmCapture()`：`"MIC".equals(source)` 分支不再 `setAudioSource(MIC)`，
   改为记录 `AUDIO_CAPTURE_SKIPPED reason=MIC_SOURCE_DISABLED projection=false`、复位
   `PCM_CAPTURE_STARTED` 并 return。**保留** `MediaProjection + AudioPlaybackCapture` 的
   `PLAYBACK_CAPTURE` 路径（CarPlay 语义上正确的手机媒体音频），用户后续授权仍可出声。
2. `ControlChannel.SeqV2Enabled()` **默认值 true → false**：A/B 定案后防止新装 / 清数据 / 删偏好
   时重新启用有害序列。显式写 `session_seq_v2=true` 仍可做逐项 A/B。
3. 版本号 1.8.29/366 → **1.8.30/367**。

### 13.4 1.8.30 真机验证（已安装）
- 安装后 28s：`AUDIO_CAPTURE_SKIPPED MIC_SOURCE_DISABLED` 8 次、
  **`AUDIO_CAPTURE_STARTED source=MIC` = 0、`AUDIO_CAPTURE_READ` = 0**、`CARPLAY_SETUP_KEYS n=10`、
  `CARPLAY_RECORD_ACKNOWLEDGED` 正常；1 次 `EVENT_CHANNEL_SocketException`（重启瞬间旧会话被杀）。
- 再观察 35s（不干预 APP）：**`SESSION_FAILURE=0`、`VIDEO_SEND_SocketException=0`、
  `status=400 op=STREAM_SETUP=0`、`V2_SEQ=0`、feedback 27 次**，会话稳定、投屏保持。
- 日志：`logs/v1.8.30-verify.log`、`logs/v1.8.30-stability.log`。

### 13.5 状态与后续
- 设备已装 **1.8.30/367**；偏好还原为原始三键（无 `session_seq_v2` 键，新代码默认 false，行为不变）；
  设备与工作区临时 xml 已清理。
- 产物 `dist/Carplay-V1.8.30-debug.apk`，六项核验 PASS，dex 锚点 `AUDIO_CAPTURE_SKIPPED` 命中 3 次。
- **V2 六项代码仍在，只是默认关闭**。若要精确定位是哪一项致害，建议按
  `P0-1 多流 SETUP` → `P0-2 ekey/eiv` → 其余 逐项加独立子开关做 A/B；当前不要整体重新打开。
- 触摸 / 反向控制通道门仍未解决（`CARPLAY_INBOUND_ANY=0`），见 §9/§12.5。

## 14. 触摸无反应：稳定会话下的复核与结论修正（2026-11-08 23:00，V1.8.30 / 367）

> 与前几轮最大差别：**这次是在投屏稳定、会话不再 10s 重建的前提下取的证**，
> 此前所有触摸抓包都发生在断连循环中，结论可能带偏差。设备 1.8.30，`session_seq_v2` 默认 false。

### 14.1 手机侧 HID 链路已全部打通（应用内日志，15:00:02–15:00:09）
| 步骤 | 证据 |
|---|---|
| 车机 `/info` 声明 | `CARPLAY_RESOURCE_STATE source=INITIAL_INFO screen_entity=2`、`CARPLAY_SCREEN_OWNER_PHONE=false`、`CARPLAY_INFO_MODES value={appStates=[...], resources=[{resourceID=1,...},{resourceID=2,...}]}` |
| APK 本地声明持屏 | `CARPLAY_RESOURCE_STATE source=LOCAL_CLAIM screen_entity=1`、`CARPLAY_SCREEN_OWNER_PHONE=true` |
| 所有权转移 | `CARPLAY_SCREEN_MODES_PAYLOAD keys=[params, type] body={params={resources=[{entity=1, resourceID=1}]}, type=modesChanged}`，`CARPLAY_SCREEN_MODES_SENT` **2 次**，均为 **entity=1**（V2 关闭也不影响，因 LOCAL_CLAIM 后 `this.d.a=1`）|
| HID 能力 | `HID_CAPABILITIES devices=3 touch=1 back=1`，主屏 UUID `e5f7a68d-...`，`display_match_main=true`；`HID_TOUCH_UNSUPPORTED index=1 reason=DESCRIPTOR` 只是第 2 个描述符不支持，主触摸描述符正常 |
| 启动 HID | `HID_SWEEP mode=1` → `HID_GATE_OPEN cnt=2 mode=1 devices=2` → 3 条 `hidSetInputMode`（uuid 3/4/2）|
| **车机确认** | **`HID_INPUT_MODE_ACCEPTED slot=0/1/2 status=200 carrier=CONTROL mode=1`（3/3 全 200）** |
| 结果 | `CARPLAY_INBOUND_ANY=0`、`CARPLAY_INBOUND_REQUEST=0`、`INBOUND_REQ=0`、`HID_REPORT_ARRIVED=0`、`TOUCH_FORWARDED=0` |

⇒ 手机→车机方向无任何缺口；**车机→手机方向一个字节都没有**。

### 14.2 纠正 §9 / §10 的三条旧推断（字节码复核）
1. **`X-Apple-Durations` 不是开门必要条件**：`0x41a7c call 0x483b0` 位于
   `HTTPStatus _requestProcessRecord(AirPlayReceiverConnectionRef, HTTPMessageRef)`（**RECORD 处理函数**，
   字符串 @0x417c4），同函数 `0x4185a lea rdx,'X-Apple-Durations'` → `0x418a7 call 0x73770`；
   但 `0x418ac test eax,eax; jne 0x41a6e` —— **头解析失败也直接跳到 `0x41a6e → call 0x483b0`**。
   §10「APK 缺该头导致 startSession 不执行」**证据不足，撤回**。规范 PDF 全文亦无 `durations` 字样，
   该头是 Cinemo/AirPlay 私有扩展且为可选。
2. **音频流状态不是 `[+0x466]` 的前置门**：`0x483e5..0x484a6` 对 `[+0x4a8]==0x66/0x64`、
   `[+0xdc0]==0x65` 的三次检查都是「满足才 `call 0xc780` 启流、不满足就跳到下一段」，
   **不会跳出成功路径**；`[+0x1cd8]<0` 也只是跳过 `_ScreenStart`。真正会跳过 `0x484f1` 的只有
   `0x485a0` / `0x48c30` / `0x3d670("startSession")` 三处返回非 0。
3. **RECORD 200 ⇒ `[+0x466]` 大概率已置位**：上述失败分支 `0x48581/0x48584` 统一
   `mov ebp,r15d → call 0x48f80 → mov eax,ebp; ret`，返回**非零错误码**，RECORD 就不可能回 200。
   真机稳定会话有 `CARPLAY_RECORD_ACKNOWLEDGED` ⇒ `0x483b0` 返回 0 ⇒ `0x484f1 mov [rbx+0x466],1` 已执行。
   ⇒ **「固件反向门没开」不再是首要怀疑**，§9 的因果链需改写。

### 14.3 发送门本身只有一个条件
`0x45100`：`mov eax,0xffffe5cb` → `cmp byte ptr [rdi+0x466],0; je 0x45150`（返回错误）；
开门后 `mov rdi,[rdi+0x468]` → `call 0x6cbb0` 发送。`[+0x468]` 由
`0x440ea call 0x6daa0` 在会话对象初始化时创建（NULL 则 `je 0x44280` 报错），
析构时 `0x4b408..0x4b419` 释放并清零。**没有第二道业务门**。
⇒ 9 处 `call 0x45100`（含 `hidSendReport` @0x4ab62）是「发送能力」，不是「触发源」。

### 14.4 真正的缺口：谁产生反向命令 / 会话登记
- `call 0x45100` 的 9 个调用点只负责构造命令；**触发源在 HMI / CarPlay 会话登记层**，
  静态字节码未找到 `AddCarPlayClient`(0x178d0) 的直接 call（疑为虚表/间接调用）。
- 真机 `CTRL_INVITATION_ACCEPTED=0`、`CTRL_TCP_ACCEPT=0`，`CTRL_SERVICE_LIVE name=CarPlayAPK (2)`、
  `CTRL_ANNOUNCE ... manual=`（**未填「手机蓝牙标识」**）。固件 `SendConnect` @0x17e30
  按 `(蓝牙名, 第二标识串)` 双串匹配，实例名带 `(2)` 且不是用户蓝牙名 ⇒ 匹配可能失败 ⇒
  车机不回连 ctrl ⇒ 会话未登记为 CarPlay ⇒ HMI 触摸不桥接到 `hidSendReport`。
- 入站回调槽（`modesChanged`→`[r14+0x60]` @0x443fd、`hidSetInputMode`→`[+0x48]` @0x4454e、
  `requestUI`→`[+0x68]` @0x44491）若为 NULL，固件只转发上层、不解释 mode —— **200 ≠ HMI 启用触摸**（§9 仍成立）。
- `display_confirmed=false` 在源码 6 处全是**写死字面量**，按项目铁律**不能当真实状态判据**。

### 14.5 下一步（按成本排序，先验证再改码）
1. **【最低成本，强烈建议先做】填「手机蓝牙标识」**：设置页填本机蓝牙名/地址，使
   `_carplay-ctrl` 实例名与车机配对记录一致。判据：`CTRL_INVITATION_ACCEPTED ≥1`、
   `CTRL_TCP_ACCEPT ≥1`、`CARPLAY_INBOUND_ANY ≥1`（任意入站即可，不必先等 HID）。
2. **消除 `(2)` 改名**：`CTRL_UNREGISTER_WAIT ms=… confirmed=true` 仍出现 `(2)`，说明冲突源不止注销时序，
   疑 `StartEarly` 与正式注册并发。需查 `CarPlayCtrlAdvertiser` 两条注册路径；或改用固定唯一实例名 +
   让固件按 TXT `id/deviceid` 匹配。
3. **整机抓包定性（决定性）**：在车机屏幕上点按，同时抓 `192.168.5.1:5001`（eventPort）与
   车机→手机方向的 TCP。若车机**根本没发**任何反向 RTSP ⇒ 问题在会话登记/HMI；
   若发了但 APK `EventChannel.z()` 读到 `raw_bytes=0` ⇒ 问题在通道方向/端口。
   静态逆向无法证明真机某次会话 `[+0x466]`/`[+0x468]` 的实际状态。
4. 不要再扫 `hidInputMode` / 改 HID 描述符 / 回到顶层键 / 伪造 startSession 命令（案例 9~13 已证伪）；
   也不要为触摸重新打开 V2（A/B 已证明会 10s 断连）。

### 14.6 边界
本轮**只做分析，未改业务代码、未动设备状态**。字节码能证明门的位置与条件，
但无法证明真机某次会话的门状态与 HMI 回调槽是否为 NULL；odex 未反编译，`AddCarPlayClient`
第二标识串含义未定。日志：`logs/v1.8.30-verify.log`、`logs/v1.8.30-stability.log`、
设备 `files/connection-log-0.txt`。

## 15. 反向通道（Accessory control channel）触摸无反应：定案与 APK 侧方案（2026-11-08 23:30，V1.8.31 / 368）

> 用户要求「结合固件 / APK / 规范，分析反向通道触摸无反应如何在 APK 中解决」。
> **本轮只分析 + 落盘，零业务代码改动**；V1.8.31 的改动是上一轮「步骤 1/2」的产物。

### 15.1 步骤 1/2 已落地并验证（V1.8.31 / 368）
- **步骤 1 发现的实质 bug**：设置页「手机蓝牙标识」写 `phone_bluetooth_name`（`MainActivity:1376`），
  但 `ControlChannel.ManualName()` 读的是历史键 `bluetooth_name`（`ControlChannel:317`）⇒ 用户填的蓝牙名
  **永远读不到**，`ServiceName()` 一直回退 `CarPlayAPK`。修复：优先读 `phone_bluetooth_name`、旧键兼容回退；
  `SaveName()` 双写两键。
- **步骤 2 `(2)` 真因钉死**：`_carplay-ctrl` 有 `StartEarly()` / `PairingSession.Q1()` /
  `WifiNetworkMonitor` 三条独立注册路径，`StopEarly()` 只关 early。V1.8.31 加全局 `REG_LOCK`
  注册仲裁（注册前关闭其它活跃实例、注册/注销共用静态锁、实例编号埋点）。
  真机日志 `CTRL_REG_CONFLICT_CLOSE new=2 prev=1` **直接证明并发双实例**就是改名根因。
- **真机结果（55s，`logs/v1.8.31-ctrl-verify.log`）**：
  `CTRL_ANNOUNCE name=OPPO_X8U svc=OPPO_X8U manual=OPPO_X8U`、
  `CTRL_REG_LIVE instance=2 expected=OPPO_X8U actual=OPPO_X8U`、
  **`CarPlayAPK (2)` 0 次 / `CTRL_SERVICE_NAME_CONFLICT` 0 次**；
  SETUP `name_source=USER_BLUETOOTH_NAME`、`RECORD_ACKNOWLEDGED`、`SESSION_FAILURE=0`。
  设备系统蓝牙名/地址 `OPPO_X8U` / `E0:42:6D:B6:D5:78` 与手填值**完全一致**（`settings get secure bluetooth_*`）。
- **但判据仍全是 0**：`V160_CTRL_TCP_ACCEPT=0`、`CTRL_INVITATION_ACCEPTED=0`、
  `CARPLAY_INBOUND_ANY=0`、`HID_REPORT_ARRIVED=0`、`TOUCH_FORWARDED=0`。
  ⇒ **§14.4「实例名匹配 / `(2)` 导致车机不回连」的推断被证伪（至少不是充分条件），必须收窄。**

### 15.2 新的决定性证据：车机从未建立任何到手机的 TCP 连接
`/proc/net/tcp` + `/proc/net/tcp6` 快照（`logs/v1.8.31-procnet.txt`）：
- 手机 `192.168.5.100:41095`（ctrl 注册端口）状态 **0A LISTEN，无对应 ESTABLISHED**。
- 3 条 ESTABLISHED 全是**手机随机高端口 → 车机 192.168.5.1**：
  `:8CBC→:1389`(5001 eventPort)、`:9BF2→:1B58`(7000 控制)、`:AF56→:178E`(6030)。
- 车机 `/info` 的 `CARPLAY_INFO_BT_IDS value=[10:E8:A7:13:AD:5B]`（只有车机自己）、
  `CAPS ... iap_channel=false`。
⇒ 目前只有「手机→车机」的媒体会话，**没有任何「车机→手机」的 RTSP 通道**。

### 15.3 规范依据（docs/Apple-CarPlay-Specification.pdf，PDF 物理页）
| 页 | 条款 | 原文要点 |
|---|---|---|
| 45 (p425) | §23.3.1.1 / Table 23-20 | Apple device 广播 `_carplay-ctrl._tcp`；**accessory 必须用该服务发现并发起 CarPlay 会话**；实例名 = user-visible name；TXT 只有 `id`(Apple device 蓝牙 MAC) 与 `srcvers`(x.y.z) |
| 46 (p426) | §23.3.1.1.1 / Table 23-21 | 车机发 `GET /ctrl-int/1/connect`，**所有请求必须带 `AirPlay-Receiver-Device-ID`**（车机主 MAC 48-bit 整数） |
| 48 (p428) | §23.3.2 会话生命周期 | 手机在发现兼容 accessory 后主动连车机 TCP 端口，经 MFi-SAP → `/info` → 初始 SETUP → 每流建连 → RECORD → 会话后流 SETUP |
| 61/62 (p441) | Table 23-36/37 | 初始 SETUP 请求 `streams` 可省（只设控制流）；响应给 `eventPort`（TCP/UDP port for events）、`keepAlivePort` |
| 65 (p445) | **Table 23-47 Channels** | **Controller control channel**：device→accessory，TCP 5000，RTSP；**Accessory control channel**：**accessory→device，dynamic TCP，RTSP** |
| 97 (p477) | §23.3.7 Commands | 命令走 `/command` on control stream；**手机命令走 Controller channel，车机命令走 Accessory channel**；每条命令必须有 HTTP 响应 |
| 101 (p481) | §23.3.7.7 / Table 23-74 | `hidSendReport` Sender=**Accessory**，顶层 `hidReport` + `uuid` |

⇒ **规范里没有任何「手机主动连车机后，车机会在同一条 TCP 上回发命令」的条款**；
车机命令必须有独立的 accessory→device 通道。当前 APK 只有一条手机→车机的 eventPort 连接，
`eventPort` 是**车机返回的端口**（Table 23-37），不能充当 Accessory control channel。

### 15.4 固件侧：反向通道的门与触发源（capstone，VA=文件偏移）
- **发送门**：`0x45100 cmp byte ptr [rdi+0x466],0` → 0 直接返回 `0xffffe5cb`；
  9 条车机→手机命令（含 `hidSendReport` @0x4ab62）都经它，payload 由 `[rdi+0x468]` 经 `0x6cbb0` 发出。
  `[+0x466]` 唯一写入 `0x484f1`（`0x483b0` 内，`call 0x3d670("startSession")` 返回 0 才置 1）；
  RECORD 200 ⇒ 该门**大概率已开**（§14.2 已论证）。**门开只是「允许发」，不是「有人发」。**
- **触发源 / 会话登记（本轮新增决定性证据）**：
  - `SendConnect` 函数 @0x17e10，唯一调用者 **@0x14017**；调用者日志格式
    **`'device_id=%s, service_name=%s'` @0x13fa8** ⇒ 入参是 `(device_id, service_name)`。
  - `SendConnect` 遍历 `[this+0x9e0]` 的客户端列表（每项 0x30 字节），
    `0x17f31/0x17f41 call 0xbcf0` 做双串比较，命中才 `0x17f65` 跳出并
    `0x17fdf mov rdi,[rax+0x9f8]; 0x17fe6 mov rsi,[rbx+0x28]; call 0xe400` 发送 connect；
    **未命中 `0x17f72 cmp rbx,rax; je 0x18040` 返回 0xb，不发**。
  - 列表由 `AddCarPlayClient` @0x178b0 填充（6 字节 MAC 按 `%02X:%02X:...` 格式化 @0x17917）；
    唯一两个调用点 **@0xe272 / @0xe338**，都在 `IosControlClientCallback` 里，
    事件日志 **`kCarPlayControlClientEvent_AddOrUpdateController` @0xe246**（另一分支是 `_Stopped`）。
  ⇒ **车机的 CarPlay 客户端列表由 iOS Control Client（mDNS `_carplay-ctrl` 发现）的
  AddOrUpdateController 事件登记；`SendConnect` 命中 `(device_id, service_name)` 才发 connect。**
- 与真机对齐：`CTRL_TCP_ACCEPT=0` + ctrl 端口零入站 ⇒ **车机这次既没登记成功，也没发 connect**，
  所以 Accessory control channel 从未建立，`hidSendReport` 根本没有可写的通道。

### 15.5 定案（替换 §14.4 的主线）
**触摸无反应的第一缺口不是 HID、不是 `modesChanged`/422、不是实例名/`(2)`、也不是固件 `[+0x466]` 门，
而是「会话发起方向 / CarPlay 会话登记」**：APK 用「手动 WLAN + 主动连车机 7000」绕过规范要求的
`_carplay-ctrl` → `GET /ctrl-int/1/connect` 握手，车机只把它当普通 AirPlay 媒体会话（视频正常），
没有把手机登记为 CarPlay controller，也没有建立车机→手机的 RTSP 通道。
`iap_channel=false` 与 `bluetoothIDs` 只含车机自身，也支持「车机侧缺少本次手机的登记」。
这同时解释了「换一台车机后同 APK 触摸正常」：那台车机的登记条件更宽松。

### 15.6 APK 侧解决方案（按优先级，均可开关、可 A/B）
| 优先级 | 动作 | 代码锚点 / 依据 | 验证判据 |
|---|---|---|---|
| **P0-1** | **把「等 ctrl connect」作为主路径**：`CarPlayAutoReconnectService.ensure()` 先确保蓝牙链路 + ctrl 广播，等 `MarkCtrlInvitation()` → `onCtrlInvitation()` 再连 7000；保留「N 秒超时后主动连」兜底，避免丢掉已稳定的投屏 | §23.3.1.1；`CarPlayCtrlAdvertiser.StartEarly()` 的 `b()` 回调、`onCtrlInvitation()` | `V160_CTRL_TCP_ACCEPT≥1`、`CTRL_INVITATION_ACCEPTED≥1` |
| **P0-2** | **TXT 严格按 Table 23-20 校验并回读自证**：至少保证 `id`=手机蓝牙 MAC（大写冒号）、`srcvers`=x.y.z；用 `NsdManager.resolveService` 解析自己注册的服务并打印 attributes（当前 `CTRL_SERVICE_TXT n=0` 是回调空对象，无法证明车机实际收到的 TXT） | `ControlChannel.FillTxt()`、`CarPlayCtrlAdvertiser.u()/z()` | `CTRL_RESOLVE_TXT keys=id,srcvers values_masked=true` |
| **P0-3** | **标识闭环校验**：`ServiceName()` 与系统 `BluetoothAdapter.getName()`（可读、无需 LOCAL_MAC_ADDRESS）比对并打 `CTRL_IDENTITY_MATCH`；`id`/`deviceid`/`btaddr`/SETUP `deviceID` 统一同一 MAC；避免用户手填错导致 `SendConnect` 双串不命中 | `ControlChannel.ManualName()/ServiceName()`、`BluetoothAdapterResolver.b()`、`AirPlaySession.t0()` | `CTRL_IDENTITY_MATCH name=true mac_format=true` |
| **P1-1** | **补蓝牙 iAP2 登记触发**：当前 `iap_channel=false`。核查 `Iap2Bootstrap`/`Iap2WirelessSetup` 是否在自动连接路径真正建立蓝牙会话并发送手机蓝牙标识（19982 等），使车机 `IosControlClientCallback(AddOrUpdateController)` 登记成功 | 固件 `0xe272/0xe338`、`0x178b0`；APK `Iap2*.java` | `CARPLAY_INFO_BT_IDS` 仍只含车机，但 `V160_CTRL_TCP_ACCEPT` 变正 |
| **P1-2** | **保持反向读能力（已具备，无需改）**：`EventChannel` 常驻读循环 `setSoTimeout(0)` + `CARPLAY_INBOUND_ANY`（`w()`/`z()` 两条路径）+ `s1()` 对入站 `/command` 回 200 已齐备；只要车机把命令发到已建立的通道就能接住 | `EventChannel:914/1043/1182`、`AirPlaySession.s1():2815` | `CARPLAY_INBOUND_ANY≥1` → `HID_REPORT_ARRIVED≥1` |
| **P2** | 若车机始终不回连：车机侧日志 / 整车抓包确认 `+0x9e0` 列表是否为空、`[+0x468]` 是否 NULL、`SetCallbacks` 槽是否注册；纯 APK 可能不足 | §10.5 / §14.6 | 车机侧日志出现 `SendConnect sending:` |

**判据必须按顺序**：`V160_CTRL_TCP_ACCEPT` → `CTRL_INVITATION_ACCEPTED` → `CARPLAY_INBOUND_ANY`
→ `HID_REPORT_ARRIVED` → `TOUCH_FORWARDED`。前两级为 0 时，任何 HID 侧改动都不会生效。

**明确不要做**：①重开 `session_seq_v2`（A/B 已证 10.1s 断连）；②再扫 `hidInputMode`/改 HID 描述符/
回顶层键（案例 9~13 已证伪）；③伪造 `startSession` 或新增 PTTH/reverse（固件无此实现、规范无此机制）；
④一次补 22 个 TXT 键（V1.7.70 回归史）；⑤把 `eventPort` 当反向通道（它是车机返回的端口，Table 23-37）。

### 15.7 边界
- `+0x9e0` 列表在真机某次会话是否为空、`[+0x468]` 是否为 NULL、HMI `SetCallbacks` 槽是否注册，
  **静态字节码无法证明**；需车机侧日志或整车抓包（本轮抓包尝试被用户改题中断，未完成）。
- 手机无 root（`CapEff=0`）、无 tcpdump、车机 5555 未开放、PC 无 WiFi 网卡 ⇒ 真 pcap 需
  VpnService 类抓包工具或车机侧权限；本轮用 `/proc/net/tcp` 连接方向做等效定性。
- 本轮**只做分析与落盘**，未改业务代码、未出包、未改设备偏好。

## 16. V1.8.32 A/B 包：P0-1/P0-2/P0-3 真机验证（2026-11-09 00:11，V1.8.32 / 369）

> 用户要求「按 P0-1/P0-2/P0-3 出 V1.8.32 A/B 包（默认关、开关可回滚）去真车验证 ctrl 回连」。
> 本轮**改了业务代码、出了包、做了真机 A/B**，结论明确。

### 16.1 落地改动（3 文件 + 1 新文件 + 版本号，均未提交 git）
- `app/build.gradle.kts`：1.8.31/368 → **1.8.32 / 369**。
- `secured/ControlChannel.java`：
  - 新增调试开关 `CtrlWaitInvitationEnabled()`（读 `carplayapk-debug.xml` 的 `ctrl_wait_invitation`，**默认 false**）。
  - `FillTxt()`（TXT 构造处）新增 **P0-2 校验**：`id/deviceid/btaddr` 必须 = `n.a` 规范化后的本机蓝牙 MAC（大写冒号）、
    `srcvers` 必填 x.y.z；校验结果打 `CTRL_TXT_VALIDATE keys=... values_masked=true`。
  - `ManualName()/ServiceName()` 已读 `phone_bluetooth_name`；新增 **P0-3 标识闭环** `CTRL_IDENTITY_MATCH`：
    `ServiceName()` 与 `BluetoothAdapter.getName()`（无需特权，可读）比对 + `id/deviceid/btaddr` 与 SETUP `deviceID` 同 MAC。
- `prototype/CarPlayCtrlAdvertiser.java`：
  - 注册成功后用 `NsdManager.resolveService` **回读自己服务 attributes** 打印 `CTRL_RESOLVE_TXT`（P0-2 自证，证明车机实际可见内容）。
  - 受 `CtrlWaitInvitationEnabled()` 统一门控（开关关则跳过校验与自证，行为完全回退）。
- `prototype/CarPlayAutoReconnectService.java`：
  - **P0-1 主路径**：`handleEnsure()` 先 `StartEarly` 确保 ctrl 广播，再 `scheduleInvitationTimeout()` 给 8s 等
    `MarkCtrlInvitation()` → `onCtrlInvitation()`（已有 `StartEarly` 回调）再连 7000；**8s 超时后主动连兜底**，
    不丢已稳定投屏。日志 `AUTO_WAIT_INVITATION_ENABLED` / `AUTO_WAIT_INVITATION_TIMEOUT` / `AUTO_WAIT_INVITATION_SKIP_FALLBACK`。
- `AndroidManifest.xml` + 新文件 `prototype/DebugSwitchReceiver.java`：
  - 调试开关广播接收器，action `io.carplay.app.action.DEBUG_SET_CTRL_WAIT`、extra `enabled=true|false`
    （写 `carplayapk-debug.xml` 的 `ctrl_wait_invitation`）。**`exported=true`**（action 为私有命名空间，调试开关风险可控），
    以便普通 `adb shell am broadcast` 可切换并回滚。

### 16.2 编译与核验
- `gradlew exportApk --offline` BUILD SUCCESSFUL；`verify_built_apk.sh` **六项 PASS**；dex 锚点全命中：
  `AUTO_WAIT_INVITATION_ENABLED`、`CTRL_RESOLVE_TXT`、`CTRL_IDENTITY_MATCH`、`CTRL_TXT_VALIDATE`、
  `DebugSwitchReceiver`、`io.carplay.app.action.DEBUG_SET_CTRL_WAIT` 等。
- **踩坑 2 次**：Manifest 注释里写了 `--ez enabled ...`（含 `--` 序列），XML 注释不允许 `--` ⇒ 合并失败。
  修正注释后重编通过。记铁律：Manifest/XML 注释不得含 `--`。

### 16.3 真机 A/B 结果（OPPO PKJ110，手机 WiFi `myBuick GS` = 192.168.5.100，车机 192.168.5.1）
**基线（默认关，开关缺失=关）**：`logs/v1.8.32-baseline.log`
- `CTRL_ANNOUNCE name=OPPO_X8U svc=OPPO_X8U manual=OPPO_X8U`、`RECORD_ACKNOWLEDGED`、`SESSION_FAILURE=0`（投屏稳定）。
- 新增调试日志全 0（`AUTO_WAIT_INVITATION_ENABLED=0`/`CTRL_RESOLVE_TXT=0`/`CTRL_IDENTITY_MATCH=0`/`CTRL_TXT_VALIDATE=0`）。
- `V160_CTRL_TCP_ACCEPT=0`、`AUTO_CTRL_INVITATION_RECEIVED=0` —— 与历史一致。

**开关开启（显式组件广播 `-n io.carplay.app/io.carplay.app.prototype.DebugSwitchReceiver -a ...DEBUG_SET_CTRL_WAIT --ez enabled true`）**：`logs/v1.8.32-wait-on.log`
- **P0-1 主路径生效**：`AUTO_WAIT_INVITATION_ENABLED ×3`；8s 兜底因会话已建立而 `AUTO_WAIT_INVITATION_SKIP_FALLBACK`。
- **P0-2 自证（决定性）**：`CTRL_RESOLVE_TXT` 显示 TXT 完全规范——
  `id/deviceid/btaddr=E0:42:6D:B6:D5:78`、`srcvers=27.2.0`、`name=OPPO_X8U`、`model=iPhone15,3`、10 键齐。
  ⇒ **车机通过 mDNS 实际看到的 TXT 正是正确内容，手机侧暴露无缺。**
- **P0-3 标识闭环**：`CTRL_IDENTITY_MATCH sys_name=OPPO_X8U manual=OPPO_X8U name_match=true`；
  MAC/实例名/系统蓝牙名三处一致，双串匹配的「手机侧」字符串完全正确。
- **但判据仍全 0**：`V160_CTRL_TCP_ACCEPT=0`、`AUTO_CTRL_INVITATION_RECEIVED=0`、
  `CARPLAY_INBOUND_ANY=0`、`HID_REPORT_ARRIVED=0`、`TOUCH_FORWARDED=0`。
  ⇒ **手机侧已做到位（TXT 规范、标识闭环、等待主路径激活），车机依旧不回连 ctrl。**

**回滚验证（`--ez enabled false`）**：`logs/v1.8.32-rollback.log`
- `carplayapk-debug.xml` 写回 `ctrl_wait_invitation=false`；新调试日志全部归零、`RECORD_ACKNOWLEDGED=2`、`SESSION_FAILURE=0`
  —— **完全回到基线行为，开关可安全回滚，无副作用。**

### 16.4 定案升级（替换 §15.5 主线的「APK 侧可能够」假设）
- 至此 **P0-1/P0-2/P0-3 全部验证：手机侧已无缺口**。TXT 规范、标识与系统蓝牙名/手填值完全一致、等待主路径已激活，
  但 `V160_CTRL_TCP_ACCEPT` 仍 0 ⇒ 问题**不在手机侧暴露的标识、不在 `(2)` 改名、不在等待时序**，
  而**在车机侧是否把手机登记为 CarPlay controller**（固件 `SendConnect` @0x17e10 的 `+0x9e0` 列表命中）。
- `+0x9e0` 列表的填充依赖 `IosControlClientCallback(AddOrUpdateController)` @0xe272/0xe338 → `AddCarPlayClient` @0x178b0，
  而该回调由 **iOS Control Client（mDNS `_carplay-ctrl` 发现）触发**。本 APK 当前是「手动 WLAN + 主动连 7000」模式，
  **iAP2 蓝牙登记链路未真正建立**（`CAPS iap_channel=false`，§15.2），车机没有触发 AddOrUpdateController ⇒ 列表为空 ⇒ SendConnect 返回 0xb 不发。
- **下一缺口 = P1-1（蓝牙 iAP2 登记触发）**，这是纯 APK 能补的最后一段；若 P1-1 后仍不回连，则属车机固件登记条件（需整车侧日志/抓包，§15.7 边界）。

### 16.5 判据归零顺序已确认
`V160_CTRL_TCP_ACCEPT(0) → CTRL_INVITATION_ACCEPTED(0) → CARPLAY_INBOUND_ANY(0) → HID_REPORT_ARRIVED(0) → TOUCH_FORWARDED(0)`。
前两级为 0 时 HID 侧任何改动都不会生效 —— 与 §15.6 判据顺序一致。

### 16.6 状态与边界
- 产物 `android-project/dist/Carplay-V1.8.32-debug.apk`（1.8.32 / 369，六项核验 PASS）。
- 任务 #121–#125 全部 completed；A/B 三态日志：`logs/v1.8.32-baseline.log` / `v1.8.32-wait-on.log` / `v1.8.32-rollback.log`。
- 设备当前装 **1.8.32/369**，`ctrl_wait_invitation` 已回滚 false（基线行为）；偏好未改其它项。
- 临时脚本已清理；本轮未做整车真 pcap（仍受 §15.7 边界约束）。
- 后续建议：**做 P1-1（iAP2 蓝牙登记触发）A/B 包**，核查 `Iap2Bootstrap`/`Iap2WirelessSetup` 在自动连接路径是否建立蓝牙会话并下发手机蓝牙标识（19982 等），使车机 AddOrUpdateController 登记成功；判据仍以 `V160_CTRL_TCP_ACCEPT` 是否由 0 变正为第一信号。

## 17. V1.8.33 诊断 / A/B 包：P1-1 蓝牙 iAP2 登记触发（2026-11-09 00:10，V1.8.33 / 370）

> 用户要求「继续完善 P1-1」——落地 §15.7 / §16.4 的下一缺口：让车机 `IosControlClientCallback(AddOrUpdateController)` 登记成功，使 `V160_CTRL_TCP_ACCEPT` 由 0 变正。不直接改 EA 默认值，改为「诊断 + 变体开关」形式，一次真车跑动即可定位断点，且默认关、可回滚。

### 17.1 改动清单（6 文件 + 版本，均未提交 git）
- `secured/ControlChannel.java`：新增 `Iap2DiagEnabled()`/`Iap2EaDrop19982()`（读 `carplayapk-debug.xml` 的 `iap2_diag`/`iap2_ea_drop_19982`，默认 false）+ `Iap2Diag(msg)`（logcat tag `CarPlayAPK`，前缀 `IAP2_DIAG `，仅开关开时打印）。
- `secured/MfiCertificateVerifier.java`：新增私有 `bEaDrop(int[],id)`；`bEaPayload()` 的 `toSend/toReceive` 改由 `toSendRaw/toReceiveRaw`（含 19982）经 `bEaDrop(19982)` 过滤生成（仅当 `iap2_ea_drop_19982=true`）；`y()` 的 `IAP2_CARPLAY_AVAILABILITY_PLAN`/`IAP2_PEER_CAPABILITIES`/`IAP2_IDENTIFIED`/`IAP2_DETECT_RESTART`、593 行 `contains(19982)` 块各加 `IAP2_DIAG` 探针。
- `prototype/PairingSession.java`：`P1_START`/`RFCOMM_SELECTED_PEER_ACCEPTED`/`RFCOMM_SELECTED_PEER_CONNECTED`/`I1_HANDSHAKE_START` 加 `IAP2_DIAG`。
- `prototype/WirelessCarPlayService.java`：`a` 回调里 `RCVD_IAP2_PEER_CAPABILITIES`/`RCVD_RFCOMM_LISTENING`/`RCVD_IAP2_AUTHENTICATED_WAITING_CARPLAY_START`/`RCVD_IAP2_DETECT_RESTART` 加 `IAP2_DIAG` 镜像。
- `prototype/DebugSwitchReceiver.java`：新增 `ACTION_IAP2`（`io.carplay.app.action.DEBUG_SET_IAP2`）读 extra `diag`/`ea_drop_19982` 写 `carplayapk-debug.xml`，日志 `DEBUG_IAP2_SET`；`AndroidManifest.xml` intent-filter 加该 action。
- `app/build.gradle.kts`：1.8.32/369 → 1.8.33/370。

### 17.2 编译与核验
`gradlew exportApk --offline` SUCCESS（仅 `MainActivity` autofill 既有 warning，与 P1-1 无关）；`verify_built_apk.sh` 六项 PASS（1.8.33/370、debuggable=true、签名有效、zip 完整）；多锚点 dex 扫描全命中：`IAP2_DIAG`×1、`iap2_diag`×2、`iap2_ea_drop_19982`×2、`DEBUG_SET_IAP2`×2、`bEaDrop`×4、`RCVD_IAP2`×3、`EA_PAYLOAD_BUILD`×1。产物 `android-project/dist/Carplay-V1.8.33-debug.apk`（1,327,843 B）。

### 17.3 安装与开关
`adb install -r` 到 OPPO PKJ110（`3B15B100CZ300000`）成功；显式组件广播开 `iap2_diag=true`/`ea_drop_19982=false`（基线），`run-as` 读 `shared_prefs/carplayapk-debug.xml` 已确认两键生效。设备当前装 **1.8.33/370**，已 armed（基线态，未剔除 19982）。

### 17.4 设计铁律
本包**只加诊断、不改 EA 默认值**（默认仍发 19982），避免回归真车已验证链路；仅在 `iap2_ea_drop_19982=true` 时剔除 19982 做对照。

### 17.5 真车 A/B 判据（待用户执行）
- 捕获：`adb logcat -d -s CarPlayAPK | grep IAP2_DIAG`，或 `adb shell run-as io.carplay.app cat files/connection-log-0.txt`。
- 判据顺序：`P1_START`→`RFCOMM_SELECTED_PEER_CONNECTED`→`I1_HANDSHAKE_START`（握手路径真跑了？）→`IAP2_DIAG PEER_CAPS transport_notification(19982)=` 真/假（对端声明 19982？）→`IAP2_DIAG DETECT_RESTART`（19982 触发重启假设的铁证）。
- 对照：若 `DETECT_RESTART` 出现，再 `am broadcast ... -a ...DEBUG_SET_IAP2 --ez ea_drop_19982 true` 复跑，看 `DETECT_RESTART` 是否消失、`V160_CTRL_TCP_ACCEPT` 是否由 0 变正。
- 边界：本包不修任何行为，仅定位断点；若 DETECT_RESTART 始终不出现且握手正常但仍 `V160_CTRL_TCP_ACCEPT=0`，则问题在车机固件登记条件（§15.7 边界），需整车 pcap。

### 17.6 状态与边界
- 产物 `android-project/dist/Carplay-V1.8.33-debug.apk`（1.8.33/370，六项核验 PASS）。
- 任务 #126 completed（实现）；#127 in_progress（编译核验已完成，真机 A/B 待用户执行）。
- 设备当前装 1.8.33/370，`iap2_diag=true`/`iap2_ea_drop_19982=false`；偏好未改其它项。
- 未提交 git。
