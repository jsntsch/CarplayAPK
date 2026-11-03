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

/* JADX INFO: compiled from: CarPlus */
/* JADX INFO: loaded from: classes.dex */
/**
 * ControlChannel
 *
 * <p>原混淆名：{@code io.carplay.app.secured.hx}（jadx 产物 hx.java）
 *
 * <p>命名依据：V1711_DIAL_GATE + /command + ctrl_tcp_seen；邀请与控制命令通道

 * <p>控制通道（iAP2 / ctrl TCP）。车机侧的「邀请」与 {@code /command} 走这条。
 *
 * <p>读它的时候注意 {@code V1711_DIAL_GATE=*} 这一族日志 —— 那是「拨号闸门」的
 * 状态机，用来判断当前卡在「等邀请」还是「已邀请未建链」。
 */
public final class ControlChannel {
    public static Context APP_CTX;
    public static volatile boolean BOOTED;
    public static volatile boolean CTRL_TCP;
    public static volatile boolean EVENT_READY;
    public static volatile boolean GATECLOSED;
    public static volatile boolean INVITED;
    public static volatile long LASTFB;
    public static volatile long LASTLOG;
    public static volatile int LASTMODE;
    public static volatile AirPlaySession LASTOBJ;
    public static volatile long LASTSEND;
    public static volatile boolean LOGGED;
    public static volatile int MODE;
    public static volatile int P0CNT;
    public static volatile long P0SEC;
    public static volatile ArrayList PEND;
    public static volatile boolean REPORTED;
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
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            try {
                String address = defaultAdapter.getAddress();
                if (address != null) {
                    String strTrim = address.trim();
                    if ("02:00:00:00:00:00".equalsIgnoreCase(strTrim)) {
                        Log("V160_TXT_ID=FALLBACK_DUMMY_MAC");
                        return str;
                    }
                    String strA = n.a(strTrim);
                    if (strA != null && strA.length() != 0) {
                        Log("V160_TXT_ID=BT");
                        return strA;
                    }
                }
            } catch (RuntimeException unused) {
            }
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
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            try {
                String name = defaultAdapter.getName();
                if (name != null) {
                    String strTrim = name.trim();
                    if (strTrim.length() != 0) {
                        return !strTrim.startsWith("iPhone") ? "iPhone_".concat(strTrim) : strTrim;
                    }
                }
            } catch (RuntimeException unused) {
            }
        }
        return "OcarPlay";
    }

    public static String DeviceMac() {
        String strB;
        Context context = APP_CTX;
        if (context != null && (strB = io.carplay.app.prototype.r.b(context)) != null && strB.length() != 0) {
            return strB;
        }
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null) {
            return null;
        }
        try {
            String address = defaultAdapter.getAddress();
            if (address == null) {
                return null;
            }
            String strTrim = address.trim();
            if ("02:00:00:00:00:00".equalsIgnoreCase(strTrim)) {
                return null;
            }
            return n.a(strTrim);
        } catch (RuntimeException unused) {
            return null;
        }
    }

    public static void FillTxt(NsdServiceInfo nsdServiceInfo, String str) {
        String strDeviceMac = DeviceMac();
        String str2 = strDeviceMac != null ? strDeviceMac : str;
        if (str2 == null || str2.length() == 0) {
            return;
        }
        try {
            nsdServiceInfo.setAttribute("id", str2);
            nsdServiceInfo.setAttribute("deviceid", str2);
            nsdServiceInfo.setAttribute("srcvers", "950.7.1");
            nsdServiceInfo.setAttribute("protovers", "1.0");
            nsdServiceInfo.setAttribute("flags", "0x4");
            nsdServiceInfo.setAttribute("model", Build.MODEL);
            Log("V170_TXT_FILLED id_len=" + str2.length() + " keys=id,deviceid,srcvers,protovers,flags,model");
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
        Log.i("OcarPlay", str);
    }

    public static void LogInt(String str, int i) {
        Log.i("OcarPlay", str + i);
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
        return (context == null || (string = context.getSharedPreferences("ocarplay-local-transport", 0).getString("bluetooth_name", "")) == null) ? "" : string.trim();
    }

    public static void MarkInvited() {
        if (INVITED) {
            return;
        }
        INVITED = true;
        Log("V160_CTRL_INVITATION_ACCEPTED");
    }

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
        context.getSharedPreferences("ocarplay-local-transport", 0).edit().putString("bluetooth_name", strTrim).commit();
        Log("V172_NAME_SAVED len=" + strTrim.length());
        return strTrim.length() == 0 ? "已清除蓝牙名称，重新连接后将使用系统蓝牙名广播。" : "已保存蓝牙名称，重新连接后生效。";
    }

    public static String ServiceName() {
        String strManualName = ManualName();
        if (strManualName.length() != 0) {
            return strManualName;
        }
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            try {
                String name = defaultAdapter.getName();
                if (name != null) {
                    String strTrim = name.trim();
                    if (strTrim.length() != 0) {
                        return strTrim;
                    }
                }
            } catch (RuntimeException unused) {
            }
        }
        return "OcarPlay";
    }
}
