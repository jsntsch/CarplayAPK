# 混淆名 ↔ 可读名 对照表

生成自 `tools/rename_map.py`（唯一事实来源，改名字请改那里再重跑脚本）。

- **共 39 个类重命名**，其余 248 个保持原名（多数是无特征的工具/日志类）。
- 「行数」是该类在 jadx 产物里的规模，用来判断值不值得先读。

## secured 包（协议栈本体）

| 原混淆名 | 可读名 | 行数 | 命名依据 |
|---|---|---|---|
| `secured.i0` | **AirPlaySession** | 4418 | SETUP/INFO/RECORD/POST /command 全流程；4418 行、307 字段；对应 hypercar rust-core/airplay.rs + engine.rs |
| `secured.y5` | **EventChannel** | 982 | RTSP 事件通道：cseq / event response sequence / RTSP connection closed；铁律 27 已确认 |
| `secured.hx` | **ControlChannel** | 309 | V1711_DIAL_GATE + /command + ctrl_tcp_seen；邀请与控制命令通道 |
| `secured.s2` | **EventChannelConnector** | 103 | EVENT_CONNECT_CANCELLED / NO_SOCKET / INTERRUPTED —— 事件通道重试外壳 |
| `secured.s0` | **ControlRequestParser** | 169 | CTRL_HEADER_FIELD / transfer-encoding / content-length —— 控制通道 HTTP 报文解析 |
| `secured.xah` | **AppleHttpHeaders** | 29 | X-Apple-Client-Name / X-Apple-ProtocolVersion 注入 |
| `secured.e1` | **Iap2WirelessSetup** | 365 | IP iAP must not replay wireless setup —— Wi-Fi 上的 iAP2 通道 |
| `secured.y2` | **Iap2Csm** | 888 | CSM reassembly limit / CSM magic；对应 hypercar iap2/Iap2Csm.kt |
| `secured.b1` | **SessionStateMachine** | 934 | MODE_INITIAL / NOT_INITIALIZED 状态迁移 |
| `secured.k6` | **StagedPeer** | 147 | STAGED_PEER_CLOSED / STAGED_PEER_IDENTITY_CHANGED |
| `secured.a3` | **MfiCertificateVerifier** | 1631 | MFI_FIRST_USE_TRUST_STORAGE_UNAVAILABLE / invalid verified leaf |
| `secured.j4` | **MfiSap** | 265 | MFiSAP requires ... —— Apple MFi 认证协处理器协议 |
| `secured.l1` | **PairingTlv8** | 397 | application/pairing+tlv8；对应 hypercar pairing.rs / tlv8.rs |
| `secured.r2` | **X25519** | 907 | This method is only for use by X25519；对应 hypercar srp6a.rs 同层 |
| `secured.b6` | **ChaCha20** | 142 | expand 16-byte k / 32-byte k |
| `secured.n5` | **Poly1305** | 199 | Poly1305 key must be 256 bits |
| `secured.h6` | **EcdsaSignature** | 229 | SHA256withECDSA / release receipt signature invalid |
| `secured.z4` | **DerCodec** | 387 | Invalid DER length / Truncated DER key encoding |
| `secured.j1` | **VideoCipher** | 561 | closed/exhausted video cipher + AVC NAL |
| `secured.d` | **AacAudioFormat** | 192 | audio/mp4a-latm / AUDIO_AAC_LOCAL_FORMAT |
| `secured.d5` | **PcmConverter** | 302 | PCM_CONVERTER_FORMAT_UNSUPPORTED / OUTPUT_LIMIT |
| `secured.e5` | **PcmQueue** | 233 | PCM_QUEUE_FORMAT / PCM_QUEUE_PACKET |
| `secured.i4` | **PcmEndpoint** | 271 | PCM endpoint / stale PCM endpoint |
| `secured.o4` | **AudioRelay** | 180 | AUDIO_RELAY_PROGRESS channel= —— 音频转发进度 |
| `secured.l` | **BinaryPlist** | 579 | bplist00 / invalid plist header；对应 hypercar bplist.rs |
| `secured.q5` | **ProtobufReader** | 411 | invalid field number / truncated protobuf field |
| `secured.b3` | **IccoaBridge** | 468 | ICCOA callback size / missing sink |
| `secured.v4` | **TouchFlow** | 127 | NATIVE_TOUCH_FLOW down= / failed= |
| `secured.btdlg` | **BluetoothDialogRunnable** | 14 | 14 行，implements Runnable；蓝牙对话框延迟任务 |
| `secured.btsave` | **BluetoothSaveListener** | 17 | 17 行，implements DialogInterface.OnClickListener；保存蓝牙名 |

## prototype 包（业务层）

