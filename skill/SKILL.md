---
name: carplayapk-build-usb-debug
description: 从源码编译 CarplayAPK（Gradle + AGP）并在 Android 真机上用 USB 调试验证无线 CarPlay 链路。覆盖环境核对、构建出包、版本号与签名、adb 安装启动、应用内日志取证，以及发现 / 握手 / 视频 / 音频 / 触摸五条链路的判定与排障。当出现「编译失败」「打不出包」「装不上」「run-as 读不到日志」「投屏黑屏」「没有声音」「有异响」「触摸无反应」「连接一会儿就断」「升级后没反应」「看不懂混淆方法名」「改了几版没效果」时使用。/ Build CarplayAPK from source with Gradle and verify the wireless CarPlay link on a real device over USB: environment checks, packaging, versioning and signing, adb install/launch, in-app log forensics, and verdicts for the five links (discovery, handshake, video, audio, touch). Also covers the obfuscated-symbol map for reading decompiled class/method names.
agent_created: true
---

# CarplayAPK 源码编译与 USB 调试

从 `android-project/` 编出 APK，装到真机，用日志判定链路走到了哪一步。

> Build the APK from `android-project/`, install it on a real device, and use the logs to decide
> how far the link actually got.

## 何时使用 / When to use

| 现象 / Symptom | 去哪一节 / Go to |
| --- | --- |
| 编译报错、打不出包 / build fails | §2 |
| 想改版本号、产物名 / bump version | §3 |
| 装不上、装完没反应 / install fails | §4 |
| `run-as` 读不到日志 / no logs | §4.2 §7 |
| 投屏黑屏 / black screen | §5 §7 |
| 没有声音、有异响 / no audio, noise | §5 §7 |
| 触摸无反应 / touch dead | §5 §7 |
| 看不懂 `Q2()` / `a1()` / `g2()` 等混淆名、按名字改错地方 / obfuscated names | §13 `references/obfuscation-map.md` |
| 连一会儿就断、重连后哑掉 / drops | §5 §7 |

不适用 / Out of scope：车机端固件修改、有线 USB CarPlay、iOS 侧行为分析。
Head-unit firmware changes, wired CarPlay and iOS-side analysis are out of scope.

## 0. 先定方向 / Direction first

```
手机 = AirPlay 发送端（画面与音频源）       → 本项目在这一侧
车机 = AirPlay 接收端（解码、显示、出声）   → 只做行为对照，不改
```

> The phone is the AirPlay sender and the head unit is the receiver. This project only touches
> the phone side; the head unit is treated as a black box that answers in observable ways.

链路上的推拉方向全部从这里推。搞反方向就会去改错的一侧。
All conclusions about who pushes what follow from this. Reversing it sends you to the wrong side.

---

## 1. 环境 / Toolchain

| 项 / Item | 值 / Value | 怎么核对 / How to check |
| --- | --- | --- |
| JDK | 17+（Android Studio 自带 JBR 即可） | `"$JAVA_HOME/bin/java" -version` |
| Gradle | 9.4.1（wrapper 自带，腾讯云镜像） | `cat gradle/wrapper/gradle-wrapper.properties` |
| AGP | 9.2.0 | `build.gradle.kts` |
| compileSdk / targetSdk | 37 | `app/build.gradle.kts` |
| minSdk | 33（Android 13） | 同上 |
| build-tools | 36.0.0（含 `aapt2` / `apksigner` / `dexdump`） | `ls $ANDROID_HOME/build-tools` |

Windows 下的固定写法：

```bash
cd D:/VSCode/CarplayAPK/android-project
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"
```

> Pin `JAVA_HOME` explicitly on Windows; the bundled Android Studio JBR is always a valid JDK.

---

## 2. 构建 / Build

