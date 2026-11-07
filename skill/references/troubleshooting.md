# 排障实例 / Troubleshooting cases

每个案例都按「现象 → 证据 → 根因 → 修法 → 验收」写，方便对照自己遇到的版本。
Each case follows symptom → evidence → root cause → fix → acceptance, so you can match your build.

---

## 案例 1：投屏黑屏，握手卡在加密段 / Black screen, handshake stops in the crypto phase

**现象**：蓝牙连上、`TX CMD 0x18001` 发出，但之后没有 `RX CMD`，或到 `0x1806b` 就没下文。
Symptom: Bluetooth connects and `0x18001` goes out, but no `RX CMD` follows, or it dies at `0x1806b`.

**证据**：日志里最后一条 `TX` 之后没有对应 `RX`；`HID_CAPABILITIES` 压根没出现（会话没跑到 HID）。
Evidence: no `RX` after the last `TX`; `HID_CAPABILITIES` never appears.

**根因（两类，都会出现）**

1. **命令分支缺失**：加密握手需要的 `0x1006a`（RSA 公钥交换请求）没有对应处理分支，
   于是 RSA 上下文一直为空，下一步 `0x1006c` 直接报 “unexpected encrypted seed”。
   A missing case for the RSA-exchange request leaves the RSA context null, so the next step
   fails with "unexpected encrypted seed".
2. **判断极性写反**：MFi 结果判断写成「验证成功就抛异常」，于是验证通过反而断开。
   An inverted MFi verdict throws exactly when verification succeeds.

**修法**：按命令号逐个补齐处理分支；把「验证成功」的分支改成继续流程。
Fix: add the missing case; make the success path continue.

**验收**：`TX 0x18001 → RX 0x10004 → TX 0x18048 → RX 0x10049 → TX 0x1806b → RX 0x1006c → RX 0x1006e
→ TX 0x18007 → RX 0x10008 → TX 0x18009`，随后 `NATIVE_VIDEO_FIRST_BYTES` 与 `VIDEO_PROGRESS`。
Acceptance: the full sequence above, followed by `NATIVE_VIDEO_FIRST_BYTES` and `VIDEO_PROGRESS`.

---

## 案例 2：投屏一帧后卡死闪退 / One frame then a freeze and crash

**现象**：画面出一帧就卡住，几秒后 ANR 或闪退。
Symptom: one frame appears, the UI freezes, then ANR or a crash.

**证据**：`adb bugreport` 里 `FS/data/anr/anr_*`，主线程停在
`waiting to lock <0x…> held by thread <id>`，持锁线程停在阻塞式 socket 读。
Evidence: in the ANR trace the main thread waits on a lock whose owner sits in a blocking socket read.

**根因**：把**阻塞式 socket 读**放进了 `synchronized` 临界区。读一帧要等车机，等的时候
全局锁一直被占，主线程拿不到锁 → ANR。
Root cause: a blocking socket read inside a `synchronized` block. While waiting for the head unit
the global lock stays held, so the main thread blocks and the watchdog fires.

**修法**：把阻塞读移出临界区（只在锁内做内存状态更新）。
Fix: move the blocking read outside the critical section; keep only state updates inside.

**验收**：连续投屏 5 分钟不 ANR；`bugreport` 里不再出现该锁的长时间持有。
Acceptance: five minutes of projection without ANR; no long lock holds in the trace.

---

## 案例 3：触摸完全无反应 / Touch does nothing

**现象**：画面正常，车机屏点划无反应，日志里 `COMMAND_REJECTED=TOUCH_UNKNOWN_DEVICE` 持续递增。
Symptom: video is fine, taps do nothing, `COMMAND_REJECTED=TOUCH_UNKNOWN_DEVICE` keeps growing.

**证据**：

```
HID_CAPABILITIES devices=4 touch=0
HID_TOUCH_UNSUPPORTED index=0 bytes=109 … reason=DESCRIPTOR
```

