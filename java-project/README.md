# OcarPlay V1.7.29 — 可读版 Java 工程

把 `Carplay-V1.7.29.apk`（versionCode 298）反编译后，按**语义**重命名类名得到的
分析用源码工程。目的是让人能顺着 CarPlay 的连接链路读代码，而不是在一堆
`i0` / `y5` / `hx` 里迷路。

## 当前状态

| 项 | 数值 |
|---|---|
| Java 源文件 | **287** 个（io.carplay 276 + bouncycastle 兼容层 11） |
| 已语义化重命名 | **118** 个类（107 顶层 + 11 嵌套） |
| 已分析、暂缓改名 | **0** 个（见 RENAME-MAP.md 末节） |
| 保持混淆原名 | 169 个（无特征的工具/日志类） |
| `javac -source 8 -target 8` | **0 error**（287 个文件全过） |

## 这份代码是怎么来的

分两阶段改名，原因是「重新 jadx」会退化：

**阶段一（39 个类）**：早期在 smali 描述符层改名后重新 jadx。

```
Carplay-V1.7.29.apk
   │  apktool d -f -r           反编译成 smali（只取代码）
   ▼
smali（全混淆名）
   │  tools/dex_rename.py       按字节码描述符改名：Lpkg/Cls; → Lpkg/New;
   ▼
smali（已改名）
   │  apktool b                 重新打包
   │  jadx --no-res             反编译成 Java
   ▼
src/main/java/...               （本工程）
```

**阶段二（新增 20 个顶层类）**：实测「已改名 apk 再 jadx」的产物比阶段一更差
（丢 `throws IOException` 子句、方法签名漂移，实测 182 个编译错误）。因此改为
**在阶段一已 0-error 的 Java 树上做机械改名**：

```
阶段一 0-error 树
   │  tools/apply_names_to_tree.py   顶层类改名 + 引用改名（包感知、类型位限定）
   │  tools/fix_type_shadow.py       嵌套类型遮蔽（仅补跑尚未应用的部分）
   │  tools/fix_jadx_defects.py      修反编译缺陷
   │  tools/fix_obscured_types.py    按 javac 报错补限定名（迭代收敛）
   ▼
最终 0-error 树
```

**为什么顶层类可以在 Java 层改，单字母类却要格外小心**

`x` / `s` / `j` 既是顶层类名，又是局部变量/字段名。`apply_names_to_tree.py`
只在这几种**类型位**改名，且按包过滤：

- `class OLD` 声明、构造器 `public OLD(`、`extends/implements OLD`、`new OLD(`；
- 「类型后接标识符」`OLD var`；
- 全限定引用 `io.carplay.app.secured.OLD`（含 `import`）；
- 「裸名是否指本类」：同包，或本文件 `import` 了本类全限定名；不同包又没 import
  时，裸名是该包自己的同名类（`prototype.x` ≠ `secured.x`），**绝不改**；
- 裸嵌套类型访问 `OLD.member` 只认嵌套类/枚举形态（成员大写，或单个小写字母），
  绝不碰 `localVar.method(...)` 这种局部变量调用。

所有脚本都在 `tools/` 下，可重跑：

```bash
python tools/apply_names_to_tree.py --src <树>     # 阶段二：顶层类机械改名
python tools/rebuild_74.py --run --target <树>     # 阶段二：完整 fix 链 + 迭代收敛
python tools/compile_check.py --src <树>           # 只做 javac 检查（0 error 验收）
python tools/gen_docs.py                           # 重新生成本文档与 RENAME-MAP.md
```

## 目录

```
src/main/java/io/carplay/app/
├── secured/     224 个类 —— 协议栈本体（RTSP、事件通道、配对、加密、编解码）
└── prototype/    52 个类 —— 业务层（Service、Activity、mDNS 广播、配对会话）
```

两个包名**故意保留原样**：包不影响阅读，改了要同步一堆 import，收益低风险高。

