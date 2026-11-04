---
name: carplayapk-build-usb-debug
description: 从源码编译 CarplayAPK（Gradle + AGP）并在 Android 真机上用 USB 调试验证无线 CarPlay 链路。覆盖环境核对、构建出包、版本号与签名、adb 安装启动、应用内日志取证，以及发现 / 握手 / 视频 / 音频 / 触摸五条链路的判定与排障。当出现「编译失败」「打不出包」「装不上」「run-as 读不到日志」「投屏黑屏」「没有声音」「有异响」「触摸无反应」「连接一会儿就断」「升级后没反应」时使用。/ Build CarplayAPK from source with Gradle and verify the wireless CarPlay link on a real device over USB: environment checks, packaging, versioning and signing, adb install/launch, in-app log forensics, and verdicts for the five links (discovery, handshake, video, audio, touch).
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
gradlew.bat exportApk                   # 调试包 → dist/Carplay-V<版本>-debug.apk（debuggable=true）
gradlew.bat exportRelease               # 发布包 → dist/Carplay-V<版本>-release.apk（debuggable=false）
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

"$AAPT2" dump badging dist/Carplay-V1.7.61-debug.apk | head -3     # package / versionCode / versionName
"$APKSIGNER" verify --print-certs dist/Carplay-V1.7.61-debug.apk    # 签名是否有效
```

三样必须一致：`versionName` = 构建配置里的 `appVersionName`、`versionCode` = `appVersionCode`、
文件名 = `Carplay-V<versionName>-debug.apk` 或 `-release.apk`。三者不一致会让现场分不清装的是哪一版。
All three must agree — otherwise nobody can tell which build is on the phone.

---

## 3. 版本号与签名 / Versioning and signing

### 3.1 版本号只改一处 / Single source of truth

```kotlin
// android-project/app/build.gradle.kts
val appVersionName = "1.7.61"   // → dist/Carplay-V1.7.61-debug.apk / -release.apk
val appVersionCode = 298        // 每次发版 +1
```

`exportApk` / `exportRelease` 的文件名自动跟随，不需要手工改名。
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
adb install -r android-project/dist/Carplay-V1.7.61-debug.apk     # 需要 run-as 读日志就用 -debug

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
| 3 | 视频 / video | `NATIVE_VIDEO_FIRST_BYTES` 之后 `VIDEO_PROGRESS` 持续递增，≈20 fps |
| 4 | 音频 / audio | `AUDIO_MAIN_MODES_CONFIRMED` + `AUDIO_FLOW ... sent_packets_total` ≈**136 包/秒**、`dropped_frames_total=0` |
| 5 | 触摸 / touch | `HID_CAPABILITIES devices=4 touch=1` → `HID_TOUCH_RANGE ... descriptor_driven=true` → `TOUCH_FORWARDED` 递增 |

补充判据 / extra markers:

* `APP_BUILD version=1.7.xx code=xxx dex=SRC` —— 确认装的是源码构建的包。
  Confirms the installed APK came from source.
* `HID_INPUT_MODE_SENT` → `HID_INPUT_MODE_ACCEPTED` —— 车机接受了 HID 输入模式。
  The head unit accepted the HID input mode.
* `NATIVE_AUDIO_PROGRESS ... FIRST_VALID_PCM reader_connected=true` —— 音频读取线程活着。
  The audio reader thread is alive.

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

---

## 9. 脚本 / Scripts

| 脚本 / Script | 用途 / Purpose |
| --- | --- |
| `scripts/diagnose_adb.sh` | 链路一键体检：车机待机 → WiFi → 蓝牙 ACL → RFCOMM → 已保存网络 |
| `scripts/bt_flap_diagnose.sh` | 量化「车机反复敲门」的频率与周期 |
| `scripts/probe_event_channel.sh` | 事件通道取证：车机到底有没有往我们这边写字节 |
| `scripts/hid_event_verdict.sh` | 抓 HID/触摸日志并直接给结论 |
| `scripts/verify_iap2_frames.py` | 从日志核对 iAP2 帧序列是否完整 |
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
