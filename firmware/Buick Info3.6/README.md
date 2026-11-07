# V167.3.1-GA 固件 CarPlay 组件研究材料

从别克/上汽通用车机固件包 `V167.3.1-GA（将解压的18个文件拷贝到U盘根目录）` 中解析并提取的 CarPlay 相关程序、库与配置。

> **更新记录**
> - 2026-10-31：初版，字符串级材料整理（§一～§七）。
> - **2026-11-07**：追加 capstone 字节码逆向结果（§四.6 配对 / 自动重连状态机、§四.7 `/command` 载荷结构），
>   并**补正 §四.1 的过时结论**（「input mode 协商被省略」已被实测证伪）。§七 待查项同步更新。
> - 本项目侧完整对比见仓库 `skill/references/pairing-reconnect-compare.md`，
>   排障案例见 `skill/references/troubleshooting.md` 案例 9 / 14。
>
> **地址约定**：`libNmeCarPlay.so` 为 x86-64 ELF，首 LOAD 段 vaddr=0 ⇒
> **虚拟地址 = 文件偏移**（`TEXT=0xcd30`，`SIZE=0x8eb03`）。下文偏移均可用 capstone 直接反汇编复核。

## 一、固件来源与平台

**源目录**：`D:\资料\V167.3.1-GA（将解压的18个文件拷贝到U盘根目录）`

18 个交付文件按 `delivery_manifest.csv` 的映射如下（只列与本次提取相关的）：

| 交付 ID | 分区（PART_ID） | 说明 | 本次处理 |
|---------|----------------|------|---------|
| 86519656 | SOC_SYSTEM | Android system 分区（ext4，3.35 GB） | ✅ 解包，CarPlay 主战场 |
| 86519659 | SOC_VENDOR | Android vendor 分区（ext4，1.57 GB） | ✅ 解包，仅 BSD 音频参数有 CarPlay |
| 86519660 | SOC_RESOURCE | 资源分区（ext4，4.19 GB） | 已解包，仅 `ro/media`、`ro/speech` 等媒体，无 CarPlay |
| 86519661 | SOC_UTILS | bootloader utils（QNX IFS / `ipk.` 格式） | 无关 |
| 86519657/58 | SOC_BOOT / SOC_RECOVERY | `ANDROID!` boot 镜像 | 未展开（不含 CarPlay） |
| 85556063 | SOC_HOSTOS | QNX/GHS INTEGRITY Hypervisor 宿主（`ipk.` 格式） | 未展开 |
| 85511627 | SOC_ABL | Intel Automotive Bootloader（`ipk.` 格式） | 无关 |

**目标平台（取自 `system/build.prop`）**

| 项 | 值 |
|---|---|
| 产品 | `gminfo36`（GM INFO3.6 座舱） |
| 版本 | `W40E-V167.3.1-MIHC21A-232`，incremental 40 |
| Android | 6.0.1 / SDK 23 |
| 架构 | x86_64（Intel Apollo Lake，`bxtp_abl`，odex 变体 `silvermont`） |
| 构建日期 | 2022-12-22 |

## 二、提取结果

总计 **43 MB / 73 个文件**（含 7 个分析产物），按分区原始路径组织：

