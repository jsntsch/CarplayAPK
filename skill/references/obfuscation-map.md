# 混淆符号语义字典 / Obfuscated-symbol map

本工程源码是 OcarPlay 0.8.53 反编译产物：`265` 个 java 文件、`155` 个 ≤3 字符的短类名、
数千个短方法名（`a` / `c` / `g` / `Q2` / `a1` …）。名字本身不含语义，几乎每一次排障都要先
花时间猜「这个方法是干嘛的」，而**本轮一半以上的返工正是因为猜错了名字**。

本字典给每个高频混淆符号补上**规范语义名（Canonical Name）+ 真实职责 + 踩过的坑 + 证据**，
相当于给混淆代码补一层可读的「变量名」。源码关键锚点用 `[OBF-MAP]` 注释互链回来。

> **主键是符号名，不是行号。** 反编译源码会随修复增删注释，行号会漂移；查符号一律用
> `grep -n "<方法名>(" 文件`，不要照抄本文或历史结论里的行号。
>
> Key by symbol name, never by line number: comments shift lines. Locate a symbol with `grep`.

---

## 0. 硬性规则：不要给标识符改名 / Never rename identifiers

「完善混淆的类和变量名」**不等于**把 `Q2()` 改成 `reuseCurrentWifi()`。以下约束使重命名在本工程
不可行，任何改名的提议都先对照这张表：

| # | 约束 | 证据 |
| --- | --- | --- |
| 1 | **反射与协议常量依赖原始类名**，两个变体都不开 R8（`isMinifyEnabled = false`） | `references/build.md` §不使用 R8；`MEMORY.md` §3 构建 |
| 2 | 短类名跨包重复（`prototype/a.java` 与 `secured/a.java` 同名），全量改名必然冲突 | 155 个 ≤3 字符类名，见 §1 |
| 3 | 短方法名是接口回调契约（`o3` / `a5` / `p0` / `q0` 分发器），改一处要同步全部调用点 | `ReleaseCheckTask.run()` 按 int 分派到 `l()` / `e()` / `g()` / `o()` |
| 4 | 改名后无法与 `.workbuddy/backup/java-20261105-003156/` 对照，历史修复证据链断裂 | V1.8.8 靠该备份找到 `t1()` 消失的调用点 |
| 5 | `javac` 未开 `-g`，**局部变量名不进 dex**；核验 APK 只能用字符串常量 / 方法名做锚点 | `SKILL.md` §2.2、`scripts/verify_built_apk.sh` 六项核对 |

允许的替代做法：加注释、加语义字典条目、加日志标记。**禁止**批量 `sed` 重命名。

---

## 1. 短名怎么读 / Reading the scheme

| 形态 | 实际含义 | 判读办法 |
| --- | --- | --- |
| `a` `b` `d` `e` `f` | 反编译按 dex 顺序分配的类名，无语义，跨包重名 | 看类里的字段类型和它实现的接口 |
| `a0` `b5` `o3` `p0` `q0` | 回调 / `Runnable` 分发器，数字只是序号 | 看 `run()` 里的 `switch(this.a)` 分支 |
| `a()` `c()` `g()` `h()` | 多为 getter、接口回调或状态查询 | 看返回类型 + 调用点，不看名字 |
| `Q2()` `W2()` `X2()` `Y2()` `i1()` | 同一方法族的编号，彼此无语义关联 | 逐个读实现，别按编号推断职责 |
| `AirPlaySession.d` / `f` | 会话内部状态对象，字段全是单字母 | 见 §2.4 字段表 |
| `t0()` / `t1()` / `t2()` | 名字相近但**职责可能完全不同** | 必须逐个核对，见 §3 坑 4 |

> 判读纪律：**先看调用点与日志字符串，再看名字**。名字相近不代表管线相同。

---

## 2. 核心符号速查 / Core symbol index

「建议语义名」是本字典给出的可读名字，供注释与沟通使用；**不写回源码标识符**。

### 2.1 协议与会话 — `secured/AirPlaySession.java`（5351 行）

