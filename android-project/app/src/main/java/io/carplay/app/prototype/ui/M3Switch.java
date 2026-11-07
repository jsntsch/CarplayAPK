package io.carplay.app.prototype.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.PathInterpolator;

/** 自绘 Material3 风格开关（移植自参考 APK 的 com.taksss.carlinkmonitor.M3Switch）。 */
public final class M3Switch extends View {

    private final Paint trackPaint;
    private final Paint thumbPaint;
    private final RectF rect;
    private final ArgbEvaluator extractor;
    private final float density;
    private final float widthPx;
    private final float heightPx;
    private final float thumbR;

    private boolean checked;
    private float progress;
    private OnToggle onToggle;
    private ValueAnimator animator;

    public M3Switch(Context context) {
        super(context);
        A.e("c", context);
        this.trackPaint = new Paint(1);
        this.thumbPaint = new Paint(1);
        this.rect = new RectF();
        this.extractor = new ArgbEvaluator();
        float f2 = context.getResources().getDisplayMetrics().density;
        this.density = f2;
        this.widthPx = 50 * f2;
        this.heightPx = 30 * f2;
        this.thumbR = 24 * f2;
        setClickable(true);
        setFocusable(true);
    }

    public void setChecked(boolean z, boolean animate) {
        OnToggle cb = this.onToggle;
        if (this.checked == z) {
            if (this.progress != (z ? 1.0f : 0.0f) && this.animator == null) {
                this.progress = z ? 1.0f : 0.0f;
                invalidate();
                return;
            }
            return;
        }
        this.checked = z;
        if (getWidth() == 0) {
            this.progress = z ? 1.0f : 0.0f;
            invalidate();
        } else {
            float target = z ? 1.0f : 0.0f;
            ValueAnimator va = this.animator;
            if (va != null) va.cancel();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(this.progress, target);
            this.animator = valueAnimator;
            valueAnimator.setDuration(180L);
            valueAnimator.setInterpolator(new PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f));
            valueAnimator.addUpdateListener(animation -> {
                this.progress = (float) animation.getAnimatedValue();
                invalidate();
            });
            valueAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    animator = null;
                }
            });
            valueAnimator.start();
        }
        if (!animate || cb == null) return;
        cb.onChanged(z);
    }

    public void setOnToggle(OnToggle toggle) {
        this.onToggle = toggle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        A.e("canvas", canvas);
        float f2 = this.progress;
        float centerX = getWidth() / 2.0f;
        float centerY = getHeight() / 2.0f;
        RectF rectF = this.rect;
        float f3 = this.widthPx;
        float f4 = this.heightPx / 2.0f;
        rectF.set(centerX - (f3 / 2.0f), centerY - f4, (f3 / 2.0f) + centerX, centerY + f4);
        Paint paint = this.trackPaint;
        Paint.Style style = Paint.Style.FILL;
        paint.setStyle(style);
        Object obj = this.extractor.evaluate(f2, Ui.f1239g, -16744193);
        paint.setColor((Integer) obj);
        canvas.drawRoundRect(rectF, f4, f4, paint);
        float f5 = this.density * 3.0f;
        float f6 = rectF.left + f5;
        float f7 = this.thumbR;
        float fWidth = (((rectF.width() - (2 * f5)) - f7) * f2) + (f7 / 2.0f) + f6;
        Paint paint2 = this.thumbPaint;
        paint2.setStyle(style);
        paint2.setColor(-1);
        canvas.drawCircle(fWidth, centerY, f7 / 2.0f, paint2);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Switch");
        info.setCheckable(true);
        info.setChecked(this.checked);
        info.setText(getContentDescription());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                View.resolveSize(Dp.round((8 * this.density) + this.widthPx), widthMeasureSpec),
                View.resolveSize((int) this.heightPx, heightMeasureSpec));
    }

    @Override
    public boolean performClick() {
        super.performClick();
        setChecked(!this.checked, true);
        return true;
    }
}
