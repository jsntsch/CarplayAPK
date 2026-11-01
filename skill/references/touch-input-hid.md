# 触摸 / 方向盘输入（HID）排障

场景：投屏画面正常、帧率稳定，但车机屏触摸和方向盘按键完全无反应。

结论先说：**这是「车机 → 手机」方向的通道问题，不是「手机解析触摸」的问题。** 应用侧解析与注入代码通常是完整的，缺的是车机愿意往那条通道写字节。

实测环境：PKJ110 / Android 16 / ColorOS + 22 款别克君威 myBuick（`10:E8:A7:13:AD:5B`）。

## 1. 方向与通道（最容易搞反的一条）

```
通道：建流时 /info 里 eventPort 那条 TCP，实测 192.168.5.1:5001
      不是独立端口，也不是蓝牙

方向：连上后角色反转 —— 车机当 RTSP 客户端，手机当服务端
      车机发 POST /command，body {type:"hidSendReport", uuid, hidReport}
      手机只需要回 RTSP/1.0 200 OK + 同 CSeq
```

由此得出两条推论，都能省掉大量试错：

1. **HID 报文不走蓝牙。** 蓝牙 RFCOMM 上只有 iAP2 心跳（实测约 10 s 一次），触摸时不会出现突发。别在 iAP2 层找触摸。
2. **`hidSetInputMode` 不是必需的。** 参考实现（orangecar）里压根没有这条命令，车机连上事件通道就直接推 `requestUI` / `changeModes` / `hidSendReport`。它只是「告诉车机用哪种输入模式」的协商，不是开关。

## 2. 三道门，逐门取证

任何一门不通，症状都是同一个：画面正常、触摸无效。必须按顺序验，不能跳。

### 门 1：车机是否声明了可用的 HID 设备

车机在 `/info` 的 `hidDevices[]` 里下发描述符。成功的解析日志长这样：

```
HID_CAPABILITIES devices=3 touch=1
HID_TOUCH_DESCRIPTOR index=0 bytes=62 display_bound=true
HID_BACK_DESCRIPTOR  index=2 bytes=72
HID_TOUCH_UNSUPPORTED index=1 bytes=29 reason=DESCRIPTOR
```

判据：

- `touch=1` 且至少一个描述符 `display_bound=true` → 门 1 通过
- `display_bound=false` → 描述符的 `displayUUID` 与主屏 UUID 不一致，解析时被门控挡在入库之外。这才是需要改 `x0()` 里 UUID 比对的地方
- `reason=DESCRIPTOR` 的项是解析失败（实测 29 字节那条很可能是方向盘/消费类设备），**它不影响触摸**，别和触摸问题混在一起查

同一次会话还能从能力摘要里一次性确认车机到底有什么输入设备：

```
CAPS schema=1 displays=1 main=1280x768@30 input=TOUCH_SCREEN hid=3 hid_valid=true
     touch=1 knobs=1 keys=1 audio_rows=9 audio_valid=true modes_declared=true
     feature_bits=6104040280 negotiated=0 iap_channel=false
```

车机声明了触摸屏 + 旋钮 + 按键三类设备，说明它**具备**反控能力，「车机不支持触摸」这个假设可以排除。

### 门 2：手机是否在「正确的那条通道」上完成协商

这是实测中真正卡住的地方，而且很隐蔽。

发送链（以 OcarPlay 为例）：

```
W()  ->  h0{ c=this, d=List(z0()), e=<y5 通道>, b=timeout }
     ->  h0.run() -> i0.y() -> I0(List, y5, gen)
     ->  I0 每条前再判 d0(y5) :  y5 == this.a0 && y5.n()    ← 只认控制通道 a0
     ->  y5.g("POST","/command",…)  ->  日志 HID_INPUT_MODE_SENT
```

两个致命细节：

- `d0()` 只接受 `this.a0`（控制通道）。把事件通道 `b0` 传进去会被**静默丢弃**——连 `HID_INPUT_MODE_SENT` 都不打印，看起来像「代码没跑」
- `W()` 执行时事件通道对象还不存在。实测插桩输出：

```
23:42:01.123  CARPLAY_EVENT_CONNECTING
23:42:01.197  W: d0 gatePassed==true
23:42:01.197  W: b0=NULL            ← 事件通道此刻是 null
```

也就是说 `hidSetInputMode` 永远落在控制通道上。而控制通道的这条命令对车机不构成「事件通道已激活」的信号。

**排查这个门的最小动作**：在发起点插一行打印，看事件通道对象是否为空、以及 `d0(该对象)` 的返回值。这是唯一能区分「命令发错通道」和「车机不认命令」的手段。

### 门 3：车机是否真的往事件通道写过字节

只看应用日志会漏判（应用可能根本没读）。用 socket 级的证据：

```bash
# 我方到车机的全部 TCP 连接，第 5 列是 tx_queue:rx_queue
adb shell "cat /proc/net/tcp /proc/net/tcp6" | grep -i "0105A8C0" \
  | awk '{print "st="$4, $2, "->", $3, "tx:rx="$5}'
```

`192.168.5.1` 的十六进制是 `0105A8C0`（小端、每字节倒序）。端口同理：`5001=0x1389`、`6030=0x178E`、`7000=0x1B58`。

判据：连接 `st=01`（ESTABLISHED）但 **`tx:rx` 两侧长期都是 0** → 车机一个字节都没写过。同时手机侧的读循环会卡在等 RTSP 头，最终拖垮整个会话：

```
CARPLAY_EVENT_INPUT point=TRANSPORT_END stage=RECORD_HEADER read_calls=1 raw_bytes=0
SCREEN_WRITE_STALLED
```

**裸探测**（区分「我们这条 socket 特殊」还是「车机根本不推」）：

```bash
adb shell "cd /data/local/tmp; rm -f probe.bin; timeout 12 nc -w 8 192.168.5.1 5001 > probe.bin; ls -l probe.bin"
```

新开连接同样 8 秒 0 字节 → 与我们的实现无关，是车机在等某个「激活」信号。

## 3. 已经被证伪的方向（别再试）

| 尝试 | 结果 | 说明 |
|---|---|---|
| 扫描 `hidInputMode` 0→4 | 无效 | 只有握手时那一次真正发出，后续重发全部没落到线上，车机端始终只见过初始值 |
| 硬编码 `hidInputMode=1` | 无效 | 车机回 `HID_INPUT_MODE_ACCEPTED status=200`，但 `HID_REPORT_RECEIVED` 仍为 0。取值不是根因 |
| 对调事件通道读写密钥 | 无效 | 曾误判为密钥装反。还原后行为不变；`y5.i(a,b)` 语义是 `a=入站解密, b=出站加密`，原始传 `(Write, Read)` 本就自洽 |
| 在控制通道上发 `/command` | 无效 | 只产生 `HID_INPUT_MODE_SENT`/`ACCEPTED`，不改变车机是否推送 |
| 怀疑车机没有触摸能力 | 已排除 | `CAPS` 明确 `input=TOUCH_SCREEN hid=3 touch=1 knobs=1 keys=1` |
| 怀疑车机没在看我们的画面 | 已排除 | `main=1280x768@30`，`SOCKET_WRITE_p50=150us queue_depth=0` 连续 8 分钟 |

## 4. 尚未验证的两个候选根因

按可信度排序：

1. **事件通道上没有成功发出过任何一条命令。** 需要在事件通道对象就绪之后再触发一次发送（例如借每秒一次的 `/feedback` 心跳做「对象非空则复位重发」），观察车机是否开始推 `requestUI`。这是最直接的一步。→ **V1.3.0 已实现，见第 7 节**
2. **会话不完整：缺音频流。** 参考实现声明 `MainScreen + MainAudio` 并 SETUP screen + audio 两条流；实测每会话只有 2 次 SETUP（`/setup` 取 eventPort + screen）。`AUDIO_MAIN_MODES_CONFIRMED` 从未出现。车机可能因为会话不完整而把交互通道整体关闭。