```bash
cd android-project

gradlew.bat compileDebugJavaWithJavac   # 最快：只编译，验证语法与注释没写坏
gradlew.bat                             # 默认：只编 debug（= exportApk）
gradlew.bat exportApk                   # 调试包 → dist/Carplay-V<版本>-debug.apk（debuggable=true）
gradlew.bat exportRelease               # 显式：发布包 → dist/Carplay-V<版本>-release.apk（debuggable=false）
gradlew.bat exportAab                   # 显式：AAB → dist/Carplay-V<版本>-release.aab（不能 adb 安装）
```

macOS / Linux 把 `gradlew.bat` 换成 `./gradlew`。

> On macOS / Linux use `./gradlew`.

**判定成功**：`BUILD SUCCESSFUL` + `dist/` 下 APK 时间戳更新。
**Success looks like** `BUILD SUCCESSFUL` and a fresh APK in `dist/`.

### 2.1 构建常见故障 / Build failures

| 报错 / Error | 原因 / Cause | 处理 / Fix |
| --- | --- | --- |
| `R$drawable is defined multiple times` | 源码树里有手写的 `R.java`，与 AGP 生成的 R 冲突<br>a hand-written `R.java` collides with AGP's generated R | 删掉 `src/main/java/io/carplay/app/R.java`（Gradle 工程不需要）<br>delete that file |
| `Could not download gradle-9.4.1-bin.zip` | 分发包地址不通<br>distribution URL unreachable | 改 `gradle-wrapper.properties` 的 `distributionUrl`<br>repoint the URL |
| `unmappable character for encoding` | 源码含中文注释但编译编码不是 UTF-8<br>non-UTF-8 compile encoding | 确认 `app/build.gradle.kts` 里 `options.encoding = "UTF-8"` 生效<br>keep the pinned encoding |
| `Unsupported class file major version` | JDK 太旧 / `JAVA_HOME` 指错<br>wrong or outdated JDK | 显式 `export JAVA_HOME=<JBR 路径>` |
| `SDK location not found` | 缺 `local.properties` | 写入 `sdk.dir=...` 指向 Android SDK |

### 2.2 构建产物核对 / Verify the artifact

```bash
AAPT2="$ANDROID_HOME/build-tools/36.0.0/aapt2.exe"
APKSIGNER="$ANDROID_HOME/build-tools/36.0.0/apksigner.bat"

"$AAPT2" dump badging dist/Carplay-V1.7.71-debug.apk | head -3     # package / versionCode / versionName
"$APKSIGNER" verify --print-certs dist/Carplay-V1.7.71-debug.apk    # 签名是否有效
```

三样必须一致：`versionName` = 构建配置里的 `appVersionName`、`versionCode` = `appVersionCode`、
文件名 = `Carplay-V<versionName>-debug.apk` 或 `-release.apk`。三者不一致会让现场分不清装的是哪一版。
All three must agree — otherwise nobody can tell which build is on the phone.

---

## 3. 版本号与签名 / Versioning and signing

### 3.1 版本号只改一处 / Single source of truth

```kotlin
// android-project/app/build.gradle.kts
val appVersionName = "1.7.71"   // → dist/Carplay-V1.7.71-debug.apk / -release.apk / -release.aab
val appVersionCode = 308        // 每次发版 +1
```

`exportApk` / `exportRelease` / `exportAab` 的文件名自动跟随，不需要手工改名。
The export tasks derive the file name automatically.

### 3.2 签名 / Signing

* debug 与 release 都使用 `~/.android/debug.keystore`，因此**可以覆盖安装**历史包。
* **两个变体的调试开关分开**：`debug` 是 `isDebuggable = true`，`release` 是 `isDebuggable = false`。
  现场排障请装 `-debug` 包，`-release` 包 `run-as` 会报 `package not debuggable`。
* 对外分发时：换自有 keystore 后重新出包。

> The variants differ on purpose: `-debug` keeps `isDebuggable = true` for `run-as` log access,
> while `-release` ships with `isDebuggable = false`.

---

## 4. 装机与启动 / Install and launch