| 混淆名 | 建议语义名 | 实际职责 | 踩过的坑 / 证据 |
| --- | --- | --- | --- |
| `R1(String, String, Map)` | `sendRtspRequest` | 所有 RTSP 请求的**统一发送入口**：`/setup`、`/command`、`/feedback`、`RECORD`、`stream SETUP` 都走它 | V1.8.15/16 在里面对 `/command` 加会话路径 / `X-Apple-Session-ID` 头，三次实验全 422 |
| `g1(Object)` | `parseResponseMap` | 把车机响应整理成 Map | 与 `g2()` 只差一个字母，别混 |
| `g2()` | `notifyModesChanged` | 发 `POST /command type=modesChanged` | **V1.7.95 关键修复**：车机回 422 后抛异常，导致其后的 `RECORD` 与 `H1()`(HID 启动) **永不执行** → 历史 12 份抓包 `videoUnits=0` / `TOUCH_FORWARDED=0` 的真因 |
| `i0()` | `runMainSessionFlow` | 会话主流程编排：`P1` → `q2` → `g2` → `l2` → `RECORD` → `H1` | 排障先看这个顺序，断在哪一步就查那一步 |
| `l2()` | `setupMediaStreams` | 发 `streams:[{type:110}]` 的流 SETUP | 只发视频流，`pcmBytes=0` 曾被误判为车机断连主因（V1.7.97 证伪） |
| `H1()` | `startHidChain` | 启动 HID 链（能力解析 → 门 → 输入模式扫掠） | 因 `g2()` 抛异常而长期没被调用 |
| `a1(a, d)` | `audioSendLoop` | **音频发送线程**（`CarPlus-audio-send`） | **V1.8.8~12**：三处 `dVar.g == 100` 判断把编码后的包全部丢掉，`sent_packets_total` 恒 0；三处必须一起放行 |
| `t1(CarLifeFrame.a, byte[], long)` | `injectPcmToAudioPipeline` | **PCM 正确注入入口**：PCM → AAC 编码 → 入队 → RTP | 11-05 备份里 `WirelessCarPlayService:1659` 还在调它，后来调用点消失 |
| `t0(EventChannel, int, l)` | `sendInitialSetup` | 构造初始 SETUP plist（`name/model/deviceID/sessionUUID/timingPort/features`） | `features` 是 14 项能力名 `List`，与 TXT 十六进制位域、`/info` BigInteger 掩码**三者不能混填** |
| `F0(CarLifeFrame.a)` | `isAudioOutputReady` | 音频输出就绪判定 | 因 `d.p`（主模式待确认）恒 false，PCM 被静默丢弃 → V1.8.6 加 `audioPcmBypass` |
| `o2(d, f)` | `initAudioPacer` | 音频节拍器初始化 | `AudioPacer` 只是时隙计算，不会长期阻塞 |
| `q2()` | `openAudioRoute` | 音频路由入口 | 仅在有 PCM 数据时触发，不是断连原因 |
| `p1(CarLifeFrame.a, int, int, int)` | `planAudioRouteForChannel` | 按通道建立音频流 SETUP | V1.7.97 用 `p1(MEDIA,48000,2,16)` 建占位流，证明断连与音频无关 |
| `E1()` | `newSessionId` | **每次调用生成新的随机 sessionID** | **V1.8.15 坑**：拼 `/<sessionID>/command` 时每次值都不同，与 SETUP 用的不是同一个 |
| `n2(Object, int)` | `parseStreamSetupReply` | 解析 stream SETUP 响应（`dataPort` / `controlPort`） | 这里的 `controlPort` 是**音频 RTP 的 UDP 控制端口**，不是 CarPlay ctrl TCP 服务 |
| `D1(Map, String)` | `intFromMap` | 从响应 Map 取整型值 | 通用工具 |
| `G0()` | `isSessionAlive` | 会话存活判定（发送循环 `while` 条件） | 通用工具 |

### 2.2 服务编排与音源 — `prototype/WirelessCarPlayService.java`（4606 行）