## 5. 排障顺序（照抄）

```
1. 确认车机能力：CAPS 里是否有 input=TOUCH_SCREEN / hid_valid=true / touch=1
   └─ 没有 → 车机本就不支持反控，收工
2. 确认描述符入库：HID_TOUCH_DESCRIPTOR ... display_bound=true
   └─ 有 UNSUPPORTED/reason=DESCRIPTOR → 那是别的设备，不影响触摸
3. 确认协商落在哪条通道：插桩打印发送时的通道对象 + d0 结果
   └─ 对象为 null / d0 为 false → 修时序，别改 hidInputMode 取值
4. 确认车机是否写字节：/proc/net/tcp 的 tx:rx + nc 裸探测
   └─ 两侧都是 0 → 车机在等激活，转到候选 1/2
5. 以上都不通 → 检查会话完整性（audio stream 是否 SETUP）
```

## 6. 会话存活时间是重要旁证

实测三次会话存活 79 s / 106 s / **62 s**，其中一次精确 `22:52:25 → 22:53:25 = 60.0 s`。

固定 60 秒被拆 = **超时**，不是随机掉线。收尾序列固定为
`read_calls=1 raw_bytes=0` → `SCREEN_WRITE_STALLED`，即卡在等事件通道的第一个报文。

所以「投屏一会儿就断」和「触摸没反应」很可能是同一个根因的两个表现。修好事件通道，两个症状会一起消失——**别把它们当两个问题分别调**。

## 7. V1.3.0：把 HID 搬上事件通道 + 重发 + 通道验收

候选 1 的落地实现。改动全部在 `secured/i0.smali` + 新增 `secured/hx.smali`，
补丁脚本 `CarPlay-Build/patch_hid_event.py`（幂等，含 `.hidopt.bak` 备份）。

### 7.1 先搞清 `y5` 的两副面孔（否则会写错）

`y5` 这个类是客户端与服务端**同一个**：

| 方法 | 语义 | 门禁 |
|---|---|---|
| `g(method,path,ctype,body)` | 发请求并挂 pending future | **要求 `t==true`**，否则抛 `IOException("Queued command requires encrypted main control")` |
| `z(y5$c)` | 进入收发循环：读一条 → 是响应则 `c(resp)` 完成 pending；是请求则 `cb.a(resp)` 拿响应体 → `D()` 写回 | 同时把 `t=true` |

所以「在事件通道上发 POST /command」**代码层面是通的**，前提是 `z()` 已经调用过。
`N0()`（由 `b0(2)` 提交）就是干这个的，它跑在 `K0()` 里，比 `W()` 晚约 74 ms。

顺带纠正一处措辞：事件通道的 TCP 连接是**我方 `connect()`**（`l0()` → `socket.connect`），
不是车机连进来。所谓「手机当服务端」指的是 **HTTP 语义**：车机在这条已建连接上发
`POST /command`，我方回 `200`。

### 7.2 补丁做了什么

```
W()                 记录 PENDING / LASTAT（本会话发起过 HID 发送）
I0() 发送点          y5.g() 的通道由 HidChannel(p2) 决定：
                       EVENT_OK && b0!=null && b0.n()  -> b0（事件通道）
                       否则                            -> p2（控制通道 a0）
I0() carrier 日志    carrier=EVENT / carrier=CONTROL 由 if-eq p1,p2 实际判定，不再硬编码
J0() 成功回调        HidOK()：事件通道成功 -> ACCEPTED=true（停）；控制通道成功 -> CTRLOK=true（继续验事件通道）
J0() 失败回调 x2     HidFail()：只有 LASTEVENT 才计数，EVFAIL>=2 -> EVENT_OK=false 永久回退
P0()（1 s tick）     HidRetryTick()：未 ACCEPTED 时每 6 s 清 J + 复位 i1 + 重发 W()
```

状态机收敛路径（正常会话）：

```
t=0     W()  -> carrier=CONTROL（b0 还没建）
t=74ms  K0() -> b0 建立 -> N0() -> z() -> t=true
t=6s    重发 -> carrier=EVENT
          ├─ 200 -> HID_EVENT_ACCEPTED，停
          └─ 超时/4xx -> EVFAIL=1
t=12s   重发 -> 仍 EVENT -> 失败 -> EVFAIL=2 -> HID_EVENT_REJECTED -> fallback CONTROL
t=18s   重发 -> carrier=CONTROL -> 200 -> CTRLOK=true
t=24s   停（EVENT_OK=false && CTRLOK=true）
```

最坏 24 s 收敛，最多 12 次重发，不会无限刷。

### 7.3 判读方法（这才是本节的用处）

```bash
adb logcat -c
# 只留关键行；注意 :V，用 :I 会把 Log.d 全滤掉
adb logcat -s CarPlayHID:V CarPlusMetrics:V | grep -E "HID_|CARPLAY_EVENT"
```

| 日志 | 含义 | 下一步 |
|---|---|---|
| `HID_INPUT_MODE_SENT slot=0 carrier=EVENT` | 事件通道上确实发出去了 | 等下一行 |
| `HID_INPUT_MODE_ACCEPTED status=200 carrier=EVENT` | **车机接受事件通道上的 POST /command** | 看是否开始出现 `HID_REPORT_RECEIVED` |
| `HID_INPUT_MODE_UNCONFIRMED carrier=EVENT reason=TIMEOUT_OR_IO` | 发出去了但车机不回 | 计 EVFAIL |
| `HID_INPUT_MODE_UNCONFIRMED carrier=EVENT reason=RESPONSE status=4xx` | 车机明确拒绝 | 计 EVFAIL |
| `HID_EVENT_REJECTED -> fallback CONTROL` | 已判事件通道不接受，回控制通道 | 转候选 2（音频流） |
| `HID_EVENT_ACCEPTED -> HID on event channel` | 已锁定事件通道 | 触摸仍无反应 → 候选 2 |

**这张表的价值**：把「车机到底认不认事件通道上的 /command」从猜测变成一次实验。
ACCEPTED + 仍然 `HID_REPORT_RECEIVED=0` → 事件通道这条路已经走通，问题不在通道选择，
去查会话完整性（audio SETUP）或车机的推送条件。

### 7.4 反编译侧的两个坑

1. **`p1` 这类参数寄存器可以被 `move-result-object` 覆盖**。`I0()` 里我拿 `p1`
   （原本是 `List`）当通道临时变量，后续 3070 行 `iget-object p1, ->i1` 会重新赋值，
   中间没有把它当 List 用——改之前必须逐行确认存活区间，否则验证器报 VerifyError。
2. **`if-eqz` / `if-nez` 在这种补丁里最容易写反**。`ACCEPTED` 那行第一版写成
   `if-eqz`（= 等于 0 则跳），语义变成「未确认就退出」，重发永远不会发生。
   写完必须念一遍：「等于 0 跳到哪」。

## 8. ⚠️ 方向性纠正（2026-10-29 实测推翻前 3 周结论）

**触摸根本不在 AirPlay 通道上。** 前 7 节围绕 `eventPort` / `y5.g()` / `hidSetInputMode`
做的全部改造（V1.1.0 → V1.3.7）**方向错误**，因为 AirPlay 只负责画面。

### 8.1 真正的双通道架构

```
画面：OcarPlay ──AirPlay screen stream──> 车机 1280x768@30（单向，无输入回传）
触摸：车机 ──CarLife 协议──> com.baidu.carlife.oppo ──> Car+(com.oplus.ocar)
                                              └──> 虚拟屏 displayId=10825 (1280x768) 注入
```