```
CarPlay-RE/fw/V167/
├── system/
│   ├── app/
│   │   ├── GMCarPlay/
│   │   │   ├── GMCarPlay.apk                    2.9 MB  CarPlay HMI（无 dex，纯资源+清单）
│   │   │   └── oat/x86_64/GMCarPlay.odex        189 KB  实际代码（预编译 dex）
│   │   └── GMCarLife/GMCarLife.apk              3.5 MB  百度 CarLife 前端（同类投影对比）
│   ├── bin/
│   │   ├── MediaManagerService                  1.05 MB ★ CarPlay 服务端宿主（Cinemo NME）
│   │   ├── carplay.sh                             105 B  有线 CarPlay 的 USB gadget IPv6 路由
│   │   ├── native_enableApple.sh                   86 B  USB gadget → dwc3.0.auto
│   │   ├── remote_enableApple.sh                   95 B  USB gadget → dabr_udc.0
│   │   ├── vehicleinputservice                   9.9 KB  车辆输入服务（触摸/按键注入）
│   │   └── remote_display                       62 KB  远端显示（CarPlay 关联待验证）
│   ├── lib64/                                   31 个库，24 MB ★
│   │   ├── libNmeCarPlay.so                     877 KB  AirPlay/iAP2 协议栈核心
│   │   ├── libNmeSDK.so                         3.5 MB  Cinemo SDK 主体
│   │   ├── libNmeBaseClasses.so                 4.5 MB  基础类/对象模型
│   │   ├── libNmeAppleAuth.so / Impl.so         148 KB  Apple 鉴权（EAP/MFi）
│   │   ├── libNmeTransport.so / AndroidTransport.so  180 KB  传输层
│   │   ├── libNmeAudio*.so（9 个）              2.5 MB  AAC / AAC-ELD / Opus / MP3 / FLAC 等
│   │   ├── libNmeVideo*.so（5 个）              4.5 MB  视频解码/渲染（含 SW、GAVD）
│   │   ├── libNmeVfs.so / libNmeImage.so / libNmeNav.so …
│   │   └── libCarLifeServiceJNI.so              937 KB  CarLife 服务 JNI（对比用）
│   ├── framework/gm-res/gm-res.apk              414 KB  权限定义：CARPLAY 权限组 / ACCESS_CARPLAY_EAP
│   ├── priv-app/
│   │   ├── GMSmartConnect/
│   │   │   ├── GMSmartConnect.apk               563 KB  无线连接管理前端
│   │   │   └── oat/x86_64/GMSmartConnect.odex    10 MB  其代码
│   │   └── GMBluetooth/GMBluetooth.apk          103 KB  蓝牙管理（含 classes.dex，可直接反编译）
│   ├── etc/                                             ALSA/音频策略（CarPlay 音频路由）
│   │   ├── asound.conf / asound_syncdump.conf
│   │   ├── audio_policy.conf
│   │   └── vehicle_audio_service.cfg
│   └── build.prop
├── vendor/
│   ├── build.prop
│   └── etc/BSD/                                 15 个 CarPlay 免手持有源降噪调音表（.bsd）
└── analysis/                                    文本化分析产物
    ├── MediaManagerService.strings.txt          2 444 条全部字符串
    ├── MediaManagerService.carplay.txt            469 条过滤后关键串
    ├── libNmeCarPlay.strings.txt                2 425 条
    ├── libNmeAppleAuth.strings.txt                237 条
    ├── GMCarPlay.odex.carplay.txt                 150 条
    ├── GMSmartConnect.carplay.txt                 713 条
    └── inventory.csv                              文件清单（路径/大小/说明）
```

## 三、CarPlay 软件架构（据二进制证据还原）

