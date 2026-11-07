package io.carplay.app.prototype.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/** 流式布局（移植自参考 APK 的 com.taksss.carlinkmonitor.FlowRow）。 */
public final class FlowRow extends ViewGroup {

    public int gapX;
    public int gapY;
    public boolean alignEnd;
    public int maxWidth;
    public int hitPadX;
    public int hitPadY;
    public View pressedChild;

    public FlowRow(Context context) {
        super(context);
        A.e("c", context);
        this.alignEnd = true;
    }

    private ArrayList<View> visibleChildren() {
        ArrayList<View> list = new ArrayList<>(getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) list.add(v);
        }
        return list;
    }

    public int[] rowCounts(int totalWidth) {
        ArrayList<View> children = visibleChildren();
        ArrayList<Integer> rows = new ArrayList<>();
        int i = 0;
        while (i < children.size()) {
            int rowW = 0;
            int count = 0;
            while (true) {
                int idx = i + count;
                if (idx >= children.size()) break;
                int w = (count > 0 ? this.gapX : 0) + children.get(idx).getMeasuredWidth();
                if (count > 0 && rowW + w > totalWidth) break;
                rowW += w;
                count++;
            }
            if (count == 0) count = 1;
            rows.add(count);
            i += count;
        }
        int[] arr = new int[rows.size()];
        for (int k = 0; k < rows.size(); k++) arr[k] = rows.get(k);
        return arr;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        A.e("ev", ev);
        View target = this.pressedChild;
        if (target != null) {
            ev.setLocation(target.getWidth() / 2.0f, target.getHeight() / 2.0f);
            boolean handled = target.dispatchTouchEvent(ev);
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                this.pressedChild = null;
            }
            return handled;
        }
        if (this.hitPadX > 0 || this.hitPadY > 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < getChildCount(); i++) {
                    View v = getChildAt(i);
                    if (v.getVisibility() == View.VISIBLE
                            && x >= v.getLeft() - this.hitPadX && x < v.getRight() + this.hitPadX
                            && y >= v.getTop() - this.hitPadY && y < v.getBottom() + this.hitPadY) {
                        this.pressedChild = v;
                        ev.setLocation(v.getWidth() / 2.0f, v.getHeight() / 2.0f);
                        return v.dispatchTouchEvent(ev);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int maxW = Math.max(0, ((r - l) - getPaddingLeft()) - getPaddingRight());
        ArrayList<View> children = visibleChildren();
        int[] rows = rowCounts(maxW);
        int top = getPaddingTop();
        int idx = 0;
        for (int rowCount : rows) {
            int rowW = 0;
            int rowH = 0;
            for (int j = 0; j < rowCount; j++) {
                View v = children.get(idx + j);
                rowW += v.getMeasuredWidth() + (j > 0 ? this.gapX : 0);
                rowH = Math.max(rowH, v.getMeasuredHeight());
            }
            int left = this.alignEnd ? (getPaddingLeft() + maxW) - rowW : getPaddingLeft();
            for (int j = 0; j < rowCount; j++) {
                View v = children.get(idx + j);
                if (j > 0) left += this.gapX;
                v.layout(left, top, left + v.getMeasuredWidth(), top + v.getMeasuredHeight());
                left += v.getMeasuredWidth();
            }
            top += rowH + this.gapY;
            idx += rowCount;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxW = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED
                ? Integer.MAX_VALUE
                : Math.max(0, (MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight());
        if (this.maxWidth > 0) maxW = Math.min(maxW, this.maxWidth);
        ArrayList<View> children = visibleChildren();
        for (View v : children) {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            int h = lp != null ? lp.height : ViewGroup.LayoutParams.WRAP_CONTENT;
            v.measure(MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST),
                    h >= 0 ? MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY) : MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
        int[] rows = rowCounts(maxW);
        int totalW = 0;
        int totalH = 0;
        int idx = 0;
        for (int rowCount : rows) {
            int rowW = 0;
            int rowH = 0;
            for (int j = 0; j < rowCount; j++) {
                View v = children.get(idx + j);
                rowW += v.getMeasuredWidth() + (j > 0 ? this.gapX : 0);
                rowH = Math.max(rowH, v.getMeasuredHeight());
            }
            totalW = Math.max(totalW, rowW);
            totalH += (totalH > 0 ? this.gapY : 0) + rowH;
            idx += rowCount;
        }
        setMeasuredDimension(View.resolveSize(getPaddingLeft() + getPaddingRight() + totalW, widthMeasureSpec),
                View.resolveSize(getPaddingTop() + getPaddingBottom() + totalH, heightMeasureSpec));
    }
}