实证命令（必须会）：

```bash
# 1. 看虚拟屏与输入端口绑定关系 —— port=<none> 即无物理输入设备，触摸只能软件注入
adb shell dumpsys input | grep -E "Viewport VIRTUAL|FocusedDisplayId"
#   Viewport VIRTUAL: displayId=10825, uniqueId=oplus_mirage_car:825,
#                     logicalFrame=[0,0,1280,768], isActive=[1]      ← port=<none>
#   FocusedDisplayId: 10826

# 2. 看 Car+ 投屏目标屏配置（应等于上面那个 displayId）
adb shell settings get global ocar_cast_display        # 10825
adb shell settings get global ucar_casting_state       # 1 = 投屏中

# 3. 看 Car+ 是否真在虚拟屏上渲染（appBounds 应等于车机分辨率）
adb logcat -d | grep -E "OCarLauncherImprovedActivity" | grep appBounds

# 4. 输入设备清单里**不会**出现任何 CarLife 注入设备，别去那里找
adb shell dumpsys input | grep -E "^  [0-9]+: |Sources:"
```

### 8.2 三条互相印证的「此路不通」证据

| 证据 | 内容 |
|---|---|
| 官方版对照 | 0.8.53（vc87，jadx 272 类完整解出）连接后**同样不能触摸**，且 `MotionEvent`=0、`ACTION_DOWN`=0、`hidSendReport` 只在命令白名单里，**没有任何接收分支** |
| 响应体为空 | `HID_CMD_RESP len=42` = 8B head + 1B obj + 1B offset + 32B trailer ⇒ 空 dict `{}`，不是 `{"status":true}`。之前的 `ACCEPTED(200)` 是 HTTP 层假成功 |
| 协议本身单向 | AirPlay 系统镜像官方口径："Cannot tap, swipe, or type. No control command goes back"。反向控制属 CarPlay(iAP2)/CarLife，不属 AirPlay |

全值扫描（V1.3.7 `hidInputMode` 0..5）也全部 `HID_REPORT_RECEIVED=0`。

### 8.3 CarLife 侧为何也失效（下一条线索）

```
CarConnect: [CarlifeConnectionFlow] getCarBrand NumberFormatException: "" under radix 16
CarConnect: [BluetoothConnectionReceiver] SupportedCarCastBleWhiteList contains null
SplitScreenAppReader: SPLIT_TOUCH support check: com.baidu.carlife.oppo = false
CAPS: iap_channel=false  negotiated=0
```

即：**车机没上报 carBrand → Car+ 匹配不到 CarCast 白名单 → 触摸注入能力降级**。
`com.oplus.ocar` 是系统应用，改不了；但车机标识是蓝牙层给的，属于可干预范围。

### 8.4 唯一干净的判别实验（先做这个，别急着改代码）

> ⚠️ 本节的结论边界已被第 9 节修订：车机在 CarPlay 会话下**确实**回传触摸。
> 这里的实验判的是「CarLife 通道回不回传」，不是「车机有没有触摸能力」。

**直接用 `com.baidu.carlife.oppo`（百度 CarLife 官方 App）连车机，测触摸。**

| 结果 | 结论 |
|---|---|
| CarLife 直连能触摸 | CarLife 通道可回传，问题在 OcarPlay 链路（Car+ 白名单/carBrand），沿着 8.3 查 |
| CarLife 直连也不能触摸 | 只说明 **CarLife 通道**不回传；不能据此断言车机没能力（见第 9 节） |

这一步成本极低、判别力最强，**任何触摸问题都必须先做它**。

### 8.5 别再动的地方

- `eventPort` / `y5` / `hidSetInputMode` / `hidInputMode` / `W()` / `HidRetryTick`
  —— 这些只影响 AirPlay 通道，对触摸无因果作用，继续改只是浪费时间
- 唯一值得保留的副作用：自举 tick 链修好了 `/feedback` 心跳，会话从 ~60s 延长到 130s+

## 9. iPhone 侧实证：车机确实具备触摸回传能力（2026-10-25 sysdiagnose）

**这一节修正第 8 节的适用范围**：第 8 节的「协议单向」只适用于 AirPlay 镜像会话，
不等于「车机不会回传触摸」。车机的触摸回传在 **CarPlay(iAP2) 会话**中被实测到。

### 9.1 证据链（iPhone sysdiagnose，全部可复核）

样本：`Downloads/Ocarpaly/sysdiagnose_2026.10.25_16-01-26+0800_iPhone-OS_iPhone_21H216`

```bash
# 1. 车机身份（蓝牙 + WiFi 双证）
cat logs/Bluetooth/CoreCapture/bluetooth_status.txt | grep -A3 myBuick
#   myBuick  Address: 10:E8:A7:13:AD:5B  Paired: Yes
python -c "import plistlib;..."   # WiFi/com.apple.wifi.known-networks.plist 内含 SSID "myBuick GS"
# WiFi/security.txt 中该 SSID 密码条目 cdat = 2026-10-25 07:58:16 UTC = 15:58:16 +0800

# 2. CarPlay 会话进程时间线（ps.txt）
#   15:31 carkitd → 15:32 CarPlay.app → 15:58 ACCCarPlayService → 15:59 CarPlayTemplateUIHost
#   16:01:26 采集 sysdiagnose（WiFi 已断，network_status 全 None）

# 3. 全包无 CarLife 痕迹（重要：别在这里找 CarLife）
grep -ail -E "carlife|baidu" summaries/*.log *.txt   # 无输出
```

### 9.2 结论

| 事实 | 含义 |
|---|---|
| 15:58 保存 `myBuick GS` WiFi 密码 → 15:59 CarPlay 界面起来 | 走的是**无线 CarPlay**：蓝牙负责配对/唤醒，视频与输入都走 WiFi |
| 无线 CarPlay 的触摸能生效 | 车机触摸屏 → 车机协议栈 → **iAP2 EA 会话的 HID 组件** → 手机，链路完好 |
| 同一车机在 AirPlay 镜像下无触摸 | 车机只对 **CarPlay 会话**开放输入回传，对「第三方 AirPlay 投屏」不开放 |

即：**不是车机没有触摸能力，而是 AirPlay 镜像这种会话形态不在车机的输入回传白名单里。**

### 9.3 对第 8.4 节判别实验的修订

原表述「CarLife 直连也不能触摸 → 车机侧不提供触摸回传，彻底结案」**过强**，应改为：

| CarLife 直连结果 | 正确结论 |
|---|---|
| 能触摸 | CarLife 通道可回传，沿 8.3（carBrand / CarCast 白名单）查 |
| 不能触摸 | 只能说明 **CarLife 通道**不回传；车机能力已在 CarPlay 下证实存在，不能据此结案 |

### 9.4 判定 CarLife 通道是否真的回传（Android 侧，不依赖车机 root）

车机上摸屏幕，同时看手机端输入子系统有没有事件落地：

```bash
# A. 是否有触摸事件真正到达 Android
adb shell getevent -l        # 期望看到 ABS_MT_POSITION_X/Y + BTN_TOUCH（来源设备名会显示）
# B. CarLife 是否注册了注入用输入设备
adb shell dumpsys input | grep -iE "carlife|baidu|virtual|oplus"
# C. CarLife 协议层日志
adb logcat -d | grep -iE "carlife|baidu|touch|input|SplitScreen"
```

判读：

- 车机上点得动 + 手机 `getevent` 有 `ABS_MT_*` → **回传成功**，OcarPlay 可复用该通道
- 车机上点得动 + 手机 `getevent` 无事件 → 触摸是车机本地处理（Car+ 虚拟屏内自消化），**没有回传**
- 车机上 CarLife 界面点不动 → 会话本身没建立，先解决连接

