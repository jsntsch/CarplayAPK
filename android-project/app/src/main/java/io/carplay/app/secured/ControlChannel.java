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
     * 登记车机下发的 connect 身份。Info3.6 的 CarPlayControlClient 只有完成这一步才把会话
     * 认作 CarPlay 会话并开放 HID 触摸回传（固件 CarPlayController/ActivateService → SendHIDReport）。
     * Records the head-unit identity carried by GET /ctrl-int/1/connect.
     */
    public static void MarkCtrlInvitation(String receiverIdentifier) {
        if (receiverIdentifier != null && !receiverIdentifier.isEmpty()) {
            CTRL_RECEIVER_ID = receiverIdentifier;
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
        String string;
        Context context = APP_CTX;
        return (context == null || (string = context.getSharedPreferences("carplayapk-local-transport", 0).getString("bluetooth_name", "")) == null) ? "" : string.trim();
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
        context.getSharedPreferences("carplayapk-local-transport", 0).edit().putString("bluetooth_name", strTrim).commit();
        Log("V172_NAME_SAVED len=" + strTrim.length());
        return strTrim.length() == 0 ? "已清除蓝牙名称，重新连接后将使用默认名称广播。" : "已保存蓝牙名称，重新连接后生效。";
    }

    public static String ServiceName() {
        // 只使用用户手动填写的蓝牙名称；未填写时用固定兜底名，不读取系统蓝牙名。
        String strManualName = ManualName();
        return strManualName.length() != 0 ? strManualName : "CarPlayAPK";
    }
}
