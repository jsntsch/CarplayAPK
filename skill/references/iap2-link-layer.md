# iAP2 链路层与 CSM 逐字节规格

权威依据：wiomoc/iap2 `link_layer.py` / `csm.py`，并与真机抓包（别克君威 myBuick，PKJ110 Android 16）逐字节对齐。

## 1. 链路层帧格式

```
偏移  长度  字段
 0     1   0xFF            同步字节 1
 1     1   0x5A            同步字节 2
 2     2    total_length   整包总长，大端
 4     1    control        控制位
 5     1    seq            本端发送序号
 6     1    ack            对最近收到帧的确认号
 7     1    session_id     链路包恒为 0x00
 8     1    header_cs      头部校验
 9     N    payload
 9+N   1    payload_cs     负载校验（N>0 时才有）
```

- `total_length` 含全部字节：有负载时为 `payload_len + 10`，无负载时为 `9`
- `header_cs = (-sum(前 8 字节)) & 0xFF`（不含自身）
- `payload_cs = (-sum(payload)) & 0xFF`
- 校验算法为二进制补码求和（two's complement），不是 CRC

## 2. MARKER 探测帧

```
FF 55 02 00 EE 10
```

固定 6 字节。`length = 0x0002`（只覆盖后面的 2 字节），control 位为 `0xEE`（包含 EAK 等位）。

**校验范围只有 `02 00 EE`**，即 `(-(0x02+0x00+0xEE)) & 0xFF = 0x10`。

### 解析陷阱

解析器的主循环若写成 `while (i + 9 <= size)`，MARKER（6 字节）永远进不去，会被整包当作「非 iAP2 数据」丢弃。必须以 **6** 作为最小帧长下界，先判 `FF 55`，再按 `length` 分流。

## 3. control 位

| 位 | 值 | 含义 |
|---|---|---|
| SYN | 0x80 | 同步 / 携带 LSP |
| ACK | 0x40 | 确认 |
| EAK | 0x20 | 扩展确认（MARKER 用到） |
| RST | 0x10 | 重置链路 |

组合：`SYN|ACK = 0xC0`。判断时必须用位与，`0xC0` 要同时命中 `SYN` 与 `ACK` 两个分支，漏了就会走进错误分支。

## 4. LSP（Link Sync Parameters）负载

车机 SYN / SYN|ACK 携带 16 字节 LSP。真机实测字段值：

| 字段 | 实测值 | 说明 |
|---|---|---|
| maxOutgoing | 5 | 发送窗口 |
| maxLen | 2048 | 最大包长 |
| retransmissionTimeout | 1500 ms | 重传超时 |
| ackTimeout | 73 ms | 延迟 ACK 时间 |
| maxRetransmissions | 30 | 最大重传次数 |
| maxAck | 3 | 累积确认阈值 |
| sessions | `sid=1/type=0/ver=1`, `sid=3/type=2/ver=1` | 控制会话 + 外部配件会话 |

编码顺序为字段直连（大端），末尾是 `numSessions` 加若干个 3 字节会话描述。**不要硬编码偏移**——按 `numSessions` 反推长度并校验 `payload_len == 10 + 3 * numSessions`，不符就当解析失败，避免把错位数据当有效参数。

`type`：`0` = 控制会话，`2` = 外部配件（EA）会话。

## 5. 握手时序（真机实测）

### 5.1 官方 App 抓包（0.8.51）

```
RFCOMM 连接成功（scn=5）
RX  6B   MARKER                  FF 55 02 00 EE 10
RX 26B   ctl=0x80 seq=100 ack=0   sid=0  LSP(16B)    车机 SYN
TX 26B   ctl=0x80 seq=98  ack=0   sid=0  LSP(16B)    我方 SYN
RX 26B   ctl=0xC0 seq=101 ack=98  sid=0  LSP(16B)    车机 SYN|ACK
TX  9B   ctl=0x40 seq=98  ack=101 sid=0               我方 ACK
TX 26B   ctl=0x80 seq=99  ack=101 sid=0  LSP(16B)    我方 SYN
RX  9B   ctl=0x40 seq=101 ack=99  sid=0               车机 ACK
TX  9B   ctl=0x40 seq=99  ack=101 sid=0               我方 ACK
→ IAP2_LINK_READY
TX 16B   CSM 0x1D00
```