```
┌──────────────────────────────────────────────────────────────┐
│ GMCarPlay.apk（com.gm.carplay.hmi）        Java，仅 UI 层      │
│  MainActivity / ProjectionConsentActivity / EnableCarplay*    │
│  SurfaceView 承载视频；未连接时弹「连接手机」引导              │
└───────────────────────────┬──────────────────────────────────┘
                            │ AIDL：gm.carplay.ICarPlayService
                            │ 客户端封装 CarPlayServiceManager
                            │ （GMCarPlay / GMSmartConnect / GMSettings 三处引用）
                            ▼
                  ┌───────────────────────┐
                  │ CarPlayService 服务端  │ ← 实现未落在 system/vendor 分区
                  │ （AIDL Stub 实现）     │    （见第七节待查项）
                  └───────────┬───────────┘
                              │ binder 服务名：gm.media.MediaManagerService
                              ▼
┌──────────────────────────────────────────────────────────────┐
│ MediaManagerService（native 进程，1.05 MB）                    │
│  MediaAppleInterface / MediaCarPlayAuthentication              │
│  MediaWirelessCarPlay / MediaDeviceManager                     │
│  CinemoCreateDDP（Device Discovery Protocol）                   │
└───────────────────────────┬──────────────────────────────────┘
                            │ dlopen
                            ▼
┌──────────────────────────────────────────────────────────────┐
│ Cinemo NME 协议栈（libNme*.so，30 个）                          │
│  libNmeCarPlay.so：AirPlay server + iAP2 + HID + 音视频桥接     │
│  libNmeAppleAuth*：Apple 鉴权（EAP session / 证书 / 签名）      │
│  libNmeTransport / AndroidTransport：USB 与 WiFi 传输          │
│  libNmeSDK / BaseClasses / Vfs / Image：SDK 基础设施            │
└──────────────────────────────────────────────────────────────┘
```
<svg viewBox="0 0 680 322" width="100%" role="img" xmlns="http://www.w3.org/2000/svg">
<title>车机与 APK 的 CarPlay 连接发起方向对比</title>
<desc>车机点火后自动恢复配对表并主动发起 connect，手机 APK 无后台、只在用户点击后主动建会话，两条链方向相反。</desc>
<defs>
<marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
<path d="M2 1L8 5L2 9" fill="none" stroke="context-stroke" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</marker>
</defs>
<text class="th" x="40" y="30" fill="#0C447C">车机侧 · 系统常驻，点火后自动发起</text>
<g><rect x="40" y="52" width="118" height="56" rx="8" fill="#E6F1FB" stroke="#185FA5" stroke-width="0.5"/>
<text class="th" x="99" y="72" text-anchor="middle" dominant-baseline="central" fill="#0C447C">恢复配对表</text>
<text class="ts" x="99" y="90" text-anchor="middle" dominant-baseline="central" fill="#185FA5">carplay_devid</text></g>
<path d="M160 80 L190 80" fill="none" stroke="#185FA5" stroke-width="1.5" marker-end="url(#arrow)"/>
<g><rect x="194" y="52" width="118" height="56" rx="8" fill="#E6F1FB" stroke="#185FA5" stroke-width="0.5"/>
<text class="th" x="253" y="72" text-anchor="middle" dominant-baseline="central" fill="#0C447C">发现服务</text>
<text class="ts" x="253" y="90" text-anchor="middle" dominant-baseline="central" fill="#185FA5">browse ctrl</text></g>
<path d="M314 80 L344 80" fill="none" stroke="#185FA5" stroke-width="1.5" marker-end="url(#arrow)"/>
<g><rect x="348" y="52" width="118" height="56" rx="8" fill="#E6F1FB" stroke="#185FA5" stroke-width="0.5"/>
<text class="th" x="407" y="72" text-anchor="middle" dominant-baseline="central" fill="#0C447C">表内匹配</text>
<text class="ts" x="407" y="90" text-anchor="middle" dominant-baseline="central" fill="#185FA5">SendConnect</text></g>
<path d="M468 80 L498 80" fill="none" stroke="#185FA5" stroke-width="1.5" marker-end="url(#arrow)"/>
<g><rect x="502" y="52" width="118" height="56" rx="8" fill="#E6F1FB" stroke="#185FA5" stroke-width="0.5"/>
<text class="th" x="561" y="72" text-anchor="middle" dominant-baseline="central" fill="#0C447C">发起 connect</text>
<text class="ts" x="561" y="90" text-anchor="middle" dominant-baseline="central" fill="#185FA5">GET ctrl-int</text></g>
<path d="M40 138 L620 138" fill="none" stroke="#888780" stroke-width="0.5" stroke-dasharray="4 4"/>
<text class="ts" x="40" y="132" fill="#5F5E5A">车机发出的 connect 到达时，手机侧没有常驻进程在监听</text>
<text class="th" x="40" y="166" fill="#791F1F">手机侧 · 本项目 APK，只在用户点击后存在</text>
<g><rect x="40" y="182" width="118" height="56" rx="8" fill="#FCEBEB" stroke="#A32D2D" stroke-width="0.5"/>
<text class="th" x="99" y="202" text-anchor="middle" dominant-baseline="central" fill="#791F1F">仅前台广播</text>
<text class="ts" x="99" y="220" text-anchor="middle" dominant-baseline="central" fill="#A32D2D">onResume</text></g>
<path d="M160 210 L190 210" fill="none" stroke="#A32D2D" stroke-width="1.5" marker-end="url(#arrow)"/>
<g><rect x="194" y="182" width="118" height="56" rx="8" fill="#FCEBEB" stroke="#A32D2D" stroke-width="0.5"/>
<text class="th" x="253" y="202" text-anchor="middle" dominant-baseline="central" fill="#791F1F">邀请仅记日志</text>
<text class="ts" x="253" y="220" text-anchor="middle" dominant-baseline="central" fill="#A32D2D">b() 回调</text></g>
<path d="M314 210 L344 210" fill="none" stroke="#A32D2D" stroke-width="1.5" marker-end="url(#arrow)"/>
<g><rect x="348" y="182" width="118" height="56" rx="8" fill="#FCEBEB" stroke="#A32D2D" stroke-width="0.5"/>
<text class="th" x="407" y="202" text-anchor="middle" dominant-baseline="central" fill="#791F1F">不启动服务</text>
<text class="ts" x="407" y="220" text-anchor="middle" dominant-baseline="central" fill="#A32D2D">无后台常驻</text></g>
<path d="M468 210 L498 210" fill="none" stroke="#A32D2D" stroke-width="1.5" marker-end="url(#arrow)"/>
<g><rect x="502" y="182" width="118" height="56" rx="8" fill="#FCEBEB" stroke="#A32D2D" stroke-width="0.5"/>
<text class="th" x="561" y="202" text-anchor="middle" dominant-baseline="central" fill="#791F1F">全靠手动点</text>
<text class="ts" x="561" y="220" text-anchor="middle" dominant-baseline="central" fill="#A32D2D">F0() 唯一入口</text></g>
<rect class="box" x="40" y="262" width="580" height="40" rx="8" stroke-width="0.5"/>
<text class="t" x="60" y="282" dominant-baseline="central">只有两侧同时被人工触发，两条各自独立的链才碰巧对齐</text>
</svg>
## 四、与本项目（无线 CarPlay 触摸回传）直接相关的证据