| 混淆名 | 建议语义名 | 实际职责 | 踩过的坑 / 证据 |
| --- | --- | --- | --- |
| `Q2()` | `reuseCurrentWifiIfMatches` | 判定当前 WiFi 能否直接复用、避免退回蓝牙 iAP2 | **V1.7.93 致命 bug**：冷启动补丁写在 `k1 == null` 的 `return null` **之后**，永远执行不到 → 手机连着 `myBuick GS` 却判「无 WiFi」→ 每 20s 重试 3 次 → `BOOTSTRAP_IOException` |
| `z1(Network, …)` | `startWirelessSession` | **真正的连接主流程**（Activity bind 后直调） | V1.8.2：`startPcmCapture()` 原挂在 `onStartCommand`，而会话走 `z1()`，导致采集从未启动；日志里 `APP_BUILD` 只出现 1 次但会话照常建立 |
| `e.h(byte[], long)` | `onNativePcmFrame`（内部类 `e`） | DisplaySession 的 **PCM 回调**，内部调 `K2()` | **V1.8.1 坑**：误以为它是音频注入入口，实际 `K2()` 是原生源/通用管线，送进去报 `unexpected bytes before AVC start code`（H.264 解析错误） |
| `J2(CarLifeFrame.a, byte[], long)` | `injectPcmByChannel` | **带通道的 PCM 注入**，走 `t1()` 音频管线 | 与 `h()` 只差一个字母；备份 `1659` 行用它 |
| `K2(byte[], long)` | `pushNativeSourceBytes` | 原生源入口（视频 / 通用管线） | 不是音频发送口，见上 |
| `I2(...)` | `onNativeAudioFormat` | 原生采集层上报音频格式，日志 `Actual native audio format observed=` | 音频源参数来源 |
| `r2(String)` | `prefsKeyForPeer` | 生成按车机区分的偏好键哈希 | **V1.7.91 坑**：误判为 SSID 读取，实为证书 pin 键；据此写的补丁已废弃 |
| `W2()` / `X2()` / `Y2()` | `selectTransportPath` 族 | 传输路径选择 / 保留 WiFi 后的 AirPlay 直连 | 名字相邻但职责不同，逐个读实现 |
| `i1(String)` | `logToFile` | **应用内日志**，只落 `files/connection-log-N.txt` | 与 `ControlChannel.Log()`（只进 logcat）是两条独立通道，互不替代 |
| `L1()` | `isServiceRunning` | 服务运行判定 | `PairingPreparationService.onStartCommand` 用它拒绝启动 |
| `startPcmCapture()` | （已是语义名，V1.7.99 新增） | 麦克风采集线程，PCM 送 `t1()` | 需在 `microphone` 类型前台服务（`16\|128`）内运行；Android 14+ 否则 `read()` 静默返回 0 |

### 2.3 控制通道与广播 — `ControlChannel` / `CarPlayCtrlAdvertiser` / `EventChannel`

| 混淆名 | 建议语义名 | 实际职责 | 踩过的坑 / 证据 |
| --- | --- | --- | --- |
| `ControlChannel.Log(String)` | `logcatTag` | 只进 logcat tag `CarPlayAPK` | 与应用内日志双写才能判读（SKILL.md §4.2 铁律） |
| `ControlChannel.MarkTcpAccept()` | `onCtrlTcpAccepted` | 打 `V160_CTRL_TCP_ACCEPT` 并置 `CTRL_TCP` | **判据名字要写全**：源码里**没有** `CTRL_TCP_ACCEPT` 字符串，用短名 grep 会误判「车机从未连接」（V1.8.19 纠正） |
| `ControlChannel.MarkCtrlInvitation()` | `onCtrlInvitationAccepted` | 打 `V167_CTRL_IDENTITY_BOUND`，登记车机 receiver id | 真正判断「车机回连过」看 `CTRL_INVITATION_ACCEPTED` |
| `ControlChannel.FillTxt()` | `buildCtrlTxtRecord` | 填 `_carplay-ctrl` 的 **10 键** TXT | 规则见 `references/txt-rules.md`；`pi` 是 HomeKit 标识且属车机 `_airplay` 广播，**不要补**（V1.8.17 固件证据） |
| `CarPlayCtrlAdvertiser.q()` | `registerAndListen` | 注册 NsdManager 服务 + 绑 ServerSocket + 起 accept 线程 | 构造第 4 个参数是**本机 MAC**（`n.a()` 校验格式），误传服务名会抛 `CTRL_SERVICE_VALID_LOCAL_IDENTITY_REQUIRED`（V1.8.17） |
| `CarPlayCtrlAdvertiser.l(ServerSocket)` | `ctrlAcceptLoop` | `accept()` 循环，串行读请求写响应 | `setSoTimeout` 原为 1500ms，V1.8.19 放宽到 5000ms；accept 是**串行**的，慢连接会阻塞后续 |
| `CarPlayCtrlAdvertiser.k(...)` | `createBoundServerSocket` | 按 LinkAddress 逐个建 ServerSocket，共享同一端口 | 同时绑 IPv4 + IPv6 link-local；V1.8.19 用 `nc` 实测两端都能触发 `V160_CTRL_TCP_ACCEPT` |
| `CarPlayCtrlAdvertiser.StartEarly/StopEarly/Probe` | （语义名，V1.8.18/19 新增） | App 到前台即广播 / 会话接管前关闭 / 浏览车机服务类型 | `Probe` 实测：车机只广播 `_airplay._tcp`（`CarPlay @192.168.5.1:7000`），`_carplay._tcp` 无实例 |
| `EventChannel.v(String,String,String,byte[])` | `writeRtspRequest` | 实际写请求并加密 | `Content-Length` 用**明文长度**，与 `/setup` 同路径，不是 422 成因 |
| `EventChannel.q()` | `classifyInboundRequest` | 给入站请求打标签 | 带前缀的 `/<id>/command` 会被归为 `op=OTHER` |
| `EventChannel.b.g()` | `ensureOkStatus` | 非 200 抛异常 | V1.7.94 在此加 `CONTROL_RESP_STATUS` 打印响应体，确认 422 的 body 为空 |
| `EventChannel.n()` | `isEncryptionEnabled` | 加密启用判定 | `/setup` 与 `/command` 同为 `encrypted=true`，前者成功 ⇒ 422 与加密无关 |