**根因**：多点触控描述符里有**两个物理集合**（两指），解析时把触摸集合的 slot **写死成 1**。
两指的 contact id 落到同一个 slot → 第二指被判为「contact id 冲突」
（`HID_AMBIGUOUS_CONTACT_ID`）→ 整个描述符被拒 → `touch=0` → 车机不认识该设备。
Root cause: the descriptor carries two physical collections (two fingers) but every touch
collection was assigned slot **1**. Both contact IDs land in the same slot, the parser reports an
ambiguous contact ID, the whole descriptor is rejected and `touch` ends up 0.

**修法**：为每个物理触摸集合分配**唯一递增**的 slot（单指描述符仍然得到 1，行为不变）。
Fix: give each physical touch collection a unique, increasing slot. Single-finger descriptors still
get slot 1, so existing behaviour is preserved.

**验收**：`HID_CAPABILITIES devices=4 touch=1` → `HID_TOUCH_RANGE … descriptor_driven=true`
→ 触摸时 `TOUCH_FORWARDED` 递增，且不再出现 `TOUCH_UNKNOWN_DEVICE`。
Acceptance: `touch=1`, `descriptor_driven=true`, `TOUCH_FORWARDED` climbing, no
`TOUCH_UNKNOWN_DEVICE`.

> 注意：`HID_TOUCH_UNSUPPORTED index=3 bytes=57` 是多媒体按键描述符，不属触摸，被拒是正常的。
> `index=3 bytes=57` is a consumer-key descriptor; rejecting it is expected.

---

## 案例 4：没有声音，同时有无法关闭的异响 / No audio plus unstoppable noise

**现象**：车机不出音乐，却有持续异响，断开 App 才停。
Symptom: no music, but a constant buzzing that only stops when the app is killed.

**证据**：

```
AUDIO_FLOW … sent_packets_total      5 秒涨 6.4 万   (≈13,000 包/秒)
AUDIO_FLOW … dropped_frames_total    每 5 秒 +24 万
PcmQueue queued_frames=12000/12000   backpressure_events=3742
```

**根因（两层，第二层修完才暴露）**

1. **发送循环少了跳出**：发完一个音频包后没有 `break`，内层循环拿着**同一块 PCM** 反复重发，
   于是车机反复播放同一段 352 帧 → 异响；队列永不排空 → 真实音乐全被丢弃。
   Missing `break`: the inner loop resends the *same* PCM buffer forever, so the head unit repeats
   352 frames (the noise) while the queue never drains (all real audio dropped).
2. **补了跳出却多了 `return`**：内层循环结束后直接 `return`，整个发送方法退出，
   `sent_packets_total` 停在 1。正确控制流是「取一包 → 发一包 → 回外层取下一包」。
   After adding the break, an extra `return` ended the whole send loop after one packet.

**修法**：内层发送一次后跳出，回到外层取下一包；只有线程被中断时才退出方法。
Fix: send exactly one packet per outer iteration; leave the method only on interrupt.

**验收**：`sent_packets_total` ≈**136 包/秒**、`dropped_frames_total` 停止增长、
`sent_frames` 每秒约 48k（= 实时）；车机出声且异响消失。
Acceptance: ≈136 packets/s, `dropped_frames_total` frozen, ≈48k frames/s (real-time), clean audio.

---

## 案例 5：重连后画面有了，声音/触摸哑掉 / After a reconnect video returns but audio is dead

**现象**：会话因事件通道断开而重启，重连后视频恢复，但 `converted_frames` / `sent_frames` 恒 0。
Symptom: the session restarts after an event-channel drop; video returns but the audio counters
stay at 0.

**证据**：新会话里没有 `NATIVE_AUDIO_PROGRESS … FIRST_VALID_PCM`，即音频读取线程从未启动。
Evidence: no `FIRST_VALID_PCM` in the new session — the reader thread never started.

**根因**：恢复路径只重建了视频与事件通道，音频（以及触摸相关）线程没有跟着重建。
Root cause: the recovery path rebuilds video and the event channel but not the audio/touch threads.

**修法**：会话恢复时按「新会话」处理，所有子系统一律重建；禁止复用上一会话的对象。
Fix: treat a reconnect as a brand-new session and rebuild every subsystem; never reuse objects.