以下均摘自本目录 `analysis/` 内的字符串导出，可逐条复核。

### 1. 触摸报告走 iAP2 HID，输入模式协商走 HTTP `/command`

`libNmeCarPlay.so` 中的 AirPlay 会话回调表：

```
SetCallbacks(): ... fnModesChanged=... fnSetInputMode=... fnGetCertificate=...
                 fnGetSignature=... fnHapticFeedback=... fnVideoPrepare=...
OnSessionSetInputMode() callback enter / leave -> %e
Set HIDInputMode
Registered HID %''@, %X   /  Deregistered HID %''@, %X
[0x%p]->SendHIDReport()
```

`MediaManagerService` 中有 iAP2 HID 组件元数据：

```
CINEMO_METANAME_IAP_HID_COMPONENT_NAME / _COMPONENT_FUNCTION
CINEMO_METANAME_IAP_HID_REPORT_DESCRIPTOR / _PRODUCT_ID / _VENDOR_ID
MMS:addToMetapool(iAP2 HID report descriptor array) Failed
```

→ 真车把触摸封装为 iAP2 的 HID component（带 report descriptor），在 AirPlay 会话内以
`SendHIDReport()` 上行；`SetInputMode / OnSessionSetInputMode` 确实存在于规范实现中。

**方向澄清**：手机→车机的 `hidSetInputMode` 走 `POST /command`（见 §四.7），
车机→手机的 `hidSendReport` 是反向控制通道上的入站命令，两者不是同一条连接。

> **⚠️ 2026-11-07 补正：本段原有结论「input mode 协商被省略」已作废。**
> 后续真机与字节码证据：
> - V1.8.20 起 `/command` 改成 `{type, params}` 后，`modesChanged` 与 `hidSetInputMode`
>   **均获车机 200**；V1.8.21 起命令真正写入控制通道（专用线程 + `X-Apple-Session-ID`）。
> - 固件 `0x44559` 起的 `hidSetInputMode` 分支**只把命令转发给上层回调**
>   （`NmeCarPlay.cpp` 的 `OnSessionSetInputMode`，回调槽 `[obj+0x848]` @0x17332），
>   **不解释 mode 值** ⇒ 200 只代表「转发成功」，不代表 HMI 已启用触摸。
> - `hidSendReport` 构造在 `0x4ab29 → 0x4ab62 call 0x45100`，统一门 `[obj+0x466]` @0x45117，
>   唯一写入点 `0x484f1`（`startSession` 成功后置 1）。
> - 全部历史抓包中 `CARPLAY_INBOUND_REQUEST` / `hidSendReport` / `HID_REPORT_ARRIVED` /
>   `TOUCH_FORWARDED` **均为 0**；`CARPLAY_EVENT_INPUT ... read_calls=1 raw_bytes=0 messages=0`。
> ⇒ 真卡点不是 input mode，而是**车机→手机的反向控制通道 / CarPlay 控制会话从未登记**，
> 见新增 §四.6。补协议应优先解决会话登记，而不是继续扫 `hidInputMode`。

### 2. 无线传输：独立于 AirPlay 的 iAP2-over-WiFi

```
NmeTransport:CreateNmeIAPWifiTransport
CINEMO_OPTION_CARPLAY_WIFI_TRANSPORT_NAME
Apple_CarPlay_enableWireless / Apple_CarPlay_touchFidelity
MMS: Cinemo Friendly Wireless CarPlay DDP usbfsDevicePath: %s
MMS: MediaWirelessCarPlay() - startSync() Entry
MMS: get carplay type from calibration ... carplayEnableWirelessValue = %s
SetOption(CINEMO_OPTION_CARPLAY_WIFI_TRANSPORT_NAME): %s
```