```bash
adb devices                                  # 先确认设备在线
adb install -r android-project/dist/Carplay-V1.7.71-debug.apk     # 需要 run-as 读日志就用 -debug

# 核对装的是哪一版
adb shell dumpsys package io.carplay.app | grep -E "versionName|versionCode"

# 启动主界面
adb shell am start -n io.carplay.app/.prototype.MainActivity
```

### 4.1 干净复现一次测试 / Clean test run

```bash
adb shell am force-stop io.carplay.app
adb shell run-as io.carplay.app rm -f files/connection-log-0.txt files/connection-log-1.txt \
    files/connection-log-2.txt files/connection-log-3.txt files/connection-log-4.txt \
    files/wireless-session-last.txt
adb shell am start -n io.carplay.app/.prototype.MainActivity
```

> **通配符坑**：`run-as ... rm -f files/connection-log-*.txt` 在部分 adb/shell 组合下会被转义吃掉，
> 结果一条都没删。逐条列文件名最稳。
>
> **Wildcard trap**: the glob is often swallowed by adb shell quoting and nothing gets deleted.
> List the files explicitly.

### 4.2 日志在哪 / Where the logs live

| 路径 / Path | 内容 / Content |
| --- | --- |
| `/data/data/io.carplay.app/files/connection-log-0.txt` … `-4.txt` | 应用内会话日志，按大小轮转 / rotated session log |
| `/data/data/io.carplay.app/files/wireless-session-last.txt` | 上一次无线会话摘要 / last wireless session summary |

```bash
adb shell run-as io.carplay.app cat files/connection-log-0.txt | tail -40
```

> **铁律**：这些日志**不进 logcat**。`adb logcat` 里看不到，不等于没发生。
> 反过来，`Log.d` 的内容也不会落文件。两条通道互不覆盖。
>
> **Hard rule**: these logs never reach logcat. "Not in logcat" does not mean "did not happen",
> and `Log.d` output never lands in the file. Two separate channels.

---

## 5. 五条链路的判定 / Verdicts for the five links

按顺序看，**前一条不通就不用看后一条**。
Read them in order — if an earlier link is broken, later ones are meaningless.

| # | 链路 / Link | 通过标志 / Proof of success |
| --- | --- | --- |
| 1 | 发现与广播 / discovery | 广播注册成功、车机主动来连 / the head unit connects by itself |
| 2 | 蓝牙 + iAP2 握手 / handshake | `RFCOMM_SELECTED_PEER_ACCEPTED` → `TX CMD 0x18001` → `RX CMD 0x10004` → `TX CMD 0x18048` → `RX CMD 0x10049` → `TX CMD 0x1806b` → `RX CMD 0x1006c` → `RX CMD 0x1006e` → `TX CMD 0x18007` → `RX CMD 0x10008` → `TX CMD 0x18009` |
| 2.5 | iAP2 识别 + CarPlay 启动门控 / identification & CarPlay start gate | `IAP2_IDENTIFICATION_EA` 出现 → 车机 `IAP2_PEER_MESSAGE_IDS direction=SEND` 含 `17153` → `waiting=CARPLAY_START_SESSION` 归零 |
| 3 | 视频 / video | `NATIVE_VIDEO_FIRST_BYTES` 之后 `VIDEO_PROGRESS` 持续递增，≈20 fps |
| 4 | 音频 / audio | `AUDIO_MAIN_MODES_CONFIRMED` + `AUDIO_FLOW ... sent_packets_total` ≈**136 包/秒**、`dropped_frames_total=0` |
| 5 | 触摸 / touch | `HID_CAPABILITIES devices=4 touch=1` → `HID_TOUCH_RANGE ... descriptor_driven=true` → `TOUCH_FORWARDED` 递增 |

补充判据 / extra markers:

* `APP_BUILD version=1.7.xx code=xxx dex=SRC` —— 确认装的是源码构建的包。
  Confirms the installed APK came from source.
* `IAP2_IDENTIFICATION_EA ... to_send=… to_receive=…` —— 手机在 `0x1D00` 声明了非空 EA 能力列表
  （V1.7.73 起），是车机愿意发 `CarPlayStartSession(17153)` 的前提。
  The phone declared a non-empty EA list in `0x1D00`, a prerequisite for the head unit to send `17153`.