**验收**：重连后 `FIRST_VALID_PCM reader_connected=true` 出现，`sent_packets_total` 恢复 136 包/秒。
Acceptance: `reader_connected=true` shows up and the packet rate returns to ≈136/s.

**临时绕过**：`am force-stop` 后重启 App（会新建会话），可立刻判断是不是恢复路径的问题。
Workaround: force-stop and relaunch — if audio comes back, it is the recovery path.

---

## 案例 6：改了常量或格式串导致类初始化失败 / Editing constants or format strings breaks init

**现象**：某个功能整块不可用，日志里出现 `NumberFormatException` 或
`UnknownFormatConversionException`。
Symptom: a whole feature is dead with `NumberFormatException` or
`UnknownFormatConversionException`.

**根因**：十六进制密钥常量、`String.format` 的格式串被「顺手改」了。前者在类初始化时抛异常，
整个类不可用（例如签名算法加载失败）；后者只在打印日志时抛，把正常流程一起打断。
Root cause: a hex key constant or a `String.format` pattern was accidentally edited. The former
throws during class initialisation (killing the whole class); the latter throws while logging and
takes the normal path down with it.

**修法**：改动后跑一次全量编译，并检查格式串占位符与常量长度；日志格式串改动要单独复查。
Fix: rebuild fully after edits and re-check format placeholders and constant lengths.

**验收**：`compileDebugJavaWithJavac` 通过，且相关功能日志正常打印、无异常栈。
Acceptance: clean compile, and the feature logs normally with no exception stack.

---

---

## 案例 7：会话始终建立不了，手机卡在 waiting / Session never starts, phone stuck waiting

**现象**：蓝牙 RFCOMM 连上、iAP2 握手（§5 第 2 条 CMD 序列）走完、`MfiCertificateVerifier` 认证通过，
但画面/声音/触摸全无，日志里反复 `waiting=CARPLAY_START_SESSION`，从不见 `CTRL_INVITATION_ACCEPTED`。
Symptom: RFCOMM up, iAP2 handshake completes, MFi auth passes, but no video/audio/touch ever appears;
the log repeats `waiting=CARPLAY_START_SESSION` and `CTRL_INVITATION_ACCEPTED` never shows up.

**证据**：
```
IAP2_PEER_MESSAGE_IDS direction=SEND count=0 ids=NONE     # 车机声明要发的列表为空（19 次）
waiting=CARPLAY_START_SESSION                              # 手机在等车机发 17153
# 手机侧 0x1D00 三处均为 x(7424, new byte[0]) → 空 Identification
```

**根因**：手机发 iAP2 `Identification(0x1D00=7424)` 时 payload 恒为空（`new byte[0]`），
车机据此在 `0x1D01` 的 **SEND 列表也为空** → `R()` 的 `this.m.contains(17153)` 永远 false
→ 车机永不发 `CarPlayStartSession(0x4301=17153)` → 手机永久 `waiting`，AirPlay 会话（`t0()`）从未启动。
注意：`direction=SEND count=0` 是**车机**声明它要发的列表空，真病因在手机 `0x1D00` 的空 payload。
Root cause: empty `0x1D00` payload from the phone makes the head unit leave its SEND list empty too,
so `R()` is never satisfied and `0x4301` is never sent.

**修法**（V1.7.73）：`MfiCertificateVerifier` 新增 `bEaPayload()` / `bEaIds()`，在 `0x1D00` 的
index6=to-send / index7=to-receive 填非空 EA 列表（to-send = 车机期望手机发的 `0x4E09–0x4E0E,EA00,EA01`；
to-receive = 含 17153 + 17152）。三处 `x(7424, new byte[0])` 改为 `x(7424, bEaPayload())`
（`43520` 这种独立 auth 消息不塞 EA 列表）。
Fix (V1.7.73): declare a non-empty EA list in `0x1D00` so the head unit puts 17153 in its SEND list.

**加固**（V1.7.74）：`Iap2Csm.y()` 解析车机 `0x4301` 时加扁平布局兜底 + 顶层字段诊断 + identifier NPE 防护，
确保车机用变体编码时也能取到 WiFi 凭证。
Hardening (V1.7.74): tolerant `0x4301` WiFi-credential parsing with flat-layout fallback + diagnostics.

