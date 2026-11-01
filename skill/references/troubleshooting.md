# 真机日志样本对照

按「日志特征 → 判断 → 根因 → 修法」组织。日志取自 PKJ110 / Android 16 / ColorOS 与 22 款别克君威 myBuick 的实测记录。

## 1. 车机完全沉默

### 1.1 零字节 + 快速断开

```
RFCOMM 连接成功: myBuick (10:E8:A7:13:AD:5B) uuid=…caff
（此后无任何 RX）
Broken pipe
```

**根因**：车机未进入「无线 CarPlay 待机」界面。此时车机的 SDP 记录仍在、也接受 RFCOMM 连接，但 iAP2 状态机没启动。

**确认**：

```bash
adb shell "cmd wifi list-scan-results" | grep -i buick
```

扫不到车机热点即确认。

**修法**：让车机通电并进入 CarPlay / 手机互联界面，使热点出现在扫描结果里。这一步不通过，任何协议修改都无效。

### 1.2 收到 MARKER 但没下文

```
RX 6B: FF 55 02 00 EE 10
RX 6B: FF 55 02 00 EE 10
... （共 10 次，间隔 1 秒）
（9 秒后车机断开）
```

**根因**：收到 MARKER 没有回。MARKER 是双向探测，不回等于拒绝握手。

**修法**：收到立刻回一个 MARKER，并主动发 SYN。

### 1.3 MARKER 被当垃圾丢掉

```
收到 6B 非 iAP2 数据: FF 55 02 00 EE 10
```

**根因**：解析循环下界写成 `i + 9 <= size`，6 字节的 MARKER 进不去。

**修法**：下界改 6，先判 `FF 55` 再按 `length` 分流。

## 2. 握手卡住

### 2.1 车机反复重传 SYN

```
RX 26B ctl=0x80 seq=29 ...
RX 26B ctl=0x80 seq=29 ...
RX 26B ctl=0x80 seq=29 ...   （每秒一次，共 10 次）
```

**根因**：我方已发过 SYN，收到车机 SYN 时回了纯 ACK（0x40）。此时正确应答是 **SYN|ACK（0xC0, ack=车机 seq）**。

**修法**：维护握手进度状态；`step >= 1` 时改用 `sendSynAckPacket()`。

### 2.2 LSP 解析失败

```
← 车机 SYN 携带 LSP 解析失败（16B）
```

**根因**：帧头长度或校验算错，导致 payload 偏移错位。

**修法**：跑 `scripts/verify_iap2_frames.py`，逐帧核对 `length`、`header_cs`、`payload_cs`。

### 2.3 握手成功但没后续

```
✓ IAP2_LINK_READY
（无 1D00 发出）
```

**根因**：链路状态已置 NORMAL，但上层没收到「就绪」事件。

**修法**：`establish()` 返回 true 后立即发 `0x1D00`，不要依赖额外回调。

## 3. WiFi 凭据（0x5703）

### 3.1 车机对 0x5702 沉默

```
→ 0x5702（第 1/3 轮）
（无 0x5703）
→ 0x5702（第 2/3 轮）
（无 0x5703）
```

**根因**：车机侧需用户在屏上确认 CarPlay；或请求被自己的白名单挡掉。

**确认**：检查 `1D01` 解析出的可接收表。若 `SEND=0`，说明 `0x5703` 只能是应答，请求必须主动发出且重试。

**修法**：`0x5702` 重试 3 轮，每轮间隔 3~4 秒，并在第 2 轮后提示用户去车机屏点「允许」。若最终仍沉默，跳过蓝牙凭据，直接用已保存的热点密码走 WiFi 段。

### 3.2 官方 App 主动丢弃请求

```
IAP2_TX_SKIPPED_NOT_ADVERTISED id=5702
IAP2_TX_SKIPPED_NOT_ADVERTISED id=4300
IAP2_LOCAL_BLUETOOTH_IDENTIFIER_NOT_PROVIDED_NO_PLACEHOLDER_SENT
```

**根因**：把 `1D01` 的 RECEIVE 表当硬门禁。该表只有 8 项（`4E09,4E0A,4E0B,4E0C,4E0D,4E0E,EA00,EA01`），不含 `5702`/`4300`。

**修法**：识别、认证、`4300`、`5700/5701/5702` 一律放行。

### 3.3 解析出空密码

**根因**：TLV 写成「id 在前」，或字段号错位（把 passphrase 当 3、security 当 4）。

**修法**：改为 `[len][id][value]`，`1=SSID 2=Passphrase 3=Security 4=Channel`。跑 `verify_wifi_config.py` 验证。

## 4. WiFi 关联

