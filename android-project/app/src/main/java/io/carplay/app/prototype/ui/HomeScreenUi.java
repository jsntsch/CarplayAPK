package io.carplay.app.prototype.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 首页（移植自参考 APK 的 HomeScreen 布局，接入本应用的 CarPlay 逻辑）。
 * 结构：大标题 → 连接状态卡（状态点 + 状态文字 + 7 段步骤条 + 开始连接按钮）
 *      → 隐藏信息条 → 「连 接」自动连接开关 → 「屏 幕」投屏画面/手机屏幕
 *      → 「投 影」屏幕方向 / 画面适配。
 */
public final class HomeScreenUi extends ScrollView {

    public static final int STEP_COUNT = 7;

    private final TextView statusTitle;
    private final TextView statusHint;
    private final TextView connectButton;
    private final TextView infoText;
    private final View statusDot;
    private final View[] steps;
    private final GradientDrawable[] cardBg;
    private final UiHolder autoConnectRow;
    private final UiHolder castResRow;
    private final UiHolder phoneResRow;
    private final UiHolder orientationRow;
    private final UiHolder fitRow;

    private int lastState = -1;
    private String lastButton = null;
    private boolean lastEnabled = true;
    private int lastStep = -1;

    public HomeScreenUi(Context context, ScreenActions.Home actions) {
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

        root.addView(Ui.l(context, "CarPlayAPK", "OPPO Car+ 转 CarPlay · 无线投屏工具"));

        // ---- 连接状态卡 ----
        LinearLayout card = Ui.e(context);
        this.cardBg = new GradientDrawable[]{Ui.d(28.0f, Ui.f1237e, context)};
        int cardPad = Ui.h(context, 18.0f);
        card.setPadding(cardPad, cardPad, cardPad, cardPad);
        card.setBackground(cardBg[0]);

        LinearLayout statusLine = new LinearLayout(context);
        statusLine.setOrientation(LinearLayout.HORIZONTAL);
        statusLine.setGravity(16);
        statusDot = new View(context);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(Ui.f1257A);
        statusDot.setBackground(dot);
        statusDot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(Ui.h(context, 8.0f), Ui.h(context, 8.0f));
        dotLp.rightMargin = Ui.h(context, 8.0f);
        statusLine.addView(statusDot, dotLp);
        statusLine.addView(Ui.j(context, "连接状态", 12.0f, Ui.f1253w, true));
        card.addView(statusLine);

        statusTitle = Ui.j(context, "未连接", 26.0f, Ui.f1252v, false);
        statusTitle.setTypeface(Ui.D);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(-2, -2);
        stLp.topMargin = Ui.h(context, 8.0f);
        card.addView(statusTitle, stLp);

        statusHint = Ui.j(context, "点下面的按钮开始", 14.0f, Ui.f1253w, false);
        statusHint.setLineSpacing(Ui.h(context, 4.0f), 1.0f);
        LinearLayout.LayoutParams shLp = new LinearLayout.LayoutParams(-1, -2);
        shLp.topMargin = Ui.h(context, 6.0f);
        card.addView(statusHint, shLp);

        // 7 段步骤条
        steps = new View[STEP_COUNT];
        LinearLayout stepRow = new LinearLayout(context);
        stepRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < STEP_COUNT; i++) {
            View s = new View(context);
            s.setBackground(Ui.d(3.0f, Ui.f1238f, context));
            s.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            steps[i] = s;
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, Ui.h(context, 4.0f), 1.0f);
            if (i > 0) slp.leftMargin = Ui.h(context, 4.0f);
            stepRow.addView(s, slp);
        }
        LinearLayout.LayoutParams srLp = new LinearLayout.LayoutParams(-1, -2);
        srLp.topMargin = Ui.h(context, 16.0f);
        card.addView(stepRow, srLp);

        // 开始连接按钮
        connectButton = Ui.j(context, "开始连接", 17.0f, Ui.f1242j, true);
        connectButton.setGravity(17);
        connectButton.setBackground(Ui.m(Ui.f1241i, Ui.h(context, 54.0f), 654311423, context));
        connectButton.setClickable(true);
        connectButton.setFocusable(true);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.connect();
            }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, Ui.h(context, 54.0f));
        btnLp.topMargin = Ui.h(context, 20.0f);
        card.addView(connectButton, btnLp);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.topMargin = Ui.h(context, 16.0f);
        root.addView(card, cardLp);

        // 隐藏信息条
        infoText = Ui.j(context, "", 12.0f, Ui.f1248r, false);
        infoText.setLineSpacing(Ui.h(context, 3.0f), 1.0f);
        infoText.setBackground(Ui.d(20.0f, Ui.f1249s, context));
        int infoPad = Ui.h(context, 14.0f);
        infoText.setPadding(infoPad, Ui.h(context, 12.0f), infoPad, Ui.h(context, 12.0f));
        infoText.setVisibility(View.GONE);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(-1, -2);
        infoLp.topMargin = Ui.h(context, 12.0f);
        root.addView(infoText, infoLp);

        // 「连 接」
        Ui.p(root, context, "连 接");
        LinearLayout connCard = Ui.e(context);
        autoConnectRow = Ui.q(context, "打开应用自动连接", false, new OnToggle() {
            @Override
            public void onChanged(boolean checked) {
                actions.setAutoConnect(checked);
            }
        });
        connCard.addView(autoConnectRow.view);
        LinearLayout.LayoutParams connLp = new LinearLayout.LayoutParams(-1, -2);
        connLp.topMargin = Ui.h(context, 2.0f);
        root.addView(connCard, connLp);

        // 「屏 幕」
        Ui.p(root, context, "屏 幕");
        LinearLayout screenCard = Ui.e(context);
        castResRow = Ui.o(context, "投屏画面", null, "—", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actions.pickResolution();
            }
        });
        if (castResRow.valueText != null) castResRow.valueText.setTextColor(Ui.f1241i);
        phoneResRow = Ui.o(context, "手机屏幕", null, "—", false, null, null);
        screenCard.addView(castResRow.view);
        Ui.g(screenCard, context);
        screenCard.addView(phoneResRow.view);
        LinearLayout.LayoutParams screenLp = new LinearLayout.LayoutParams(-1, -2);
        screenLp.topMargin = Ui.h(context, 2.0f);
        root.addView(screenCard, screenLp);

        // 「投 影」
        Ui.p(root, context, "投 影");
        LinearLayout projCard = Ui.e(context);
        orientationRow = Ui.o(context, "屏幕方向", null, "横屏", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ui.i((android.app.Activity) getContext(), v, new String[]{"横屏", "竖屏"},
                        "竖屏".equals(orientationRow.valueText != null ? orientationRow.valueText.getText() : "横屏") ? 1 : 0,
                        new OnSelect() {
                            @Override
                            public void onSelected(int index) {
                                actions.setOrientation(index);
                            }
                        });
            }
        });
        fitRow = Ui.o(context, "画面适配", null, "适应", true, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ui.i((android.app.Activity) getContext(), v, new String[]{"适应", "拉伸"},
                        "拉伸".equals(fitRow.valueText != null ? fitRow.valueText.getText() : "适应") ? 1 : 0,
                        new OnSelect() {
                            @Override
                            public void onSelected(int index) {
                                actions.setFitMode(index);
                            }
                        });
            }
        });
        projCard.addView(orientationRow.view);
        Ui.g(projCard, context);
        projCard.addView(fitRow.view);
        root.addView(projCard, new LinearLayout.LayoutParams(-1, -2));
    }

    /** 更新连接状态。state: 0 未连接 / 1 等待 / 2 已连接 / 3 异常。 */
    public void setConnectionStatus(String title, String detail, int state, String buttonText, boolean enabled) {
        if (!title.equals(statusTitle.getText().toString())) statusTitle.setText(title);
        if (!detail.equals(statusHint.getText().toString())) statusHint.setText(detail);
        if (state != lastState) {
            lastState = state;
            int dotColor, textColor;
            switch (state) {
                case 1: dotColor = Ui.f1255y; textColor = Ui.f1248r; break;
                case 2: dotColor = Ui.f1254x; textColor = Ui.f1254x; break;
                case 3: dotColor = Ui.f1256z; textColor = Ui.f1247p; break;
                default: dotColor = Ui.f1257A; textColor = Ui.f1252v; break;
            }
            GradientDrawable d = (GradientDrawable) statusDot.getBackground();
            d.setColor(dotColor);
            statusTitle.setTextColor(textColor);
        }
        if (buttonText == null) buttonText = "开始连接";
        if (!buttonText.equals(lastButton)) {
            lastButton = buttonText;
            connectButton.setText(buttonText);
        }
        if (enabled != lastEnabled) {
            lastEnabled = enabled;
            connectButton.setAlpha(enabled ? 1.0f : 0.45f);
            connectButton.setEnabled(enabled);
        }
    }

    public void setStep(int step) {
        if (step == lastStep) return;
        lastStep = step;
        for (int i = 0; i < steps.length; i++) {
            int color = i < step ? Ui.f1241i : Ui.f1238f;
            steps[i].setBackground(Ui.d(3.0f, color, getContext()));
        }
    }

    public void setResolution(String cast, String phone) {
        if (castResRow.valueText != null) castResRow.valueText.setText(cast);
        if (phoneResRow.valueText != null) phoneResRow.valueText.setText(phone);
    }

    public void setOrientation(int which) {
        if (orientationRow.valueText != null)
            orientationRow.valueText.setText(which == 1 ? "竖屏" : "横屏");
    }

    public void setFitMode(int which) {
        if (fitRow.valueText != null)
            fitRow.valueText.setText(which == 1 ? "拉伸" : "适应");
    }

    public void setAutoConnect(boolean on) {
        if (autoConnectRow.switchView != null) autoConnectRow.switchView.setChecked(on, true);
    }

    public void showInfo(String text) {
        if (text == null || text.length() == 0) {
            infoText.setVisibility(View.GONE);
            return;
        }
        infoText.setText(text);
        infoText.setVisibility(View.VISIBLE);
    }
}