**验收**：`IAP2_IDENTIFICATION_EA` 出现 → `IAP2_PEER_MESSAGE_IDS direction=SEND` **含 17153**
→ `waiting` 归零 → `CARPLAY_START_WIFI_*` 解析出 SSID → `CTRL_INVITATION_ACCEPTED` + `t0()` 跑通。
Acceptance: `IAP2_IDENTIFICATION_EA` present, 17153 in the car's SEND list, `waiting` hits zero,
SSID parsed, then `CTRL_INVITATION_ACCEPTED` and `t0()` run.

> 完整流程与字段编码见 **`references/iap2-carplay-flow.md`**。
> 该案例**尚未在真车端到端验证**：V1.7.74 是诊断包，待复现抓取后对照 §4 标记确认车机是否真的发 17153。

---

## 案例 8：按混淆方法名猜职责，反复改错地方 / Guessing obfuscated method names

**现象**：同一个问题连改几版都不见效；每次都「觉得改对了」，但计数纹丝不动。
Symptom: several builds in a row with no effect — the counters never move.

**证据**（本轮真实返工，均为字节码或日志证据）：

* V1.7.73/74 补的 EA 列表从未生效 —— `SyntheticRefs.c()` 其实是 `throw new IOException`，被 catch 吞掉。
* V1.7.77 只修了 `Iap2Csm.q()` 的重复 TLV，而 `0x1D01` 走的是 `v()`。
* V1.8.1 把 PCM 送进 `h()` → `K2()`（原生源 / 视频管线），报 `unexpected bytes before AVC start code`；
  正确入口是 `J2()` → `AirPlaySession.t1()`。
* V1.8.8~12 音频 `sent_packets_total` 恒 0：`a1()` 里 `d.g == 100` 共有**三道**同类判断，
  只放行一处时包在「发送块内层」被整块跳过，连 `accepted` / `rejected` 都不计数。
* V1.8.19 用 `CTRL_TCP_ACCEPT` grep 得出「车机从未连接」—— 源码里根本没有这个字面量，
  真实日志是 `V160_CTRL_TCP_ACCEPT`。

**根因**：反编译产物的名字不含语义（`265` 个 java 文件 / `155` 个 ≤3 字符短类名）。
把 `h`/`J2`、`q`/`v`、`g1`/`g2`、`t0`/`t1` 当成同族方法，或拿写死字符串、短名、行号当判据，
就会在错误的一层反复修改。完整清单见 **`references/obfuscation-map.md` §3**。

**修法**：

1. 先查 `references/obfuscation-map.md` 拿到建议语义名与职责，再动代码。
2. 用 `grep -n "<方法名>(" 文件` 定位，**不照抄历史结论里的行号**（注释增删会漂移）。
3. 改一道门前先 `grep` 找齐所有同类判断（例：`grep -n "dVar.g"`）。
4. 判据字符串先在源码里确认字面量存在；两条日志通道分开看（`i1()` 落文件 / `ControlChannel.Log()` 进 logcat）。
5. **不要给标识符改名**：反射与协议常量依赖原始类名，两个变体都不开 R8（字典 §0）。

**验收**：能用一条日志计数说明「改之前是 0、改之后在涨」；若计数仍为 0，
先怀疑自己找错了方法，而不是继续在同一个方法里加补丁。
Acceptance: one counter must move from 0 to increasing; if it stays 0, re-check which method you are in.

---

## 案例 9：`/command` 恒 422、触摸无反应 / Command 422 and dead touch

**现象**：投屏与音频都正常，但所有 `POST /command` 返回 422、响应体为空；
HID 描述符/UUID 匹配/输入模式扫掠都正常，`HID_REPORT_ARRIVED` 与 `TOUCH_FORWARDED` 恒 0。

**证据**：

* 最后一次日志（00:10:39）：`CONTROL_COMMAND_SEND path=/command type=modesChanged keys=[type, resources] plistLen=112`
  → `CONTROL_RESP_STATUS status=422 op=COMMAND bodyLen=0`；`setUIContext` 是 `keys=[type, uiContext]` 同样 422。
