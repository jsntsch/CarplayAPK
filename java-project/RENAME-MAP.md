# 混淆名 ↔ 可读名 对照表

生成自 `tools/rename_map.py`（唯一事实来源，改名字请改那里再重跑脚本）。

- **已应用 107 个顶层类 + 11 个嵌套类重命名**；另有 **0 个**已分析待改（见末节）；
  其余 169 个保持原名（多数是无特征的工具/日志类）。
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
| `secured.d0` | **SyntheticRefs** | 0 | R8 合成的方法引用宿主：静态 b/c/d/f 分别是 IllegalArgumentException::new / IOException::new / IllegalStateException::new / r5::new；实例 a(long)=Thread::sleep、e(Map)=AirPlaySession.R0 委托 |
| `secured.a0` | **HidCommandSender** | 108 | iAPSendMessage / hidSendReport / setInputMode / changeVideoPlaybackToAudioOnly（同 Car+ a0） |
| `secured.p1` | **AudioSourceCapability** | 180 | audio_source_channel_unsupported / display_candidates / cap_main_display_unavailable |
| `secured.y0` | **HidConsumerParser** | 895 | hid_consumer_stack / hid_consumer_id / hid_consumer_report_length / hid_consumer_unknown_report |
| `secured.t4` | **DisplaySession** | 3003 | NATIVE_FOREGROUND_STATUS_OBSERVED / NATIVE_FIRST_VIDEO_CALLBACK_UNAVAILABLE / media_reta（3003 行主链路） |
| `secured.z0` | **HidDescriptorParser** | 712 | hid_long_item_unsupported / hid_logical_range / hid_report_id_limit / hid_unbalanced_stack |
| `secured.u5` | **Iap2Bootstrap** | 202 | iap2_ack_timeout / rfcomm_sele / wifi_selected_network_lost / wifi_selected_network_replaced |
| `secured.h5` | **BtDiscovery** | 392 | 00000000-deca-fade-deca-deafdecacafe / discovery_bluetooth_unavailable / eir_requested=true |
| `secured.x` | **HidCapabilities** | 1428 | cap_hid_incomplete / touch= / iapchannel / videoplayback（1428 行 HID 能力协商） |
| `secured.i0$b` | **Iap2PostResponseOwner** | 0 | iap2_post_response_owner / network_iap_post_response_queued / control post iap unavailable |
| `secured.x0` | **HidBackParser** | 859 | hid_back_layout / hid_back_not_declared / hid_back_report_length / hid_back_unknown_report |
| `secured.s` | **AudioCapabilities** | 780 | audioformats / audiolatencies / audio_native_format_unsupported / audio_type_unsupported |
| `secured.v1` | **EncryptProofNeg** | 730 | encrypt_ready before actual key/ack / proof before actual challenge / duplicate |
| `secured.z0$e` | **HidTouchParser** | 0 | hid_unsupported_touch_usage / hid_ambiguous_in_range / hid_ambiguous_confidence |
| `secured.l1$d` | **Srp3072** | 0 | pair-setup / SRP 大素数 ffffffff...c90fdaa22168c234c4c6628b80dc1cd129 |
| `secured.c1` | **Iap2PostResponseBuilder** | 76 | iap2_post_response_status / iap2_post_response_fields / iapsendmessage |
| `secured.j` | **AvcSpsParser** | 367 | not_sps / sps_metadata / unsupported_profile / unsupported_bit_depth |
| `secured.u1` | **MicCapture** | 256 | caller must confirm microphone format / microphone byte order/encoding unverified / 16000/mono/sig |
| `secured.t4$a` | **NativeAudioChannel** | 0 | native audio channel required / channels= / native audio aes negotiated without cipher |
| `secured.y2$f` | **Iap2FrameValidator** | 0 | invalid iap2 magic / invalid iap2 detect / truncated iap2 frame / iap2 header checksum |
| `secured.q1` | **Iap2Ncm** | 0 | bluetooth/iap2 bootstrap / host iap2/ncm / vehicle wi-fi/rtsp/media/input |
| `secured.j1$b` | **AvcVideoInput** | 0 | avc resume parameters must precede incoming video / avc slice header invalid / avc input invalid |
| `secured.r6` | **CarLifeFrame** | 309 | carlife / 8-byte header has no timestamp / truncated carlife frame / wrong header size |
| `secured.j1$a` | **AacPacket** | 0 | bounded raw aac-lc packet required / aligned bounded pcm16 packet required |
| `secured.n1` | **VideoEncoderStats** | 34 | queuedframesavg / sentframesavg / txusageavg |
| `secured.f6` | **RsaAesCrypto** | 105 | invalid negotiated aes key length / verified target aes configuration required |
| `secured.x0$a` | **HidKeyboardParser** | 0 | hid_back_value_range / hid_back_array_index |
| `secured.x1` | **VideoStreamNeg** | 127 | video payload decode failed / not a normal phone video payload / bounded video contract |
| `secured.f5` | **PcmAudioOutput** | 363 | audio stream is failed / partial/empty pcm frame / create a fresh session |
| `secured.g4$e` | **PcmFormat** | 0 | pcm sample encoding unsupported/unverified / pcm byte order unverified |
| `secured.j1$c` | **AvcNalCipher** | 0 | video access unit limit / avc nal limit / closed/exhausted video cipher |
| `secured.g4$h` | **VideoMime** | 0 | video dimensions/layout / video/hevc / unsupported video mime |
| `secured.b` | **SessionVerificationTracker** | 320 | VerifyResult 枚举 + 「session already failed / missing encryption verdict / awaiting observations」 |
| `secured.c` | **AesEcbCipher** | 121 | Cipher.getInstance("AES/ECB/PKCS5Padding") + SecretKeySpec |
| `secured.e` | **ByteArrays** | 85 | 全静态字节数组工具：全零判定 / Arrays.equals / 清零 / clone |
| `secured.f` | **AsymmetricKeyParameter** | 16 | implements CipherParameters + privateKey/isPrivate，对应 BC AsymmetricKeyParameter |
| `secured.g` | **AudioPacer** | 66 | AUDIO_PACER_RATE / AUDIO_PACER_PACKET 节拍器 |
| `secured.h` | **AvcAccessUnit** | 174 | AVC unit bounds/limit/picture mismatch，List<byte[]> NAL 组装 |
| `secured.i` | **AvcLengthParser** | 60 | truncated/invalid AVC length，ByteArrayOutputStream 解析 |
| `secured.i1` | **KeepAliveServer** | 66 | keepAlivePort / KEEPALIVE_PORT_INVALID + AutoCloseable |
| `secured.i2` | **CryptoServicesRegistrar** | 117 | globalConfig/threadLocalConfig/defaultRandomConfig/ecImplicitlyCA/dhDefaultParams，即 BC CryptoServicesRegistrar |
| `secured.i5` | **DeviceMetadataStore** | 159 | device UUID / display name / device metadata owners |
| `secured.i6` | **TouchGestureEncoder** | 111 | TouchAction 枚举 + duplicate down / no active gesture，产出 protobuf 触摸消息 |
| `secured.l4` | **MfiAuthSession** | 317 | MD_INFO / MD_AUTHEN_RESPONSE / CHALLENGE_ISSUED / empty mobile proof |
| `secured.m1` | **RemoteCommandHandler** | 219 | setNightMode / updateViewArea / REMOTE_VIEW_AREA_* 系列校验 |
| `secured.b2` | **RfcommSessionLog** | 287 | RFCOMM_SELECTED_PEER_* 事件记录 + SESSION START 分隔 |
| `secured.c2` | **CarLifeFrameCipher** | 53 | CarLifeFrame 明文/密文长度契约 + negotiated cipher required |
| `secured.c3` | **ResourceFingerprintBuilder** | 134 | NON_STRING/OVERSIZE/HASH_UNAVAILABLE/INITIAL_RESOURCE + key_fingerprints= |
| `secured.e4` | **MediaSourceTracker** | 116 | implements MediaFormat；sink/evidence、stale/wrong media source、source generation |
| `secured.g4` | **MediaFormat** | 0 | 被 PcmFormat / VideoMime 实现的媒体格式接口（video/avc、PCM sample encoding） |
| `secured.j2` | **DhParameters** | 70 | org.bouncycastle.dh.allow_unsafe_p_value + CipherParameters |
| `secured.m5` | **RsaPkcs1Cipher** | 245 | Cipher.getInstance("RSA/ECB/PKCS1Padding") |
| `secured.w1` | **ProtoMessageBase** | 0 | 所有 protobuf 消息类的共同父类：索引越界检查 + toString 字段拼接 |
| `secured.x2` | **HexCodec** | 71 | invalid characters encountered in Hex string / 十六进制编解码 |
| `secured.d3` | **IntegerMath** | 31 | a/b/c/d/e/f(int,int) 用 -Integer.MIN_VALUE 做无符号偏移的整数运算 |
| `secured.h1` | **ProtoFieldAccessor** | 83 | a(Map)→String / b(Object)→String / c(Object,Object)→boolean 的 protobuf 字段读取 |
| `secured.a2` | **LittleEndianShortCodec** | 46 | a(byte[],int)= bArr[i]|bArr[i+1]<<8，16 位小端读写 |
| `secured.y4` | **LittleEndianIntCodec** | 68 | a(byte[],int)= bArr[i]|bArr[i+1]<<8|bArr[i+2]<<16|bArr[i+3]<<24，32 位小端读写 |
| `secured.w5` | **ReleaseCatalog** | 241 | extends ProtoMessageBase；release integer / catalog redirect 校验 + carplus-carplay-updates/version.json |
| `secured.o5` | **CarLifeChannel** | 108 | implements l5 + AutoCloseable；静态工厂写入 "CARLIFE" 魔数，按串读写长度前缀帧 |
| `secured.g5` | **DeviceTimeRange** | 75 | DEVICE_TIME_RANGE / DEVICE_TIME_MINUTE_PRECISION + g(Instant, ZoneId) |
| `secured.g3` | **AesCipher** | 63 | 实现 o 接口；AES encrypt/decrypt + "AES encryption failed" + Provider/SecretKey/AlgorithmParameters |
| `secured.p6` | **IoStatsSnapshot** | 167 | EnumMap<ENCODE/DECODE/ADAPTER_QUEUE> 统计 + 五元 long 快照消息 |
| `secured.z5` | **RuntimeCryptoException** | 8 | 抽象基类；子类抛 Input/Output buffer too short、2^70 byte limit —— 对应 BC RuntimeCryptoException |
| `secured.n2` | **DataLengthException** | 8 | throw 站点全是 "Input buffer too short" —— BC DataLengthException |
| `secured.x4` | **OutputLengthException** | 8 | extends DataLengthException；throw 站点 "Output buffer too short" —— BC OutputLengthException |
| `secured.d4` | **MaxBytesExceededException** | 8 | throw 站点 "2^70 byte limit per IV would be exceeded" —— BC MaxBytesExceededException |
| `secured.f3` | **InvalidCipherTextException** | 8 | throw 站点 "mac check in ChaCha20Poly1305 failed" / "data too short" —— BC InvalidCipherTextException |
| `secured.d2` | **CryptoException** | 15 | 抽象 Exception + getCause；f3 的父类 —— BC CryptoException |
| `secured.o2` | **DecoderException** | 16 | throw 站点 "exception decoding Hex string: " —— BC DecoderException |
| `secured.r5` | **ProtocolIOException** | 16 | IOException 子类，携带协议层错误：ENCRYPT_READY before actual key/ack、body length exceeds channel limit |

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
| `prototype.m0` | **UiFactory** | 652 | 653 行、26 个静态方法，全部在拼 LinearLayout / TextView / GradientDrawable / CompoundButton；内嵌 a extends CompoundButton、b extends Drawable |
| `prototype.k0` | **StatusText** | 143 | 把上游 reason 字符串翻成中文提示语（AUTH/CERT/MFI/TIMEOUT/EOF 分类）；a(reason,connected) / b(state,connected,reason) / c(native_foreground) / d |
| `prototype.j0` | **DonationDialog** | 93 | 「自愿赞助 / 不影响功能」文案 + AlertDialog 构建；内嵌 Runnable 群 |
| `prototype.g` | **ReleaseCheckTask** | 0 | run() 执行发布校验：RELEASE_CHECK / APK_SIGNER_MISMATCH / UPDATE_REQUIRED / catalog 持久化 |
| `prototype.q` | **InstalledPackageIdentity** | 80 | a(Context) 取已安装包身份并比对；异常 installed package identity unavailable / unlisted APK splits |
| `prototype.r` | **BluetoothAdapterResolver** | 98 | a(Context,String) 返回 (address, PUBLIC_ADAPTER)；ocarplay-local-transport |
| `prototype.v` | **NetworkTransportDescriber** | 94 | b/c/d 生成 UNKNOWN/CELLULAR/WIFI/BLUETOOTH/ETHERNET 传输描述串 |
| `prototype.w` | **NetworkSelector** | 38 | a(Context)→Network、b(NetworkCapabilities)→boolean 的网卡挑选 |