### 2.4 音频状态与队列 — `AirPlaySession.d` / `f` / `PcmQueue`

`AirPlaySession.d` 是音频输出的**全单字母字段状态对象**，最容易误判：

| 字段 | 建议语义名 | 实际含义 |
| --- | --- | --- |
| `d.g` | `resourceOwnership` | **资源所有权状态**；`100` = 等车机确认 modes（pending） |
| `d.p` | `audioMainModePending` | 音频主模式待确认，恒 true 时 `F0()` 返回 false |
| `d.m` | `pcmQueue` | `PcmQueue` 实例 |
| `d.n` | `audioPacer` | `AudioPacer` 实例 |
| `d.l` | `pcmConverter` | `PcmConverter` 实例 |
| `d.o` | `aacEncoder` | 编码器（`t` 类型） |
| `d.i` | `isClosed` 判定用的 DatagramSocket | 发送循环 `while (G0() && !dVar.i.isClosed())` |

`f.g()` = `canSendNow`（pacer 是否放行）。`carplay-audio` 三道门全部读 `d.g`。

| `PcmQueue` 混淆名 | 建议语义名 | 踩过的坑 |
| --- | --- | --- |
| `p()` | `dequeueBlocking` | **V1.8.11**：`wait()` 无超时，异常时序下发送线程只跑一轮就永久阻塞；已改 200ms 超时返回 null，`a1()` 收到 null 改 `continue` 不再 `return` |
| `g(a)` | `isExpectedSequence` | 包的序号必须等于 `this.h`，否则丢弃 |
| `m()` | `dequeueInternal` | 用 `this.h` 出队 |
| `o(long)` | `resetEpoch` | 会改写 `h` 并清空队列；反复调用会让包永远对不上号 |
| `i()` | `enqueue` | 含 `notifyAll()`，不是丢包点 |
| `l()` | `queuedFrames` | 队列积压计数 |

### 2.5 iAP2 与识别 — `Iap2Csm` / `MfiCertificateVerifier` / `PairingTlv8` / `BtDiscovery`