### 4.1 Specifier 权限被拒

```
WifiNetworkSpecifier 连接异常: com.ocarplay was not granted
android.permission.CHANGE_NETWORK_STATE / WRITE_SETTINGS
```

**修法**：改 `WifiNetworkSuggestion`。

### 4.2 提交建议后整条链路断流

```
WifiConnection: addNetworkSuggestions(status=0) ssid=myBuick GS
WifiConnection: ✓ WiFi 建议已提交，等待系统关联
（此后 App 无任何日志，但系统日志显示 15:12:02 WiFi 已 CONNECTED）
```

**根因**：只挂了 `onStateChanged` 回调，而内部提交建议后只发 `CONNECTING`，从不发 `CONNECTED`。

**修法**：提交后显式调用 `awaitConnected()` 轮询等待。

### 4.3 一直等不到「已关联」

**根因**：`WifiInfo.ssid` 被脱敏成 `<unknown ssid>`，或 `netId = -1`、`ipAddress = 0`。

**修法**：三判据递进（SSID 匹配 → 脱敏但拿到 IP → 从 `ConnectivityManager` 的 `LinkProperties` 反查）。

## 5. 发现与建流

### 5.1 mDNS 发现成功（正常）

```
serviceDiscovery: [MdnsDiscoveryManager._airplay._tcp.local] Handling response from service: CarPlay
BonjourDiscovery: 发现 CarPlay 端点: ._airplay._tcp @ 192.168.5.1:7000 (CarPlay)
BonjourDiscovery: 发现完成，共 2 个端点
```

两个端点是：车机的 `_airplay._tcp`（`192.168.5.1:7000`）与自己的 `_carplay-ctrl._tcp`。

### 5.2 自己注册失败

```
MdnsAdvertiser: ✓ mDNS 已注册: OcarPlay null port=0
serviceDiscovery: Skipping SocketKey{...} as ifIndex ... was requested
```

**根因**：`NsdManager` 在无 Internet 的 ephemeral 网络上注册受限，`port=0` 表示未真正生效。

**修法**：不阻断发现流程；必要时改用手工组播（`MulticastSocket` 绑定 wlan0 接口）自行响应 PTR 查询。

### 5.3 车机不广播

**修法**：用 `wifiLinkInfo()` 读出的真实网关 IP 直接 `OPTIONS *` 探 `:7000`。

## 6. 车机行为（非 Bug）

| 现象 | 说明 |
|---|---|
| `AA03` 之后车机对所有消息沉默 | 车机策略：MFi 之后需屏上确认，或蓝牙仅用于敲门 |
| `wireless_carplay=0` | 传输组件表的读法问题，不代表不支持无线 |
| 音频 `pcmBytes=0` | 官方 App 同样如此，不是失败标志 |
| 车机不下发 `0x5703` | 见 3.1，主动请求 + 重试仍可拿到；拿不到就走已保存密码 |

## 7. 车机反复连接（蓝牙 ACL 打环）

### 7.1 每 1~2 秒一次 ACL，150 ms 就断

```
22:31:50.913 on_incoming_connection addr:xx:xx:xx:xx:ad:5b cod:760-4-08
22:31:50.929 OnConnectSuccess  initiator:remote handle:14
22:31:50.930 btm_sec_execute_procedure: security_required:0x0 ... Start get name
22:31:50.985 l2c_csm_open: Calling Disconnect_Ind_Cb(), CID: 0x0061
22:31:51.073 OnClassicLinkDisconnected reason:REMOTE_USER_TERMINATED_CONNECTION(0x13)
22:31:51.074 bta_dm_pm_stop: bta_dm_conn_srvcs.count=0
22:31:51.074 oplus_btif_hf: acl_disconnected: Invalid index -1
22:31:51.074 btif_av_handle_event: handle=0x0 event=ACL_DISCONNECTED
22:31:52.3xx （1.3 秒后下一轮敲门）
```

3.5 分钟窗口实测：**145 次 Connected / 80 次 Disconnected**，链路寿命 140~150 ms，回连间隔 1.2~1.3 s。

**先确认发起方**：`initiator:remote` + `is local inistated: false` → 车机发起、车机断开。手机侧没有打环。

**关键判据：数 L2CAP PSM 分布。**

```bash
adb logcat -d | grep -aoE "psm: [A-Z_0-9()x]+" | sort | uniq -c
```

| 分布 | 含义 |
|---|---|
| 只有 `BT_PSM_SDP`（实测 823 次），`BT_PSM_RFCOMM` 为 0 | 车机在 SDP 里查不到 iAP2 服务记录，**连 RFCOMM 都不尝试**就挂断 → 见 7.2 |
| 有 `BT_PSM_RFCOMM` 但连上零字节 | 另一个问题：连错 SCN，见排障决策树 |

