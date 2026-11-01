package io.carplay.app.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicBoolean;

/* JADX INFO: loaded from: classes.dex */
final class j0 {
    static final String a = "如果这个软件帮到了你，欢迎自愿赞助支持开发。赞助完全自愿，不影响任何功能使用。";
    static final String b = "自愿赞助\n不影响功能";
    private static final int c = -7256049;

    private j0() {
    }

    public static Button f(Activity activity, LinearLayout linearLayout) {
        Button button = new Button(activity);
        button.setVisibility(8);
        return button;
    }

    public static Button g(Activity activity, LinearLayout linearLayout) {
        Button button = new Button(activity);
        button.setVisibility(8);
        return button;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void i(AtomicBoolean atomicBoolean, Button button, TextView textView, Activity activity, int i, AlertDialog alertDialog, View view) {
        int i2 = 0;
        if (atomicBoolean.compareAndSet(false, true)) {
            button.setEnabled(false);
            textView.setText("正在保存原图…");
            i0 i0Var = new i0(i2);
            i0Var.c = activity;
            i0Var.b = i;
            i0Var.d = atomicBoolean;
            i0Var.e = alertDialog;
            i0Var.f = button;
            i0Var.g = textView;
            VarHandle.storeStoreFence();
            Thread thread = new Thread(i0Var, "OcarPlay-sponsor-export");
            thread.setDaemon(true);
            thread.start();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void j(Activity activity, int i, AtomicBoolean atomicBoolean, AlertDialog alertDialog, Button button, TextView textView) {
        boolean zM = m(activity, i);
        e0 e0Var = new e0();
        e0Var.a = atomicBoolean;
        e0Var.b = activity;
        e0Var.c = alertDialog;
        e0Var.d = button;
        e0Var.e = textView;
        e0Var.f = zM;
        VarHandle.storeStoreFence();
        activity.runOnUiThread(e0Var);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void k(AtomicBoolean atomicBoolean, Activity activity, AlertDialog alertDialog, Button button, TextView textView, boolean z) {
        atomicBoolean.set(false);
        if (activity.isDestroyed() || !alertDialog.isShowing()) {
            return;
        }
        button.setEnabled(true);
        textView.setText(z ? "已保存到相册 Pictures/OcarPlay，可在支付宝扫一扫中从相册选择。" : "未能保存，请重试；也可使用另一台设备扫描赞助码。");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void l(AlertDialog alertDialog, Activity activity, DialogInterface dialogInterface) {
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(m0.B(activity, -1, 24));
            alertDialog.getWindow().setLayout(Math.min(activity.getResources().getDisplayMetrics().widthPixels - m0.l(activity, 28), m0.l(activity, 540)), -2);
        }
    }

    private static boolean m(Activity activity, int i) {
        return false;
    }

    public static AlertDialog n(Activity activity) {
        return null;
    }
}
