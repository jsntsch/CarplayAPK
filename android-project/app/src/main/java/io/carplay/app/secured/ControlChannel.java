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
     * [V1.8.29] 会话序列 V2 总开关（P0-3 / P0-2 / P0-1 / P1-1 / P1-2 / P1-3）。
     * [V1.8.30] **默认改为 false**：2026-11-08 22:44 真机 A/B 证实，V2 开启时
     * 「RECORD 后多流 SETUP」被车机 400 并触发每 ~10.1s `VIDEO_SEND Broken pipe`
     * 的会话重建；关闭 V2 后 40 秒内 `SESSION_FAILURE=0`、投屏恢复。
     * 置 true 可重新启用做逐项 A/B，但当前版本不建议在生产路径开启。
     */
    public static boolean SeqV2Enabled() {
        Context ctx = APP_CTX;
        if (ctx == null) {
            return false;
        }
        try {
            return ctx.getSharedPreferences("carplayapk-debug", 0).getBoolean("session_seq_v2", false);
        } catch (Throwable ignored) {
            return false;
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

    /** [V1.8.33] P1-1 诊断日志：仅在 iap2_diag=true 时打印，前缀 IAP2_DIAG，便于 logcat 过滤。 */
    public static void Iap2Diag(String message) {
        if (!Iap2DiagEnabled()) {
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
     * 本实现保留 10 键是因为 Buick Info3.6 的 libNmeCarPlay.so TXT 键表 @0xa0a3a
     * 确实含 deviceid/flags/model/protovers/pi/srcvers/id，删键有回归风险；
     * 固件按 key 取值，多余键不会导致服务被判无效（BrowseCallback 0x53c70 无 TXT 硬门控，
     * 只读 srcvers @0x52c2c，services/removedServices/ifname 来自 Bonjour info 字典）。
     * 唯一实质违规是 srcvers 少一段：从 "27.2" 改为 "27.2.0"。
     * 固件 0x8e7b0 解析为 major*10000+minor*100+patch，两者均为 270200 ⇒ 零行为差异。 */
    private static final String TXT_SRCVERS = "27.2.0";
    private static final String TXT_OSVERS = "1.0";
    private static final String TXT_PROTOVERS = "1.1";
    private static final String TXT_MODEL = "iPhone15,3";
    private static final String TXT_MANUFACTURER = "Apple Inc.";
    private static final String TXT_FEATURES = "0x445F8A00,0x1C340";
    private static final String TXT_FLAGS = "0x4";

    public static void FillTxt(NsdServiceInfo nsdServiceInfo, String str) {
        // deviceid / btaddr 直接使用手机设置里填写的本机蓝牙 MAC；无手动值时保留配对快照兜底。
        String strDeviceMac = LocalBluetoothIdentifier();
        String str2 = strDeviceMac != null ? strDeviceMac : str;
        if (str2 == null || str2.length() == 0) {
            return;
        }
        try {
            nsdServiceInfo.setAttribute("id", str2);
            nsdServiceInfo.setAttribute("deviceid", str2);
            nsdServiceInfo.setAttribute("btaddr", str2);
            nsdServiceInfo.setAttribute("model", TXT_MODEL);
            nsdServiceInfo.setAttribute("manufacturer", TXT_MANUFACTURER);
            nsdServiceInfo.setAttribute("srcvers", TXT_SRCVERS);
            nsdServiceInfo.setAttribute("features", TXT_FEATURES);
            nsdServiceInfo.setAttribute("osvers", TXT_OSVERS);
            nsdServiceInfo.setAttribute("protovers", TXT_PROTOVERS);
            // id / flags 是历史已有键，保留以避免现有 ctrl 发现行为回归。
            nsdServiceInfo.setAttribute("flags", TXT_FLAGS);
            Log("V170_TXT_FILLED n=10 keys=id,deviceid,btaddr,model,manufacturer,srcvers,features,osvers,protovers,flags"
                    + " srcvers=" + TXT_SRCVERS + " features=" + TXT_FEATURES + " protovers=" + TXT_PROTOVERS
                    + " identity_source=" + (strDeviceMac != null ? "LOCAL_BLUETOOTH" : "PAIRING_SNAPSHOT")
                    + " identity_matches_iap2=" + (strDeviceMac != null) + " values_logged=false");
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

    public static void Log(String str) {
        Log.i("CarPlayAPK", str);
    }

    public static void LogInt(String str, int i) {
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
            Log.i("CarPlayHID", "HID_PEND_ADD uuid=" + str);
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
