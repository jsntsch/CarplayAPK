#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为 CarplayAPK 源码批量添加「协议头 + 中英对照用途注释」。

Adds a bilingual license/purpose header and a bilingual Javadoc block to every
type declaration.  Idempotent: files that already carry the SPDX marker or an
existing Javadoc in front of a type are left untouched.

用法 / Usage:
    python tools/annotate_sources.py [--src <java source root>] [--check]
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

# --------------------------------------------------------------------------
# 协议头 / License header
# --------------------------------------------------------------------------
HEADER = [
    "/*",
    " * SPDX-License-Identifier: MIT",
    " * Copyright (c) 2026 SCH",
    " *",
    " * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。",
    " * This file is released under the MIT License; see LICENSE in the project root.",
    " *",
    " * 文件用途 / File purpose:",
    " *   {zh}",
    " *   {en}",
    " *",
    " * 所属子系统 / Subsystem:",
    " *   {scope}",
    " */",
]

SPDX_MARK = "SPDX-License-Identifier"

# --------------------------------------------------------------------------
# 子系统说明 / Subsystem description
# --------------------------------------------------------------------------
PKG_ZH = {
    "io/carplay/app/secured": "协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道）",
    "io/carplay/app/prototype": "业务层（界面、前台服务、配对编排、原生组件桥接）",
    "org/bouncycastle": "随包内置的 Bouncy Castle 精简实现（Ed25519 / X25519 / ChaCha20-Poly1305）",
}
PKG_EN = {
    "io/carplay/app/secured": "protocol core (AirPlay / iAP2 / HID / crypto / media channels)",
    "io/carplay/app/prototype": "app layer (UI, foreground service, pairing orchestration, native bridge)",
    "org/bouncycastle": "vendored Bouncy Castle subset (Ed25519 / X25519 / ChaCha20-Poly1305)",
}