* `CARPLAY_START_WIFI_TOPLEVEL_FIELDS` / `CARPLAY_START_WIFI_LAYOUT` —— 车机 `0x4301` 的真实字段布局
  （嵌套 vs 扁平），复现后用于确认 WiFi 凭证能否取到。
  The head unit's actual `0x4301` field layout (nested vs flat).
* `HID_INPUT_MODE_SENT` → `HID_INPUT_MODE_ACCEPTED` —— 车机接受了 HID 输入模式。
  The head unit accepted the HID input mode.
* `NATIVE_AUDIO_PROGRESS ... FIRST_VALID_PCM reader_connected=true` —— 音频读取线程活着。
  The audio reader thread is alive.

> 链路 2.5（iAP2 识别 + CarPlay 启动门控）是 V1.7.73/1.7.74 新打通的关键段：此前手机 `0x1D00` 空 payload
> 导致车机永不发 `17153`，会话卡在 `waiting`。完整协议与修复见 **`references/iap2-carplay-flow.md`**，
> 对应排障案例见 `references/troubleshooting.md` **案例 7**。
>
> Link 2.5 (iAP2 identification + CarPlay start gate) was the key previously-broken segment: an empty
> `0x1D00` payload made the head unit never send `17153`. See `references/iap2-carplay-flow.md` and
> troubleshooting case 7.

**不要拿写死的字符串当结论** / never trust hard-coded strings:
`local_flush_only=true`、`native_app_response_observed=false`、`playback_confirmed=false`、
`pcm_paused=true` 都是源码里的**字面量**，不反映运行时状态。只看计数与状态迁移。
Those are literal strings in the source, not runtime state. Only counters and transitions count.

---

## 6. 数值基线 / Numeric baselines

| 量 / Quantity | 正确值 / Correct | 说明 / Note |
| --- | --- | --- |
| 视频帧率 / video fps | ≈19.9 | 显著低于此值先查编码与链路 / much lower means encoder or link trouble |
| 音频包率 / audio packet rate | ≈136 包/秒 | 352 帧/包 @48 kHz；上万包/秒 = 同一块 PCM 被反复重发<br>352 frames/packet; tens of thousands means the same buffer is resent |
| 音频丢帧 / audio drops | `dropped_frames_total` 不增长 | 每 5 秒 +24 万 = 整条流被丢弃<br>+240k per 5 s means the whole stream is dropped |
| UDP 保活间隔 / keep-alive | **5 秒** | 与车机看门狗等长会必然断链（10 秒是错的）<br>matching the watchdog interval guarantees a drop |
| 触摸坐标范围 / touch range | 随描述符变化 | 看 `HID_TOUCH_RANGE` 的 `logical_x/y` 与 `display`<br>read it from `HID_TOUCH_RANGE` |

---

## 7. 症状 → 判定 → 动作 / Symptom → verdict → action