| 混淆名 | 建议语义名 | 踩过的坑 / 证据 |
| --- | --- | --- |
| `Iap2Csm.q()` / `v()` | `parseTlvMap` 两处实现 | **V1.7.77/78**：重复 TLV 会抛异常导致 EA 列表恒空；`0x1D01` 走的是 `v()`，只改 `q()` 无效，两处都要「保留首值 + 告警」 |
| `Iap2Csm.t()` | `safeGetParam` | 容错 getter，诊断码改用它后不再被重复 TLV 打断 |
| `Iap2Csm.y()` | `parseCarPlayStartSession` | 解析 `0x4301`，V1.7.74 加扁平布局兜底 |
| `Iap2Csm.A(String)` / `B(int, byte[])` / `f(...)` | `tlvString` / `tlvBytes` / `tlvConcat` | 构造识别消息的 TLV；EA ID 换算 `0x4E09..0x4E0E = 19977..19982`，曾算成 19945（差 `0x20`） |
| `MfiCertificateVerifier.bEaPayload()` | `buildEaNegotiationPayload` | **V1.7.75**：内部误用 `SyntheticRefs.c()` 当日志函数，实际抛 `IOException` 被 catch 吞掉 ⇒ EA 恒空，V1.7.73/74 从未真正生效 |
| `MfiCertificateVerifier.o()` / `R()` | `isCredentialsReady` / `isWifiCredentialMode` | V1.7.67：`R()` 在 Buick 为 false，导致本机蓝牙标识不发送；已移出该条件 |
| `PairingTlv8.h()` | `doPairSetupOrVerify` | V1.7.85 改为**有存储 peer 时先 `pair-verify`**，失败回退 `pair-setup`；此前每次都重做 pair-setup |
| `BtDiscovery.u(Context,int)` | `registerIap2SdpRecords` | 「车机识别修复」核心：按 `s/t/v` 三种组合注册 3 个 iAP2 UUID 的 `listenUsingRfcommWithServiceRecord`，打 `PHONE_DISCOVERY_RECORDS_READY` |
| `BtDiscovery.s/t/v()` | `prepareFull / prepareTwo / prepareOne` | 分别是 3 / 2 / 1 条 SDP 记录组合 |

### 2.6 通用陷阱类

| 混淆名 | 建议语义名 | 踩过的坑 |
| --- | --- | --- |
| `SyntheticRefs.c(String)` | `throwIo`（**不是日志**） | 函数体就是 `throw new IOException(str)`；`b()` 抛 `IllegalArgumentException`、`d()` 抛 `IllegalStateException`、`f()` 抛 `ProtocolIOException`。**全类没有日志函数**，用它打日志必然被 catch 吞掉 |
| `SyntheticRefs` 其余 | `throwXxx` | 反编译器用来替代不可达/合成语句的占位抛错类，见到即视为控制流，不要当工具库 |
| `SessionStateMachine.Q()` | `buildModesChangedPayload` | 构造 `modesChanged`；**V1.8.20 起为 `{type, params:{resources}}`**。此前顶层 `type`+`resources` 是 V1.7.86 依规范改的回归，缺 params ⇒ 恒 422（V1.8.13 只改资源列表无效的真因） |
| `SessionStateMachine.q(Map)` | `handleChangeModes` | 处理**入站** `changeModes` 的状态机方法，与 `Q()`（构造出站 `modesChanged`）不是一回事 |
| `HidCommandSender.b` | `knownCommandTypes` | 命令类型白名单集合（`changeModes` / `modesChanged` / `hidSendReport` / `setNightMode` …），排查 `/command` 时先看这里 |
| `ReleaseCheckTask.run()` | `dispatchByCase` | 按 `this.a` 分派：`case 0` → `CarPlayCtrlAdvertiser.l()`（accept 循环），`default` → `PairingSession.a.o()`。新增 Runnable 别复用到别处 |
| `o3` / `a5` / `b5` / `p0` / `q0` | `clickDispatcher` / `runnableXxx` | UI 点击与后台线程分发器，语义全在 `run()` 的 `switch` 里 |

---

## 3. 因混淆名误判踩过的坑 / Misread-name pitfalls

按「误判 → 真相 → 修法」记，都是真机或字节码证据，不是推测。

