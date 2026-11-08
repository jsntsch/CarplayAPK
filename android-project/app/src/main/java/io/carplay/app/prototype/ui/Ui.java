package io.carplay.app.prototype.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * UI 工具包（移植自参考 APK 的 h.J0）。
 * 提供明/暗双主题与卡片/分区/行/文本/分段选择/开关等构造器。
 * 方法名沿用参考实现（l/e/o/q/p/j/d/m/n/g/h/i/c/k/a/b/f/r）以便屏幕布局逐行对照。
 */
public final class Ui {

    // ---- 主题色（明色为默认）----
    public static boolean f1233a = true;
    public static int f1234b = -16777216;
    public static int f1235c = -16777216;
    public static int f1236d = -14935012;
    public static int f1237e = -15066598;
    public static int f1238f = -13750738;
    public static int f1239g = -12961222;
    public static int f1240h = -13750738;
    public static int f1241i = -16750849;
    public static int f1242j = -1;
    public static int f1243k = -1;
    public static int f1244l = -7566196;
    public static int f1245m = -11908534;
    public static int f1246o = -38308;
    public static int f1247p = -9772;
    public static int f1248r = -24018;
    public static int f1249s = -13425651;
    public static int f1250t = -16777216;
    public static int f1251u = -16777216;
    public static int f1252v = -1;
    public static int f1253w = -7566196;
    public static int f1254x = -14497740;
    public static int f1255y = -24018;
    public static int f1256z = -38308;
    public static int f1257A = -11908534;

    public static final Typeface B = Typeface.create("sans-serif-medium", 0);
    public static final Typeface C = Typeface.create("sans-serif", 0);
    public static final Typeface D = Typeface.create("sans-serif", 1);
    public static int E = 536870911;
    public static int F = 520120063;

    private Ui() {}

    /** 应用主题：mode 1=亮色, 2=暗色, 0=跟随系统。 */
    public static void c(Context context, int mode) {
        boolean dark;
        if (mode == 1) dark = false;
        else if (mode == 2) dark = true;
        else dark = context != null && (context.getResources().getConfiguration().uiMode & 48) == 32;
        f1233a = dark;
        if (dark) {
            f1234b = -16777216;
            f1235c = -16777216;
            f1236d = -14935012;
            f1237e = -15066598;
            f1238f = -13750738;
            f1241i = -16750849;
            f1242j = -1;
            f1243k = -1;
            f1244l = -7566196;
            f1245m = -11908534;
            f1246o = -38308;
            f1247p = -9772;
            f1248r = -24018;
            f1249s = -13425651;
            f1250t = -16777216;
            f1251u = -16777216;
            f1252v = -1;
            f1253w = -7566196;
            f1254x = -14497740;
            f1255y = -24018;
            f1256z = -38308;
            f1257A = -11908534;
            E = 536870911;
            F = 520120063;
            f1239g = -12961222;
            f1240h = -13750738;
        } else {
            f1234b = -920844;
            f1235c = -920844;
            f1236d = -1;
            f1237e = -1;
            f1238f = -1644566;
            f1241i = -16750849;
            f1242j = -1;
            f1243k = -16053492;
            f1244l = -8816003;
            f1245m = -4604994;
            f1246o = -2542806;
            f1247p = -5036516;
            f1248r = -2524672;
            f1249s = -3617;
            f1250t = -920844;
            f1251u = -920844;
            f1252v = -16053492;
            f1253w = -8816003;
            f1254x = -16736235;
            f1255y = -2524672;
            f1256z = -2542806;
            f1257A = -4604994;
            E = 520093696;
            F = 520120063;
            f1239g = -2499876;
            f1240h = -1578774;
        }
    }

    /** 圆角纯色背景。 */
    public static GradientDrawable d(float radiusDp, int color, Context context) {
        A.e("c", context);
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(Dp.px(context, radiusDp));
        return g;
    }

