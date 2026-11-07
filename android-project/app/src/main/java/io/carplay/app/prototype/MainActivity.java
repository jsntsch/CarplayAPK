/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 SCH
 *
 * 本文件以 MIT 协议发布，完整条款见项目根目录 LICENSE。
 * This file is released under the MIT License; see LICENSE in the project root.
 *
 * 文件用途 / File purpose:
 *   本文件定义 MainActivity。主界面：权限申请、服务启停、状态展示与用户操作入口。
 *   This file defines MainActivity. Main screen: permission requests, service start/stop, status display and user actions.
 *
 * 所属子系统 / Subsystem:
 *   业务层（界面、前台服务、配对编排、原生组件桥接） / app layer (UI, foreground service, pairing orchestration, native bridge)
 */

package io.carplay.app.prototype;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
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
import android.view.ViewGroup;
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
import io.carplay.app.secured.BluetoothDialogRunnable;
import io.carplay.app.secured.BluetoothSaveListener;
import io.carplay.app.secured.ControlChannel;
import io.carplay.app.secured.EcdsaSignature;
import io.carplay.app.secured.Iap2Csm;
import io.carplay.app.secured.a4;
import io.carplay.app.secured.b4;
import io.carplay.app.secured.c4;
import io.carplay.app.secured.BtDiscovery;
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
import io.carplay.app.secured.ProtoMessageBase;
import io.carplay.app.secured.w3;
import io.carplay.app.secured.y3;
import io.carplay.app.secured.z3;
import io.carplay.app.prototype.ui.AboutScreenUi;
import io.carplay.app.prototype.ui.HomeScreenUi;
import io.carplay.app.prototype.ui.Pager;
import io.carplay.app.prototype.ui.ScreenActions;
import io.carplay.app.prototype.ui.SettingsScreenUi;
import io.carplay.app.prototype.ui.Ui;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;


/**
 * 主界面：权限申请、服务启停、状态展示与用户操作入口。
 *
 * Main screen: permission requests, service start/stop, status display and user actions.
 */