* 固件 `libNmeCarPlay.so`：`0x421cf` 取 `type`、`0x421f3` 取 `params`；
  `0x4441b test r13,r13` → `0x4441e je 0x4464a` → `0x422b5 mov ebp,0x1a6`(422)。
  `0x447d0` 从 **params** 取 `appStates` / `resources`（不是顶层）。
* `0x443d0` 入站白名单只有 `modesChanged / requestUI / updateFeedback / hidSetInputMode / iAPSendMessage`。
* 11-05 源码备份里 `hidSetInputMode` / `iAPSendMessage` 本来就是 `{"type":…, "params":{…}}`。

**根因**：**规范与固件实现不一致，且 V1.8.14~16 的探针选错了命令**。
Apple CarPlay 规范 Table 23-71 把 `appStates`/`resources` 写成顶层键，V1.7.86 据此改了源码；
但 Buick 的 Cinemo SDK 按 `{type, params}` 解析，`params` 为空就直接 422。
而 `setNightMode` 探针根本不在白名单（是车机→手机命令），它 422 不能证明「端点整体被拒」——
此前据此得出的结论**作废**。

**修法（V1.8.20）**：

1. `modesChanged` 与 `hidSetInputMode` 恢复 `{type, params}` 包裹。
2. `setUIContext` → `requestUI`（白名单内，`params.url`）；探针 → `updateFeedback`。
3. 保留 `g2()` 对 422 的容忍与 `audioPcmBypass` 兜底，避免再次阻断 `RECORD` / `H1()`。

**验收**：`CONTROL_RESP_STATUS status=200 op=COMMAND`；`HID_INPUT_MODE_ACCEPTED > 0`；
车机开始发 `hidSendReport` → `HID_REPORT_ARRIVED` → `TOUCH_FORWARDED` 递增。

> 若 200 之后仍无触摸：问题转到固件侧 `SetCallbacks()`(0x160f0) 的
> `fnModesChanged` 槽位 `[r14+0x60]` 是否为 NULL，以及 Java 层是否调用
> `CarPlayControlClientStart`(vtable 0xe420) 启动 `_carplay-ctrl._tcp` browse（见 `SKILL.md` §14）。

---

## 案例 10：`hidSetInputMode` 从未发出（提交成功 ≠ 已发送）/ Task queued, never sent

**现象 / Symptom**（2026-11-07 11:21~11:25 抓包）：投屏稳定 3.5 分钟
（`VIDEO_PROGRESS units=4183`）、音频在流、`/feedback` 每 2 秒 200，但触摸 0 反应；
`HID_GATE_OPEN` 8 次、`>>> EXECUTE send hidSetInputMode count=3` 7 次，
`HID_INPUT_MODE_ACCEPTED` / `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED` 全 0。

**三个叠在一起的原因，按优先级**：

1. **版本错配（本轮日志作废的直接原因）**：`dumpsys package` 显示设备是
   `1.8.19 / 356`（00:12 安装），而 `dist/Carplay-V1.8.20-debug.apk` 10:27 才出包，从未安装。
   日志里 `modesChanged` 仍是 `keys=[type, resources]`、探针仍是 `setNightMode`
   ⇒ V1.8.20 的 `{type, params}` 修复完全没被验证。
   **抓日志第一件事**：`adb shell dumpsys package io.carplay.app | grep -E 'versionName|lastUpdateTime'`。
2. **HID 任务提交后从未执行**：`openHidGate()` 把 `h0` 任务提交到共享 `Y`
   （`Executors.newScheduledThreadPool(4, new n0(0))`），而 `Y` 的线程被三条常驻循环占住：
   `N0()` 事件读循环（`eventChannel.z()`）、`r2()` timing UDP `receive()` 循环、
   `u2()` 视频发送 `while(!closed)` 循环，再加 `G1()` 的 50 ms 任务。
   证据：`>>> EXECUTE` 7 次，但 `HID_SEND_ENTER`/`HID_CMD_REQ`/`HID_SEND`/
   `HID_INPUT_MODE_SENT`/`HID_CB`/`HID_INPUT_MODE_UNCONFIRMED` **各 0 条**。
   `i1` 每轮被 `HID_SWEEP` 里的 `set(false)` 重置，所以能反复提交、反复排队。
   `kill -3` 栈转储落到 `/data/anr/trace_03`，非 root 读不到，别在这条路上耗时间；
   用逐门日志定位更稳。
