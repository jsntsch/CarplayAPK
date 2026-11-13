# V1.8.55 — 触摸终局判定 + 关闭屏幕自动旋转

> 时间：2026-11-13 18:07~18:15
> 版本：V1.8.54/391 → **V1.8.55/392（已装包并验证方向锁定）**
> 目标：① 结合社区 + 固件，定论「投屏成功但不触摸」；② 关闭 App 的屏幕自动旋转

---

## 一、触摸：手机侧已到协议边界（A 级终局证据）

### 1.1 决定性判据：车机**全部接受**了 HID 武装命令

V1.8.53 应用内日志（两个会话，共 6 条命令）：

```
HID_SEND_CMD slot=0 type=hidSetInputMode mode=0 uuid=3
HID_SEND_CMD slot=1 type=hidSetInputMode mode=0 uuid=4
HID_SEND_CMD slot=2 type=hidSetInputMode mode=0 uuid=2
HID_INPUT_MODE_ACCEPTED slot=0 status=200 carrier=CONTROL mode=0 awaiting_hid_report=false
HID_INPUT_MODE_ACCEPTED slot=1 status=200 carrier=CONTROL mode=0 awaiting_hid_report=false
HID_INPUT_MODE_ACCEPTED slot=2 status=200 carrier=CONTROL mode=0 awaiting_hid_report=false
（共 5 次 ACCEPTED）
```

合并 `CARPLAY_RESP op=COMMAND lastCseq=12/14/16`，可以确认全链路：

| 环节 | 结果 |
| --- | --- |
| `hidSetInputMode` 发出 | ✅ 6 条 |
| 走的是事件通道（`EventChannel.g`），非 RTSP 控制连接 | ✅ 符合社区「必须走事件通道」 |
| uuid 用 `/info` 原样字符串（"3"/"2"/"4"） | ✅ 符合社区 |
| 车机 HTTP 状态 | ✅ **200** |
| 响应体 `status` | ✅ **0**（`w2()` 通过，未抛 `HID_INPUT_MODE_APPLICATION_REJECTED`） |
| 我方 `HidOK()` | ✅ 已收尾（`awaiting_hid_report=false`） |
| **车机 `hidSendReport`** | ❌ **0 条** |
| **事件通道入站字节** | ❌ **`raw_bytes=0`** |

⇒ **命令层 100% 成功，执行层 0 输出。** 这在协议内已无可再试的手机侧动作。

### 1.2 固件说明了执行层为什么是 0（`libNmeVfs.so`，iAP2/VFS 层）

```
0x2bb59b  SendHIDReport: Error reading iAP2 HID configuration
0x2bb5d7  SendHIDReport: HID component has not been started via StartHID yet
0x2bb622  SendHIDReport: No matching HID component found for "%s"
0x29f206  handle_StartHID        0x29f22d  handle_StartHID no device
0x29f2a7  handle_SendHIDReport   0x29f2c3  handle_SendHIDReport no device
```

配套元数据键：`NMEMETANAME_IAP_HID_BLUETOOTH_TRANS_COMP_ID`、`IAP_HID_REPORT_DESCRIPTOR`、
`IAP_HID_COMPONENT_NAME/FUNCTION/VENDOR_ID/PRODUCT_ID`。

**含义**：车机要发触摸，必须先有一套 **iAP2 HID 组件**（`StartHID` 之后才有 `SendHIDReport`），
而这套组件的元数据只能由 **Declarer 侧（车机自己作为 Accessory）** 在 `1D01` 里声明。

### 1.3 车机 `1D01` 里没有这套声明

```
IAP2_PEER_1D01_PARAMS 0=8 1=8 2=15 3=15 4=26 5=4 6=0 7=16 8=1 9=2 10=32x4 11=11 12=3 13=3 17=42 total=18
IAP2_PEER_MESSAGE_IDS direction=SEND count=0 ids=NONE
IAP2_PEER_TRANSPORT_COMPONENTS bluetooth=1 wireless_carplay=0 usb_device=0 usb_host=0
```

- 只有 param **0..13 与 17**，**没有 18/19/20（USBHostHID / BluetoothHID / USBDeviceHID Component）**
- `SEND count=0` ⇒ 车机不声明会主动发任何 iAP2 消息
- 车机自己报 `NATIVE_MODULE_STATUS connect=UNDECLARED mic=UNDECLARED vr=UNDECLARED`

⇒ 车机既没有「通过 iAP2 发 HID」的组件，也没有「通过 AirPlay 发 `/command`」的动作。

### 1.4 与社区经验的交叉验证

| 社区要点（cardeck「触摸链五连破」/ DiPlay） | 我方现状 |
| --- | --- |
| 车机命令必须走**事件通道** socket 上的 `POST /command` | ✅ 已走 `EventChannel.g()` |
| `hidSetInputMode`（uuid **原样字符串**回传）武装 HID | ✅ 已原样回传 "3"/"2"/"4"，车机 200 + status 0 |
| `/info` 的 `hidDevices[].uuid` 按原样用 | ✅ 我方解析 `map.get("hidDevices")→get("uuid")`，未做类型改写 |
| 事件通道是双向 RTSP，来向请求必须立即 200 | ⚠️ 车机从未发来向请求（`raw_bytes=0`），无从触发 |
| 屏幕流 SETUP 需真实 timingPort + 完整 features | ✅ 已满足（否则不会有画面） |