> `getevent -l` 必须带 `-l`（标签模式），否则只有裸码，看不出是哪个设备。
> 若 CarLife 用 `uinput` 注入，设备名通常是 `carlife` / `virtual-touch` 之类，会在 `dumpsys input` 的 Devices 列表里出现。

### 9.5 下一步的正确方向

既然：
1. 车机触摸回传走 **iAP2 EA 会话 HID**（9.2 已证）
2. 本项目的 iAP2 链路层已与真机 `myBuick` 跑通（见 `iap2-link-layer.md` 第 5 节）
3. OcarPlay 内部本就实现了 CarPlay 控制会话（`0x180xx`，见 `IAP2_COMMANDS.md`）

那么触摸的**第一优先级**不是继续调 AirPlay 的 `/command`，而是确认：
**iAP2 的 EA 会话（`0xAA00`/`0xAA01`）是否建立，以及是否注册/订阅了 HID 组件。**

这才是车机唯一愿意送出触摸的通道。

## 10. EA 会话与 HID 组件的实证结论（2026-10-30 代码 + 日志双向核对）

数据源：`Downloads/Ocarpaly/OcarPlay-log-20261022-22款别克君威.txt`（0.8.51 重建版实测）
+ `CarPlay-Build/jadx-Carplay-V1.0.5/sources/`（Java）+ `OcarPlay-dec-patched/smali/`（当前 V1.3.x）。

### 10.1 EA 会话：建立成功，全流程 327 ms

```
13:42:42.976  IAP2_LINK_READY
13:42:42.999  RX 1D01  IdentificationInformation
13:42:43.001  IAP2_IDENTIFIED            （回 1D02 并发 AA00）
13:42:43.013  TX AA00 seq=124 bytes=16   ← StartEASession
13:42:43.040  RX AA01 918B               ← 车机回 EA 会话 + 证书链（27 ms）
13:42:43.046  TX AA02 seq=125 bytes=40   ← 认证挑战
13:42:43.339  RX AA03 138B               ← 挑战签名（293 ms）
13:42:43.339  MFI_KEY_POSSESSION_VERIFIED
13:42:43.340  TX AA05 seq=126 bytes=16   ← EA 认证完成
```

**结论：EA 会话不是瓶颈。** 任何「先怀疑 EA 没建起来」的假设都可以直接排除。
（`TX_SKIPPED_NOT_ADVERTISED id=5702 / 4300` 只是车机 RECEIVE 表里没有这两个 ID，与 EA 无关。）

### 10.2 HID 设备：车机声明了触摸屏，OcarPlay 也登记了

```
HID_TOUCH_DESCRIPTOR index=0 bytes=62 reports=1 display_bound=true uuid_kind=STRING
HID_TOUCH_UNSUPPORTED index=1 bytes=29 reason=DESCRIPTOR
HID_BACK_DESCRIPTOR  index=2 bytes=72 reports=1
HID_CAPABILITIES devices=3 touch=1
CAPS ... input=TOUCH_SCREEN hid=3 hid_valid=true touch=1 ... negotiated=0 iap_channel=false
```

**车机在 `/info` 里明确给了 3 个 HID 设备，其中 index=0 是绑定了 display 的触摸屏。**
来源是 `i0.x0(Map)`：从 `/info` 的 `hidDevices` 数组解析 `hidDescriptor` / `uuid` / `displayUUID`，
按描述符类型分拣到三个 Map：`I0`=触摸、`J0`=返回键、`K0`=媒体键。

### 10.3 订阅：hidSetInputMode 发了、车机 200 了、mode 0~5 全扫过

```
13:47:44.238  HID_INPUT_MODE_SENT slot=0 carrier=CONTROL
13:47:44.239  HID_INPUT_MODE_SENT slot=1 carrier=CONTROL
13:47:45.237  HID_INPUT_MODE_ACCEPTED slot=0 status=200 carrier=CONTROL
13:47:45.238  HID_INPUT_MODE_ACCEPTED slot=1 status=200 carrier=CONTROL
```

报文由 `i0.z0()` 构造，参数是 **uuid + hidInputMode**（不是 slot）：

```
{"type":"hidSetInputMode","params":{"hidInputMode":MODE,"uuid":<deviceUuid>}}
```

⚠️ 日志里的 `slot=0/1` 只是遍历下标，**不代表协议里有 slot 字段**。
V1.3.7 把 `hx.MODE` 从 0 扫到 5，每次都 200。

### 10.4 关键否证：`report_received=false` 是硬编码文案，不能当证据

```java
// i0.java:731
this.n.a("HID_INPUT_MODE_ACCEPTED slot=" + i2 + " status=" + bVar.h()
        + " carrier=CONTROL report_received=false");   // ← 写死的字符串
```

真正能判定的只有日志里有没有 `HID_REPORT_RECEIVED`。实测：**8 次会话，0 条。**

```
grep -c "HID_REPORT_RECEIVED\|hidSendReport\|TOUCH_FORWARDED"  → 0
```

### 10.5 接收代码其实是齐全的

`i0.s1(y5.b, String)` 处理入站 `/command`，按 `type` 分发，支持：

| type | 处理 |
|---|---|
| `updateViewArea` / `requestSiri` / `updateVehicleInformation` / `requestUI` | 远程 UI / 语音 |
| `iAPSendMessage` | 转发到 iAP2 |
| `hidSetInputMode` | 回 200 |
| **`hidSendReport`** | **取 `params.uuid` + `params.hidReport` → `TOUCH_FORWARDED=` → 注入** |
| `forceKeyFrame` / `setLimitedUI` / `setNightMode` / `changeModes` | 其他 |
| 其他 | `OTHER` |

即：**「车机发 hidSendReport → 我们注入触摸」这条路是通的、有代码的**。
所以结论不是「没实现」，而是「车机从没发过一条」。

### 10.6 剩下三个没排除的疑点（按优先级）

| # | 疑点 | 依据 | 验证方式 |
|---|---|---|---|
| 1 | **车机 3 个设备只下发了 2 个 input mode**。index=1 的 29B 描述符解析失败被跳过，它的 uuid 从未收到过 `hidSetInputMode`。若车机要求全部 HID 设备都进入输入模式才启用触摸，这里就是死锁 | 日志只有 slot=0/1；`HID_TOUCH_UNSUPPORTED index=1` | 把 index=1 的 uuid 也发一遍 `hidInputMode=1` |
| 2 | `negotiated=0 iap_channel=false` 出现在 CAPS 行 | CAPS 打印于 13:47:44，而同一次连接 iAP2 早在 13:42:43 就 READY | 打印该字段的取值来源，确认它是否参与门禁 |
| 3 | HID 报告可能不走 event channel 的 `/command`，而走独立 HID stream 或 iAP2 控制会话 | 入站分发里没有任何 HID 相关 CSM 消息 ID | 抓 iPhone 侧的 iAP2 流量对照 |

### 10.7 下一步的唯一动作

**先做 10.6 的第 1 条**——这是唯一一个「代码侧可控、且从没试过」的变量。

做法：在 `i0.x0(Map)` 里，把 `HID_TOUCH_UNSUPPORTED index=1` 那个描述符解析失败的设备
也登记进一个「待设 input mode」列表（只存 uuid，不需要描述符），
让 `z0()` 对它也下发一次 `hidSetInputMode`。然后看是否出现第一条 `HID_REPORT_RECEIVED`。

## 11. 疑点 #1 已排除：3 个 HID 设备全部下发 hidSetInputMode，仍无一条报告（V1.3.8）

### 11.1 做了什么

