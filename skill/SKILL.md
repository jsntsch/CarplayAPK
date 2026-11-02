---
name: ocarplay-carplay-connect
description: 在 Android 手机上建立或排障无线 CarPlay 链路（OcarPlay 等第三方实现接车机）。覆盖蓝牙 iAP2 链路层握手、RFCOMM 通道探测、WiFi AP 关联、mDNS 服务发现、AirPlay RTSP 建流、事件通道与 HID 触摸输入七个环节。当出现「车机零响应」「车机反复连接/反复敲门」「RFCOMM 连上但收不到数据」「MARKER 发了没人理」「WiFi 密码拿不到」「mDNS 发现不到车机」「RTSP 连不上」「投屏正常但触摸方向盘无反应」「投屏一会儿就断」等问题时使用。
agent_created: true
---

# OcarPlay 无线 CarPlay 连接

把 Android 手机作为无线 CarPlay 的一端与车机（实测别克君威 myBuick）建链，或定位链路上的断点。

## 何时使用

- 车机反复连接、每隔一两秒敲门一次
- 车机接受 RFCOMM 连接但一个字节都不发
- App 连发 MARKER / SYN，车机毫无反应
- iAP2 握手卡在某个阶段（DETECT / NEGOTIATE / 等 ACK）
- 车机不下发 0x5703 WiFi 凭据
- WiFi 关联失败，或关联成功但后续没动作
- mDNS 发现不到车机 `_airplay._tcp`
- RTSP 连不上 / 建流失败
- **投屏画面正常，但车机屏触摸、方向盘按键完全无反应**
- **投屏能看一会儿就被断，时长很固定**
- **广播了 `_carplay-ctrl` 但车机从不来连、从不下发 `connect`**
- 需要从 iOS sysdiagnose 取原生行为做对照（含「解析出 0 行」的情况）
- **点某个按钮 / 切到某个页面就闪退**（`AbstractMethodError` / `VerifyError` / `IllegalAccessError`，见铁律 61/62）

不适用：USB 有线 CarPlay、纯 iAP2 配件（充电桩、音箱）。

## 角色方向（先定这个，否则整轮白做）

```
手机 = AirPlay 发送端（画面源）
车机 = AirPlay 接收端（解码显示）
```

所有推拉方向的结论都从这里推：

| 数据 | 方向 | 通道 |
|---|---|---|
| 画面 / 音频 | 手机 → 车机 | 屏幕流 TCP 6030 等 |
| RTSP 请求 | 手机 → 车机 | 7000 |
| **触摸 / 方向盘** | **车机 → 手机** | **AirPlay 事件通道 TCP 5001 上的 `hidSendReport`**（原生实测 7 条 / Body 96）；CarLife→Car+ 虚拟屏是另一条独立路 |
| AirPlay HID（事件的 eventPort，实测 5001） | 车机 → 手机 | **这才是触摸的正路**；「0 字节」不是通道不存在，是会话没被车机认成 CarPlay 会话（铁律 28） |
| iAP2 身份与凭据 | 手机 ⇄ 车机 | 蓝牙 RFCOMM |

> ⚠️ **2026-10-31 复核（覆盖 10-29 的旧结论，以此为准）**
> 同一份 iPhone sysdiagnose 用新方法（`scripts/sysdiag_strings_probe.py`）重扫后拿到逐条原文：
> 触摸走的是 **AirPlay 事件通道 TCP 5001**，报文是车机侧发来的 `POST /command RTSP/1.0` + `hidSendReport`（Body 96），
> 重连会话里 15 秒内 **7 条**。同时 `hidSetInputMode`、`hidDevices`、`requestUI` 命中 **均为 0**。
> 也就是说：**车机在 CarPlay 会话里无条件推报告，不需要手机先「武装 HID」**。
> 之前「iAP2 EA 会话 HID」的推断只对车机侧（对 USB/其它主机）成立，不是无线 CarPlay 的实际路径。
> 我们事件通道 0 字节的原因不在通道，在**会话形态**：车机没经由 `_carplay-ctrl` 下发 `connect`，它就不把这场会话当 CarPlay。
> **所以：先验「车机有没有来连 ctrl / 有没有下发 connect」，再去谈 HID。** 详见 `references/touch-input-hid.md` 第 16 节。

## 三阶段架构

```
阶段 1  蓝牙 RFCOMM —— 只负责「敲门 + 身份」，不传画面，也不传触摸
        └─ iAP2：MARKER → SYN/LSP → SYN|ACK → ACK → 1D00/1D01 → MFi 认证
        └─ 0x5702(请求凭据) → 0x5703(SSID/密码/信道/安全类型)
阶段 2  WiFi —— 车机自己是 AP，手机作为 STA 关联
        └─ 车机热点 SSID 只在「无线 CarPlay 待机」时才会开
阶段 3  CarPlay Control + mDNS + AirPlay RTSP —— 真正的数据面
        └─ 本机广播 _carplay-ctrl → 车机下发 connect 命令
        └─ _airplay._tcp(SRV→Android.local→IP) → 车机 IP:7000 → PAIR_SETUP → ... → 屏幕流 6030
        └─ 事件通道 eventPort(5001) 建立后角色反转，车机才可能推 requestUI / hidSendReport
```

**关键认知**：蓝牙握手成功 ≠ 能投屏。画面 100% 走 WiFi + AirPlay。蓝牙的唯一作用是把「这台手机」和「即将建立的 AirPlay 会话」对上号。

**触发方向**：原生实现里，最后一步是**车机**通过手机广播的 `_carplay-ctrl` 通道反向下发 `connect`，
手机收到后才去连 7000。我们主动撞 7000 能连通，但缺少这个握手起点，车机可能不认可会话。

**配对路线**：`/pair-setup`（SRP+PIN，**注册**配对）和 `/pair-verify`（Curve25519，**认证**会话）是两条路。
原生连车机**只做 pair-verify**，因为它的配对记录是在 iAP2/蓝牙阶段建立的。
所以拿 SRP 去怼 `/pair-setup` 收到 `470` 属正常——不是参数错，是方向错。详见 `references/airplay-pairing-spec.md`。

## 混淆名 ↔ 语义名（读代码先看这张表）

APK 是 R8 全混淆的，`i0` / `y5` / `hx` 这类名字没有语义。工作区里有一份
**可读版 Java 工程** `OcarPlay-Java-V1729/`（基于 V1.7.29，39 个类按证据重命名），
和本 skill 里的混淆名一一对应。写文档、讨论问题时优先用可读名，
要落到 smali 改代码时再查这张表。

| 可读名 | 混淆名 | 职责 |
|---|---|---|
| **AirPlaySession** | `secured.i0` | SETUP/INFO/RECORD、POST /command、iAP2；4418 行，App 的心脏 |
| **EventChannel** | `secured.y5` | 事件通道（车机→手机）：触摸、按键、`/command` |
| **ControlChannel** | `secured.hx` | 控制通道：邀请 + `V1711_DIAL_GATE` |
| **EventChannelConnector** | `secured.s2` | 事件通道重试外壳 `EVENT_CONNECT_*` |
| **ControlRequestParser** | `secured.s0` | 控制通道 HTTP 报文头解析 |
| **AppleHttpHeaders** | `secured.xah` | `X-Apple-Client-Name` / `X-Apple-ProtocolVersion` |
| **Iap2Csm** | `secured.y2` | iAP2 CSM 分片重组 |
| **Iap2WirelessSetup** | `secured.e1` | Wi-Fi 上的 iAP2 通道 |
| **CarPlayCtrlAdvertiser** | `prototype.h` | 广播 `_carplay-ctrl._tcp` + 自 resolve 校验 |
| **AirplayServiceProbe** | `prototype.j` | 发现 `_airplay._tcp` 并选候选 |
| **WifiNetworkMonitor** | `prototype.m` | `CURRENT_WIFI_*` 网络变化 |
| **PairingSession** | `prototype.r0` | AirPlay 配对主流程（2515 行） |
| **MfiCertificateVerifier** | `secured.a3` | MFi 证书链校验 |
| **PairingTlv8** | `secured.l1` | `/pair-setup` 的 TLV8 |
| **AvcEncoder** | `prototype.f` | H.264 编码 |
| **VideoCipher** | `secured.j1` | 视频加密 |
| **AppLogger** | `prototype.d0` | 日志（注意 `d0.c` 会抛异常，见铁律 30） |
| **TouchFlow** | `secured.v4` | `NATIVE_TOUCH_FLOW` 触摸转发统计 |

完整 39 项 + 每一条的命名依据 + 「为什么另外 248 个类不改名」，见工程里的
`RENAME-MAP.md`。**V1.7.29 的闪退点**（原 `h$z`）现在读作
`CarPlayCtrlAdvertiser` 里的 `ResolveListener` 内部类，方法名 `onServiceResolved`。

## 铁律

### 第一组：iAP2 协议（九条）

| # | 铁律 | 违反后的症状 |
|---|---|---|
| 1 | MARKER 是 **6 字节**（`FF 55 02 00 EE 10`），解析循环下界必须 ≤ 6 | 车机连发 10 次 MARKER 全被当「非 iAP2 数据」丢弃 |
| 2 | MARKER 是**双向探测**，收到必须立刻回一个 | 车机发满 10 次（约 9 秒）后放弃并断开 |
| 3 | 我方已发过 SYN 时，收到车机 SYN 必须回 **SYN\|ACK（0xC0）**，不能只回纯 ACK（0x40） | 车机每秒重传 SYN，共 10 次后断开 |
| 4 | iAP2 链路包的 `session_id` 必须 = **0**，不是控制会话号 | 车机把包当脏数据，零响应 |
| 5 | RFCOMM 的 **SCN 是动态分配**的；`isConnected=true` 只代表连上了某个通道，不代表连对服务 | 连上后零字节，误判为「车机不响应」 |
| 6 | 判断 WiFi 是否关联成功**不能只读 `WifiInfo.ssid`**（Android 13+ 会脱敏成 `<unknown ssid>`） | 明明已连上，App 却一直等，发现流程一步都不走 |
| 7 | mDNS 的 `_airplay._tcp` 服务名是 `CarPlay`，**SRV target 却是主机名 `Android.local.`**，必须做 SRV→主机名→A/AAAA 两跳 | 把服务名当主机名去解析 → 全部失败，或卡死在 IPv6 链路本地地址 |
| 8 | 「连上」的判据是**屏幕流 TCP 6030 稳住**，不是 7000 返回 200 | 误报成功，随后车机关流，用户只看到黑屏 |
| 9 | 自动重连**只重跑 AirPlay 段**，不要重跑蓝牙/WiFi，并加「already started」幂等保护 | 双会话互踢，重连比首连还慢 |

### 第二组：链路与取证（五条，全部实测踩过）