| 症状 / Symptom | 先看 / Check first | 结论与动作 / Verdict and action |
| --- | --- | --- |
| 装完没反应、`run-as` 报 not debuggable | `aapt2 dump badging` 的 `application-debuggable` | 装的是 `-release` 包 → 换装 `-debug` 包再排障 |
| 投屏黑屏 / black screen | `VIDEO_PROGRESS` 有没有 | 有递增但屏黑 → 车机侧显示；完全没有 → 握手没走完，回到 §5 第 2 条 |
| 握手停在某条 CMD | 最后一条 `TX/RX CMD 0x…` | 对照 §5 第 2 条的完整序列，缺哪一步就查那一步的响应 |
| 没有声音 / no audio | `AUDIO_FLOW` 的 `sent_packets_total` | 恒 0 且 `source_pcm_observed=true` → 发送线程没起来；重启 App 若恢复，说明是**重连恢复路径**没重启音频线程 |
| 异响、无法关闭 / noise | `sent_packets_total` 每秒涨幅 | 上万包/秒且丢帧暴涨 → 同一块 PCM 被无限重发（发送循环少了跳出），查发送循环控制流 |
| `POST /command` 恒 422、空 body / command rejected | 请求体是否含 `params` | §14 + `references/troubleshooting.md` 案例 9；Cinemo 固件按 `{type, params}` 解析，规范写顶层 |
| 触摸无反应 / touch dead | `HID_CAPABILITIES` 的 `touch=` | `touch=0` → 描述符被拒，查 `HID_TOUCH_UNSUPPORTED` 的 reason；`touch=1` 但无 `TOUCH_FORWARDED` → 车机没发报告或映射错 |
| 触摸有效但坐标偏 / offset | `HID_TOUCH_RANGE` | 逻辑范围与 `display` 尺寸对不上 → 检查坐标缩放（必须用浮点，不能整数除法） |
| 一帧后卡死闪退 / freeze | `adb bugreport` 里的 `FS/data/anr/` | 主线程被阻塞式 socket 读卡住（临界区内做阻塞 IO）→ 把阻塞调用移出锁 |
| 连接固定时长后断 / periodic drop | 保活间隔 | 5 秒是硬要求；同时确认事件通道每个来向请求都立即回 200 |
| 重连后画面有、音频/触摸哑掉 | 各子系统是否按会话重建 | 恢复路径必须重建所有线程，不能复用上一会话对象 |

> 排障顺序永远是：**先证明上游通，再怀疑下游**。跳过 §5 的顺序去调下游，一定白做。
>
> Always prove the upstream link before touching downstream code.

---

## 8. 硬性规则 / Hard rules

1. **日志有两条通道**：`Log.d` 只进 logcat，应用内日志只落文件。互相不能当证据替代。
   Two log channels; neither substitutes for the other.
2. **建链成功 ≠ 握手成功**。TCP 连上只证明端口对；车机愿不愿意推事件取决于 SETUP 协商结果。
   A TCP connect only proves the port; willingness depends on the SETUP negotiation.
3. **修好一个 bug 会激活下一个**。让一条从未成功过的路径第一次成功时，必须复检它下游所有从未执行过的代码。
   Making a never-working path work for the first time exposes everything downstream.
4. **一次性门禁会把指标压扁**。只放行第一条日志的 `compareAndSet`，统计出的 0 是「没记录」，不是「没发生」。
   One-shot gates turn "not recorded" into "did not happen".
5. **别用写死字符串判断状态**，只看计数、状态迁移与真机现象。
   Judge by counters, transitions and real behaviour, never by literal log strings.
6. **不要给混淆标识符改名**：反射与协议常量依赖原始类名，两个变体都不开 R8。
   判读按符号名（不按行号）；改一道门前先 `grep` 找齐所有同类判断。见 `references/obfuscation-map.md`。
   Never rename obfuscated identifiers; key by symbol name, not line number, and find every sibling gate.
7. **协议以真机固件为准，不以规范文本为准**：Buick Cinemo SDK 的 `/command` 要 `{type, params}`，
   与 Apple CarPlay 规范 Table 23-71 的顶层键不一致；见 §14。
   When the spec and the firmware disagree, trust the firmware.
8. **`>>> EXECUTE` 只代表任务提交成功，不代表请求已发出**：判据必须是下游
   `HID_SEND_ENTER` → `HID_CMD_REQ` → `HID_INPUT_MODE_SENT` → `HID_CB`。
   共享线程池被常驻循环占满时，`execute()` 成功但任务永远排队（见案例 10）。
   "Submitted" is not "sent": require the downstream send/response counters.
9. **抓日志前先核对设备版本**：`adb shell dumpsys package io.carplay.app | grep versionName`。
   `dist/` 里出了新包 ≠ 手机装了新包；版本错配时该轮日志全部作废。
   Verify the installed version before interpreting any capture.