原代码 `i0.z0()` 只遍历 `I0/J0/K0` 三张 Map 生成 `hidSetInputMode` 列表。
`x0()` 里描述符解析失败的设备（`HID_TOUCH_UNSUPPORTED index=1 reason=DESCRIPTOR`）
不会进任何一张 Map，于是它的 uuid 从没收到过 `hidSetInputMode`。

V1.3.8 的改法（`patch_hid_pend.py`）：

| 位置 | 改动 |
|---|---|
| `hx` | 新增 `PEND:Ljava/util/ArrayList;` + `PendClear()` / `PendAdd(String)` |
| `i0.x0()` | `L1()` 之后 `PendClear()`；`check-cast v10, [B` 之后无条件 `PendAdd(v12)`——**与解析结果无关** |
| `i0.z0()` | `.locals 10→11`，开头 `move-object v10, p0`；`:cond_2` 处先 prune 再遍历补发 |

补发只需 uuid，不需要描述符对象，所以绕开 `p/h/i` 三张 Map 的构造依赖。

### 11.2 实测结果（vc250）

```
HID_PEND_ADD uuid=s:3 / s:2 / s:4          ← 3 个设备全登记
HID_PEND_REPLAY total=3 pend=1              ← prune 后只剩缺失的那 1 个
HID_INPUT_MODE_SENT slot=0 / slot=1 / slot=2
HID_INPUT_MODE_ACCEPTED slot=2 status=200
HID_REPORT_RECEIVED                          ← 0 条
```

**结论：疑点 #1 排除。** 设备补全了、车机也 200 了，报告依然一条没有。
「必须 3 个设备都进入输入模式才解锁触摸」这个假设不成立。

### 11.3 三个必须记住的坑（本次全部踩过）

1. **条件码连续写反 5 次**：`if-eqz p0`（null 才返回）写成了 `if-nez p0`；
   `contains` 判断反；`hasNext` 判断反；prune 的 `containsKey` 判断反（导致 3 个 uuid 全被删，`pend=0`）。
   **写 if-* 时必须在旁边用注释写清「零则跳/非零则跳 + 业务含义」，再对照一遍。**
2. **不要在 `x0()` 里借寄存器**：借了 v3/v4/v5 存 Map，而 v4 在该点已被声明为 `Integer`
   → `VerifyError: register v4 has type Reference: java.util.Map but expected Integer`。
   `x0` 有 `.locals 18` 但绝大多数都被占用，**加逻辑一律去 `z0()`，那里寄存器可控**。
3. **prune 与遍历必须分成两段独立循环**：把过滤塞进补发循环体，会让 `:pend_loop`
   回跳点的 v4/v5 出现 `Map` 与 `String`/`int` 的类型冲突 → VerifyError。
   两段循环各自用寄存器，才能过 verifier。

### 11.4 剩余疑点

| # | 疑点 | 状态 |
|---|---|---|
| 1 | 3 个设备只下发 2 个 input mode | **已排除**（11.2） |
| 2 | `negotiated=0 iap_channel=false` | 待查 |
| 3 | HID 报告可能不走 event channel 的 `/command` | 待查 |

> 注：`HID_INPUT_MODE_ACCEPTED ... report_received=false` 里的 `false` 是硬编码字符串，
> 永远不能作为「没收到报告」的证据；唯一判据是 `HID_REPORT_RECEIVED` / `TOUCH_FORWARDED`。

## 12. 触摸回传断路的双重根因（2026-10-30 实测 + Car+ 反编译）

数据源：本次车机点击实测 logcat + `Car+ 车联_17.31.2.apk` 反编译（dexdump/jadx）。

### 12.1 实测事实

用户点击车机屏后：`HID_REPORT_RECEIVED` / `TOUCH_FORWARDED` / CarLife 收到的触摸坐标 = **全部 0**。
手机侧唯一的 MotionEvent 是 `deviceId=6 displayId=0`（手机本机 touchpanel），被 InputDispatcher 丢弃
（`no touchable window at (568,1794) on display 0`）—— 是手机本地手势，不是车机回传。

### 12.2 OcarPlay 实际跑两条并行连接

```
iAP2 路（OcarPlay 自己的无线组件，tag=CarPlusWireless，pid=io.carplay.app）：
  00:41:59  Wireless session started
  00:42:03  RFCOMM_SELECTED_PEER_CONNECTED        ← 蓝牙 RFCOMM
  00:42:04  IAP2_LSP_SESSION sid=1/3 · SELECTED_CONTROL sid=1
  00:42:04  IAP2_TX AA00 ... MFI_KEY_POSSESSION_VERIFIED_PINNED_APPLE_CHAIN_UNVERIFIED

CarLife 路（OcarPlay 拉起 Car+，com.oplus.ocar/.CarlifeActivity）：
  00:42:12  launchedFrom=io.carplay.app → com.oplus.ocar
  00:42:12  SPLIT_TOUCH support check:com.baidu.carlife.oppo = false
  00:42:13  getCarBrand NumberFormatException ：For input string: "" under radix 16
  之后       carlife_other_connect_status(null) 每 5s 轮询，恒 null
```

### 12.3 CarLife 路根因（代码级实锤）

`com.oplus.ocar.connect.carlife.CarlifeConnectionFlow.y()` 返回品牌码，逻辑：

```java
public int y() {
    int v0 = 0;
    String brand = connectionStateFlow.v().k;   // h0.k 字段 = 车机品牌标识
    if (brand == null) brand = "";
    int radix = 16, N = 8;
    if (brand.length() >= 8) brand = brand.subSequence(0, 8).toString();
    try { return Integer.parseInt(brand, 16); }
    catch (NumberFormatException e) {
        Log("report addCarkit getCarBrand NumberFormatException ：" + e.getMessage());
    }
    return 0;
}
```

- 品牌标识来自 `com.oplus.ocar.connect.engine.h0.k`（String 字段）。
- 别克君威这台**CarPlay 车机不通过 CarLife 上报品牌** → `h0.k` 空 → `parseInt("", 16)` 抛异常 → 品牌码 0。
- 品牌码是 `addCarkit`（登记车机设备）的关键，异常后 `carlife_other_connect_status` 恒 null → CarLife 会话从未建立。

### 12.4 iAP2 路的新线索：认证状态可疑

```
MFI_KEY_POSSESSION_VERIFIED_PINNED_APPLE_CHAIN_UNVERIFIED
```

= 密钥拥有权验证**通过**，但「固定苹果证书链」标记为 **UNVERIFIED（未验证）**。
车机可能因此拒绝开放完整的输入回传（HID 触摸），与第 11 节疑点 #2（`iAPChannel` 未协商）互为表里。

### 12.5 方向结论（重要）

| 路径 | 状态 | 结论 |
|---|---|---|
| CarLife 路（拉起 Car+） | carBrand 空 → 连接恒 null | **死路**：车机是 CarPlay 车机，不支持 CarLife，别再投入 |
| iAP2 路（OcarPlay 自身） | EA 会话建立 + MFI 密钥通过，但苹果链 UNVERIFIED | **正路**：触摸回传唯一可能通道 |

**下一步聚焦 iAP2 路**：查 `PINNED_APPLE_CHAIN_UNVERIFIED` 是否是车机拒绝 HID 输入回传的根因，
以及 `h0.k`（品牌标识）能否从蓝牙 GATT/广播补上（这条同时决定 CarLife 是否还能救）。

## 13. 第 1 条调查结论：PINNED_APPLE_CHAIN_UNVERIFIED 是误判，真正的根因是 iAP2 HID 组件整体缺失（2026-10-30）

数据源：`jadx-Carplay-V1.0.5/sources/io/carplay/app/secured/a3.java`（iAP2 协议栈）
+ `i0.java`（HID 订阅/命令层）+ 全解码树 grep。