3. **两条 `/command` 路径请求头不一致**：`R1()` 的同步 `/command` 走 `EventChannel.w()`
   并带 `X-Apple-Session-ID`；`I0()` 的 queued `/command` 走 `EventChannel.g()`，
   请求头只有 `CSeq` / `Content-Type`。即使任务跑起来，会话归属也可能不成立。

**修法（V1.8.21 / 358，已出包并安装）**：

1. 新增 `hidSendExecutor`（`ScheduledThreadPoolExecutor(1, n0(0))`）专供 `h0`，
   在 `close()` 里与 `Y` 一起 `shutdownNow()`；gate 日志增加 `pool=HID_DEDICATED queued=N`。
2. `I0()` 每个早退分支打点：`HID_SEND_ENTER` / `HID_SEND_ABORT reason=CHANNEL_NOT_READY|GENERATION`
   / `HID_SEND_SKIP reason=PARAMS_NOT_MAP|DUPLICATE`，把「排队」与「早退」一次分清。
3. `EventChannel.g()` 增加带 `Map<String,String> extraHeaders` 的重载（原签名委托给新重载），
   `I0()` 发 `hidSetInputMode` 时补 `X-Apple-Session-ID`，并打 `HID_SEND_SESSION_HEADER`。

**验收（下一轮抓包必看）**：

| 判据 | 期望 |
| --- | --- |
| `dumpsys` versionName | `1.8.21` |
| `CONTROL_COMMAND_SEND path=/command type=modesChanged` | `keys=[type, params]` |
| `CONTROL_RESP_STATUS status=200 op=COMMAND` | > 0（固件 `params` 门） |
| `HID_SEND_ENTER` / `HID_CMD_REQ` / `HID_INPUT_MODE_SENT` | ≥ 1，证明真的发出 |
| `HID_INPUT_MODE_ACCEPTED` / `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED` | 递增 |

> 若 200 且 HID 已发出仍无 report，再回到案例 9 的固件层：
> `fnModesChanged` 槽位 `[r14+0x60]`、`CarPlayControlClientStart`、`[obj+0x466]` 门控。

---

## 案例 11：HID 输入模式 200 被接受，车机仍不发 `hidSendReport` / Mode accepted but no reports

**现象 / Symptom**（V1.8.21，2026-11-07 11:37:32~11:40:37）：投屏 20 fps、音频、
`/feedback` 全部稳定 3 分钟；`updateFeedback` 探针 `ACCEPTED`、`modesChanged` 已是
`keys=[type, params]`、`CARPLAY_SCREEN_MODES_SENT`；`HID_SEND_ENTER` 1 次、
`HID_INPUT_MODE_SENT` 3 条、`HID_INPUT_MODE_ACCEPTED slot=0/1/2 status=200` 3 条（历史首次）。
但 `CARPLAY_COMMAND_RECEIVED` / `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED` 仍全 0，
且**回溯历史所有抓包也是 0** ⇒ 车机从未向手机发过任何入站 `/command`。

**规范 / Spec**（`docs/Apple-CarPlay-Specification.pdf`）：

| 位置 | 结论 |
| --- | --- |
| §23.3.7 (p.477) | Apple 的命令走 **Controller control channel**，车机的命令走 **Accessory control channel**；两者都打在 `/command` 上，每个请求都要 HTTP 响应 |
| §23.3.7.7 / Table 23-74 (p.481) | `hidSendReport` 由车机发出，必带 `hidReport`(data)、`uuid`，可选 `timestamp` |
| §23.3.7.8 / Table 23-76 (p.482) | `hidInputMode`：`0` Default（点击/平移）、`1` Character、`2` Scrolling…… |
| Table 23-37 (p.441) | SETUP 的 `sourceVersion` = Apple device 侧 CarPlay 版本串，例 `"101.7"` |

