package io.carplay.app.prototype.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * 设置页（移植自参考 APK 的 SettingsScreen 布局）。
 * 结构：标题 → 外 观(主题) → 连 接(蓝牙配对 / 手机蓝牙标识)
 *      → 调 试(调试模式 / 运行日志)。
 * 注：「选择车机」已移到 主页 → 车 机 → 蓝牙，此处不再重复提供。
 */
public final class SettingsScreenUi extends ScrollView {

    private final UiHolder themeRow;
    private final UiHolder btPairRow;
    private final UiHolder localBtRow;
    private final UiHolder proactiveIap2Row;
    private final UiHolder mdnsEnhanceRow;
    private final UiHolder debugRow;
    private final UiHolder logRecordRow;
    private final UiHolder logRow;

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

        root.addView(Ui.l(context, "设置", "外观 · 连接 · 调试"));

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
                                actions.applyTheme(index);
                            }
                        });
            }
        });
        appearance.addView(themeRow.view);
        LinearLayout.LayoutParams appearanceLp = new LinearLayout.LayoutParams(-1, -2);
        appearanceLp.topMargin = Ui.h(context, 2.0f);
        root.addView(appearance, appearanceLp);

        // 连 接（「选择车机」已移至 主页 → 车 机 → 蓝牙）
        Ui.p(root, context, "连 接");
        LinearLayout conn = Ui.e(context);
        btPairRow = Ui.o(context, "蓝牙配对", "搜索并选择车机蓝牙", "未配对", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.bluetoothPair();
            }
        });
        localBtRow = Ui.o(context, "手机蓝牙标识", "手机蓝牙名称与地址", "未设置", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.localBtIdentity();
            }
        });
        conn.addView(btPairRow.view);
        Ui.g(conn, context);
        conn.addView(localBtRow.view);
        Ui.g(conn, context);
        // [V1.8.35] P1-2：会话认证后由手机主动建立 iAP2-over-WiFi（修复车机→手机触摸通道）。
        proactiveIap2Row = Ui.q(context, "主动发起 iAP2-over-WiFi",
                "会话建立后由手机主动建立 iAP2 通道（修复触摸）", false, new OnToggle() {
                    @Override
                    public void onChanged(boolean checked) {
                        actions.setProactiveIap2(checked);
                    }
                });
        conn.addView(proactiveIap2Row.view);
        Ui.g(conn, context);
        // [V1.8.60] 移除「蓝牙 iAP2 引导（保留重连）/ 会话序列 V2 / 等待车机 connect」三个设置项：
        // 三者均为早期单变量 A/B 的实验开关，结论已定（默认关闭即正确行为），
        // 保留在 UI 上只会被误触。运行时开关一并收敛为常量 false，见 ControlChannel。
        // [V1.8.38] 车机发现增强：诊断车机是否 browse _carplay-ctrl，并可选单播通告绕过热点隔离。
        mdnsEnhanceRow = Ui.q(context, "车机发现增强（mDNS）",
                "监听车机发现请求并向车机单播通告控制服务", false, new OnToggle() {
                    @Override
                    public void onChanged(boolean checked) {
                        actions.setMdnsEnhance(checked);
                    }
                });
        conn.addView(mdnsEnhanceRow.view);
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
        // [V0.0.1] 连接日志默认关闭：只有先打开「调试模式」，这里才会真正落盘。
        logRecordRow = Ui.q(context, "连接日志记录",
                "写入本机文件（需先打开调试模式）", false, new OnToggle() {
                    @Override
                    public void onChanged(boolean checked) {
                        actions.setLogRecording(checked);
                    }
                });
        debug.addView(logRecordRow.view);
        Ui.g(debug, context);
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

    public void setLocalBt(String text, boolean highlight) {
        if (localBtRow.valueText != null) {
            localBtRow.valueText.setText(text);
            // [V1.8.34] 未设置（name/address 任一为空）时高亮为红色提醒用户补全。
            localBtRow.valueText.setTextColor(highlight ? 0xFFE53935 : Ui.f1253w);
        }
    }

    public void setDebugMode(boolean on) {
        if (debugRow.switchView != null) debugRow.switchView.setChecked(on, true);
    }

    /** [V0.0.1] 连接日志记录开关状态（需调试模式开启才有效）。 */
    public void setLogRecording(boolean on) {
        if (logRecordRow.switchView != null) logRecordRow.switchView.setChecked(on, true);
    }

    /** [V1.8.35] 主动发起 iAP2-over-WiFi（P1-2）开关状态。 */
    public void setProactiveIap2(boolean on) {
        if (proactiveIap2Row.switchView != null) proactiveIap2Row.switchView.setChecked(on, true);
    }

    /** [V1.8.38] 车机发现增强开关状态。 */
    public void setMdnsEnhance(boolean on) {
        if (mdnsEnhanceRow.switchView != null) mdnsEnhanceRow.switchView.setChecked(on, true);
    }
}