# --------------------------------------------------------------------------
# 用途描述库 / Curated descriptions
#    key = 相对源码根的路径（不含 .java），value = (中文, English)
# --------------------------------------------------------------------------
CURATED: dict[str, tuple[str, str]] = {
    # ---------------- secured：会话与通道 ----------------
    "secured/AirPlaySession": (
        "AirPlay 会话主流程：SETUP / INFO / RECORD / SET_PARAMETER、POST /command 的处理，"
        "以及视频、音频、HID、事件各通道的建立、保活与拆除。",
        "Main AirPlay session: handles SETUP / INFO / RECORD / SET_PARAMETER and POST /command, "
        "and owns setup, keep-alive and teardown of the video, audio, HID and event channels.",
    ),
    "secured/DisplaySession": (
        "显示会话：把解码后的视频帧、触摸与按键事件编码成 CarLife 原生帧写入车机通道。",
        "Display session: encodes decoded video frames plus touch/key events into CarLife "
        "native frames and writes them to the head-unit channel.",
    ),
    "secured/ControlChannel": (
        "控制通道：接收车机邀请、完成 iAP2 控制握手并维护控制连接状态。",
        "Control channel: accepts head-unit invitations, performs the iAP2 control handshake "
        "and maintains control-connection state.",
    ),
    "secured/EventChannel": (
        "事件通道：承载车机推送的触摸/按键/命令（/command、/feedback），并维持长连接与保活。",
        "Event channel: carries head-unit pushed touch, key and command traffic "
        "(/command, /feedback) and keeps the long-lived connection alive.",
    ),
    "secured/EventChannelConnector": (
        "事件通道连接器：建立 TCP 连接、区分超时/取消/IO 失败并给出可诊断的错误码。",
        "Event-channel connector: establishes the TCP connection and classifies "
        "timeout / cancel / IO failures into diagnosable error codes.",
    ),
    "secured/RemoteCommandHandler": (
        "远程命令处理器：解析并分发车机下发的 /command 请求（播放控制、会话控制等）。",
        "Remote command handler: parses and dispatches /command requests pushed by the head unit "
        "(playback control, session control, …).",
    ),
    "secured/KeepAliveServer": (
        "UDP 保活服务：按 5 秒间隔向车机发送保活包，避免车机端看门狗判定链路失效。",
        "UDP keep-alive server: sends a keep-alive datagram every 5 seconds so the head-unit "
        "watchdog does not declare the link dead.",
    ),
    "secured/CarLifeChannel": (
        "CarLife 原生通道：对 CarLife 帧做 HMAC 摘要、收发与顺序校验。",
        "CarLife native channel: HMAC digest, send/receive and ordering checks for CarLife frames.",
    ),
    "secured/CarLifeFrame": (
        "CarLife 帧结构：头部字段、长度与校验和的编解码与边界校验。",
        "CarLife frame layout: encoding, decoding and bounds checks for header fields, "
        "length and checksum.",
    ),
    "secured/CarLifeFrameCipher": (
        "CarLife 帧加密：使用协商出的会话密钥对控制类帧做加解密。",
        "CarLife frame cipher: encrypts/decrypts control-class frames with the negotiated session key.",
    ),
    "secured/IccoaBridge": (
        "ICCOA 桥接：把 ICCOA 回调转换为内部事件，并防止过期代次（stale generation）回调。",
        "ICCOA bridge: translates ICCOA callbacks into internal events and rejects stale generations.",
    ),

    # ---------------- secured：iAP2 / 配对 ----------------
    "secured/Iap2Bootstrap": (
        "iAP2 引导：在 RFCOMM 链路上完成链路层协商前的准备与首帧交互。",
        "iAP2 bootstrap: prepares the RFCOMM link and performs the first frame exchange "
        "before link-layer negotiation.",
    ),
    "secured/Iap2Csm": (
        "iAP2 CSM（连接状态管理）消息：重组分片、校验 magic 与长度。",
        "iAP2 CSM (connection state management) messages: reassembles fragments and validates "
        "magic and length.",
    ),
    "secured/Iap2Ncm": (
        "iAP2 NCM 接口：定义通知/控制消息的媒体格式契约。",
        "iAP2 NCM interface: media-format contract for notification and control messages.",
    ),
    "secured/Iap2PostResponseBuilder": (
        "iAP2 POST 响应构造：生成 iAPSendMessage 信封与响应字段。",
        "iAP2 POST response builder: builds the iAPSendMessage envelope and response fields.",
    ),
    "secured/Iap2WirelessSetup": (
        "iAP2 无线设置：下发/接收 WiFi 凭据与网络配置，驱动无线链路建立。",
        "iAP2 wireless setup: exchanges WiFi credentials and network configuration to bring up "
        "the wireless link.",
    ),
    "secured/PairingSession": (
        "配对会话：RFCOMM 建链、iAP2 握手、WiFi 凭据下发与配对状态推进。",
        "Pairing session: RFCOMM link setup, iAP2 handshake, WiFi credential delivery and "
        "pairing state progression.",
    ),
    "secured/PairingTlv8": (
        "配对 TLV8 编解码：解析/生成配对载荷中的 TLV 条目（身份、公钥、签名等）。",
        "Pairing TLV8 codec: parses and emits TLV items in pairing payloads "
        "(identity, public key, signature, …).",
    ),
    "secured/StagedPeer": (
        "暂存对端：保存尚未完成验证的对端身份，验证通过后才提交为可信设备。",
        "Staged peer: holds a not-yet-verified peer identity until verification succeeds.",
    ),
    "secured/DeviceMetadataStore": (
        "设备元数据存储：校验并保存设备 UUID、显示名等配对元数据。",
        "Device metadata store: validates and persists device UUID and display name used in pairing.",
    ),
    "secured/BtDiscovery": (
        "蓝牙设备发现：按 iAP2 服务 UUID 过滤车机并缓存候选设备。",
        "Bluetooth discovery: filters head units by iAP2 service UUID and caches candidates.",
    ),
    "secured/RfcommSessionLog": (
        "RFCOMM 会话日志：按统一格式记录建链、选路与失败原因。",
        "RFCOMM session log: records link setup, profile selection and failure reasons "
        "in a uniform format.",
    ),

    # ---------------- secured：MFi / 加解密 ----------------
    "secured/MfiAuthSession": (
        "MFi 认证会话：驱动 MFi 挑战-响应流程并维护认证代次。",
        "MFi authentication session: drives the MFi challenge-response flow and tracks "
        "authentication generations.",
    ),
    "secured/MfiCertificateVerifier": (
        "MFi 证书校验：解析并验证 Apple 签发的证书链、有效期与用途。",
        "MFi certificate verifier: parses and validates the Apple-issued certificate chain, "
        "validity window and key usage.",
    ),
    "secured/MfiSap": (
        "MFi SAP 协议：构造/解析 SAP 请求与响应，处理配对验证前置条件。",
        "MFi SAP protocol: builds and parses SAP requests/responses and enforces "
        "pair-verification preconditions.",
    ),
    "secured/EncryptProofNeg": (
        "加密证明协商：与车机协商加密算法与证明材料，记录对端证据。",
        "Encryption proof negotiation: negotiates cipher suites and proof material with the "
        "head unit and records peer evidence.",
    ),
    "secured/RsaAesCrypto": (
        "RSA + AES 混合加密：RSA 交换会话密钥，AES 承载后续加密载荷。",
        "RSA + AES hybrid crypto: exchanges the session key with RSA and protects later "
        "payloads with AES.",
    ),
    "secured/RsaPkcs1Cipher": (
        "RSA PKCS#1 签名/验签：处理 MFi 证书与非 X.509 证书的签名校验。",
        "RSA PKCS#1 sign/verify: signature checks for MFi and non-X.509 certificates.",
    ),
    "secured/AesCipher": (
        "AES 加解密封装：显式指定算法/模式/填充，失败时给出明确错误。",
        "AES cipher wrapper: requires explicit algorithm/mode/padding and reports clear errors.",
    ),
    "secured/AesEcbCipher": (
        "AES/ECB/PKCS5Padding 工具：用于车机协议里固定密钥字段的加解密。",
        "AES/ECB/PKCS5Padding helper for fixed-key fields in the head-unit protocol.",
    ),
    "secured/ChaCha20": (
        "ChaCha20 流密码：密钥流生成与加解密，支持 8/12 字节 nonce 变体。",
        "ChaCha20 stream cipher: keystream generation and encryption, with 8/12-byte nonce variants.",
    ),
    "secured/Poly1305": (
        "Poly1305 消息认证码：为 ChaCha20 提供完整性校验。",
        "Poly1305 MAC: provides integrity protection for ChaCha20.",
    ),
    "secured/X25519": (
        "X25519 密钥协商：椭圆曲线 Diffie-Hellman 实现（含有限域与标量乘内部运算）。",
        "X25519 key agreement: elliptic-curve Diffie-Hellman implementation, including the "
        "field and scalar-multiplication internals.",
    ),
    "secured/EcdsaSignature": (
        "ECDSA 签名校验：验证发布信息与回执的签名。",
        "ECDSA signature verification for release metadata and receipts.",
    ),
    "secured/DerCodec": (
        "DER 编解码：解析/生成 MFi 密钥与证书使用的 DER 结构。",
        "DER codec: parses and emits the DER structures used by MFi keys and certificates.",
    ),
    "secured/CryptoServicesRegistrar": (
        "Bouncy Castle 全局配置：管理默认随机源与线程本地配置。",
        "Bouncy Castle global configuration: manages the default random source and "
        "thread-local configuration.",
    ),
    "secured/AsymmetricKeyParameter": (
        "非对称密钥参数基类：Ed25519 / X25519 密钥参数的公共父类。",
        "Asymmetric key parameter base class shared by Ed25519 / X25519 key parameters.",
    ),
    "secured/DhParameters": (
        "Diffie-Hellman 参数：保存并校验 DH 的 p/g/l 参数。",
        "Diffie-Hellman parameters: stores and validates the p/g/l values.",
    ),
    "secured/CryptoException": (
        "加密异常基类：区分可恢复与不可恢复的加密错误。",
        "Base crypto exception distinguishing recoverable from fatal crypto errors.",
    ),
    "secured/RuntimeCryptoException": (
        "运行时加密异常：无需显式捕获的加密错误。",
        "Unchecked crypto exception for errors that need no explicit catch.",
    ),
    "secured/DataLengthException": (
        "数据长度异常：输入/输出缓冲区长度不满足算法要求。",
        "Data-length exception raised when buffers do not satisfy the algorithm requirements.",
    ),
    "secured/OutputLengthException": (
        "输出长度异常：输出缓冲区空间不足。",
        "Output-length exception raised when the output buffer is too small.",
    ),
    "secured/InvalidCipherTextException": (
        "密文非法异常：解密后填充或校验不通过。",
        "Invalid-ciphertext exception raised when padding or integrity checks fail.",
    ),
    "secured/MaxBytesExceededException": (
        "字节上限异常：单次读取/累积超过允许的最大字节数。",
        "Max-bytes exception raised when a read or accumulation exceeds the allowed limit.",
    ),
    "secured/DecoderException": (
        "解码异常：编解码器内部状态非法。",
        "Decoder exception for illegal codec state.",
    ),
    "secured/ProtocolIOException": (
        "协议 IO 异常：协议层读写失败（对端关闭、帧非法等）。",
        "Protocol IO exception for protocol-level read/write failures (peer closed, bad frame, …).",
    ),

    # ---------------- secured：HID / 触摸 ----------------
    "secured/HidDescriptorParser": (
        "HID 报告描述符解析：识别 Apple 多点触控集合，按物理集合分配 slot 并提取 "
        "X/Y、Tip Switch、Contact ID 等字段。",
        "HID report-descriptor parser: detects Apple multitouch collections, assigns one slot "
        "per physical collection and extracts X/Y, Tip Switch and Contact ID fields.",
    ),
    "secured/HidCapabilities": (
        "HID 能力集合：维护每个 HID 设备的描述符、字段表与报告分发。",
        "HID capability set: keeps per-device descriptors and field tables and dispatches reports.",
    ),
    "secured/HidCommandSender": (
        "HID 命令发送：构造并发送 requestUI / requestSiri / changeModes 等控制报文。",
        "HID command sender: builds and sends requestUI / requestSiri / changeModes control reports.",
    ),
    "secured/HidConsumerParser": (
        "HID Consumer 页解析：处理多媒体按键描述符（播放、音量等）。",
        "HID Consumer-page parser for multimedia key descriptors (playback, volume, …).",
    ),
    "secured/HidBackParser": (
        "HID 按键/返回键解析：处理非触摸的按键描述符与事件映射。",
        "HID button/back-key parser handling non-touch key descriptors and event mapping.",
    ),
    "secured/TouchFlow": (
        "触摸链路统计：累计 down/move/up 与失败次数，用于判定触摸是否真正转发。",
        "Touch pipeline statistics: accumulates down/move/up and failure counters to prove "
        "whether touches are really forwarded.",
    ),
    "secured/TouchGestureEncoder": (
        "触摸手势编码：把 HID 报告转换成 CarLife TOUCH 帧（含坐标缩放）。",
        "Touch gesture encoder: converts HID reports into CarLife TOUCH frames with coordinate scaling.",
    ),

    # ---------------- secured：音视频 ----------------
    "secured/AudioPacer": (
        "音频发送节拍器：按 PCM 时长计算发送时刻，避免一次性灌包。",
        "Audio pacing: derives send deadlines from PCM duration so packets are not burst-injected.",
    ),
    "secured/AudioRelay": (
        "原生音频通道状态记录：首次初始化、首个有效 PCM、视频后等待等关键节点。",
        "Native audio channel state recorder: first init, first valid PCM, post-video wait, …",
    ),
    "secured/AudioCapabilities": (
        "音频能力协商：解析车机上报的 stream type 与 formats 位图，选定采样率/声道。",
        "Audio capability negotiation: parses the head unit's stream types and format bitmaps "
        "to pick sample rate and channels.",
    ),
    "secured/AudioSourceCapability": (
        "音频源能力：描述 media / alert 等音频源的格式与优先级。",
        "Audio source capability describing formats and priorities of media / alert sources.",
    ),
    "secured/AacAudioFormat": (
        "AAC 音频格式：audio/mp4a-latm 的帧参数与 ADTS 头处理。",
        "AAC audio format: frame parameters and ADTS header handling for audio/mp4a-latm.",
    ),
    "secured/PcmQueue": (
        "PCM 环形队列：在采集与发送线程之间缓冲 PCM，队列满时丢弃最旧帧。",
        "PCM ring queue buffering audio between capture and send threads; drops the oldest frame "
        "when full.",
    ),
    "secured/PcmConverter": (
        "PCM 格式转换：重采样到 48 kHz、位深/声道转换并校验输出上限。",
        "PCM format conversion: resampling to 48 kHz, bit-depth/channel conversion and output "
        "limit checks.",
    ),
    "secured/PcmEndpoint": (
        "PCM 端点：描述一路 PCM 输出的 stream usage 与生命周期，拒绝过期端点写入。",
        "PCM endpoint describing one output stream's usage and lifetime, rejecting stale writes.",
    ),
    "secured/PcmAudioOutput": (
        "原生 PCM 输出：按「16 字节 INIT + 4096 字节 DATA」写入车机原生音频通道。",
        "Native PCM output: writes 16-byte INIT plus 4096-byte DATA records to the head-unit "
        "native audio channel.",
    ),
    "secured/MicCapture": (
        "麦克风采集：AudioRecord 采集上行音频并校验字节序与端点状态。",
        "Microphone capture: records uplink audio via AudioRecord and validates byte order "
        "and endpoint state.",
    ),
    "secured/MediaFormat": (
        "媒体帧契约：统一描述视频/音频帧的格式、长度与边界校验。",
        "Media frame contract: uniform description of format, length and bounds for video/audio frames.",
    ),
    "secured/MediaSourceTracker": (
        "媒体源跟踪：记录原生媒体源是否建立，拒绝对过期/错误源的写入。",
        "Media source tracker: records whether a native media source exists and rejects writes "
        "to stale or mismatched sources.",
    ),
    "secured/VideoCipher": (
        "视频解密与拆包：解密视频载荷、按 AVC NAL 边界切分并做长度上限保护。",
        "Video decryption and depacketisation: decrypts payloads, splits AVC NAL units and "
        "enforces size limits.",
    ),
    "secured/VideoStreamNeg": (
        "视频流协商：校验分辨率/帧率/编码契约后建立视频流。",
        "Video stream negotiation: validates resolution, frame rate and codec contract before "
        "starting the stream.",
    ),
    "secured/AvcAccessUnit": (
        "AVC 访问单元：一帧 H.264 的 NAL 列表与边界校验。",
        "AVC access unit: NAL list and bounds checks for one H.264 frame.",
    ),
    "secured/AvcLengthParser": (
        "AVC 长度前缀解析：把 length-prefixed 的 NAL 流转成访问单元。",
        "AVC length-prefix parser converting length-prefixed NAL streams into access units.",
    ),
    "secured/AvcSpsParser": (
        "AVC SPS 解析：从 SPS 中取出分辨率、profile 与 level。",
        "AVC SPS parser extracting resolution, profile and level from the sequence parameter set.",
    ),
    "secured/VideoEncoderStats": (
        "编码器统计：采集帧率、码率与发送用量等运行指标。",
        "Encoder statistics: collects frame rate, bitrate and transmit-usage metrics.",
    ),
    "secured/IoStatsSnapshot": (
        "IO 统计快照：按通道汇总收发字节与错误计数。",
        "IO statistics snapshot aggregating bytes and error counters per channel.",
    ),

    # ---------------- secured：基础设施 ----------------
    "secured/BinaryPlist": (
        "二进制 plist 编解码：解析配对记录（bplist00）并校验对象表边界。",
        "Binary plist codec: parses pairing records (bplist00) and validates the object table bounds.",
    ),
    "secured/ProtobufReader": (
        "Protobuf 读取器：变长整数、长度定界字段与嵌套消息的安全解析。",
        "Protobuf reader: safe parsing of varints, length-delimited fields and nested messages.",
    ),
    "secured/ProtoMessageBase": (
        "Protobuf 消息基类：提供字段缓存、序列化与相等性比较的公共实现。",
        "Protobuf message base class providing field caching, serialization and equality.",
    ),
    "secured/ProtoFieldAccessor": (
        "Protobuf 字段访问器：按字段号读写 UUID、字符串等值。",
        "Protobuf field accessor reading/writing UUID and string values by field number.",
    ),
    "secured/ReleaseCatalog": (
        "发布目录消息：描述可用版本、下载地址与校验值。",
        "Release catalog message describing available versions, download URLs and checksums.",
    ),
    "secured/DeviceTimeRange": (
        "设备时间范围消息：表示设备侧的时间区间与精度。",
        "Device time range message representing a device-side time interval and precision.",
    ),
    "secured/ResourceFingerprintBuilder": (
        "资源指纹构造：按 resourceID / entity 生成稳定的比对指纹。",
        "Resource fingerprint builder producing stable digests from resourceID / entity.",
    ),
    "secured/SessionStateMachine": (
        "会话状态机：定义初始化、信息交换、就绪等状态与合法迁移。",
        "Session state machine defining initial, info-exchange and ready states with legal transitions.",
    ),
    "secured/SessionVerificationTracker": (
        "会话校验跟踪：记录各阶段校验结果并拒绝过期代次的状态。",
        "Session verification tracker recording per-stage verification results and rejecting "
        "stale generations.",
    ),
    "secured/AppleHttpHeaders": (
        "AirPlay HTTP 头常量：X-Apple-* 请求头的名称定义。",
        "AirPlay HTTP header constants defining the X-Apple-* request headers.",
    ),
    "secured/ControlRequestParser": (
        "控制请求解析：解析 HTTP 请求行、头部与 body，供控制通道分发。",
        "Control request parser: parses request line, headers and body for the control channel.",
    ),
    "secured/ByteArrays": (
        "字节数组工具：非空、长度与区间校验，失败时抛出明确异常。",
        "Byte-array helpers for null, length and range validation with explicit exceptions.",
    ),
    "secured/HexCodec": (
        "十六进制编解码：严格校验字符集与长度。",
        "Hex codec with strict character-set and length validation.",
    ),
    "secured/IntegerMath": (
        "整数数学工具：提供溢出安全的上取整与位运算辅助。",
        "Integer math helpers: overflow-safe ceil division and bit operations.",
    ),
    "secured/LittleEndianIntCodec": (
        "小端 32 位整数编解码：协议字段与内存字节序互转。",
        "Little-endian 32-bit integer codec between protocol fields and memory byte order.",
    ),
    "secured/LittleEndianShortCodec": (
        "小端 16 位整数编解码：HID 报告与协议短字段使用。",
        "Little-endian 16-bit integer codec used by HID reports and short protocol fields.",
    ),
    "secured/SyntheticRefs": (
        "编译期引用占位：保证重建代码在缺少原始内部类时仍可编译通过。",
        "Compile-time reference placeholder keeping the reconstructed code compilable without the "
        "original internal classes.",
    ),
    "secured/BluetoothDialogRunnable": (
        "蓝牙开启对话框任务：在 UI 线程弹出系统蓝牙请求。",
        "Bluetooth enable dialog task posting the system Bluetooth request on the UI thread.",
    ),
    "secured/BluetoothSaveListener": (
        "蓝牙名称保存回调：处理「保存设备名」对话框的确认动作。",
        "Bluetooth name save listener handling confirmation of the rename dialog.",
    ),

    # ---------------- secured：内部运算与任务（混淆名） ----------------
    "secured/l7": (
        "椭圆曲线群运算内部实现：点加/倍点与标量乘，供 Ed25519 使用。",
        "Elliptic-curve group arithmetic internals (point add/double and scalar multiplication) "
        "used by Ed25519.",
    ),
    "secured/k4": (
        "有限域算术内部实现：模加减与归约，供曲线运算调用。",
        "Finite-field arithmetic internals (modular add/sub and reduction) used by curve operations.",
    ),
    "secured/m4": (
        "有限域元素比较与批量转换：曲线运算的辅助例程。",
        "Field-element comparison and batch conversion helpers for curve operations.",
    ),
    "secured/d6": (
        "有限域标量运算：位长计算与多精度乘法辅助。",
        "Field-scalar arithmetic: bit-length computation and multi-precision multiply helpers.",
    ),
    "secured/c6": (
        "曲线参数与编码：字段元素打包/解包及合法性校验。",
        "Curve parameters and encoding: field-element pack/unpack with validity checks.",
    ),
    "secured/t1": (
        "协议命令号常量表：0x180xx / 0x100xx 命令与响应的取值定义。",
        "Protocol command-ID table defining the 0x180xx / 0x100xx commands and responses.",
    ),
    "secured/l5": (
        "验证结果接口：返回 VERIFIED / REJECTED / CONTRACT_UNAVAILABLE 三态。",
        "Verification-result interface returning VERIFIED / REJECTED / CONTRACT_UNAVAILABLE.",
    ),
    "secured/k5": (
        "验证结果的默认实现：恒返回 VERIFIED。",
        "Default verification result implementation that always reports VERIFIED.",
    ),
    "secured/w4": (
        "原生 AVC 单元解析：把原生视频通道回传的单元拆成可校验的 AVC 帧。",
        "Native AVC unit parsing: splits units coming back from the native video channel into "
        "verifiable AVC frames.",
    ),
    "secured/t2": (
        "连接失败日志：格式化 CONNECT_FAILURE 的 phase 与原因。",
        "Connection-failure logging: formats CONNECT_FAILURE phase and reason.",
    ),
    "secured/t5": (
        "接收端配置消息：保存接收者 UUID、实验通道开关等 protobuf 字段。",
        "Receiver configuration message storing receiver UUID and experimental-channel flags.",
    ),
    "secured/u4": (
        "版本兼容判定：比较协议/固件版本决定可用的特性开关。",
        "Version compatibility checks deciding which feature flags are available.",
    ),
    "secured/p5": (
        "键值映射辅助：维护有序映射并提供带默认值的查询。",
        "Key-value mapping helper offering ordered iteration and defaulted lookups.",
    ),

    # ---------------- prototype ----------------
    "prototype/AirplayServiceProbe": (
        "车机服务发现：通过 mDNS 发现 _airplay._tcp 并解析候选地址，供后续建流使用。",
        "Head-unit service discovery over mDNS (_airplay._tcp) resolving candidate addresses "
        "for stream setup.",
    ),
    "prototype/CarPlayCtrlAdvertiser": (
        "控制服务广播：注册 _carplay-ctrl._tcp，让车机发现本机并发起连接。",
        "Control service advertiser registering _carplay-ctrl._tcp so the head unit can find "
        "and connect to this phone.",
    ),
    "prototype/WirelessCarPlayService": (
        "无线 CarPlay 前台服务：编排蓝牙、WiFi、会话与界面的完整生命周期。",
        "Wireless CarPlay foreground service orchestrating Bluetooth, WiFi, sessions and UI "
        "through the whole lifecycle.",
    ),
    "prototype/MainActivity": (
        "主界面：权限申请、服务启停、状态展示与用户操作入口。",
        "Main screen: permission requests, service start/stop, status display and user actions.",
    ),
    "prototype/HelpActivity": (
        "帮助页：展示连接步骤与常见问题说明。",
        "Help screen showing connection steps and FAQ.",
    ),
    "prototype/LogActivity": (
        "日志页：查看、筛选并导出应用内 connection-log 历史。",
        "Log screen: view, filter and export the in-app connection-log history.",
    ),
    "prototype/AppLogger": (
        "应用内日志：写入 files/connection-log-N.txt，按大小轮转并保留历史。",
        "In-app logger writing files/connection-log-N.txt with size-based rotation and history.",
    ),
    "prototype/CarPlusApplication": (
        "Application 入口：注册全局异常兜底、初始化偏好设置与进程状态。",
        "Application entry point registering the global crash handler and initialising "
        "preferences and process state.",
    ),
    "prototype/CarLifeBridge": (
        "原生组件桥接：启动 Car+ / CarLife 投屏界面、查询安装状态并传递通道句柄。",
        "Native bridge starting the Car+ / CarLife projection UI, querying installation state "
        "and handing over channel handles.",
    ),
    "prototype/AvcEncoder": (
        "H.264 硬编码封装：配置 MediaCodec、投递屏幕帧并输出 AVC 访问单元。",
        "H.264 hardware encoder wrapper: configures MediaCodec, submits screen frames and emits "
        "AVC access units.",
    ),
    "prototype/PairingSession": (
        "配对编排：驱动蓝牙配对、iAP2 握手与 WiFi 凭据下发的完整流程。",
        "Pairing orchestration driving Bluetooth pairing, the iAP2 handshake and WiFi credential "
        "delivery.",
    ),
    "prototype/PairingPreparationService": (
        "配对准备服务：响应 PREPARE_PAIRING / STOP_PAIRING 指令，预热蓝牙与网络。",
        "Pairing preparation service responding to PREPARE_PAIRING / STOP_PAIRING and warming up "
        "Bluetooth and network.",
    ),
    "prototype/WifiNetworkMonitor": (
        "WiFi 状态监听：跟踪关联结果、网络切换与重连触发。",
        "WiFi monitor tracking association results, network switches and reconnect triggers.",
    ),
    "prototype/NetworkSelector": (
        "网络选择：挑选并绑定当前可用的 WiFi/蜂窝网络。",
        "Network selection binding the currently usable WiFi/cellular network.",
    ),
    "prototype/NetworkTransportDescriber": (
        "传输类型描述：把 NetworkCapabilities 翻译成 WIFI / CELLULAR / UNKNOWN。",
        "Transport describer mapping NetworkCapabilities to WIFI / CELLULAR / UNKNOWN.",
    ),
    "prototype/BluetoothAdapterResolver": (
        "蓝牙适配器解析：获取适配器与本机蓝牙地址（无地址时按约定兜底）。",
        "Bluetooth adapter resolver obtaining the adapter and local address with a documented "
        "fallback.",
    ),
    "prototype/BluetoothRfcommProfiles": (
        "RFCOMM profile 管理：枚举并建立 iAP2 所需的串口 profile。",
        "RFCOMM profile management enumerating and opening the serial profiles iAP2 needs.",
    ),
    "prototype/UpdateChecker": (
        "更新检查：拉取发布信息、校验 SHA-256 与包大小后下载 APK。",
        "Update checker fetching release info, verifying SHA-256 and size, then downloading the APK.",
    ),
    "prototype/ReleaseCheckTask": (
        "版本检查任务：在后台线程完成发布比对与签名校验。",
        "Release check task performing release comparison and signature verification off the UI thread.",
    ),
    "prototype/InstalledPackageIdentity": (
        "已安装包身份：读取包名、版本与签名信息用于兼容性判断。",
        "Installed package identity reading package name, version and signature for compatibility "
        "decisions.",
    ),
    "prototype/StatusText": (
        "状态文案：把内部状态码翻译成界面可读的中英文提示。",
        "Status text mapping internal state codes to readable UI strings.",
    ),
    "prototype/UiFactory": (
        "控件工厂：统一构造按钮、对话框、列表等界面元素与样式。",
        "Widget factory building buttons, dialogs and lists with consistent styling.",
    ),
    "prototype/DonationDialog": (
        "赞助对话框：展示自愿赞助入口，不影响任何功能。",
        "Donation dialog offering optional sponsorship without affecting features.",
    ),

    # ---------------- org/bouncycastle ----------------
    "org/bouncycastle/crypto/CipherParameters": (
        "密码算法参数标记接口：所有密钥/参数对象的公共父接口。",
        "Cipher-parameters marker interface shared by all key and parameter objects.",
    ),
    "org/bouncycastle/crypto/modes/ChaCha20Poly1305": (
        "ChaCha20-Poly1305 AEAD：加密并生成认证标签，解密时校验标签。",
        "ChaCha20-Poly1305 AEAD: encrypts with an authentication tag and verifies it on decrypt.",
    ),
    "org/bouncycastle/crypto/params/AEADParameters": (
        "AEAD 参数：承载密钥、nonce 与关联数据。",
        "AEAD parameters carrying key, nonce and associated data.",
    ),
    "org/bouncycastle/crypto/params/KeyParameter": (
        "对称密钥参数：包装原始密钥字节。",
        "Symmetric key parameter wrapping raw key bytes.",
    ),
    "org/bouncycastle/crypto/params/Ed25519PrivateKeyParameters": (
        "Ed25519 私钥参数：保存种子/私钥并可导出公钥。",
        "Ed25519 private key parameters holding the seed and deriving the public key.",
    ),
    "org/bouncycastle/crypto/params/Ed25519PublicKeyParameters": (
        "Ed25519 公钥参数：校验并保存公钥编码。",
        "Ed25519 public key parameters validating and storing the encoded public key.",
    ),
    "org/bouncycastle/crypto/params/X25519PrivateKeyParameters": (
        "X25519 私钥参数：保存标量并派生公钥。",
        "X25519 private key parameters storing the scalar and deriving the public key.",
    ),
    "org/bouncycastle/crypto/params/X25519PublicKeyParameters": (
        "X25519 公钥参数：保存对端公钥编码。",
        "X25519 public key parameters storing the peer public key encoding.",
    ),
    "org/bouncycastle/crypto/signers/Ed25519Signer": (
        "Ed25519 签名器：生成与验证签名。",
        "Ed25519 signer producing and verifying signatures.",
    ),
    "org/bouncycastle/crypto/signers/a": (
        "Ed25519 双倍点运算辅助：签名/验签过程中的点运算。",
        "Ed25519 point-doubling helper used during signing and verification.",
    ),
}