| # | 坑 | 误判 | 真相 | 修法 / 版本 |
| --- | --- | --- | --- | --- |
| 1 | `SyntheticRefs.c()` | 当成日志函数写进 EA 构造 | 就是 `throw new IOException` | 改 `ControlChannel.Log`；V1.7.75 才让 EA 真正生效 |
| 2 | `Q2()` | 以为 SSID 冷启动补丁会执行 | 补丁在 `k1 == null` 早退**之后**，冷启动永不执行 | 把 k1 快路径收进 `if (lVar != null …)`；V1.7.93 |
| 3 | `g2()` | 以为 422 只影响屏幕所有权 | 抛异常让 `RECORD` 与 `H1()` 永不执行 | catch 后容忍；V1.7.95，视频从 0 → 20fps |
| 4 | `h()` vs `J2()` | 把 PCM 送进 `h()` → `K2()` | `K2()` 是原生源/视频管线，报 AVC start code 错误 | 改走 `t1()`；V1.8.1 |
| 5 | `t0()` / `t1()` / `t2()` | 以为同一族职责相近 | `t0` = 初始 SETUP、`t1` = PCM 注入，完全不同 | 逐个核对实现 |
| 6 | `r2(A)` | 当成 SSID 读取 | 证书 pin 的偏好键哈希 | 撤销该补丁；V1.7.91 |
| 7 | `E1()` | 以为是已保存的会话 ID | 每次生成新的随机值 | 改从 `this.e` 提取 SETUP 实际 session；V1.8.16 |
| 8 | `a1()` 里的 `dVar.g` | 以为放行一处即可 | 取包外层 / 发送块内层 / `send()` 前 **三处**同类判断，内层整块跳过时连计数都不涨 | 三处统一加 `audioPcmBypass`；V1.8.8~12 |
| 9 | `PcmQueue.p()` | 以为发送线程没启动 | `wait()` 无超时，线程卡死 | 200ms 超时 + null 改 `continue`；V1.8.11 |
| 10 | `Iap2Csm.q()` | 只改 `q()` 就以为修好重复 TLV | `0x1D01` 走 `v()` | 两处都改；V1.7.78 |
| 11 | `CTRL_TCP_ACCEPT` | 用短名 grep 判断「车机从未连接」 | 实际日志是 `V160_CTRL_TCP_ACCEPT`，短名不存在 | 用完整字符串或 `CTRL_INVITATION_ACCEPTED`；V1.8.19 |
| 12 | `controlPort` | 以为是 CarPlay ctrl TCP 端口，要补进 SETUP | 是**音频 RTP 的 UDP 控制端口**（`n2()` 里 `datagramSocket.connect`） | 撤销该方向，不要给初始 SETUP 加这个键 |
| 13 | `pi` | 以为 `_carplay-ctrl` TXT 缺 `pi` 导致车机不认 | 固件 `libNmeCarPlay.so` 显示它是 HomeKit 配对标识、条件性添加，且属车机 `_airplay` 广播 | 不改 10 键；V1.8.17 |
| 14 | 写死字符串 | 拿 `pcm_paused=true` / `playback_confirmed=false` 当运行时状态 | 都是源码字面量 | 只看计数与状态迁移（SKILL.md §5 铁律） |
| 15 | `/command` 422 | 用 `setNightMode` 探针 422 ⇒ 判「端点级整体拒绝」 | 该命令是车机→手机方向、不在固件入站白名单；真因是请求体缺 `params`（0x4441b） | 恢复 `{type, params}`，探针改 `updateFeedback`；V1.8.20，旧结论作废 |
| 16 | 规范当实现 | 按 Table 23-71 把 `resources` 放顶层 | Cinemo 固件从 `params` 取 `resources`（0x447d0 / 0x44991） | 以真机固件为准；V1.8.20 |

---

## 4. 判读纪律 / Reading discipline

1. **符号名优先于行号**：本文所有位置以「文件 + 方法名」为准，注释增删会让行号漂移。
2. **局部变量名不进 dex**：核验 APK 只能用字符串常量 / 方法名做锚点，别用局部变量名。
3. **看调用点，不看名字**：`h()` 与 `J2()`、`g1()` 与 `g2()`、`t0()` 与 `t1()` 都是反例。
4. **改一道门要找齐所有同类门**：`a1()` 三处 `g == 100` 的教训；用 `grep -n "dVar.g"` 全量核对。
5. **两条日志通道互不替代**：`i1()` 落文件、`ControlChannel.Log()` 进 logcat（SKILL.md §4.2）。
6. **判据字符串要写全**：`CTRL_TCP_ACCEPT` 与 `V160_CTRL_TCP_ACCEPT` 的教训；grep 前先确认源码里有这个字面量。
7. **新增语义条目时**：源码加 `[OBF-MAP]` 注释 + 本表加行，两者同步；行号仍不写死。

---

## 5. 维护规则 / Maintenance

* 新增 `[OBF-MAP]` 源码注释 → 同步本表对应行（建议语义名、职责、踩坑）。
* 修好一个 bug → 在 §3 追加一行「误判 → 真相 → 修法」，注明版本与日志证据。
* 发现新的「名字相近但职责不同」组合 → 补进 §1 与 §2，避免下次再猜。
* 本表只做语义映射，**任何情况下都不批量重命名标识符**（§0）。