→ 无线 CarPlay 的 iAP2 走 **专用 WiFi transport**（由 `CreateNmeIAPWifiTransport` 建链），
而不是复用 AirPlay 的 event channel。`carplayEnableWirelessValue` 由标定（calibration）决定开关，
这也解释了为什么同一固件在不同车型/配置上无线能力不同。

### 3. 蓝牙 OOB 配对（对应「蓝牙反复重连」问题域）

```
CINEMO_METANAME_IAP_BT_MAC_ADDRESS
MediaAppleInterface::OOBBTPairingLinkKeyInformation()
m_deviceType = %s, mEnableOOBBTPairing = %d, mEnableWIFIComponent = %d
bluetoothIDs
```

→ 无线 CarPlay 建链依赖 **蓝牙 OOB（Out-of-Band）链路密钥交换**，车机把 BT MAC 与 link key
通过 iAP2 元数据交给 iPhone，随后 iPhone 用 BT 发起 WiFi 关联。`mEnableOOBBTPairing` 是开关；
若 OOB 失败会退化为反复重连——正是之前日志里观察到的抖动形态。

### 4. 鉴权与会话

```
MMS: MediaCarPlayAuthentication() - startProjectionAuth(): Entry
MMS: Received EAPSessionInitiated callback from Cinemo
MMS: Received EndEAPSession callback from Cinemo
MMS: extractDevNameAndUUID done for CarPlay Authentication
CINEMO_OPTION_APPLEAUTH_URL
```

→ 连接前先做 Apple EAP 鉴权（`gm.permission.ACCESS_CARPLAY_EAP` 即对应此能力的 Android 权限），
`startProjectionAuth` 失败即整体连接失败，是排障时第一个应检查的点。

### 5. 有线通路

```
carplay.sh              : ip -6 route add fe80::/64 dev usb0    （USB 网卡链路本地）
native_enableApple.sh   : echo dwc3.0.auto  > .../CarPlayGadget/UDC
remote_enableApple.sh   : echo dabr_udc.0   > .../CarPlayGadget/UDC
```

→ 有线 CarPlay 通过 USB gadget（`CarPlayGadget`）模拟 Apple 配件，UDC 可在 SoC 原生口与座舱远端口之间切换。

### 6. 配对与车机启动自动重连状态机（2026-11-07 新增）

> 证据等级：**A** = `libNmeCarPlay.so` capstone 字节码；**B** = odex / 字符串表（**未反编译**，
> 只证明符号存在，不证明调用时序）；**C** = 本项目历史抓包计数。
> 完整两侧对比见 `skill/references/pairing-reconnect-compare.md`。

#### 6.1 首次配对（B 级为主）

| 阶段 | 证据 |
| --- | --- |
| 配对发起 / UX | `android.intent.action.CARPLAY_PAIRING_INITIATED`（`GMCarPlay.odex` @0xcad4）、`com.gm.carplay.intent.action.PROMPT_CARPLAY_CONNECTED` / `PROMPT_ABRUPT_DISCONNECTION`；`already_paired` / `progress_pairing` / `pair_success` / `pair_failure` / `setPairingConfirmation` |
| 蓝牙 OOB | `MediaAppleInterface::OOBBTPairingLinkKeyInformation()`、`mEnableOOBBTPairing`、`CINEMO_METANAME_IAP_BT_MAC_ADDRESS`（见 §四.3） |
| Apple EAP 鉴权 | `MMS: MediaCarPlayAuthentication() - startProjectionAuth(): Entry`（见 §四.4） |
| 设备持久化 | `carplay_devid` / `carplay_phone_name` / `carplay_pp_capability` / `carplay_icon`（`GMSmartConnect.odex` 字符串表相邻，@0x5986ac 附近）；`isBtPaired` / `isBtConnected` / `isBTIdDevice` / `isBtAddressValid` / `getPairedPhonesDetail` |
| 会话状态 | `CARPLAY_STATE_NOT_CONNECTED` ↔ `CARPLAY_STATE_SESSION_INPROGRESS`（@0x382cf5） |
| CarPlay 客户端登记 | **`AddCarPlayClient()` @0x178d0**（见 6.3） |
| 会话开始 | `SessionStarted()` @0x18727（回调槽 `[obj+0x828]`）、`OnSessionDisableBluetooth()` @0x171ef（槽 `[obj+0x840]`）、`Wireless Carplay Device connected` |