| # | 铁律 | 违反后的症状 |
|---|---|---|
| 10 | 判「车机连上了但没服务」不能只看 ACL，要**数 L2CAP PSM**：只有 `BT_PSM_SDP` 而没有 `BT_PSM_RFCOMM` = 手机没在广播服务记录 | 车机每 1~2 秒敲门一次、每次 150 ms 就断，误判成「配对坏了」去反复重配 |
| 11 | `listenUsingRfcommWithServiceRecord` 的 SDP 记录**随监听窗口到期而消失**；常见写法是 90 秒，且分布在三处 | 车机在某段时刻能连上、其余时刻永远敲门。修的时候只改一处等于没改 |
| 12 | 取证时 logcat 过滤等级必须低于插桩等级：`-s TAG:I` 会把 `Log.d` **整条滤掉** | 得出「代码根本没执行」的错误结论，进而推翻正确判断 |
| 13 | 报文发不出去先怀疑「对象还没建好」：同一会话里控制通道先于事件通道存在 | 报文静默落到错误通道，日志只显示「发出了」，对端却从未收到 |
| 14 | 改 smali 门控前先确认条件码语义：`if-eqz` 等于 0 则跳、`if-nez` 不为 0 则跳、`if-ne` 不相等则跳 | 不会报错，只会让判断恒真/恒假，症状像「逻辑本来就不通」 |
| 15 | 「在事件通道上发命令」要先满足 `y5.z()` 已调用（门禁 `t==true`），否则 `g()` 直接抛 `Queued command requires encrypted main control` | 命令发出去了但永远等不到响应，误判为「车机不接受」 |
| 16 | **dex 方法头的 `outs_size` 不会因为你在 smali 里写了个多参数 invoke 就自动变大**。原方法最大 5 字 invoke 时 outs=5，你插一条 7 字 invoke 必崩：`VerifyError: invalid argument count (7) exceeds outsSize (5)`。要么把参数压到 ≤ 原 outs，要么用 `invoke-*/range`（**并不能绕过**，一样受 outs 限制） | 编译 100% 通过、`cmd package compile -m verify` 也报 0，但一进会话就闪退，且堆栈只指向方法名不指行号 |
| 17 | **改完 dex 必须用 `dexdump` 做 invoke/outs 全量校验**，别信 `dex2oat verify`：后者实测对这类问题漏报 | 白让用户重连一次、白装一次包 |
| 18 | 自举周期任务的幂等要用**实例引用**（`if-eq obj, p0`）而不是静态布尔标志 | 静态布尔跨会话无法复位 → 新会话永远起不来链 / 或链数指数爆炸 |
| 19 | **动手改触摸前先分清「车机有没有能力」和「这条通道回不回传」**。车机触摸回传能力已在 iPhone 无线 CarPlay 下实测存在（走 iAP2 EA 会话 HID）；AirPlay 镜像本身单向无回传。判别实验要看的是「CarLife 通道回不回传」，不是「车机有没有触摸」（见 touch-input-hid 第 9 节） | 会在完全错误的通道上消耗数周，且每次都有「200 成功」的假象支撑错误结论；也容易把「CarLife 不通」误判成「车机没能力」而错误结案 |
| 20 | 判断「命令是否生效」不能只看 HTTP 200，要看**响应体**。bplist 长度 42B（8B头+1B对象+1B偏移+32B尾）= 空 dict `{}`，等于敷衍 | 把空响应当成功，导致重试逻辑提前停止、永远发现不了真相 |
| 21 | **每写一条 `if-*` 就地在旁边注释「零则跳/非零则跳 + 业务含义」，再逐条对照一遍**。本项目已连续写反 9 次（`if-lt`/`if-ge`/`if-eqz`/`if-nez`），最近一次是把 prune 的 `containsKey` 判断写反，把 3 个 uuid 全删成 `pend=0` | 逻辑不生效但不报错，日志看起来「跑了」，只能靠耗时实测才能发现 |
| 22 | **不要在 `i0.x0()` 里借寄存器加逻辑**。`.locals 18` 几乎全被占用，借到的 v4 在该点是 `Integer`，存 Map 直接 `VerifyError: register v4 has type Reference: java.util.Map but expected Integer`。加逻辑一律放 `z0()`（`.locals` 可扩到 11，寄存器可控） | 编译通过、dex 校验通过，一进会话就闪退 |
| 23 | 在已有循环里插过滤逻辑，**必须另起一段独立循环**（prune 完再遍历）。塞进同一循环体会让回跳点的寄存器出现 `Map` 与 `String`/`int` 的类型冲突 | VerifyError，且报错行号指向回跳标签，极难定位 |
| 24 | **判断一条认证/状态日志是「成功」还是「失败」，必须看它前后的控制流（在 `t=true`/`return` 之前还是之后），不能只看字符串字面**。`MFI_KEY_POSSESSION_VERIFIED_PINNED_APPLE_CHAIN_UNVERIFIED` 是认证**成功**日志（紧跟 `this.t=true`），UNVERIFIED 只指「未走苹果 pinned CA 链」这一第三方实现的固有特征，不是故障 | 把成功当失败，把排查引到「认证不完整」这条死路 |
| 25 | **「声明了设备 / 订阅 200 / 命令有响应」≠「报告通道存在」**。`hidDevices`、`hidSetInputMode` 200 只是能力声明 + 订阅层；触摸报告真正的传输层是 **AirPlay event channel 的 `hidSendReport`**（见铁律 26）。且 `hidSetInputMode` 根本不是协议必需项（orangecar 从不发，车机照样发报告） | 在声明/订阅层反复优化（V1.3.0~V1.3.8），永远等不到报告 |
| 26 | **协议走向不要凭空推断，先查开源参考实现 `sbuysse/orangecar`**（GPL-3.0，完整 CarPlay phone-side 实现 + 字节级协议文档，实测 Jaguar）。它明确：iAP2 只管 link/identification/authentication/control session，**视频/音频/触摸全走 AirPlay**；触摸报告在 event channel 上以 `POST /command {type:hidSendReport, uuid, hidReport}` 回传，5 字节报告（tip+X/Y u16 LE，逻辑 0..32767，缩放到 capture size 非显示尺寸）。`libimobiledevice` 是 iOS 设备侧协议，对 CarPlay phone-side 无直接参考价值 | 把触摸报告误判为「iAP2 HID 组件（0x66xx）缺失」（第 13 节），在错误的方向上得出「此路不通」的结论 |
| 27 | **hidSetInputMode 是否必需因车机而异，且必须发在「事件通道」**。orangecar（Jaguar **有线** USB）从不发 hidSetInputMode 车机也推报告；但 cardeck（奥迪 MIB3 **无线**）实测「hidSetInputMode 武装 HID 后车机才开始发触摸」，且「车机命令必须走事件通道 socket 的 POST /command，发在 RTSP 控制连接上车机收不到」。别克君威是无线车机，同奥迪。`y5` 双通道发送语义（反编译实锤）：`g()`=控制通道（门禁 `if(this.t \|\| f==null \|\| g==null) throw`，要求 **t==false**；事件通道 `z()` 置 t=true 后 g() 必抛）、**`l()`=事件通道**（内部置 t=true、挂 r map，由 z() 循环的 c() 完成）。**第 7.1 节「g() 要求 t==true」是写反了** | 把 hidSetInputMode 一直发在控制通道（V1.3.0~V1.3.8 的 `HidChannel()` 硬编码 `CONTROL(forced)`），车机不武装 HID，8 次会话 0 条 `HID_REPORT_RECEIVED`。**2026-11-01 补正（本车实测）：原厂 OcarPlay 走的就是控制通道** —— `i0.W()` 里 `v0 = i0->a0` → `h0.e` → `I0(list, a0, ts)`，`I0` 内直接 `p2.g(...)`；`y5.g()` 的门禁正是为它写的（`method=="POST" && uri=="/command" && contentType=="application/x-apple-binary-plist"` 三条全中）。`HidChannel()` 分流是 V1.3.9 后加的，不是原厂。而官方 0.8.53 走 `g()` **连接后同样不能触摸**（`touch-input-hid.md` §14）。⇒ **通道选择在别克君威上不是变量，不要再拿改通道当修复手段**；改通道只会让 `carrier=EVENT` 这类判据失真。 |
| 28 | **别克君威不需要 hidSetInputMode**（2026-10-31 sysdiagnose 复核，覆盖铁律 27 对别克的推断）：iOS 17.7.1 全程 `hidSetInputMode`=0 命中、`hidDevices`=0 命中，车机照样推 7 条 `hidSendReport`。车机是否推报告，取决于**会话是不是 CarPlay 会话**（即车机有没有先经 `_carplay-ctrl` 下发 `connect`），不取决于手机发不发协商 | 继续在「武装 HID」上做第 7 轮改造，仍然 0 条；且每加一处协商就多一处抛异常的面 |
| 29 | **「等车机发邀请再连 7000」是错误模型，本项目已实机推翻（2026-10-31 V1.7.12）**。iPhone 是 ctrl **服务端**（注册 `_carplay-ctrl` 后等车机 `GET /ctrl-int/1/connect`，间隔 4.7 s）；但本 app 是 ctrl **客户端**——实测 `CARPLAY_CONTROL_CONNECTING` 后直接连车机 `192.168.5.1:7000`，车机**从不**回连 app 的 ctrl（全程无 `V160_CTRL_TCP_ACCEPT`）。所谓「1.28 s 抢跑把会话做成 AirPlay 镜像」是 V1.7.9 的 `d0.c` 假日志 + `MarkInvited` 条件码写反共同造成的假象。V1.7.13 已移除等待门禁 | 在客户端形态上加 8 s 等待门禁，只会给首连增加黑屏，对会话形态零收益 |
| 30 | **改 smali 时别把 Kotlin 的 `require*` 辅助方法当日志**。`io.carplay.app.secured.d0` 里 `b/c/d/f` 分别是 `throw IllegalArgumentException/IOException/IllegalStateException`，`c(String)` = `throw new IOException(msg)`。本项目 V1.6.0 把 `d0.c` 当 `Log.d` 用了 **9 处**（`MarkTcpAccept` / `MarkInvited` / `AwaitInvitation` ×4 / `BluetoothId` ×3），全部在 `hx.smali` | 车机来连 ctrl 时请求处理中断、等邀请立刻抛异常、`CTRL_INVITATION_ACCEPTED` 恒 0 —— 且现象像「车机没来邀请」 |
| 31 | **车机在事件通道上的请求行是 `POST /command RTSP/1.0`**（原生日志原文），不是 `HTTP/1.1`；控制通道的 ctrl 请求则是 `GET /ctrl-int/1/connect`。自写解析器要同时容忍 `RTSP/1.0` / `EVENT/1.0` / `HTTP/1.x` | 等事件通道真开始回传时，认死 `HTTP/1.1` 的解析器会把包丢掉，表现为「收到了但没处理」 |
| 32 | **iOS 17 的 sysdiagnose 用 `UnifiedLog 0.3` 会解析出 0 行**（`0x600d` 块内条目改成 `0x6004` 类型、proc 名与消息内联；旧库遇未知 tag 后对齐错乱，且异常被吞，`Parse()` 仍返回 True）。取证改用 `scripts/sysdiag_strings_probe.py` 直读解压 | 误判「这份 sysdiagnose 没采到 CarPlay 日志」，白白放弃唯一权威参照 |
| 33 | **在本包（`secured`）里跨包调用 `prototype` 包里 Kotlin 内部类的静态方法会 `IllegalAccessError` 崩**（V1.7.11 实机 boot-loop 根因）。`hx.DeviceMac` 调了 `prototype/r.b(Context)`，`r` 是 `.class final`（**包内可见，无 public**）。ART 在加载/Verify 阶段直接抛，进程启动即崩、每 ~5 s 重启一次。修法：把目标类改成 `.class public final`，或在同包内加 public 桥接方法。给第三方内部类加插桩前先用 `head -1` 看访问标志 | 改一版 APK，adb 一装就无限重启，日志里只有 `INVITE_RESET` 然后 `FATAL IllegalAccessError`，完全不像业务逻辑错 |