### 5.2 自研实现跑通的最短路径

```
RX  6B   MARKER
TX  6B   MARKER（必须回）
TX 26B   ctl=0x80 seq=98 ack=0    sid=0 LSP           SYN#1
RX 26B   ctl=0x80 seq=99 ...      sid=0 LSP           车机 SYN
TX 26B   ctl=0xC0 seq=99 ack=99   sid=0 LSP           SYN|ACK  ← 关键
RX  9B   ctl=0x40 ack=99
→ IAP2_LINK_READY
```

两条路径的差异在于谁先发 SYN。**两种都要兼容**：收到 MARKER 就回 MARKER 并主动发 SYN；同时也要能处理车机先发 SYN 的情况。

### 5.3 序号规则

- 每方向独立计数，不要用对端 seq/ack 覆盖自己的 `sentPsn`
- 我方初值 98，每发一个 SYN 递增 1
- 纯 ACK 不消耗序号
- `ack` 填「最近成功收到的对端 seq」

## 6. CSM（Control Session Message）

```
偏移  长度  字段
 0     1   0x40
 1     1   0x40
 2     2   total_length   整条 CSM 长度，大端
 4     2   message_id     大端
 6     ... 参数 TLV 序列
```

参数 TLV 为 **长度在前**：

```
[param_len 2B][param_id 2B][value ...]
```

注意与 `0x5703` WiFi 配置的 TLV 一致（都是 len-first），但与部分文档里「id 在前」的写法相反。搞反会导致取值全部错位。

## 7. 常用消息 ID

| ID | 名称 | 方向 |
|---|---|---|
| 0x1D00 | StartIdentification | 手机 → 车机 |
| 0x1D01 | IdentificationInformation | 车机 → 手机 |
| 0x1D02 | IdentificationAccepted | 手机 → 车机 |
| 0x1D03 | IdentificationRejected | 手机 → 车机 |
| 0x4300 | DeviceTransportIdentifier | 手机 → 车机（绑定蓝牙链路 ↔ AirPlay 会话） |
| 0x4E09 | AccessoryInfo | 双方 |
| 0x4E0A | PeerInfo | 双方 |
| 0x4E0B | TransportComponents | 双方 |
| 0x4E0C | Capabilities | 双方 |
| 0x4E0D | StartDependencies | 双方 |
| 0x5702 | RequestAccessoryWiFiConfigurationInformation | 手机 → 车机 |
| 0x5703 | AccessoryWiFiConfigurationInformation | 车机 → 手机（SSID/密码明文） |
| 0xAA00 | EA会话启动 | 手机 → 车机 |
| 0xAA01 | EA会话已启动（约 900B，含配件证书链） | 车机 → 手机 |
| 0xAA02 | 认证挑战（20B 随机数） | 手机 → 车机 |
| 0xAA03 | 挑战签名（132B） | 车机 → 手机 |
| 0xAA05 | 认证完成 | 双方 |
| 0xEA00 | CarPlay 会话启动 | 手机 → 车机 |
| 0xEA01 | CarPlay 会话已启动 | 车机 → 手机 |

## 8. 0x5703 WiFi 配置参数

```
1 = SSID          UTF-8 字符串
2 = Passphrase    UTF-8 字符串
3 = SecurityType  1 字节：0=NONE 1=WEP 2=WPA/WPA2
4 = Channel       1 字节（0 表示自动）
```

真机响应约 16 字节（`myBuick GS` + 密码短串时）。长度随 SSID/密码变化，不要按固定长度解析。

## 9. 1D01 里的两道门

车机在 1D01 中声明两个方向的可接收消息表：

```
IAP2_PEER_MESSAGE_IDS direction=SEND    count=0 ids=NONE
IAP2_PEER_MESSAGE_IDS direction=RECEIVE count=8 ids=4E09,4E0A,4E0B,4E0C,4E0D,4E0E,EA00,EA01
```

- `SEND=0` 意味着车机**不主动推送任何消息**，`0x5703` 只可能是对 `0x5702` 的应答
- 若把 `RECEIVE` 表当硬门禁，`0x5702` / `0x4300` 会被自己的白名单挡掉（官方 App 就是这样，认证后 1 ms 丢弃请求）
- 正确做法：识别、认证、`0x4300`、`0x5700/0x5701/0x5702` 一律放行，其余才查表
