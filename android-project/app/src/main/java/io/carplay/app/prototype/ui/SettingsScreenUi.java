package io.carplay.app.prototype.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * 设置页（移植自参考 APK 的 SettingsScreen 布局）。
 * 结构：标题 → 外 观(主题) → 连 接(蓝牙配对 / 本地蓝牙标识)
 *      → 调 试(调试模式 / 运行日志) → 教 程(使用教程 / 免责声明)。
 */
public final class SettingsScreenUi extends ScrollView {

    private final UiHolder themeRow;
    private final UiHolder carRow;
    private final UiHolder btPairRow;
    private final UiHolder localBtRow;
    private final UiHolder debugRow;
    private final UiHolder logRow;
    private final UiHolder tutorialRow;
    private final UiHolder declarationRow;

    public SettingsScreenUi(Context context, ScreenActions.Settings actions) {
        super(context);
        A.e("c", context);

        setBackground(Ui.d(0.0f, Ui.f1234b, context));
        setFillViewport(true);
        setVerticalScrollBarEnabled(false);
        setClipToPadding(false);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int padX = Ui.h(context, 18.0f);
        root.setPadding(padX, Ui.h(context, 8.0f), padX, Ui.h(context, 88.0f));
        addView(root, new LinearLayout.LayoutParams(-1, -2));

        root.addView(Ui.l(context, "设置", "外观 · 连接 · 调试 · 教程"));

        // 外 观
        Ui.p(root, context, "外 观");
        LinearLayout appearance = Ui.e(context);
        themeRow = Ui.o(context, "主题", null, "跟随系统", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ui.i((android.app.Activity) getContext(), v,
                        new String[]{"跟随系统", "亮色", "暗色"},
                        currentTheme(), new OnSelect() {
                            @Override
                            public void onSelected(int index) {
                                actions.setTheme(index);
                            }
                        });
            }
        });
        appearance.addView(themeRow.view);
        LinearLayout.LayoutParams appearanceLp = new LinearLayout.LayoutParams(-1, -2);
        appearanceLp.topMargin = Ui.h(context, 2.0f);
        root.addView(appearance, appearanceLp);

        // 连 接
        Ui.p(root, context, "连 接");
        LinearLayout conn = Ui.e(context);
        carRow = Ui.o(context, "选择车机", "已记住的车机将自动连接", "未选择", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.selectCar();
            }
        });
        btPairRow = Ui.o(context, "蓝牙配对", "搜索并选择车机蓝牙", "未配对", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.bluetoothPair();
            }
        });
        localBtRow = Ui.o(context, "本地蓝牙标识", "手机蓝牙地址 / 名称", "未设置", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.localBtIdentity();
            }
        });
        conn.addView(carRow.view);
        Ui.g(conn, context);
        conn.addView(btPairRow.view);
        Ui.g(conn, context);
        conn.addView(localBtRow.view);
        LinearLayout.LayoutParams connLp = new LinearLayout.LayoutParams(-1, -2);
        connLp.topMargin = Ui.h(context, 2.0f);
        root.addView(conn, connLp);

        // 调 试
        Ui.p(root, context, "调 试");
        LinearLayout debug = Ui.e(context);
        debugRow = Ui.q(context, "调试模式", false, new OnToggle() {
            @Override
            public void onChanged(boolean checked) {
                actions.setDebugMode(checked);
            }
        });
        logRow = Ui.o(context, "运行日志", null, null, true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.openLog();
            }
        });
        debug.addView(debugRow.view);
        Ui.g(debug, context);
        debug.addView(logRow.view);
        LinearLayout.LayoutParams debugLp = new LinearLayout.LayoutParams(-1, -2);
        debugLp.topMargin = Ui.h(context, 2.0f);
        root.addView(debug, debugLp);

        // 教 程
        Ui.p(root, context, "教 程");
        LinearLayout help = Ui.e(context);
        tutorialRow = Ui.o(context, "使用教程", null, null, true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.openTutorial();
            }
        });
        declarationRow = Ui.o(context, "免责声明", null, null, true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.openDeclaration();
            }
        });
        help.addView(tutorialRow.view);
        Ui.g(help, context);
        help.addView(declarationRow.view);
        LinearLayout.LayoutParams helpLp = new LinearLayout.LayoutParams(-1, -2);
        helpLp.topMargin = Ui.h(context, 2.0f);
        root.addView(help, helpLp);
    }

    private int currentTheme() {
        if (themeRow.valueText == null) return 0;
        String v = themeRow.valueText.getText().toString();
        if ("亮色".equals(v)) return 1;
        if ("暗色".equals(v)) return 2;
        return 0;
    }

    public void setTheme(int which) {
        if (themeRow.valueText != null)
            themeRow.valueText.setText(which == 1 ? "亮色" : which == 2 ? "暗色" : "跟随系统");
    }

    public void setBtPair(String text) {
        if (btPairRow.valueText != null) btPairRow.valueText.setText(text);
    }

    public void setSelectedCar(String text) {
        if (carRow.valueText != null) carRow.valueText.setText(text);
    }

    public void setLocalBt(String text) {
        if (localBtRow.valueText != null) localBtRow.valueText.setText(text);
    }

    public void setDebugMode(boolean on) {
        if (debugRow.switchView != null) debugRow.switchView.setChecked(on, true);
    }
}