### 13.1 PINNED_APPLE_CHAIN_UNVERIFIED = 认证成功，不是故障（纠正第 12.4 节）

`a3.java` 的 `a0(y2.c)` 方法处理 `i2 == 43523`（0xAA03 挑战签名）时：

```java
// a3.java:695-727（节选）
if (i2 == 43523) {                       // 0xAA03 挑战签名
    if (m5Var == null || this.t) { ... OUT_OF_ORDER ... }
    if (!m5Var.j(y2.v(cVar.b, 0))) {     // 验证签名失败
        throw new IOException("MFI_CHALLENGE_SIGNATURE_FAILED");
    }
    if (this.B0) { ... }                 // 首次使用：记住证书
    this.t = true;                       // ★ 认证通过标志
    this.a.h("MFI_KEY_POSSESSION_VERIFIED_PINNED_APPLE_CHAIN_UNVERIFIED");  // ★ 成功日志
    x(43525, new byte[0]);               // 回 0xAA05 EA 认证完成
    if (!this.C0 || this.s) { W(); return; }   // 继续启动会话
    else { this.D0 = true; x(7424, ...); }
}
```

判定：这条日志**紧跟 `this.t = true`（认证通过）之后**，是认证成功的标志，不是错误。
- 「KEY_POSSESSION_VERIFIED」= 已通过挑战-签名证明车机持有 MFI 私钥；
- 「PINNED_APPLE_CHAIN_UNVERIFIED」= 未走苹果官方 pinned CA 证书链（**所有第三方/破解
  CarPlay 实现的固有特征**，它们没有苹果根证书链）。

认证成功后代码继续 `x(43525)` → `W()`/发识别信息，会话正常建立（与第 10 节 327ms 走完吻合）。
**因此「认证不完整导致车机拒绝 HID 回传」不成立。第 12.4 节作废。**

### 13.2 真正的根因：OcarPlay 的 iAP2 栈没有 HID 组件

`a3.java` 的入站消息分发方法 `a0(y2.c)` 只处理 7 个消息 ID：

| 消息 ID | 十六进制 | 含义 |
|---|---|---|
| 7425 | 0x1D01 | IdentificationInformation 识别信息 |
| 43521 | 0xAA01 | EA 会话响应 + 证书链 |
| 43523 | 0xAA03 | 挑战签名 |
| 43524 | 0xAA04 | 认证失败 |
| 43526 | 0xAA06 | 串行（serial）相关 |
| 44546 | 0xAE02 | 电源 |
| 22275 | 0x5703 | WiFi 凭证 |

发送侧另有 0xAA00/0xAA02/0xAA05、0x1D00/0x1D02；CarPlay 控制会话 0x180xx（画面投屏 + 内容加密，
见 `IAP2_COMMANDS.md`）。

**全解码树 grep `0x66xx`（iAP2 HID 组件消息范围）只有 `r2.smali` 里一个无关的 `0x666666` 常量。**
`a3.java` 无任何 `hid` 关键字、无 HID 组件消息 ID 分支。

而现有的 HID 代码（`i0.x0()` 解析 hidDevices、`i0.z0()` 发 hidSetInputMode、`i0.s1()` 处理
hidSendReport）全部是 **JSON over HTTP（y5 通道 `/info` `/command`）**：

```java
// i0.java:3776（z0() 生成的其实是 JSON 消息，不是 iAP2 二进制消息）
arrayList.add(Map.of("type", "hidSetInputMode",
                     "params", Map.of("hidInputMode", 0, "uuid", it.next().a)));
```

### 13.3 结论：两套系统，触摸回传的「报告层」根本不存在

| 层 | 车机真实协议 | OcarPlay 现状 |
|---|---|---|
| 能力声明（hidDevices） | CarPlay capabilities 字段（displays/hidDevices） | ✅ `i0.S(map)`/`x0()` 解析到了 3 个设备 |
| 订阅（hidSetInputMode） | iAP2 HID 组件消息 | ⚠️ 只有 JSON over HTTP 版本，非 iAP2 |
| **报告（触摸坐标）** | **iAP2 HID 组件报告消息（0x66xx）** | ❌ **完全缺失** |

即：设备「声明了」、订阅「200 了」（HTTP 控制面自嗨），但车机真正回传触摸的
**iAP2 HID 组件报告通道，OcarPlay 的 iAP2 栈从来没实现过**。所以 8 次会话 0 条报告，
不是「车机不配合」，而是「这条通道在代码里不存在」。

### 13.4 下一步（唯一能通向触摸的路）

要在 iAP2 栈补 HID 组件接收，需要三块：

1. **确定 iAP2 HID 组件的消息 ID**（0x66xx 具体值，对照 MFi iAP2 规范或参考实现，
   如 libimobiledevice / 开源 CarPlay 栈）。
2. **HID report 描述符解析**：车机在 `hidDevices[].hidDescriptor` 里给的 62/29/72 字节
   就是 HID 报告描述符（`i0.x0()` 已用 `z0.e(bArr)` 解析过），据此把 report 数据解出
   `ABS_MT_POSITION_X/Y`、`BTN_TOUCH` 等字段。
3. **坐标注入**：`i0.s1()` 里 `hidSendReport` 分支已有「取 uuid + hidReport → TOUCH_FORWARDED
   → 注入」的代码，可作为落点复用。

> 风险提示：iAP2 HID 组件消息 ID 属 MFi 保密规范，公开资料不全，这块需要外部参考实现佐证，
> 不要凭猜测硬编码 ID。若拿不到规范，此路可能长期停滞。

## 14. 第 13 节被证伪：触摸回传走 AirPlay event channel，不是 iAP2（orangecar 开源实现）

数据源：`sbuysse/orangecar`（GPL-3.0，完整开源 CarPlay phone-side 实现 + 字节级协议文档，
实测 Jaguar F-PACE 量产车，Rust）。

### 14.1 关键证伪（一句话）

> "The car sends HID reports over the **AirPlay event channel**."
> "The Jaguar ... **declares no iAP2 HID component**, so the event channel is its only input path."

**第 13 节「iAP2 HID 组件（0x66xx）缺失是根因」的判断是错的。** iAP2 只负责 link/identification/
authentication/control session（GPS、power、NowPlaying），**视频、音频、触摸全部走 AirPlay（RTSP over
NCM/WiFi）**。车机根本没有 iAP2 HID 组件，触摸报告的物理通道就是 AirPlay event channel。

### 14.2 正确的架构（orangecar 实测）

```
iAP2（蓝牙 RFCOMM / USB）：link → identification → authentication(EA) → control session
                                        （只承载 GPS / power / NowPlaying）

AirPlay（RTSP over NCM 网络 / WiFi）：
  手机 ──主动连──> 车 eventPort（TCP，角色反转：车发请求，手机回 200）
    ├─ GET /info        → displays / audioFormats / hidDevices / modes / eventPort
    ├─ 视频：H.264 流
    ├─ 音频：双向流
    └─ event channel    ← 车发 POST /command（含 hidSendReport）→ 手机回 RTSP 200
```

### 14.3 触摸报告（hidSendReport）的字节级格式

event channel 上的消息（角色反转，车 → 手机）：

```
POST /command RTSP/1.0
{ type: "hidSendReport", uuid: <device uuid>, hidReport: <data> }
手机回：RTSP/1.0 200 OK\r\nCSeq: n\r\n\r\n
```

`/info.hidDevices[]` 每项含 `uuid` / `name` / `hidDescriptor`（二进制报告描述符）。

Jaguar 触摸屏报告（`hidDescriptor` 解析出）：

