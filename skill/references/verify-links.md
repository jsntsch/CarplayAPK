# 五条链路的日志判据 / Log verdicts for the five links

所有判据都来自 `files/connection-log-*.txt`。**按顺序看，前一条不通就不用看后一条。**
Every verdict comes from `files/connection-log-*.txt`. Read in order; a broken early link makes
later ones meaningless.

## 0. 先确认包的身份 / Identify the build first

```
APP_BUILD version=1.7.71 code=308 dex=SRC
```

`dex=SRC` 表示这是源码构建的包。装的是历史外壳包时这里不会出现。
`dex=SRC` marks a source-built APK; it is absent for legacy shell packages.

---

## 1. 发现与广播 / Discovery and advertising

| 期望 / Expected | 含义 / Meaning |
| --- | --- |
| 广播注册成功（`_carplay-ctrl._tcp`） | 本机已在等车机 / this phone is waiting for the head unit |
| 车机主动发起 RFCOMM 连接 | 发现链路通 / discovery works |
| `ENDPOINT_DISCOVERY_START` 后出现候选地址 | 反方向查车机 `_airplay._tcp` 也通 / reverse lookup works |

**不通时的判定**：广播了但车机从不来连 → 车机侧缓存/配对记录问题，先删车机上的旧设备再试。
**When it fails**: advertised but never connected — clear the stale device entry on the head unit.

---

## 2. 蓝牙 + iAP2 握手 / Bluetooth and iAP2 handshake

完整序列（缺一不可）：

```
RFCOMM_SELECTED_PEER_ACCEPTED
TX CMD 0x18001   →  RX CMD 0x10004
TX CMD 0x18048   →  RX CMD 0x10049      (MFi 挑战)
TX CMD 0x1806b   →  RX CMD 0x1006c      (RSA 公钥交换)
                    RX CMD 0x1006e
TX CMD 0x18007   →  RX CMD 0x10008      (INIT_DONE)
TX CMD 0x18009                          (VIDEO START)
```

判定要点 / key points:

* 卡在哪一条 `TX` 之后没有对应 `RX`，就查那一步的**响应处理分支**，不要往后看。
  If a `TX` gets no matching `RX`, debug that response branch only.
* `0x10049`（MFi 验证结果）**必须是「验证成功才继续」**，极性写反会在验证成功时抛协议错。
  The MFi verdict must continue on success; an inverted test throws right when verification passes.
* `0x1006a` 是 RSA 公钥交换请求，漏掉这个 case 会让 `c0` 恒空，随后 `0x1006c` 报
  “unexpected encrypted seed”。
  Miss the RSA-exchange case and the encrypted-seed step fails with "unexpected encrypted seed".

---

## 3. 视频 / Video

| 日志 / Log | 判定 / Verdict |
| --- | --- |
| `NATIVE_VIDEO_FIRST_BYTES video_packets=1` | 首帧到达 / first frame arrived |
| `VIDEO_PROGRESS units=…` 持续递增 | 在流 / streaming |
| 帧率 ≈19.9 fps | 正常 / healthy |
| 有 `VIDEO_PROGRESS` 但屏黑 | 发送侧正常，问题在车机显示 / sender is fine, look at the head unit |

---

## 4. 音频 / Audio

| 日志 / Log | 判定 / Verdict |
| --- | --- |
| `AUDIO_MAIN_MODES_CONFIRMED` | 车机确认了音频主模式 / main audio modes confirmed |
| `NATIVE_AUDIO_PROGRESS … FIRST_VALID_PCM reader_connected=true` | 音频读取线程活着 / reader thread alive |
| `AUDIO_FLOW … sent_packets_total` ≈**136 包/秒** | 实时发送 / real-time |
| `dropped_frames_total` **不增长** | 没有丢帧 / no drops |
| `sent_packets_total` 上万/秒 + 丢帧暴涨 | 同一块 PCM 被反复重发 / same buffer resent |
| `sent_packets_total` 恒 0 且 `source_pcm_observed=true` | 源有数据、发送线程没跑 / sender thread not running |

