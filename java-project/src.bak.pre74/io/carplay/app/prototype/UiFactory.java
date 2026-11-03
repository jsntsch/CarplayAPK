package io.carplay.app.prototype;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.invoke.VarHandle;

/* JADX INFO: loaded from: classes.dex */
/**
 * UiFactory
 *
 * <p>原混淆名：{@code io.carplay.app.prototype.m0}（jadx 产物 m0.java）
 *
 * <p>命名依据：653 行、26 个静态方法，全部在拼 LinearLayout / TextView / GradientDrawable / CompoundButton；内嵌 a extends CompoundButton、b extends Drawable
 */
final class UiFactory {
    static final int a = -15258551;
    static final int b = -10193265;
    static final int c = -14328088;
    static final int d = -854534;

    public static class a extends CompoundButton {
        final Paint a;

        public a(Context context) {
            super(context);
            this.a = new Paint(3);
        }

        @Override // android.widget.CompoundButton, android.widget.TextView, android.view.View
        public void onDraw(Canvas canvas) {
            int i;
            super.onDraw(canvas);
            float f = getResources().getDisplayMetrics().density;
            float width = getLayoutDirection() == 1 ? 12.0f * f : getWidth() - (58.0f * f);
            float f2 = 26.0f * f;
            float height = (getHeight() - f2) / 2.0f;
            Paint paint = this.a;
            if (isEnabled()) {
                i = isChecked() ? UiFactory.c : -3681565;
            } else {
                i = -2695446;
            }
            paint.setColor(i);
            float f3 = f * 13.0f;
            canvas.drawRoundRect(width, height, (46.0f * f) + width, height + f2, f3, f3, this.a);
            this.a.setColor(-1);
            canvas.drawCircle(((isChecked() ? 33 : 13) * f) + width, height + f3, f * 9.0f, this.a);
        }
    }

    public static final class b extends Drawable {
        final String b;
        final int c;
        final int d;
        final Paint a = new Paint(3);
        int e = 255;

        public b(Context context, String str, int i) {
            this.b = str;
            this.c = i;
            this.d = UiFactory.l(context, 21);
        }

        public void a(Canvas canvas, float f, float f2, float f3, float f4) {
            canvas.drawLine(f, f2, f3, f4, this.a);
        }