| 字段 | 位偏移 | 大小 | Usage |
|---|---|---|---|
| tip switch | 0 | 1 bit | Digitizer 0x33 |
| padding | 1 | 7 bits | — |
| X | 8 | 16 bits | Generic Desktop 0x30 |
| Y | 24 | 16 bits | Generic Desktop 0x31 |

- 报告总长 **5 字节**，LSB 位序，**无 report id**
- 逻辑坐标 **0..32767**（15 位）
- 缩放：**逻辑 32768×32768 → capture size（采集/编码尺寸，不是显示尺寸）**
  `x_pixel = (logical_x * capture_width) >> 15`；早期用显示尺寸缩放的版本，触摸全落在右下角

### 14.4 hidSetInputMode 根本不是协议必需项

orangecar **从不发 `hidSetInputMode`**，车机照样主动发 `hidSendReport`。因此：

- OcarPlay 的 `hidSetInputMode` / V1.3.0~V1.3.8 的 mode 0..5 扫描，**对触摸回传无因果作用**；
- 「订阅 200 才有报告」是错误心智模型——车机发报告不需要订阅触发。

### 14.5 三个解析陷阱（orangecar 明确记录）

1. **数值可能是字符串**：Jaguar 把数字发成 `"100"` / `"0x8000"`，解析要兼容 int / 十进制串 / 0x hex。
2. **绝不硬编码报告字节**：usage 必须从 `hidDescriptor` 动态解析。
3. **Constant 位**：Jaguar 把关键字段标 `Input(Constant, Array)`（`81 01`）却仍带 usage 且随按键变化，
   **有 usages 的字段无视 constant 位照常解码，无 usages 才视为 padding**。

### 14.6 对 OcarPlay 的重新定位（正确结论）

| 层 | 事实 |
|---|---|
| 触摸报告通道 | AirPlay event channel（`POST /command` 的 `hidSendReport`） |
| OcarPlay 接收代码 | **已存在**：`i0.s1(y5.b, String)` 的 `hidSendReport` 分支（取 uuid+hidReport → 注入） |
| iAP2 HID 组件 | **车机本来就没有**，OcarPlay 无需实现 |

**因此真正的问题收敛为：OcarPlay 的 event channel 上为什么收不到车机的 `hidSendReport`？**
候选（按概率）：① event channel 没建立/连错 port；② 入站消息没路由到 `i0.s1()`；
③ 收到了但数值按 int 解析、遇到字符串 `"0x8000"` 抛异常被吞。

### 14.7 下一步（唯一动作）

查 OcarPlay 的 event channel 建立链路：`eventPort` 从哪来（`/info`？）、b0 事件通道连的是哪个 port、
入站 `POST /command` 是否真的进了 `i0.s1()`。同时核对实测日志里有没有 `EVENT_CONNECT` / event channel
的入站记录——之前第 3 段发现「事件通道 b0 被 z() 置 t=1」，需确认这是否阻塞了**入站**。

## 15. 决定性结论：hidSetInputMode 必须发在事件通道，且 y5.l() 才是事件通道发送法（V1.3.9）

数据源：反编译 `i0.smali` / `y5.java` + **cardeck**（`shaonialife/cardeck`，Android 手机做 CarPlay 发送端，
奥迪 MIB3 实车全通，2026-10-11）+ orangecar。

### 15.1 三个候选根因在代码层面全部排除

第 14.6 节的三个候选逐一验证，**OcarPlay 接收侧代码链完整且正确**：

| 环节 | 结论 | 代码位置 |
|---|---|---|
| eventPort 来源 | `/info` 响应的 `eventPort` 字段，`D1(map,"eventPort")` 转 int，校验 1..65535 | `i0.K0()` |
| b0 连哪个 port | `l0(eventPort, 15000)` → `connect(InetSocketAddress(this.c, port))`，有取证日志 `EVTCP connect host=<IP> port=<port>` | `i0.L0/l0()` |
| 密钥 | `Events-Salt` 派生 `Events-Write/Read-Encryption-Key`，`b0.i(wKey, rKey)` | `i0.K0()` |
| 入站路由 | `y5.z(j0)` 收发循环 → 收到请求回调 `j0.a(y5.b)` → `i0.O0(bVar)` → `i0.s1(bVar,"EVENT")` | `y5.z()` / `j0` / `i0.O0()` |
| s1 分发 | `endsWith("/command")` → 解析 type → `sparse-switch`，`hidSendReport`=case 0x4，取 uuid+hidReport → `x0.f([B)` 解析报告 | `i0.s1()` |

即：**不是 OcarPlay 收不到，是车机根本不往 event channel 推**（第 3 节已实测 socket 0 字节 + 裸探测 0 字节）。

### 15.2 决定性反转：hidSetInputMode 是必需的，但必须发在事件通道

orangecar（Jaguar，**有线 USB**）说「hidSetInputMode 非必需、从不发车机也推」——**这只对 Jaguar 成立**。

cardeck（奥迪 MIB3，**无线**）实测相反：

> 「`hidSetInputMode`（uuid 原样字符串回传）武装 HID 后车机才开始发触摸」
> 「车机命令必须走**事件通道 socket** 上的 POST /command，发在 RTSP 控制连接上车机收不到」

别克君威是**无线** CarPlay 车机，行为与奥迪同类：**需要事件通道上的 hidSetInputMode 来武装 HID**。

### 15.3 根因：y5.g() 只能在控制通道发，HidChannel 被硬编码强制 CONTROL

`y5` 双通道发送语义（反编译 `y5.java` 实锤，第 7.1 节**把 g() 门禁写反了**）：

| 方法 | 通道 | 门禁/行为 | 挂载 map |
|---|---|---|---|
| `g()` | **控制通道 a0** | `if (this.t || f==null || g==null) throw` → 要求 **t==false** | `s`（由控制通道 `w()` 的 `e()` 完成） |
| `l()` | **事件通道 b0** | 内部 `this.t = true`，无 t 门禁 | `r`（由 `z()` 循环的 `c()` 完成） |
| `z()` | 事件通道循环 | 置 `t=true`，读消息：响应→`c()`，请求→回调 `s1` | — |

关键：**第 7.1 节写成「g() 要求 t==true」是错的**。实际是 g() 要求 t==false（控制通道）。
事件通道 b0 的 `z()` 置 t=true，所以 g() 在 b0 上必抛「Queued command requires encrypted main control」。

于是上一轮把 `HidChannel()` 硬编码成「强制返回控制通道」（`HID_CHAN=CONTROL(forced)`），
`I0()` 永远用 `g()` 发在控制通道。第 10.3 节实测 `carrier=CONTROL` 即此。

**而车机不认控制通道上的 hidSetInputMode → 从不武装 HID → 从不推 hidSendReport → 8 次会话 0 报告。**

### 15.4 修复（V1.3.9，`patch_hid_event_channel.py`，两处最小改动）

1. `HidChannel()`：`b0` 就绪（非空且 `n()==true`）→ 返回 `b0`；否则回退控制通道 `p1`。
2. `I0()` 发送点：`HidChannel` 返回 `b0`（`p1 != p2`）→ 用 `l()`；返回 `a0`（`p1 == p2`）→ 用 `g()`。

时序（正常会话）：

```
t=0     W() -> b0 尚未建 -> HidChannel 返回 a0 -> g() 发控制通道（旧行为，无害）
t=74ms  K0() -> b0 建立 -> N0() -> z() -> t=true
t=6s    HidRetryTick 重发 -> HidChannel 返回 b0 -> l() 发事件通道 ★ 车机武装 HID
t=6s+   车机开始推 hidSendReport -> HID_REPORT_RECEIVED / TOUCH_FORWARDED
```

### 15.5 判读方法（真机验证用）

