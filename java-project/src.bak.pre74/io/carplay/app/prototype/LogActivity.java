package io.carplay.app.prototype;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import io.carplay.app.secured.b2;
import io.carplay.app.secured.d1;
import io.carplay.app.secured.h3;
import io.carplay.app.secured.i3;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public final class LogActivity extends Activity {
    private static final int e = 81;
    private final ExecutorService a = Executors.newSingleThreadExecutor();
    private TextView b;
    private Button c;
    private Button d;

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void h() {
        String strD = AppLogger.d(this);
        int iIndexOf = strD.indexOf("\n===== 历史 HISTORY");
        if (iIndexOf >= 0) {
            strD = strD.substring(0, iIndexOf);
        }
        if (strD.length() > 24000) {
            strD = "（下方仅显示最新记录，导出包含完整历史）\n".concat(strD.substring(strD.length() - 24000));
        }
        h3 h3Var = new h3(1);
        h3Var.b = this;
        h3Var.c = strD;
        VarHandle.storeStoreFence();
        runOnUiThread(h3Var);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void i(String str) {
        if (isDestroyed()) {
            return;
        }
        this.b.setText(str);
        this.c.setEnabled(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void j(Uri uri) {
        String str;
        try {
            OutputStream outputStreamOpenOutputStream = getContentResolver().openOutputStream(uri, "wt");
            try {
                if (outputStreamOpenOutputStream == null) {
                    throw new IOException("No output stream");
                }
                outputStreamOpenOutputStream.write(b2.b(AppLogger.d(this)).getBytes(StandardCharsets.UTF_8));
                outputStreamOpenOutputStream.flush();
                str = "日志文件已保存";
                AppLogger.g(this, "USER_LOG_EXPORT_COMPLETE");
                outputStreamOpenOutputStream.close();
                h3 h3Var = new h3(0);
                h3Var.b = this;
                h3Var.c = str;
                VarHandle.storeStoreFence();
                runOnUiThread(h3Var);
            } catch (Throwable th) {
                if (outputStreamOpenOutputStream != null) {
                    try {
                        outputStreamOpenOutputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Exception e2) {
            AppLogger.g(this, "USER_LOG_EXPORT_FAILED type=".concat(e2.getClass().getSimpleName()));
            str = "日志保存失败，请换一个保存位置重试";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void k(String str) {
        if (isDestroyed()) {
            return;
        }
        this.d.setEnabled(true);
        this.d.setText("导出完整日志文件");
        p(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void l(View view) {
        try {
            startActivityForResult(new Intent("android.intent.action.CREATE_DOCUMENT").addCategory("android.intent.category.OPENABLE").setType("text/plain").putExtra("android.intent.extra.TITLE", "OcarPlay-log-" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date()) + ".txt"), e);
        } catch (RuntimeException unused) {
            p("没有可用的文件保存程序，请稍后重试。");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void m(View view) {
        o();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void n(View view) {
        finish();
    }

    private void o() {
        this.c.setEnabled(false);
        this.b.setText("正在读取本机日志…");
        ExecutorService executorService = this.a;
        d1 d1Var = new d1(4);
        d1Var.b = this;
        VarHandle.storeStoreFence();
        executorService.execute(d1Var);
    }

    private void p(String str) {
        Toast.makeText(this, str, 1).show();
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != e || i2 != -1 || intent == null || intent.getData() == null) {
            return;
        }
        Uri data = intent.getData();
        this.d.setEnabled(false);
        this.d.setText("正在保存…");
        ExecutorService executorService = this.a;
        io.carplay.app.secured.p0 p0Var = new io.carplay.app.secured.p0(2);
        p0Var.b = this;
        p0Var.c = data;
        VarHandle.storeStoreFence();
        executorService.execute(p0Var);
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout linearLayoutX = UiFactory.x(this);
        UiFactory.D(this, linearLayoutX, "连接日志", "每次连接都有记录，排查问题更清楚");
        LinearLayout linearLayoutJ = UiFactory.j(linearLayoutX);
        UiFactory.m(linearLayoutJ, "记录留在你的手机", "shield");
        UiFactory.C(linearLayoutJ, "自动保存，退出后保留。最近 5 个文件；导出包含完整历史，由你选择保存位置。分享前请检查设备与网络隐私。", 13, false).setTextColor(-10193265);
        Button button = new Button(this);
        this.d = button;
        button.setText("导出完整日志文件");
        Button button2 = this.d;
        i3 i3Var = new i3(0);
        i3Var.b = this;
        VarHandle.storeStoreFence();
        button2.setOnClickListener(i3Var);
        linearLayoutX.addView(this.d);
        UiFactory.i(this.d, true);
        LinearLayout linearLayoutZ = UiFactory.z(linearLayoutX);
        Button button3 = new Button(this);
        this.c = button3;
        button3.setText("刷新日志");
        Button button4 = this.c;
        i3 i3Var2 = new i3(1);
        i3Var2.b = this;
        VarHandle.storeStoreFence();
        button4.setOnClickListener(i3Var2);
        linearLayoutZ.addView(this.c);
        UiFactory.i(this.c, false);
        Button button5 = new Button(this);
        button5.setText("返回首页");
        i3 i3Var3 = new i3(2);
        i3Var3.b = this;
        VarHandle.storeStoreFence();
        button5.setOnClickListener(i3Var3);
        linearLayoutZ.addView(button5);
        UiFactory.i(button5, false);
        TextView textView = new TextView(this);
        this.b = textView;
        textView.setTextIsSelectable(true);
        this.b.setTextColor(-2299910);
        this.b.setTextSize(12.0f);
        this.b.setLineSpacing(UiFactory.l(this, 3), 1.0f);
        this.b.setTypeface(Typeface.MONOSPACE);
        this.b.setPadding(UiFactory.l(this, 16), UiFactory.l(this, 16), UiFactory.l(this, 16), UiFactory.l(this, 16));
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackground(UiFactory.B(this, -15260095, 18));
        scrollView.addView(this.b);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 0, 1.0f);
        layoutParams.topMargin = UiFactory.l(this, 16);
        linearLayoutX.addView(scrollView, layoutParams);
        setContentView(linearLayoutX);
        UiFactory.w(this);
        o();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        this.a.shutdown();
        super.onDestroy();
    }
}