        public void b(Canvas canvas, float... fArr) {
            Path path = new Path();
            path.moveTo(fArr[0], fArr[1]);
            for (int i = 2; i < fArr.length; i += 2) {
                path.lineTo(fArr[i], fArr[i + 1]);
            }
            canvas.drawPath(path, this.a);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(getBounds().left, getBounds().top);
            canvas.scale(getBounds().width() / 24.0f, getBounds().height() / 24.0f);
            int[] state = getState();
            int length = state.length;
            int i = 0;
            boolean z = false;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (state[i] == 16842910) {
                    z = true;
                }
                i++;
            }
            this.a.setColor(this.c);
            this.a.setAlpha((getState().length <= 0 || z) ? this.e : 95);
            this.a.setStyle(Paint.Style.STROKE);
            this.a.setStrokeWidth(1.8f);
            this.a.setStrokeCap(Paint.Cap.ROUND);
            this.a.setStrokeJoin(Paint.Join.ROUND);
            String str = this.b;
            str.getClass();
            switch (str) {
                case "export":
                case "log":
                    canvas.drawRoundRect(5.0f, 3.0f, 19.0f, 22.0f, 2.0f, 2.0f, this.a);
                    a(canvas, 9.0f, 8.0f, 15.0f, 8.0f);
                    a(canvas, 9.0f, 12.0f, 15.0f, 12.0f);
                    if (this.b.equals("export")) {
                        a(canvas, 12.0f, 15.0f, 12.0f, 20.0f);
                        b(canvas, 9.0f, 17.0f, 12.0f, 20.0f, 15.0f, 17.0f);
                        break;
                    } else {
                        a(canvas, 9.0f, 16.0f, 13.0f, 16.0f);
                        break;
                    }
                case "shield":
                    b(canvas, 12.0f, 2.0f, 21.0f, 6.0f, 20.0f, 14.0f, 17.0f, 19.0f, 12.0f, 22.0f, 7.0f, 19.0f, 4.0f, 14.0f, 3.0f, 6.0f, 12.0f, 2.0f);
                    b(canvas, 8.0f, 12.0f, 11.0f, 15.0f, 16.0f, 9.0f);
                    break;
                case "car":
                    canvas.drawRoundRect(3.0f, 9.0f, 21.0f, 18.0f, 3.0f, 3.0f, this.a);
                    a(canvas, 5.0f, 9.0f, 7.0f, 4.0f);
                    a(canvas, 7.0f, 4.0f, 17.0f, 4.0f);
                    a(canvas, 17.0f, 4.0f, 19.0f, 9.0f);
                    a(canvas, 6.0f, 18.0f, 6.0f, 21.0f);
                    a(canvas, 18.0f, 18.0f, 18.0f, 21.0f);
                    a(canvas, 6.0f, 13.0f, 8.0f, 13.0f);
                    a(canvas, 16.0f, 13.0f, 18.0f, 13.0f);
                    break;
                case "back":
                    b(canvas, 10.0f, 5.0f, 3.0f, 12.0f, 10.0f, 19.0f);
                    a(canvas, 3.0f, 12.0f, 21.0f, 12.0f);
                    break;
                case "help":
                    canvas.drawCircle(12.0f, 12.0f, 9.0f, this.a);
                    canvas.drawArc(9.0f, 6.0f, 15.0f, 12.0f, 180.0f, 245.0f, false, this.a);
                    a(canvas, 12.0f, 12.0f, 12.0f, 14.0f);
                    canvas.drawCircle(12.0f, 18.0f, 0.5f, this.a);
                    break;
                case "scan":
                    b(canvas, 3.0f, 8.0f, 3.0f, 3.0f, 8.0f, 3.0f);
                    b(canvas, 16.0f, 3.0f, 21.0f, 3.0f, 21.0f, 8.0f);
                    b(canvas, 21.0f, 16.0f, 21.0f, 21.0f, 16.0f, 21.0f);
                    b(canvas, 8.0f, 21.0f, 3.0f, 21.0f, 3.0f, 16.0f);
                    a(canvas, 7.0f, 12.0f, 17.0f, 12.0f);
                    break;
                case "stop":
                    canvas.drawRoundRect(5.0f, 5.0f, 19.0f, 19.0f, 3.0f, 3.0f, this.a);
                    break;
                case "wifi":
                    canvas.drawArc(2.0f, 6.0f, 22.0f, 24.0f, 220.0f, 100.0f, false, this.a);
                    canvas.drawArc(6.0f, 11.0f, 18.0f, 23.0f, 220.0f, 100.0f, false, this.a);
                    canvas.drawCircle(12.0f, 20.0f, 0.6f, this.a);
                    break;
                case "group":
                    canvas.drawCircle(9.0f, 8.0f, 3.0f, this.a);
                    canvas.drawArc(2.0f, 14.0f, 16.0f, 25.0f, 185.0f, 170.0f, false, this.a);
                    canvas.drawArc(14.0f, 5.0f, 20.0f, 11.0f, 260.0f, 190.0f, false, this.a);
                    canvas.drawArc(15.0f, 14.0f, 23.0f, 22.0f, 230.0f, 120.0f, false, this.a);
                    break;
                case "heart":
                    Path path = new Path();
                    path.moveTo(12.0f, 21.0f);
                    path.cubicTo(9.0f, 18.0f, 2.0f, 13.0f, 2.0f, 8.0f);
                    path.cubicTo(2.0f, 2.0f, 9.0f, 1.0f, 12.0f, 6.0f);
                    path.cubicTo(15.0f, 1.0f, 22.0f, 2.0f, 22.0f, 8.0f);
                    path.cubicTo(22.0f, 13.0f, 15.0f, 18.0f, 12.0f, 21.0f);
                    canvas.drawPath(path, this.a);
                    break;
                case "refresh":
                    canvas.drawArc(4.0f, 4.0f, 20.0f, 20.0f, 40.0f, 275.0f, false, this.a);
                    b(canvas, 20.0f, 4.0f, 20.0f, 10.0f, 14.0f, 10.0f);
                    break;
                case "settings":
                    canvas.drawCircle(12.0f, 12.0f, 4.0f, this.a);
                    canvas.drawCircle(12.0f, 12.0f, 9.0f, this.a);
                    for (int i2 = 0; i2 < 8; i2++) {
                        double d = (((double) i2) * 3.141592653589793d) / 4.0d;
                        a(canvas, (((float) Math.cos(d)) * 9.0f) + 12.0f, (((float) Math.sin(d)) * 9.0f) + 12.0f, (((float) Math.cos(d)) * 11.0f) + 12.0f, (((float) Math.sin(d)) * 11.0f) + 12.0f);
                    }
                    break;
                case "bluetooth":
                    b(canvas, 11.0f, 2.0f, 17.0f, 7.0f, 7.0f, 17.0f);
                    b(canvas, 11.0f, 22.0f, 17.0f, 17.0f, 7.0f, 7.0f);
                    a(canvas, 11.0f, 2.0f, 11.0f, 22.0f);
                    break;
                default:
                    b(canvas, 9.0f, 5.0f, 16.0f, 12.0f, 9.0f, 19.0f);
                    break;
            }
            canvas.restore();
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return this.d;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return this.d;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public boolean isStateful() {
            return true;
        }

