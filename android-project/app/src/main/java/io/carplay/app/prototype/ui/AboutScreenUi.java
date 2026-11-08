package io.carplay.app.prototype.ui;

import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 关于页（移植自参考 APK 的 AboutScreen 布局）。
 * 结构：标题 → 版 本(版本/构建/设备/系统) → 关 注(GitHub) → 帮 助 与 支 持(使用教程/免责声明)。
 * 已按需求去掉抖音/小红书/酷安，删除底部说明文字；帮助与支持区已恢复。
 */
public final class AboutScreenUi extends ScrollView {

    public AboutScreenUi(Context context, ScreenActions.About actions) {
        super(context);
        A.e("c", context);

        setBackground(Ui.d(0.0f, Ui.f1234b, context));
        setFillViewport(true);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int padX = Ui.h(context, 16.0f);
        root.setPadding(padX, Ui.h(context, 16.0f), padX, Ui.h(context, 120.0f));
        addView(root, new LinearLayout.LayoutParams(-1, -2));

        root.addView(Ui.l(context, "关于", "OPPO Car+ 转 CarPlay · 无线投屏工具"));

        // 版 本
        Ui.p(root, context, "版 本");
        LinearLayout verCard = Ui.e(context);
        verCard.addView(Ui.o(context, "版本", null, appVersionName(context), false, null, null).view);
        Ui.g(verCard, context);
        verCard.addView(Ui.o(context, "构建", null, "build " + appVersionCode(context), false, null, null).view);
        Ui.g(verCard, context);
        verCard.addView(Ui.o(context, "设备", null, Build.MANUFACTURER + " " + Build.MODEL, false, null, null).view);
        Ui.g(verCard, context);
        verCard.addView(Ui.o(context, "系统", null, "Android " + Build.VERSION.RELEASE + "（API " + Build.VERSION.SDK_INT + "）", false, null, null).view);
        LinearLayout.LayoutParams verLp = new LinearLayout.LayoutParams(-1, -2);
        verLp.topMargin = Ui.h(context, 2.0f);
        root.addView(verCard, verLp);

        // 关 注
        Ui.p(root, context, "关 注");
        LinearLayout followCard = Ui.e(context);
        M3Icon gh = new M3Icon(context, 2, true, Ui.f1241i);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(Ui.h(context, 24.0f), Ui.h(context, 24.0f));
        iconLp.rightMargin = Ui.h(context, 14.0f);
        followCard.addView(Ui.o(context, "GitHub", "jsntsch/CarplayAPK", null, true, gh, new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                actions.openGit();
            }
        }).view);
        LinearLayout.LayoutParams followLp = new LinearLayout.LayoutParams(-1, -2);
        followLp.topMargin = Ui.h(context, 2.0f);
        root.addView(followCard, followLp);

        // 帮 助 与 支 持
        Ui.p(root, context, "帮助与支持");
        LinearLayout helpCard = Ui.e(context);
        helpCard.addView(Ui.o(context, "使用教程", null, null, true, null, new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                actions.openTutorial();
            }
        }).view);
        Ui.g(helpCard, context);
        helpCard.addView(Ui.o(context, "免责声明", null, null, true, null, new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                actions.openDeclaration();
            }
        }).view);
        LinearLayout.LayoutParams helpLp = new LinearLayout.LayoutParams(-1, -2);
        helpLp.topMargin = Ui.h(context, 2.0f);
        root.addView(helpCard, helpLp);
    }

    private static String appVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName != null ? pi.versionName : "";
        } catch (Throwable th) {
            return "";
        }
    }

    private static String appVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(pi.getLongVersionCode());
        } catch (Throwable th) {
            return "";
        }
    }
}