| 34 | **本 app 是「无线发送端」：手机画面推给车机，车机把触摸经 Wi-Fi RTSP 送回；注入走原生 source（t4/i6/c2），所以「全库无 MotionEvent/injectInputEvent」是 grep 假阴性，不是没有输入管线**（2026-10-31 深夜更正，再次推翻「无输入管线」误判）。smali 里 `hidSendReport`(15 文件)/`HidReport`(11)/`hidSetInputMode`(12)/`SetInputMode`(18)/`touch`(23)/`KeyEvent`(5) 全部存在；`MotionEvent`/`injectInputEvent`/`InputManager`/`uinput` 为 **0 属正常**——因为本 app 把触摸报告桥接给 iPhone，而不是注入 Android 输入。真实链路（i0.smali / y0.smali / hx.smali 实证）：① 车机发 HID 描述符 → app 在 `i0.try_start_8` 用 `y0.j([B)` 解析，成功入 K0/I0/J0 设备表，失败(`IOException`)→`catch_4` 记 `HID_TOUCH_UNSUPPORTED ... reason=DESCRIPTOR` 并丢弃该设备；② `W()`(i0:8150) 需 `EVENT_READY`，且 `z0()`(设备表) 非空时才在事件通道发 `hidSetInputMode`(记 `HID_INPUT_MODE_SENT carrier=EVENT`)；③ 收到 `hidSendReport`(i0:18579 `:cond_32`) **不是只置 REPORTED**：按 `params.uuid` 查 `I0`(touch)/`K0`(consumer)/`J0`(back) 设备表 → `z0.b` 解码 → `q6.n(...)` 换算显示坐标 → `i0$n.r(x,y,down)`(i0:19620) → `WirelessCarPlayService$c.r(IIZ)`(2189) → `t4.T(i6$a,x,y)` → `i6.a` 打包 → `c2.b(r6$a,len,payload)` 写原生 source，日志 **`NATIVE_TOUCH_SENT action=`**——即触摸最终注入到原生 source，不走 MotionEvent。真实设备证据（`OcarPlay-log-20261030-175051.txt`，原版 V1.4.x）：touch 描述符 index=0(62B) 解析 OK、`HID_CAPABILITIES devices=3 touch=1`、`HID_INPUT_MODE_SENT carrier=EVENT`，但 `HID_INPUT_MODE_UNCONFIRMED carrier=CONTROL reason=TIMEOUT_OR_IO` 反复出现，且全程**无** `HID_REPORT_RECEIVED`/`REPORTED`/`TOUCH_FORWARDED`——HID 输入根本没启动。「触摸无反应」两个真实候选根因：(A) `y0.j` 拒绝某条描述符(`reason=DESCRIPTOR`)→ 设备被剔出 `z0()`；若被剔的是 index=0(touch)则 `z0` 变空、`W()` 直接 `SKIP` 发 `hidSetInputMode` → 彻底无触摸；(B) 控制通道 `SetHIDInputMode` 确认超时(`TIMEOUT_OR_IO`)，若 app 把 HID 激活门禁挂在控制确认上，车机永不发 `hidSendReport`——这比(A)更可能是真凶（原版已 `touch=1`、事件已发，却无报告）。**与铁律 25/27/28 的衔接**：那几条来自 iPhone 原生 / orangecar 对照（车机对"被正确识别的 CarPlay 手机"会无条件推报告、不依赖 `hidSetInputMode`）；但本 app 实现的是**自己的** HID 协商栈，断点就在这套栈里（描述符解析 / 确认门禁），所以对**本 app** 而言 HID 代码路径是首要嫌疑，不是"改了无用"。V1.7.15(E12) 已在 `catch_4` 把失败描述符原始字节 `Arrays.toString` dump 进同一行日志，下次上机即可取到；用 `scripts/decode_hid_descriptor.py` 离线解。 |
| 35 | **【2026-11-01 二次修正，V1.7.22】「5001 是单向通道」这个结论作废——它基于一次没有对照的 `nc` 实验。** 补做对照后：对 **7000（控制通道，实测 57,669 条 `status=200`）** 做完全相同的 `(sleep 8) | nc 192.168.5.1 7000` 测试，结果**也是** `elapsed_ms=1129 rc=1` 零字节。`toybox nc` 在这个 Android 环境里约 1.1 秒就自行退出，跟对端行为无关。⇒ **任何用 `nc` 手写探测得出的「通道单向 / 可用」判断都无效，除非同时拿一个已知可用端口做对照。** 事件通道 5001 到底能不能读，只能靠 APK 内的 `CARPLAY_EVENT_BOOTSTRAP status= body=` 判据（见铁律 39）。 **仍然成立的部分**：`y5.v()` / `y5.w()` 是 `declared-synchronized` 且吃满 15s SO_TIMEOUT，**不能在主链路同步调**；但放独立线程是可行的（V1.7.22 E21-c 就是这么做的，且实测 `w()` 内部没有任何 `close()`，超时只抛异常、不会拆通道）。 证伪过程：`CarPlay-Build/docs/v1722-boot-guard.md` §0。 |
| 36 | **【V1.7.19 真凶】`hidInputMode` 恒发 0：MODE 扫描的推进条件写反了。** `i0.smali` 的 `HidRetryTick` 里原意是 MODE 在 0..5 循环前进：`sget v4,MODE; add-int/lit8 v4,v4,1; const/4 v6,5; if-gt v4,v6,:hid_ret_mode; const/4 v4,0; :hid_ret_mode; sput v4,MODE`。`if-gt` 语义是「v4 > v6 则跳转」，于是 +1 后的 1..5 **全部落下来被 `const/4 v4,0` 清零**，只有 6 才保留（且越界），**MODE 恒为 0**，`z0()` 取它作 `params.hidInputMode`。实测证据：`HID_SWEEP mode=0` 从头到尾只出现 0，10 次里没有一个非 0 值；`HID_CMD_REQ len=111 body=...` 里 hidInputMode 恒 0。`hidInputMode=0` 是「未指定/无输入」，车机收到不会认为触摸屏已启用，一条 `hidSendReport` 都不会推。**修法：`if-gt` → `if-le`**（v4<=5 保留、v4=6 才回绕 0）。附带把扫描上限 `TRIES>=6` 放宽到 `>=18`（6 次只够 0..5 跑一轮）。 **排查纪律**：看到 `HID_SWEEP mode=` 连续同一值，先怀疑条件指令写反，而不是通道/载荷。 |
| 37 | **HID 发送链的失败是「静默」的，判据必须打在 `l()`/`g()` 返回之后。** `W()` 只是把发送任务丢进线程池（`Executor.execute(h0)`），`h0.run()` → `i0.y()` → `I0()`，中途抛任何异常都被线程池吞掉，**logcat 里连堆栈都没有**（用 `'*:E'` 抓也抓不到）。所以 `HID_CMD_REQ`（`l()` 之前）打印了、`HID_CHAN=EVENT`（在 `HidChannel()` 内）也打印了，**都不代表请求发出去了**；真正的判据是 `HID_INPUT_MODE_SENT ... carrier=EVENT|CONTROL`，它在这两者之后、`l()` 成功返回之后才打印。 V1.7.15 有 39 行 SENT，V1.7.18 是 **0 行**——只看前两条日志会得出「已发送、只是没回包」的完全错误结论（本轮踩过）。 **交叉验证**：`INBOUND_REQ=0` 也不能当作「对端没发」的证据——V1.7.17 里控制通道明明有 57,669 条 200 响应，`INBOUND_REQ` 照样是 0，说明 E14/E15 的 `O0`/`S0` 插桩点根本没覆盖控制通道的真实入站。插桩没打出来 ≠ 事件没发生。 |
| 38 | **同一个 `y5` 实例上 `l()` 与 `g()` 互斥，`l()` 会污染实例。** `y5.l()` 一进来就无条件 `iput-boolean 1, y5->t:Z`，而 `y5.g()` 的门禁是 `if (t != 0) throw IOException("Queued command requires encrypted main control")`。另外 `l()` 要求 `r`（pending 表）`.size() < 0x10`，否则抛 `"Too many pending RTSP control commands"`，且这条 IOException 会走 catch 分支 `close()` 掉整条通道。事件通道永远收不到回包 ⇒ `r` 里的条目永不清理 ⇒ 迟早撞上限。**因此：不要为了「试试看」在事件通道上调 `l()`（V1.7.18 E16 这么干了一次，直接把 21 次 HID 发送全废掉）；`g()` 只能用在从没被 `l()` 碰过的实例上（控制通道 p1 满足）。** 两条通道实测都 `TIMEOUT_OR_IO`，说明通道不是变量，**别再把改通道当修复手段**（V1.5.4 已试过一次，全 TIMEOUT）。 |