    /** 卡片容器（竖直，圆角 28dp，主题卡片底色）。 */
    public static LinearLayout e(Context context) {
        A.e("c", context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackground(d(28.0f, f1237e, context));
        ll.setClipToOutline(true);
        return ll;
    }

    public static int f(Activity activity) {
        int w = activity.getResources().getDisplayMetrics().widthPixels;
        return Math.min(Math.max(Dp.px(activity, 200.0f), w - Dp.px(activity, 36.0f)), (Dp.px(activity, 14.0f) * 2) + Dp.px(activity, 400));
    }

    /** 卡片内分隔线（中性灰，随主题明暗自适应）。 */
    public static void g(LinearLayout parent, Context context) {
        A.e("c", context);
        View v = new View(context);
        v.setBackgroundColor(f1233a ? 0x33FFFFFF : 0x24000000);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Math.max(1, Dp.px(context, 0.7f)));
        lp.leftMargin = Dp.px(context, 18.0f);
        parent.addView(v, lp);
    }

    /** dp → px。 */
    public static int h(Context context, float dp) {
        A.e("c", context);
        return Dp.px(context, dp);
    }

    /** 下拉选择（弹窗）。 */
    public static void i(Activity activity, View anchor, String[] items, int selected, OnSelect onSelect) {
        if (activity == null || anchor == null || items.length == 0) return;
        PopupWindow pw = new PopupWindow(activity);
        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, Dp.px(activity, 6.0f), 0, Dp.px(activity, 6.0f));
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            boolean sel = i == selected;
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(16);
            row.setMinimumHeight(Dp.px(activity, 52.0f));
            row.setPadding(Dp.px(activity, 18.0f), Dp.px(activity, 12.0f), Dp.px(activity, 12.0f), Dp.px(activity, 12.0f));
            row.setBackground(n(0.0f, sel ? F : E, activity));
            row.addView(j(activity, items[i], 16.0f, sel ? f1241i : f1252v, sel), new LinearLayout.LayoutParams(0, -2, 1.0f));
            if (sel) row.addView(j(activity, "✓", 16.0f, f1241i, true));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();
                    onSelect.onSelected(idx);
                }
            });
            ll.addView(row);
        }
        pw.setBackgroundDrawable(d(20.0f, f1236d, activity));
        pw.setContentView(ll);
        pw.setOutsideTouchable(true);
        pw.setFocusable(true);
        pw.setElevation(Dp.px(activity, 10.0f));
        pw.setAnimationStyle(android.R.style.Animation_Dialog);
        pw.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        int maxW = Math.max(anchor.getWidth(), Dp.px(activity, 220.0f));
        ll.measure(View.MeasureSpec.makeMeasureSpec(maxW, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int h = ll.getMeasuredHeight();
        pw.setWidth(maxW);
        pw.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        int screenH = activity.getResources().getDisplayMetrics().heightPixels;
        Rect rect = new Rect();
        anchor.getGlobalVisibleRect(rect);
        int below = screenH - rect.bottom - Dp.px(activity, 8.0f);
        int above = rect.top - Dp.px(activity, 8.0f);
        if (h <= below || above <= h) pw.showAsDropDown(anchor, 0, Dp.px(activity, 4.0f));
        else pw.showAsDropDown(anchor, 0, -(Dp.px(activity, 4.0f) + rect.height() + h));
    }

    /** 文本视图。 */
    public static TextView j(Context context, String text, float sizeSp, int color, boolean bold) {
        A.e("c", context);
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        tv.setTypeface(bold ? B : C);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    /** 跟随系统栏内边距。 */
    public static void k(View view) {
        if (view == null) return;
        final int pl = view.getPaddingLeft();
        final int pt = view.getPaddingTop();
        final int pr = view.getPaddingRight();
        final int pb = view.getPaddingBottom();
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets wi) {
                Insets ins = wi.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(pl + ins.left, pt + ins.top, pr + ins.right, pb + ins.bottom);
                return wi;
            }
        });
    }

    /** 大标题（标题 + 副标题）。 */
    public static LinearLayout l(Context context, String title, String subtitle) {
        A.e("c", context);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(16);
        ll.setMinimumHeight(Dp.px(context, 76.0f));
        TextView t = j(context, title, 24.0f, f1252v, false);
        t.setTypeface(D);
        ll.addView(t);
        if (subtitle != null && subtitle.length() != 0) {
            TextView s = j(context, subtitle, 12.0f, f1253w, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.topMargin = Dp.px(context, 3.0f);
            ll.addView(s, lp);
        }
        return ll;
    }

    /** 按钮背景（Ripple）。 */
    public static RippleDrawable m(int color, int heightPx, int alpha, Context context) {
        A.e("c", context);
        GradientDrawable bg = d(0.0f, color, context);
        bg.setCornerRadius(heightPx / 2.0f);
        return new RippleDrawable(ColorStateList.valueOf(alpha), bg, null);
    }

    /** 行/卡片 Ripple 背景。 */
    public static RippleDrawable n(float radiusDp, int color, Context context) {
        A.e("c", context);
        return new RippleDrawable(ColorStateList.valueOf(color), d(radiusDp, 0, context), null);
    }

    /** 信息行：标签 + 副标题 + 值 + 箭头 + 可选图标，可点击。返回 UiHolder。 */
    public static UiHolder o(Context context, String label, String sub, String value, boolean chevron, View icon, View.OnClickListener listener) {
        A.e("c", context);
        UiHolder h = new UiHolder();
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(16);
        int padX = Dp.px(context, 18.0f);
        int padY = Dp.px(context, 12.0f);
        ll.setPadding(padX, padY, padX, padY);
        ll.setMinimumHeight(Dp.px(context, 56.0f));
        if (listener != null) {
            ll.setClickable(true);
            ll.setFocusable(true);
            ll.setOnClickListener(listener);
            ll.setBackground(n(0.0f, E, context));
        }
        if (icon != null) {
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(Dp.px(context, 24.0f), Dp.px(context, 24.0f));
            ilp.rightMargin = Dp.px(context, 14.0f);
            ll.addView(icon, ilp);
        }
        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.addView(j(context, label, 16.0f, f1252v, true));
        if (sub != null && sub.length() != 0) {
            TextView subTv = j(context, sub, 13.0f, f1253w, false);
            h.subText = subTv;
            subTv.setLineSpacing(Dp.px(context, 2.0f), 1.0f);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-2, -2);
            slp.topMargin = Dp.px(context, 2.0f);
            textCol.addView(subTv, slp);
        }
        ll.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (value != null) {
            TextView valTv = j(context, value, 14.0f, f1253w, false);
            h.valueText = valTv;
            valTv.setGravity(8388613);
            ll.addView(valTv, new LinearLayout.LayoutParams(-2, -2));
        } else {
            h.valueText = j(context, "", 14.0f, f1253w, false);
        }
        if (chevron) {
            TextView ch = j(context, "›", 16.0f, f1245m, false);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, -2);
            clp.leftMargin = Dp.px(context, 8.0f);
            ll.addView(ch, clp);
        }
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -2);
        llp.leftMargin = Dp.px(context, 4.0f);
        llp.rightMargin = Dp.px(context, 4.0f);
        ll.setLayoutParams(llp);
        h.view = ll;
        return h;
    }

    /** 分区标题。 */
    public static void p(LinearLayout parent, Context context, String title) {
        A.e("c", context);
        TextView tv = j(context, title, 12.0f, f1253w, true);
        tv.setMinHeight(Dp.px(context, 16.0f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.leftMargin = Dp.px(context, 20.0f);
        lp.topMargin = Dp.px(context, 16.0f);
        lp.bottomMargin = Dp.px(context, 6.0f);
        parent.addView(tv, lp);
    }

    /** 开关行：标签 + M3Switch。 */
    public static UiHolder q(Context context, String label, boolean checked, OnToggle onToggle) {
        return q(context, label, null, checked, onToggle);
    }

    /** [V1.8.35] 开关行：标签 + 副标题 + M3Switch（设置 → 连接 使用）。 */
    public static UiHolder q(Context context, String label, String sub, boolean checked, OnToggle onToggle) {
        A.e("c", context);
        UiHolder h = new UiHolder();
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(16);
        int padX = Dp.px(context, 18.0f);
        int padY = Dp.px(context, 12.0f);
        ll.setPadding(padX, padY, padX, padY);
        ll.setMinimumHeight(Dp.px(context, 56.0f));
        ll.setBackground(n(0.0f, E, context));
        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.addView(j(context, label, 16.0f, f1252v, true));
        if (sub != null && sub.length() != 0) {
            TextView subTv = j(context, sub, 13.0f, f1253w, false);
            h.subText = subTv;
            subTv.setLineSpacing(Dp.px(context, 2.0f), 1.0f);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-2, -2);
            slp.topMargin = Dp.px(context, 2.0f);
            textCol.addView(subTv, slp);
        }
        ll.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1.0f));
        M3Switch sw = new M3Switch(context);
        sw.setChecked(checked, false);
        sw.setOnToggle(onToggle);
        LinearLayout.LayoutParams swlp = new LinearLayout.LayoutParams(-2, -2);
        swlp.leftMargin = Dp.px(context, 12.0f);
        ll.addView(sw, swlp);
        h.switchView = sw;
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sw.performClick();
            }
        });
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -2);
        llp.leftMargin = Dp.px(context, 4.0f);
        llp.rightMargin = Dp.px(context, 4.0f);
        ll.setLayoutParams(llp);
        h.view = ll;
        return h;
    }

    /** 弹窗样式。 */
    public static void r(Dialog dialog) {
        Window w = dialog.getWindow();
        if (w == null) return;
        w.setBackgroundDrawable(new ColorDrawable(0));
        w.setLayout(-1, -2);
        w.setGravity(17);
        WindowManager.LayoutParams a = w.getAttributes();
        a.dimAmount = 0.6f;
        w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        w.setAttributes(a);
    }

    /** 选择对话框内部布局（供 b 使用）。 */
    public static LinearLayout a(Activity activity, Dialog dialog, String[] items, Runnable[] actions, int selected) {
        int gap = Dp.px(activity, 8.0f);
        ArrayList<TextView> list = new ArrayList<>(items.length);
        int total = 0;
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            boolean sel = i == selected;
            A.e("text", items[i]);
            TextView tv = j(activity, items[i], 15.0f, sel ? f1242j : f1241i, true);
            tv.setGravity(17);
            tv.setPadding(Dp.px(activity, 20.0f), Dp.px(activity, 10.0f), Dp.px(activity, 20.0f), Dp.px(activity, 10.0f));
            tv.setSingleLine(true);
            tv.setMaxLines(1);
            int minH = Dp.px(activity, 48.0f);
            tv.setMinimumHeight(minH);
            tv.setBackground(m(sel ? f1241i : 0, minH, sel ? (f1242j & 16777215) | 855638016 : E, activity));
            tv.setClickable(true);
            tv.setFocusable(true);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actions != null && idx < actions.length && actions[idx] != null) actions[idx].run();
                }
            });
            list.add(tv);
        }
        int maxW = Math.max(0, list.size() - 1) * gap;
        Iterator<TextView> it = list.iterator();
        while (it.hasNext()) {
            TextView tv = it.next();
            tv.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
            maxW += tv.getMeasuredWidth();
        }
        int avail = (f(activity) - Dp.px(activity, 44.0f)) - Dp.px(activity, 48.0f);
        LinearLayout ll = new LinearLayout(activity);
        ll.setPadding(0, Dp.px(activity, 20.0f), 0, 0);
        if (maxW <= avail) {
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(8388629);
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
                    lp.leftMargin = gap;
                    ll.addView(list.get(i), lp);
                } else {
                    ll.addView(list.get(i));
                }
            }
        } else {
            ll.setOrientation(LinearLayout.VERTICAL);
            ArrayList<TextView> adjusted = new ArrayList<>(list);
            if (selected >= 0 && selected < adjusted.size()) {
                TextView sel = adjusted.remove(selected);
                adjusted.add(sel);
            }
            for (int i = 0; i < adjusted.size(); i++) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                if (i > 0) lp.topMargin = gap;
                ll.addView(adjusted.get(i), lp);
            }
        }
        return ll;
    }

    /** 选择对话框。 */
    public static void b(Activity activity, String title, String body, String[] items, Runnable[] actions, int selected) {
        A.e("audio_only", activity);
        A.e("body", body);
        Dialog dialog = new Dialog(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Dp.px(activity, 22.0f);
        root.setPadding(pad, pad, pad, pad);
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(d(28.0f, f1237e, activity));
        card.setPadding(Dp.px(activity, 24.0f), Dp.px(activity, 24.0f), Dp.px(activity, 24.0f), Dp.px(activity, 16.0f));
        TextView t = j(activity, title, 20.0f, f1252v, false);
        t.setTypeface(D);
        card.addView(t);
        TextView b = j(activity, body, 14.0f, f1253w, false);
        b.setLineSpacing(Dp.px(activity, 5.0f), 1.0f);
        b.setPadding(0, Dp.px(activity, 12.0f), 0, 0);
        card.addView(b);
        card.addView(a(activity, dialog, items, actions, selected));
        root.addView(card);
        dialog.setContentView(root);
        r(dialog);
        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) w.setLayout(f(activity), -2);
    }
}
