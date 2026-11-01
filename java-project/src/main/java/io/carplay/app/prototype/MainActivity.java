package io.carplay.app.prototype;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import io.carplay.app.secured.a4;
import io.carplay.app.secured.b4;
import io.carplay.app.secured.BluetoothDialogRunnable;
import io.carplay.app.secured.BluetoothSaveListener;
import io.carplay.app.secured.c4;
import io.carplay.app.secured.h5;
import io.carplay.app.secured.EcdsaSignature;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.m3;
import io.carplay.app.secured.n3;
import io.carplay.app.secured.o3;
import io.carplay.app.secured.p3;
import io.carplay.app.secured.q3;
import io.carplay.app.secured.r3;
import io.carplay.app.secured.s3;
import io.carplay.app.secured.t3;
import io.carplay.app.secured.u2;
import io.carplay.app.secured.u3;
import io.carplay.app.secured.v2;
import io.carplay.app.secured.v3;
import io.carplay.app.secured.w1;
import io.carplay.app.secured.w3;
import io.carplay.app.secured.x3;
import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.y3;
import io.carplay.app.secured.z3;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/* JADX INFO: loaded from: classes.dex */
public final class MainActivity extends Activity {
    private static final String Z = "com.oplus.ocar";
    private static final String a0 = "com.oplus.ocar.CarlifeActivity";
    private static final String b0 = "android.permission.BLUETOOTH_CONNECT";
    private static final String c0 = "android.permission.NEARBY_WIFI_DEVICES";
    private static final String d0 = "android.permission.ACCESS_LOCAL_NETWORK";
    private static final String e0 = "android.permission.POST_NOTIFICATIONS";
    private static final int f0 = 43;
    private static final int g0 = 44;
    private static final int h0 = 45;
    private static final int i0 = 46;
    private static final String j0 = "android.permission.BLUETOOTH_ADVERTISE";
    private static final String k0 = "1124969763";
    private static final String l0 = "1125859877";
    private static final String m0 = "ocarplay-consent";
    private static final String n0 = "2026-10-20-v1";
    private View[] A;
    private Button[] B;
    private RadioButton C;
    private RadioButton D;
    private boolean E;
    private Button H;
    private Button I;
    private Button J;
    private Button K;
    private Button L;
    private Button M;
    private Button N;
    private Button O;
    private Button P;
    private CompoundButton Q;
    private AlertDialog R;
    private AlertDialog S;
    private AlertDialog T;
    private AlertDialog U;
    private AlertDialog V;
    private TextView W;
    private TextView X;
    private boolean a;
    private Button b;
    private boolean d;
    private boolean e;
    private boolean f;
    private boolean g;
    private boolean h;
    private boolean i;
    private String j;
    private String l;
    private Button m;
    private Button n;
    private Button o;
    private long p;
    private TextView s;
    private TextView t;
    private TextView u;
    private TextView v;
    private TextView w;
    private TextView x;
    private LinearLayout y;
    private LinearLayout z;
    private final Handler c = new Handler(Looper.getMainLooper());
    private String k = "";
    private int q = -1;
    private List<PairingSession.l> r = Collections.EMPTY_LIST;
    private int F = -1;
    private int[] G = new int[3];
    private final Runnable Y = new a();

