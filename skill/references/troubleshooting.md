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