```bash
adb logcat -c
adb logcat -s CarPlayHID:V CarPlusMetrics:V | grep -E "HID_CHAN|HID_INPUT_MODE_SENT|HID_REPORT_RECEIVED|TOUCH_FORWARDED|EVTCP"
```

| 日志 | 含义 | 下一步 |
|---|---|---|
| `HID_CHAN=CONTROL`（t=0 首条） | b0 还没建，首次发控制通道（无害） | 等 6s 重发 |
| `HID_CHAN=EVENT`（t=6s 重发） | **已改发事件通道** | 看车机是否开始推 |
| `HID_INPUT_MODE_SENT ... carrier=EVENT` | 事件通道上确实发出去了 | 等 `HID_REPORT_RECEIVED` |
| `HID_REPORT_RECEIVED` / `TOUCH_FORWARDED` | **车机已武装 HID 并回传触摸（成功判据，之前恒 0）** | 收工 |
| 仍恒 `HID_CHAN=CONTROL` | b0 一直没就绪，或 `n()` 恒 false | 查 `EVTCP connect` 与 b0 建立 |
| `HID_CHAN=EVENT` 但无报告 | 事件通道已走通但车机仍不推 | 转查会话完整性（audio SETUP / iAP2 控制会话） |

---

## 16. 2026-10-31 定论：车机主动推 `hidSendReport`，全程不需要 `hidSetInputMode`

第 15 节那套「先武装 HID 才会收到报告」的判断，在别克君威上**不成立**。证据来自同一份 iPhone
sysdiagnose（iOS 17.7.1，`sysdiagnose_2026.10.25_16-01-26+0800`），只是换了取证方法才拿到原文：
`UnifiedLog 0.3` 对 iOS 17 的归档解析出 0 行，改用 `scripts/sysdiag_strings_probe.py` 直读后，
全量 329 万条字符串里 CarPlay 相关 9670 条。

### 16.1 原始行（可逐条复核）

```
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:07.797909
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:08.997503
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:09.54463
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:12.384137
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:12.429157
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:22.315526
APTCReceivedCommandCache - hidSendReport, Body 96, Received at 15:59:22.318392
HTTP Request:  POST /command RTSP/1.0
```

同一份日志里，三个关键字命中数：

| 关键字 | 命中 | 结论 |
|---|---|---|
| `hidSendReport` | 7 | 触摸报告确实在事件通道上，Body 96 |
| `hidSetInputMode` | **0** | iPhone 从未协商输入模式 |
| `hidDevices` | **0** | iPhone 从未声明 HID 设备 |
| `requestUI` | **0** | 车机也不发 UI 请求 |

车机→手机的命令全集只有两类：`hidSendReport`（7 次）和 `iAPSendMessage`（13 次，Body 97/187/192/247）。

### 16.2 由此确定的三件事

1. **`hidSetInputMode` 不是触摸的必要条件。** 别克君威与 orangecar 的 Jaguar 一样，车机无条件推报告。
   第 15 节「V1.3.0 补丁让车机武装 HID」的因果链要撤掉 —— 那条补丁从未被证明产生过报告。
2. **`hidDevices` 声明也不是必要条件。** 车机不需要手机告诉它「我有触摸屏」，它推的是**它自己的**触屏事件。
3. **真正的门是「会话形态」。** 只有车机自己经 `_carplay-ctrl` 下发 `connect` 的会话，才会被登记为
   CarPlay 会话；APK 自拨 7000 建立的会话在车机侧只是普通 AirPlay 镜像，所以事件通道 0 字节。

### 16.3 请求行版本（下一个坑，先记着）

车机在事件通道上发的是 `POST /command RTSP/1.0` —— 是 **RTSP/1.0**，不是 HTTP/1.1，也不是 `EVENT/1.0`。
自写解析器如果写死 `HTTP/1.1`，会在事件通道第一次有字节时把包丢掉，现象是「socket 有数据但应用没反应」。
建议同时容忍 `RTSP/1.0` / `EVENT/1.0` / `HTTP/1.0` / `HTTP/1.1`。

### 16.4 取证顺序（取代第 5 节的顺序）

```
1  车机有没有来连 ctrl        CTRL_TCP_ACCEPT / CTRL_INVITATION_ACCEPTED
   └─ 0 → 停。会话形态不对，别碰 HID
2  事件通道 rx 有没有字节      /proc/net/tcp 的 tx:rx 第 5 列
   └─ 0 → 同上，回到 1
3  有字节但没处理             → 查请求行版本（16.3）
4  有处理但注入不到屏幕        → 这时才轮到 Car+ 虚拟屏 / 注入层
```

配套：`docs/three-way-v179.md`（APK ⇄ 原生 ⇄ 固件三方对照）里有这一轮的完整差异表与 P0 缺陷清单。

### 16.5 落地：V1.7.11 起停用「武装 HID」，并把门禁挪到拨号之前

2026-10-31 晚，按 16.1~16.4 的结论改了 APK（`Carplay-V1.7.11.apk`，vc280）：

| 改动 | 位置 | 效果 |
|---|---|---|
| 停用 HID 扫描链 | `i0.W()` 方法头 `return` | 不再发 `hidSetInputMode` / `hidDevices`；原逻辑完整保留，删 3 行即可恢复 |
| 邀请门禁前移 | `WirelessCarPlayService.z1` 的 `:try_start_11` 之前 | 从「拨号后等」改成「等到了才拨」，窗口 8 s |
| `MarkInvited` 条件码 | `hx.MarkInvited` `if-eqz` → `if-nez` | 原写法的 `sput` 永不执行，`INVITED` 恒 false |

**为什么「停用」而不是「删掉」**：这条扫描链有 6 个版本的历史（V1.3.0~V1.7.9），
每一版都在赌「先发协商，车机才推报告」。把它整体留着、方法头短路，
一是万一将来遇到别的车机（cardeck 报告过奥迪 MIB3 需要 `hidSetInputMode`）能一行恢复，
二是避免大改引入 VerifyError —— 这个类的 `.locals` 已经被占满（见 android-pitfalls 第 9 节）。

**`MarkInvited` 那半行是本轮最贵的一处**：它让「车机从不下发邀请」这个结论本身不可信。
以后凡是把结论建立在「某条日志没出现」上，先确认那行日志的**调用路径不会提前 return**。
判别方法与 `d0.c` 那类「假日志」同源，见 `android-pitfalls.md` 第 9 节末尾。

### 16.6 实机验证：触摸能力在 `hidSetInputMode=0` 下协商成功（2026-10-31 V1.7.12）

在别克君威实车（手机 OnePlus PKJ110 作 CarPlay 源）上，V1.7.11/12 停用「武装 HID」后连接，
`CarPlusWireless` 日志显示：

```
V1711_HID_ARM_DISABLED=1 native_hidSetInputMode_count=0
HID_TOUCH_DESCRIPTOR index=0 bytes=62 reports=1 display_bound=true uuid_kind=STRING
HID_TOUCH_UNSUPPORTED index=1 bytes=29 reason=DESCRIPTOR
HID_CAPABILITIES devices=3 touch=1
CAPS schema=1 ... input=TOUCH_SCREEN hid=3 hid_valid=true touch=1 ...
```

即：**`touch=1` / `TOUCH_SCREEN` / `hid_valid=true` 在 `native_hidSetInputMode_count=0` 下达成**。
这是铁律 28（别克不需要 hidSetInputMode）的实机终局证据。唯一 `HID_TOUCH_UNSUPPORTED index=1`
是车机给了两条 touch 描述符、主屏那条（index 0, 62B, `display_bound=true`）被接受、index 1（29B）被拒，
整体 `touch=1`，不影响主屏触摸。

> 仍待确认：能力协商成功 ≠ 车机真的把触摸事件回传并渲染。需在车机屏实际点按验证 app 侧 HID 报告入口。