10. **HTTP 200 只代表请求被受理，不代表功能已启用**：Cinemo 固件的 `hidSetInputMode`
    分支（0x44559 → 0x44624）只是把命令转发给上层回调 `[r14+0x48]`；
    判据必须看下游产物 `hidSendReport` / `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED`。见案例 11。
    HTTP 200 means "accepted and dispatched", not "feature active".

---

## 9. 脚本 / Scripts

| 脚本 / Script | 用途 / Purpose |
| --- | --- |
| `scripts/diagnose_adb.sh` | 链路一键体检：车机待机 → WiFi → 蓝牙 ACL → RFCOMM → 已保存网络 |
| `scripts/bt_flap_diagnose.sh` | 量化「车机反复敲门」的频率与周期 |
| `scripts/probe_event_channel.sh` | 事件通道取证：车机到底有没有往我们这边写字节 |
| `scripts/hid_event_verdict.sh` | 抓 HID/触摸日志并直接给结论 |
| `scripts/verify_iap2_frames.py` | iAP2 帧格式自检 + 握手字段回环验证 + **0x1D00 EA 列表编码对齐 V1.7.73 bEaPayload()** |
| `scripts/verify_wifi_config.py` | 核对下发的 WiFi 配置与关联结果 |
| `scripts/mdns_probe.py` | 绕过 App 直接查车机是否在广播 CarPlay 服务 |
| `scripts/mock_carplay_sink.py` | 假车机（接收端），不发车也能验发送侧 |
| `scripts/verify_built_apk.sh` | 构建产物核对：版本、签名、可调试标志、dex 里是否含本次改动 |

---

## 10. 交付纪律 / Delivery discipline

* 临时截图、`uiautomator` 的 `ui.xml`、反汇编临时目录**用完即删**，结论用文字汇报。
* 汇报要说清：装的是哪个版本、五条链路各自状态、失败计数、下一步动作。
* 不把「猜测」写成「结论」：没有日志或计数支撑的判断，标注为待验证。

> Temporary screenshots, `ui.xml` dumps and scratch directories are deleted right after use;
> findings are reported as text. Never present a guess as a conclusion — mark it as unverified.


---

## 11. _carplay-ctrl TXT 键值规则 / TXT record rules

当前实现（`V1.7.71 / 308`）的完整规则表、证据与变更史见 **`references/txt-rules.md`**。要点：

| 键 | 值 |
| --- | --- |
| `deviceid` / `btaddr` / `id` | 手动填的本机蓝牙 MAC（同一个值） |
| `model` / `manufacturer` | `iPhone15,3` / `Apple Inc.` |
| `srcvers` / `osvers` | `27.2` / `1.0` |
| `protovers` | **`1.1`**（V1.7.70 为 `1.0`） |
| `features` | **`0x445F8A00,0x1C340`**（V1.7.70 为基础位域 `0x30e44`） |
| `flags` | `0x4` |

* 共 **10 键**；改值只改 `ControlChannel` 的 `TXT_*` 常量，`V170_TXT_FILLED n=10` 走 logcat tag `CarPlayAPK`。
* **服务实例名** = `ServiceName()`（手动蓝牙名，空则 `CarPlayAPK`），**不加 `iPhone_` 前缀**。
* **三个 `features` 不能混填**：TXT 是十六进制位域、AirPlay SETUP plist 是 14 项能力名 `List`
  （交集语义）、车机 `/info` 响应是 BigInteger 位掩码。带 `deviceID/name/model/features` 的
  会话 SETUP 是**手机发、车机解析**，车机发、手机解析的是 SETUP 响应与 `/info` 响应。
* 这套取值**未在真车验证**（iOS sysdiagnose 没打印 TXT 内容），别把「按规则填了」写成「车机认了」。

---

## 12. iAP2 识别与 CarPlay 启动门控 / iAP2 identification & CarPlay start gate

完整协议流程、`0x1D00`(7424) EA 列表修复（V1.7.73）、`0x4301`(17153) WiFi 凭证解析加固（V1.7.74）、
诊断标记与验收流程见 **`references/iap2-carplay-flow.md`**。