## 先读哪几个类

按「一条触摸从车机回到手机」的顺序：

| 顺序 | 类 | 原混淆名 | 干什么 |
|---|---|---|---|
| 1 | `CarPlayCtrlAdvertiser` | `prototype.h` | 广播 `_carplay-ctrl._tcp`，让车机知道这台手机在等它 |
| 2 | `AirplayServiceProbe` | `prototype.j` | 发现车机 `_airplay._tcp`，选一个候选去连 |
| 3 | `AirPlaySession` | `secured.i0` | SETUP / INFO / RECORD 全流程，4400 行，App 的心脏 |
| 4 | `EventChannel` | `secured.y5` | 事件通道：车机推过来的触摸、按键、`/command` 全走这 |
| 5 | `ControlChannel` | `secured.hx` | 控制通道：邀请与 iAP2 |

完整对照见 `RENAME-MAP.md`。

## 命名原则（重要）

**只改有证据的，不猜。**

每个新名字都有可核对的依据，写在 `RENAME-MAP.md` 的「命名依据」列里，来源只有四类：

1. **类里的协议字符串常量** —— 如 `_carplay-ctrl._tcp`、`application/pairing+tlv8`、`X-Apple-Client-Name`
2. **它实现的框架接口** —— 如 `implements NsdManager$ResolveListener`
3. **已经确认的分析结论** —— `docs/v1724-*.md`、`v1728-*.md`、`v1729-*.md`
4. **Car+ 反混淆词表的协议串签名匹配**（阶段二新增 20 项的主要来源）

对不上任何一条的类**保留原名**，因为它们大多是各模块的日志拼接辅助类，字符串里
全是 `StringBuilder`，没有可判定的身份特征。**给它们编个名字比保留混淆名更有害。**

## 编译现状

用 `android-36/android.jar` + `bcprov-jdk18on-1.79.jar` 做 classpath，
`javac -source 8 -target 8 -nowarn` 的结果：**287 个文件，0 error**。

达成路径（`tools/rebuild_74.py` 按此顺序执行）：

1. `annotate` 注入中文职责注释；
2. `fix_jadx_defects` 修 `??` 占位符、畸形 `for`、`R.drawable` 归属；
3. `fix_type_shadow` 修嵌套类型与同名字段遮蔽（幂等：已应用则跳过）；
4. `fix_missing_catch_var` / `fix_catch_param_shadow` / `fix_varnames`；
5. 迭代「编译 → `fix_obscured_types` 补限定名 → 编译 → `fix_field_shadow`」直至收敛。

**0 error ≠ 能直接打成可运行 APK**：仍缺 Android SDK 的 `R` 类、`.so`、资源文件。

其余已知限制：

- **字段名和局部变量名仍是混淆的**（`a`、`b`、`f0`…）。可读性靠**类名 + 类注释
  + 关键常量值**承担。
- **11 个嵌套类暂未改名**（`AvcVideoInput`、`HidTouchParser`、`PcmFormat` 等）。
  它们与同名字段/方法/局部变量大量共存，Java 文本层无法可靠区分；名称已分析确认，
  记录在 `rename_map.CORE` 与 `RENAME-MAP.md`，待后续用 smali 描述符级改名落地。

## 和 skill / docs 的关系

- `~/.workbuddy/skills/ocarplay-carplay-connect/SKILL.md` 里的铁律大量引用
  混淆名（`i0.t0`、`y5.z()`），本工程的 `RENAME-MAP.md` 提供双向对照。
- 版本演进与每次改动的判据在 `../CarPlay-Build/docs/v17xx-*.md`。

## V1.7.30 变更（2026-11-03）

### 版本规则（后续迭代沿用）

- 版本号形如 `1.7.NN`（两位序号），文件名 `Carplay-V1.7.NN.apk`；
- 清单 `versionName` 必须与文件名一致；
- 一条命令出包：`python CarplayAPKBuild/tools/release.py 1.7.31`
  （内部：改清单版本串 → 编 dex → 置换 → zipalign → 签名）；
