# CarplayAPK

把别克（上汽通用 GM INFO3.6）车机的**无线 CarPlay** 接进 Android 手机的可编译工程，
外加车机端参考材料与一套「源码编译 + USB 调试」技能包。

A buildable Android project that makes an Android phone act as the wireless CarPlay sender for
a Buick / SAIC-GM INFO3.6 head unit, plus head-unit reference material and a
"build from source + debug over USB" skill package.

> 一句话：手机负责 AirPlay 发送端（画面/音频源），车机是接收端（解码显示）；
> 本项目在手机侧完整重建了建链、握手、投屏、音频与触摸五条链路。
>
> In one line: the phone is the AirPlay sender (screen/audio source) and the head unit is the
> receiver. This project reconstructs all five links on the phone side — discovery, handshake,
> video, audio and touch.

---

## 目录结构 / Repository layout

| 路径 / Path | 说明 / Description |
| --- | --- |
| `android-project/` | **可编译的 Android 工程**（Gradle + AGP），产出一个可安装的 APK<br>A **buildable Android project** (Gradle + AGP) producing an installable APK |
| `firmware/` | 车机端固件提取物（程序、原生库、配置），只作行为对照，不参与构建<br>Head-unit firmware extracts used for behaviour comparison only; not part of the build |
| `skill/` | 「CarplayAPK 源码编译 + USB 调试」技能包，中英对照<br>The "build CarplayAPK from source and debug over USB" skill, bilingual |
| `LICENSE` | MIT（仅覆盖本仓库自有代码，固件与第三方 APK 除外）<br>MIT, covering this repository's own code only |

---

## 1. 快速开始 / Quick start

**环境要求 / Requirements**