⇒ 社区经验里「手机侧要做的」我们**全部满足**；社区没有给出「车机不发」的解法，因为那属于接收端 HMI。

### 1.5 最终归因

```
✓ AirPlay：SETUP / INFO / SCREEN_ROUTE_OPENED / RECORD_ACK / 视频 19.9fps / 音频 1MB / feedback
✓ HID 命令：6 条 hidSetInputMode，事件通道 → 车机 200 → status=0 → HidOK
✗ 车机 iAP2 层：无 HID 组件（1D01 无 param 18/19/20）+ SEND count=0
✗ 车机 HMI：不调用 SendHIDReport
⇒ 触摸永远不会有
```

**这是车机侧 `WirelessCarPlayDevice` 未登记 / `Apple_CarPlay_enableWireless` 未启用 /
`Enable_Application_Apple_Carplay_CalID` 未开 的统一表现**（见 `v1851-carpaly-firmware-gate-analysis.md` 三道门）。
手机侧唯一还能做的是把 **iAP2-over-WiFi 反向通道**建起来（`p1_2_proactive_iap2`），已在本轮开启待验证。

> 诚实边界：若 P1-2 开启后 `NATIVE_MODULE_STATUS` 仍 `UNDECLARED`，则手机侧确实到硬边界，
> 只能走车机侧（删旧设备 → 彻底断电 → 重配对；确认车机无线 CarPlay 开关）。

---

## 二、关闭屏幕自动旋转（已实现并验证）

### 2.1 现状排查（先确认「是谁开的」）

| 检查项 | 结果 |
| --- | --- |
| 代码里 `Settings.System.putInt(ACCELEROMETER_ROTATION)` | **0 处** |
| `OrientationEventListener` / `Display.getRotation` / `setRequestedOrientation` | **0 处** |
| `AndroidManifest` 的 `screenOrientation` | **原本没有** |
| 系统开关 `settings get system accelerometer_rotation` | `1`（开着，**不是 App 改的**） |

⇒ 真实原因：**三个 Activity 都没声明方向**，系统按重力自由旋转 ⇒ 体感就是「自动旋转被打开」。
App 从未、也不会去改系统「自动旋转」开关。

### 2.2 改动

| 位置 | 内容 |
| --- | --- |
| `AndroidManifest.xml:49` | `MainActivity` 增加 `android:screenOrientation="portrait"` |
| `MainActivity.onCreate`（`super.onCreate` 之后） | `setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)` 兜底 + 判据日志 |
| 开关 | `carplayapk-debug / cp_f_lock_rotation`（默认 **true**）；置 false 恢复跟随系统 |
| 判据 | `CP_F_LOCK_ROTATION enabled=true orientation=PORTRAIT system_rotation_untouched=true`（logcat tag `CarPlayAPK`） |

**设计取舍**：只锁 App 自身方向，**不改系统「自动旋转」开关**（零权限、不影响用户其他场景）。
如需连系统开关一起关，需 `WRITE_SETTINGS` 特殊授权，属侵入性操作，需单独确认后再做。

### 2.3 验证（已实证，非推测）

```
adb shell dumpsys activity activities | grep requestedOrientation
  → requestedOrientation=SCREEN_ORIENTATION_PORTRAIT   ✅ 生效

APK 二进制校验：
  AXML 中 'screenOrientation'（UTF-16LE 池）命中 = True
  属性资源 ID 0x0101001e 出现 = 1 次                     ✅ 已写入 Manifest
```

---

## 三、构建与核验

- 备份：`.workbuddy/backup/java-20261113-180022/`
- `compileDebugJavaWithJavac` 27s → `clean` 48s → `exportApk` 18s，全 SUCCESSFUL
- 六项 **PASS**；dex 锚点 **19/19 OK**（新增 2 + 保持 17，MISS=0）
- 产物 `dist/Carplay-V1.8.55-debug.apk`（1,374,163 B）
- **已装包**：versionName=1.8.55 / versionCode=392，pid 20636，崩溃基线 21→21
- prefs 装包后完整保留：`p1_2_proactive_iap2=true`、`cp_c_guard_sec=0`、`iap2_tx_4e0e=false`、`wait_ctrl_connect=false`

---

## 四、下一步（只剩两件事）

| 优先级 | 事项 | 判据 |
| --- | --- | --- |
| P0 | 车机上电后跑一轮，看 P1-2 是否把 iAP2 反向通道建起来 | `P12_PROACTIVE_BUILT` ≥1 → **`NATIVE_MODULE_STATUS connect=` 不再是 UNDECLARED** → `CP_C_INBOUND_COMMAND>0` → `TOUCH_FORWARDED>0` |
| P1 | 车机侧操作（唯一能真正解锁触摸的） | 车机删除旧手机配对 → **彻底断电 ≥1min** → 重新添加；确认「无线 CarPlay / 无线投屏」为开 |

若 P0 全绿且 `TOUCH_FORWARDED>0` ⇒ 问题解决；
若 `NATIVE_MODULE_STATUS` 仍 `UNDECLARED` 且车机侧也确认开启 ⇒ 结论是这台 Info3.6 不向非原厂 App 开放 HID 反向通道，
手机侧再无可做项，应转「车机侧取证」（车机 logcat / 车机 HMI 设置项）。