public final class MainActivity extends Activity
        implements ScreenActions.Home, ScreenActions.Settings, ScreenActions.About {
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
    // 系统内录（MediaProjection）授权请求码。音源要的是手机里播放的音乐（含 Car+），
    // 麦克风只能录到环境声，因此连接前先申请一次内录授权。
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 47;
    private static final String j0 = "android.permission.BLUETOOTH_ADVERTISE";
    private static final String m0 = "carplayapk-consent";
    private static final String n0 = "2026-10-20-v1";
    
    private static String lastComponentSupport;
    // 对话框字段（与三屏无关，保留）
    private AlertDialog R;
    private AlertDialog S;
    private AlertDialog T;
    private AlertDialog V;
    private TextView W;
    private TextView X;
    // 引擎状态字段
    private boolean a;
    private boolean d;
    private boolean e;
    private boolean f;
    private boolean g;
    private boolean h;
    private String j;
    private String l;
    private boolean E;
    // 等待内录授权期间暂存的连接目标，授权回调回来后继续发起连接。
    private String pendingConnectTarget;
    private long p;
    private final Handler c = new Handler(Looper.getMainLooper());
    private String k = "";
    private int q = -1;
    private List<PairingSession.l> r = Collections.EMPTY_LIST;
    private boolean logEnabled = true;
    private boolean debugMode = false;
    private boolean autoConnect = false;
    private boolean autoTriggered = false;
    // 新版三屏 + 横向翻页容器（移植自参考 APK 的布局）
    private HomeScreenUi homeScreen;
    private SettingsScreenUi settingsScreen;
    private AboutScreenUi aboutScreen;
    private Pager pager;
    private Button[] tabButtons;
    private int currentPage = 0;
    private final Runnable Y = new a();

    /**
     * 后台任务：在独立线程中执行一次异步动作。
     *
     * Background task executing one asynchronous step on its own thread.
     */
    public class a implements Runnable {
        public a() {
        }

        @Override 
        public void run() {
            if (MainActivity.this.d && MainActivity.this.S0()) {
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
                        MainActivity.this.startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE").putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", BtDiscovery.v), MainActivity.i0);
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

    /**
     * 扩展实现：在 ScrollView 基础上补充 MainActivity 需要的局部行为。
     *
     * Extension of ScrollView adding the local behaviour MainActivity needs.
     */
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

        @Override 
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            super.onLayout(z, i, i2, i3, i4);
            int i5 = this.a;
            if (i5 >= 0) {
                scrollTo(0, i5);
                this.a = -1;
            }
        }
    }

    
    public  void A1() {
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

    
    public  void B1(AlertDialog alertDialog, EditText editText, String str, DialogInterface dialogInterface) {
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

    
    public  void C1(EditText editText, String str, AlertDialog alertDialog, View view) {
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
        if (!getSharedPreferences("carplayapk-local-transport", 0).edit().putString("phone_bluetooth_address", strA).commit()) {
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

    
    public  void D1(AlertDialog alertDialog, String str, View view) {
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

    
    public  void E1(String str, AlertDialog alertDialog, EditText editText, View view) {
        if (str != null) {
            alertDialog.dismiss();
            return;
        }
        if (WirelessCarPlayService.L1()) {
            o2("连接已启动，请先断开再修改。");
        } else {
            if (!getSharedPreferences("carplayapk-local-transport", 0).edit().remove("phone_bluetooth_address").commit()) {
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

    
    public  void F1(EditText editText, AlertDialog alertDialog, DialogInterface dialogInterface) {
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
        UiFactory.i(button, false);
        return button;
    }

    // [V1.8.26] G0 重载：直接接受 OnClickListener，便于 UI 重构中使用匿名点击处理。
    private Button G0(LinearLayout linearLayout, String str, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(str);
        button.setOnClickListener(listener);
        linearLayout.addView(button);
        UiFactory.i(button, false);
        return button;
    }

    
    public static  boolean G1(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    private void H0() {
        CarLifeBridge.d dVarF = CarLifeBridge.f(this);
        this.e = dVarF.g();
        String support = dVarF.h();
        if (!support.equals(lastComponentSupport)) {
            lastComponentSupport = support;
            AppLogger.g(this, support);
        }
    }
    public  void I1(SharedPreferences sharedPreferences, String str, String str2, DialogInterface dialogInterface, int i) {
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

    
    public  void J1(SharedPreferences sharedPreferences, DialogInterface dialogInterface, int i) {
        sharedPreferences.edit().remove("pending_address").remove("pending_fingerprint").remove("pending_certificate").remove("pending_current_wifi").apply();
        o2("已取消首次连接。");
    }

    public static EcdsaSignature.a K0(Context context) {
        return EcdsaSignature.a.c(new EcdsaSignature(), context);
    }

    
    public  void K1(DialogInterface dialogInterface) {
        this.f = false;
    }
    public  void L1(DialogInterface dialogInterface, int i) {
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

    
    public  void M1(DialogInterface dialogInterface) {
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
    private int O0(int i) {
        return (int) (i * getResources().getDisplayMetrics().density);
    }
    private void P0() {
        if (S0() || !this.d || isFinishing() || isDestroyed()) {
            return;
        }
        l2(true);
    }
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

    
    public  void Q1(DialogInterface dialogInterface) {
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

    
    public static  boolean R1(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    
    public boolean S0() {
        return n0.equals(getSharedPreferences(m0, 0).getString("accepted_revision", ""));
    }

    private void S1(String str) {
        if (!S0()) {
            P0();
            return;
        }
        long j = this.p;
        if (!M0(j)) {
            o2("当前 WLAN 已变化，请重新选择连接方式。");
            return;
        }
        H0();
        if (str.equals(i2())) {
            Stream<PairingSession.l> stream = this.r.stream();
            m3 m3Var = new m3(2);
            m3Var.b = str;
            VarHandle.storeStoreFence();
            if (!stream.noneMatch(m3Var) && h2() && this.e) {
                if (this.p == j && M0(j)) {
                    F0(str);
                    return;
                }
                o2("WLAN 或连接方式已变化，请重新点击连接。");
                return;
            }
        }
        o2("车机选择或连接条件已变化，请重新点击连接。");
    }

    private void T1() {
        if (!S0()) {
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

    
    public static  boolean V0(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    private void V1(String str) {
        try {
            startActivity(new Intent(str), ActivityOptions.makeBasic().setLaunchDisplayId(0).toBundle());
        } catch (RuntimeException unused) {
            o2("暂时无法打开系统设置，请从手机设置进入。");
        }
    }

    
    public  void W0(DialogInterface dialogInterface, int i) {
        g2();
    }

    
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

    
    public  void X0(DialogInterface dialogInterface, int i) {
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
    private boolean Y1(String str) {
        if (!S0()) {
            P0();
            return true;
        }
        if (this.d && !isFinishing() && !isDestroyed() && !WirelessCarPlayService.L1()) {
            if (io.carplay.app.prototype.BluetoothAdapterResolver.a(this, str).g() != null) {
                return false;
            }
            this.l = str;
            n2();
        }
        return true;
    }

    
    public static  boolean Z0(String str, PairingSession.l lVar) {
        return lVar.b.equals(str);
    }

    
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

    
    public  void a1() {
        startActivity(new Intent(this, (Class<?>) LogActivity.class));
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void a2() {
        SharedPreferences sp = getSharedPreferences(WirelessCarPlayService.b1, 0);
        boolean zL1 = WirelessCarPlayService.L1();
        String state = zL1 ? sp.getString("state", "CHECKING") : "STOPPED";
        String reason = zL1 ? sp.getString("reason", "") : "";
        boolean zS0 = S0();
        boolean zE = PairingPreparationService.e();

        StringBuilder detail = new StringBuilder();
        detail.append(zL1 ? StatusText.b(state, zL1, reason) : "本机尚未连接，点下方按钮开始。");
        if (zL1 && state.equals("WIFI") && !sp.getString("connection_route", "bluetooth").equals("bluetooth")) {
            detail.append("\n").append(sp.getString("connection_route", "").equals("retained_wifi")
                    ? "正在复用这台车仍然连接的 WLAN\n保持当前网络，正在重新发现并完整认证车机"
                    : "正在当前车机 WLAN 查找投屏服务\n保持当前网络，后续仍需完整认证");
        }
        if (zL1) {
            String nativeFg = StatusText.c(sp.getString("native_foreground_state", ""));
            if (!nativeFg.isEmpty()) detail.append("\n").append(nativeFg);
        }
        if (!this.e && !zL1) {
            detail.append("\n").append("当前手机组件暂不支持\n请打开使用教程查看兼容说明");
        }
        int nw = sp.getInt("native_width", 0), nh = sp.getInt("native_height", 0);
        int rw = sp.getInt("receiver_width", 0), rh = sp.getInt("receiver_height", 0);
        if (nw > 0 && nh > 0) {
            detail.append("\n").append((zL1 ? "画面尺寸" : "上次画面尺寸")).append("：").append(nw).append(" × ").append(nh);
        }
        if (!(nw <= 0 || nh <= 0 || !sp.getBoolean("native_exact_geometry_requested", false))) {
            int sw = sp.getInt("source_requested_width", 0);
            int sh = sp.getInt("source_requested_height", 0);
            detail.append("\n").append((nw == sw && nh == sh) ? "原生 Car+ 已返回目标画布尺寸"
                    : "原生 Car+ 返回的尺寸与请求不同：" + sw + " × " + sh);
        }
        String fit = sp.getString("fit_mode", "");
        if (fit.equals("fitted")) {
            detail.append("\n").append((zL1 ? "自动适配输出" : "上次适配输出")).append("：")
                    .append(sp.getInt("output_width", 0)).append(" × ").append(sp.getInt("output_height", 0))
                    .append(sp.getBoolean("fill_display", false) ? "（拉伸铺满）" : "（等比显示）");
        } else if (fit.equals("starting")) {
            detail.append("\n正在准备画面自动适配…");
        }

        String title;
        int stateColor;
        if (zL1) {
            title = StatusText.d(state, true);
            stateColor = 2;
        } else if (!this.e) {
            title = "当前手机组件暂不可用";
            stateColor = 3;
        } else if (!zS0) {
            title = "请先阅读使用说明";
            stateColor = 0;
        } else {
            title = "准备连接你的爱车";
            stateColor = 0;
        }

        String info;
        if (!this.e && !zL1) {
            info = "当前手机组件暂不支持，请打开使用教程查看兼容说明。";
        } else if (!zS0 && !zL1) {
            info = "请先阅读并同意使用说明。";
        } else if (zL1 && state.equals("FORWARDING_NATIVE")) {
            info = "短暂中断会自动尝试恢复，日志持续保存在本机。";
        } else {
            info = "";
        }

        String btnText;
        boolean btnEnabled;
        if (zL1) {
            btnText = "断开连接";
            btnEnabled = true;
        } else if (zE) {
            btnText = "停止等待";
            btnEnabled = true;
        } else if (!zS0 || !this.e) {
            btnText = "开始连接";
            btnEnabled = false;
        } else {
            btnText = "开始连接";
            btnEnabled = true;
        }

        if (homeScreen != null) {
            homeScreen.setConnectionStatus(title, detail.toString(), stateColor, btnText, btnEnabled);
            homeScreen.setAutoConnect(this.autoConnect);
        }
        renderStepsAndResolution(sp, zL1, state);
        if (homeScreen != null) {
            homeScreen.showInfo(zE && !zL1
                    ? (PairingPreparationService.f() ? "识别准备就绪\n请从车机搜索/配对手机，然后刷新列表、选择车机连接。准备最多两分钟。"
                        : "正在准备蓝牙识别信息…")
                    : info);
        }
        this.k = state;
    }

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
    private void c2() {
        // 旧的 RadioGroup 连接模式选择已移除，选择车机改由设置页「选择车机」处理。
    }

    
    public  void d1() {
        startActivity(new Intent(this, (Class<?>) HelpActivity.class));
    }

    
    public void d2() {
        if (!S0()) {
            P0();
        } else {
            this.p = 0L;
            e2();
        }
    }
    private void e2() {
        if (!S0()) {
            P0();
            return;
        }
        if (!this.d || WirelessCarPlayService.L1()) {
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
        // RECORD_AUDIO：音频通道需要 PCM 源（当前接麦克风验证链路，后续换系统内录）。
        // 不放进权限列表的话，startPcmCapture() 会因 granted=false 直接跳过，车机永远无声。
        String[] strArr = {b0, c0, "android.permission.RECORD_AUDIO"};
        for (int i = 0; i < 3; i++) {
            String str = strArr[i];
            if (checkSelfPermission(str) != 0) {
                arrayList.add(str);
            }
        }
        if (Build.VERSION.SDK_INT >= 37 && checkSelfPermission(d0) != 0) {
            arrayList.add(d0);
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

    /**
     * 连接前先申请系统内录授权。
     *
     * 音源必须是「手机里正在播放的声音」（Car+ / 音乐 App），麦克风只能录到环境声。
     * 对照 OcarPlay 0.8.53：其 APK 无 native 库、无 AudioRecord、无 MediaProjection，
     * manifest 也没有 RECORD_AUDIO，PCM 由 ColorOS 宿主 native 层直接喂入（日志
     * "Actual native PCM entered wireless sender"）。该通路只对宿主内应用开放，
     * 第三方应用只能用公开 API 的等价方案：AudioPlaybackCapture + MediaProjection。
     */
    private void a4(String str) {
        if (android.os.Build.VERSION.SDK_INT >= 29 && !MediaProjectionHolder.ready()) {
            try {
                android.media.projection.MediaProjectionManager manager =
                        (android.media.projection.MediaProjectionManager)
                                getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE);
                if (manager != null) {
                    this.pendingConnectTarget = str;
                    startActivityForResult(manager.createScreenCaptureIntent(), MEDIA_PROJECTION_REQUEST_CODE);
                    o2("请在弹窗中选择「立即开始」，让车机播放手机内的声音；拒绝则回退麦克风。");
                    return;
                }
            } catch (Throwable failure) {
                AppLogger.g(this, "MEDIA_PROJECTION_REQUEST_FAILED msg=" + failure.getMessage());
            }
        }
        v2(str);
    }

    public void f2() {
        if (!S0()) {
            P0();
            return;
        }
        if (!this.d || WirelessCarPlayService.L1()) {
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
    private boolean h2() {
        if (checkSelfPermission(b0) == 0 && checkSelfPermission(c0) == 0) {
            return Build.VERSION.SDK_INT < 37 || checkSelfPermission(d0) == 0;
        }
        return false;
    }

    
    public String i2() {
        int i = this.q;
        if (i < 0 || i >= this.r.size()) {
            return null;
        }
        return this.r.get(this.q).b;
    }

    
    public  void j1() {
        if (WirelessCarPlayService.L1()) {
            startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.a1));
            o2("已请求恢复声音，请在手机播放音乐；画面连接保持。");
        }
    }

    
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

    
    public  void k1() {
        startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.Y0));
    }

    
    public void k2() {
        l2(!S0());
    }

    
    public  void l1() {
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
            if (z) {
                o3Var = new o3(23);
                o3Var.b = this;
                VarHandle.storeStoreFence();
            }
            AlertDialog alertDialogK = UiFactory.k(this, "免责声明与使用说明", "使用前请仔细阅读。点击“已阅读并同意”表示你已理解以下说明；不同意可退出，之后再次打开仍可阅读。\n\n一、独立项目\nCarPlayAPK 是独立的第三方车联工具，并非苹果、手机厂商或汽车厂商的官方产品，也不代表与相关品牌存在授权、认可或合作关系。相关名称与标识归其权利人所有。\n\n二、驾驶安全\n请仅在安全停车后完成安装、配对、设置、排障和日志导出。驾驶中遵守当地法律及交通规则，勿操作手机或观看与驾驶无关的内容。乘员也应避免影响驾驶。不得用本软件替代原车安全提示、仪表、驾驶辅助或紧急功能。\n\n三、兼容与使用风险\n功能依赖手机系统、原生 Car+ 组件、车机能力及连接环境，无法保证所有设备、所有功能或持续稳定连接。系统更新可能改变兼容性。可能出现延迟、无声、卡顿、断连、发热及功耗增加；请按实际情况决定是否继续使用。手机端发送帧率不等于车机实际显示帧率。\n\n原生 Car+ 自带录屏的兼容性仍在验证中，首次录屏相关断开、零画面或车机重启等问题的实际原因与效果未全部确认。出现异常请在安全条件下断开连接，必要时停用本软件并使用原车功能。不得仅凭本机测试认为实车已全面验证。\n\n四、数据与隐私\n连接日志自动保存在本机，不自动上传；可能包含设备名称、网络名称及连接阶段。导出位置由你选择。对外分享前请检查并遮盖个人或设备信息。覆盖升级保留本机记录；卸载、清除应用数据或系统清理可能使记录丢失，请自行保管已导出文件。\n\n五、第三方与再分发\n手机系统、原生组件、车机、其他应用及非官方修改包的行为不由本软件作者控制。请使用可信来源的安装包，遵守第三方服务条款与知识产权要求。修改、转售或再分发者应对其修改与承诺负责，不得冒用作者或官方名义作兼容、安全或售后保证。\n\n六、责任范围\n在法律允许范围内，因用户违法或违规操作、第三方改包等非作者原因造成的后果，由相关责任方依法承担，作者不对此作额外保证。本声明不构成对全部设备、全部使用场景或特定目的适用性的承诺。依法不得排除或限制的责任不受本声明影响，也不限制你依法享有的权利。", str, b4Var, str2, o3Var);
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

    
    public  void m1(RadioGroup radioGroup, int i) {
        // 旧的 RadioGroup 连接模式选择已移除。
    }

    
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

    
    public  void n1() {
        this.g = false;
        this.h = false;
        this.j = null;
        this.autoTriggered = false;
        w2();
        startService(new Intent(this, (Class<?>) WirelessCarPlayService.class).setAction(WirelessCarPlayService.W0));
        o2("已断开连接。下次使用时点击连接车机。");
    }

    
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
        UiFactory.m(linearLayout, "本机蓝牙标识", "bluetooth");
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
        editText.setText(io.carplay.app.prototype.BluetoothAdapterResolver.b(this));
        editText.setPadding(O0(12), O0(12), O0(12), O0(12));
        GradientDrawable gradientDrawableB = UiFactory.B(this, -854534, 12);
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

    
    public  void o1() {
        i1(1);
    }

    
    public void o2(String str) {
        if (str != null && !str.isEmpty()) {
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
        AppLogger.g(this, "USER_NOTICE ".concat(str == null ? "" : str));
    }

    
    public  void p1() {
        V1("android.settings.BLUETOOTH_SETTINGS");
    }

    
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

    
    public  void q1(CompoundButton compoundButton, boolean z) {
        getSharedPreferences("wireless-ui", 0).edit().putBoolean("auto_fit", z).apply();
        a2();
    }
    public static  boolean r1(PairingSession.l lVar, PairingSession.l lVar2) {
        return lVar2.a.equals(lVar.a);
    }
    public  void s1(DialogInterface dialogInterface, int i) {
        V1("android.settings.WIFI_SETTINGS");
    }

    
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

    
    public  void t1(DialogInterface dialogInterface, int i) {
        Network networkY2 = y2();
        if (networkY2 == null) {
            o2("请连接车机 WLAN，并关闭其他并行 WLAN 连接后再试。");
        } else {
            this.p = networkY2.getNetworkHandle();
            e2();
        }
    }

    
    
    public void i1(int i) {
        currentPage = Math.max(0, Math.min(2, i));
        if (pager != null) pager.setCurrentItem(currentPage);
        updateTabHighlight();
    }

    private void updateTabHighlight() {
        if (tabButtons == null) return;
        for (int i = 0; i < tabButtons.length; i++) {
            boolean sel = i == currentPage;
            tabButtons[i].setTextColor(sel ? Ui.f1241i : Ui.f1244l);
            tabButtons[i].setTypeface(sel ? Ui.B : Ui.C);
        }
    }

    
    public  void u1(AlertDialog alertDialog, DialogInterface dialogInterface) {
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

    
    public  void v1(DialogInterface dialogInterface, int i) {
        this.q = i;
        z2();
        dialogInterface.dismiss();
    }

    public static void v170NameDialog(MainActivity mainActivity) {
        LinearLayout linearLayout = new LinearLayout(mainActivity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(mainActivity.O0(20), mainActivity.O0(18), mainActivity.O0(20), mainActivity.O0(16));
        UiFactory.m(linearLayout, "蓝牙名称", "bluetooth");
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
        GradientDrawable gradientDrawableB = UiFactory.B(mainActivity, -854534, 12);
        gradientDrawableB.setStroke(mainActivity.O0(1), -2300431);
        editText.setBackground(gradientDrawableB);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = mainActivity.O0(14);
        layoutParams.bottomMargin = mainActivity.O0(12);
        linearLayout.addView(editText, layoutParams);
        AlertDialog.Builder view = new AlertDialog.Builder(mainActivity).setView(linearLayout);
        BluetoothSaveListener bluetoothSaveListener = new BluetoothSaveListener();
        bluetoothSaveListener.a = mainActivity;
        bluetoothSaveListener.b = editText;
        VarHandle.storeStoreFence();
        view.setPositiveButton("保存", bluetoothSaveListener).setNegativeButton("取消", (DialogInterface.OnClickListener) null).create().show();
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

    
    public  void w1(boolean z) {
        if (z) {
            if (getSharedPreferences(m0, 0).edit().putString("accepted_revision", n0).commit()) {
                AppLogger.g(this, "USER_DISCLAIMER_ACCEPTED revision=2026-10-20-v1");
                E0();
            } else {
                Toast.makeText(this, "无法保存确认，请重新打开后重试。", 1).show();
                finish();
            }
        }
    }

    
    public void w2() {
        this.a = false;
        stopService(new Intent(this, (Class<?>) PairingPreparationService.class));
    }

    
    public  void x1() {
        this.g = false;
        this.h = false;
        this.j = null;
        finish();
    }

    private TextView x2(LinearLayout linearLayout, String str, int i) {
        return UiFactory.C(linearLayout, str, i, false);
    }

    
    public  void y1(boolean z, DialogInterface dialogInterface) {
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

    
    public  void z1(String str, DialogInterface dialogInterface, int i) {
        getSharedPreferences("wireless-ui", 0).edit().putBoolean(ProtoMessageBase.e("fill_display_", str), i == 1).apply();
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
        if (settingsScreen != null) settingsScreen.setSelectedCar(strX1);
    }

    @Override 
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == MEDIA_PROJECTION_REQUEST_CODE) {
            // 无论授权与否都要继续连接：授权成功走 AudioPlaybackCapture（手机媒体音频），
            // 拒绝则回退麦克风，投屏本身不受影响。
            String target = this.pendingConnectTarget;
            this.pendingConnectTarget = null;
            if (i2 == -1 && intent != null) {
                MediaProjectionHolder.set(i2, intent);
                AppLogger.g(this, "MEDIA_PROJECTION_GRANTED audio_source=playback_capture");
                o2("已获得内录授权，音频将传输手机内播放的声音。");
            } else {
                AppLogger.g(this, "MEDIA_PROJECTION_DENIED audio_source=mic_fallback");
                o2("未授权内录，音频将回退为麦克风；如需音乐请在下次连接时允许。");
            }
            if (target != null) {
                v2(target);
            }
            return;
        }
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

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SharedPreferences dbgPrefs = getSharedPreferences("carplayapk-debug", 0);
        this.logEnabled = dbgPrefs.getBoolean("log_enabled", true);
        this.debugMode = dbgPrefs.getBoolean("debug_mode", false);
        this.autoConnect = dbgPrefs.getBoolean("auto_connect_launch", false);
        AppLogger.setRecordingEnabled(this.logEnabled);

        this.g = bundle != null && bundle.getBoolean("continue_source_intent", false);
        this.a = bundle != null && bundle.getBoolean("pairing_visibility_pending", false);
        this.l = bundle == null ? null : bundle.getString("identity_connect_address");
        this.p = bundle != null ? bundle.getLong("current_wifi_handle", 0L) : 0L;
        this.E = bundle != null && bundle.getBoolean("home_manual_route", false);

        // 应用主题（跟随系统）
        Ui.c(getApplicationContext(), 0);

        // 三屏（移植自参考 APK 的 HomeScreen / SettingsScreen / AboutScreen 布局）
        homeScreen = new HomeScreenUi(this, this);
        settingsScreen = new SettingsScreenUi(this, this);
        aboutScreen = new AboutScreenUi(this, this);

        // 横向翻页容器
        pager = new Pager(this);
        pager.addView(homeScreen, new ViewGroup.LayoutParams(-1, -1));
        pager.addView(settingsScreen, new ViewGroup.LayoutParams(-1, -1));
        pager.addView(aboutScreen, new ViewGroup.LayoutParams(-1, -1));
        pager.setOnPageChangeListener(new Pager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int index) {
                currentPage = index;
                updateTabHighlight();
            }
        });

        // 顶部标签栏
        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(Ui.f1234b);
        int tabH = Ui.h(this, 52.0f);
        tabBar.setPadding(Ui.h(this, 12.0f), 0, Ui.h(this, 12.0f), 0);
        String[] tabs = {"主页", "设置", "关于"};
        tabButtons = new Button[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            Button tb = new Button(this);
            tb.setText(tabs[i]);
            tb.setAllCaps(false);
            tb.setBackground(null);
            tb.setMinHeight(0);
            tb.setMinimumHeight(0);
            tb.setTextSize(15.0f);
            tb.setTypeface(i == 0 ? Ui.B : Ui.C);
            tb.setTextColor(i == 0 ? Ui.f1241i : Ui.f1244l);
            tb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    i1(idx);
                }
            });
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, tabH, 1.0f);
            tlp.gravity = 16;
            tabBar.addView(tb, tlp);
            tabButtons[i] = tb;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        root.addView(tabBar, new LinearLayout.LayoutParams(-1, -2));
        root.addView(pager, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        setContentView(root);
        Ui.k(root);

        H0();
        if (S0()) {
            T1();
        }
        a2();
        // [UI 重构] 启动后自动连接：已开启且记住车机，延迟触发一次
        if (S0() && this.autoConnect && i2() != null && !WirelessCarPlayService.L1() && !this.autoTriggered) {
            this.autoTriggered = true;
            this.c.postDelayed(new Runnable() {
                @Override public void run() { startConnectFromHome(); }
            }, 600L);
        }
        AppLogger.g(this, ProtoMessageBase.e("USER_UI_OPEN version=", A2(this)));
    }

    public void startConnectFromHome() {
        if (!S0()) { P0(); return; }
        if (WirelessCarPlayService.L1()) { o2("连接已在进行中。"); return; }
        if (i2() == null) {
            o2("请先选择已配对的车机，再开始连接。");
            j2();
            return;
        }
        this.E = false;
        e2();
    }

    /**
     * 渲染 7 段步骤高亮 + 投屏分辨率（要求 2/4）。
     */
    private void renderStepsAndResolution(SharedPreferences sp, boolean connected, String state) {
        int nw = sp.getInt("native_width", 0);
        int nh = sp.getInt("native_height", 0);
        int rw = sp.getInt("receiver_width", 0);
        int rh = sp.getInt("receiver_height", 0);
        if (homeScreen != null) {
            homeScreen.setResolution(nw > 0 && nh > 0 ? (nw + " × " + nh) : "—",
                                    rw > 0 && rh > 0 ? (rw + " × " + rh) : "—");
        }
        int cur = 1;
        if (connected) {
            if (state.equals("FORWARDING_NATIVE") || state.equals("CAR_SCREEN_IN_USE") || state.equals("READY_FOR_CARPLUS") || state.equals("RESUMING_NATIVE")) {
                cur = 7;
            } else if (state.equals("WAITING_CARPLAY_ENDPOINT") || state.equals("NATIVE_SOURCE_ARMED") || state.equals("WAITING_NATIVE_VIDEO") || state.equals("VIDEO_OUTPUT_UNAVAILABLE")) {
                cur = 6;
            } else if (state.equals("WAITING_ACCESSORY_START") || state.equals("PAIRING") || state.equals("TRUST_REQUIRED")) {
                cur = 5;
            } else if (state.equals("WIFI")) {
                cur = 3;
            } else {
                cur = 4;
            }
        } else if (state.equals("WIFI")) {
            cur = 3;
        } else if (state.contains("AUTH") || state.contains("CERT") || state.contains("PAIR")) {
            cur = 2;
        } else {
            cur = 1;
        }
        if (homeScreen != null) homeScreen.setStep(cur);
    }

    private void clearLogs() {
        AppLogger.clear(this);
        o2("已清除本地历史日志文件。");
        AppLogger.g(this, "USER_LOG_CLEARED");
    }

    // ===== ScreenActions 接口实现 =====

    @Override
    public void connect() {
        if (WirelessCarPlayService.L1()) {
            startService(new Intent(this, WirelessCarPlayService.class).setAction(WirelessCarPlayService.W0));
            o2("正在断开连接…");
        } else {
            startConnectFromHome();
        }
    }

    @Override
    public void setAutoConnect(boolean on) {
        this.autoConnect = on;
        getSharedPreferences("carplayapk-debug", 0).edit().putBoolean("auto_connect_launch", on).apply();
        if (homeScreen != null) homeScreen.setAutoConnect(on);
        o2(on ? "已开启自动连接，下次启动应用会自动连接车机。" : "已关闭自动连接。");
    }

    @Override
    public void pickResolution() {
        SharedPreferences sp = getSharedPreferences(WirelessCarPlayService.b1, 0);
        int nw = sp.getInt("native_width", 0), nh = sp.getInt("native_height", 0);
        int rw = sp.getInt("receiver_width", 0), rh = sp.getInt("receiver_height", 0);
        String msg = "投屏画面：" + (nw > 0 ? nw + " × " + nh : "—") + "\n手机屏幕：" + (rw > 0 ? rw + " × " + rh : "—");
        new AlertDialog.Builder(this).setTitle("投屏分辨率").setMessage(msg)
                .setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
    }

    @Override
    public void setOrientation(int which) {
        try {
            setRequestedOrientation(which == 1 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            o2(which == 1 ? "已切换为竖屏" : "已切换为横屏");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setFitMode(int which) {
        getSharedPreferences("wireless-ui", 0).edit().putInt("fit", which == 1 ? 1 : 0).apply();
        o2(which == 1 ? "画面适配：拉伸铺满" : "画面适配：等比显示");
    }

    @Override
    public void setTheme(int which) {
        Ui.c(getApplicationContext(), which);
        recreate();
    }

    @Override
    public void selectCar() {
        if (!S0()) { P0(); return; }
        T1();
        if (this.r.isEmpty()) {
            o2("还没有已配对的车机，请先点「蓝牙配对」。");
            j2();
            return;
        }
        String[] items = new String[this.r.size()];
        for (int i = 0; i < this.r.size(); i++) items[i] = X1(i);
        int checked = (this.q >= 0 && this.q < this.r.size()) ? this.q : 0;
        new AlertDialog.Builder(this).setTitle("选择车机").setSingleChoiceItems(items, checked,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.q = which;
                        String addr = MainActivity.this.r.get(which).b;
                        getSharedPreferences("wireless-ui", 0).edit().putString("selected_address", addr).apply();
                        dialog.dismiss();
                        z2();
                        a2();
                        o2("已选择：" + X1(which));
                    }
                }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    @Override
    public void setDebugMode(boolean on) {
        this.debugMode = on;
        getSharedPreferences("carplayapk-debug", 0).edit().putBoolean("debug_mode", on).apply();
        if (settingsScreen != null) settingsScreen.setDebugMode(on);
        o2(on ? "已开启调试模式。" : "已关闭调试模式。");
    }

    @Override
    public void openTutorial() { d1(); }

    @Override
    public void openDeclaration() { k2(); }

    @Override
    public void bluetoothPair() { j2(); }

    @Override
    public void localBtIdentity() { n2(); }

    @Override
    public void openLog() { a1(); }

    public void openGit() {
        try {
            startActivity(new Intent("android.intent.action.VIEW", android.net.Uri.parse("https://github.com/jsntsch/CarplayAPK")));
        } catch (RuntimeException unused) {
            o2("无法打开浏览器，请手动访问 https://github.com/jsntsch/CarplayAPK");
        }
    }

    @Override 
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
        super.onDestroy();
    }

    @Override 
    public void onPause() {
        this.d = false;
        this.c.removeCallbacks(this.Y);
        N0();
        super.onPause();
    }

    @Override 
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
                // 音源需要手机媒体音频（Car+ / 音乐 App），先取 MediaProjection 授权再连。
                a4(str);
            }
        }
    }

    @Override 
    public void onResume() {
        super.onResume();
        // App 一到前台就广播 _carplay-ctrl，让车机从自己那一侧发起时也能发现本机。
        CarPlayCtrlAdvertiser.StartEarly(this);
        // [V1.8.25] 前台时确保常驻自动重连服务在跑（开机广播之外的一层保险）。
        CarPlayAutoReconnectService.ensure(this, "APP_FOREGROUND");
        // 同时浏览车机侧的服务类型，用于判断车机走的是 _carplay / _carplay-ctrl / _airplay 哪条通道。
        CarPlayCtrlAdvertiser.Probe(this);
        this.d = true;
        if (S0()) {
            E0();
        } else {
            P0();
        }
        // [V1.8.26] 自动连接：启动后按流程连接车机（仅触发一次，断开后重置）
        if (this.autoConnect && !this.autoTriggered && i2() != null && !WirelessCarPlayService.L1()) {
            this.autoTriggered = true;
            startConnectFromHome();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("continue_source_intent", this.g);
        bundle.putBoolean("pairing_visibility_pending", this.a);
        bundle.putString("identity_connect_address", this.l);
        bundle.putLong("current_wifi_handle", this.p);
        bundle.putBoolean("home_manual_route", this.E);
        bundle.putInt("workspace_page", currentPage);
        super.onSaveInstanceState(bundle);
    }
}