## 为什么有些类没改名

| 情况 | 例子 | 原因 |
|---|---|---|
| 本来就是语义名 | `WirelessCarPlayService`、`MainActivity`、`LogActivity` | 原厂没混淆这些入口类 |
| 无特征字符串 | 多个 `b`/`l4`/`g5` 之类 | 类里只有 `StringBuilder` 拼接的日志，判不出职责 |
| 单字母 + 多义 | `secured.d` vs `prototype.d` | 同名不同类，强行改名会串味 |

想继续推进命名，做法是：从 `tools/features.json` 里挑 `loc` 大、`strings` 有
业务关键字的类，先确认它被谁调用（`grep`），再补进 `rename_map.CORE`，重跑脚本。

## 已分析、暂缓改名的嵌套类

当前**没有**暂缓项：11 个嵌套类（`Outer$Inner`）已于 2026-11-03 全部落地。

它们此前无法用文本正则改名，因为类名与同名字段/方法/局部变量大量共存
（`VideoCipher` 里既有嵌套类 `b`，又有方法 `VideoCipher.b(...)`；`g4` 里既有枚举
`e`/`h`，又有 `instanceof h` 与 `(e) obj` 强转），文本替换实测产生 300+ 处
`cannot find symbol`。最终改用 `tools/rename_engine/RenameTool.java`：
`javac` 语义分析后按 `Trees.getElement()` 得到的真实符号定位，安全落地。

| 原混淆名 | 拟用名 | 暂缓原因 |
|---|---|---|

## 与 APK 里 smali 的对应

| 可读名 | smali 路径 |
|---|---|
（当前没有对应 smali 文件）
