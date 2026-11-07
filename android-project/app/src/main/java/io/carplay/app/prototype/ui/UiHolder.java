package io.carplay.app.prototype.ui;

import android.widget.LinearLayout;
import android.widget.TextView;

/** 行/开关构造器返回的对象（移植自参考 APK 的 h.I0）。 */
public final class UiHolder {
    public LinearLayout view;      // 整行可点击视图
    public TextView subText;       // 副标题
    public TextView valueText;     // 右侧值
    public M3Switch switchView;    // 行内开关（仅开关行有）
}