        @Override // android.graphics.drawable.Drawable
        public boolean onStateChange(int[] iArr) {
            invalidateSelf();
            return true;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.e = i;
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.a.setColorFilter(colorFilter);
        }
    }

    private UiFactory() {
    }

    public static void A(LinearLayout linearLayout, String str, String str2) {
        C(linearLayout, str, 20, true).setPadding(0, 0, 0, l(linearLayout.getContext(), 6));
        TextView textViewC = C(linearLayout, str2, 12, false);
        textViewC.setTextColor(b);
        textViewC.setPadding(0, 0, 0, l(linearLayout.getContext(), 14));
    }

    public static GradientDrawable B(Context context, int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(l(context, i2));
        return gradientDrawable;
    }

    public static TextView C(LinearLayout linearLayout, String str, int i, boolean z) {
        Context context = linearLayout.getContext();
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(a);
        textView.setLineSpacing(l(context, 3), 1.0f);
        textView.setIncludeFontPadding(false);
        if (z) {
            textView.setTypeface(Typeface.DEFAULT, 1);
        }
        linearLayout.addView(textView, new LinearLayout.LayoutParams(-1, -2));
        return textView;
    }

    public static void D(Activity activity, LinearLayout linearLayout, String str, String str2) {
        TextView textViewC = C(linearLayout, "OcarPlay", 13, true);
        textViewC.setTextColor(c);
        textViewC.setLetterSpacing(0.03f);
        textViewC.setGravity(16);
        textViewC.setCompoundDrawablesRelativeWithIntrinsicBounds(new b(activity, "car", c), (Drawable) null, (Drawable) null, (Drawable) null);
        textViewC.setCompoundDrawablePadding(l(activity, 8));
        C(linearLayout, str, 22, true).setPadding(0, l(activity, 8), 0, l(activity, 7));
        TextView textViewC2 = C(linearLayout, str2, 12, false);
        textViewC2.setTextColor(b);
        textViewC2.setPadding(0, 0, 0, l(activity, 16));
    }

    public static CompoundButton E(Context context, String str, boolean z) {
        a aVar = new a(context);
        aVar.setButtonDrawable((Drawable) null);
        aVar.setText(str);
        aVar.setTextSize(14.0f);
        aVar.setTextColor(a);
        aVar.setChecked(z);
        aVar.setMinHeight(l(context, 60));
        aVar.setClickable(true);
        aVar.setFocusable(true);
        aVar.setGravity(16);
        aVar.setPaddingRelative(l(context, 12), l(context, 14), l(context, 70), l(context, 14));
        aVar.setBackground(B(context, -591620, 16));
        aVar.setOnCheckedChangeListener(new n0());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = l(context, 12);
        aVar.setLayoutParams(layoutParams);
        return aVar;
    }

