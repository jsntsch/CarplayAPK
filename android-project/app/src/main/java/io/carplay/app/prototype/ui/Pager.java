package io.carplay.app.prototype.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.PathInterpolator;

/**
 * 横向翻页容器（移植自参考 APK 的 Pager）。
 * 支持滑动翻页 + setCurrentItem(int) + 页面切换监听。不依赖 media3。
 */
public final class Pager extends ViewGroup {

    public interface OnPageChangeListener {
        void onPageSelected(int index);
    }

    private final long animDuration = 260L;
    private final PathInterpolator interpolator = new PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f);
    private OnPageChangeListener listener;
    private int currentItem;
    private final int touchSlop;
    private final int minFlingVelocity;
    private float downX;
    private float downY;
    private float startScrollX;
    private boolean dragging;
    private boolean verticalLocked;
    private VelocityTracker velocityTracker;
    private ValueAnimator animator;

    public Pager(Context context) {
        super(context);
        A.e("c", context);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.minFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    public void setOnPageChangeListener(OnPageChangeListener l) {
        this.listener = l;
    }

    public int getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(int item) {
        int count = getChildCount();
        if (count == 0 || getWidth() == 0) {
            this.currentItem = Dp.clamp(item, 0, count - 1);
            return;
        }
        int target = Dp.clamp(item, 0, count - 1);
        float width = target * getWidth();
        if (getScrollX() == width) {
            if (target != this.currentItem) {
                this.currentItem = target;
                if (listener != null) listener.onPageSelected(target);
            }
            return;
        }
        if (animator != null) animator.cancel();
        ValueAnimator va = ValueAnimator.ofFloat(getScrollX(), width);
        va.setDuration(animDuration);
        va.setInterpolator(interpolator);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (Float) animation.getAnimatedValue();
                scrollTo(Dp.round(v), 0);
            }
        });
        va.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (target != currentItem) {
                    currentItem = target;
                    if (listener != null) listener.onPageSelected(target);
                }
            }
        });
        animator = va;
        va.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        if (velocityTracker != null) velocityTracker.recycle();
        velocityTracker = null;
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        A.e("ev", ev);
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (animator != null) animator.cancel();
            if (velocityTracker != null) velocityTracker.recycle();
            velocityTracker = VelocityTracker.obtain();
            downX = ev.getX();
            downY = ev.getY();
            startScrollX = getScrollX();
            dragging = false;
            verticalLocked = false;
            return false;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
            dragging = false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (verticalLocked) return false;
            float dx = ev.getX() - downX;
            float dy = ev.getY() - downY;
            boolean wasDragging = dragging;
            if (!wasDragging && Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                dragging = true;
                ViewParent parent = getParent();
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
                if (velocityTracker != null) velocityTracker.addMovement(ev);
                return true;
            }
            if (!dragging && Math.abs(dy) > touchSlop) {
                verticalLocked = true;
            }
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            int left = i * w;
            getChildAt(i).layout(left, 0, left + w, h);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeW = MeasureSpec.getSize(widthMeasureSpec);
        int sizeH = MeasureSpec.getSize(heightMeasureSpec);
        int wSpec = MeasureSpec.makeMeasureSpec(sizeW, MeasureSpec.EXACTLY);
        int hSpec = MeasureSpec.makeMeasureSpec(sizeH, MeasureSpec.EXACTLY);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(wSpec, hSpec);
        }
        setMeasuredDimension(sizeW, sizeH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && oldw > 0) {
            scrollTo(currentItem * w, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        A.e("ev", ev);
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (animator != null) animator.cancel();
            if (velocityTracker != null) velocityTracker.recycle();
            velocityTracker = VelocityTracker.obtain();
            downX = ev.getX();
            downY = ev.getY();
            startScrollX = getScrollX();
            dragging = false;
            verticalLocked = false;
            return true;
        }
        float clamped = 0.0f;
        if (action == MotionEvent.ACTION_UP) {
            if (dragging) {
                if (velocityTracker != null) velocityTracker.addMovement(ev);
                if (velocityTracker != null) velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = velocityTracker != null ? velocityTracker.getXVelocity() : 0.0f;
                int count = getChildCount();
                int target;
                if (Math.abs(xVelocity) <= minFlingVelocity) {
                    target = Dp.clamp(Dp.round(((getWidth() / 2.0f) + getScrollX()) / getWidth()), 0, count - 1);
                } else if (xVelocity < 0.0f) {
                    target = currentItem + 1;
                    if (target > count - 1) target = count - 1;
                } else {
                    target = currentItem - 1;
                    if (target < 0) target = 0;
                }
                setCurrentItem(target);
            }
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
            dragging = false;
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            if (dragging) {
                setCurrentItem(Dp.clamp(Dp.round(((getWidth() / 2.0f) + getScrollX()) / getWidth()), 0, getChildCount() - 1));
            }
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
            dragging = false;
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (!dragging) {
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                if (Math.abs(dx) <= touchSlop || Math.abs(dx) <= Math.abs(dy)) {
                    return true;
                }
                dragging = true;
                ViewParent parent = getParent();
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
            }
            if (velocityTracker != null) velocityTracker.addMovement(ev);
            float maxScroll = getWidth() * (getChildCount() - 1);
            float next = startScrollX - (ev.getX() - downX);
            if (maxScroll <= 0.0f) {
                float value = next >= 0.0f ? next : 0.0f;
                scrollTo(Dp.round(value), 0);
            } else {
                float value = next < 0.0f ? 0.0f : (next > maxScroll ? maxScroll : next);
                scrollTo(Dp.round(value), 0);
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }
}