# 嵌套类专项描述 / curated nested-type descriptions (key = "pkg/Outer.Inner")
NESTED_CURATED: dict[str, tuple[str, str]] = {
    "secured/AirPlaySession.t4": (
        "原生 CMD 通道读循环：读取车机帧、解密并按命令号分发。",
        "Native CMD channel read loop: reads head-unit frames, decrypts them and dispatches by "
        "command ID.",
    ),
    "secured/DisplaySession.c": (
        "显示会话回调接口：接收原生通道的帧与错误事件。",
        "Display-session callback interface receiving native-channel frames and errors.",
    ),
}

# --------------------------------------------------------------------------
# 结构化推断 / Structural inference
# --------------------------------------------------------------------------
JAVA_NOISE = {
    "String", "Object", "List", "Map", "Set", "AtomicInteger", "AtomicBoolean",
    "AtomicLong", "SharedPreferences", "byte", "int", "long", "short", "boolean",
    "float", "double", "char", "Exception", "Throwable",
}

SIMPLE_ZH = {
    "Runnable": ("后台任务：在独立线程中执行一次异步动作。", "Background task executing one asynchronous step on its own thread."),
    "Predicate": ("过滤谓词：按条件筛选候选对象。", "Predicate filtering candidate objects by a condition."),
    "Consumer": ("消费回调：处理上游产生的事件。", "Consumer callback handling an upstream event."),
    "BiConsumer": ("双参消费回调：处理键值/对象对。", "Bi-consumer callback handling a key/value or object pair."),
    "Function": ("转换函数：把一个对象映射为另一个对象。", "Function mapping one object to another."),
    "BiFunction": ("双参转换函数。", "Bi-function mapping two inputs to one result."),
    "Supplier": ("惰性取值：按需提供对象。", "Supplier providing a value on demand."),
    "LongSupplier": ("惰性时间戳/计数提供者。", "Supplier providing a timestamp or counter on demand."),
    "BooleanSupplier": ("条件提供者：返回布尔状态。", "Boolean supplier returning a condition."),
    "IntPredicate": ("整型过滤谓词。", "Integer predicate."),
    "IntConsumer": ("整型消费回调。", "Integer consumer callback."),
    "IntFunction": ("整型转换函数。", "Integer function."),
    "ToLongFunction": ("对象到 long 的取值函数。", "Function extracting a long from an object."),
    "ToIntFunction": ("对象到 int 的取值函数。", "Function extracting an int from an object."),
    "Comparator": ("比较器：定义候选对象的排序规则。", "Comparator defining the ordering of candidates."),
    "ThreadFactory": ("线程工厂：统一命名并创建后台线程。", "Thread factory naming and creating background threads."),
    "Executor": ("执行器：把任务提交到指定线程/线程池。", "Executor submitting tasks to a thread or pool."),
    "AutoCloseable": ("可关闭资源：持有需要显式释放的通道或缓冲区。", "Closeable resource holding a channel or buffer that must be released."),
}