| 项 / Item | 版本 / Version |
| --- | --- |
| JDK | 17 或更高（Android Studio 自带 JBR 即可）/ 17+ (Android Studio's bundled JBR works) |
| Gradle | 9.4.1（wrapper 自带，走腾讯云镜像）/ bundled via wrapper, Tencent mirror |
| Android Gradle Plugin | 9.2.0 |
| compileSdk / targetSdk | 37 |
| minSdk | 33（Android 13）/ Android 13 |
| Build-tools | 36.0.0 |

**构建 / Build**

```bash
cd android-project

# 默认构建：不带参数执行 gradlew 只编 debug（根 build.gradle.kts 的 defaultTasks）
gradlew.bat                      # macOS / Linux: ./gradlew
# 等价于 → dist/Carplay-V1.7.71-debug.apk（debuggable = true）
gradlew.bat exportApk

# 以下两项必须显式调用，默认构建不会触碰
# Release APK → dist/Carplay-V1.7.71-release.apk（debuggable = false）
gradlew.bat exportRelease
# 发布用 AAB → dist/Carplay-V1.7.71-release.aab（不能 adb 安装）
gradlew.bat exportAab

# 仅编译校验（最快）
gradlew.bat compileDebugJavaWithJavac
```

> 构建策略：唯一默认产物是 debug；R8 全程关闭（`isMinifyEnabled = false`），
> `gradle.properties` 里 `android.builtInKotlin = false` 避免 Kotlin 运行时被打进 dex。
> Build policy: debug-only by default, no R8, and built-in Kotlin disabled.

首次构建会从腾讯云镜像拉取 Gradle 分发包；若内网限制，改
`gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` 即可。
The first build downloads the Gradle distribution from the Tencent mirror; point
`distributionUrl` elsewhere if your network requires it.

---

## 2. android-project — 手机端实现 / the phone-side implementation

### 2.1 功能范围 / Scope

| 能力 / Capability | 状态 / Status | 关键实现 / Key implementation |
| --- | --- | --- |
| mDNS 发现与广播 / discovery & advertising | ✅ | `prototype/CarPlayCtrlAdvertiser`、`prototype/AirplayServiceProbe` |
| 蓝牙 RFCOMM + iAP2 握手 / RFCOMM + iAP2 | ✅ | `prototype/PairingSession`、`secured/Iap2*` |
| WiFi 凭据协商与关联 / WiFi credential setup | ✅ | `secured/Iap2WirelessSetup`、`prototype/WifiNetworkMonitor` |
| AirPlay 建流（SETUP/INFO/RECORD）/ stream setup | ✅ | `secured/AirPlaySession` |
| 视频（H.264 编解码与投屏）/ video | ✅ ≈20 fps | `prototype/AvcEncoder`、`secured/VideoCipher`、`secured/DisplaySession` |
| 音频（48 kHz PCM 上行至车机）/ audio | ✅ 实时 | `secured/PcmQueue`、`secured/PcmConverter`、`secured/PcmAudioOutput`、`secured/AudioPacer` |
| 触摸与按键（HID）/ touch & keys | ✅ | `secured/HidDescriptorParser`、`secured/TouchGestureEncoder` |
| 会话保活与恢复 / keep-alive & recovery | ✅ | `secured/KeepAliveServer`、`secured/EventChannel` |

### 2.2 代码结构 / Source layout

```
android-project/app/src/main/java/           273 个源文件 / 60.0k 行
├── io/carplay/app/prototype/    43 个  业务层：界面、前台服务、配对编排、原生桥接
├── io/carplay/app/secured/     220 个  协议栈：AirPlay、iAP2、HID、加解密、媒体通道
└── org/bouncycastle/            10 个  随包编译的 Bouncy Castle 子集（Ed25519 / X25519 / ChaCha20-Poly1305）
```

> 业务层负责「什么时候做什么」，协议层负责「字节怎么走」。改协议行为先看 `secured/`，
> 改交互与生命周期先看 `prototype/`。
>
> The app layer decides *when* things happen; the protocol layer decides *how the bytes flow*.
> Touch `secured/` for protocol behaviour and `prototype/` for lifecycle and UI.

### 2.3 关键类 / Key classes

| 类 / Class | 行数 | 职责 / Responsibility |
| --- | --- | --- |
| `secured/AirPlaySession` | ~4.9k | AirPlay 会话主流程：SETUP / INFO / RECORD / `POST /command`，各通道的建立与拆除<br>main AirPlay session and channel lifecycle |
| `secured/DisplaySession` | ~2.3k | 视频帧 + 触摸事件编码成 CarLife 原生帧写入车机<br>encodes video frames and touch events into CarLife native frames |
| `secured/HidDescriptorParser` | ~830 | HID 描述符解析：按物理集合分配触摸 slot，提取 X/Y、Tip Switch、Contact ID<br>HID descriptor parsing with one slot per multitouch collection |
| `secured/EventChannel` | ~1.1k | 车机推来的触摸/按键/命令（`/command`、`/feedback`）<br>event channel for head-unit pushed touch, keys and commands |
| `secured/PcmAudioOutput` | ~360 | 按「16 B INIT + 4096 B DATA」写车机原生音频通道<br>native audio output framing |
| `secured/KeepAliveServer` | ~70 | UDP 保活，5 秒间隔（与车机看门狗等长会断链）<br>UDP keep-alive every 5 s |
| `prototype/WirelessCarPlayService` | ~4.3k | 前台服务：整条链路的生命周期编排<br>foreground service orchestrating the whole link |

### 2.4 版本号与产物 / Version and artifacts

版本号只有一处真源 / single source of truth —— `app/build.gradle.kts`：

```kotlin
val appVersionName = "1.7.71"   // → Carplay-V1.7.71-debug.apk / -release.apk / -release.aab
val appVersionCode = 308       // 每次发版 +1 / bump on every release
```

| 产物 / Artifact | 说明 / Notes |
| --- | --- |
| `dist/Carplay-V1.7.71-debug.apk` | **默认产物**，debug 变体，`debuggable = true`，可 `run-as` 读日志 |
| `dist/Carplay-V1.7.71-release.apk` | 显式 `exportRelease` 才产出，`debuggable = false`，同样用 debug 证书签发 |
| `dist/Carplay-V1.7.71-release.aab` | 显式 `exportAab` 才产出，包内 dex 压缩，交 Play 后按设备切分分发 |

> **两个变体的调试开关是分开的**：`-debug` 开调试，供现场用
> `adb shell run-as io.carplay.app cat files/connection-log-*.txt` 取日志；
> `-release` 关调试，`run-as` 会报 `package not debuggable`。
>
> The two variants differ on purpose: `-debug` keeps `debuggable=true` for `run-as`
> log access, while `-release` ships with `debuggable=false`.

### 2.5 注释与许可头 / Comments and license headers

每个 `.java` 文件顶部都有中英对照的协议头（MIT + 文件用途 + 所属子系统），
每个类型声明前都有中英对照的用途 Javadoc。这一层注释由脚本统一维护：

```bash
python tools/annotate_sources.py            # 幂等补齐，已标注的文件会跳过
python tools/annotate_sources.py --check    # 只报告未标注文件
```

> Every `.java` file carries a bilingual header (MIT, file purpose, subsystem) and every type
> declaration has a bilingual purpose Javadoc. The script above is idempotent.

源码含中文注释，工程已固定 `options.encoding = "UTF-8"`；移植到其他机器不会再出现乱码。
The build pins `options.encoding = "UTF-8"` so the Chinese comments cannot break other machines.

---

## 3. 真机调试 / On-device debugging

```bash
adb install -r android-project/dist/Carplay-V1.7.71-debug.apk
adb shell am start -n io.carplay.app/.prototype.MainActivity

# 应用内日志（不进 logcat，必须用 run-as 读）
adb shell run-as io.carplay.app cat files/connection-log-0.txt
```

**判断「真的通了」的日志标志 / markers that prove the link is healthy**

| 日志 / Log line | 含义 / Meaning |
| --- | --- |
| `APP_BUILD version=1.7.71 code=308 dex=SRC` | 装的是源码构建的包，不是旧外壳 / built from source |
| `SESSION START` → `TX CMD 0x18001` → `RX CMD 0x10004` | 建链与首轮握手 / link up and first handshake |
| `HID_CAPABILITIES devices=4 touch=1` | 触摸描述符被接受（`touch=0` 表示被拒）/ touch descriptor accepted |
| `HID_TOUCH_RANGE ... descriptor_driven=true` | 坐标映射已按描述符建立 / coordinate mapping ready |
| `TOUCH_FORWARDED=N` 递增 | 触摸确实转发出去了 / touches really forwarded |
| `VIDEO_PROGRESS units=...` 持续递增，≈20 fps | 视频在流 / video streaming |
| `AUDIO_FLOW ... sent_packets_total` ≈136 包/秒、`dropped_frames_total=0` | 音频实时且无丢帧 / audio real-time, no drops |

排障顺序与判定脚本见 `skill/`。
For the troubleshooting decision tree see `skill/`.

---

## 4. firmware — 车机端参考 / head-unit reference

从 `V167.3.1-GA` 固件包解出的 CarPlay 相关程序、原生库与配置（43 MB），
用于解释「车机为什么这样回」，**不参与构建**。
CarPlay-related binaries, libraries and configuration extracted from the `V167.3.1-GA`
firmware (43 MB). They explain *why the head unit answers the way it does*; they are **not**
part of the build.

| 项 / Item | 值 / Value |
| --- | --- |
| 产品 / Product | `gminfo36`（GM INFO3.6 座舱 / cockpit） |
| 版本 / Version | `W40E-V167.3.1-MIHC21A-232` |
| Android | 6.0.1 / SDK 23 |
| 架构 / ABI | x86_64（Intel Apollo Lake） |

重点目录：`system/app/GMCarPlay/`（HMI）、`system/lib64/libNme*.so`（车机端 AirPlay 栈）、
`system/bin/carplay.sh` 等启停入口、`analysis/`（字符串与清单）。
详见 `firmware/Buick Info3.6/README.md`。

---

## 5. skill — 编译与调试技能 / build & debug skill

`skill/SKILL.md` 是一份可执行的「从源码编出 APK 并在真机上验证」手册：环境核对、
构建、装机、日志取证、五条链路（发现 / 握手 / 视频 / 音频 / 触摸）的判定与会诊，
外加配套脚本。

`skill/SKILL.md` is an executable playbook for "build the APK from source and verify it on a
real device": environment checks, build, install, log forensics, verdicts for the five links
(discovery / handshake / video / audio / touch), plus helper scripts.

```
skill/
├── SKILL.md                 主手册（中英对照）/ main playbook (bilingual)
├── references/              专题：构建 / 装机 / 日志 / 视频 / 音频 / 触摸 / 排障
└── scripts/                 判定脚本（adb、日志、HID、音频速率、dex 校验）
```

---

## 6. 已知限制 / Known limitations

1. **Android 13+（minSdk 33）**：依赖较新的蓝牙与网络 API。
   Requires Android 13+ (minSdk 33) for the newer Bluetooth and network APIs.
2. **仅无线 CarPlay**：USB 有线 CarPlay 不在范围内。
   Wireless CarPlay only; wired USB CarPlay is out of scope.
3. **无原生 `.so`**：视频解码/显示由车机侧原生组件承担，本工程只负责发送端。
   No bundled native libraries; decoding/display happens on the head unit.
4. **debug / release 调试开关分离**：见 2.4 的说明。
   Debug and release differ on `debuggable` — see 2.4.

---

## 7. 免责声明 / Disclaimer

`firmware/` 下的程序、库与配置版权归原厂（上汽通用 / NME 等）所有，
此处仅作为**互操作性研究**的参考资料，不做分发、不用于商业用途。
The binaries, libraries and configuration under `firmware/` remain the property of their
respective vendors (SAIC-GM, Dongfeng PSA, NME, …). They are kept solely as interoperability
research material — not redistributed, not used commercially.

`apk/` 下的第三方安装包同理；仓库自有代码以 MIT 发布。
The third-party APKs under `apk/` are treated the same way. This repository's own code is MIT.

---

## 8. 许可 / License

自有代码（`android-project/`、`skill/`、脚本）以 **MIT** 发布，全文见 `LICENSE`。
Own code (`android-project/`, `skill/`, scripts) is released under the **MIT** license; see `LICENSE`.
