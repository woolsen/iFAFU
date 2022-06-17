package cn.ifafu.ifafu.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BottomDrawerLayout extends FrameLayout {

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    private final ViewDragHelper mBottomDragger;
    private final BottomViewDragCallback mBottomCallback;


    @IntDef(value = {Gravity.BOTTOM}, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    private @interface EdgeGravity {}

    public BottomDrawerLayout(@NonNull Context context) {
        this(context, null);
    }

    public BottomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        mBottomCallback = new BottomViewDragCallback();

        mBottomDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mBottomCallback);
        mBottomDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
        mBottomDragger.setMinVelocity(minVel);
        mBottomCallback.setDragger(mBottomDragger);
    }

    boolean isBottomDrawerView(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        return gravity == Gravity.BOTTOM;
    }

    View findBottomDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int childGravity = ((LayoutParams) child.getLayoutParams()).gravity;
            if (childGravity == Gravity.BOTTOM) {
                return child;
            }
        }
        return null;
    }

    /**
     * Open the bottom drawer by animating it out of view.
     */
    public void openDrawer() {
        final View drawerView = findBottomDrawer();
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity TOP");
        }
        if (!isBottomDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }
        mBottomDragger.smoothSlideViewTo(drawerView, drawerView.getLeft(),
                getHeight() - drawerView.getHeight());
        invalidate();
    }

    /**
     * Close all currently open drawer views by animating them out of view.
     */
    public void closeDrawers() {
        boolean needsInvalidate = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (!isBottomDrawerView(child)) {
                continue;
            }

            needsInvalidate |= mBottomDragger.smoothSlideViewTo(child,
                    child.getLeft(), getHeight());
        }

        if (needsInvalidate) {
            invalidate();
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof FrameLayout.LayoutParams
                ? new LayoutParams((FrameLayout.LayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

            if (isBottomDrawerView(child)) {
                final int childWidth = child.getMeasuredWidth();

                final int height = bottom - top;

                child.layout(0, height - lp.bottomMargin - child.getMeasuredHeight(),
                        childWidth, height - lp.bottomMargin);

                setDrawerViewOffset(child, 1);
            }
        }
    }

    void setDrawerViewOffset(View drawerView, float slideOffset) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (slideOffset == lp.onScreen) {
            return;
        }
        lp.onScreen = slideOffset;
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return mBottomDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mBottomDragger.processTouchEvent(event);

        return true;
    }

    float getDrawerViewOffset(View drawerView) {
        return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mBottomDragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class BottomViewDragCallback extends ViewDragHelper.Callback {

        private ViewDragHelper mDragger;

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return isBottomDrawerView(child);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int height = getHeight();
            return Math.max(height - child.getHeight(), Math.min(top, height));
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
            View child = findBottomDrawer();
            if (child != null) {
                mDragger.captureChildView(child, pointerId);
            }
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {

            final int childHeight = changedView.getHeight();
            final int height = getHeight();
            float offset = (float) (height - top) / childHeight;

            setDrawerViewOffset(changedView, offset);
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            final float offset = getDrawerViewOffset(releasedChild);
            final int childHeight = releasedChild.getHeight();
            final int height = getHeight();
            final int top = xvel < 0 || (xvel == 0  && offset > 0.6f) ? height - childHeight : height;

            mDragger.settleCapturedViewAt(releasedChild.getLeft(), top);
            invalidate();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return isBottomDrawerView(child) ? child.getHeight() : 0;
        }

    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        float onScreen;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
        }
    }
}