- 基线外壳固定 `apk/Carplay-V1.7.29-fix2.apk`，只改清单里的版本字符串（等长二进制替换，
  见 `tools/bump_version.py`），不动资源、不动签名结构。

### 事件通道保活（真机「约 1 分钟投屏中断」根因）

日志证据：7 次建链全部在 **10.3~10.6s** 被车机 FIN，`raw_bytes=0 messages=0`，
说明车机一个字节都没收到；App 把每次 EOF 升级为整会话重认证，3 次重试耗尽后
第 4 次断开，合计 62 秒。视频侧 `VIDEO_FLOW sent_fps_x100=1996 queue_depth=0` 一直健康。

修复：
- `EventChannel.sendFeedback()`（原 `h()`）：按 AirPlay 约定在 event 连接周期发
  空体 `POST /feedback`，登记进 `pendingCommands`；
- `EventChannel.handleEventResponse()`（原 `c()`）：**未知 CSeq 静默丢弃**，
  并补上 `pendingCommands` 的配对。原实现抛 `IOException`，读线程随即
  `close()` 整条事件通道 —— 等于保活自己把自己搞断；
- `EventChannel` 读循环：入站请求处理器抛异常只回 501，不再关通道
  （`s1(...)` 仍是桩，车机一发 `/command` 就会抛 `UnsupportedOperationException`）；
- `AirPlaySession.eventKeepAliveTick()`（原 `P0()`）：修正节流方向，失败只记日志。

### HID 闸门死锁

`HID_GATE_CLOSED reason=REPORTED` 在 `HID_CHAIN_START` 后 **同一毫秒** 出现，
一整场会话 `hidSetInputMode` 一次都没发。三处写反/死锁：

| 位置 | 原逻辑 | 修正 |
|---|---|---|
| `openHidGate()` | `!HID_MODE_REPORTED` → 永久关闸 | 删掉该前置判定 |
| `openHidGate()` | `now - LASTSEND < 2000` | `>= 2000` |
| `HidRetryTick()` | `now - LASTAT < 6000` | `>= 6000` |
| `HidOK()` | 从不置 `HID_MODE_REPORTED=true` | 车机 200 应答后置真 |

另外还原了 `I0(...)`（原 621 单元 jadx 桩）：它是闸门打开后真正发
`hidSetInputMode` 的方法，保留桩会让调度线程抛 `UnsupportedOperationException`。
还原后每会话最多 8 次、每个设备键去重、15s 超时走 `J0` 回调。

### 命名（本轮补丁相关）

| 原名 | 新名 |
|---|---|
| `hx.LASTFB` / `hx.REPORTED` | `ControlChannel.LAST_FEEDBACK_AT` / `HID_MODE_REPORTED` |
| `i0.P0()` / `i0.W()` | `AirPlaySession.eventKeepAliveTick()` / `openHidGate()` |
| `i0.b0`（事件通道字段） | `AirPlaySession.eventChannel` |
| `i0.fbs` / `i0.fbf` | `eventFeedbackSentCount` / `eventFeedbackFailedCount` |
| `y5.h()` / `y5.c()` / `y5.o()` / `y5.D()` | `sendFeedback()` / `handleEventResponse()` / `nextCSeq()` / `writeMessage()` |
| `y5.s` / `y5.r` / `y5.o` / `y5.f` / `y5.g` / `y5.t` / `y5.j` | `pendingCommands` / `pendingEventRequests` / `closed` / `readKey` / `writeKey` / `asyncStarted` / `cSeqCounter` |

### 下一份真机日志怎么判读

- ✅ 成功：不再出现每 10.4s 的 `TRANSPORT_END`；有 `CARPLAY_EVENT_FEEDBACK_SENT`，
  并伴随 `CARPLAY_RESP op=FEEDBACK`；