    public class a implements Runnable {
        public a() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (MainActivity.this.d && MainActivity.this.S0()) {
                MainActivity.this.I0();
                MainActivity.this.a2();
                MainActivity.this.b2();
                MainActivity.this.p2();
                if (MainActivity.this.T == null && MainActivity.this.l != null && !WirelessCarPlayService.L1() && !MainActivity.this.f) {
                    boolean zEquals = MainActivity.this.l.equals(MainActivity.this.i2());
                    MainActivity mainActivity = MainActivity.this;
                    if (zEquals) {
                        mainActivity.n2();
                    } else {
                        mainActivity.l = null;
                    }
                }
                if (MainActivity.this.a && PairingPreparationService.f()) {
                    MainActivity.this.a = false;
                    try {
                        MainActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE").putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", h5.v), MainActivity.i0);
                    } catch (RuntimeException unused) {
                        MainActivity.this.w2();
                        MainActivity.this.o2("系统暂时无法开启可被发现，请查看日志。");
                    }
                }
                if (MainActivity.this.g && !MainActivity.this.f && WirelessCarPlayService.s1()) {
                    MainActivity.this.g = false;
                    MainActivity.this.W1();
                }
                MainActivity.this.c.postDelayed(this, 600L);
            }
        }
    }

    public static final class b extends ScrollView {
        private int a;

        public b(Context context) {
            super(context);
            this.a = -1;
        }

        public void a(int i) {
            this.a = Math.max(0, i);
            requestLayout();
        }

        @Override // android.widget.ScrollView, android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            int i5 = this.a;
            if (i5 >= 0) {
                scrollTo(0, i5);
                this.a = -1;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void A1() {
        try {
            startActivity(new Intent("android.settings.DEVICE_INFO_SETTINGS"));
        } catch (RuntimeException unused) {
            V1("android.settings.SETTINGS");
        }
    }

    public static String A2(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.PackageInfoFlags.of(0L)).versionName;
        } catch (Exception unused) {
            return "";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void B1(AlertDialog alertDialog, EditText editText, String str, DialogInterface dialogInterface) {
        int[] iArr = {-1, -2, -3};
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = 1;
            if (i2 >= 3) {
                Button button = alertDialog.getButton(-1);
                n3 n3Var = new n3(i);
                n3Var.b = this;
                n3Var.e = editText;
                n3Var.c = str;
                n3Var.d = alertDialog;
                VarHandle.storeStoreFence();
                button.setOnClickListener(n3Var);
                Button button2 = alertDialog.getButton(-2);
                v2 v2Var = new v2(i3);
                v2Var.c = this;
                v2Var.d = alertDialog;
                v2Var.b = str;
                VarHandle.storeStoreFence();
                button2.setOnClickListener(v2Var);
                Button button3 = alertDialog.getButton(-3);
                n3 n3Var2 = new n3(i3);
                n3Var2.b = this;
                n3Var2.c = str;
                n3Var2.d = alertDialog;
                n3Var2.e = editText;
                VarHandle.storeStoreFence();
                button3.setOnClickListener(n3Var2);
                return;
            }
            Button button4 = alertDialog.getButton(iArr[i2]);
            button4.setTextSize(12.0f);
            button4.setSingleLine(true);
            button4.setPadding(O0(6), 0, O0(6), 0);
            button4.setMinWidth(0);
            button4.setMinimumWidth(0);
            button4.setMinHeight(O0(48));
            button4.setTextColor(-13934104);
            i2++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void C1(EditText editText, String str, AlertDialog alertDialog, View view) {
        if (WirelessCarPlayService.L1()) {
            o2("连接已启动，请先断开再修改。");
            return;
        }
        String strA = io.carplay.app.secured.n.a(editText.getText().toString().trim());
        if (strA == null) {
            editText.setError(str == null ? "请输入有效的本机蓝牙地址；不接受占位或广播地址。清除已保存地址请使用“清除手动地址”。" : "请输入有效的本机蓝牙地址；不接受占位或广播地址，也可点“暂不填写”继续。");
            return;
        }
        if (strA.equalsIgnoreCase(i2())) {
            editText.setError("这是所选车机的地址，请填写手机本机地址。");
            return;
        }
        if (!getSharedPreferences("ocarplay-local-transport", 0).edit().putString("phone_bluetooth_address", strA).commit()) {
            editText.setError("保存未完成，请重试。");
            return;
        }
        AppLogger.g(this, "USER_LOCAL_BLUETOOTH_IDENTIFIER_SET present=true value_logged=false");
        o2("已保存本机蓝牙地址，原配对和LOG保留。");
        alertDialog.dismiss();
        if (str != null) {
            J0(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void D1(AlertDialog alertDialog, String str, View view) {
        alertDialog.dismiss();
        if (str != null) {
            AppLogger.g(this, "USER_LOCAL_BLUETOOTH_IDENTIFIER_SKIPPED value_logged=false");
            J0(str);
        }
    }

    private void E0() {
        if (this.d && S0()) {
            H0();
            T1();
            a2();
            this.c.removeCallbacks(this.Y);
            this.c.post(this.Y);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void E1(String str, AlertDialog alertDialog, EditText editText, View view) {
        if (str != null) {
            alertDialog.dismiss();
            return;
        }
        if (WirelessCarPlayService.L1()) {
            o2("连接已启动，请先断开再修改。");
        } else {
            if (!getSharedPreferences("ocarplay-local-transport", 0).edit().remove("phone_bluetooth_address").commit()) {
                editText.setError("清除未完成，请重试。");
                return;
            }
            AppLogger.g(this, "USER_LOCAL_BLUETOOTH_IDENTIFIER_CLEARED value_logged=false");
            o2("已清除手动地址，原配对和LOG保留。");
            alertDialog.dismiss();
        }
    }

    private void F0(String str) {
        try {
            getSharedPreferences("wireless-ui", 0).edit().putString("selected_address", str).apply();
            startForegroundService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.V0).putExtra("selectedBtAddress", str).putExtra("fill_display", R0(str)).putExtra("useCurrentWifi", this.p != 0).putExtra("currentWifiHandle", this.p));
            this.g = true;
            o2(this.p == 0 ? "正在自动连接所选车机。仅在系统提示时允许 WLAN 连接，无需手动查找热点。" : "正在当前车机 WLAN 查找服务，随后进行配对和证书认证。请保持这个网络连接。");
            a2();
        } catch (RuntimeException e) {
            AppLogger.g(this, "USER_START_FAILED ".concat(e.getClass().getSimpleName()));
            o2("连接未能启动，请查看日志后重试。");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void F1(EditText editText, AlertDialog alertDialog, DialogInterface dialogInterface) {
        editText.setText("");
        if (this.T == alertDialog) {
            this.T = null;
            this.l = null;
        }
    }

    private Button G0(LinearLayout linearLayout, String str, Runnable runnable) {
        Button button = new Button(this);
        button.setText(str);
        u2 u2Var = new u2(2);
        u2Var.b = runnable;
        VarHandle.storeStoreFence();
        button.setOnClickListener(u2Var);
        linearLayout.addView(button);
        m0.i(button, false);
        return button;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean G1(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    private void H0() {
        TextView textView;
        CarLifeBridge.d dVarF = CarLifeBridge.f(this);
        this.e = dVarF.g();
        TextView textView2 = this.x;
        if (textView2 != null) {
            textView2.setText(dVarF.o());
        }
        if (this.e || (textView = this.t) == null) {
            return;
        }
        textView.setText(dVarF.o());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void I0() {
        if (this.d && S0() && !WirelessCarPlayService.L1()) {
            q2();
            if (this.i || UpdateChecker.c()) {
                return;
            }
            this.i = true;
            s sVar = new s(0);
            sVar.b = this;
            VarHandle.storeStoreFence();
            UpdateChecker.b(this, sVar);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void I1(SharedPreferences sharedPreferences, String str, String str2, DialogInterface dialogInterface, int i) {
        boolean zCommit;
        synchronized (WirelessCarPlayService.U0) {
            zCommit = sharedPreferences.edit().putString(WirelessCarPlayService.r2(str), str2).remove("pending_address").remove("pending_fingerprint").remove("pending_certificate").remove("pending_current_wifi").commit();
        }
        o2(zCommit ? "已记住车机，请重新点击连接。" : "保存失败，请重试。");
    }

    private void J0(String str) {
        if (!S0()) {
            P0();
            return;
        }
        if (!this.d || isFinishing() || isDestroyed() || WirelessCarPlayService.L1()) {
            return;
        }
        if (str.equals(i2())) {
            Stream<PairingSession.l> stream = this.r.stream();
            m3 m3Var = new m3(3);
            m3Var.b = str;
            VarHandle.storeStoreFence();
            if (!stream.noneMatch(m3Var)) {
                if (!h2()) {
                    o2("连接权限已变化，请重新点击连接并按系统提示授权。");
                    return;
                }
                H0();
                if (this.e) {
                    S1(str);
                    return;
                } else {
                    o2("当前 Car+ 组件暂不支持连接，请查看兼容说明。");
                    return;
                }
            }
        }
        o2("车机选择已变化，请重新选择后点击连接。");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void J1(SharedPreferences sharedPreferences, DialogInterface dialogInterface, int i) {
        sharedPreferences.edit().remove("pending_address").remove("pending_fingerprint").remove("pending_certificate").remove("pending_current_wifi").apply();
        o2("已取消首次连接。");
    }

    public static EcdsaSignature.a K0(Context context) {
        return EcdsaSignature.a.c(new EcdsaSignature(), context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void K1(DialogInterface dialogInterface) {
        this.f = false;
    }

    private void L0(String str) {
        if (k0.equals(str) || l0.equals(str)) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(ClipboardManager.class);
            if (clipboardManager == null) {
                o2("暂时无法复制，请手动保存群号 " + str + "。");
                return;
            }
            clipboardManager.setPrimaryClip(ClipData.newPlainText("OcarPlay交流QQ群", str));
            Toast.makeText(this, "已复制 " + (k0.equals(str) ? "一群 " : "二群 ") + str, 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void L1(DialogInterface dialogInterface, int i) {
        w2();
        finishAffinity();
    }

    private boolean M0(long j) {
        if (j == 0) {
            return true;
        }
        Network networkY2 = y2();
        return networkY2 != null && networkY2.getNetworkHandle() == j;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void M1(DialogInterface dialogInterface) {
        this.V = null;
    }

    private void N0() {
        TextView textView = this.W;
        if (textView != null) {
            textView.setText("");
        }
        TextView textView2 = this.X;
        if (textView2 != null) {
            textView2.setText("");
        }
        AlertDialog alertDialog = this.R;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        this.R = null;
        this.W = null;
        this.X = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void N1(View view) {
        L0(k0);
    }

    private int O0(int i) {
        return (int) (i * getResources().getDisplayMetrics().density);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void O1(View view) {
        L0(l0);
    }

    private void P0() {
        if (S0() || !this.d || isFinishing() || isDestroyed()) {
            return;
        }
        l2(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void P1(DialogInterface dialogInterface) {
        this.U = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void Q0() {
        if (!S0()) {
            P0();
            return;
        }
        if (WirelessCarPlayService.L1() || PairingPreparationService.e()) {
            return;
        }
        AlertDialog.Builder message = new AlertDialog.Builder(this).setTitle("让车机重新识别手机").setMessage("仅用于车机设备列表只有 Bluetooth、没有 Apple CarPlay 的情况。\n\n开始后允许系统将手机设为可被发现，再在车机添加/搜索手机并确认配对。随后回本软件刷新列表、选择车机并点击连接。\n\n已有配对可先尝试车机重新搜索或重新连接。本软件不会删除配对；若车机一直保留旧能力，请按车机提示重新配对。准备最多保留两分钟，不影响普通连接。");
        u3 u3Var = new u3(3);
        u3Var.b = this;
        VarHandle.storeStoreFence();
        AlertDialog.Builder negativeButton = message.setPositiveButton("开始准备", u3Var).setNegativeButton("取消", (DialogInterface.OnClickListener) null);
        u3 u3Var2 = new u3(4);
        u3Var2.b = this;
        VarHandle.storeStoreFence();
        negativeButton.setNeutralButton("关闭所选车增强识别", u3Var2).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void Q1(DialogInterface dialogInterface) {
        TextView textView = this.W;
        if (textView != null) {
            textView.setText("");
        }
        this.R = null;
        this.W = null;
        this.X = null;
    }

    private boolean R0(String str) {
        return str != null && getSharedPreferences("wireless-ui", 0).getBoolean("fill_display_".concat(str), false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean R1(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean S0() {
        return n0.equals(getSharedPreferences(m0, 0).getString("accepted_revision", ""));
    }

    private void S1(String str) {
        if (!S0()) {
            P0();
            return;
        }
        if (q2()) {
            return;
        }
        if (UpdateChecker.c()) {
            o2("正在核对版本，请稍后再连接。");
            return;
        }
        long j = this.p;
        if (!M0(AirplayServiceProbe)) {
            o2("当前 WLAN 已变化，请重新选择连接方式。");
            return;
        }
        o2("正在联网核对版本、发布签名和安装包完整性…");
        t tVar = new t();
        tVar.a = this;
        tVar.b = str;
        tVar.c = j;
        VarHandle.storeStoreFence();
        UpdateChecker.b(this, tVar);
        a2();
    }

    private void T1() {
        if (this.m == null || !S0()) {
            return;
        }
        String strI2 = i2();
        if (strI2 == null) {
            strI2 = getSharedPreferences("wireless-ui", 0).getString("selected_address", null);
        }
        try {
            this.r = PairingSession.C0(this);
        } catch (RuntimeException unused) {
            this.r = Collections.EMPTY_LIST;
        }
        this.q = -1;
        if (strI2 != null) {
            for (int i = 0; i < this.r.size(); i++) {
                if (strI2.equals(this.r.get(i).b)) {
                    this.q = i;
                    break;
                }
            }
        }
        z2();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void U0(boolean z) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        a2();
        q2();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void U1() {
        String str;
        try {
            PackageManager packageManager = getPackageManager();
            ComponentName componentName = new ComponentName("com.oplus.ocar", "com.oplus.ocar.settings.CarUnionPreferencesActivity");
            boolean z = u2(packageManager, "com.oplus.ocar", "e49802409584ce53152a9000820a51e4fa8a723b7bcc263e335240acf100bf9e") || u2(packageManager, "com.oplus.ocar", "fc98dae63ad39626c8c67fbe83f2f06f74932a9cd146b92cecfc6a047a904386");
            ActivityInfo activityInfo = packageManager.getActivityInfo(componentName, PackageManager.ComponentInfoFlags.of(0L));
            if (z && activityInfo.enabled && activityInfo.exported && activityInfo.applicationInfo.enabled && ((str = activityInfo.permission) == null || checkSelfPermission(str) == 0)) {
                startActivity(new Intent().setComponent(componentName), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
                AppLogger.g(this, "USER_OPEN_NATIVE_SETTINGS route=NATIVE_HOME active_connection_unchanged");
                return;
            }
            AppLogger.g(this, "USER_OPEN_NATIVE_SETTINGS route=SYSTEM_SETTINGS reason=HOME_NOT_PUBLICLY_ACCESSIBLE");
            try {
                startActivity(new Intent("android.settings.SETTINGS"), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
                Toast.makeText(this, "请在设置搜索“智慧车载”或“Car+车联”，进入原生首页", 1).show();
            } catch (RuntimeException unused) {
                o2("请手动打开手机设置，搜索“智慧车载”或“Car+车联”。当前连接与LOG保留。");
            }
        } catch (Exception e) {
            AppLogger.g(this, "USER_OPEN_NATIVE_SETTINGS route=SYSTEM_SETTINGS reason=HOME_UNAVAILABLE type=".concat(e.getClass().getSimpleName()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean V0(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    private void V1(String str) {
        try {
            startActivity(new Intent(str), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
        } catch (RuntimeException unused) {
            o2("暂时无法打开系统设置，请从手机设置进入。");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void W0(DialogInterface dialogInterface, int i) {
        g2();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void W1() {
        if (!S0()) {
            P0();
            return;
        }
        if (this.d && WirelessCarPlayService.s1()) {
            try {
                startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.X0));
                startActivity(new Intent().setComponent(new ComponentName("com.oplus.ocar", a0)), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
                o2("请按原生 Car+ 提示完成首次连接。");
            } catch (RuntimeException unused) {
                startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.W0));
                o2("Car+ 未能打开，连接已停止。详情已记录到日志。");
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void X0(DialogInterface dialogInterface, int i) {
        String strI2 = i2();
        if (strI2 == null) {
            o2("请先选择已配对车机，再关闭其增强识别。");
            return;
        }
        getSharedPreferences("wireless-ui", 0).edit().putBoolean("enhanced_discovery_" + WirelessCarPlayService.r2(strI2), false).apply();
        o2("已恢复这台车机的普通连接流程，配对和LOG保留。");
    }

    private String X1(int i) {
        PairingSession.l lVar = this.r.get(i);
        Stream<PairingSession.l> stream = this.r.stream();
        a4 a4Var = new a4(0);
        a4Var.b = lVar;
        VarHandle.storeStoreFence();
        if (stream.filter(a4Var).count() <= 1) {
            return lVar.a;
        }
        String str = lVar.a;
        String str2 = lVar.b;
        return str + " · " + str2.substring(Math.max(0, str2.length() - 5));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void Y0(String str, long j, boolean z) {
        if (isDestroyed() || isFinishing() || !this.d || WirelessCarPlayService.L1() || !S0()) {
            return;
        }
        if (!z) {
            o2(getSharedPreferences("release-verification", 0).getString("message", "版本验证未通过，请重试。"));
            q2();
            return;
        }
        H0();
        if (str.equals(i2())) {
            Stream<PairingSession.l> stream = this.r.stream();
            m3 m3Var = new m3(2);
            m3Var.b = str;
            VarHandle.storeStoreFence();
            if (!stream.noneMatch(m3Var) && h2() && this.e) {
                if (this.p == j && M0(AirplayServiceProbe)) {
                    F0(str);
                    return;
                } else {
                    o2("WLAN 或连接方式已变化，请重新点击连接。");
                    return;
                }
            }
        }
        o2("车机选择或连接条件已变化，请重新点击连接。");
    }

    private boolean Y1(String str) {
        if (!S0()) {
            P0();
            return true;
        }
        if (this.d && !isFinishing() && !isDestroyed() && !WirelessCarPlayService.L1()) {
            if (r.a(this, str).g() != null) {
                return false;
            }
            this.l = str;
            n2();
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean Z0(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void Z1() {
        if (WirelessCarPlayService.L1()) {
            return;
        }
        if (checkSelfPermission(b0) != 0) {
            requestPermissions(new String[]{b0}, g0);
        } else {
            T1();
            o2(this.r.isEmpty() ? "还没有已配对的车机，请先点蓝牙配对。" : "列表已刷新，请选择车机。");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void a1() {
        startActivity(new Intent(this, (Class<?>) LogActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Code duplicated, block: B:188:0x040a  */
    /* JADX WARN: Code duplicated, block: B:194:0x0417  */
    /* JADX WARN: Code duplicated, block: B:200:0x0435  */
    /* JADX WARN: Code duplicated, block: B:203:0x043d  */
    /* JADX WARN: Code duplicated, block: B:204:0x043f  */
    /* JADX WARN: Code duplicated, block: B:207:0x0447  */
    /* JADX WARN: Code duplicated, block: B:208:0x0449  */
    /* JADX WARN: Code duplicated, block: B:211:0x045e  */
    /* JADX WARN: Code duplicated, block: B:212:0x0461  */
    /* JADX WARN: Code duplicated, block: B:221:0x047b  */
    /* JADX WARN: Code duplicated, block: B:224:0x0487  */
    /* JADX WARN: Code duplicated, block: B:225:0x0489  */
    /* JADX WARN: Code duplicated, block: B:228:0x0495  */
    /* JADX WARN: Code duplicated, block: B:230:0x0499  */
    /* JADX WARN: Code duplicated, block: B:232:0x049f  */
    /* JADX WARN: Code duplicated, block: B:233:0x04a1  */
    /* JADX WARN: Code duplicated, block: B:236:0x04ac  */
    /* JADX WARN: Code duplicated, block: B:237:0x04ae  */
    /* JADX WARN: Code duplicated, block: B:240:0x04b3  */
    /* JADX WARN: Code duplicated, block: B:242:0x04bb  */
    /* JADX WARN: Code duplicated, block: B:244:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:245:? A[RETURN, SYNTHETIC] */
    public void a2() {
        String strD;
        boolean z;
        String strD2;
        Object obj;
        String str;
        String str2;
        int i;
        boolean z2;
        boolean z3;
        int i2;
        int i3;
        int i4;
        String str3;
        int i5;
        String str4;
        int i6;
        String str5;
        SharedPreferences sharedPreferences = getSharedPreferences(WirelessCarPlayService.b1, 0);
        boolean zL1 = WirelessCarPlayService.L1();
        String string = zL1 ? sharedPreferences.getString("state", "CHECKING") : "STOPPED";
        this.w.setText(k0.a(sharedPreferences.getString("reason", ""), zL1));
        String strB = k0.b(string, zL1, sharedPreferences.getString("reason", ""));
        if (zL1 && string.equals("WIFI") && !sharedPreferences.getString("connection_route", "bluetooth").equals("bluetooth")) {
            strB = sharedPreferences.getString("connection_route", "").equals("retained_wifi") ? "正在复用这台车仍然连接的 WLAN\n保持当前网络，正在重新发现并完整认证车机" : "正在当前车机 WLAN 查找投屏服务\n保持当前网络，后续仍需完整认证";
        }
        if (zL1) {
            String strC = k0.c(sharedPreferences.getString("native_foreground_state", ""));
            if (!strC.isEmpty()) {
                strB = strB + "\n" + strC;
            }
        }
        if (!this.e && !zL1) {
            strB = "当前手机组件暂不支持\n请打开使用教程查看兼容说明";
        }
        int i7 = sharedPreferences.getInt("native_width", 0);
        int i8 = sharedPreferences.getInt("native_height", 0);
        int i9 = sharedPreferences.getInt("receiver_width", 0);
        int i10 = sharedPreferences.getInt("receiver_height", 0);
        if (i7 > 0 && i8 > 0) {
            strB = strB + "\n" + (zL1 ? "画面尺寸" : "上次画面尺寸") + "：" + i7 + " × " + i8;
        }
        if (i7 <= 0 || i8 <= 0 || !sharedPreferences.getBoolean("native_exact_geometry_requested", false)) {
            zL1 = zL1;
        } else {
            int i11 = sharedPreferences.getInt("source_requested_width", 0);
            int i12 = sharedPreferences.getInt("source_requested_height", 0);
            if (i7 == i11 && i8 == i12) {
                str5 = "原生 Car+ 已返回目标画布尺寸";
            } else {
                str5 = "原生 Car+ 返回的尺寸与请求不同：" + i11 + " × " + i12;
            }
            strB = strB + "\n" + str5;
        }
        if (zL1 && string.equals("FORWARDING_NATIVE")) {
            int i13 = sharedPreferences.getInt("measured_sent_fps_x100", -1);
            long jElapsedRealtime = SystemClock.elapsedRealtime() - sharedPreferences.getLong("measured_sent_fps_at_ms", 0L);
            strB = (strB + "\n发送帧率：" + ((i13 < 0 || jElapsedRealtime < 0 || jElapsedRealtime >= 9000) ? "统计中…" : String.format(Locale.ROOT, "%.1f fps", Double.valueOf(((double) i13) / 100.0d)))).concat("\n手机端实测，车机显示帧率未测量");
        }
        if (zL1 && sharedPreferences.getString("audio_state", "").equals("unavailable")) {
            strB = strB + "\n声音通道暂不可用，画面连接保留，详情已记入日志";
        }
        if (zL1 && sharedPreferences.getString("audio_state", "").equals("needs_user")) {
            strB = strB + "\n车机尚未交出声音，请播放音乐后点“恢复车机声音”";
        }
        if (zL1 && sharedPreferences.getString("audio_state", "").equals("negotiating")) {
            strB = strB + "\n正在向车机请求声音播放权限…";
        }
        String string2 = sharedPreferences.getString("fit_mode", "");
        if (string2.equals("fitted")) {
            strB = strB + "\n" + (zL1 ? "自动适配输出" : "上次适配输出") + "：" + sharedPreferences.getInt("output_width", 0) + " × " + sharedPreferences.getInt("output_height", 0) + (sharedPreferences.getBoolean("fill_display", false) ? "（拉伸铺满）" : "（等比显示）");
        }
        if (string2.equals("starting")) {
            strB = strB + "\n正在准备画面自动适配…";
        }
        if (string2.equals("fallback")) {
            strB = strB + "\n自动适配暂不可用，已切回原生画面；可按教程设置分辨率";
        }
        if (!string2.equals("fitted") && !string2.equals("starting") && i7 > 0 && i8 > 0 && i9 > 0 && i10 > 0 && Math.abs(((((double) i7) / ((double) i8)) / (((double) i9) / ((double) i10))) - 1.0d) > 0.02d) {
            strB = strB + "\n画面比例不同：建议在 Car+ 中设置 " + sharedPreferences.getInt("requested_width", i9) + " × " + sharedPreferences.getInt("requested_height", i10) + " 后重连，教程内有步骤";
        }
        if (!strB.contentEquals(this.s.getText())) {
            this.s.setText(strB);
        }
        TextView textView = this.u;
        if (this.e || zL1) {
            strD = zL1 ? k0.d(string, true) : "准备连接你的爱车";
        } else {
            strD = "当前手机组件暂不可用";
        }
        textView.setText(strD);
        this.u.setTextColor(zL1 ? -14328088 : -15258551);
        boolean zE = PairingPreparationService.e();
        if (!zL1 && zE) {
            this.s.setText(PairingPreparationService.f() ? "识别准备就绪\n请从车机搜索/配对手机，然后刷新列表、选择车机连接。准备最多两分钟。" : "正在准备蓝牙识别信息…");
        }
        boolean zS0 = S0();
        this.b.setEnabled(zS0 && !zL1 && !zE && this.e);
        Button button = this.I;
        if (zL1 || !UpdateChecker.c()) {
            z = zL1;
            strD2 = k0.d(string, z);
        } else {
            strD2 = "正在验证版本…";
            z = zL1;
        }
        button.setText(strD2);
        this.I.setEnabled(zS0 && !z && this.e && !UpdateChecker.c());
        this.n.setEnabled(zS0 && !z && this.e && !UpdateChecker.c());
        this.C.setEnabled((!zS0 || z || UpdateChecker.c()) ? false : true);
        this.D.setEnabled((!zS0 || z || UpdateChecker.c()) ? false : true);
        this.H.setEnabled(zS0 && !UpdateChecker.c());
        this.v.setText(getSharedPreferences("release-verification", 0).getString("message", "版本校验已跳过（本地补丁版），可直接连接。"));
        this.N.setEnabled(UpdateChecker);
        this.O.setEnabled(UpdateChecker);
        Button button2 = this.P;
        if (UpdateChecker) {
            str = "audio_state";
            str2 = "";
            obj = "needs_user";
            if (sharedPreferences.getString(str, str2).equals(obj)) {
                i = 0;
            }
            button2.setVisibility(i);
            Button button3 = this.P;
            if (!z && S0() && sharedPreferences.getString(str, str2).equals(obj)) {
                z2 = true;
            } else {
                z2 = false;
            }
            button3.setEnabled(z2);
            Button button4 = this.J;
            if (!z || zE) {
                z3 = true;
            } else {
                z3 = false;
            }
            button4.setEnabled(z3);
            boolean z4 = !z;
            this.m.setEnabled(z4);
            this.L.setEnabled(z4);
            this.M.setEnabled(z4);
            Button button5 = this.J;
            if (!z || zE) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            button5.setVisibility(i2);
            Button button6 = this.N;
            if (UpdateChecker) {
                i3 = 0;
            } else {
                i3 = 8;
            }
            button6.setVisibility(i3);
            Button button7 = this.O;
            if (UpdateChecker) {
                i4 = 0;
            } else {
                i4 = 8;
            }
            button7.setVisibility(i4);
            this.Q.setEnabled(z4);
            Button button8 = this.o;
            if (R0(i2())) {
                str3 = "画面比例：拉伸铺满";
            } else {
                str3 = "画面比例：保持比例";
            }
            button8.setText(str3);
            this.o.setEnabled(z && zS0 && this.Q.isChecked() && i2() != null);
            Button button9 = this.K;
            if (WirelessCarPlayService.s1()) {
                i5 = 0;
            } else {
                i5 = 8;
            }
            button9.setVisibility(i5);
            if (string.equals(this.k)) {
            }
            this.k = string;
            if (!z) {
                if (string.equals("FORWARDING_NATIVE")) {
                    this.t.setText("短暂中断会自动尝试恢复，日志持续保存在本机。");
                    this.t.setVisibility(0);
                    return;
                }
                return;
            }
            TextView textView2 = this.t;
            if (this.e) {
                str4 = str2;
            } else {
                str4 = "请展开连接详情，查看组件兼容说明。";
            }
            textView2.setText(str4);
            TextView textView3 = this.t;
            if (this.e) {
                i6 = 8;
            } else {
                i6 = 0;
            }
            textView3.setVisibility(i6);
        }
        obj = r10;
        str = r3;
        str2 = r7;
        i = 8;
        button2.setVisibility(i);
        Button button10 = this.P;
        if (!z) {
            z2 = false;
        } else {
            z2 = false;
        }
        button10.setEnabled(z2);
        Button button11 = this.J;
        if (UpdateChecker) {
            z3 = true;
        } else {
            z3 = true;
        }
        button11.setEnabled(z3);
        boolean z5 = !z;
        this.m.setEnabled(z5);
        this.L.setEnabled(z5);
        this.M.setEnabled(z5);
        Button button12 = this.J;
        if (UpdateChecker) {
            i2 = 0;
        } else {
            i2 = 0;
        }
        button12.setVisibility(i2);
        Button button13 = this.N;
        if (UpdateChecker) {
            i3 = 0;
        } else {
            i3 = 8;
        }
        button13.setVisibility(i3);
        Button button14 = this.O;
        if (UpdateChecker) {
            i4 = 0;
        } else {
            i4 = 8;
        }
        button14.setVisibility(i4);
        this.Q.setEnabled(z5);
        Button button15 = this.o;
        if (R0(i2())) {
            str3 = "画面比例：拉伸铺满";
        } else {
            str3 = "画面比例：保持比例";
        }
        button15.setText(str3);
        this.o.setEnabled(z && zS0 && this.Q.isChecked() && i2() != null);
        Button button16 = this.K;
        if (WirelessCarPlayService.s1()) {
            i5 = 0;
        } else {
            i5 = 8;
        }
        button16.setVisibility(i5);
        if (string.equals(this.k)) {
            this.k = string;
            if (!z) {
                if (string.equals("FORWARDING_NATIVE")) {
                    this.t.setText("短暂中断会自动尝试恢复，日志持续保存在本机。");
                    this.t.setVisibility(0);
                    return;
                }
                return;
            }
            TextView textView4 = this.t;
            if (this.e) {
                str4 = str2;
            } else {
                str4 = "请展开连接详情，查看组件兼容说明。";
            }
            textView4.setText(str4);
            TextView textView5 = this.t;
            if (this.e) {
                i6 = 8;
            } else {
                i6 = 0;
            }
            textView5.setVisibility(i6);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void b1(View view) {
        r2();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void b2() {
        if (this.W == null || this.X == null) {
            return;
        }
        Iap2Csm.g gVarH3 = WirelessCarPlayService.h3();
        TextView textView = this.W;
        if (gVarH3 == null) {
            textView.setText("车机 WLAN：等待车机下发");
            this.X.setText(WirelessCarPlayService.L1() ? "正在等待车机下发网络配置，收到后将自动尝试接入。无需手动查找或创建热点。" : "请先点击“连接车机”，软件会等待车机下发配置并自动接入。无需手动查找或创建热点。");
            return;
        }
        textView.setText("车机 WLAN：" + gVarH3.a);
        this.X.setText(WirelessCarPlayService.K1() ? "已接入车机 WLAN。接下来继续车机认证与投屏协商；若后续连接失败，请查看连接状态或导出日志，无需反复更改 WLAN 设置。" : "已取得车机网络配置，正在自动尝试接入。请保持 WLAN 开启，仅在系统提示时允许连接或授权；无需自行查找或创建热点。");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void c1(View view) {
        r2();
    }

    private void c2() {
        this.I.setVisibility(this.E ? 8 : 0);
        this.n.setVisibility(this.E ? 0 : 8);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void d1() {
        startActivity(new Intent(this, (Class<?>) HelpActivity.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void d2() {
        if (!S0()) {
            P0();
        } else {
            this.p = 0L;
            e2();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void e1() {
        L0(k0);
    }

    private void e2() {
        if (!S0()) {
            P0();
            return;
        }
        if (q2() || !this.d || WirelessCarPlayService.L1()) {
            return;
        }
        if (PairingPreparationService.e() && !PairingPreparationService.f()) {
            o2("识别信息正在准备，请稍候或断开后重试。");
            return;
        }
        H0();
        if (!this.e) {
            o2("当前 Car+ 组件暂不支持连接，请查看兼容说明。");
            return;
        }
        ArrayList arrayList = new ArrayList();
        String[] strArr = {b0, c0};
        for (int i = 0; i < 2; i++) {
            String str = strArr[i];
            if (checkSelfPermission(str) != 0) {
                arrayList.add(str);
            }
        }
        if (Build.VERSION.SDK_INT >= 37 && checkSelfPermission(AppLogger) != 0) {
            arrayList.add(AppLogger);
        }
        SharedPreferences sharedPreferences = getSharedPreferences("wireless-ui", 0);
        if (checkSelfPermission(e0) != 0 && !sharedPreferences.getBoolean("notification_permission_requested", false)) {
            arrayList.add(e0);
            sharedPreferences.edit().putBoolean("notification_permission_requested", true).apply();
        }
        if (arrayList.isEmpty()) {
            v2(i2());
            return;
        }
        this.h = true;
        this.j = i2();
        requestPermissions((String[]) arrayList.toArray(new String[0]), f0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void f1() {
        L0(l0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void f2() {
        if (!S0()) {
            P0();
            return;
        }
        if (!this.d || WirelessCarPlayService.L1() || UpdateChecker.c()) {
            return;
        }
        if (i2() == null) {
            o2("请先选择已配对的车机，再连接车机 WLAN。");
            return;
        }
        AlertDialog.Builder message = new AlertDialog.Builder(this).setTitle("先连车机蓝牙和 WLAN").setMessage("1. 在手机系统设置中连接所选车机的蓝牙。\n\n2. 连接车机提供的 CarPlay WLAN，提示无互联网时保持连接。普通上网热点可能不提供投屏服务。\n\n3. 确认当前 WLAN 属于所选车机后，点击开始。软件会在这个网络查找服务并进行完整认证。首次证书确认需要你核对设备后手动重试。\n\n未找到服务时请导出日志；不会自动改连其他网络。");
        u3 u3Var = new u3(0);
        u3Var.b = this;
        VarHandle.storeStoreFence();
        AlertDialog.Builder negativeButton = message.setNeutralButton("WLAN 设置", u3Var).setNegativeButton("取消", (DialogInterface.OnClickListener) null);
        u3 u3Var2 = new u3(1);
        u3Var2.b = this;
        VarHandle.storeStoreFence();
        AlertDialog alertDialogCreate = negativeButton.setPositiveButton("开始查找", u3Var2).create();
        v3 v3Var = new v3();
        v3Var.a = this;
        v3Var.b = alertDialogCreate;
        VarHandle.storeStoreFence();
        alertDialogCreate.setOnShowListener(v3Var);
        alertDialogCreate.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void g1() {
        s sVar = new s(1);
        sVar.b = this;
        VarHandle.storeStoreFence();
        UpdateChecker.b(this, sVar);
    }

    private void g2() {
        if (!S0()) {
            P0();
            return;
        }
        if (!this.d || WirelessCarPlayService.L1() || PairingPreparationService.e()) {
            return;
        }
        H0();
        if (!this.e) {
            o2("请先准备受支持的原生 Car+ 组件。");
            return;
        }
        ArrayList arrayList = new ArrayList();
        String[] strArr = {b0, j0};
        for (int i = 0; i < 2; i++) {
            String str = strArr[i];
            if (checkSelfPermission(str) != 0) {
                arrayList.add(str);
            }
        }
        if (!arrayList.isEmpty()) {
            requestPermissions((String[]) arrayList.toArray(new String[0]), h0);
            return;
        }
        try {
            startForegroundService(new Intent(this, (Class<?>) PairingPreparationService.class).setAction(PairingPreparationService.e));
            this.a = true;
            o2("正在准备蓝牙识别信息，请稍候系统授权，再从车机搜索手机。");
        } catch (RuntimeException unused) {
            this.a = false;
            o2("识别准备未启动，请查看连接日志。");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void h1(boolean z) {
        q2();
    }

    private boolean h2() {
        if (checkSelfPermission(b0) == 0 && checkSelfPermission(BluetoothRfcommProfiles) == 0) {
            return Build.VERSION.SDK_INT < 37 || checkSelfPermission(AppLogger) == 0;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String i2() {
        int i = this.q;
        if (i < 0 || i >= this.r.size()) {
            return null;
        }
        return this.r.get(this.q).b;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void j1() {
        if (WirelessCarPlayService.L1()) {
            startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.a1));
            o2("已请求恢复声音，请在手机播放音乐；画面连接保持。");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void j2() {
        if (!S0()) {
            P0();
            return;
        }
        if (WirelessCarPlayService.L1()) {
            return;
        }
        if (checkSelfPermission(b0) != 0) {
            requestPermissions(new String[]{b0}, g0);
            return;
        }
        T1();
        if (this.r.isEmpty()) {
            o2("还没有已配对的车机，请先点击蓝牙配对，再刷新列表。");
            return;
        }
        int size = this.r.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = X1(i);
        }
        AlertDialog.Builder title = new AlertDialog.Builder(this).setTitle("选择已配对的车机");
        int i2 = this.q;
        u3 u3Var = new u3(2);
        u3Var.b = this;
        VarHandle.storeStoreFence();
        title.setSingleChoiceItems(strArr, i2, u3Var).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void k1() {
        startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.Y0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void k2() {
        l2(!S0());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void l1() {
        startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.Z0));
    }

    private void l2(boolean z) {
        AlertDialog alertDialog = this.S;
        if (alertDialog == null || !alertDialog.isShowing()) {
            String str = z ? "已阅读并同意" : "关闭";
            b4 b4Var = new b4();
            b4Var.a = this;
            b4Var.b = z;
            VarHandle.storeStoreFence();
            o3 o3Var = null;
            String str2 = z ? "不同意并退出" : null;
            if (UpdateChecker) {
                o3Var = new o3(23);
                o3Var.b = this;
                VarHandle.storeStoreFence();
            }
            AlertDialog alertDialogK = m0.k(this, "免责声明与使用说明", "使用前请仔细阅读。点击“已阅读并同意”表示你已理解以下说明；不同意可退出，之后再次打开仍可阅读。\n\n一、独立项目\nOcarPlay 是独立的第三方车联工具，并非苹果、手机厂商或汽车厂商的官方产品，也不代表与相关品牌存在授权、认可或合作关系。相关名称与标识归其权利人所有。\n\n二、驾驶安全\n请仅在安全停车后完成安装、配对、设置、排障和日志导出。驾驶中遵守当地法律及交通规则，勿操作手机或观看与驾驶无关的内容。乘员也应避免影响驾驶。不得用本软件替代原车安全提示、仪表、驾驶辅助或紧急功能。\n\n三、兼容与使用风险\n功能依赖手机系统、原生 Car+ 组件、车机能力及连接环境，无法保证所有设备、所有功能或持续稳定连接。系统更新可能改变兼容性。可能出现延迟、无声、卡顿、断连、发热及功耗增加；请按实际情况决定是否继续使用。手机端发送帧率不等于车机实际显示帧率。\n\n原生 Car+ 自带录屏的兼容性仍在验证中，首次录屏相关断开、零画面或车机重启等问题的实际原因与效果未全部确认。出现异常请在安全条件下断开连接，必要时停用本软件并使用原车功能。不得仅凭本机测试认为实车已全面验证。\n\n四、数据与隐私\n连接日志自动保存在本机，不自动上传；可能包含设备名称、网络名称及连接阶段。导出位置由你选择。对外分享前请检查并遮盖个人或设备信息。覆盖升级保留本机记录；卸载、清除应用数据或系统清理可能使记录丢失，请自行保管已导出文件。\n\n五、第三方与再分发\n手机系统、原生组件、车机、其他应用及非官方修改包的行为不由本软件作者控制。请使用可信来源的安装包，遵守第三方服务条款与知识产权要求。修改、转售或再分发者应对其修改与承诺负责，不得冒用作者或官方名义作兼容、安全或售后保证。\n\n六、责任范围\n在法律允许范围内，因用户违法或违规操作、第三方改包等非作者原因造成的后果，由相关责任方依法承担，作者不对此作额外保证。本声明不构成对全部设备、全部使用场景或特定目的适用性的承诺。依法不得排除或限制的责任不受本声明影响，也不限制你依法享有的权利。", str, b4Var, str2, o3Var);
            this.S = alertDialogK;
            alertDialogK.setCancelable(!z);
            this.S.setCanceledOnTouchOutside(!z);
            AlertDialog alertDialog2 = this.S;
            c4 c4Var = new c4();
            c4Var.a = this;
            c4Var.b = z;
            VarHandle.storeStoreFence();
            alertDialog2.setOnDismissListener(c4Var);
            this.S.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void m1(RadioGroup radioGroup, int i) {
        this.E = i == this.D.getId();
        c2();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void m2() {
        if (WirelessCarPlayService.L1()) {
            return;
        }
        String strI2 = i2();
        if (strI2 == null) {
            o2("请先选择车机，再设置这台车的画面比例。");
            return;
        }
        boolean zR0 = R0(strI2);
        s3 s3Var = new s3(1);
        s3Var.b = this;
        s3Var.c = strI2;
        VarHandle.storeStoreFence();
        new AlertDialog.Builder(this).setTitle("这台车的画面比例").setSingleChoiceItems(new String[]{"等比完整显示（可能留边）", "拉伸铺满（画面会变形）"}, zR0 ? 1 : 0, s3Var).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void n1() {
        this.g = false;
        this.h = false;
        this.j = null;
        w2();
        startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.W0));
        o2("已断开连接。下次使用时点击连接车机。");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void n2() {
        if (!S0()) {
            P0();
            return;
        }
        if (this.T != null) {
            return;
        }
        if (WirelessCarPlayService.L1()) {
            o2("请先断开连接，再修改本机蓝牙标识；配对和日志会保留。");
            return;
        }
        String str = this.l;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(O0(20), O0(18), O0(20), O0(16));
        m0.m(linearLayout, "本机蓝牙标识", "bluetooth");
        x2(linearLayout, str == null ? "此处保存这部手机的真实蓝牙地址。已保存的地址会保留，覆盖升级无需重新填写。" : "系统没有提供可用的手机蓝牙地址，部分车机启动时会用到这个标识。请从手机设置查看后填写；点“暂不填写”也可继续连接。", 14);
        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setTextSize(16.0f);
        editText.setHint("手机蓝牙地址（六组冒号分隔）");
        editText.setInputType(528385);
        editText.setSaveEnabled(false);
        editText.setAutofillHints(null);
        editText.setImportantForAutofill(2);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        editText.setText(r.b(this));
        editText.setPadding(O0(12), O0(12), O0(12), O0(12));
        GradientDrawable gradientDrawableB = m0.B(this, -854534, 12);
        gradientDrawableB.setStroke(O0(1), -2300431);
        editText.setBackground(gradientDrawableB);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = O0(14);
        layoutParams.bottomMargin = O0(12);
        linearLayout.addView(editText, layoutParams);
        o3 o3Var = new o3(0);
        o3Var.b = this;
        VarHandle.storeStoreFence();
        G0(linearLayout, "打开手机设置查看", o3Var);
        x2(linearLayout, "怎么查手机蓝牙地址\n1. 先打开手机蓝牙。\n2. 打开手机设置 → 关于本机 / 关于手机 → 状态信息 → 蓝牙地址。不同系统也可能叫“状态”或“所有参数与信息”。\n3. 找不到时，在手机设置中搜索“蓝牙地址”或“状态信息”；如显示不可用，开启蓝牙后重新查看。\n4. 将显示的真实值复制或填入上方，共六组字符，用英文冒号分隔。\n\n必须是这部手机的蓝牙地址，不能填车机地址或 WLAN 地址，也不要填写系统占位地址。找不到时不要猜。", 14);
        x2(linearLayout, "仅保存在本机，日志不记录地址。只补充匹配能力的启动通知，不更换现有 CarPlay 密钥或配对。清除后恢复自动获取。", 12);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout);
        AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setView(scrollView).setPositiveButton(str == null ? "保存" : "保存并连接", (DialogInterface.OnClickListener) null).setNegativeButton(str == null ? "取消" : "暂不填写", (DialogInterface.OnClickListener) null).setNeutralButton(str == null ? "清除手动地址" : "取消连接", (DialogInterface.OnClickListener) null).create();
        this.T = alertDialogCreate;
        p3 p3Var = new p3();
        p3Var.a = this;
        p3Var.b = alertDialogCreate;
        p3Var.c = editText;
        p3Var.d = str;
        VarHandle.storeStoreFence();
        alertDialogCreate.setOnShowListener(p3Var);
        q3 q3Var = new q3();
        q3Var.a = this;
        q3Var.b = editText;
        q3Var.c = alertDialogCreate;
        VarHandle.storeStoreFence();
        alertDialogCreate.setOnDismissListener(q3Var);
        alertDialogCreate.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void o1() {
        i1(1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void o2(String str) {
        this.t.setText(str);
        this.t.setVisibility(str.isEmpty() ? 8 : 0);
        AppLogger.g(this, "USER_NOTICE ".concat(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void p1() {
        V1("android.settings.BLUETOOTH_SETTINGS");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void p2() {
        if (!this.d || this.f || isFinishing() || isDestroyed()) {
            return;
        }
        int i = 0;
        SharedPreferences sharedPreferences = getSharedPreferences(WirelessCarPlayService.c1, 0);
        String string = sharedPreferences.getString("pending_address", null);
        String string2 = sharedPreferences.getString("pending_fingerprint", null);
        if (string == null || string2 == null) {
            return;
        }
        String string3 = sharedPreferences.getString(WirelessCarPlayService.r2(string), null);
        boolean z = sharedPreferences.getBoolean("pending_current_wifi", false);
        if (string3 != null || z) {
            if (string3 == null || !string3.equalsIgnoreCase(string2)) {
                this.f = true;
                this.g = false;
                Stream<PairingSession.l> stream = this.r.stream();
                m3 m3Var = new m3(1);
                m3Var.b = string;
                VarHandle.storeStoreFence();
                String str = (String) stream.filter(m3Var).map(new io.carplay.app.secured.r(4)).findFirst().orElse("所选车机");
                AlertDialog.Builder message = new AlertDialog.Builder(this).setTitle(string3 == null ? "确认当前 WLAN 车机证书" : "确认车机证书变更").setMessage("设备：" + str + "\n\n" + (string3 == null ? "当前 WLAN 候选已通过真实证书挑战，但网络发现本身不能证明它就是所选车机。请确认手机连接的是这台车提供的 CarPlay WLAN，且没有其他接收端。无法确认时请取消。确认后再次点击使用当前车机 WLAN 连接。" : "这台已记住的车机证书发生了变化。请确认设备正确后再更新记录。") + "\n\n证书指纹：\n" + string2);
                r3 r3Var = new r3();
                r3Var.a = this;
                r3Var.b = sharedPreferences;
                r3Var.c = string;
                r3Var.d = string2;
                VarHandle.storeStoreFence();
                AlertDialog.Builder positiveButton = message.setPositiveButton("确认并记住", r3Var);
                s3 s3Var = new s3(i);
                s3Var.b = this;
                s3Var.c = sharedPreferences;
                VarHandle.storeStoreFence();
                AlertDialog.Builder cancelable = positiveButton.setNegativeButton("取消", s3Var).setCancelable(false);
                t3 t3Var = new t3(i);
                t3Var.b = this;
                VarHandle.storeStoreFence();
                cancelable.setOnDismissListener(t3Var).show();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void q1(CompoundButton compoundButton, boolean z) {
        getSharedPreferences("wireless-ui", 0).edit().putBoolean("auto_fit", z).apply();
        a2();
    }

    private boolean q2() {
        ExecutorService executorService = UpdateChecker.a;
        try {
            if (getSharedPreferences("release-verification", 0).getLong("latest_code", 0L) > getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(0L)).getLongVersionCode()) {
                if (this.d && !isFinishing() && !isDestroyed() && !WirelessCarPlayService.L1()) {
                    this.g = false;
                    this.h = false;
                    this.l = null;
                    if (this.V != null) {
                        return true;
                    }
                    String string = getSharedPreferences("release-verification", 0).getString("latest_version", "");
                    AlertDialog.Builder neutralButton = new AlertDialog.Builder(this).setTitle("发现新版本，请先更新").setMessage("当前版本：" + A2(this) + "\n最新版本：" + string + "\n\n请从 OcarPlay 交流 QQ 群获取新版后覆盖安装：\n一群 1124969763\n二群 1125859877\n\n不更新请退出软件。覆盖安装保留 LOG、配对和设置；不要卸载或清除数据。").setPositiveButton("复制一群", (DialogInterface.OnClickListener) null).setNeutralButton("复制二群", (DialogInterface.OnClickListener) null);
                    u3 u3Var = new u3(5);
                    u3Var.b = this;
                    VarHandle.storeStoreFence();
                    AlertDialog alertDialogCreate = neutralButton.setNegativeButton("退出软件", u3Var).setCancelable(false).create();
                    this.V = alertDialogCreate;
                    alertDialogCreate.setCanceledOnTouchOutside(false);
                    AlertDialog alertDialog = this.V;
                    int i = 3;
                    t3 t3Var = new t3(i);
                    t3Var.b = this;
                    VarHandle.storeStoreFence();
                    alertDialog.setOnDismissListener(t3Var);
                    this.V.show();
                    Button button = this.V.getButton(-1);
                    x3 x3Var = new x3(2);
                    x3Var.b = this;
                    VarHandle.storeStoreFence();
                    button.setOnClickListener(x3Var);
                    Button button2 = this.V.getButton(-3);
                    x3 x3Var2 = new x3(i);
                    x3Var2.b = this;
                    VarHandle.storeStoreFence();
                    button2.setOnClickListener(x3Var2);
                    int[] iArr = {-1, -2, -3};
                    for (int i2 = 0; i2 < 3; i2++) {
                        Button button3 = this.V.getButton(iArr[i2]);
                        button3.setTextSize(12.0f);
                        button3.setSingleLine(true);
                        button3.setMinWidth(0);
                        button3.setMinimumWidth(0);
                        button3.setPadding(O0(6), 0, O0(6), 0);
                        button3.setMinHeight(O0(48));
                    }
                }
                return true;
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ boolean r1(PairingSession.l lVar, PairingSession.l lVar2) {
        return lVar2.a.equals(lVar.a);
    }

    private void r2() {
        if (this.U != null || isFinishing() || isDestroyed()) {
            return;
        }
        AlertDialog alertDialogN = j0.n(this);
        this.U = alertDialogN;
        if (alertDialogN != null) {
            t3 t3Var = new t3(2);
            t3Var.b = this;
            VarHandle.storeStoreFence();
            alertDialogN.setOnDismissListener(t3Var);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void s1(DialogInterface dialogInterface, int i) {
        V1("android.settings.WIFI_SETTINGS");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void s2() {
        AlertDialog alertDialog = this.R;
        if (alertDialog == null || !alertDialog.isShowing()) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(O0(20), O0(8), O0(20), O0(8));
            x2(linearLayout, "软件会自动取得车机的 WLAN 配置并尝试接入。请保持手机 WLAN 开启，系统要求连接或授权时按提示允许。\n\n车机没有热点菜单，或普通 WLAN 列表中看不到该名称，都不能单独说明网络未提供。", 15);
            TextView textViewX2 = x2(linearLayout, "", 17);
            this.W = textViewX2;
            textViewX2.setPadding(0, O0(18), 0, O0(12));
            this.W.setSaveEnabled(false);
            TextView textViewX3 = x2(linearLayout, "", 16);
            this.X = textViewX3;
            textViewX3.setSaveEnabled(false);
            ScrollView scrollView = new ScrollView(this);
            scrollView.addView(linearLayout);
            AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setTitle("车机 WLAN 帮助").setView(scrollView).setPositiveButton("关闭", (DialogInterface.OnClickListener) null).create();
            this.R = alertDialogCreate;
            t3 t3Var = new t3(1);
            t3Var.b = this;
            VarHandle.storeStoreFence();
            alertDialogCreate.setOnDismissListener(t3Var);
            this.R.show();
            b2();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void t1(DialogInterface dialogInterface, int i) {
        Network networkY2 = y2();
        if (networkY2 == null) {
            o2("请连接车机 WLAN，并关闭其他并行 WLAN 连接后再试。");
        } else {
            this.p = networkY2.getNetworkHandle();
            e2();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: t2, reason: merged with bridge method [inline-methods] */
    public void i1(int i) {
        int i2 = this.F;
        if (i2 >= 0) {
            this.G[i2] = this.A[i2].getScrollY();
        }
        this.F = Math.max(0, Math.min(2, i));
        int i3 = 0;
        while (true) {
            View[] viewArr = this.A;
            if (i3 >= viewArr.length) {
                int i4 = this.F;
                ((b) viewArr[i4]).a(this.G[i4]);
                return;
            } else {
                viewArr[i3].setVisibility(i3 == this.F ? 0 : 8);
                m0.I(this.B[i3], i3 == this.F);
                i3++;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void u1(AlertDialog alertDialog, DialogInterface dialogInterface) {
        int[] iArr = {-1, -2, -3};
        for (int i = 0; i < 3; i++) {
            Button button = alertDialog.getButton(iArr[i]);
            button.setTextSize(12.0f);
            button.setSingleLine(true);
            button.setPadding(O0(6), 0, O0(6), 0);
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setMinHeight(O0(48));
        }
    }

    private static boolean u2(PackageManager packageManager, String str, String str2) {
        int length = str2.length() / 2;
        byte[] bArr = new byte[length];
        for (int i = 0; i < length; i++) {
            int i2 = i * 2;
            bArr[i] = (byte) Integer.parseInt(str2.substring(i2, i2 + 2), 16);
        }
        return packageManager.hasSigningCertificate(str, bArr, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void v1(DialogInterface dialogInterface, int i) {
        this.q = i;
        z2();
        dialogInterface.dismiss();
    }

    public static void v170NameDialog(MainActivity mainActivity) {
        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(mainActivity.O0(20), mainActivity.O0(18), mainActivity.O0(20), mainActivity.O0(16));
        m0.m(linearLayout, "蓝牙名称", "bluetooth");
        StringBuilder sb = new StringBuilder();
        sb.append("这里填写手机蓝牙的完整名称，ctrl 广播直接用该名称，不加任何前缀。留空则回落到系统蓝牙名。修改后需重新连接生效。\n\n当前已保存：");
        String strManualName = ControlChannel.ManualName();
        if (strManualName.length() == 0) {
            strManualName = "（未设置）";
        }
        sb.append(strManualName);
        sb.append("    输入框内容与上面不一致时，以输入框为准。");
        mainActivity.x2(linearLayout, sb.toString(), 14);
        EditText editText = new EditText(mainActivity);
        editText.setSingleLine(true);
        editText.setTextSize(16.0f);
        editText.setHint("例如：我的手机（留空用系统蓝牙名）");
        editText.setInputType(1);
        editText.setSaveEnabled(false);
        editText.setAutofillHints(null);
        editText.setImportantForAutofill(2);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        editText.setText(ControlChannel.ManualName());
        editText.setPadding(mainActivity.O0(12), mainActivity.O0(12), mainActivity.O0(12), mainActivity.O0(12));
        GradientDrawable gradientDrawableB = m0.B(mainActivity, -854534, 12);
        gradientDrawableB.setStroke(mainActivity.O0(1), -2300431);
        editText.setBackground(gradientDrawableB);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = mainActivity.O0(14);
        layoutParams.bottomMargin = mainActivity.O0(12);
        linearLayout.addView(editText, layoutParams);
        AlertDialog.Builder view = new AlertDialog.Builder(mainActivity).setView(linearLayout);
        BluetoothSaveListener btsaveVar = new BluetoothSaveListener();
        btsaveVar.a = mainActivity;
        btsaveVar.b = editText;
        VarHandle.storeStoreFence();
        view.setPositiveButton("保存", btsaveVar).setNegativeButton("取消", (DialogInterface.OnClickListener) null).create().show();
    }

    public static void v170SaveName(MainActivity mainActivity, EditText editText) {
        String strSaveName = ControlChannel.SaveName(mainActivity, editText != null ? editText.getText().toString() : "");
        if (mainActivity != null) {
            mainActivity.o2(strSaveName);
        }
    }

    private void v2(String str) {
        if (!S0()) {
            P0();
            return;
        }
        if (str == null) {
            o2("请先选择一台已配对的车机。");
            return;
        }
        Stream<PairingSession.l> stream = this.r.stream();
        m3 m3Var = new m3(0);
        m3Var.b = str;
        VarHandle.storeStoreFence();
        if (stream.noneMatch(m3Var)) {
            o2("所选车机不在配对列表中，请重新配对并刷新。");
        } else {
            if (isDestroyed() || !this.d || WirelessCarPlayService.L1() || Y1(str)) {
                return;
            }
            S1(str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void w1(boolean z) {
        if (UpdateChecker) {
            if (getSharedPreferences(m0, 0).edit().putString("accepted_revision", n0).commit()) {
                AppLogger.g(this, "USER_DISCLAIMER_ACCEPTED revision=2026-10-20-v1");
                E0();
            } else {
                Toast.makeText(this, "无法保存确认，请重新打开后重试。", 1).show();
                finish();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void w2() {
        this.a = false;
        stopService(new Intent(this, (Class<?>) PairingPreparationService.class));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void x1() {
        this.g = false;
        this.h = false;
        this.j = null;
        finish();
    }

    private TextView x2(LinearLayout linearLayout, String str, int i) {
        return m0.C(linearLayout, str, i, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void y1(boolean z, DialogInterface dialogInterface) {
        this.S = null;
        if (!z || S0() || isFinishing()) {
            return;
        }
        finish();
    }

    private Network y2() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(ConnectivityManager.class);
        if (connectivityManager == null) {
            return null;
        }
        Network network = null;
        for (Network network2 : connectivityManager.getAllNetworks()) {
            if (WifiNetworkMonitor.E(connectivityManager.getNetworkCapabilities(network2)) && CarPlayCtrlAdvertiser.m(connectivityManager.getLinkProperties(network2)) != null) {
                if (network != null) {
                    return null;
                }
                network = network2;
            }
        }
        return network;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void z1(String str, DialogInterface dialogInterface, int i) {
        getSharedPreferences("wireless-ui", 0).edit().putBoolean(w1.e("fill_display_", str), i == 1).apply();
        dialogInterface.dismiss();
        a2();
    }

    private void z2() {
        String strX1;
        int i = this.q;
        if (i < 0 || i >= this.r.size()) {
            strX1 = this.r.isEmpty() ? "请先蓝牙配对，再刷新列表" : "请选择已配对的车机";
        } else {
            strX1 = X1(this.q);
        }
        this.m.setText(strX1);
        this.m.setContentDescription("选择车机，" + strX1);
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == i0) {
            AppLogger.g(this, "PHONE_DISCOVERY_VISIBILITY allowed=" + (i2 > 0) + " radio_confirmed=false");
            if (i2 > 0) {
                o2("请在车机搜索并配对本手机，然后返回刷新列表、选择车机连接。菜单变化以车机实际显示为准。");
            } else {
                w2();
                o2("已取消蓝牙可见授权，普通连接仍可使用。");
            }
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int i = 1;
        int i2 = 0;
        this.g = bundle != null && bundle.getBoolean("continue_source_intent", false);
        this.a = bundle != null && bundle.getBoolean("pairing_visibility_pending", false);
        this.l = bundle == null ? null : bundle.getString("identity_connect_address");
        this.p = bundle != null ? bundle.getLong("current_wifi_handle", 0L) : 0L;
        this.E = bundle != null && bundle.getBoolean("home_manual_route", false);
        int[] intArray = bundle != null ? bundle.getIntArray("workspace_scroll_y") : null;
        if (intArray != null && intArray.length == 3) {
            this.G = (int[]) intArray.clone();
        }
        LinearLayout linearLayoutH = m0.H(this);
        o3 o3Var = new o3(i);
        o3Var.b = this;
        VarHandle.storeStoreFence();
        o3 o3Var2 = new o3(11);
        o3Var2.b = this;
        VarHandle.storeStoreFence();
        m0.F(linearLayoutH, o3Var, o3Var2);
        LinearLayout linearLayoutG = m0.G(this);
        LinearLayout linearLayoutG2 = m0.G(this);
        LinearLayout linearLayoutG3 = m0.G(this);
        FrameLayout frameLayout = new FrameLayout(this);
        linearLayoutH.addView(frameLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        this.A = new View[3];
        LinearLayout[] linearLayoutArr = {linearLayoutG, linearLayoutG2, linearLayoutG3};
        for (int i3 = 0; i3 < 3; i3++) {
            b bVar = new b(this);
            bVar.setFillViewport(true);
            bVar.addView(linearLayoutArr[i3]);
            frameLayout.addView(bVar, new FrameLayout.LayoutParams(-1, -1));
            this.A[i3] = bVar;
        }
        LinearLayout linearLayoutJ = m0.j(linearLayoutG);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setBaselineAligned(false);
        linearLayout.setGravity(16);
        linearLayoutJ.addView(linearLayout, new LinearLayout.LayoutParams(-1, -2));
        TextView textViewM = m0.m(linearLayout, "准备连接你的爱车", "car");
        this.u = textViewM;
        textViewM.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        this.u.setPadding(0, 0, O0(8), 0);
        Button buttonG = j0.g(this, linearLayout);
        x3 x3Var = new x3(i);
        x3Var.b = this;
        VarHandle.storeStoreFence();
        buttonG.setOnClickListener(x3Var);
        TextView textViewX2 = x2(linearLayoutJ, "", 13);
        this.t = textViewX2;
        textViewX2.setTextColor(-10193265);
        this.t.setPadding(0, O0(4), 0, O0(8));
        this.t.setVisibility(8);
        o3 o3Var3 = new o3(17);
        o3Var3.b = this;
        VarHandle.storeStoreFence();
        this.P = G0(linearLayoutJ, "恢复车机声音", o3Var3);
        LinearLayout linearLayoutZ = m0.z(linearLayoutJ);
        o3 o3Var4 = new o3(18);
        o3Var4.b = this;
        VarHandle.storeStoreFence();
        this.N = G0(linearLayoutZ, "返回车机", o3Var4);
        o3 o3Var5 = new o3(19);
        o3Var5.b = this;
        VarHandle.storeStoreFence();
        this.O = G0(linearLayoutZ, "返回投屏", o3Var5);
        LinearLayout linearLayoutJ2 = m0.j(linearLayoutG);
        m0.m(linearLayoutJ2, "车机与连接方式", "bluetooth");
        o3 o3Var6 = new o3(20);
        o3Var6.b = this;
        VarHandle.storeStoreFence();
        Button buttonG0 = G0(linearLayoutJ2, "选择已配对的车机", o3Var6);
        this.m = buttonG0;
        buttonG0.setGravity(8388627);
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(1);
        linearLayoutJ2.addView(radioGroup);
        this.C = m0.y(this, "自动连接 · 推荐");
        this.D = m0.y(this, "当前车机 WLAN");
        this.C.setId(View.generateViewId());
        this.D.setId(View.generateViewId());
        radioGroup.addView(this.C);
        radioGroup.addView(this.D);
        radioGroup.check((this.E ? this.D : this.C).getId());
        int i4 = 12;
        x2(linearLayoutJ2, "自动：开启蓝牙与 WLAN。\n手动：先连这台车的蓝牙和 CarPlay WLAN。", 12).setTextColor(-10193265);
        o3 o3Var7 = new o3(21);
        o3Var7.b = this;
        VarHandle.storeStoreFence();
        Button buttonG1 = G0(linearLayoutJ2, "连接车机", o3Var7);
        this.I = buttonG1;
        m0.i(buttonG1, true);
        o3 o3Var8 = new o3(22);
        o3Var8.b = this;
        VarHandle.storeStoreFence();
        Button buttonG2 = G0(linearLayoutJ2, "使用当前车机 WLAN 连接", o3Var8);
        this.n = buttonG2;
        m0.i(buttonG2, true);
        z3 z3Var = new z3();
        z3Var.a = this;
        VarHandle.storeStoreFence();
        radioGroup.setOnCheckedChangeListener(z3Var);
        c2();
        TextView textViewX3 = x2(linearLayoutJ2, "", 12);
        this.w = textViewX3;
        textViewX3.setTextColor(-10193265);
        this.w.setPadding(0, O0(12), 0, 0);
        o3 o3Var9 = new o3(2);
        o3Var9.b = this;
        VarHandle.storeStoreFence();
        this.K = G0(linearLayoutJ2, "继续打开 Car+", o3Var9);
        o3 o3Var10 = new o3(3);
        o3Var10.b = this;
        VarHandle.storeStoreFence();
        this.J = G0(linearLayoutJ2, "断开连接", o3Var10);
        o3 o3Var11 = new o3(4);
        o3Var11.b = this;
        VarHandle.storeStoreFence();
        G0(linearLayoutG, "查看连接详情", o3Var11);
        TextView textViewX4 = x2(linearLayoutG, "日志自动保存在本机；切换页面不会中断连接。", 11);
        textViewX4.setTextColor(-10193265);
        int i5 = 10;
        textViewX4.setPadding(0, O0(10), 0, 0);
        m0.A(linearLayoutG2, "连接工具", "调整设置或查看本次连接详情");
        LinearLayout linearLayoutJ3 = m0.j(linearLayoutG2);
        this.z = linearLayoutJ3;
        m0.m(linearLayoutJ3, "连接详情", "log");
        TextView textViewX5 = x2(this.z, "", 13);
        this.s = textViewX5;
        textViewX5.setTextIsSelectable(true);
        TextView textViewX6 = x2(this.z, "", 12);
        this.x = textViewX6;
        textViewX6.setTextColor(-10193265);
        LinearLayout linearLayoutJ4 = m0.j(linearLayoutG2);
        this.y = linearLayoutJ4;
        m0.m(linearLayoutJ4, "车机与画面", "car");
        LinearLayout linearLayoutZ2 = m0.z(this.y);
        o3 o3Var12 = new o3(5);
        o3Var12.b = this;
        VarHandle.storeStoreFence();
        this.M = G0(linearLayoutZ2, "蓝牙配对", o3Var12);
        o3 o3Var13 = new o3(6);
        o3Var13.b = this;
        VarHandle.storeStoreFence();
        this.L = G0(linearLayoutZ2, "刷新列表", o3Var13);
        LinearLayout linearLayout2 = this.y;
        o3 o3Var14 = new o3(7);
        o3Var14.b = this;
        VarHandle.storeStoreFence();
        G0(linearLayout2, "本机蓝牙标识", o3Var14);
        LinearLayout linearLayout3 = this.y;
        if (linearLayout3 != null) {
            BluetoothDialogRunnable btdlgVar = new BluetoothDialogRunnable();
            btdlgVar.a = this;
            VarHandle.storeStoreFence();
            G0(linearLayout3, "蓝牙名称", btdlgVar);
        }
        CompoundButton compoundButtonE = m0.E(this, "画面自动适配", getSharedPreferences("wireless-ui", 0).getBoolean("auto_fit", true));
        this.Q = compoundButtonE;
        w3 w3Var = new w3();
        w3Var.a = this;
        VarHandle.storeStoreFence();
        compoundButtonE.setOnCheckedChangeListener(w3Var);
        this.y.addView(this.Q);
        LinearLayout linearLayout4 = this.y;
        o3 o3Var15 = new o3(8);
        o3Var15.b = this;
        VarHandle.storeStoreFence();
        this.o = G0(linearLayout4, "画面比例：保持比例", o3Var15);
        LinearLayout linearLayoutZ3 = m0.z(this.y);
        o3 o3Var16 = new o3(9);
        o3Var16.b = this;
        VarHandle.storeStoreFence();
        this.b = G0(linearLayoutZ3, "车机识别修复", o3Var16);
        o3 o3Var17 = new o3(i5);
        o3Var17.b = this;
        VarHandle.storeStoreFence();
        G0(linearLayoutZ3, "WLAN 帮助", o3Var17);
        m0.A(linearLayoutG3, "帮助与支持", "教程、交流与版本更新");
        Button buttonF = j0.f(this, linearLayoutG3);
        x3 x3Var2 = new x3(i2);
        x3Var2.b = this;
        VarHandle.storeStoreFence();
        buttonF.setOnClickListener(x3Var2);
        LinearLayout linearLayoutJ5 = m0.j(linearLayoutG3);
        o3 o3Var18 = new o3(i4);
        o3Var18.b = this;
        VarHandle.storeStoreFence();
        G0(linearLayoutJ5, "使用教程", o3Var18);
        o3 o3Var19 = new o3(13);
        o3Var19.b = this;
        VarHandle.storeStoreFence();
        G0(linearLayoutJ5, "免责声明", o3Var19);
        LinearLayout linearLayoutJ6 = m0.j(linearLayoutG3);
        m0.m(linearLayoutJ6, "版本更新", "refresh");
        this.v = x2(linearLayoutJ6, "连接前自动核对版本、发布签名和安装包完整性。", 12);
        o3 o3Var20 = new o3(16);
        o3Var20.b = this;
        VarHandle.storeStoreFence();
        this.H = G0(linearLayoutJ6, "检查更新", o3Var20);
        x2(linearLayoutG3, w1.e("OcarPlay · v", A2(this)), 11).setTextColor(-10193265);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setOrientation(0);
        linearLayout5.setBaselineAligned(false);
        linearLayout5.setBackgroundColor(-1);
        linearLayoutH.addView(linearLayout5, new LinearLayout.LayoutParams(-1, -2));
        this.B = new Button[3];
        String[] strArr = {"连接", "工具", "支持"};
        for (int i6 = 0; i6 < 3; i6++) {
            Button[] buttonArr = this.B;
            String str = strArr[i6];
            y3 y3Var = new y3();
            y3Var.a = this;
            y3Var.b = i6;
            VarHandle.storeStoreFence();
            buttonArr[i6] = G0(linearLayout5, str, y3Var);
        }
        setContentView(linearLayoutH);
        i1(bundle != null ? bundle.getInt("workspace_page", 0) : 0);
        m0.w(this);
        H0();
        if (S0()) {
            T1();
        }
        a2();
        AppLogger.g(this, w1.e("USER_UI_OPEN version=", A2(this)));
    }

    @Override // android.app.Activity
    public void onDestroy() {
        this.c.removeCallbacks(this.Y);
        AlertDialog alertDialog = this.V;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.V = null;
        }
        AlertDialog alertDialog2 = this.T;
        if (alertDialog2 != null) {
            alertDialog2.setOnDismissListener(null);
            this.T.dismiss();
            this.T = null;
        }
        AlertDialog alertDialog3 = this.S;
        if (alertDialog3 != null) {
            alertDialog3.setOnDismissListener(null);
            this.S.dismiss();
            this.S = null;
        }
        AlertDialog alertDialog4 = this.U;
        if (alertDialog4 != null) {
            alertDialog4.setOnDismissListener(null);
            this.U.dismiss();
            this.U = null;
        }
        super.onDestroy();
    }

    @Override // android.app.Activity
    public void onPause() {
        this.d = false;
        this.c.removeCallbacks(this.Y);
        N0();
        super.onPause();
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        T1();
        if (i == h0) {
            if (checkSelfPermission(b0) == 0 && checkSelfPermission(j0) == 0) {
                g2();
                return;
            } else {
                o2("识别修复需要系统蓝牙可见权限，普通连接不受影响。");
                return;
            }
        }
        if (i == g0) {
            o2(checkSelfPermission(b0) == 0 ? "请选择车机后连接。" : "需要附近蓝牙设备权限才能显示车机。");
            return;
        }
        if (i == f0) {
            boolean z = this.h;
            String str = this.j;
            this.h = false;
            this.j = null;
            if (!h2()) {
                o2("连接需要附近设备和本地网络权限，请允许后重试。");
            } else if (!z || str == null) {
                o2("权限已就绪，请选择车机后连接。");
            } else {
                v2(str);
            }
        }
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        this.d = true;
        if (S0()) {
            E0();
        } else {
            P0();
        }
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("continue_source_intent", this.g);
        bundle.putBoolean("pairing_visibility_pending", this.a);
        bundle.putString("identity_connect_address", this.l);
        bundle.putLong("current_wifi_handle", this.p);
        bundle.putBoolean("home_manual_route", this.E);
        bundle.putInt("workspace_page", this.F);
        int i = this.F;
        if (i >= 0) {
            this.G[i] = this.A[i].getScrollY();
        }
        bundle.putIntArray("workspace_scroll_y", (int[]) this.G.clone());
        super.onSaveInstanceState(bundle);
    }
}