| 39 | **【V1.7.22 真凶】`hx.Boot()` 的 BOOTED 守卫写反了，自举从未执行过一次。** `hx.smali:218` 是 `sget-boolean v0, hx->BOOTED` 后接 `if-nez v0, :v150b_skip`——语义是「BOOTED==0（首次）就跳 SKIP」，正好反了。注释里明确写着「V1.5.1 修正」，代码却没改。⇒ V1.5.3 以「阻塞 15s」为由删掉的是一段**永远进不去的死调用**；注释里「真机 88B/0 body → 200/1909B」是更早（无守卫）版本的数据，不能拿来当当前版本的实测依据。**判断行为必须读指令，不能读注释。** 修法：`if-nez v0, :v150b_skip` → `if-eqz v0, :v150b_go`，并把 `sput-boolean BOOTED` 移到 `:v150b_go` 开头。**实测结局（V1.7.22）**：守卫修好后自举确实执行了，但每轮都在 `y5.w()` 的阻塞 `read()` 上吃满 15.02s SO_TIMEOUT 抛 `SocketTimeoutException` —— 写成功、车机不回。**这条路已判死，不要再修守卫、不要再补 priming。** |
| 40 | **「会话被停」必须先在时间线上标出来，否则会把停止后的连锁失败当成根因。** V1.7.21 日志里 461 次 `HID_SEND_STATE done=true exc=true` **全部**发生在 `00:38:00.652 I/CarPlusWireless: Stopped by user` **之后**——那是通道关闭后发送必然失败的结果，不是故障本身。真实会话窗口只有 7.5 秒（00:37:53 ~ 00:38:00.652），里面只有 40 次 `done=false exc=false`。⇒ 分析任何 `exc=true` / `TIMEOUT` / `TRANSPORT_END` 之前，先 `grep -a "Stopped by user\|SESSION_END\|Final sender counters" 日志` 定位会话真实终点，再判断哪些日志属于有效窗口。 |
| 41 | **自己加的插桩本身就是 bug 源，删插桩要当成一次修复来看待。** V1.7.16 E13 那条 `payloadSize` 插桩里，`p1` 已被 `move-result-object p1` 覆盖成 `y5`，代码却仍按 `List` 调 `size()`，必抛 `IncompatibleClassChangeError`，被 catchall 接住后直接 rethrow，**后面所有日志全被跳过**。结果是 `HID_INPUT_MODE_SENT` 从 V1.7.15 的 39 行静默到 0 行，横跨 6 个版本，并连带误判了「E16 制造新故障」「已发送只是没回包」两件事。⇒ ① 插桩必须打在「寄存器语义已确定」的位置（`move-result` 之后不要再按旧类型用同一个寄存器）；② 一条日志突然变 0，先怀疑插桩本身。 |
| 42 | **`HID_TOUCH_UNSUPPORTED index=1` 是误报，不要当成描述符问题查。** 那 29 字节解码后是 Telephony / Hook Switch：`05 0B 09 01 A1 01 05 0B 09 20 09 26 15 00 25 01 75 01 95 02 81 02 75 06 95 01 81 01 C0`，本来就不是触摸描述符；而 `index=0` 的触摸描述符 62 字节解析成功、`display_bound=true`、`HID_CAPABILITIES devices=3 touch=1`。⇒ 描述符链路正常，别再往这里查。 |
| 43 | **【V1.7.22 事故】在任何 `declared-synchronized` 的发送 API 上做「探活」必然伤会话。** `y5.v()` / `y5.w()` / `y5.g()` / `y5.n()` / `y5.D()` 全是 `declared-synchronized`，探活线程一旦进入 `v()` 就会**持锁整整一个 SO_TIMEOUT（`y5.e`=15000ms）**。V1.7.22 把 `hx.Boot()` 放到独立线程跑，结果 5 个会话里 4 个 `videoUnits=0`：`N0()` 卡在 `W()` 之后不再往下走，`V153_MEDIA_PATH_CONTINUE` / `EVTCP connect port=6030` / `CARPLAY_MEDIA_READY` 全部消失，连 1 秒一次的 `HID_P0 tick` 都停摆 12 秒。⇒ **任何探活都不能碰主通道实例的 monitor**；要么新建独立 `y5`，要么别做。做完行为类改动必须单独一轮验证，不能和插桩混发。 |
| 44 | **`read_calls=1 raw_bytes=0` 不等于「读循环坏了」。** `y5.u()` 里 `y`（read_calls）每次调用**先 +1**，`z`（raw_bytes）只有 `InputStream.read() >= 0` 才 +1。所以 `1 / 0` 的含义是「一次阻塞 read 从这里等到会话结束，一个字节都没来」。V1.7.21 那次 7.5 秒会话正是如此（read 阻塞 7.5s 后随会话停止返回 EOF）——**读线程是健康的，是车机不说话**。要判断读是否正常，看长会话里 `read_calls` 有没有涨，不要只看某一次快照。 |
| 45 | **判「车机不说话」之前，必须先排除「没触发它说话」。** `hidSendReport` 是「车机有触摸才发」，没有画面 → 用户没法点 → 车机不发 → `raw_bytes=0`，与「通道单向」在日志上完全同形。这个混淆连续误判了三轮（nc 探测、E16 priming、hx.Boot）。⇒ 每次要下「对端不说话」的结论，先确认三件事同时成立：① 会话时长 > 20s；② 车机屏确实有画面（`CARPLAY_MEDIA_READY` + `VIDEO_PROGRESS units` 在涨）；③ 期间有人真的在车机屏上点过。缺一条，结论作废。 |
| 46 | **【2026-11-01，判「通道单向」的最硬证据是「同窗口双端口对照」，不是裸探测。】** 取一个 ≥60s 的稳定会话，在**同一个时间窗口**里同时数两个通道的对端写入量：V1.7.21 会话 3（66.3s）里控制通道 7000 回了 **771 条 `CARPLAY_RESPONSE` 全 200**，事件通道 5001 是 **`raw_bytes=0`**，而 `videoUnits` 1→1228（画面活着）。⇒ 同一条 WiFi、同一时刻、两个端口待遇完全相反。这一条同时排除「网络不通 / 事件通道加密未实现 / 读循环坏了」三种解释（加密只影响读不读得懂，不影响字节有无；读循环健康看 `read_calls` 会不会在长会话里涨）。**比 `nc` 探测和「发了没回」强得多，以后一律用这个。** |
| 47 | **【V1.6.0 E2 未生效】`_carplay-ctrl` 的实例名调错了方法。** `h.smali` 广播时调的是 `hx.ServiceName()`（返回**裸蓝牙名**，取不到就兜底 `"OcarPlay"`），而 E2 专门写好、会补 `iPhone_` 前缀的 `hx.CtrlServiceName()` **全库 0 调用点＝死代码**。iPhone 原生广播的是 `iPhone_SCH._carplay-ctrl._tcp`，前缀对不上就少了车机最直观的一条匹配依据。而且 `CTRL_SERVICE_REGISTERED` 里是 `values_logged=false`，**服务名从来没有落过值**，所以「广播出去了什么」这件事至今没有被验证过。⇒ ① 改广播前先加 `serviceName=` / `port=` 两行日志；② 别信「已经改过了」，去 grep 调用点。**写了新方法不等于用上了新方法**（与铁律 41「插桩本身是 bug 源」同源）。⚠️ **2026-11-01 更正：不要加 `iPhone_` 前缀。** 车机认的是**手机蓝牙名称本身**，`iPhone_<设备名>` 只是 iOS 侧 audiomxd 自己的命名习惯，不是匹配条件。所以 `CtrlServiceName()` 这条路整体放弃，`ServiceName()` 保持原样。真正要验的只有一件事：**广播出去的是不是手机蓝牙名**。`ServiceName()` 优先级 = 手动名(设置页 `SaveName` 写 `bluetooth_name`) > `BluetoothAdapter.getName()` > `"OcarPlay"`，两级都失败就兜底成 `OcarPlay`（此时名字是错的）。⇒ 日志要**同时**打 `svc=` 和 `manual=`：`manual` 非空=用设置页填的名；`manual` 空+`svc`=手机名=预期；`manual` 空+`svc=OcarPlay`=两级都挂了，去设置页手动命名。V1.7.23 就是这个方案，**命名逻辑一行未改**。**改配置项必须打印「实际生效值」，不能只打印「我调了哪个方法」。** |
| 48 | **【V1.7.24 新坑，会直接杀进程】smali 里 `goto` 绝不能从 catch handler 跳回 try 块内部 —— 而 apktool 汇编、打包、再反编译全部正常，构建日志里一个字都看不出来。** 典型写法：两个 try 块（sleep 一个、业务一个），sleep 的 catch handler 用 `goto` 跳回业务 try 的入口做「失败也继续」。ART verifier 会在汇合点做寄存器类型合并：正常路径那一路 `v0` 是 `long-lo`（`const-wide/16` 的低位），handler 那一路 `v0` 是 `Throwable`（`move-exception` 之后），`primitive × reference` 合并成 `conflict`，方法判为不可用，**首次调用即抛，且这段代码若在 `Thread.run()` 里，异常走进程级 `UncaughtExceptionHandler` 直接杀进程**。⇒ ① 任何 try/catch 的 handler 只放方法末尾，绝不 `goto` 回 try 内部；② 需要「失败也继续」就把会抛的那一步抽成独立方法（内部自吞异常），让主方法只剩一个直线 try；③ 新增/改写 smali 方法后，除了 apktool 往返，还要专门数一遍 `h$X.run` 这类线程入口里的 `goto` 数量（应为 0），并确认 `.locals` 够用。**「能打包」不等于「能验证」，这两件事在 ART 上差得很远。** |
| 49 | **看单点计数没有意义，先按秒画曲线。** 某个量「很大」不一定是问题（353 条 `/feedback` 看着吓人，其实无害）；某个量「一直涨」一定是问题。判定方法：把日志按秒聚合，把候选指标逐秒列出来。V1.7.24 就是这么抓到真凶的——`FEEDBACK/秒` 从第 3 秒的 2 一路 **每秒精确 +1** 涨到第 26 秒的 26，累计量按 `t²/2` 走，实测 `cseq` 在 16 s 时 130（=128）、长会话末尾 20195，两条都严丝合缝。这种「无平台期的单调递增」只可能是**周期任务被重复注册而不是重排**。⇒ 反过来说，如果一条曲线是平的，那个方向就别再查了，省下来的时间够查三条斜线。 |
| 50 | **该打印却没打印的日志，本身就是最强的证据。** `i0.smali:2811` 的 `HID_CMD_REQ`（发送前）在 V1.7.24 里**一次都没出现**，而位置更靠后的 `HID_CHAN`（2835）出现了 1327 次。位置在前的没打、在后的打了 ⇒ **实际执行到的根本不是你以为的那段代码**，而是从某个 label 直接跳进了后半段。后果：那一版真正发出去的 body 完全没被观测过，任何关于 payload 结构的结论都必须降级为「仅对 ≤V1.7.21 成立」。⇒ ① 加日志必须加在**唯一必经点**（本例应在 `HidChannel()` 返回之后、调 `g()/l()` 之前）；② 上机后先 grep 一遍「新加的日志是否真的出现」，没出现就先查路径，别急着解读后面的日志。 | ⚠️ **2026-11-01 复核：这条的推断部分被推翻，但排查动作仍然正确。** `HidChannel()` 在全工程**只有一个调用点**，就在 `HID_CMD_REQ` 那行的下一行（同一基本块，中间没有分支）。它若执行 1327 次，前面那条日志必然也被执行 1327 次 ⇒「实际路径不同」站不住。真实原因是**那条日志整行被 logcat 丢了**：body 是二进制 plist，`new String(byte[])` 拼出大量 NUL 与控制字符。⇒ 结论修正为：**「该打印却没打印」先分两种情况 —— ① 路径真的不同；② 日志行本身被丢了（含二进制/超长/换行）。判别方法：在同一位置**再加一条纯文本日志**（只打长度或计数），它出现了就是②，仍然不出现才是①。本例 V1.7.25 加了 `CarPlayHID HID_SEND len=N`，从此发送次数才第一次可证。**
| 51 | **同一个被裁剪的回调对象会同时丢多个字段，别把一个字段的异常当成独立 bug。** `CTRL_SERVICE_LIVE port=0` 曾经被当作「真注册成 0 端口」，还专门写了一版 resolve 去验；结果 resolve 直接抛 `Service type cannot be empty`——**同一个对象连 `serviceType` 都是空的**。两个字段一起丢 ⇒ 就是「ColorOS 的回调对象只填了 serviceName」这一件事，`port=0` 从来不是 bug。⇒ 遇到「某字段值异常」，先在同一个对象上再读一个**本该必然非空**的字段做对照；两个都空 = 采集假象，一个空一个正常 = 才是真异常。这条同时省掉一次上机实验。 |
| 52 | **日志行里绝不能塞二进制内容，那行会静默消失，而你会把它当成「代码没执行」。** `i0` 的 `HID_CMD_REQ` 用 `new String(byte[])` 把整个 bplist 拼进日志，V1.7.24 里这条一次都没出现，直接导致上一轮把「发送路径不对」当成结论（见铁律 50 的复核）。Android 的 logger 在 C 层按 C 字符串处理，遇 NUL 即止；超长还会被整条丢弃。⇒ ① 任何用于**计数**的日志必须是纯 ASCII、定长、无换行、无 NUL；② 要看 body 结构就打 hex 前 N 字节，别打原始字节串；③ 一条插桩日志若「时有时无」，先怀疑内容而不是路径。**计数日志的要求是「一条不多、一条不少」，不是「信息量大」。** |
| 53 | **音频卡顿先看「发出去多少」，别先怀疑网络。** `AUDIO_FLOW` 一次给全：`sent_frames/converted_frames` = 实际转发比例（<60% 就是卡顿）、`queued_frames` 长期贴顶 = 发送跟不上源、`udp_write_max_us` 只有几百~几千微秒 = **网络不是瓶颈**、`scheduler_late_max_us` 几百毫秒 = 发送任务被饿死。2026-11-01 实测（会话 2.7h）：音频只发 46~53%、丢帧 2623 万、调度迟到 434~560ms，而 socket 写最慢 4ms。同期 `VIDEO_FLOW SOURCE_TO_WRITE_p95` 和 `assembly_p95` 也都是 460ms 量级 ⇒ **是全局周期性停顿，不是音频独有**。⇒ 排查顺序固定为：转发比例 → 调度迟到 → 队列深度 → 最后才看 socket。另注：`dropped_frames_total` 是累计值，要看**每秒增量**的趋势；本案例它从 23632 单调涨到 25770 帧/秒，这才能证明「越用越差」。 |
| 54 | **本项目的日志有两条通道，插桩写哪一条决定你下一轮能不能看见它。** `i0.j(String)` 就是 `Log.d("CarPlayHID", msg)` —— **只进 logcat**；只有 `i0$n.a(String)` 回调才写进 App 日志文件（用户发给你的那个 .txt）。2026-11-01 实测：`HID_P0 tick` / `HID_SWEEP mode=` / `HID_CMD_REQ` 三个只走 `i0.j` 的字符串在 App 日志里**恒为 0**，而它们同位置的 `i0$n.a` 孪生行全在。上一轮因此误判「P0 没执行」，白查一整轮。⇒ ① 任何用于判读的日志**必须双写**（`i0$n.a` + `i0.j`）；② 看到「该有的日志没有」，先确认这条日志走的是哪条通道，再怀疑执行路径；③ 给用户取日志的步骤要写清是 logcat 还是 App 内导出。 |
| 55 | **把实例引用放进静态槽位做幂等，等于又变回全局状态 —— 多实例交替调用时槽位来回翻，每次翻动都重新起一条链。** `W()` 原来是 `if (hx.LASTOBJ == this) skip; else { LASTOBJ = this; 起一条 1Hz 链; }`。两个 `i0` 实例交替调 `W()` 就变成 A→B→A→B，每次都是「不等于」，每次都起新链 ⇒ 链数随调用次数线性增长，`/feedback` 速率就等于链数（实测 2.7h 会话稳态 58~68 条/秒、间隔中位 5.2ms 成簇出现）。修法：幂等标记必须是**实例字段**（`i0.zzChainOn:Z`），不是静态槽位；再加一层与链数无关的**时间窗限流**兜底。判据：加一条「每秒汇总 tick 次数」的日志，`n≈1` 才算治好。 |
| 56 | **「降噪」改动必须先验证它还在打日志，否则等于永久静音。** V1.7.26 把逐条的 `CARPLAY_RESPONSE` 改成「每 300 条汇总一次」，条件写成 `if-lt v2, 0x12c, :resp_sum`。`if-lt` 是「小于才跳」⇒ RESPCNT<300 时去打印、==300 时 `return` 且**永不归零**：前 299 条各打一行（内容还写着 n=300），第 300 条起一条都不再打。⇒ ① 任何限流/聚合的判断写完，先算一遍「第 1 条、第 N 条、第 N+1 条」分别走哪支；② 聚合日志必须自带计数，`n=` 要和实际窗口一致；③ 聚合/限流上线后第一份日志先数条数，对不上就是条件方向错了。**降噪不能把可观测性一起降掉。** |
| 57 | **找对照实现要先对齐「角色」，再谈协议。本项目唯一可直接对照的开源实现是 `fw867/hypercar`。** 它和本 App 是**同一种拓扑**：Android 手机当 CarPlay 手机侧（自己编码画面推给车机），车机是无线 AirPlay 接收端，触摸由车机经事件通道回推（README 原文「实时触摸：车机 HID → Rust」，实车基准 BMW MGU-55/iD8.5）。而 `orangecar` 是有线 USB、`catplay` 以接收端为主、`node-CarPlay` 偏镜像，角色都对不上。它给出的三条最关键事实：① **SETUP 的 `features` 是交集语义** —— `enabledFeatures = 车机支持 ∩ 手机提议`，源码注释原话「只提议 videoPlayback 会让视频管线整个不启用——奥迪 400 教训」；② **全程不发 `hidDevices` / `hidSetInputMode` / `hidStartSession`**，收到 `hidSetInputMode` 也是 `=> {}` 空实现忽略；③ **不实现 `_carplay-ctrl` 服务端**，和本 App 一样直接连车机 7000，照样拿到 HID。⇒ 这三条同时说明：HID「武装」不是变量（与本项目铁律 28 一致，现在有第二个独立实现佐证），变量只在**会话层的能力提议**。2026-11-01 复核：本 App 的会话 SETUP 只 put 了 9 个键，**一个 `features` 都没有** ⇒ propose=∅ ⇒ enabledFeatures=∅ ⇒ 车机把会话当普通 AirPlay 镜像端，事件通道一条 `POST /command` 都不发。V1.7.28 补上 14 项 features。 |
| 58 | **「建链即置位」的状态标志不能当握手成功的证据。** `i0.N0()` 里 `hx.EVENT_READY` 是**无条件**置 true 的（`const/4 v2,0x1; sput-boolean ...; log "V153_EVENT_READY=true"`），它只表示「事件 socket 建好了」，与会话是否被车机接受无关。同类还有 `CARPLAY_EVENT_CONNECTING`、`CTRL_SERVICE_REGISTERED`——都是「我做了动作」，不是「对端认了」。⇒ 判据一律要落在**对端行为**上：`INBOUND_REQ chan=`、`HID_REPORT_RECEIVED`、`status=` 回包、`read_calls` 是否增长。 |
| 59 | **一次性门禁会把「本该有时间序列的指标」压成单次快照，而你会拿这一次去代表整场会话。** `i0.O1(y5, point)` 里 `P0.compareAndSet(false,true)` 通过才打日志，于是 4 个调用点里只有第一个（会话头部 `HID_WAIT`）落盘，`TRANSPORT_END` 那条永远不会出现。⇒ `CARPLAY_EVENT_INPUT ... raw_bytes=0 read_calls=1` 是**早期快照**，不能单独断言「整场零字节」。补救办法不是拆门禁（会变成每秒几十条），而是**换一个不受门禁影响的埋点交叉验证**：`i0.O0`/`i0.S0` 里每个入站请求都无条件打 `INBOUND_REQ chan=EVENT\|CONTROL uri=`，这条恒 0 才是「对端从未发过」的硬证据。**任何「XX=0」的结论，先确认那条日志有没有被门禁/限流压过。** |
| 60 | **判「哪个是读密钥、哪个是写密钥」只能看使用点，不能看变量名、注释或参数顺序。** `y5` 里 `i(p1,p2)` 把 p1 存 `f`、p2 存 `g`；真正区分靠使用点：`t()`（`s()` 解析时取数据）用 `f` ⇒ **f=读/解密**，`D(String,Map,byte[])`（`z()` 回包时写）用 `g` ⇒ **g=写/加密**。本 App 传 `i(hkdf(Events-Write-Encryption-Key), hkdf(Events-Read-Encryption-Key))` ⇒ 读密钥取自 **Events-Write**，与 hypercar `derive_event_keys()` 的 swap 方向（read←"Events-Write-Encryption-Key"）**完全一致，是对的**。hypercar 注释记载过同类事故：「曾错用 Control-Salt 派生 → 事件通道解密必失败 → 触摸/命令全丢」。⇒ 核这类问题的顺序：先找**加密/解密两个方向的唯一使用点**，再看装填顺序，最后才看名字。 |
| 61 | **【V1.7.29 真凶，原厂就有的潜伏 bug】「原厂死代码」里的接口方法名可能是错的，你一旦修好它前面的入口异常，它就是下一个崩溃点。** `prototype/h$z` 声明 `implements NsdManager$ResolveListener`，实际只写了 `onResolveFailed` + **`onResolved`**，而接口要的是 `onServiceResolved`。原厂 `resolveService()` 的入参 `serviceType` 是空串（ColorOS 裁剪），入口就抛 `IllegalArgumentException("Service type cannot be empty")`，回调永不发生 ⇒ 这段代码是死代码，方法名写错也没人察觉。V1.7.25 P0 自建 `NsdServiceInfo` + `setServiceType(h.r)` 修好入口后，resolve 第一次真的成功，框架回调 `onServiceResolved`，`h$z` 没有这个方法 → `AbstractMethodError`（ConnectivityThread，无人捕获）→ 进程被杀 = 用户看到的「点连接就闪退」。⇒ ① **让一条从未成功过的路径第一次成功，必须复查「它成功之后会走哪段代码，那段被验证过吗」**；② 修 `AbstractMethodError` 时要把**该接口的所有实现类**列出来逐个对方法名（本项目 4 个 NsdManager 实现类里只有 `h$z` 错、`j$b` 是对的，只看报错那个会漏掉「另一个是对的」这个判据）；③ 这类 bug 原厂就存在，不是你引入的，别写进「本轮回归」——但要在文档里标清「哪个版本激活、哪个版本修」。 | 继续在「为什么这次才崩」上打转，或者把它当成新引入的回归去回滚一个正确的修复 |
| 62 | **用户报「闪退」，第一条命令是 `adb logcat -d -b crash`，不是读代码。** `AbstractMethodError: abstract method "void ...X.method(...)" on receiver java.lang.Class<Y>` 的读法是「**Y 声明实现了某接口但缺这个方法**」；堆栈落在 framework 回调线程（`ConnectivityThread` / `NsdManager$ServiceHandler` / `HandlerThread`）说明**是系统在调你**，不是你调错了 API。配合 `dumpsys package <pkg> \| grep -E "versionName\|lastUpdateTime"` 对一下崩溃时间与装包时间：相差几十秒就是**这次改动**踩到的，不是偶发。本次从报障到定位共两条命令：crash buffer 取堆栈 → 按类名 `grep -n "^\.method"` 列方法。**别一上来就翻 15 个版本的历史 patch。** | 花几小时在协议/时序上猜，而真凶就在一行方法名里；或把两次同类崩溃（23:18:50 / 23:18:56）当成两个不同问题 |

