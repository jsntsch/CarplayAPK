package io.carplay.app.prototype.ui;

import android.content.Context;
import android.util.DisplayMetrics;

/** dp/px 工具（移植自参考 APK 的 a.k）。 */
public final class Dp {
    private Dp() {}

    public static int px(Context context, float dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (dm.density * dp + 0.5f);
    }

    public static int clamp(int value, int low, int high) {
        return Math.max(low, Math.min(high, value));
    }

    /** 浮点四舍五入转 int（参考 a.k.X）。 */
    public static int round(float v) {
        return (int) (v + 0.5f);
    }
}