要点速记 / quick facts:

| 项 | 值 |
| --- | --- |
| 手机→车机 识别消息 | `0x1D00` (7424) StartIdentification，payload 含 EA 列表（index6=to-send / index7=to-receive） |
| 车机→手机 识别消息 | `0x1D01` (7425) IdentificationInformation；`a0()` 解析 index6/7 为 `this.m`(SEND)/`this.l`(RECEIVE) |
| CarPlay 可用 | `0x4300` (17152) 手机→车机 |
| **CarPlayStartSession** | **`0x4301` (17153) 车机→手机，带 WiFi 凭证（SSID/Pass/Security）** |
| 车机发 17153 的前提 | 手机 `0x1D00` 的 to-receive 含 17153 → 车机 SEND 列表含 17153 → `R()` 转 true |
| V1.7.73 修复 | `x(7424, bEaPayload())` ×3（782/1412/1438 行）；`43520` 独立 auth 消息不塞 EA 列表 |
| V1.7.74 加固 | `Iap2Csm.y()`：扁平布局兜底 + `CARPLAY_START_WIFI_*` 诊断 + identifier NPE 防护 |
| 关键判据 | `IAP2_IDENTIFICATION_EA` → 车机 `SEND` 含 17153 → `waiting` 归零 |

* 该链路是 V1.7.73/1.7.74 新打通的，对应排障案例见 `references/troubleshooting.md` **案例 7**。
* 以上编码与门控来自**社区抓包 + 固件逆向**，尚未在 myBuick GS 真车端到端验证；V1.7.74 为诊断包，
  待复现抓取后对照 `references/iap2-carplay-flow.md §4` 的标记确认车机确实发出 17153。

---

## 13. 混淆符号读法 / Reading obfuscated symbols

本工程是反编译产物：`265` 个 java 文件、`155` 个 ≤3 字符短类名、数千个短方法名。
**排障第一步不是猜名字，而是查字典**：

**`references/obfuscation-map.md`** —— 混淆符号语义字典（本轮踩坑固化）：

| 节 | 内容 |
| --- | --- |
| §0 | 为什么不重命名标识符（硬性规则：反射 / R8 / 源码备份对照） |
| §1 | 短类名、单字母方法、`Q2/W2/a1` 编号族的判读规律 |
| §2 | 核心符号速查：混淆名 → **建议语义名** → 职责 → 文件 → 踩坑证据（6 个子系统分组） |
| §3 | 14 条「因混淆名误判踩过的坑」：误判 → 真相 → 修法 → 版本 |
| §4 | 判读纪律（符号名优先于行号、找齐同类门、两条日志通道…） |
| §5 | 维护规则 |

源码关键锚点以 `[OBF-MAP]` 注释互链：
`grep -rn "\[OBF-MAP\]" android-project/app/src/main/java`

要点速记 / quick facts:

* `SyntheticRefs.c()` **不是日志**，是 `throw new IOException`；全类没有日志函数。
* `h()` / `K2()` 是原生源管线；`J2()` → `AirPlaySession.t1()` 才是 PCM 音频注入。
* `Iap2Csm.q()` 与 `v()` 是两份独立 TLV 解析，修重复 TLV 必须两处同步。
* 音频 `a1()` 中 `d.g == 100` 有**三道**同类门，只放行一处时包会凭空消失。
* `g2()` 的 422 必须容忍，否则 `RECORD` 与 `H1()`(HID) 永不执行。
* 判据写全：`V160_CTRL_TCP_ACCEPT`（源码里**没有** `CTRL_TCP_ACCEPT` 这个短名字面量）。
* **行号会漂移**，一律 `grep -n "<方法名>(" 文件`；**局部变量名不进 dex**，核验 APK 用字符串常量 / 方法名。

> 「完善混淆的类和变量名」的正确落地是**补语义字典 + 源码注释**，不是批量 `sed` 重命名（§0）。
> Enriching obfuscated names means adding a semantic map and comments, never mass renaming.

