# CarplayAPK

把别克（上汽通用 GM INFO3.6）车机的无线 CarPlay 接进 OPPO Car+ 车机投屏的分析材料与工具集。

这里没有可直接编译运行的 Android 工程，只有三类东西：**读懂协议栈用的 Java 源码**、
**读懂车机端实现用的固件提取物**、**排障用的技能与脚本**。

```
CarplayAPK/
├── java-project/    手机端 App（OcarPlay V1.7.29）反编译 + 语义重命名后的可读源码
├── firmware/        车机端固件（V167.3.1-GA）里与 CarPlay 相关的程序、库与配置
└── skill/           这套逆向工作沉淀下来的排障技能：协议笔记 + 判定脚本
```

---

## java-project — 手机端协议栈

来源：`Carplay-V1.7.29.apk`（versionCode 298），jadx 反编译后**按语义重命名 39 个类**，
并为每个重命名类写了中文职责注释。

```
java-project/
├── README.md        怎么读这份代码（先看这个）
├── RENAME-MAP.md    39 项「混淆名 ↔ 可读名」对照表，附 smali 路径
├── src/main/java/io/carplay/app/
│   ├── secured/     224 个类 —— 协议栈本体（RTSP、事件通道、配对、加密、编解码）
│   └── prototype/    52 个类 —— 业务层（Service、Activity、mDNS 广播、配对会话）
└── tools/           可重跑的生成脚本（重命名引擎、特征提取、文档生成）
```

按「一条触摸从车机回到手机」的顺序读：

| 可读名 | 原混淆名 | 职责 |
|---|---|---|
| `CarPlayCtrlAdvertiser` | `prototype.h` | 广播 `_carplay-ctrl._tcp`，让车机知道本机在等它 |
| `AirplayServiceProbe` | `prototype.j` | 发现车机 `_airplay._tcp`，选候选去连 |
| `AirPlaySession` | `secured.i0` | SETUP / INFO / RECORD / `POST /command` 全流程，4400 行 |
| `EventChannel` | `secured.y5` | 事件通道：车机推来的触摸、按键、`/command` |
| `ControlChannel` | `secured.hx` | 控制通道：邀请与 iAP2 |

**只改有证据的，不猜**：新名字的依据只有三类——类里的协议字符串常量、实现的框架接口、
已确认的实测结论。对不上任何一条的 248 个类保留混淆原名，因为给它们编名字比留着更有害。

工程**不能编译**（缺 `R` 类、`.so`、资源），它是用来读的，不是用来跑的。

---

## firmware — 车机端实现

从 `V167.3.1-GA` 固件包（18 个交付文件）中解包 `SOC_SYSTEM` / `SOC_VENDOR` 分区，
提取出与 CarPlay 相关的 43 MB / 73 个文件，按分区原始路径组织。

| 项 | 值 |
|---|---|
| 产品 | `gminfo36`（GM INFO3.6 座舱） |
| 版本 | `W40E-V167.3.1-MIHC21A-232` |
| Android | 6.0.1 / SDK 23 |
| 架构 | x86_64（Intel Apollo Lake） |

关键内容：

- `system/app/GMCarPlay/` —— CarPlay HMI（连接提示、授权弹窗、SurfaceView 承载画面），
  代码在 `oat/x86_64/*.odex` 里
- `system/lib64/libNme*.so` —— 车机端 AirPlay/CarPlay 原生栈（`libNmeCarPlay.so`、
  `libNmeAppleAuth.so`、`libNmeAudio*.so`、`libNmeVideoSW.so` …）
- `system/bin/carplay.sh`、`native_enableApple.sh`、`remote_display`、`vehicleinputservice`
  —— 启停与输入注入入口
- `system/etc/*.conf` —— 音频路由与车辆音频服务配置
- `analysis/` —— 从上述文件提取的字符串与清单（`*.strings.txt`、`inventory.csv`）

`firmware/README.md` 有完整的提取过程与文件级说明。

---

## skill — 排障技能

`skill/SKILL.md` 是这份工作的主体笔记：六十多条「铁律」，每条都对应一次踩过的坑，
外加协议笔记（RTSP 流程、iAP2 链路层、触摸与 HID、原生握手）和判定脚本。

```
skill/
├── SKILL.md         主笔记：架构、铁律、排障决策树、混淆名对照
├── references/      专题：airplay-pairing-spec / rtsp-flow / iap2-link-layer
│                    / native-carplay-handshake / touch-input-hid
│                    / android-pitfalls / troubleshooting
└── scripts/         判定脚本：mdns_probe / probe_event_channel / hid_event_verdict
                     / verify_dex_outs / mock_carplay_sink / sysdiag_strings_probe …
```

几条最有普适价值的结论：

- **日志有两条通道**：走 `Log.d` 的只进 logcat，只有走 App 自身回调的才落文件。
  拿「日志里没有」当「代码没执行」，会得出完全相反的结论。
- **建链成功 ≠ 握手成功**。TCP 连上只证明端口对；车机愿不愿意推事件，
  取决于 SETUP 协商出来的 `enabledFeatures`。
- **修好一个 bug 会激活下一个 bug**。让一条从未成功过的路径第一次成功时，
  必须复查它下游所有从未执行过的代码。
- **一次性门禁会把指标压扁**。用 `compareAndSet` 只放行第一条日志，
  统计出来的 0 不是「没发生」，是「没记录」。

---

## 免责声明

`firmware/` 下的程序、库与配置版权归原厂（上汽通用 / 神龙 / NME 等）所有，
此处仅作为**互操作性研究**的参考资料，不做分发、不用于商业用途。
如需公开传播，请自行评估合规风险，必要时把仓库转为私有。

`java-project/` 是对公开可下载 APK 的逆向分析结果，同样仅用于互操作性与排障研究。

代码与笔记部分（`skill/`、`tools/`）以 MIT 协议发布，见 `LICENSE`。