## 工作流

### 步骤 0：车机是否在反复敲门（先看这个，比协议重要）

```bash
adb logcat -d | grep -c "on_incoming_connection"
adb logcat -d | grep -aoE "psm: [A-Z_0-9()x]+" | sort | uniq -c
```

- 敲门次数在几十秒内涨到上百，`psm` 只有 `BT_PSM_SDP` → **手机没在广播 iAP2 服务记录**，跳到「车机反复连接」一节
- `psm` 里出现 `BT_PSM_RFCOMM` 且链路稳定 → 蓝牙段正常，继续步骤 1

一键量化：`bash scripts/bt_flap_diagnose.sh`

### 步骤 1：确认车机是否在待机态（最常见的前置失败）

```bash
adb shell "cmd wifi list-scan-results" | grep -i buick
```

扫不到车机热点 = 车机没进入「无线 CarPlay / 手机互联」界面。此时车机仍会接受 RFCOMM 连接（SDP 记录还在），但 iAP2 状态机没启动，表现为 **0 字节 + 1 秒后 Broken pipe**。这一步不通过，后面所有协议调试都没有意义。

### 步骤 2：确认 RFCOMM 连到了正确通道

SCN 每次连接都可能变（实测出现过 4、5、6、7）。唯一可靠的判据是**连上后 2~3 秒内能否收到 `FF 55` 开头的帧**。收到即锁定该 SCN，并把预读数据交给上层（否则会丢掉第一个 MARKER）。