**固件 / Firmware**（`libNmeCarPlay.so`，VA = 文件偏移）：

| 地址 | 结论 |
| --- | --- |
| `0x4ab29`→`0x4ab62` | 构造 `type=hidSendReport` / `uuid` / `hidReport` 后 `call 0x45100` 发送 |
| `0x45117` | 统一发送门 `cmp byte ptr [rdi+0x466],0`，唯一写入点 `0x484f1`（startSession 成功后置 1） |
| `0x44559`→`0x44624` | `hidSetInputMode` 分支**只**转发给上层回调 `[r14+0x48]`，固件本身不解释 mode 值 ⇒ 回 200 ≠ HMI 启用触摸 |
| `0x42e6c`→`0x8e7b0` | 取 SETUP 的 `sourceVersion`，解析为 `major*10000+minor*100+patch` 存入 `[obj+0x130]` |
| `0x512e7` | `test eax,eax; je 0x51469` —— 版本号为 0 时整段逻辑被跳过 |

`"CarPlus-0.6"` 首字符不是数字 ⇒ `0x8e7b0` 返回 **0** ⇒ 命中零分支。
这是手机侧确定的规范违背，且固件把它当数值用。

**V1.8.22 / 359 改动**（已出包、已安装）：

1. SETUP `sourceVersion` `"CarPlus-0.6"` → `"320.17"`（x.y 格式，与车机 `/info`
   `sourceVersion=320.17.3` 同代），并打 `CARPLAY_SETUP_SOURCE_VERSION`。
2. `s1()` 开头加 `CARPLAY_INBOUND_REQUEST origin=... uri=... len=...`，
   用于区分「车机在 eventPort:5001 上没发」与「手机没读/没分发」。
3. `I0()` 加 `HID_SEND_CMD slot=... mode=... uuid=...`，确认实际发出的输入模式。

**下一轮验收**：`versionName=1.8.22` → `CARPLAY_SETUP_SOURCE_VERSION` →
`CARPLAY_INBOUND_REQUEST` 是否出现 → `HID_REPORT_ARRIVED` / `TOUCH_FORWARDED`。

> 若入站日志仍为 0：问题在车机侧 HMI 桥接（`SetCallbacks()` 0x160f0 的
> `[r14+0x48]` 回调、`AddCarPlayClient` 会话登记）。
> 若入站有流量但无 `hidSendReport`：下一批候选是 SETUP `model` 仍为 `Build.MODEL`(PKJ110)、
> 与 TXT `iPhone15,3` 不一致；以及首次发出的 `hidInputMode=1`（Character）与触摸屏不匹配
> 且 `HidOK()` 在 200 后停止扫掠，永远回不到 `0`(Default)。

---

## 案例 12：`sourceVersion` 修好了，触摸仍无反应 —— 反向控制通道从未建立

**现象 / Symptom**（V1.8.22，11:56:53~11:57:18）：
`CARPLAY_SETUP_SOURCE_VERSION value=320.17 firmware_parsed=3201700` 已生效，
`HID_INPUT_MODE_ACCEPTED status=200` 3 条、`status=422` 0 条；但
`CARPLAY_INBOUND_REQUEST` / `CARPLAY_COMMAND_RECEIVED` / `HID_REPORT_ARRIVED` /
`TOUCH_FORWARDED` 仍全 0。且**本次会话 22 秒就被车机关断**：
`SESSION_FAILURE detail=EVENT_CHANNEL_SocketException message=Socket closed`，
`CARPLAY_EVENT_INPUT point=TRANSPORT_END read_calls=1 raw_bytes=0 messages=0 requests=0 responses=0`。

**证据链 / Evidence**：