- ✅ HID：出现 `HID_GATE_OPEN ... devices=1` → `HID_INPUT_MODE_SENT slot=0 carrier=CONTROL`
  → 成功则 `HID_INPUT_MODE_ACCEPTED`；
- ❌ 仍中断：看 `CARPLAY_RESP_UNMATCHED` / `CARPLAY_INBOUND_HANDLER_FAILED` 的计数，
  前者说明车机应答的 CSeq 对不上，后者说明入站 `/command` 仍落在 `s1` 桩上。

## V1.7.31 变更（2026-11-03 13:00）

### 修复：真机提示「原生组件不可用」

现象：装上 V1.7.30 后主界面提示「当前原生 Car+ 组件不可用，请查看兼容说明。」，
连接按钮不可用；而 V1.7.29-fix2 同一台手机上是「原厂组件已核验」。

根因：`CarLifeBridge.d(ApplicationInfo)`（收集 APK / split APK 清单）被 jadx 反编译时
把 `throw new f("APK_FILE_BOUNDS")` 提到了 `if` **外面**，等价于「第一个文件无论是好是坏
都抛异常」。于是 `g() → d()` 必抛，`f()` 落到 `catch (CarLifeBridge.f)` 返回
`BLOCKED / APK_FILE_BOUNDS`，`g()` 为 false → 界面判定组件不可用。

原始字节码（`u.smali` 的 `d()`）真意是：

```text
if-eqz v7, :cond_2      # !isFile        -> 抛
if-eqz v7, :cond_2      # !canRead       -> 抛
if-ltz  v7, :cond_2     # length < 1     -> 抛
if-gtz  v7, :cond_2     # length > 512MB -> 抛
if-gtz  v7, :cond_2     # 累计 > 1GB     -> 抛
invoke-virtual ArrayList->add(...)      # 全部通过才登记
goto :goto_1                            # 回到循环
:cond_2  throw APK_FILE_BOUNDS
```

修复后：

```java
if (file.isFile() && file.canRead() && length >= 1 && length <= i) {
    j2 += length;
    if (j2 <= 1073741824) {
        arrayList2.add(new e(str, length, file.lastModified(), 0));
        continue;                     // 通过就继续下一个文件
    }
}
throw new f("APK_FILE_BOUNDS");       // 只有不通过才抛
```

回编译后反查 smali，`d()` 的控制流与基线 `u.smali` **逐条一致**。

### 可观测性：组件结论进日志

`MainActivity.H0()` 现在把 `CarLifeBridge.d.h()` 写进本机日志（同一结论只写一次）：

```text
NATIVE_COMPONENT_SUPPORT status=BLOCKED reason=HOST_SIGNER host_code=-1 plugin_code=-1 ...
```

以前这条只在 `WirelessCarPlayService` 建链时才打，主界面拦截时日志一片空白，只能靠截图判断。
现在即使组件校验失败，也能从 `reason=` 直接读出是被哪一步拦住
（HOST_SIGNER / PLUGIN_SIGNER / PACKAGE_IDENTITY / APPLICATION_DISABLED /
HOST_ENTRY_UNAVAILABLE / INSTALLATION_CHANGED / SIGNER_CHANGED /
COMPONENT_INSPECTION_UNAVAILABLE / APK_FILE_BOUNDS / PROTOCOL_STRUCTURE_UNAVAILABLE 等）。

### 产物

- `dist/Carplay-V1.7.31.apk`（540.7 KB，versionName 1.7.31，签名与 fix2 相同，可直接覆盖安装）
- 校验：`aapt2 badging` = 1.7.31；`cmd package compile -m verify` = Success；
  模拟器启动正常、FATAL 0；日志出现 `APP_START version=1.7.31` 与
  `NATIVE_COMPONENT_SUPPORT status=BLOCKED reason=HOST_SIGNER`（模拟器未装 Car+，属预期）。