    public static void F(LinearLayout linearLayout, Runnable runnable, Runnable runnable2) {
        Context context = linearLayout.getContext();
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(0);
        linearLayout2.setBaselineAligned(false);
        linearLayout2.setGravity(16);
        linearLayout2.setPadding(l(context, 16), l(context, 5), l(context, 10), l(context, 5));
        linearLayout2.setBackgroundColor(-1);
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(-1, -2));
        int i = 1;
        TextView textViewC = C(linearLayout2, "OcarPlay", 19, true);
        textViewC.setTextColor(a);
        textViewC.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        String[] strArr = {"原生 Car+", "日志"};
        Runnable[] runnableArr = {runnable, runnable2};
        int i2 = 0;
        while (i2 < 2) {
            Button button = new Button(context);
            button.setText(strArr[i2]);
            button.setAllCaps(false);
            button.setTextSize(13.0f);
            button.setTextColor(c);
            button.setTypeface(Typeface.DEFAULT, 1);
            button.setIncludeFontPadding(false);
            button.setSingleLine(true);
            button.setGravity(17);
            button.setStateListAnimator(null);
            button.setElevation(0.0f);
            button.setTranslationZ(0.0f);
            button.setMinHeight(l(context, 48));
            button.setMinimumHeight(l(context, 48));
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setPadding(l(context, 10), 0, l(context, 10), 0);
            button.setBackground(new RippleDrawable(ColorStateList.valueOf(405102312), B(context, -1182721, 10), null));
            Runnable runnable3 = runnableArr[i2];
            f0 f0Var = new f0(i);
            f0Var.b = runnable3;
            VarHandle.storeStoreFence();
            button.setOnClickListener(f0Var);
            button.setContentDescription(i2 == 0 ? "打开原生 Car+" : "打开连接日志");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, l(context, 48));
            if (i2 > 0) {
                layoutParams.setMarginStart(l(context, 6));
            }
            linearLayout2.addView(button, layoutParams);
            i2++;
        }
    }

    public static LinearLayout G(Activity activity) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        int iL = l(activity, Math.max(16, (activity.getResources().getConfiguration().screenWidthDp - 680) / 2));
        linearLayout.setPadding(iL, l(activity, 14), iL, l(activity, 20));
        return linearLayout;
    }

    public static LinearLayout H(Activity activity) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(d);
        p0 p0Var = new p0();
        p0Var.a = linearLayout;
        VarHandle.storeStoreFence();
        linearLayout.setOnApplyWindowInsetsListener(p0Var);
        return linearLayout;
    }

    public static void I(Button button, boolean z) {
        Context context = button.getContext();
        button.setTextSize(14.0f);
        button.setTextColor(z ? c : b);
        button.setCompoundDrawables(null, null, null, null);
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(405102312), B(context, z ? -1182721 : -1, 12), null));
        button.setMinHeight(l(context, 52));
        button.setMinimumHeight(l(context, 52));
        button.setPadding(l(context, 4), l(context, 10), l(context, 4), l(context, 10));
        button.setSelected(z);
        button.setContentDescription(String.valueOf(button.getText()).concat(z ? "，当前页面" : "，切换页面"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.setMargins(l(context, 4), l(context, 6), l(context, 4), l(context, 6));
        button.setLayoutParams(layoutParams);
    }

    public static void i(Button button, boolean z) {
        Context context = button.getContext();
        button.setAllCaps(false);
        button.setTextSize(14.0f);
        button.setTypeface(Typeface.DEFAULT, 1);
        button.setGravity(17);
        button.setMinHeight(l(context, 52));
        button.setMinimumHeight(l(context, 52));
        button.setPadding(l(context, 12), l(context, 12), l(context, 12), l(context, 12));
        button.setStateListAnimator(null);
        int i = c;
        int i2 = z ? -1 : c;
        button.setTextColor(new ColorStateList(new int[][]{new int[]{-16842910}, new int[0]}, new int[]{-6904648, i2}));
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{-16842910}, B(context, -1051913, 16));
        int[] iArr = new int[0];
        if (!z) {
            i = -1182721;
        }
        stateListDrawable.addState(iArr, B(context, i, 16));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(z ? 872415231 : 405102312), stateListDrawable, null));
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(new b(context, n(button.getText().toString()), i2), (Drawable) null, (Drawable) null, (Drawable) null);
        button.setCompoundDrawablePadding(l(context, 7));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = l(context, 9);
        ViewParent parent = button.getParent();
        if (parent instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) parent;
            if (linearLayout.getOrientation() == 0) {
                layoutParams.width = 0;
                layoutParams.weight = 1.0f;
                if (linearLayout.indexOfChild(button) > 0) {
                    layoutParams.setMarginStart(l(context, 8));
                }
            }
        }
        button.setLayoutParams(layoutParams);
    }

    public static LinearLayout j(LinearLayout linearLayout) {
        Context context = linearLayout.getContext();
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(l(context, 14), l(context, 14), l(context, 14), l(context, 14));
        GradientDrawable gradientDrawableB = B(context, -1, 16);
        gradientDrawableB.setStroke(l(context, 1), -1577483);
        linearLayout2.setBackground(gradientDrawableB);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.bottomMargin = l(context, 12);
        linearLayout.addView(linearLayout2, layoutParams);
        return linearLayout2;
    }

    public static AlertDialog k(Activity activity, String str, String str2, String str3, Runnable runnable, String str4, Runnable runnable2) {
        LinearLayout linearLayout = new LinearLayout(activity);
        int i = 1;
        linearLayout.setOrientation(1);
        linearLayout.setPadding(l(activity, 22), l(activity, 22), l(activity, 22), l(activity, 18));
        m(linearLayout, str, "shield");
        ScrollView scrollView = new ScrollView(activity);
        scrollView.setFillViewport(false);
        scrollView.setClipToPadding(false);
        TextView textView = new TextView(activity);
        textView.setText(str2);
        textView.setTextSize(14.0f);
        textView.setTextColor(b);
        textView.setLineSpacing(l(activity, 5), 1.0f);
        textView.setTextIsSelectable(true);
        textView.setPadding(0, l(activity, 8), 0, l(activity, 16));
        scrollView.addView(textView);
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(-1, Math.max(l(activity, 130), Math.min(l(activity, 430), activity.getResources().getDisplayMetrics().heightPixels - l(activity, 270)))));
        AlertDialog alertDialogCreate = new AlertDialog.Builder(activity).setView(linearLayout).create();
        Button button = new Button(activity);
        button.setText(str3);
        linearLayout.addView(button);
        i(button, true);
        o0 o0Var = new o0(0);
        o0Var.b = runnable;
        o0Var.c = alertDialogCreate;
        VarHandle.storeStoreFence();
        button.setOnClickListener(o0Var);
        if (str4 != null) {
            Button button2 = new Button(activity);
            button2.setText(str4);
            linearLayout.addView(button2);
            i(button2, false);
            o0 o0Var2 = new o0(i);
            o0Var2.b = runnable2;
            o0Var2.c = alertDialogCreate;
            VarHandle.storeStoreFence();
            button2.setOnClickListener(o0Var2);
        }
        h0 h0Var = new h0(i);
        h0Var.b = alertDialogCreate;
        h0Var.c = activity;
        VarHandle.storeStoreFence();
        alertDialogCreate.setOnShowListener(h0Var);
        return alertDialogCreate;
    }

    public static int l(Context context, int i) {
        return (int) ((i * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static TextView m(LinearLayout linearLayout, String str, String str2) {
        TextView textViewC = C(linearLayout, str, 17, true);
        textViewC.setGravity(16);
        textViewC.setCompoundDrawablePadding(l(linearLayout.getContext(), 9));
        textViewC.setCompoundDrawablesRelativeWithIntrinsicBounds(new b(linearLayout.getContext(), str2, c), (Drawable) null, (Drawable) null, (Drawable) null);
        textViewC.setPadding(0, 0, 0, l(linearLayout.getContext(), 7));
        return textViewC;
    }

    private static String n(String str) {
        if (str.contains("蓝牙")) {
            return "bluetooth";
        }
        if (str.contains("刷新")) {
            return "refresh";
        }
        if (str.contains("日志")) {
            return str.contains("导出") ? "export" : "log";
        }
        if (str.contains("教程")) {
            return "help";
        }
        if (str.contains("WLAN")) {
            return "wifi";
        }
        if (str.contains("声明") || str.contains("同意")) {
            return "shield";
        }
        if (str.contains("群") || str.contains("交流")) {
            return "group";
        }
        if (str.contains("断开")) {
            return "stop";
        }
        if (str.contains("识别")) {
            return "scan";
        }
        if (str.contains("原生")) {
            return "settings";
        }
        if (str.contains("赞助")) {
            return "heart";
        }
        if (str.contains("返回")) {
            return "back";
        }
        return (str.contains("连接") || str.contains("车机")) ? "car" : "next";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void o(Runnable runnable, AlertDialog alertDialog, View view) {
        if (runnable != null) {
            runnable.run();
        }
        alertDialog.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void p(Runnable runnable, AlertDialog alertDialog, View view) {
        if (runnable != null) {
            runnable.run();
        }
        alertDialog.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void q(AlertDialog alertDialog, Activity activity, DialogInterface dialogInterface) {
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(B(activity, -1, 26));
            alertDialog.getWindow().setLayout(Math.min(activity.getResources().getDisplayMetrics().widthPixels - l(activity, 28), l(activity, 560)), -2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void r(Activity activity) {
        WindowInsetsController windowInsetsController = activity.getWindow().getDecorView().getWindowInsetsController();
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsAppearance(24, 24);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ WindowInsets s(int i, Activity activity, View view, WindowInsets windowInsets) {
        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
        ViewParent parent = view.getParent();
        if (!(parent instanceof ScrollView)) {
            view.setPadding(insets.left + i, insets.top + l(activity, 22), i + insets.right, insets.bottom + l(activity, 24));
            return windowInsets;
        }
        ScrollView scrollView = (ScrollView) parent;
        scrollView.setBackgroundColor(d);
        scrollView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        scrollView.setClipToPadding(true);
        view.setPadding(i, l(activity, 22), i, l(activity, 24));
        return windowInsets;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ WindowInsets v(LinearLayout linearLayout, View view, WindowInsets windowInsets) {
        int iSystemBars = WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout();
        Insets insets = windowInsets.getInsets(iSystemBars);
        linearLayout.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        return new WindowInsets.Builder(windowInsets).setInsets(iSystemBars, Insets.NONE).build();
    }

    public static void w(Activity activity) {
        activity.getWindow().setStatusBarColor(d);
        activity.getWindow().setNavigationBarColor(d);
        View decorView = activity.getWindow().getDecorView();
        p pVar = new p(2);
        pVar.b = activity;
        VarHandle.storeStoreFence();
        decorView.post(pVar);
    }

    public static LinearLayout x(Activity activity) {
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(d);
        int iL = l(activity, Math.max(16, (activity.getResources().getConfiguration().screenWidthDp - 680) / 2));
        linearLayout.setPadding(iL, l(activity, 24), iL, l(activity, 28));
        l0 l0Var = new l0();
        l0Var.a = iL;
        l0Var.b = activity;
        VarHandle.storeStoreFence();
        linearLayout.setOnApplyWindowInsetsListener(l0Var);
        return linearLayout;
    }

    public static RadioButton y(Context context, String str) {
        RadioButton radioButton = new RadioButton(context);
        radioButton.setText(str);
        radioButton.setTextSize(14.0f);
        radioButton.setTextColor(a);
        radioButton.setMinHeight(l(context, 44));
        radioButton.setPadding(0, l(context, 6), 0, l(context, 6));
        radioButton.setButtonTintList(ColorStateList.valueOf(c));
        return radioButton;
    }

    public static LinearLayout z(LinearLayout linearLayout) {
        Context context = linearLayout.getContext();
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setBaselineAligned(false);
        linearLayout2.setOrientation((context.getResources().getConfiguration().screenWidthDp < 380 || context.getResources().getConfiguration().fontScale > 1.15f) ? 1 : 0);
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(-1, -2));
        return linearLayout2;
    }
}