`bta_dm_conn_srvcs.count=0` 也印证同样结论：HFP / A2DP / AVRCP 一个都没起来，车机是因为「连上了却拿不到服务」才反复重试。

### 7.2 根因：iAP2 服务记录只活了 90 秒

手机侧确实有 `listenUsingRfcommWithServiceRecord("… iAP2 Phone", 00000000-deca-fade-deca-deafdecacafe)`，但监听窗口是 **90000 ms**，用在三处（accept 循环截止、`postDelayed` 定时关闭、截止时间戳 `elapsedRealtime()+V`）。

窗口一过 → SDP 记录消失 → 车机查不到 → 挂断 → 1.3 秒后重试 → 无限循环。

**对照证据**：某一轮恰好在车机敲门期间应用把监听建起来，日志立刻出现

```
23:06:47.696 Bluetooth: RFCOMM_BufDataInd: event 4, p_mcb=…, scn=4
```

同一秒敲门停止，`23:06:46` 之后再无 `on_incoming_connection`。

**修法**：三处窗口值全部改成 `0x7fffffff`（约 24.8 天），让监听随进程常驻。只改一处等于没改。

**验证**：用 `cmd bluetooth_manager disable → enable` 做全链路复位，然后

```bash
adb logcat -d | grep -c "on_incoming_connection"      # 修复后应为 1~2 次后归零
adb logcat -d | grep -c "RFCOMM_BufDataInd"           # 应有持续心跳（约 30s 一次）
```

实测修复前后：`145 次/3.5 分钟持续敲门` → `2 次即建成，0 次断开`；静置 60 秒敲门数 2→2（零新增）。

**注意**：应用进程被杀，监听随之消失，车机会回到打环。要彻底根治需常驻前台服务。

### 7.3 排除掉的两个嫌疑人

| 嫌疑人 | 排除方法 | 结论 |
|---|---|---|
| OPPO「Car+ 车机互联」在打环 | `am force-stop com.oplus.ocar` 后统计敲门次数 | 127→142，照旧。它只是被敲门唤醒的旁观者 |
| 配对 / link key 陈旧 | `dumpsys bluetooth_manager` 查 Bonded devices 与 btif 配置 | 配对完好、有 32 字节 link key；`security_required:0x0` 说明根本没走到加密 |

## 8. 投屏正常但触摸/方向盘无反应

完整分析见 **`references/touch-input-hid.md`**。这里只放最短的日志对照。

### 8.1 应用侧一切正常的证据（说明不是解析问题）

```
HID_CAPABILITIES devices=3 touch=1
HID_TOUCH_DESCRIPTOR index=0 bytes=62 display_bound=true
HID_BACK_DESCRIPTOR  index=2 bytes=72
STATE I0=1  →  d0 gatePassed==true
>>> EXECUTE send hidSetInputMode
HID_INPUT_MODE_SENT slot=0 carrier=CONTROL
HID_INPUT_MODE_ACCEPTED slot=0 status=200
HID_INPUT_MODE_ACCEPTED slot=1 status=200
```

握手全绿，但 `HID_REPORT_RECEIVED` 恒为 **0**。

### 8.2 决定性判据：事件通道两侧都是 0

```
CARPLAY_EVENT_INPUT point=TRANSPORT_END stage=RECORD_HEADER read_calls=1 raw_bytes=0 messages=0 requests=0
SCREEN_WRITE_STALLED
```

```bash
adb shell "cat /proc/net/tcp /proc/net/tcp6" | grep -i "0105A8C0" | awk '{print "st="$4, $2"->"$3, "tx:rx="$5}'
# st=01 …:5001 -> …:5001 tx:rx=0:0     ← 车机一个字节没写
```

裸探测同端口也是 0 字节 → 与我们的实现无关。

### 8.3 命令发到了错误的通道（隐蔽的真凶）

```
23:42:01.123  CARPLAY_EVENT_CONNECTING
23:42:01.197  W: d0 gatePassed==true
23:42:01.197  W: b0=NULL          ← 事件通道对象此刻还不存在
```

`hidSetInputMode` 因此永远落在控制通道上。控制通道上这条命令只会拿到 200，不构成「事件通道已激活」的信号。

### 8.4 会话精确 60 秒被拆

三次实测 79 s / 106 s / **62 s**，其中一次 `22:52:25 → 22:53:25 = 60.0 s`。固定时长 = 超时，不是随机掉线。

**「投屏一会儿就断」和「触摸没反应」是同一根因的两个表现**，不要拆成两个问题分别调。