UI_ZH = ("界面事件监听器：处理该界面上的用户交互。", "UI listener handling user interaction on this screen.")


def _target(fields: list[str]) -> str:
    for f in fields:
        if f and f[0].isupper() and f not in JAVA_NOISE:
            return f
    return ""


def infer(rel: str, kind: str, ext: str, impl: str, fields: list[str], nested_in: str = "") -> tuple[str, str]:
    """按结构特征推断用途 / infer purpose from structural signals."""
    tgt = _target(fields)
    if kind == "enum":
        return ("枚举：本子系统使用的状态/类型取值集合。",
                "Enumeration: state/type values used by this subsystem.")
    if kind == "interface":
        if "ProtoMessageBase" in ext:
            return ("消息接口：定义 protobuf 消息的读写契约。",
                    "Message interface defining the protobuf read/write contract.")
        return ("回调接口：定义单一职责的调用契约，由调用方实现。",
                "Callback interface defining a single-responsibility contract implemented by callers.")
    if "NsdManager.DiscoveryListener" in impl:
        return ("mDNS 服务发现回调：接收车机服务的发现与丢失事件。",
                "mDNS discovery callback receiving head-unit service found/lost events.")
    if "NsdManager.ResolveListener" in impl:
        return ("mDNS 解析回调：拿到服务地址后继续建链。",
                "mDNS resolve callback continuing link setup once the address is known.")
    if "View." in impl:
        return UI_ZH
    if "DialogInterface." in impl:
        return ("对话框回调：处理弹窗的点击/显示/关闭事件。",
                "Dialog callback handling click/show/dismiss events.")
    if "CompoundButton." in impl:
        return ("开关控件回调：处理勾选状态变化。", "Checkbox callback handling checked-state changes.")
    if "RadioGroup." in impl:
        return ("单选组回调：处理选项切换。", "Radio-group callback handling option changes.")
    if "Thread.UncaughtExceptionHandler" in impl:
        return ("线程异常兜底：记录并处理未捕获异常。",
                "Thread uncaught-exception handler recording and containing crashes.")
    if "ProtoMessageBase" in ext:
        return ("protobuf 消息：承载一组协议字段并负责序列化。",
                "Protobuf message carrying a group of protocol fields and its serialization.")
    if "ChaCha20" in ext:
        return ("ChaCha20 变体：调整 nonce/计数器长度以匹配协议要求。",
                "ChaCha20 variant adjusting nonce/counter length to the protocol requirement.")
    if "ThreadFactory" in impl:
        return SIMPLE_ZH["ThreadFactory"]
    for key, pair in SIMPLE_ZH.items():
        if key in impl:
            if tgt:
                return (f"{pair[0]}（服务于 {tgt}）", f"{pair[1]} (serves {tgt}.)")
            return pair
    if nested_in:
        if impl:
            first = impl.split(",")[0].strip()
            if "." in first:
                return (f"回调实现：实现 {first} 定义的契约。",
                        f"Callback implementation of the {first} contract.")
            if first:
                return (f"接口实现：实现 {first} 定义的契约。",
                        f"Implementation of the {first} interface.")
        if ext and ext not in ("Object",):
            return (f"扩展实现：在 {ext} 基础上补充 {nested_in} 需要的局部行为。",
                    f"Extension of {ext} adding the local behaviour {nested_in} needs.")
        if tgt:
            return (f"{nested_in} 的内部辅助类：围绕 {tgt} 组织状态与操作。",
                    f"Helper nested in {nested_in}, grouping state and operations around {tgt}.")
        return (f"{nested_in} 的内部辅助类：承担该类的局部实现细节。",
                f"Helper nested in {nested_in}, carrying local implementation detail.")
    if tgt:
        return (f"内部实现类：围绕 {tgt} 组织状态与操作，服务于本子系统。",
                f"Internal implementation class grouping state and operations around {tgt}.")
    return ("内部实现类：本子系统使用的数据结构与辅助方法。",
            "Internal implementation class holding data structures and helpers for this subsystem.")


