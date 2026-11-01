# AirPlay / CarPlay RTSP 建流流程

车机在 `_airplay._tcp` 上暴露 RTSP 服务，实测地址 `192.168.5.1:7000`，服务实例名 **`CarPlay`**。

## 1. 连接前置

- WiFi 已关联车机 AP，并拿到同网段 IP（实测 `192.168.5.101/24`）
- 手机侧需要以 CarPlay 主机身份在 mDNS 广播 `_carplay-ctrl._tcp`（端口 45697，TXT `srcvers=950.7.1`, `id=<本机 MAC>`）
- 蓝牙 iAP2 链路若已建立，`0x4300 DeviceTransportIdentifier` 会把该链路与 AirPlay 会话绑定

车机在建立会话时会反查对端 control 服务；没有广播的话，会话可能被判定为「非 CarPlay 主机」。

## 2. 报文格式

标准 RTSP/1.0 文本协议，`Content-Length` 分帧。部分步骤（`MFI_AUTH_SETUP` 之后）正文为加密的二进制 plist。

请求头至少包含：

```
CSeq: <递增整数>
User-Agent: AirPlay/950.7.1
Content-Type: application/octet-stream   (有 body 时)
Session: <建流后返回的 session id>        (后续请求)
```

## 3. 完整命令序列（官方 0.8.51 实测）

| CSeq | 方法 | 路径 | 加密 | 说明 |
|---|---|---|---|---|
| 1 | `OPTIONS` | `*` | 否 | 取支持方法列表 |
| 2 | `GET` | `/info` | 否 | 取车机能力表（bin-plist） |
| 3 | `POST` | `/pair-setup` | 否 | 配对第一步 |
| 4 | `POST` | `/pair-setup` | 否 | 配对第二步 |
| 5 | `POST` | `/pair-verify` | 否 | 校验第一步 |
| 6 | `POST` | `/pair-verify` | 否 | 校验第二步，此后密钥协商完成 |
| 7 | `POST` | `/mfi-auth-setup` | **是** | MFi 认证 |
| 8 | `POST` | `/stream-setup` | 是 | 第一次建流尝试 |
| 9 | `GET` | `/info` | 是 | 加密能力查询 |
| 10 | `POST` | `/command` | 是 | 会话控制 |
| 11 | `POST` | `/stream-setup` | 是 | 正式建流 |
| 12 | `POST` | `/stream-record` | 是 | 开始推流，1280×768@30 |
| 13 | `POST` | `/hid` | 是 | HID 通道建立 |
| 14 | `POST` | `/hid` | 是 | HID 报告 |
| 15+ | `POST` | `/feedback` | 是 | 反馈循环（持续） |

`PAIR_SETUP` 的最初几轮是明文 plist，`PAIR_VERIFY` 完成密钥协商后全部转为 ChaCha20-Poly1305 加密 payload。

## 4. 实测流量特征

12 秒内：

- 视频单元 236 个
- 累计 1.58 MB
- `pcmBytes = 0`（官方自己音频也未通，属正常现象，不要据此判定失败）

分辨率 `1280×768@30`，与 22 款君威中控屏一致。屏幕尺寸不同的车机会在 `/info` 的 bin-plist 里声明，按声明值设置。

## 5. 最小验证（确认对端是活的）

不必实现完整配对，先做两步就能确认协议栈对得上：

```bash
# 1. 端口与 RTSP 存活
printf 'OPTIONS * RTSP/1.0\r\nCSeq: 1\r\nUser-Agent: AirPlay/950.7.1\r\n\r\n' \
  | nc 192.168.5.1 7000

# 2. 取能力表（返回二进制 plist，能看到 model / features 等键）
printf 'GET /info RTSP/1.0\r\nCSeq: 2\r\nUser-Agent: AirPlay/950.7.1\r\n\r\n' \
  | nc 192.168.5.1 7000 | head -c 400
```

拿到 `/info` 的 plist 就说明链路层、WiFi、RTSP 三段全部打通，剩下的只是配对与加解密实现。

## 6. 常见失败点

| 现象 | 原因 |
|---|---|
| 连接被立即 RST | 车机未进入接收态（车机屏上没确认 CarPlay） |
| `OPTIONS` 正常，`/pair-setup` 403 | 已存在旧配对，需先 `POST /pair-remove` 或换设备标识重启 |
| `/stream-setup` 返回 4xx 且 body 不可读 | 加密密钥未协商好，检查 `pair-verify` 是否完整往返 |
| 建流成功但无画面 | `stream-record` 的 body 里 display 尺寸或 codec 与 `/info` 声明不符 |

## 7. 与蓝牙的关系

蓝牙 iAP2 只在两个地方对 RTSP 有用：

1. `0x4300 DeviceTransportIdentifier` —— 把蓝牙链路与 AirPlay 会话绑定，车机据此确认「这台手机就是刚才那台」
2. `1D01` 里的 `BluetoothTransportComponent` MAC —— 车机用它做设备识别

**画面与音频完全不走蓝牙。** 因此蓝牙握手失败时，只要 WiFi + RTSP 能走通，仍可能跑出画面；反之蓝牙再完美，WiFi 断开就一定黑屏。