OPPO/ColorOS 会主动清 SDP 缓存（`oplus_clear_sdp_cache`），缓存空时按 UUID 连接必须现场查 SDP，而现场查容易被 `CONTROLLER_BUSY(0x3a)` 打断 → `SDP_CFG_FAILED`。对策：`fetchUuidsWithSdp()` 预热 + 反射 `BluetoothDevice.getServiceChannel()` 读通道号直连。

### 步骤 3：跑通 iAP2 链路层

严格照抄官方时序，详见 `references/iap2-link-layer.md`。要点：

- 我方 seq 从 98 起，每发一个 SYN 递增
- 车机 SYN 的 seq 与 ack 独立计算，不要拿对端值覆盖自己的 seq
- ACK 不消耗序号
- 收到车机的 LSP 后校验：`window=5 packet=2048 rto=1500 ackTo=73 retries=30 maxAck=3`，会话 `sid=1/type=0`、`sid=3/type=2`

### 步骤 4：识别 + 认证 + WiFi 凭据

`1D00 → 1D01(解析车机可接收消息表) → 1D02 → AA00 → AA01 → AA02 → AA03 → AA05`。

**车机是否下发 0x5703 取决于它有没有被请求**：若 1D01 声明 `SEND=0`（不主动推送），则 `0x5703` 只能是对 `0x5702` 的应答。必须主动发 `0x5702`，且要重试（车机侧需用户在屏上确认 CarPlay）。`0x5703` 的 TLV 是 **`[len][id][value]`**，`1=SSID 2=Passphrase 3=Security 4=Channel`。

若车机始终沉默，直接跳过蓝牙凭据环节，用已保存的热点密码走阶段 2。

### 步骤 5：WiFi 关联

用 `WifiNetworkSuggestion`（只需 `CHANGE_WIFI_STATE`），不要用 `WifiNetworkSpecifier`（需要 `CHANGE_NETWORK_STATE`/`WRITE_SETTINGS`，普通 App 必被拒）。提交后必须**主动轮询等待关联**，三判据见铁律 6 与 `references/android-pitfalls.md`。

判断「真的连上了」只看 `ip -4 addr show wlan0` 里有没有同网段地址。**`cmd wifi status` 会说谎**（会话正常时也可能报 `Wifi is disabled`）。

### 步骤 6：mDNS 发现 + RTSP

先把自己广播出去（`_carplay-ctrl._tcp`），再发现车机。车机 `_airplay._tcp` 的服务名通常是 **`CarPlay`**，地址 `车机IP:7000`。拿到就直接发 RTSP，详见 `references/rtsp-flow.md`。

广播要点（实测，别照抄旧文档）：

- **端口是动态的，不要写死 45697。** 原生首会话注册在 `51841`、重连会话 `51846`；
  车机必须从 mDNS SRV 读端口，任一端写死固定值都会在第二会话失联。
- 服务实例名格式是 `iPhone_<设备名>`（本机 `iPhone_SCH`），不是 `CarPlay`。
- 广播有前置条件：本端 carkitd 先确认要广播，AirPlay 侧才注册；会话结束立刻注销。
- **本 app 是 ctrl 客户端，不是服务端**：V1.7.12 实机确认，app 注册完 `_carplay-ctrl`（Bonjour）
  后直接连车机 `192.168.5.1:7000`（`CARPLAY_CONTROL_CONNECTING`），车机**不会**回连 app 的 ctrl。
  所以「注册后等车发 `GET /ctrl-int/1/connect` 再连 7000」是 iPhone 服务端模型，对这里不适用；
  **不要加等待门禁**（V1.7.13 已移除 `AwaitInvitation()` 调用，它的 8 s 超时只会给首连加黑屏）。
- **判据看车机侧的成功链**（实机验证 V1.7.12/13，连接即成功时会出现）：
  ```bash
  adb logcat -d | grep -E "CarPlusWireless\(" | grep -E "CTRL_SERVICE_REGISTERED|CARPLAY_CONTROL_CONNECTING|V153_EVENT_READY|HID_CAPABILITIES|NATIVE_VIDEO_FIRST_BYTES"
  # CTRL_SERVICE_REGISTERED network_scoped=true      ← ctrl 广播注册成功
  # CARPLAY_CONTROL_CONNECTING                        ← 已回连车机 ctrl(7000)
  # V153_EVENT_READY=true                             ← 事件通道(5001)连上
  # HID_CAPABILITIES devices=3 touch=1                ← 触摸能力协商成功（无需 hidSetInputMode）
  # NATIVE_VIDEO_FIRST_BYTES bytes=...                ← 视频首帧已到，投屏活了
  ```
  若 `CTRL_SERVICE_REGISTERED` 之后没有 `CARPLAY_CONTROL_CONNECTING` → 查 TXT/Bonjour 是否被拦；
  若 `V153_EVENT_READY` 之后没有 `HID_CAPABILITIES touch=1` → 查事件通道与 HID 描述符。
- TXT 键集：`id` / `deviceid` / `srcvers` / `protovers` / `flags` / `model` 来自固件键池推断，
  原生日志**没有**打印过 TXT 内容，别把推断当事实写死判断逻辑（判定「车机靠哪个键匹配」需要车机侧抓包）。

### 步骤 7：事件通道 + 输入（触摸/方向盘）

建流后车机给 `eventPort`（实测 5001）。连上后**角色反转**：车机当客户端在 **同一条 socket** 上发请求，手机回 200。

```
POST /command RTSP/1.0                      ← 车机的请求行，注意是 RTSP/1.0
{type:"hidSendReport", uuid, hidReport}     ← Body 96（原生实测）
```

**取证顺序（2026-10-31 重排：先验会话形态，再验报文；顺序反了会白干）**

```bash
# 0) 最关键一步：车机有没有来连 ctrl、有没有下发 connect
adb logcat -d | grep -E "CTRL_TCP_ACCEPT|CTRL_INVITATION_ACCEPTED|V170_TXT_FILLED"
#    没有 → 停在这里。会话不是 CarPlay 会话，改 HID 一万次也不会有报告

# 1) 事件通道有没有字节进来（tx:rx 的第 5 列）
adb shell "cat /proc/net/tcp /proc/net/tcp6" | grep -i "0105A8C0" | awk '{print "st="$4, $2"->"$3, "tx:rx="$5}'

# 2) 车机是否推过 hidSendReport（应用侧打点）
adb logcat -d -s OcarPlay:V | grep -E "HID_REPORT_RECEIVED|TOUCH_FORWARDED"
```

`tx:rx=0:0` 持续 → 车机没推。裸探测同端口确认：

```bash
adb shell "cd /data/local/tmp; timeout 12 nc -w 8 192.168.5.1 5001 | wc -c"
```

**不要再纠结 `hidInputMode` 的取值，也不要再补 `hidSetInputMode` 的发送通道**：原生全程不发（铁律 28）。
`eventPort` 本身也不用改 —— 它来自 SETUP 响应，方向是对的。

## 车机反复连接（蓝牙 ACL 打环）

症状：车机每 1~2 秒发起一次 ACL，100~150 ms 后自己挂断（`reason 19 / 0x13 REMOTE_USER_TERMINATED`），无限循环。

```
22:31:50.913 on_incoming_connection addr:…:ad:5b
22:31:50.929 OnConnectSuccess  initiator:remote
22:31:50.985 l2c_csm_open: Calling Disconnect_Ind_Cb(), CID: 0x0061
22:31:51.074 OnClassicLinkDisconnected reason:REMOTE_USER_TERMINATED_CONNECTION(0x13)
22:31:51.074 bta_dm_pm_stop: bta_dm_conn_srvcs.count=0     ← 一个 profile 都没起来
```

**判据**：数 L2CAP PSM。

| 分布 | 结论 |
|---|---|
| 只有 `BT_PSM_SDP`（实测 823 次 / 3.5 分钟），`BT_PSM_RFCOMM` 为 0 | 车机 SDP 查不到 iAP2 服务，连 RFCOMM 都不试就挂断 |
| 有 `BT_PSM_RFCOMM` 但零字节 | 连错 SCN（铁律 5） |

**根因（实测）**：手机只把 iAP2 的 SDP 记录注册了 **90 秒**（监听窗口 `V=90000`，分布在 accept 截止 / `postDelayed` / 截止时间戳三处）。窗口一过记录消失 → 车机查不到 → 挂断 → 1.3 秒重试。

**修法**：三处窗口值一起改成 `0x7fffffff`（约 24.8 天），让监听随进程常驻。

**验证**：强制全链路复位后数敲门次数。

```bash
adb shell "cmd bluetooth_manager disable"; sleep 10
adb shell "cmd bluetooth_manager enable";  sleep 12
# 修复后：敲门 1~2 次即建链，0 次断开；RFCOMM 约每 30s 一次 iAP2 心跳
```

> `svc bluetooth disable` 在 ColorOS 上常被系统秒拉回，必须用 `cmd bluetooth_manager`。

**先排除两个常见误判**：`com.oplus.ocar`（OPPO 车机互联）force-stop 后敲门照旧；配对记录完好、`security_required:0x0` 说明根本没走到加密——都不是配对问题。

**遗留**：进程被杀 = 监听消失 = 车机回到打环。要根治需常驻前台服务。

## 排障决策树

