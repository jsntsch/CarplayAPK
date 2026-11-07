package io.carplay.app.prototype.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** 自绘 Material 风格图标（移植自参考 APK 的 com.taksss.carlinkmonitor.M3Icon，仅保留通用图标）。 */
public final class M3Icon extends View {

    public int iconType;
    public boolean filled;
    public int color;

    private final Paint strokePaint;
    private final Paint fillPaint;
    private final Path pathA;
    private final Path pathB;

    public M3Icon(Context context, int type, boolean fill, int color) {
        super(context);
        A.e("c", context);
        this.iconType = type;
        this.filled = fill;
        this.color = color;
        Paint p = new Paint(1);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        this.strokePaint = p;
        Paint p2 = new Paint(1);
        p2.setStyle(Paint.Style.FILL);
        this.fillPaint = p2;
        this.pathA = new Path();
        this.pathB = new Path();
        setImportantForAccessibility(2);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        A.e("canvas", canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0.0f || height <= 0.0f) return;
        float scale = Math.min(width, height) / 24.0f;
        canvas.save();
        float box = 24.0f * scale;
        canvas.translate((width - box) / 2.0f, (height - box) / 2.0f);
        canvas.scale(scale, scale);
        Paint sp = this.strokePaint;
        sp.setColor(this.color);
        Paint fp = this.fillPaint;
        fp.setColor(this.color);
        sp.setStrokeWidth(this.filled ? 2.1f : 1.7f);
        int t = this.iconType;
        Path a = this.pathA;
        switch (t) {
            case 0: // 主页
                a.reset();
                a.moveTo(3.6f, 10.6f);
                a.lineTo(12.0f, 3.8f);
                a.lineTo(20.4f, 10.6f);
                a.lineTo(20.4f, 20.2f);
                a.lineTo(3.6f, 20.2f);
                a.close();
                Path b = this.pathB;
                b.reset();
                b.moveTo(10.0f, 20.2f);
                b.lineTo(10.0f, 14.6f);
                b.lineTo(14.0f, 14.6f);
                b.lineTo(14.0f, 20.2f);
                b.close();
                if (this.filled) {
                    Path c = new Path();
                    c.op(a, b, Path.Op.DIFFERENCE);
                    canvas.drawPath(c, fp);
                } else {
                    canvas.drawPath(a, sp);
                    canvas.drawPath(b, sp);
                }
                break;
            case 1: { // 菜单/三横线
                float[] xs = {6.6f, 12.0f, 17.4f};
                float[] ys = {9.2f, 15.0f, 7.6f};
                for (int i3 = 0; i3 < 3; i3++) {
                    canvas.drawLine(3.4f, xs[i3], 20.6f, xs[i3], sp);
                    if (this.filled) canvas.drawCircle(ys[i3], xs[i3], 2.5f, fp);
                    else canvas.drawCircle(ys[i3], xs[i3], 2.3f, fp);
                }
                break;
            }
            case 2: // 人
                canvas.drawCircle(12.0f, 12.0f, 8.4f, sp);
                canvas.drawCircle(12.0f, 8.4f, this.filled ? 1.35f : 1.2f, fp);
                canvas.drawLine(12.0f, 11.2f, 12.0f, 16.4f, sp);
                break;
            case 3: // 文档
                a.reset();
                a.moveTo(6.8f, 3.4f);
                a.lineTo(17.2f, 3.4f);
                a.quadTo(19.6f, 3.4f, 19.6f, 5.8f);
                a.lineTo(19.6f, 18.2f);
                a.quadTo(19.6f, 20.6f, 17.2f, 20.6f);
                a.lineTo(6.8f, 20.6f);
                a.quadTo(4.4f, 20.6f, 4.4f, 18.2f);
                a.lineTo(4.4f, 5.8f);
                a.quadTo(4.4f, 3.4f, 6.8f, 3.4f);
                a.close();
                if (this.filled) {
                    Path c = new Path();
                    c.addRect(8.0f, 8.5f, 16.0f, 10.1f, Path.Direction.CW);
                    c.addRect(8.0f, 12.1f, 16.0f, 13.7f, Path.Direction.CW);
                    c.addRect(8.0f, 15.7f, 13.4f, 17.3f, Path.Direction.CW);
                    Path d = new Path();
                    d.op(a, c, Path.Op.DIFFERENCE);
                    canvas.drawPath(d, fp);
                } else {
                    canvas.drawPath(a, sp);
                    canvas.drawLine(8.0f, 9.4f, 16.0f, 9.4f, sp);
                    canvas.drawLine(8.0f, 13.0f, 16.0f, 13.0f, sp);
                    canvas.drawLine(8.0f, 16.6f, 13.4f, 16.6f, sp);
                }
                break;
            default:
                break;
        }
        canvas.restore();
    }
}
