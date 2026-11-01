# -*- coding: utf-8 -*-
"""混淆名 -> 语义名 映射表（证据驱动，不做猜测）。

每条都给出「凭什么这么叫」的依据：要么是类里的协议字符串常量，
要么是它实现的框架接口，要么是已确认的分析结论（docs/v17xx-*.md）。
没有足够证据的类**一律保留原名**，在 RENAME-MAP.md 里标 unclassified。
"""

# (原全限定名, 新简单名, 依据)
CORE = [
    # ---------------- 会话 / 通道（CarPlay 主链路）----------------
    ("io.carplay.app.secured.i0", "AirPlaySession",
     "SETUP/INFO/RECORD/POST /command 全流程；4418 行、307 字段；对应 hypercar rust-core/airplay.rs + engine.rs"),
    ("io.carplay.app.secured.y5", "EventChannel",
     "RTSP 事件通道：cseq / event response sequence / RTSP connection closed；铁律 27 已确认"),
    ("io.carplay.app.secured.hx", "ControlChannel",
     "V1711_DIAL_GATE + /command + ctrl_tcp_seen；邀请与控制命令通道"),
    ("io.carplay.app.secured.s2", "EventChannelConnector",
     "EVENT_CONNECT_CANCELLED / NO_SOCKET / INTERRUPTED —— 事件通道重试外壳"),
    ("io.carplay.app.secured.s0", "ControlRequestParser",
     "CTRL_HEADER_FIELD / transfer-encoding / content-length —— 控制通道 HTTP 报文解析"),
    ("io.carplay.app.secured.xah", "AppleHttpHeaders",
     "X-Apple-Client-Name / X-Apple-ProtocolVersion 注入"),
    ("io.carplay.app.secured.e1", "Iap2WirelessSetup",
     "IP iAP must not replay wireless setup —— Wi-Fi 上的 iAP2 通道"),
    ("io.carplay.app.secured.y2", "Iap2Csm",
     "CSM reassembly limit / CSM magic；对应 hypercar iap2/Iap2Csm.kt"),
    ("io.carplay.app.secured.b1", "SessionStateMachine",
     "MODE_INITIAL / NOT_INITIALIZED 状态迁移"),
    ("io.carplay.app.secured.k6", "StagedPeer",
     "STAGED_PEER_CLOSED / STAGED_PEER_IDENTITY_CHANGED"),

    # ---------------- 服务发现（mDNS / NSD）----------------
    ("io.carplay.app.prototype.h", "CarPlayCtrlAdvertiser",
     "_carplay-ctrl._tcp 注册 + 自 resolve 校验；对应 hypercar net/CarPlayCtrlAdvertiser.kt"),
    ("io.carplay.app.prototype.j", "AirplayServiceProbe",
     "_airplay._tcp 发现 + candidate 选择；对应 hypercar net/AirplayServiceProbe.kt"),
    ("io.carplay.app.prototype.m", "WifiNetworkMonitor",
     "CURRENT_WIFI_SELECTED_NETWORK_CHANGED / LOST"),
    ("io.carplay.app.prototype.c0", "BluetoothRfcommProfiles",
     "RFCOMM_SELECTED_PROFILE_SNAPSHOT；对应 hypercar bluetooth/BtProfiles.kt"),

    # ---------------- 配对 / 证书 / 加密 ----------------
    ("io.carplay.app.prototype.r0", "PairingSession",
     "AirPlay 配对 UUID 00000000-deca-fade-deca-deafdecacafe；对应 hypercar pairing.rs"),
    ("io.carplay.app.secured.a3", "MfiCertificateVerifier",
     "MFI_FIRST_USE_TRUST_STORAGE_UNAVAILABLE / invalid verified leaf"),
    ("io.carplay.app.secured.j4", "MfiSap",
     "MFiSAP requires ... —— Apple MFi 认证协处理器协议"),
    ("io.carplay.app.secured.l1", "PairingTlv8",
     "application/pairing+tlv8；对应 hypercar pairing.rs / tlv8.rs"),
    ("io.carplay.app.secured.r2", "X25519",
     "This method is only for use by X25519；对应 hypercar srp6a.rs 同层"),
    ("io.carplay.app.secured.b6", "ChaCha20",
     "expand 16-byte k / 32-byte k"),
    ("io.carplay.app.secured.n5", "Poly1305",
     "Poly1305 key must be 256 bits"),
    ("io.carplay.app.secured.h6", "EcdsaSignature",
     "SHA256withECDSA / release receipt signature invalid"),
    ("io.carplay.app.secured.z4", "DerCodec",
     "Invalid DER length / Truncated DER key encoding"),

    # ---------------- 媒体 ----------------
    ("io.carplay.app.prototype.f", "AvcEncoder",
     "AVC frame rate / AVC_ENCODER_FRAGMENT_SIZE"),
    ("io.carplay.app.secured.j1", "VideoCipher",
     "closed/exhausted video cipher + AVC NAL"),
    ("io.carplay.app.secured.d", "AacAudioFormat",
     "audio/mp4a-latm / AUDIO_AAC_LOCAL_FORMAT"),
    ("io.carplay.app.secured.d5", "PcmConverter",
     "PCM_CONVERTER_FORMAT_UNSUPPORTED / OUTPUT_LIMIT"),
    ("io.carplay.app.secured.e5", "PcmQueue",
     "PCM_QUEUE_FORMAT / PCM_QUEUE_PACKET"),
    ("io.carplay.app.secured.i4", "PcmEndpoint",
     "PCM endpoint / stale PCM endpoint"),
    ("io.carplay.app.secured.o4", "AudioRelay",
     "AUDIO_RELAY_PROGRESS channel= —— 音频转发进度"),

    # ---------------- 数据格式 / 工具 ----------------
    ("io.carplay.app.secured.l", "BinaryPlist",
     "bplist00 / invalid plist header；对应 hypercar bplist.rs"),
    ("io.carplay.app.secured.q5", "ProtobufReader",
     "invalid field number / truncated protobuf field"),
    ("io.carplay.app.prototype.d0", "AppLogger",
     "CarPlusWireless / Log history rotation；注意 d0.c 会抛异常（铁律 30）"),
    ("io.carplay.app.prototype.z", "UpdateChecker",
     "APK integrity + gitee 更新地址；对应 hypercar update/UpdateChecker.kt"),
    ("io.carplay.app.prototype.u", "CarLifeBridge",
     "com.oplus.ocar / com.baidu.carlife.oppo —— CarLife/Car+ 虚拟屏桥"),
    ("io.carplay.app.secured.b3", "IccoaBridge",
     "ICCOA callback size / missing sink"),
    ("io.carplay.app.secured.v4", "TouchFlow",
     "NATIVE_TOUCH_FLOW down= / failed="),

    # ---------------- 小工具 ----------------
    ("io.carplay.app.secured.btdlg", "BluetoothDialogRunnable",
     "14 行，implements Runnable；蓝牙对话框延迟任务"),
    ("io.carplay.app.secured.btsave", "BluetoothSaveListener",
     "17 行，implements DialogInterface.OnClickListener；保存蓝牙名"),
]

# 已经是语义名、不需要改的（仅登记，便于生成报告）
KEEP = {
    "io.carplay.app.prototype.WirelessCarPlayService",
    "io.carplay.app.prototype.MainActivity",
    "io.carplay.app.prototype.LogActivity",
    "io.carplay.app.prototype.PairingPreparationService",
    "io.carplay.app.prototype.CarPlusApplication",
    "io.carplay.app.secured.ChaCha20Poly1305",
}

# 包不重命名：保持 io.carplay.app.secured / .prototype 原样。
# 理由：包名不影响阅读，改了要同步 import，收益低风险高。
PACKAGE_POLICY = "keep"
