/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 ControlChannel。控制通道：接收车机邀请、完成 iAP2 控制握手并维护控制连接状态。
 *   This file defines ControlChannel. Control channel: accepts head-unit invitations, performs the iAP2 control handshake and maintains control-connection state.
 *
 * 所属子系统 / Subsystem:
 *   协议栈核心层（AirPlay / iAP2 / HID / 加解密 / 媒体通道） / protocol core (AirPlay / iAP2 / HID / crypto / media channels)
 */

package io.carplay.app.secured;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import java.net.DatagramPacket;
import java.util.ArrayList;




/**
 * 控制通道：接收车机邀请、完成 iAP2 控制握手并维护控制连接状态。
 *
 * Control channel: accepts head-unit invitations, performs the iAP2 control handshake and maintains control-connection state.
 */
public final class ControlChannel {
    public static Context APP_CTX;
    /**
     * [V0.0.1] 调试模式（设置页「调试模式」，默认关闭）。
     * 关闭时 ControlChannel 的所有 logcat / 诊断输出一律为空操作 —— 发布包不再预埋 logcat。
     * 由 MainActivity.onCreate 从 prefs 恢复，并由设置页开关实时更新。
     */
    private static volatile boolean DEBUG_MODE = false;

    public static void setDebugMode(boolean on) {
        DEBUG_MODE = on;
    }

    public static boolean DebugModeEnabled() {
        return DEBUG_MODE;
    }

    public static volatile boolean BOOTED;
    public static volatile boolean CTRL_TCP;
    public static volatile boolean EVENT_READY;
    public static volatile boolean GATECLOSED;
    public static volatile boolean INVITED;
    public static volatile long LAST_FEEDBACK_AT;
    public static volatile long LASTLOG;
    public static volatile int LASTMODE;
    public static volatile AirPlaySession LASTOBJ;
    public static volatile long LASTSEND;
    public static volatile boolean LOGGED;
    public static volatile int MODE;
    public static volatile int P0CNT;
    public static volatile long P0SEC;
    public static volatile ArrayList PEND;
    public static volatile boolean HID_MODE_REPORTED;
    public static volatile int RESPCNT;
    public static volatile int SENTCNT;
    public static volatile int TMCNT;
    public static volatile boolean ACCEPTED = false;
    public static volatile boolean EVENT_OK = true;
    public static volatile int EVFAIL = 0;
    public static volatile long LASTAT = 0;
    public static volatile boolean LASTEVENT = false;
    public static volatile boolean PENDING = false;
    public static volatile int TRIES = 0;
    public static volatile boolean CTRLOK = false;
    /** 车机经 _carplay-ctrl 下发 connect 时携带的 AirPlay-Receiver-Device-ID（不写日志）。 */
    public static volatile String CTRL_RECEIVER_ID;
    /** 最近一次车机 connect 的时间（elapsedRealtime）。 */
    public static volatile long CTRL_INVITATION_AT;
    /** 身份链闭合：本机蓝牙标识与车机 receiver id 都就位；判据落在对端行为上。 */
    public static volatile boolean CTRL_IDENTITY_BOUND;

    /**
     * 本机蓝牙标识：与 _carplay-ctrl 的 TXT id/deviceid、iAP2 CarPlay availability 同源。
     * DeviceMac() 取系统/手动地址，再用 n.a() 统一校验为合法单播 MAC。
     * Local Bluetooth identifier shared by mDNS TXT and iAP2 availability.
     */
    public static String LocalBluetoothIdentifier() {
        return n.a(DeviceMac());
    }

