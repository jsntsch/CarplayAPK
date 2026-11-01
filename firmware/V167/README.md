# V167.3.1-GA 固件 CarPlay 组件研究材料

从别克/上汽通用车机固件包 `V167.3.1-GA（将解压的18个文件拷贝到U盘根目录）` 中解析并提取的 CarPlay 相关程序、库与配置。

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

## 四、与本项目（无线 CarPlay 触摸回传）直接相关的证据

以下均摘自本目录 `analysis/` 内的字符串导出，可逐条复核。

### 1. 触摸走的是 iAP2 HID，不是 HTTP `/command`

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
这与之前逆向第三方接收器时「事件通道未见 `hidSetInputMode`、只有 `POST /command` + `hidSendReport`」
形成对照：**HID 通道成立，但 input mode 协商被省略**，可作为后续补协议的依据。

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
3. **`GMCarPlay.apk` 反编译**：需先用 baksmali/`dexdump` 处理 `GMCarPlay.odex`
   （odex 内含预编译 dex），可直接还原 `com.gm.carplay.hmi.*` 的连接状态机。
