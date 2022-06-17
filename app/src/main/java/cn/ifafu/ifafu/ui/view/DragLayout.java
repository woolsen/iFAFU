package cn.ifafu.ifafu.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.nineoldandroids.view.ViewHelper;

import cn.ifafu.ifafu.util.DensityUtils;


/**
 * 自定义侧面菜单布局
 * Created by BlueMor
 * https://github.com/BlueMor/DragLayout
 */
public class DragLayout extends FrameLayout {

    private int range;//排除主页面的左侧页面宽度
    private int width;
    private int height;
    private int mainLeft;
    private LinearLayout mLeftLayout;
    private MyLinearLayout mMainLayout;
    private Status status = Status.Close;

    private final int dp200;

    private final ViewDragHelper mDragHelper;
    private final ViewDragHelper.Callback mDragCallback = new DragCallback();
    private final GestureDetectorCompat mGestureDetector;
    private DragListener mDragListener = new DragListener() {
        @Override
        public void onOpen() {

        }

        @Override
        public void onClose() {

        }

        @Override
        public void onDrag(float percent) {

        }
    };

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetectorCompat(context,
                new YScrollDetector());
        mDragHelper = ViewDragHelper.create(this, mDragCallback);
        dp200 = DensityUtils.dp2px(context, 200);
    }

    public void setDragListener(DragListener mDragListener) {
        this.mDragListener = mDragListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftLayout = (LinearLayout) getChildAt(0);
        mMainLayout = (MyLinearLayout) getChildAt(1);
        mMainLayout.setDragLayout(this);
        mLeftLayout.setClickable(true);
        mMainLayout.setClickable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = mLeftLayout.getMeasuredWidth();
        height = mLeftLayout.getMeasuredHeight();
        //range = (int) (width * 0.6f);
        //改良贴近美工图  最大滑动范围为宽度减200 但是主页面会进行缩小0.7倍所以最终左侧显示大小为宽度-140（200*0.7） 在layout文件中右边距为140dp
        range = width - dp200;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mLeftLayout.layout(0, 0, width, height);
        mMainLayout.layout(mainLeft, 0, mainLeft + width, height);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev)
                && mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            mDragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void dispatchDragEvent(int mainLeft) {
        //mianleft 左侧手指拖动的宽度
        if (mDragListener == null) {
            return;
        }
        float percent = mainLeft / (float) range;//已经滑动的范围占可滑动范围的百分比
        animateView(percent);
        mDragListener.onDrag(percent);
        Status lastStatus = status;
        if (lastStatus != getStatus() && status == Status.Close) {
            mDragListener.onClose();
        } else if (lastStatus != getStatus() && status == Status.Open) {
            mDragListener.onOpen();
        }
    }

    private void animateView(float percent) {
        float f1 = 1 - percent * 0.3f;
        ViewHelper.setScaleX(mMainLayout, f1);   //主布局逐渐变大
        ViewHelper.setScaleY(mMainLayout, f1);
        ViewHelper.setTranslationX(mLeftLayout, -mLeftLayout.getWidth() / 2.3f
                + mLeftLayout.getWidth() / 2.3f * percent);
        ViewHelper.setScaleX(mLeftLayout, 0.5f + 0.5f * percent);//左边的布局逐渐变小
        ViewHelper.setScaleY(mLeftLayout, 0.5f + 0.5f * percent);
        ViewHelper.setAlpha(mLeftLayout, percent);
        getBackground().setColorFilter(
                evaluate(percent, Color.BLACK, Color.TRANSPARENT),
                Mode.SRC_OVER);
    }

    private Integer evaluate(float fraction, Object startValue, Integer endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24)
                | ((startR + (int) (fraction * (endR - startR))) << 16)
                | ((startG + (int) (fraction * (endG - startG))) << 8)
                | (startB + (int) (fraction * (endB - startB)));
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public Status getStatus() {
        if (mainLeft == 0) {
            status = Status.Close;
        } else if (mainLeft == range) {
            status = Status.Open;
        } else {
            status = Status.Drag;
        }
        return status;
    }

    public void open() {
        open(true);
    }

    public void open(boolean animate) {
        if (animate) {
            if (mDragHelper.smoothSlideViewTo(mMainLayout, range, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainLayout.layout(range, 0, range * 2, height);
            dispatchDragEvent(range);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean animate) {
        if (animate) {
            if (mDragHelper.smoothSlideViewTo(mMainLayout, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainLayout.layout(0, 0, width, height);
            dispatchDragEvent(0);
        }
    }

    private boolean isInIgnoredView(MotionEvent ev, View v) {
        if (v == null) {
            return false;
        }
        Rect rect = new Rect();
        v.getHitRect(rect);

        return rect.contains((int) ev.getX(), (int) ev.getY());

    }

    public enum Status {
        Drag, Open, Close
    }

    public interface DragListener {
        void onOpen();

        void onClose();

        void onDrag(float percent);
    }

    static class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx,
                                float dy) {
            return Math.abs(dy) <= Math.abs(dx);
        }
    }

    private class DragCallback extends ViewDragHelper.Callback {

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (mainLeft + dx < 0) {
                return 0;
            } else if (mainLeft + dx > range) {
                return range;
            } else {
                return left;
            }
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return width;
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (xvel > 0) {
                open();
            } else if (xvel < 0) {
                close();
            } else if (releasedChild == mMainLayout && mainLeft > range * 0.3) {
                open();
            } else if (releasedChild == mLeftLayout && mainLeft > range * 0.7) {
                open();
            } else {
                close();
            }
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top,
                                          int dx, int dy) {
            if (changedView == mMainLayout) {
                mainLeft = left;
            } else {
                mainLeft = mainLeft + left;
            }
            if (mainLeft < 0) {
                mainLeft = 0;
            } else if (mainLeft > range) {
                mainLeft = range;
            }

            if (changedView == mLeftLayout) {
                mLeftLayout.layout(0, 0, width, height);
                mMainLayout.layout(mainLeft, 0, mainLeft + width, height);
            }

            dispatchDragEvent(mainLeft);
        }
    }
}