# --------------------------------------------------------------------------
# 插入逻辑 / Insertion
# --------------------------------------------------------------------------
TYPE_RE = re.compile(
    r"^(?P<indent>[ \t]*)(?:(?:public|private|protected|static|final|abstract|strictfp)\s+)*"
    r"(?P<kind>class|interface|enum)\s+(?P<name>[A-Za-z_$][\w$]*)"
)
FIELD_RE = re.compile(
    r"^\s*(?:public|private|protected)?\s*(?:static)?\s*(?:final)?\s*"
    r"([A-Z][\w.]*(?:<[^>]*>)?)\s+\w+\s*[;=]"
)


def header_lines(zh: str, en: str, scope: str) -> list[str]:
    out = []
    for ln in HEADER:
        if "{zh}" in ln:
            out.append(ln.format(zh=zh))
        elif "{en}" in ln:
            out.append(ln.format(en=en))
        elif "{scope}" in ln:
            out.append(ln.format(scope=scope))
        else:
            out.append(ln)
    return out


def scope_of(pkg_dir: str) -> str:
    for prefix in sorted(PKG_ZH, key=len, reverse=True):
        if pkg_dir == prefix or pkg_dir.startswith(prefix + "/"):
            return f"{PKG_ZH[prefix]} / {PKG_EN[prefix]}"
    return "CarplayAPK 重建工程 / CarplayAPK reconstruction project"