    /**
     * [V1.8.53][B] HID 强制武装：/info 的 HID 能力解析完成后重发一次 hidSetInputMode。
     *
     * 根因（A 级，V1.8.43 logcat 时序）：
     *   08:58:36.860 HID_GATE_OPEN cnt=1 mode=0 devices=0  ← 门打开时 I0/J0/K0 还是空的
     *   08:58:36.860 HID_PEND_REPLAY total=0 pend=-1        ← 于是 0 条 hidSetInputMode 入队
     *   08:58:36.860 HID_GATE: <<< SKIP send
     *   08:58:36.882 HID_CAPABILITIES devices=3 touch=1     ← 能力此时才解析出来
     *   之后 HidOK()（车机对空命令也回 200）把扫掠停掉 ⇒ 永不重发。
     * 社区对照（cardeck 触摸链五连破）：hidSetInputMode（uuid 原样字符串）武装 HID 后车机才开始发触摸。
     * 默认 true；置 false 回退 V1.8.52 行为。
     */
    public static boolean CpHidForceArmEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("cp_b_hid_force_arm", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.53][C] 未激活保护：RECORD 后 N 秒内若车机没有下发任何 /command（EVTCP :5001），
     * 主动断开 AirPlay 会话，避免长时间黑屏挂着（历史上表现为「强连 7000 ⇒ 车机触摸无反应」）。
     * 0 = 关闭保护。默认 45s。
     */
    public static int CpGuardSec() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return 45;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getInt("cp_c_guard_sec", 45);
        } catch (Throwable ignored) {
            return 45;
        }
    }

    /** [V1.8.53][C] 入站 /command 计数（车机是否真的把本机当投影源）。 */
    public static final java.util.concurrent.atomic.AtomicInteger CP_INBOUND_COMMANDS =
            new java.util.concurrent.atomic.AtomicInteger();

    /**
     * [V1.8.57][H] 只给「有效触摸设备」发 hidSetInputMode。
     *
     * 依据（A 级真机 18:30）：
     *   `/info` 的 hidDevices 有 3 个（uuid=3 触摸 / uuid=2 描述符解析失败 / uuid=4 back），
     *   我方 `HID_TOUCH_UNSUPPORTED index=1 reason=DESCRIPTOR` 已记录 uuid=2 无效，
     *   但 `z0()` 仍给它和下发给 back 的 uuid=4 各发了一条 hidSetInputMode（共 3 条）。
     * 假设：车机对「无效 uuid 的武装请求」可能整体判失败，或按最后一次覆盖，
     *       ⇒ 只发触摸设备（uuid=3）时才有可能被真正武装。
     * true = 只发 I0（touch 桶）；false = 现状（I0+J0+K0+PEND）。默认 false 保持现状。
     */
    public static boolean CpHidTouchOnlyEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("cp_h_hid_touch_only", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** [V1.8.56][G] 入站 iAPSendMessage 计数（车机是否主动发起 iAP2 虚拟链路）。 */
    public static final java.util.concurrent.atomic.AtomicInteger CP_G_INBOUND_IAPSEND =
            new java.util.concurrent.atomic.AtomicInteger();

    /**
     * [V1.8.56][G] iAP2-over-WiFi 被动模式。
     *
     * 背景（A 级真机 18:16）：P1-2 建好路由后我方主动发 SYN 6 次，每次都走
     * `/command type=iAPSendMessage` 且车机回 HTTP 200（`CARPLAY_RESPONSE` 非 200 计数 = 0），
     * 但 iAP2 应用层 `rx_bytes=0 / rx_frames=0 / last_ack=NONE`，6 秒后 `IAP2_ACK_TIMEOUT_SYN`。
     *
     * 社区（cardeck）：事件通道 iAPSendMessage 承载**车机发起**的虚拟 iAP2 链路
     * （marker=DETECT 一问一答，车机是客户端发 SYN；服务器重发 DETECT 会把它打回 Detecting）。
     *
     * 因此本开关用于区分两个分支：
     *   true = 被动模式：只建路由、不主动发 SYN，观察车机是否自己发起
     *          （判据 `CP_G_INBOUND_IAPSEND` >0 ⇒ 方向问题可修；=0 ⇒ 车机侧未开 connect 模块）
     *   false = 现状（V1.8.55 行为，主动发 SYN）。默认 false，避免默认行为变化。
     */
    public static boolean CpIap2PassiveEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("cp_g_iap2_passive", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.58][I] iAP2-over-WiFi 端点监听开关。
     *
     * 依据（A 级固件，`libNmeTransport.so`）：
     *   `CreateNmeIAPWifiTransport` 先从 metapool 取名为 `wifi` 的 tag，取不到即
     *   `CreateNmeIAPWifiTransport: no wifi tag` 失败；随后由
     *   `NmeWifiTransportClient::wait_for_connect()/recv()/send()` 主动连向手机。
     * ⇒ 车机是**客户端**，手机必须先监听，它才可能连上来。
     *
     * 默认 **false**（V1.8.59 起）。原因（见 `v1859-p1-audit.md`）：审计发现
     * **车机从不主动连手机** —— 真 iPhone 侧所有 CarPlay 连接都是手机→车机
     * （7000 / 5020 / 5001），社区逆向记录同样写明"车机 browse `_carplay-ctrl`
     * 但从不打开 TCP 7100"。所以这个监听**不是**修复路径，只是诊断口子。
     * 打开：carplayapk-debug / cp_i_wifi_iap_listen = true。
     */
    public static boolean CpWifiIapListenEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("cp_i_wifi_iap_listen", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** [V1.8.58][I] 监听端口，默认 52800。 */
    public static int CpWifiIapPort() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return 52800;
        }
        try {
            int value = ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getInt("cp_i_wifi_iap_port", 52800);
            return (value > 0 && value < 65536) ? value : 52800;
        } catch (Throwable ignored) {
            return 52800;
        }
    }

    /**
     * [V1.8.58][I] 收到连接后回一次的固定字节（十六进制字符串，空 = 不回）。
     *
     * 之所以做成可配置而不是写死：车机链路帧格式尚未取证，盲发曾导致
     * `IAP2_PEER_RESTARTED`。留这个口子，是为了拿到 `CP_I_RX` 的原始字节后
     * 能不重新出包就试回包。
     */
    public static String CpWifiIapReplyHex() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return "";
        }
        try {
            String value = ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getString("cp_i_syn_reply_hex", "");
            return value == null ? "" : value.trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    /**
     * [V1.8.54][D] requestUI 携带 uuid。
     *
     * 依据（A 级固件）：`libNmeCarPlay.so` 有
     *   `OnSessionRequestUI(const char* uuid, const char* url): %s, %s`
     *   `OnSessionRequestUI(const char* url) callback enter`
     * 说明车机的 UI 请求回调**带 uuid**（要指定在哪块屏/哪个显示单元上起 UI）。
     * 我方 V1.8.53 只发 `params:{url:""}`，既无 uuid 也无实质 url。
     * 默认 true：带上主显示 uuid（`/info` displays[0].uuid）。
     * 置 false 回退 V1.8.53 的 `{url:""}`。
     */
    public static boolean CpRequestUiUuidEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("cp_b_request_ui_uuid", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.32] 反向通道调试总开关（P0-1 / P0-2 / P0-3 共用）。
     * 默认 false：行为与 V1.8.31 完全一致（主动连车机 7000，等邀请仅作兜底）。
     * 置 true：
     *   P0-1 让「等车机 ctrl 回连」成为主路径（先播 ctrl、等 GET /ctrl-int/1/connect，超时再主动连）；
     *   P0-2 在 ctrl 注册后打印本地 TXT 属性自证（CTRL_RESOLVE_TXT）；
     *   P0-3 打印 ManualName 与系统蓝牙名的一致性（CTRL_IDENTITY_MATCH）。
     * 通过 adb 广播切换，可一键回滚，不影响生产路径。
     */
    public static boolean CtrlWaitInvitationEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("ctrl_wait_invitation", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.33] P1-1 蓝牙 iAP2 登记触发诊断总开关。
     * 默认 false：行为与 V1.8.32 完全一致，不打印任何 iAP2 握手细节。
     * 置 true：在 PairingSession / MfiCertificateVerifier / WirelessCarPlayService 的
     * RFCOMM 监听、iAP2 识别、IAP2_IDENTIFIED、IAP2_PEER_CAPABILITIES、IAP2_PEER_RESTARTED
     * 等关键节点打印 IAP2_DIAG_* 日志，用于在真车上定位握手断在哪一步
     * （对照固件 IosControlClientCallback/AddCarPlayClient 的 +0x9e0 列表是否被填充）。
     */
    public static boolean Iap2DiagEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_diag", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.33] P1-1 的 EA 能力列表变体开关：去掉 19982（transport_notification）。
     * 默认 false：沿用 V1.8.32 的 toSend={19977..19982,22274,59904,59905}。
     * 置 true：从 EA 的 to-send / to-receive 列表移除 19982，验证 V1.7.67 旧疑点
     * （19982 触发车机 IAP2_PEER_RESTARTED 重启 iAP2 引导、使登记永远走不完）是否成立。
     */
    public static boolean Iap2EaDrop19982() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ea_drop_19982", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.41] 补齐 iAP2 EA（External Accessory）设备登记：主动发起 EA 协议会话。
     * 默认 true（本次目标就是补齐门控第 1 层，关闭会让本版退化为 V1.8.40 行为）。
     * 背景（libNmeCarPlay 反汇编）：
     *   MediaManagerService 只有在 EA 会话建立后才会写 addEAPMetaEntries
     *   （CINEMO_METANAME_IAP_EAP_APP_SUPPORTS_CARPLAY），并置 Wireless Carplay Device connected；
     *   该登记是 GMCarPlay/MediaManagerService 认可「CarPlay 已连接」的唯一上游。
     * 做法：IAP2_IDENTIFIED 之后向车机发 0xEA00（59904 StartExternalAccessoryProtocolSession），
     *   param 0 = 协议名，逐个尝试 com.buick.vip / com.buick.noprotocol / com.gm.noprotocol / com.gm.vip；
     *   收到 0xEA00 应答（status=0）即视为登记完成，后续 0xEA02 数据走透传并计数。
     */
    public static boolean Iap2EaSessionEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ea_session", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.42] Identification(0x1D00) 是否声明 4 条 param 10 EAP 协议条目。
     * 默认 true。勘误：V1.8.41 把协议串写进了 param 9，而规范里
     * param 9 = maximum_current_drawn_from_device (Uint16, mA)，
     * param 10 = supported_external_accessory_protocol (List, 重复 TLV)。
     * 依据 wiomoc/iap2 identification.py，且被 Info3.6 车机 0x1D01 的 10=32x4 实证。
     * 置 false 即回到「0x1D00 不带任何 EAP 条目」的 V1.8.40 行为，便于 A/B。
     */
    public static boolean Iap2EaParam10Enabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ea_param10", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.48] A/B1：0xEA00 请求 param 0 的编码方式。
     * 默认 true = Uint8 ProtocolIndex（EA_PROTO_INDEX[proto]，com.buick.vip=1），
     * 依据 sysdiagnose 的车机 EAP 字典（ACCExternalAccessoryProtocolIndex=1）+ 结构化条目推断。
     * 置 false = V1.8.47 旧行为（param 0 = NUL 结尾协议名字符串 Iap2Csm.A(proto)）。
     * 判据：应用内 IAP2_EA_SESSION_RESP status=0 + V1848_EA_SESSION_ESTABLISHED。
     */
    public static boolean Iap2Ea00IndexEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ea00_index", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.48] P1：CarPlayAvailability(0x4300) 的本机蓝牙标识是否与 Identification 同源。
     * 默认 true = ControlChannel.LocalBluetoothIdentifier()（手动填写 MAC，与 0x1D00 param 3/17 同源；
     *   LocalBluetoothIdentifier() 内部已做 n.a() 合法性校验，直接返回大写合法 MAC）。
     * 置 false = V1.8.47 旧行为（n.a(this.a.l())，经 BluetoothAdapterResolver.a(ctx, remoteAddr)，
     *   当手动地址为空或等于远端地址时返回 null ⇒ availability_identifier_present=false）。
     * 判据：应用内 IAP2_CARPLAY_AVAILABILITY_PLAN identifier_source=manual_same_as_identification
     *   且 V1.8.47 的 IAP2_FINAL availability_identifier_present=true。
     */
    public static boolean Iap2AvailSameSourceEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_avail_same_source", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.49] EA00 时序 A/B（单变量）：0xEA00 的发起时机。
     * true  = MFi 认证完成（AA03 / this.t=true / AA05 之后）才发起 EA 会话请求。
     *         依据 Spec §23.2.7 / §23.2.5.1、sysdiagnose（carPlayCapable 0→1 在 MFi 之后）、
     *         固件 MediaManagerService.strings.txt:923 "Received EAPSessionInitiated callback from Cinemo"
     *         （车机是 EA 会话被动接收方，回调在认证推进之后）。
     * false = V1.8.48 旧行为：IAP2_IDENTIFIED 之后立即发（EA00 早于 AA03 343ms）。
     * 默认 true（改动生效）；显式 false 回退旧时序做 A/B 对照。
     */
    public static boolean Iap2Ea00AfterAuthEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ea00_after_auth", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.51] P0-a（单变量）：0xEA00 StartExternalAccessoryProtocolSession 是否携带 param 1。
     * 规范/社区双证（wiomoc iap2/control_session_message/eap.py + CSDN 车机侧
     * _iap2_process_start_eap_session）：
     *   param 0 = protocol_id (Uint8)，param 1 = session_id (Uint16 BE，由 iPhone 分配)。
     * V1.8.50 及以前只发 param 0 ⇒ 车机拿不到会话 ID，EA session(sid=3) 从未激活。
     * 默认 true（改动生效，装包即可验证）；显式 false 回退 V1.8.50 单参数行为做 A/B。
     * 判据：应用内 IAP2_EA03_STATUS status=0 / V1851_EA_SESSION_ESTABLISHED。
     */
    public static boolean Iap2Ea00SessionIdEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ea00_session_id", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.52] A（修正性）：车机 0x1D01 未声明 param 24（WirelessCarPlayTransportComponent）时，
     * 是否仍然强发 0x5702 / 0x4300。
     * 默认 true = 门控生效（不发）——依据：
     *   ① 社区 CSDN 136412276：车机须在 0x1D01 配 BluetoothTransportComponent +
     *      WirelessCarPlayTransportComponent，手机才会发无线 AP 请求；
     *   ② 本机实测（V1.8.51）车机 param24 缺失、SEND=0、wifi_request=false，
     *      强发 0x4300 触发 IAP2_PEER_RESTARTED ×6（每次 ~1.5s 车机重启 iAP2）。
     * 置 false = 回到 V1.8.51 行为（无条件强发 5702 + 可选 p1_3 强发 4300），仅复现用。
     * 判据：IAP2_AVAILABILITY_GATED / IAP2_FORCE_5702_GATED，且 IAP2_PEER_RESTARTED 归零。
     */
    public static boolean Iap2Param24GateEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return true;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_param24_gate", true);
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * [V1.8.52] B（实验性）：0x1D00 StartIdentification 是否发空载荷。
     * 默认 false = 历史行为（bEaPayload() 377B，含 name/model/param6/7/10/17）。
     * 置 true = 按规范发空（wiomoc `StartIdentification: pass`、TI 官方无参、CSDN 车机侧收到后再回 0x1D01），
     * 用于验证「车机是否因我方 1D00 载荷而只给最小能力集」。
     * 判据：车机 0x1D01 是否出现 param 24 / SEND count>0。
     */
    public static boolean Iap2IdentEmptyEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ident_empty", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.52] C（复现用，默认 false）：是否强制发送 0x4E0E DeviceTransportIdentifierNotification。
     * ⚠️ 历史实测（V1.7.x~V1.8.x）：MAC 串 / 空 / TLV(0,uint16 组件ID) / TLV(0,uint8 通道类型) /
     *    补发 param17 共 5 种载荷全部不被 ACK，且是 IAP2_PEER_RESTARTED 的唯一触发点，
     *    故 V1.8.x 起已不再发送。默认 false 保持现状；仅在需要复现该失败时置 true。
     */
    public static boolean Iap2Tx4e0eEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_tx_4e0e", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.42] 严格按 iAP2 规范修正身份参数：param 8 = power_providing_capability (Uint8)、
     * param 9 = maximum_current_drawn_from_device (Uint16 mA)、param 12 = current_language。
     * 默认 false：先保留历史 param 8 = "zh\0" 的行为，避免同时引入多个变量。
     * 置 true 后可与 false 做单变量 A/B，观察车机 0x1D01 是否变化。
     */
    public static boolean Iap2IdentStrictEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("iap2_ident_strict", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.34] P1-2 主动发起 iAP2-over-WiFi 总开关。
     * 默认 false：沿用 V1.8.33 行为（network iAP2 仅由车机首条 CONTROL_POST 被动建立）。
     * 置 true：会话认证完成后，手机作为 iAP2 host **主动**建立 network iAP2 会话并向车机
     * 发送 iAP2 识别（绕开「车机等手机先发、手机等车机先发」的死锁），使车机
     * NmeWifiTransportServer.on_iap_message 收到并置 iap_channel=true，
     * 从而打通车机→手机的反向 iAP2 HID 触摸通道。
     * [V1.8.35] 可通过 设置 → 连接 → 主动发起 iAP2-over-WiFi 开关切换，adb 广播 DEBUG_SET_P12 仍然可用。
     */
    public static boolean P1_2ProactiveIap2Enabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("p1_2_proactive_iap2", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.37] 强制发送 CarPlayAvailability(0x4300) 的开关（默认 false）。
     * 背景：车机 0x1D01 的 RECEIVE 列表通常只含 EA 消息（Info3.6: 4E09..4E0E/EA00/EA01），
     * 不含 17152(0x4300)。原实现直接 z(17152) ⇒ 被 NOT_ADVERTISED 丢弃 ⇒ 0x4300 从未发出
     * ⇒ 车机永远不发 17153(CarPlayStartSession)，IAP2_DETECT_AFTER_LINK 永久 waiting_start=true。
     * 置 true 时绕过广告位校验直接发送，并携带本机蓝牙标识。
     */
    public static boolean P1_3ForceAvailabilityEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("p1_3_force_availability", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * [V1.8.38] 车机发现增强开关（默认 false）。
     * 打开后由 CarPlayCtrlMdnsHelper 监听 mDNS query（判断车机是否在 browse
     * _carplay-ctrl._tcp），并向车机网关单播 mDNS 通告，绕过热点对客户端间
     * multicast 的隔离。只做发现层，不触碰 AirPlay 会话 / RECORD / HID / BT iAP2。
     * 背景：V1.8.36 补 BT iAP2、V1.8.37 强发 0x4300 均已被真机证伪且引发投屏黑屏，
     * 车机不发 GET /ctrl-int/1/connect 的缺口收敛到 Bonjour browser 登记这一级。
     */
    public static boolean CtrlMdnsEnhanceEnabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0)
                    .getBoolean("ctrl_mdns_enhance", false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** [V1.8.33] P1-1 诊断日志：仅在 iap2_diag=true 时打印，前缀 IAP2_DIAG，便于 logcat 过滤。 */
    public static void Iap2Diag(String message) {
        // [V0.0.1] 预埋 logcat 已关闭：未开调试模式时连诊断日志也不产生。
        if (!DEBUG_MODE || !Iap2DiagEnabled()) {
            return;
        }
        Log.i("CarPlayAPK", "IAP2_DIAG " + message);
    }

    /**
     * [V1.8.29] 会话序列 V2 的统一日志：logcat tag CarPlayAPK（前缀 V2_SEQ）+ 应用内
     * files/v2-seq-log.txt 双写。AppLogger 是 prototype 包私有，secured 包不能访问，
     * 所以这里独立追加，避免改动业务层注入链。
     */
    public static void V2Log(String message) {
        if (!DEBUG_MODE) {
            return;
        }
        String line = "V2_SEQ " + message;
        Log.i("CarPlayAPK", line);
        Context ctx = APP_CTX;
        if (ctx == null) {
            return;
        }
        try {
            java.io.FileOutputStream out = ctx.openFileOutput("v2-seq-log.txt", Context.MODE_APPEND);
            try {
                out.write((java.time.Instant.now() + " " + line + "\n")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } finally {
                out.close();
            }
        } catch (Throwable ignored) {
            // 日志写入失败绝不影响会话。
        }
    }


    /**
     * 登记车机下发的 connect 身份。Info3.6 的 CarPlayControlClient 只有完成这一步才把会话
     * 认作 CarPlay 会话并开放 HID 触摸回传（固件 CarPlayController/ActivateService → SendHIDReport）。
     * Records the head-unit identity carried by GET /ctrl-int/1/connect.
     */
    public static void MarkCtrlInvitation(String receiverIdentifier) {
        if (receiverIdentifier != null && !receiverIdentifier.isEmpty()) {
            CTRL_RECEIVER_ID = receiverIdentifier;
            // [V1.8.25] 车机侧用 carplay_devid 记住手机；手机侧同样把
            // AirPlay-Receiver-Device-ID 落盘，作为"已配对车机"的稳定标识。
            io.carplay.app.prototype.CarPlayLinkStore.saveReceiverId(APP_CTX, receiverIdentifier);
        }
        CTRL_INVITATION_AT = SystemClock.elapsedRealtime();
        boolean localPresent = LocalBluetoothIdentifier() != null;
        boolean remotePresent = CTRL_RECEIVER_ID != null;
        CTRL_IDENTITY_BOUND = localPresent && remotePresent;
        Log("V167_CTRL_IDENTITY_BOUND local_identifier_present=" + localPresent
                + " receiver_identifier_present=" + remotePresent
                + " values_logged=false");
    }

    public static void AwaitInvitation() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log("V1711_DIAL_GATE=SKIP_MAIN_THREAD");
            return;
        }
        if (INVITED) {
            Log("V1711_DIAL_GATE=ALREADY_INVITED waited_ms=0");
            return;
        }
        Log("V1711_DIAL_GATE=WAIT_INVITATION timeout_ms=8000");
        int i = 8000;
        int i2 = 0;
        do {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException unused) {
            }
            i2 += 100;
            if (INVITED) {
                LogInt("V1711_DIAL_GATE=INVITED waited_ms=", i2);
                return;
            }
            i -= 100;
        } while (i > 0);
        LogInt("V1711_DIAL_GATE=TIMEOUT waited_ms=", i2);
        if (CTRL_TCP) {
            Log("V1711_DIAL_GATE=ctrl_tcp_seen=0");
        } else {
            Log("V1711_DIAL_GATE=ctrl_tcp_seen=1");
        }
        INVITED = true;
    }

    public static String BluetoothId(String str) {
        String manual = LocalBluetoothIdentifier();
        if (manual != null) {
            Log("V160_TXT_ID=USER_ENTERED");
            return manual;
        }
        Log("V160_TXT_ID=FALLBACK_NO_ADDRESS");
        return str;
    }

    public static void Boot(EventChannel eventChannel, AirPlaySession.n nVar) {
        if (BOOTED) {
            nVar.a("V150_BOOT=SKIP_ALREADY");
            return;
        }
        BOOTED = true;
        try {
            EventChannel.b bVarV = eventChannel.v("POST", "/command", "application/x-apple-binary-plist", new byte[0]);
            int iH = bVarV.h();
            byte[] bArrC = bVarV.c();
            int length = bArrC.length;
            nVar.a("CARPLAY_EVENT_BOOTSTRAP status=" + iH + " body=" + length + " head=" + Hex(bArrC, 32));
            if (length <= 0) {
                try {
                    nVar.a("V150_BOOT_PLIST " + BinaryPlist.e(bArrC));
                } catch (Throwable th) {
                    nVar.a("V150_BOOT_PLIST_FAILED");
                }
            }
        } catch (Throwable th2) {
            nVar.a("CARPLAY_EVENT_BOOTSTRAP_FAILED type=" + th2.getClass().getSimpleName());
        }
    }

    public static String CtrlServiceName() {
        // 死代码保留兼容：只读手动名，不加 iPhone_ 前缀（车机认的是手机蓝牙名本身）。
        String manual = ManualName();
        return manual.length() != 0 ? manual : "CarPlayAPK";
    }

    public static String DeviceMac() {
        // 只取用户手动填写的本机蓝牙地址；不再读取系统蓝牙适配器（Android 6.0+ 也拿不到真实值）。
        // Manual entry only; never query the system Bluetooth adapter.
        Context context = APP_CTX;
        if (context == null) {
            return null;
        }
        return io.carplay.app.prototype.BluetoothAdapterResolver.b(context);
    }

    /* _carplay-ctrl TXT 取值（按现场给定规则）。集中成常量便于复核与微调。
     * [V1.8.24] 规范符合性核查（Apple CarPlay Specification PDF）：
     *   Table 23-20 (p.425) 定义 _carplay-ctrl._tcp（手机广播）TXT 只有 2 键：
     *     id      = Apple device 的 Bluetooth MAC，例 "00:11:22:33:44:55"
     *     srcvers = Apple CarPlay source version，格式必须是 "x.y.z"
     *   Table 23-22 (p.427) 的 deviceid/features/model/protovers/srcvers/flags 属于
     *     _airplay._tcp（车机广播），不是手机 ctrl 服务的规范字段。
     * 本实现保留 11 键是因为 Buick Info3.6 的 libNmeCarPlay.so TXT 键表 @0xa0a3a
     * 确实含 deviceid/flags/model/protovers/pi/srcvers/id，删键有回归风险；
     * 固件按 key 取值，多余键不会导致服务被判无效（BrowseCallback 0x53c70 无 TXT 硬门控，
     * 只读 srcvers @0x52c2c，services/removedServices/ifname 来自 Bonjour info 字典）。
     * [V1.8.43 U4] 版本串统一为 sysdiagnose 真 iPhone 的 CarPlay build：
     *   audiomxd「Stats Collection Disabled, sourceVersion 3201703.」出现在 CarPlay
     *   CarSession / com.apple.carkit.service 上下文；固件 Host 也是 AirPlay/320.17.3。
     *   固件 0x8e7b0 按 major*10000+minor*100+patch 解析，"320.17.3" ⇒ 3201703，
     *   与真 iPhone 的整数值完全相等（旧 TXT "27.2.0" ⇒ 270200、SETUP "320.17" ⇒ 3201700）。
     * [V1.8.43 U5] 机型统一为 iPhone15,3，与 TXT / SETUP / GET /info / iAP2 0x1D00 同源。
     * 这两个常量是全部对外协议身份字段的唯一真源，勿在别处另写字面量。 */
    public static final String CARPLAY_BUILD_VERSION = "320.17.3";
    public static final String CARPLAY_MODEL = "iPhone15,3";
    private static final String TXT_SRCVERS = CARPLAY_BUILD_VERSION;
    private static final String TXT_OSVERS = "1.0";
    private static final String TXT_PROTOVERS = "1.1";
    private static final String TXT_MODEL = CARPLAY_MODEL;
    private static final String TXT_MANUFACTURER = "Apple Inc.";
    private static final String TXT_FEATURES = "0x445F8A00,0x1C340";
    private static final String TXT_FLAGS = "0x4";

    public static void FillTxt(NsdServiceInfo nsdServiceInfo, String str) {
        // deviceid / btaddr 直接使用手机设置里填写的本机蓝牙 MAC；无手动值时保留配对快照兜底。
        String strDeviceMac = LocalBluetoothIdentifier();
        String str2 = strDeviceMac != null ? strDeviceMac : str;
        if (str2 == null || str2.length() == 0) {
            // [V1.8.42] 此前这里是静默 return：本机蓝牙标识与配对快照都为空时，
            // 一个 TXT 键都不写，车机 0x586b0 取 deviceID 必然落空，门控永远打不开，
            // 而日志上完全看不出来。改成显式告警，便于一眼定位「TXT 全空」。
            Log("CTRL_TXT_EMPTY identity_missing=true txt_keys=0"
                    + " manual_mac_present=" + (strDeviceMac != null)
                    + " pairing_snapshot_present=" + (str != null));
            return;
        }
        try {
            nsdServiceInfo.setAttribute("id", str2);
            nsdServiceInfo.setAttribute("deviceid", str2);
            // [V1.8.41] 固件 libNmeCarPlay 取 MAC 的 key 是 deviceID（大写 ID，0xb1e4b），
            // 由 0x586b0 从 Bonjour service 字典取值后经 0x8e190 解析成 6 字节。
            // iOS 原生只广播小写 deviceid，车机内部应有映射，但该映射未被证实；
            // 两个键同值并存，任一路径取值都不会落空（B1）。值取自同一变量，不会漂移。
            nsdServiceInfo.setAttribute("deviceID", str2);
            nsdServiceInfo.setAttribute("btaddr", str2);
            nsdServiceInfo.setAttribute("model", TXT_MODEL);
            nsdServiceInfo.setAttribute("manufacturer", TXT_MANUFACTURER);
            nsdServiceInfo.setAttribute("srcvers", TXT_SRCVERS);
            nsdServiceInfo.setAttribute("features", TXT_FEATURES);
            nsdServiceInfo.setAttribute("osvers", TXT_OSVERS);
            nsdServiceInfo.setAttribute("protovers", TXT_PROTOVERS);
            // id / flags 是历史已有键，保留以避免现有 ctrl 发现行为回归。
            nsdServiceInfo.setAttribute("flags", TXT_FLAGS);
            Log("V170_TXT_FILLED n=11 keys=id,deviceid,deviceID,btaddr,model,manufacturer,srcvers,features,osvers,protovers,flags"
                    + " srcvers=" + TXT_SRCVERS + " features=" + TXT_FEATURES + " protovers=" + TXT_PROTOVERS
                    + " identity_source=" + (strDeviceMac != null ? "LOCAL_BLUETOOTH" : "PAIRING_SNAPSHOT")
                    + " identity_matches_iap2=" + (strDeviceMac != null) + " values_logged=false");
            LogIdentityAudit();
            if (CtrlWaitInvitationEnabled()) {
                boolean idOk = n.a(str2) != null;
                boolean srcOk = TXT_SRCVERS.matches("[0-9]+\\.[0-9]+\\.[0-9]+");
                Log("CTRL_TXT_VALIDATE id_format_ok=" + idOk + " srcvers_format_ok=" + srcOk
                        + " id=" + str2 + " name=" + ServiceName());
            }
        } catch (Throwable th) {
            Log("V170_TXT_FILL_FAILED");
        }
    }

    /**
     * [V1.8.43 第 5 项] 身份链审计日志：一次性打印 SDP 服务名、EIR 实际来源、iAP2 内部名字，
     * 供真机 logcat 核对「手机在蓝牙层/发现层/协议层各报了什么名字」。
     *
     * 固件核查结论（A 级）：libNmeCarPlay.so 内 "CarPlus|Wireless iAP|SDP|EIR|iAP2" 字符串 0 命中，
     * 车机按标准 UUID 00000000-deca-fade-deca-deafdecacafe 做 SDP 发现，不比较 SDP 服务名；
     * 车机 MediaManagerService 的 extractDeviceName()/getAppleDeviceName() 走 USB/BTSA DevPath，
     * 与我们的 RFCOMM 服务名不是同一路径。因此服务名仅作可读性对齐，不是门控条件。
     *
     * EIR 结论（A 级）：Android 应用层无法直接写 EIR（需 BLUETOOTH_PRIVILEGED / 反射蓝牙栈），
     * BtDiscovery 里 "eir_requested=true radio_confirmed=false" 只是历史日志字面量，未真正配置。
     * EIR 里的设备名实际取自系统蓝牙适配器名（SystemBluetoothName），不由本 App 控制。
     */
    private static void LogIdentityAudit() {
        try {
            String manual = ManualName();
            String sysBt = SystemBluetoothName();
            Log("IDENTITY_AUDIT sdp_service_name=Wireless iAP v2"
                    + " sdp_uuid=00000000-deca-fade-deca-deafdecacafe"
                    + " sdp_compared_by_firmware=false"
                    + " eir_source=SYSTEM_BLUETOOTH_ADAPTER app_writable=false"
                    + " eir_system_name_present=" + (sysBt != null)
                    + " eir_matches_manual=" + (sysBt != null && sysBt.equals(manual))
                    + " ctrl_instance_name_source=" + (manual.isEmpty() ? "FALLBACK_CarPlayAPK" : "MANUAL_BT_NAME")
                    + " iap2_model=" + CARPLAY_MODEL
                    + " iap2_name_source=ServiceName"
                    + " carplay_build=" + CARPLAY_BUILD_VERSION
                    + " values_logged=false");
        } catch (Throwable ignored) {
        }
    }

    public static String Hex(byte[] bArr, int i) {
        StringBuilder sb = new StringBuilder();
        int length = bArr.length;
        if (length < i) {
            length = i;
        }
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = bArr[i2] & 255;
            if (i3 < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(i3));
            sb.append(" ");
        }
        return sb.toString();
    }

    /** [V0.0.1] 统一 logcat 出口：未开调试模式时静默（发布包不再预埋任何 tag 输出）。 */
    public static void LogTag(String tag, String message) {
        if (!DEBUG_MODE) {
            return;
        }
        Log.i(tag, message);
    }

    public static void LogTagD(String tag, String message) {
        if (!DEBUG_MODE) {
            return;
        }
        Log.d(tag, message);
    }

    public static void Log(String str) {
        // [V0.0.1] 预埋 logcat 已关闭：仅在用户手动打开「调试模式」后才输出。
        if (!DEBUG_MODE) {
            return;
        }
        Log.i("CarPlayAPK", str);
    }

    public static void LogInt(String str, int i) {
        if (!DEBUG_MODE) {
            return;
        }
        Log.i("CarPlayAPK", str + i);
    }

    public static void LogTiming(DatagramPacket datagramPacket) {
        int i = TMCNT;
        if (i >= 8) {
            return;
        }
        int i2 = i + 1;
        TMCNT = i2;
        Log("V170_TIMING_RX n=" + i2 + " len=" + datagramPacket.getLength());
    }

    public static String ManualName() {
        Context context = APP_CTX;
        if (context == null) {
            return "";
        }
        android.content.SharedPreferences prefs =
                context.getSharedPreferences("carplayapk-local-transport", 0);
        // [V1.8.31] 设置页「手机蓝牙标识」保存的是 phone_bluetooth_name（MainActivity），
        // 旧实现却读 bluetooth_name（SaveName 的历史键）⇒ 用户填写的蓝牙名永远读不到，
        // ServiceName() 一直回退 "CarPlayAPK"，车机按蓝牙名匹配 SendConnect 会对不上。
        // 优先读 UI 键，旧键仅作兼容回退。
        String ui = prefs.getString("phone_bluetooth_name", "");
        if (ui != null && ui.trim().length() != 0) {
            return ui.trim();
        }
        String legacy = prefs.getString("bluetooth_name", "");
        return legacy == null ? "" : legacy.trim();
    }

    public static void MarkInvited() {
        if (INVITED) {
            return;
        }
        INVITED = true;
        Log("V160_CTRL_INVITATION_ACCEPTED");
    }

    // [OBF-MAP] onCtrlTcpAccepted | 职责: 记录车机回连 ctrl，打 V160_CTRL_TCP_ACCEPT。
    // 踩坑(V1.8.19): 日志字面量是 V160_CTRL_TCP_ACCEPT，源码里没有 CTRL_TCP_ACCEPT；用短名 grep 会误判「从未连接」。
    public static void MarkTcpAccept() {
        CTRL_TCP = true;
        Log("V160_CTRL_TCP_ACCEPT at=" + SystemClock.elapsedRealtime());
    }

    public static void PendAdd(String str) {
        if (str != null) {
            ArrayList arrayList = PEND;
            if (arrayList == null) {
                arrayList = new ArrayList();
                PEND = arrayList;
            }
            if (arrayList.contains(str)) {
                return;
            }
            arrayList.add(str);
            ControlChannel.LogTag("CarPlayHID", "HID_PEND_ADD uuid=" + str);
        }
    }

    public static void PendClear() {
        ArrayList arrayList = PEND;
        if (arrayList != null) {
            arrayList.clear();
        }
    }

    public static void ResetInvite() {
        INVITED = false;
        CTRL_TCP = false;
        Log("V1711_INVITE_RESET session_start");
    }

    public static String SaveName(Context context, String str) {
        if (context == null) {
            context = APP_CTX;
        }
        if (context == null) {
            return "保存失败，请重试。";
        }
        if (APP_CTX != null) {
            APP_CTX = context;
        }
        String str2 = str;
        if (str2 == null) {
            str2 = "";
        }
        String strTrim = str2.trim();
        // [V1.8.31] 双写两个键：UI 键供 ManualName()/ServiceName() 读取，旧键保留兼容。
        context.getSharedPreferences("carplayapk-local-transport", 0).edit()
                .putString("phone_bluetooth_name", strTrim)
                .putString("bluetooth_name", strTrim).commit();
        Log("V172_NAME_SAVED len=" + strTrim.length() + " keys=phone_bluetooth_name+bluetooth_name");
        return strTrim.length() == 0 ? "已清除蓝牙名称，重新连接后将使用默认名称广播。" : "已保存蓝牙名称，重新连接后生效。";
    }

    public static String ServiceName() {
        // 只使用用户手动填写的蓝牙名称；未填写时用固定兜底名，不读取系统蓝牙名。
        String strManualName = ManualName();
        return strManualName.length() != 0 ? strManualName : "CarPlayAPK";
    }

    /** [V1.8.32 P0-3] 系统蓝牙名，用于与手填蓝牙名比对，确认 ctrl 服务实例名与车机配对记录一致。 */
    public static String SystemBluetoothName() {
        try {
            android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                return null;
            }
            String name;
            try {
                name = adapter.getName();
            } catch (SecurityException ignored) {
                return null;
            }
            return (name == null || name.isEmpty()) ? null : name.trim();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