#### 6.2 点火 / 启动后自动重连（A 级链路）

```
车机上电
 → GMSmartConnect 读蓝牙配对表 / BLUETOOTH_DEVICE_AUTO_IDENTIFICATION_CalID     [B]
 → 蓝牙自动回连上次手机 + EVENT_PHONE_PAIRED_DEVICE_CHANGED                     [B]
 → AddCarPlayClient(event=0, ..., BT_MAC, 设备标识) 恢复 [obj+0x9e0] 客户端表    [A]
 → _CarPlayControlClientEnsureStarted() @0x52fc0
     · [obj+0x30] 已启动则跳过；否则 0x5303b 建立 "_carplay-ctrl._tcp" browse
       （服务类型字符串 @0xa3d0f，域名 "local." @0xa0b81）
 → _CarPlayControlClientBrowseCallback() @0x536e0
     · event=1 "Add" → 0x53860 → ControlClientCallback @0x53c00
       读 "removedServices" / "services" → 0x537d0（服务激活/入列，
       EnsureStarted @0x530f6 也调用它）
     · event=2 "Remove" → 0x5379f；"Stop" / "Restarted" 事件名在
       0x53722–0x5376f，其分支走向待核
 → SendConnect() @0x17e30：客户端表内双字符串匹配，命中才发命令              [A]
 → _CarPlayControlClientSendCommand() @0x54900：GET /ctrl-int/1/connect       [A]
 → 手机回 200 → SessionStarted → fnCarplayServiceUpdate 回调 [obj+0x898] → HMI
```

ctrl 客户端区读取的键：`name` @0x52ae7、`srcvers` @0x52c2c、`services` @0x52cd9、
`ifname` @0x52d18（`services` 究竟来自 TXTRecord 还是 Bonjour info 字典**尚未确认**，
不要据此直接补 TXT 键）。

#### 6.3 两个关键门控（A 级，本次新定位）

| 函数 | 范围 | 行为 |
| --- | --- | --- |
| `AddCarPlayClient()` | `0x178d0–0x17ca1` | `0x178f7` 把 6 字节入参格式化为 `%02X:%02X:...`（MAC 串，存 `[rsp+0x80]`），与 `[rsp+0x40]` 的第二标识串组成二元组；表在 `[obj+0x9e0]`、条目步长 `0x30`；`0x179a0 cmp ebp,1` ⇒ **event=0 添加**（`0x17aad` 构造、`[+0x68]=1`、`0x17d20` 入表）、**event=1 删除**（`0x17b00` 匹配后 `0x17d90` erase）；添加前用 `0xbce0`/`0xbcf0` 双串比对去重（日志 `AddCarPlayClient() %s == %s && %s == %s` @0x17a66）；`0x17ba7` 判 `[obj+0x898]` 后调 `fnCarplayServiceUpdate` @0x17c25 |
| `SendConnect()` | `0x17e30–0x1806a` | 遍历同一张表，`0x17f31`/`0x17f41` 两次 `0xbcf0` 比较 `[entry+0x10]` 与 `[entry+0]`；`0x17f72 cmp rbx,rax; je 0x18040` ⇒ **未命中返回 `ebp=0xb`，不发任何 connect**，命中才 `0x17fdf → call 0xe400` |

⇒ **车机只向客户端表里已登记的 `(MAC, 设备标识)` 二元组发 connect**，这就是「车机认得上次手机」的判据，
也是触摸桥接的前置条件。这些函数属于 `NmeCarPlay.cpp` 的 C++ 适配层（日志前缀
`../../../NmeCarPlay/src/NmeCarPlay.cpp`）。

#### 6.4 connect 请求的实际格式（A 级，@0x54900–0x54ad0）

```
GET /ctrl-int/1/connect HTTP/1.1        ← 模板 "/ctrl-int/1/%s" @0xa429f + "connect" @0xa415c
Host: <...>
User-Agent: AirPlay/320.17.3            ← @0xa42b3
AirPlay-Receiver-Device-ID: %llu        ← @0xa42c4，车机自己的 receiver id
```

本项目 APK 的 `ControlRequestParser.c()` 恰好只认 `GET /ctrl-int/1/connect`（非 GET→405、
`Content-Length` 非 0 判 400）⇒ **请求格式本身对得上**，问题在手机侧有没有常驻进程在监听。

#### 6.5 车机自带的失败恢复（A 级 xref）