```
点某个按钮/进某个页面就闪退？
├─ 第一动作：adb logcat -d -b crash -v time | grep -A20 <包名>   ← 别先读代码（铁律 62）
├─ AbstractMethodError ... on receiver Class<X>
│  └─→ X 声明实现了某接口但缺方法 → 把该接口所有实现类列出来逐个对方法名（铁律 61）
├─ VerifyError: invalid argument count (N) exceeds outsSize (M)
│  └─→ 插桩的 invoke 参数数超过原方法 outs_size（铁律 16/17）
├─ VerifyError: register vN has type X but expected Y
│  └─→ 借了别人在用的寄存器，或 handler 里 goto 回 try 内部（铁律 22/23/48）
├─ IllegalAccessError
│  └─→ 跨包调了 `.class`（包内可见）的静态方法（铁律 33）
└─ 装包后无限重启、日志只有 INVITE_RESET
   └─→ 加载期就挂，`am start` 后立刻抓 crash buffer，看不出就走 `dumpsys dropbox`

车机反复连/反复敲门？
├─ 每 1~2s 一次，150ms 就断 ──────→ 数 PSM：只有 SDP → iAP2 服务记录没广播（见「车机反复连接」）
└─ 有 RFCOMM 但零字节 ───────────→ 连错 SCN（铁律5）

车机一个字节都不发？
├─ 车机热点扫不到 ──────────────→ 车机没进待机态（步骤1）
└─ 车机热点在
   ├─ 连上后零字节 + 1s 断开 ───→ SCN 连错，或车机 ACK 了旧连接
   │                              → 重置蓝牙：cmd bluetooth_manager disable，等 10s，enable
   └─ 收到 FF 55 但握手不过
      ├─ 收到 MARKER 后无下文 ──→ 没回 MARKER（铁律2）
      ├─ 车机反复重传 SYN ─────→ 回成纯 ACK 了，要 SYN|ACK（铁律3）
      └─ 收到 SYN 但 LSP 解析失败 → 帧头长度/校验算错，跑 verify_iap2_frames.py
WiFi 连不上？
├─ Specifier 报 not granted ────→ 换 WifiNetworkSuggestion
├─ 关联成功但 App 无动作 ───────→ 只挂了回调没调 awaitConnected
├─ SSID 读成 <unknown ssid> ────→ 改用 LinkProperties 反查（铁律6）
└─ cmd wifi status 报 disabled ─→ 该命令不可信，看 ip -4 addr show wlan0
mDNS 发现不到？
├─ 自己注册失败 ────────────────→ NsdManager 在无 Internet 的 ephemeral 网络上受限
└─ 车机不广播 ──────────────────→ 用网关 IP 直接试 :7000
投屏正常但触摸/方向盘无反应？
├─ 第一步仍先验会话形态（会话没被当 CarPlay，后面全白做）
│  ├─ CTRL_TCP_ACCEPT / CTRL_INVITATION_ACCEPTED 全 0
│  │  └─→ 车机没把这场会话当 CarPlay → 去查 ctrl 广播（端口动态、等 ≥5s 下发 connect、TXT 键集是否被车机认）
│  └─ 有 connect → 进 HID 协商栈（2026-10-31 晚更正：本 app 自带 HID 协商栈，是首要嫌疑，不是"改 HID 无用"，见铁律 34）
├─ HID 协商栈（铁律 34；2026-10-31 深夜更正：本 app 是**无线发送端**——手机画面推给车机，触摸由车机经 Wi-Fi RTSP 送回；注入走原生 source（t4/i6/c2），所以全库无 MotionEvent/onTouchEvent 是**正常的**，不能据此说"没有输入管线"）
│  ├─ ★ 第一步：`grep "HID_INPUT_MODE_SENT"` 有几行？（铁律 37：这是唯一能证明「请求真的发出去了」的日志）
│  │  ├─ **0 行**（而 `HID_CMD_REQ`/`HID_CHAN` 都有）→ 发送在 `y5.l()`/`g()` 里抛异常，被线程池吞了
│  │  │  └─→ 先查有没有在事件通道上多做了一次 `l()`（铁律 38：`l()` 会置 `t=true` + 撑大 pending 表，
│  │  │      下次就抛 `Too many pending RTSP control commands`）。V1.7.18 的 E16 priming 正是这么把 21 次发送全废掉的。
│  │  │      **不要**再补 priming（铁律 35：5001 实测单向，发什么都没用）。
│  │  └─ 有 N 行 → 请求发出去了，继续看下一层 `HID_SWEEP mode=`
│  ├─ ★ 第二步：`HID_SWEEP mode=` 是不是一直是同一个数？（铁律 36，2026-11-01 V1.7.19 真凶）
│  │  ├─ 恒为 `mode=0` → **MODE 扫描条件写反**：`if-gt` 应为 `if-le`，+1 后 1..5 全被 `const/4 v4,0` 清零。
│  │  │  └─→ `hidInputMode` 恒发 0（=未指定/无输入），车机不会认为触摸屏已启用 ⇒ 一条 `hidSendReport` 都不推。
│  │  │      修法见 `CarPlay-Build/patch_v1719.py` E17-b/E17-c；同时确认 `TRIES` 上限 ≥18（6 次只够 0..5 跑一轮）。
│  │  └─ mode 在 0..5 之间变化 → 扫描正常，看 `HID_REPORT_RECEIVED` 有没有来
│  ├─ ★ 第三步：事件通道入站字节（`CARPLAY_EVENT_INPUT ... raw_bytes=`）
│  │  └─ `raw_bytes=0 read_calls=1` 是**常态，不是病**（铁律 35：5001 单向）。只要控制通道
│  │     `CARPLAY_RESPONSE ... 200` 正常、`HID_INPUT_MODE_SENT` 有行，就不要把「事件通道 0 字节」当根因再去改通道/补 priming。
│  ├─ logcat OcarPlay:V 里 `HID_TOUCH_UNSUPPORTED ... reason=DESCRIPTOR`
│  │  └─→ `y0.j` 拒绝解析某条描述符 → 该设备被剔出 `z0()`；若被剔的是 index=0(touch) 则 `z0` 变空、`W()` 直接 SKIP 发 `hidSetInputMode` → 彻底无触摸。装 V1.7.15 后同行会 dump 失败描述符原始字节，用 `scripts/decode_hid_descriptor.py` 解，看 y0.j 卡在哪条校验
│  ├─ `HID_INPUT_MODE_SENT carrier=EVENT` / `HID_INPUT_MODE_ACCEPTED slot=` / `HID_INPUT_MODE_UNCONFIRMED ... TIMEOUT_OR_IO`
│  │  └─→ 2026-10-31 晚逐行确认（i0.smali 2820-2950 / 3517-3630 / 11774-11860）的实锤结论：
│  │      (a) **发送通道是对的**：`HidChannel()` 选事件通道 `y5.l()`（见 2838-2848 的 `if-ne p1,p2`），日志 `carrier=EVENT` 为真；控制通道 `y5.g()` 仅当 `p1==p2` 才走。
│  │      (b) **`UNCONFIRMED carrier=CONTROL` 是假标签**：那串 `carrier=CONTROL reason=TIMEOUT_OR_IO` 是超时分支里 `const-string`（i0.smali:3598）**硬编码的字面量**，并不表示真发在控制通道。别被它误导去改通道。
│  │      (c) **真凶在更上游**：原版日志 `touch=1` 已达成（HID_CAPABILITIES devices=3 touch=1），`hidSetInputMode` 也确实在事件通道发出（SENT 每隔 1s 重发），但 `HID_INPUT_MODE_ACCEPTED` **全 0**、15s future 超时 → `HidFail` → `ACCEPTED` 永不置，且 `HID_REPORT_RECEIVED` **全程 0 条**。
│  │      (d) **`ACCEPTED` 门禁不是触摸不动的直接原因**：`hidSendReport` 接收处理器（i0.smali:18568）只置 `REPORTED` 并停扫描，**不查 `ACCEPTED`**。也就是说：只要车机推报告，app 就会处理；车机一条都没推，才是死结。
│  │      ⇒ 结论：车机**既不回 ACK 也不推 `hidSendReport`**。这指向「会话没被车机认作完整 CarPlay 会话 / `hidSetInputMode` 载荷被车机静默丢弃」——与铁律 28（君威原生全程 `hidSetInputMode`=0 车机照样推 7 条）对照，说明车机对"被正确识别的 CarPlay 手机"会无条件推报告，本 app 的会话形态/握手没让车机进入该状态。
│  │      (e) HidOK 注释 "y5.g() 在 t=1 时必抛 requires encrypted main control" 是控制通道路径的历史坑，但本路径走的是事件通道 `l()`，注释与当前死链不直接相关，仅作背景。
│  │      取证要点（装 V1.7.15 后）：① `DESCRIPTOR_BYTES=` 那行（E12 dump 的失败描述符原始字节）用 `scripts/decode_hid_descriptor.py` 解，看 `y0.j` 卡哪条校验；② 抓 `HID_CMD_RESP len=... body=...` 看车机对 `hidSetInputMode` 的真实回包；③ 抓 `CTRL_TCP_ACCEPT`/`CTRL_INVITATION_ACCEPTED` 确认会话形态。
│  │      候选修复（V1.7.16 待定，须先看上面③的会话形态取证）：若会话形态 OK 但车机就是不推 → 重点查 `hidSetInputMode` 载荷构造（z0() 设备表是否含被拒描述符导致车机整体拒绝）；若会话形态不对（无 connect）→ 回到 ctrl 广播（铁律 29）。
│  ├─ 传输/会话定位（2026-10-31 深夜实锤，详见 `CarPlay-Build/docs/v1717-transport.md`）
│  │  ├─ `hidSendReport` 走 **Wi-Fi TCP 上的 RTSP**，不走蓝牙、不走 iAP2（日志 `CAPS ... iap_channel=false`）。蓝牙只做握手：`h5`/`r0` 建 RFCOMM **server**（`listenUsingRfcommWithServiceRecord`），`c0` 只代理 HFP/A2DP；全库无 BluetoothHidHost。用户报的 `handleAclConnected set deviceInfo failed!`、`RNR ... le_link:true` 在 smali **0 命中**＝系统蓝牙栈日志，**与触摸链路无关**，别往蓝牙方向查。
│  │  ├─ 两个会话都汇到同一个分发器 `i0.s1(y5$b, String channel)`（`y5$b`={uri, headers, body}）：
│  │  │    EVENT   `i0.b0`（`s2.b()` 建 socket，端口 **0x2710=10000**，`Events-Write-Encryption-Key`/`Events-Salt`；`i0.g`→`O0`→`s1(req,"EVENT")`）
│  │  │    CONTROL `i0.a0`（`i0.j0()`，`CARPLAY_CONTROL_CONNECTING`；`i0.w`→`S0`→`s1(req,"CONTROL")`），接线见 `j0.smali:73/85`（选择子 a=3→EVENT，a=4→CONTROL）
│  │  ├─ 分发落点：`i0.smali:17437` `:sswitch_6` → `:cond_32`(18579)，**不是空壳处理**：按 `params.uuid` 查 `I0`(touch)/`K0`(consumer)/`J0`(back)。
│  │  └─ 注入链（触摸真的有落地）：`z0.b([B,z0$f)` 解码 → `q6.n(...)` 换算显示坐标 → `i0$n.r(x,y,down)`(i0:19620) → `WirelessCarPlayService$c.r(IIZ)`(2189) → `t4.T(i6$a,x,y)` → `i6.a` 打包 → `c2.b(r6$a,len,payload)` → 原生 source 注入，日志 **`NATIVE_TOUCH_SENT action=`**；back 键走 `x0.f` + 日志 `native_key_written=true`。
│  ├─ 失败分支全是**回包字符串、默认不落日志**：`TOUCH_UNKNOWN_DEVICE`/`TOUCH_SCREEN_NOT_OWNED`/`TOUCH_ANOTHER_DEVICE_ACTIVE`/`TOUCH_DISPLAY_UNAVAILABLE`/`TOUCH_SOURCE_UNAVAILABLE`/`TOUCH_INVALID_ENVELOPE`/`TOUCH_SESSION_CLOSED`/`TOUCH_MISSING_REPORT`，以及 `t4.T` 的 `native source not ready; touch rejected`。**日志里没有 ≠ 没收到**。
│  ├─ V1.7.16 实机：`V153_EVENT_READY=true`、`HID_CHAN=EVENT`、`STATE I0=1`、`VIDEO_UNITS_SENT=1`，但 `CARPLAY_REMOTE_UI_REQUEST=0`、`CARPLAY_EVENT_INPUT=0`、`HID_REPORT_RECEIVED=0`、`NATIVE_TOUCH_SENT=0` ⇒ **EVENT 通道连着但对端一条应用层请求都没发**。
│  ├─ **V1.7.17(vc286) 实测结论（2026-11-01 00:05）：三选一命中①——`INBOUND_REQ` 一条都没有**（EVENT 与 CONTROL 均 0），且 `CARPLAY_EVENT_INPUT raw_bytes=0 read_calls=1`、控制通道 57,669 条 `CARPLAY_RESPONSE` 全 200。⇒ 不是「命令名不对/通道不对」，是**事件通道压根没被对端写入**，见上方 ★ 铁律 35（priming 缺失）。
│  └─ 若后续再排查（入站已有字节但无 `hidSendReport`）：② 有请求无报告 → 查 HID 设备表/`hidInputMode` 取值；③ 有报告 → 看回包错误码 + `NATIVE_TOUCH_SENT` / `native source not ready`。
│  └─ `NATIVE_TOUCH_SENT` / `HID_REPORT_RECEIVED` / `native_key_written`
│       └─→ 有 `NATIVE_TOUCH_SENT` 但车机屏 UI 不动 = 注入落到原生 source 了但 source 没把事件打进目标窗口（查 `TOUCH_SCREEN_NOT_OWNED` / `CARPLAY_SCREEN_OWNER_PHONE`）；三者全 0 = 报告根本没来，沿上面 `INBOUND_REQ` 查。
└─ CarLife 判别实验只在「想确认车机到底有没有触摸能力」时做（铁律 19），不要用来判断本 app 的 HID 栈对错
投屏一会儿就断？
├─ 存活时长很固定（60/62/79/106s）→ 超时，看事件通道 first-byte 是否到达
└─ 时长随机 ───────────────────→ 看 SCREEN_WRITE 队列与 6030 是否被对端关
```

## 官方时序基线（iPhone 原生实测，唯一权威参照）

来自 iPhone 17.7.1 真机 sysdiagnose（别克君威，首连 + 车机侧断开 + 自动重连）。
**怀疑自己实现有问题时，先对照这张表定位断点，细节见 `references/native-carplay-handshake.md`。**

| 阶段 | 真实耗时 | 关键动作（缺一个后面必挂） |
|---|---|---|
| A 蓝牙 | ~6 s | ACL 建链 → **full SDP（9 条记录）** → SSP 自发起配对（Auth type 2，车机屏确认） |
| B iAP2 | ~7 s | identify → **车机弹「允许」** → `requesting WiFi credentials` → 车机回 `0x5703`（ch149 / securityType 2）→ 存库 → **开 `_carplay-ctrl` 广播** |
| C WiFi | ~1 s | 凭据落库触发 auto-join → 调度切 `fg→carplay`、0 ms 重试 → 关联 → DHCP `192.168.5.26/24` |
| D Control | **4.7 s** | `CarPlayControlServer` 起 → 广播 `_carplay-ctrl`（51841）→ **车机下发 `connect`** → 建立 `APEndpointCarPlay`。注册到 connect 之间**静默 4.7 s**，这段时间不要抢跑 |
| E AirPlay | **118 ms** | SRV→`Android.local.`→`192.168.5.1:7000` → PAIR_SETUP 200 → 认证/授权/激活 → 端口 5001/5020/**6030** |
| F 断开 | — | 车机侧关 6030 → `StartupFailed -16613`。**码流本身没问题**：25 fps / 0.85 MB/s、零丢帧，首会话活 14 s、重连会话活 33 s，两次死法一样 |
| G 重连 | ~14 s | 跳过 BT/WiFi，只重跑 AirPlay 段；ctrl 换端口 51846 同名重注册；`screen iOS / mainAudio Car` |
| H 触摸 | 会话内 3.5 s 后 | 事件通道收到 `POST /command RTSP/1.0` + `hidSendReport`（Body 96），重连会话 15 s 内 **7 条**；全程 **0 次** `hidSetInputMode`、**0 次** `hidDevices` |

车机侧端口固定为 **7000(RTSP) / 5001(事件) / 5020(UDP 保活) / 6030(屏幕流)**，屏幕 **1280×768**，音频 AAC 48 kHz 立体声。
原生「连上」的完整链路耗时 ≈ 18 s；任一环节超 20 s 未完成，基本可判定失败。

**⚠️ 上表是 iPhone（ctrl 服务端）基线。本 app 是 ctrl 客户端**：V1.7.12 实机确认 app 注册
`_carplay-ctrl` 后**直接回连车机 7000**（`CARPLAY_CONTROL_CONNECTING`），车机**不会**回连 app 的 ctrl，
因此 D 阶段的「4.7 s 静默等车下发 connect」在本实现里不存在、也不要去复刻（V1.7.13 已移除等待门禁）。
H 阶段在本 app 上对应实机结果：`HID_CAPABILITIES touch=1` / `input=TOUCH_SCREEN` /
`hid_valid=true` 在 `native_hidSetInputMode_count=0` 下协商成功——**触摸能力不依赖 `hidSetInputMode`**，与铁律 28 一致。

**⚠️ 先分清「连不上」和「连上了被掐」**：本次实测里 iPhone 两个会话都成功建流（`FrameEncodes/sec 27`、
`EncoderDrops/sec 0`、`BytesSent/sec 887090`），随后由**对端**关闭 6030 接收端。若我们的现象是
「能投几秒黑屏/花屏后被踢」，问题在**接收端**（解码器初始化、宽高/帧率/色彩格式与 `fvdp_displaySetMode`
下发的 `1280x768@30 RGB_8bit` 是否一致），不在连接握手。取证细节见
`references/native-carplay-handshake.md` 第 6.1 节。

## 附带资源

| 文件 | 用途 |
|---|---|
| `references/iap2-link-layer.md` | 帧格式逐字节规格、官方握手时序、LSP 字段、CSM 结构 |
| `references/android-pitfalls.md` | 平台级坑：SCN/WiFi/mDNS/**反编译重打包/smali 条件码/logcat 取证/proc 取证** |
| `references/rtsp-flow.md` | AirPlay RTSP 完整命令序列与响应要点 |
| `references/touch-input-hid.md` | **触摸专项**：**第 16 节（新）为 iOS 17.7.1 逐条实证——车机主动推 `hidSendReport`、全程无 `hidSetInputMode`**；第 9 节为 CarLife 判别方法；1-7 节为已被推翻的 AirPlay HID 尝试，仅作历史参考 |
| `references/troubleshooting.md` | 真机日志样本对照（症状 → 日志特征 → 根因） |
| `references/native-carplay-handshake.md` | **iPhone 原生首连 + 自动重连全流程逐条实测**（阶段 A~H、端口表、结论；第 11 节为 2026-10-31 复核与勘误） |
| `references/airplay-pairing-spec.md` | **pair-setup / pair-verify 实现级规格**（反编译 DiPlay 0.2.20，含 SRP 3072 参数、120B 密文结构与控制密钥派生） |
| `scripts/verify_iap2_frames.py` | 帧格式与握手时序离线自检，改协议后必跑 |
| `scripts/verify_wifi_config.py` | 0x5703 / WiFi TLV 编解码自检 |
| `scripts/diagnose_adb.sh` | adb 一键体检：车机待机、WiFi、蓝牙 ACL、RFCOMM 通道 |
| `scripts/bt_flap_diagnose.sh` | **蓝牙打环量化**：敲门次数 / PSM 分布 / RFCOMM 心跳，直接给结论 |
| `scripts/probe_event_channel.sh` | **事件通道取证**：到车机的 socket 队列 + 裸探测是否有数据推来 |
| `scripts/hid_event_verdict.sh` | HID 通道结论机：抓 `carrier=` 日志给结论。**注意：其判据已被铁律 28 推翻，仅作历史参考** |
| `scripts/mdns_probe.py` | PC 侧手工 mDNS 查询 `_airplay._tcp`，绕过 App 直接确认车机是否广播 |
| `scripts/carplay_sysdiag_timeline.py` | 从 iOS sysdiagnose 还原 CarPlay 时间线（dsc v2 补丁）。**iOS 17 归档会解析出 0 行，见下一条** |
| `scripts/sysdiag_strings_probe.py` | **iOS 16/17 sysdiagnose 直读取证**（不依赖 UnifiedLog）：解压 `0x600d` 块 + 扫字符串 + 按进程/正则过滤。`timeline` 出 0 行时用它 |

## 改协议后的固定动作

每次调整帧格式、时序或 TLV 后，按顺序执行：

```bash
python scripts/verify_iap2_frames.py     # 帧格式 + 握手序列
python scripts/verify_wifi_config.py     # WiFi TLV
bash   scripts/diagnose_adb.sh           # 真机体检
```

离线自检不过就不要上真机——真机上分不清是协议错还是环境错，会浪费大量试错轮次。

**改插桩前先做一件事**：确认你用的「日志方法」真的打日志。反编译包里 `d0.c(String)` 这类
Kotlin `require*` 辅助方法是 `throw new IOException`，当日志用必炸（铁律 30）。
新加埋点一律走自己写的 `Log.i` 包装，别复用现成的小写单字母方法。

对照原生行为时，用 `scripts/sysdiag_strings_probe.py` 从 sysdiagnose 直接捞原文
（`carplay_sysdiag_timeline.py` 在 iOS 17 上会返回 0 行，见铁律 32）。

改完重打包上真机时，**必须验三样**，否则很可能一直在装旧包：

```bash
aapt2 dump badging "$APK" | grep -i versionName          # 版本号是新值
unzip -p "$APK" classes.dex | grep -c "<你的插桩字符串>"    # 改动真的进去了
adb shell dumpsys package <pkg> | grep versionCode       # 手机上装的是新包
```

## 反编译工程的构建与验收（铁律 63~65）

读 Java 源码工程（`D:/VSCode/CarplayAPK/java-project`）或用它对照 APK 时，
这三条决定结论可不可信。

**铁律 63：类名要改在 dex 层，不要在 Java 文本层做正则。**
jadx 的 Java 里同一个简单名有多种含义：`secured/l.java` 是顶层类，
`secured/i0.java` 里还有一个 `public interface l`。同一个 `l` 在 i0 内部指嵌套
接口、在 d0 里写作 `i0.l`、在别的类里写作 `l.foo()` 指顶层类。正则分不清，
改错一处就是编译错误。正确做法：`apktool d` 出 smali，把
`Lio/carplay/app/secured/i0;` 换成 `L.../AirPlaySession;`（边界用 `(?=[;$])`），
`apktool b` 重打包后再 jadx。描述符带分号，不存在遮蔽，也不存在泛型位置遗漏。

**铁律 64：javac 有语法错误时不进语义分析，「错误数对齐」是假验收。**
曾经用「原始 jadx 20 处语法错误 = 生成工程 20 处」当验收，看着完美。实际是：
只要有语法错误，javac 就停止，语义错误一条都不报。把 20 处修完才露出 426 条。
验收必须分开看：语法错误数、语义错误数，且以「原始产物修掉同类缺陷后」为基线。

**铁律 65：证明改名没改变行为，靠 smali 逐字节比对，不靠编译通过。**
编译通过只说明类型能对上，说明不了「还是原来那个 APK」。做法：
把原始 smali 树按同一映射正向改名（内容+路径），与改名后的树逐字节比。
V1.7.29 的结果：565 个文件、4 285 方法、2 525 字段、4 881 字符串常量，
14 584 处描述符替换，181 个文件改名，**565/565 完全一致**。

**顺带一条判读**：`long/boolean/int cannot be dereferenced` + `cannot find symbol`
成片出现时，多半不是代码逻辑问题，而是 R8 混淆后字段名与类名撞成同一个字母
（JLS 6.5.2 遮蔽规则：`c.BLOCKED` 里的 `c` 先当变量解释）。这类错误在原始
jadx 产物里同样存在，别记到改名头上。