> `AUDIO_FLOW` 的 `converted_frames` / `sent_frames` 是**窗口增量**，会来回跳，属正常。
> 判断趋势要看 `sent_packets_total` 与 `dropped_frames_total`。
>
> `converted_frames` and `sent_frames` are per-window deltas and will oscillate. Trend lives in
> `sent_packets_total` and `dropped_frames_total`.

---

## 5. 触摸 / Touch

| 日志 / Log | 判定 / Verdict |
| --- | --- |
| `HID_GATE_OPEN devices=N` | HID 门禁打开 / HID gate open |
| `HID_INPUT_MODE_SENT` → `HID_INPUT_MODE_ACCEPTED` | 车机接受 HID 输入模式 / input mode accepted |
| `HID_CAPABILITIES devices=4 touch=1` | 触摸描述符被接受；`touch=0` 表示被拒 / descriptor accepted |
| `HID_TOUCH_DESCRIPTOR … display_bound=true` | 描述符与屏幕绑定 / bound to the display |
| `HID_TOUCH_RANGE logical_x=0:1080 logical_y=0:2160 display=1080x2160 descriptor_driven=true` | 坐标映射就绪 / mapping ready |
| `HID_REPORT_RECEIVED count=N index=0 bytes=12` | 收到触摸报告 / report received |
| `TOUCH_FORWARDED=N` 递增 | 触摸真的转发出去 / forwarded |
| `NATIVE_TOUCH_SENT action=DOWN/MOVE` | 已编码成原生触摸帧（**只在前 4 次与每 100 次打印**） |

失败码 / failure codes:

| 码 / Code | 含义 / Meaning | 动作 / Action |
| --- | --- | --- |
| `HID_TOUCH_UNSUPPORTED reason=DESCRIPTOR` | 描述符解析失败 | 看同行的 `bytes=` 与失败点；多点触控要为每个物理集合分配独立 slot |
| `COMMAND_REJECTED=TOUCH_UNKNOWN_DEVICE` | 车机不认识该 HID 设备 | 描述符没进设备表（上一条的直接后果） |
| `HID_AMBIGUOUS_CONTACT_ID` | 两指抢同一个 slot | slot 不能写死为 1，要按集合递增 |

> `NATIVE_TOUCH_SENT` 有节流（前 4 次 + 每 100 次），第二段会话从 5 起就不再逐条打印。
> 看不到它不代表没发送，看 `TOUCH_FORWARDED` 是否增长。
>
> `NATIVE_TOUCH_SENT` is throttled (first four, then every 100th). Its absence is not evidence of
> failure — watch `TOUCH_FORWARDED`.

---

## 6. 会话健康 / Session health

| 日志 / Log | 含义 / Meaning |
| --- | --- |
| `KEEPALIVE` 5 秒一次 | 保活正常（10 秒会与看门狗等长而断链）/ keep-alive healthy |
| `EVENT_CHANNEL_*` 无 `SocketException` | 事件通道稳定 / event channel stable |
| `CARPLAY_EVENT_FEEDBACK_SENT` + `CARPLAY_RESP op=FEEDBACK` | 事件通道双向通 / bidirectional |
| 会话重启（重新 MFi/WiFi） | 恢复路径会被走一遍，需确认音频/触摸线程一起重建 |

---

## 7. 一页速查 / One-page cheat sheet

```
① APP_BUILD … dex=SRC            包对不对
② RFCOMM_SELECTED_PEER_ACCEPTED  蓝牙通了
③ TX/RX CMD 0x18001…0x18009      握手走完没
④ VIDEO_PROGRESS 递增 ≈20fps     画面在流
⑤ AUDIO_FLOW 136 包/秒 丢帧 0     声音在流
⑥ HID_CAPABILITIES touch=1      描述符被接受
⑦ TOUCH_FORWARDED 递增           触摸生效
```