`_BonjourBrowser_ReconfirmDevice` @0x581f2、`_BonjourBrowser_RetryTimerFired`
@0x57e03 / 0x57e8a / 0x5b61b、`_IdleStateKeepAliveStart` @0x4b693 / 0x4be23、
`_IdleStateKeepAliveStop` @0x48d61 等、`_IdleStateKeepAliveReceiveBeacon` @0x4c18e、
`_ControlIdleStateTransition` @0x475ca、`_ControlStart` @0x48793 / 0x48808。
即车机有独立于手机的常驻保活与重新发现机制，无次数上限。

#### 6.6 与本项目 APK 的核心差异（C 级佐证）

**车机是连接发起方，手机 APK 却把自己做成了唯一发起方**：本项目除 `MainActivity.F0()` 的
UI 点击外没有任何启动源（0 receiver / 无 BOOT / `START_NOT_STICKY`），`_carplay-ctrl` 只在
`onResume` 广播；车机主动发来的 connect 邀请在 `StartEarly.b()` 里**只打一行日志**，不会拉起服务。
历史全量抓包 `CTRL_INVITATION_ACCEPTED` / `CTRL_IDENTITY_BOUND` / `CARPLAY_INBOUND_REQUEST`
**均为 0**，`V160_CTRL_TCP_ACCEPT` 仅 2 次且早于广播开始 54 秒。
⇒ 车机单方面回连时手机无进程；手机单方面发起时车机 `SendConnect` 因表未登记而不发 connect，
只有两边同时人工触发才碰巧对齐。修复优先级见 `pairing-reconnect-compare.md` §6。

### 7. `/command` 载荷结构：规范 vs Cinemo 固件（2026-11-07 新增）

`_requestProcessCommand()` @0x42150 按 **`{type, params}`** 解析（与 Apple CarPlay 规范
Table 23-71 的顶层键写法**不一致**，真机以固件为准）：

| 地址 | 行为 |
| --- | --- |
| `0x421cf` / `0x421f3` | 分别取 `type` / `params` |
| `0x4441b` | `test r13,r13` —— `params` 为空则跳 `0x4464a` 报错 |
| `0x422b5` | `mov ebp,0x1a6` ⇒ **HTTP 422**；同时跳过 `0x4227a` 的响应体构造 ⇒ **空 body 由此而来** |
| `0x447d0` | 从 `params` 取 `appStates` (@0x4480c) / `resources` (@0x44991) |
| `0x443d0` | 入站命令白名单：`modesChanged` / `requestUI` / `updateFeedback` / `hidSetInputMode` / `iAPSendMessage` |

`setUIContext` / `setNightMode` **不在白名单**（后者是车机→手机命令），拿它们当探针必然 422；
此前「`/command` 端点被整体拒绝」的判断因此作废 —— 是**载荷结构**问题，不是端点问题。
本项目 V1.8.20 起把 `modesChanged` / `hidSetInputMode` 恢复 `{type, params}`、
`setUIContext` 改 `requestUI(params.url)`、探针改 `updateFeedback`，实测 200。
另：`/command` 只接受 binary plist（JSON 回 400）。

## 五、工具（可继续深挖固件）

| 脚本 | 用途 |
|------|------|
| `fw/ext4tool.py` | 纯 Python 读 ext4 镜像：`ls` / `find` / `cat` / `get`，无需 root、无需挂载 |
| `fw/strings.py` | `strings` 替代（ASCII + UTF-16LE），支持正则过滤 |
| `fw/content_scan.py` | 按内容批量扫描镜像内指定类型文件 |
| `fw/scan_manifests.py` | 批量解析所有 APK 的二进制 AndroidManifest，按关键词定位组件 |

用法示例（注意 MSYS 会改写以 `/` 开头的参数，需加 `MSYS_NO_PATHCONV=1`）：

```bash
cd CarPlay-RE/fw
MSYS_NO_PATHCONV=1 python ext4tool.py ls   "D:/_fw_tmp/86519656" "/lib64"
MSYS_NO_PATHCONV=1 python ext4tool.py find "D:/_fw_tmp/86519656" "carplay|airplay|iap2" 25
MSYS_NO_PATHCONV=1 python ext4tool.py get  "D:/_fw_tmp/86519656" "/bin/MediaManagerService" out.bin
python strings.py out.bin -n 8 --grep "carplay|hid" --sort
```

### 字节码复核（2026-11-07 新增）

§四.6 / §四.7 的地址用 capstone 直接复核（依赖 `pip install capstone`）：