| 观察 | 含义 |
| --- | --- |
| 事件通道 `raw_bytes=0 messages=0` | 手机连上 `eventPort:5001` 后，车机一个字节都没发 |
| `INBOUND_REQ chan=EVENT` 0 条（`O0()` 里原有埋点） | 不是 `s1()` 没记录，是真没入站请求 |
| `V160_CTRL_TCP_ACCEPT` / `CTRL_INVITATION_ACCEPTED` / `CTRL_IDENTITY_BOUND` 全 0 | 车机从未连入手机广播的 `_carplay-ctrl` TCP 端口 |
| `CTRL_SERVICE_LIVE name=OPPO_X8U (2) port=0` | 实例名被 NsdManager 冲突改名；车机按蓝牙名匹配时可能对不上 |

**固件 / Firmware**：`0x5303b` 建 `_carplay-ctrl._tcp` Bonjour browser，
`0x53768` 是 BrowseCallback，event=1(Add) → `0x53860` → `0x53c00`（解析 TXT）→ `0x537d0`（入列）。
`0xa429f` `/ctrl-int/1/%s`、`0xa42b3` `AirPlay/320.17.3`、`0xa42c4`
`AirPlay-Receiver-Device-ID`、`0xa42e4` `********** CarPlayControl Request **********`
⇒ **车机作为 CarPlayControl Client 主动连回手机**，才做 `AddCarPlayClient` 登记并桥接 HMI 触摸。
固件里**没有** `/reverse` / `PTTH` / `X-Apple-Purpose`（唯一 `Upgrade` 是 HTTP 428 文本），
所以不是「手机漏发 reverse 升级」的问题。

**结论（结论边界，别写成已解决）**：手机→车机方向（SETUP / `/command` / `modesChanged` /
`hidSetInputMode`）已全部打通并被 200 接受；触摸卡在**车机→手机的反向控制通道**，
车机既不连 ctrl、也不在 eventPort 上发命令。`sourceVersion` 是确定的规范违背但不是触摸根因。

**V1.8.23 / 360**（已出包，设备断开未安装）：

1. `z0()` 的 `hidInputMode` payload 固定 **0 (Default)**。规范 Table 23-76 的 0 才对应
   点击/平移，车机是触摸屏；V1.8.21/22 首次真正发出的却是 mode=1（gate 在 devices=0 时
   SKIP，6s 后 sweep 已递增到 1），而 `HidOK()` 收到 200 就停止扫掠 ⇒ 永远回不到 Default。
   MODE 扫掠保留用于驱动 gate 重发，但 payload 恒为 0。
2. `CarPlayCtrlAdvertiser.onServiceRegistered` 把 mDNS **TXT 全量**写进应用日志
   （`CTRL_SERVICE_TXT`），避免 logcat 握手期被系统日志冲掉。

**下一轮必看**：`CTRL_SERVICE_TXT` 的实例名/端口/10 个键、`V160_CTRL_TCP_ACCEPT`、
`CARPLAY_INBOUND_REQUEST`、`HID_SEND_CMD mode=0`、`HID_REPORT_ARRIVED`。
抓包前先 `adb logcat -G 16M && adb logcat -c`，否则握手期会被冲掉。

> 若 ctrl 仍 0 accept：下一步是 iAP2 侧的 CarPlay 启动门（车机 browse 由蓝牙链路触发），
> 或让服务实例名严格等于蓝牙名（消除 `(2)` 冲突后缀）。

---

## 速查决策树 / Decision tree

```
装的是源码包吗？(APP_BUILD … dex=SRC)
 ├─ 否 → 重装 dist/ 里的包
 └─ 是
    ├─ 有 RFCOMM_SELECTED_PEER_ACCEPTED 吗？
    │   ├─ 否 → 发现/广播问题（车机缓存、广播注册）
    │   └─ 是
    │      ├─ 握手序列走完 0x18001…0x18009 吗？
    │      │   ├─ 否 → 查缺的那一步（案例 1）
    │      │   └─ 是
    │      │      ├─ VIDEO_PROGRESS 递增吗？  否 → 视频通道（案例 1/2）
    │      │      ├─ AUDIO_FLOW 136 包/秒吗？ 否 → 音频（案例 4/5）
    │      │      └─ HID_CAPABILITIES touch=1 且 TOUCH_FORWARDED 递增吗？ 否 → 触摸（案例 3）
```