对应排障案例见 `references/troubleshooting.md` **案例 8**。

---

## 14. `/command` 协议：规范 vs Cinemo 固件 / Command envelope mismatch

**症状**：`/setup`、`/feedback` 都 200，唯独所有 `POST /command` 恒 422 且响应体为空；
视频/音频正常但车机从不发 `hidSendReport`，触摸无反应。

### 结论（V1.8.20，固件字节码证据）

Buick Info3.6 用 Cinemo SDK（`libNmeCarPlay.so`，**x86-64 ELF**；第一个 LOAD 段 vaddr=0 ⇒ VA=文件偏移）。
`_requestProcessCommand`(0x42150) 从请求体取 **`type`**(0x421cf) 与 **`params`**(0x421f3)，
再调 `AirPlayReceiverSessionControl`(0x443d0)：

| 地址 | 逻辑 |
| --- | --- |
| `0x42206` | delegate `[r12+0x10]` 为空 → 454 |
| `0x42232` | `call 0x443d0` |
| `0x4223d` | 返回非零 → `jne 0x422b5` |
| `0x422b5` | `mov ebp,0x1a6`（422），**跳过 `0x4227a` 响应体构造 ⇒ bodyLen=0** |
| `0x4441b` | `test r13,r13`（params）→ `0x4441e je 0x4464a` 返回错误 |
| `0x447d0` | 从 params 取 `appStates`(0x4480c) / `resources`(0x44991) |

`0x443d0` 的**入站命令白名单**只有 5 个：`modesChanged`、`requestUI`、`updateFeedback`、
`hidSetInputMode`、`iAPSendMessage`。`setUIContext`、`setNightMode` 都不在白名单；
`setNightMode` 是**车机→手机**方向（xref 0x4a714），拿它当探针永远 422，
会误导成「端点级整体拒绝」（V1.8.14~16 的错误结论，已作废）。

### 规范与实现的偏差

| 来源 | `modesChanged` 请求体 |
| --- | --- |
| Apple CarPlay 规范 Table 23-71（PDF p.480） | 顶层 `type` + `appStates[]` + `resources[]` |
| 社区 AirPlay 2（Shairport Sync / Apple `AirPlayReceiver.framework`） | 键是 `type` 的兄弟节点，不嵌在 params |
| 社区旧 AirPlay（`ap2-receiver.py` 的 `handle_command`） | 直接读 `plist["params"]` |
| **Buick Cinemo SDK（本项目真机）** | **`{type, params:{resources:[{resourceID,entity}]}}`** |

V1.7.86 依规范把 11-05 备份里的 `{type, params}` 改成顶层键，是一次**回归**：
params 恒空 ⇒ 422 ⇒ 屏幕所有权不转移 ⇒ 规范 §23.3.5.1.3「手机不拥有屏幕时车机不得发 HID 用法」
⇒ 车机永不发 `hidSendReport` ⇒ 触摸无反应。

### 修法（V1.8.20，已出包）

1. `modesChanged`：`{"type":"modesChanged","params":{"resources":[{"resourceID":1,"entity":1}]}}`
2. `hidSetInputMode`：`{"type":"hidSetInputMode","params":{"hidInputMode":N,"uuid":...}}`
3. `setUIContext` → `requestUI`（白名单内，`params.url`）
4. 探针 `setNightMode` → `updateFeedback`（白名单内、无槽位检查、无副作用）

**验收**：`CONTROL_COMMAND_SEND ... keys=[type, params]` 且 `CONTROL_RESP_STATUS status=200 op=COMMAND`；
随后 `HID_INPUT_MODE_ACCEPTED > 0` → `HID_REPORT_ARRIVED` → `TOUCH_FORWARDED` 递增。

> 排查纪律：**先确认命令名在固件白名单内，再谈载荷**。用不在白名单的命令做探针，
> 得到的 422 不能证明端点状态。

对应排障案例：`references/troubleshooting.md` **案例 9**。