```python
import capstone, re
p = r"firmware/Buick Info3.6/system/lib64/libNmeCarPlay.so"
d = open(p, "rb").read()                       # VA = 文件偏移
md = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
md.detail = True; md.skipdata = True

# 坑：线性反汇编起点必须落在指令边界，从 0x178d0 直接开始会返回空。
# 从已知函数入口 0x17180 起扫，再按地址过滤出 AddCarPlayClient。
for i in md.disasm(d[0x17180:0x17cb0], 0x17180):
    if i.address < 0x178d0 or i.mnemonic == ".byte":
        continue
    print(f"{i.address:06x}  {i.mnemonic:8s} {i.op_str}")

# rip 相对引用 → 目标字符串地址（用于定位键名与日志文本）
i = next(md.disasm(d[0x52c2c:0x52c3b], 0x52c2c))
m = re.search(r"\[rip \+ 0x([0-9a-f]+)\]", i.op_str)
t = i.address + i.size + int(m.group(1), 16)   # -> 0xa0b0b "srcvers"
```

odex 字符串表上下文（`carplay_devid` 等 B 级证据的取法）：

```python
d = open(r"firmware/Buick Info3.6/system/priv-app/GMSmartConnect/oat/x86_64/GMSmartConnect.odex", "rb").read()
pos = d.find(b"carplay_devid")
print(re.findall(rb"[ -~]{4,}", d[pos-300:pos+300]))
```

本节只做**只读复核**，不修改固件。两个 odex 尚未反编译，见 §七 待查项 3。

## 六、临时文件

解包出的 raw ext4 镜像仍保留在 `D:\_fw_tmp\`（合计约 9.2 GB），供继续提取：

| 文件 | 大小 | 内容 |
|------|------|------|
| `86519656` | 3.35 GB | system |
| `86519659` | 1.57 GB | vendor |
| `86519660` | 4.19 GB | resource |
| `86519661` | 22 MB | utils（QNX IFS） |
| `services.odex` | 18 MB | 从 system 提取，system_server 代码 |

不再需要时可直接删除整个 `D:\_fw_tmp`，材料完整保留在本目录，重新解包约 30 秒。

## 七、待查项

1. **`gm.carplay.ICarPlayService` 的服务端实现位置**：`system`/`vendor` 全部 APK 与 odex、
   `services.odex` 中均只有客户端（`CarPlayServiceManager`、`ICarPlayService$Stub$Proxy`），
   未见 `Lgm/carplay/CarPlayService;` 类。可能位于 QNX 侧（`86519655` VIP App / `85556063` HostOS）
   或由 `MediaManagerService` 以 native binder 方式承担。
2. **`/etc/airplay.conf`**：`libNmeCarPlay.so` 引用该路径，但 system/vendor 均无此文件，
   推测使用编译期默认值或运行时由标定下发。
3. **`GMCarPlay.apk` / `GMSmartConnect.apk` 反编译**：本次只从两个 odex 的**字符串表**
   拿到配对键与状态常量（B 级证据，§四.6.1）。仍需 baksmali / `dexdump` 还原
   `com.gm.carplay.hmi.*` 与 GMSmartConnect 的连接状态机，才能确认「上电后何时读
   `carplay_devid`」「蓝牙自动回连如何触发登记」的**时序**。
4. **`AddCarPlayClient` 二元组中第二个字符串的含义**（deviceID / name / uuid 未定），
   以及 `ControlClientCallback` 读的 `services` 究竟来自 TXTRecord 还是 Bonjour info 字典
   （需跟 `0x5b7a0` 的返回类型）。**未确认前不要补 TXT 键**：本项目 V1.7.70 一次补 22 键
   出过 ctrl 发现回归。
5. **`fnCarplayServiceUpdate` 回调（`[obj+0x898]`）的上层实现所在进程**，以及
   `carplay_devid` / `carplay_phone_name` 的实际读写点（SharedPreferences? Settings? 标定?）。
6. **蓝牙 OOB → CarPlay 客户端登记的触发条件**：`BLUETOOTH_DEVICE_AUTO_IDENTIFICATION_CalID`
   目前只有标定常量字符串，未见字节码级调用链；这是点火自动重连能否成立的关键。
7. **`hidSendReport` 的 `[obj+0x466]` 门控**：唯一写入点 `0x484f1` 属 `startSession` 成功后置位，
   但「会话登记为 CarPlay」与「HMI 桥接 HMI 触摸」之间还有哪些条件未查清（对应触摸无反应）。