| 原混淆名 | 可读名 | 行数 | 命名依据 |
|---|---|---|---|
| `prototype.h` | **CarPlayCtrlAdvertiser** | 475 | _carplay-ctrl._tcp 注册 + 自 resolve 校验；对应 hypercar net/CarPlayCtrlAdvertiser.kt |
| `prototype.j` | **AirplayServiceProbe** | 469 | _airplay._tcp 发现 + candidate 选择；对应 hypercar net/AirplayServiceProbe.kt |
| `prototype.m` | **WifiNetworkMonitor** | 495 | CURRENT_WIFI_SELECTED_NETWORK_CHANGED / LOST |
| `prototype.c0` | **BluetoothRfcommProfiles** | 337 | RFCOMM_SELECTED_PROFILE_SNAPSHOT；对应 hypercar bluetooth/BtProfiles.kt |
| `prototype.r0` | **PairingSession** | 2515 | AirPlay 配对 UUID 00000000-deca-fade-deca-deafdecacafe；对应 hypercar pairing.rs |
| `prototype.f` | **AvcEncoder** | 1314 | AVC frame rate / AVC_ENCODER_FRAGMENT_SIZE |
| `prototype.d0` | **AppLogger** | 239 | CarPlusWireless / Log history rotation；注意 d0.c 会抛异常（铁律 30） |
| `prototype.z` | **UpdateChecker** | 148 | APK integrity + gitee 更新地址；对应 hypercar update/UpdateChecker.kt |
| `prototype.u` | **CarLifeBridge** | 570 | com.oplus.ocar / com.baidu.carlife.oppo —— CarLife/Car+ 虚拟屏桥 |

## 为什么有些类没改名

| 情况 | 例子 | 原因 |
|---|---|---|
| 本来就是语义名 | `WirelessCarPlayService`、`MainActivity`、`LogActivity` | 原厂没混淆这些入口类 |
| 无特征字符串 | `t4`(3003 行)、`x`(1428)、`y0`(895) | 类里只有 `StringBuilder` 拼接的日志，判不出职责 |
| 疑似日志辅助 | `b`、`r6`、`l4`、`f5`、`u5` | 同上，且被几十个类调用 |
| 单字母 + 多义 | `secured.d` vs `prototype.d` | 同名不同类，强行改名会串味 |

想继续推进命名，做法是：从 `tools/features.json` 里挑 `loc` 大、`strings` 有
业务关键字的类，先确认它被谁调用（`grep`），再补进 `rename_map.CORE`，重跑脚本。

## 与 APK 里 smali 的对应

| 可读名 | smali 路径 |
|---|---|
| `AirPlaySession` | `smali/io/carplay/app/secured/i0.smali` |
| `EventChannel` | `smali/io/carplay/app/secured/y5.smali` |
| `ControlChannel` | `smali/io/carplay/app/secured/hx.smali` |
| `EventChannelConnector` | `smali/io/carplay/app/secured/s2.smali` |
| `ControlRequestParser` | `smali/io/carplay/app/secured/s0.smali` |
| `AppleHttpHeaders` | `smali/io/carplay/app/secured/xah.smali` |
| `Iap2WirelessSetup` | `smali/io/carplay/app/secured/e1.smali` |
| `Iap2Csm` | `smali/io/carplay/app/secured/y2.smali` |
| `SessionStateMachine` | `smali/io/carplay/app/secured/b1.smali` |
| `StagedPeer` | `smali/io/carplay/app/secured/k6.smali` |
| `CarPlayCtrlAdvertiser` | `smali/io/carplay/app/prototype/h.smali` |
| `AirplayServiceProbe` | `smali/io/carplay/app/prototype/j.smali` |
| `WifiNetworkMonitor` | `smali/io/carplay/app/prototype/m.smali` |
| `BluetoothRfcommProfiles` | `smali/io/carplay/app/prototype/c0.smali` |
| `PairingSession` | `smali/io/carplay/app/prototype/r0.smali` |
| `MfiCertificateVerifier` | `smali/io/carplay/app/secured/a3.smali` |
| `MfiSap` | `smali/io/carplay/app/secured/j4.smali` |
| `PairingTlv8` | `smali/io/carplay/app/secured/l1.smali` |
| `X25519` | `smali/io/carplay/app/secured/r2.smali` |
| `ChaCha20` | `smali/io/carplay/app/secured/b6.smali` |
| `Poly1305` | `smali/io/carplay/app/secured/n5.smali` |
| `EcdsaSignature` | `smali/io/carplay/app/secured/h6.smali` |
| `DerCodec` | `smali/io/carplay/app/secured/z4.smali` |
| `AvcEncoder` | `smali/io/carplay/app/prototype/f.smali` |
| `VideoCipher` | `smali/io/carplay/app/secured/j1.smali` |
| `AacAudioFormat` | `smali/io/carplay/app/secured/d.smali` |
| `PcmConverter` | `smali/io/carplay/app/secured/d5.smali` |
| `PcmQueue` | `smali/io/carplay/app/secured/e5.smali` |
| `PcmEndpoint` | `smali/io/carplay/app/secured/i4.smali` |
| `AudioRelay` | `smali/io/carplay/app/secured/o4.smali` |
| `BinaryPlist` | `smali/io/carplay/app/secured/l.smali` |
| `ProtobufReader` | `smali/io/carplay/app/secured/q5.smali` |
| `AppLogger` | `smali/io/carplay/app/prototype/d0.smali` |
| `UpdateChecker` | `smali/io/carplay/app/prototype/z.smali` |
| `CarLifeBridge` | `smali/io/carplay/app/prototype/u.smali` |
| `IccoaBridge` | `smali/io/carplay/app/secured/b3.smali` |
| `TouchFlow` | `smali/io/carplay/app/secured/v4.smali` |
| `BluetoothDialogRunnable` | `smali/io/carplay/app/secured/btdlg.smali` |
| `BluetoothSaveListener` | `smali/io/carplay/app/secured/btsave.smali` |