def doc_lines(indent: str, zh: str, en: str) -> list[str]:
    return [f"{indent}/**", f"{indent} * {zh}", f"{indent} *", f"{indent} * {en}", f"{indent} */"]


def process(path: Path, root: Path) -> tuple[bool, str]:
    raw = path.read_bytes()
    text = raw.decode("utf-8")
    newline = "\r\n" if "\r\n" in text else "\n"
    lines = text.split(newline)

    rel = path.relative_to(root).with_suffix("").as_posix()
    pkg_dir = rel.rsplit("/", 1)[0] if "/" in rel else ""
    stem = rel.rsplit("/", 1)[-1]
    # 描述库按「类路径」索引 / description keys are package-relative
    key = rel[len("io/carplay/app/"):] if rel.startswith("io/carplay/app/") else rel

    # 结构画像：顶层类型、继承/实现、字段类型 / structural profile
    kind = ext = impl = top_name = ""
    fields: list[str] = []
    for ln in lines[:400]:
        m = TYPE_RE.match(ln)
        if m and not m.group("indent"):
            kind = m.group("kind")
            top_name = m.group("name")
            seg = ln[m.end():]
            if "extends" in seg:
                ext = seg.split("extends", 1)[1].split("implements", 1)[0].strip(" {")
            if "implements" in seg:
                impl = seg.split("implements", 1)[1].strip(" {")
            break
    for ln in lines:
        fm = FIELD_RE.match(ln)
        if fm:
            fields.append(fm.group(1).split(".")[-1].split("<")[0])

    # 文件级描述 / file-level description
    if key in CURATED:
        zh, en = CURATED[key]
    else:
        zh, en = infer(rel, kind, ext, impl, fields)
    zh_h = f"本文件定义 {top_name or stem}。{zh}" if top_name else zh
    en_h = f"This file defines {top_name or stem}. {en}" if top_name else en

    out: list[str] = []
    added_header = False
    if SPDX_MARK not in text:
        out.extend(header_lines(zh_h, en_h, scope_of(pkg_dir)))
        out.append("")
        added_header = True

    i = 0
    added_docs = 0
    n = len(lines)
    while i < n:
        ln = lines[i]
        m = TYPE_RE.match(ln)
        if m:
            indent = m.group("indent")
            # 已经紧跟 javadoc 则跳过 / skip if already documented
            prev = i - 1
            while prev >= 0 and not lines[prev].strip():
                prev -= 1
            documented = prev >= 0 and lines[prev].strip().endswith("*/")
            if not documented:
                name = m.group("name")
                qual_rel = f"{key}.{name}" if indent else key
                if qual_rel in NESTED_CURATED:
                    zh_c, en_c = NESTED_CURATED[qual_rel]
                elif indent:
                    seg = ln[m.end():]
                    czh, cen = infer(rel, m.group("kind"),
                                     seg.split("extends", 1)[1].strip(" {") if "extends" in seg else "",
                                     seg.split("implements", 1)[1].strip(" {") if "implements" in seg else "",
                                     [], nested_in=stem)
                    zh_c, en_c = czh, cen
                else:
                    zh_c, en_c = zh, en
                out.extend(doc_lines(indent, zh_c, en_c))
                added_docs += 1
        out.append(ln)
        i += 1

    if not added_header and added_docs == 0:
        return False, rel
    path.write_bytes(newline.join(out).encode("utf-8"))
    return True, rel


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", default=str(Path(__file__).resolve().parent.parent
                                         / "app/src/main/java"))
    ap.add_argument("--check", action="store_true", help="只报告未标注文件 / report only")
    args = ap.parse_args()
    root = Path(args.src)
    files = sorted(root.rglob("*.java"))
    changed = []
    for f in files:
        if args.check:
            t = f.read_text(encoding="utf-8", errors="replace")
            if SPDX_MARK not in t:
                changed.append(f.name)
            continue
        ok, rel = process(f, root)
        if ok:
            changed.append(rel)
    print(f"files={len(files)} changed={len(changed)}")
    for c in changed[:20]:
        print("  -", c)
    return 0


if __name__ == "__main__":
    sys.exit(main())
