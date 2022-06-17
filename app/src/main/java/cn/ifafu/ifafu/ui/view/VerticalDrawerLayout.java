package cn.ifafu.ifafu.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class VerticalDrawerLayout extends FrameLayout {

    private static final String TAG = "VDrawerLayout";

    static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.layout_gravity
    };

    /**
     * Whether we can use NO_HIDE_DESCENDANTS accessibility importance.
     */
    static final boolean CAN_HIDE_DESCENDANTS = Build.VERSION.SDK_INT >= 19;

    /**
     * Minimum velocity that will be detected as a fling
     */

    private static final int MIN_FLING_VELOCITY = 400; // dips per second

    /**
     * Experimental feature.
     */
    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    private float mScrimOpacity;

    private final ViewDragHelper mTopDragger;
    private final ViewDragHelper mBottomDragger;
    private final ViewDragCallback mTopCallback;
    private final ViewDragCallback mBottomCallback;

    private float mInitialMotionX;
    private float mInitialMotionY;
    private CharSequence mTitleTop;
    private CharSequence mTitleBottom;

    public VerticalDrawerLayout(Context context) {
        this(context, null);
    }

    public VerticalDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        mTopCallback = new ViewDragCallback(Gravity.TOP);
        mBottomCallback = new ViewDragCallback(Gravity.BOTTOM);

        mTopDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mTopCallback);
        mTopDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
        mTopDragger.setMinVelocity(minVel);
        mTopCallback.setDragger(mTopDragger);

        mBottomDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mBottomCallback);
        mBottomDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
        mBottomDragger.setMinVelocity(minVel);
        mBottomCallback.setDragger(mBottomDragger);

        // So that we can catch the back button
        setFocusableInTouchMode(true);

        ViewCompat.setImportantForAccessibility(this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate());
        setMotionEventSplittingEnabled(false);
    }

    /**
     * Simple gravity to string - only supports LEFT and RIGHT for debugging output.
     *
     * @param gravity Absolute gravity value
     * @return LEFT or RIGHT as appropriate, or a hex string
     */
    static String gravityToString(@EdgeGravity int gravity) {
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            return "TOP";
        }
        if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            return "BOTTOM";
        }
        return Integer.toHexString(gravity);
    }

    static boolean includeChildForAccessibility(View child) {
        // If the child is not important for accessibility we make
        // sure this hides the entire subtree rooted at it as the
        // IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDATS is not
        // supported on older platforms but we want to hide the entire
        // content and not opened drawers if a drawer is opened.
        return ViewCompat.getImportantForAccessibility(child)
                != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                && ViewCompat.getImportantForAccessibility(child)
                != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO;
    }

    /**
     * Sets the title of the drawer with the given gravity.
     * <p>
     * When accessibility is turned on, this is the title that will be used to
     * identify the drawer to the active accessibility service.
     *
     * @param edgeGravity Gravity.TOP, BOTTOM. Expresses which
     *                    drawer to set the title for.
     * @param title       The title for the drawer.
     */
    public void setDrawerTitle(@EdgeGravity int edgeGravity, @Nullable CharSequence title) {
        if (edgeGravity == Gravity.TOP) {
            mTitleTop = title;
        } else if (edgeGravity == Gravity.BOTTOM) {
            mTitleBottom = title;
        }
    }

    /**
     * Returns the title of the drawer with the given gravity.
     *
     * @param edgeGravity Gravity.TOP, BOTTOM. Expresses which drawer to
     *                    return the title for.
     * @return The title of the drawer, or null if none set.
     * @see #setDrawerTitle(int, CharSequence)
     */
    @Nullable
    public CharSequence getDrawerTitle(@EdgeGravity int edgeGravity) {
        if (edgeGravity == Gravity.TOP) {
            return mTitleTop;
        } else if (edgeGravity == Gravity.BOTTOM) {
            return mTitleBottom;
        }
        return null;
    }

    void setDrawerViewOffset(View drawerView, float slideOffset) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (slideOffset == lp.onScreen) {
            return;
        }

        lp.onScreen = slideOffset;
    }

    float getDrawerViewOffset(View drawerView) {
        return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
    }

    /**
     * @return the absolute gravity of the child drawerView, resolved according
     * to the current layout direction
     */
    int getDrawerViewAbsoluteGravity(View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return gravity & Gravity.VERTICAL_GRAVITY_MASK;
    }

    boolean checkDrawerViewAbsoluteGravity(View drawerView, int checkFor) {
        final int absGravity = getDrawerViewAbsoluteGravity(drawerView);
        return (absGravity & checkFor) == checkFor;
    }

    View findOpenDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
            if ((childLp.openState & LayoutParams.FLAG_IS_OPENED) == 1) {
                return child;
            }
        }
        return null;
    }

    void moveDrawerToOffset(View drawerView, float slideOffset) {
        final float oldOffset = getDrawerViewOffset(drawerView);
        final int height = drawerView.getHeight();
        final int oldPos = (int) (height * oldOffset);
        final int newPos = (int) (height * slideOffset);
        final int dy = newPos - oldPos;

        drawerView.offsetTopAndBottom(
                checkDrawerViewAbsoluteGravity(drawerView, Gravity.TOP) ? dy : -dy);
        setDrawerViewOffset(drawerView, slideOffset);
    }

    /**
     * @param gravity the gravity of the child to return. If specified as a
     *                relative value, it will be resolved according to the current
     *                layout direction.
     * @return the drawer with the specified gravity
     */
    View findDrawerWithGravity(int gravity) {
        final int absVerGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int childAbsGravity = getDrawerViewAbsoluteGravity(child);
            if ((childAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) == absVerGravity) {
                return child;
            }
        }
        return null;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int height = b - t;
        final int layoutDirection = ViewCompat.getLayoutDirection(this);

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
            } else { // Drawer, if it wasn't onMeasure would have thrown an exception.
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                int childTop;

                final float newOffset;
                if (checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
                    childTop = -childHeight + (int) (childHeight * lp.onScreen);
                    newOffset = (float) (childHeight + childTop) / childHeight;
                } else { // Right; onMeasure checked for us.
                    childTop = height - (int) (childHeight * lp.onScreen);
                    newOffset = (float) (height - childTop) / childHeight;
                }

                final boolean changeOffset = newOffset != lp.onScreen;
                final int horizontalGravity = GravityCompat.getAbsoluteGravity(
                        lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK,
                        layoutDirection);

                switch (horizontalGravity) {
                    default:
                    case Gravity.LEFT: {
                        child.layout(lp.leftMargin, childTop, lp.leftMargin + childWidth,
                                childTop + childHeight);
                        break;
                    }

                    case Gravity.RIGHT: {
                        final int width = r - l;
                        child.layout(width - lp.rightMargin - child.getMeasuredWidth(),
                                childTop,
                                width - lp.rightMargin,
                                childTop + childHeight);
                        break;
                    }

                    case Gravity.CENTER_HORIZONTAL: {
                        final int width = r - l;
                        int childLeft = (width - childWidth) / 2;

                        // Offset for margins. If things don't fit right because of
                        // bad measurement before, oh well.
                        if (childLeft < lp.leftMargin) {
                            childLeft = lp.leftMargin;
                        } else if (childLeft + childWidth > width - lp.rightMargin) {
                            childLeft = width - lp.rightMargin - childWidth;
                        }
                        child.layout(childLeft, childTop, childLeft + childWidth,
                                childTop + childHeight);
                        break;
                    }
                }

                if (changeOffset) {
                    setDrawerViewOffset(child, newOffset);
                }

//                final int newVisibility = lp.onScreen > 0 ? VISIBLE : INVISIBLE;
//                if (child.getVisibility() != newVisibility) {
//                    child.setVisibility(newVisibility);
//                }
            }
        }
    }

    @Override
    public void computeScroll() {
        final int childCount = getChildCount();
        float scrimOpacity = 0;
        for (int i = 0; i < childCount; i++) {
            final float onscreen = ((LayoutParams) getChildAt(i).getLayoutParams()).onScreen;
            scrimOpacity = Math.max(scrimOpacity, onscreen);
        }
        mScrimOpacity = scrimOpacity;
//        System.out.println("computeScroll: scrimOpacity = " + scrimOpacity);

        boolean topDraggerSettling = mTopDragger.continueSettling(true);
        boolean bottomDraggerSettling = mBottomDragger.continueSettling(true);
        if (topDraggerSettling || bottomDraggerSettling) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    boolean isDrawerView(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        if ((absGravity & Gravity.TOP) != 0) {
            // This child is a left-edge drawer
            return true;
        }
        // This child is a right-edge drawer
        return (absGravity & Gravity.BOTTOM) != 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        // "|" used deliberately here; both methods should be invoked.
        final boolean interceptForDrag = mTopDragger.shouldInterceptTouchEvent(ev)
                | mBottomDragger.shouldInterceptTouchEvent(ev);

        boolean interceptForTap = false;

//        System.out.println("onInterceptTouchEvent: ev = " + ev);
//        System.out.println("mScrimOpacity: " + mScrimOpacity);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                if (mScrimOpacity > 0) {
                    final View child = mTopDragger.findTopChildUnder((int) x, (int) y);
                    if (child != null && isContentView(child)) {
                        interceptForTap = true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
//                   closeDrawers(true);
            }
        }

        return interceptForDrag || interceptForTap;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mTopDragger.processTouchEvent(ev);
        mBottomDragger.processTouchEvent(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                final float x = ev.getX();
                final float y = ev.getY();
                boolean peekingOnly = true;
                final View touchedView = mTopDragger.findTopChildUnder((int) x, (int) y);
                if (touchedView != null && isContentView(touchedView)) {
                    final float dx = x - mInitialMotionX;
                    final float dy = y - mInitialMotionY;
                    final int slop = mTopDragger.getTouchSlop();
                    if (dx * dx + dy * dy < slop * slop) {
                        // Taps close a dimmed open drawer but only if it isn't locked open.
                        final View openDrawer = findOpenDrawer();
//                        if (openDrawer != null) {
//                            peekingOnly = getDrawerLockMode(openDrawer) == LOCK_MODE_LOCKED_OPEN;
//                        }
                        closeDrawers(peekingOnly);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
//                closeDrawers(true);
//                mDisallowInterceptRequested = false;
//                mChildrenCanceledTouch = false;
                break;
            }
        }

        return true;
    }

    /**
     * Open the specified drawer view by animating it into view.
     *
     * @param drawerView Drawer view to open
     */
    public void openDrawer(@NonNull View drawerView) {
        openDrawer(drawerView, true);
    }

    /**
     * Open the specified drawer view.
     *
     * @param drawerView Drawer view to open
     * @param animate    Whether opening of the drawer should be animated.
     */
    public void openDrawer(@NonNull View drawerView, boolean animate) {
        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }

        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
//        if (mFirstLayout) {
//            lp.onScreen = 1.f;
//            lp.openState = LayoutParams.FLAG_IS_OPENED;
//
//            updateChildrenImportantForAccessibility(drawerView, true);
//        } else
        if (animate) {
            lp.onScreen = 1.f;
            lp.openState |= LayoutParams.FLAG_IS_OPENING;

            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.TOP)) {
                mTopDragger.smoothSlideViewTo(drawerView, drawerView.getLeft(), 0);
            } else {
                mBottomDragger.smoothSlideViewTo(drawerView, drawerView.getLeft(),
                        getHeight() - drawerView.getHeight());
            }
        } else {
            moveDrawerToOffset(drawerView, 1.f);
//            updateDrawerState(lp.gravity, STATE_IDLE, drawerView);
            drawerView.setVisibility(VISIBLE);
        }
        invalidate();
    }

    /**
     * Open the specified drawer by animating it out of view.
     *
     * @param gravity Gravity.TOP to move the top drawer or Gravity.BOTTOM for the bottom.
     */
    public void openDrawer(@EdgeGravity int gravity) {
        openDrawer(gravity, true);
    }

    /**
     * Open the specified drawer.
     *
     * @param gravity Gravity.TOP to move the top drawer or Gravity.BOTTOM for the bottom.
     * @param animate Whether opening of the drawer should be animated.
     */
    public void openDrawer(@EdgeGravity int gravity, boolean animate) {
        final View drawerView = findDrawerWithGravity(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity "
                    + gravityToString(gravity));
        }
        openDrawer(drawerView, animate);
    }

    /**
     * Close all currently open drawer views by animating them out of view.
     */
    public void closeDrawers() {
        closeDrawers(false);
    }

    void closeDrawers(boolean peekingOnly) {
        boolean needsInvalidate = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (!isDrawerView(child)) {
//            if (!isDrawerView(child) || (peekingOnly && !lp.isPeeking)) {
                continue;
            }

            final int childHeight = child.getHeight();

            if (checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
                needsInvalidate |= mTopDragger.smoothSlideViewTo(child,
                        child.getLeft(), -childHeight);
            } else {
                needsInvalidate |= mBottomDragger.smoothSlideViewTo(child,
                        child.getLeft(), getHeight());
            }

//            lp.isPeeking = false;
        }

//        mTopDragger.removeCallbacks();
//        mBottomDragger.removeCallbacks();

        if (needsInvalidate) {
            invalidate();
        }
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. To check for partial visibility use
     * {@link #isDrawerVisible(View)}.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer view is in an open state
     * @see #isDrawerVisible(View)
     */
    public boolean isDrawerOpen(@NonNull View drawer) {
        if (!isDrawerView(drawer)) {
            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        LayoutParams drawerLp = (LayoutParams) drawer.getLayoutParams();
        return (drawerLp.openState & LayoutParams.FLAG_IS_OPENED) == 1;
    }

    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    boolean isContentView(View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }


    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere inbetween.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer is visible on-screen
     * @see #isDrawerOpen(View)
     */
    public boolean isDrawerVisible(@NonNull View drawer) {
        if (!isDrawerView(drawer)) {
            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        return ((LayoutParams) drawer.getLayoutParams()).onScreen > 0;
    }

    View findVisibleDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (isDrawerView(child) && isDrawerVisible(child)) {
                return child;
            }
        }
        return null;
    }

    @IntDef(value = {Gravity.TOP, Gravity.BOTTOM}, flag = true)
    @Retention(RetentionPolicy.SOURCE)
    private @interface EdgeGravity {
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        private static final int FLAG_IS_OPENED = 0x1;
        private static final int FLAG_IS_OPENING = 0x2;
        private static final int FLAG_IS_CLOSING = 0x4;

        public int gravity = Gravity.NO_GRAVITY;
        float onScreen;
        boolean isPeeking;
        int openState;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
            this.gravity = gravity;
        }

        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }
    }

    static final class ChildAccessibilityDelegate extends AccessibilityDelegateCompat {
        @Override
        public void onInitializeAccessibilityNodeInfo(View child,
                                                      AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(child, info);

            if (!includeChildForAccessibility(child)) {
                // If we are ignoring the sub-tree rooted at the child,
                // break the connection to the rest of the node tree.
                // For details refer to includeChildForAccessibility.
                info.setParent(null);
            }
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        private final int mGravity;
        private ViewDragHelper mDragger;

        ViewDragCallback(int gravity) {
            mGravity = gravity;
        }

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            // Only capture views where the gravity matches what we're looking for.
            // This lets us use two ViewDragHelpers, one for each side drawer.
            return isDrawerView(child) && checkDrawerViewAbsoluteGravity(child, mGravity);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float offset;
            final int childHeight = changedView.getHeight();

            // This reverses the positioning shown in onLayout.
            if (checkDrawerViewAbsoluteGravity(changedView, Gravity.TOP)) {
                offset = (float) (childHeight + top) / childHeight;
            } else {
                final int height = getHeight();
                offset = (float) (height - top) / childHeight;
            }
            setDrawerViewOffset(changedView, offset);
            changedView.setVisibility(offset == 0 ? INVISIBLE : VISIBLE);
            invalidate();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            // Offset is how open the drawer is, therefore top/bottom values
            // are reversed from one another.
            final float offset = getDrawerViewOffset(releasedChild);
            final int childHeight = releasedChild.getHeight();

            int top;
            if (checkDrawerViewAbsoluteGravity(releasedChild, Gravity.TOP)) {
                top = yvel > 0 || (yvel == 0 && offset > 0.5f) ? 0 : -childHeight;
            } else {
                final int height = getHeight();
                top = yvel < 0 || (yvel == 0 && offset > 0.5f) ? height - childHeight : height;
            }

            mDragger.settleCapturedViewAt(releasedChild.getLeft(), top);
            invalidate();
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            final View toCapture;
            if ((edgeFlags & ViewDragHelper.EDGE_TOP) == ViewDragHelper.EDGE_TOP) {
                toCapture = findDrawerWithGravity(Gravity.TOP);
            } else {
                toCapture = findDrawerWithGravity(Gravity.BOTTOM);
            }

            if (toCapture != null) {
                mDragger.captureChildView(toCapture, pointerId);
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return isDrawerView(child) ? child.getHeight() : 0;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (checkDrawerViewAbsoluteGravity(child, Gravity.TOP)) {
                return Math.max(-child.getHeight(), Math.min(top, 0));
            } else {
                final int height = getHeight();
                return Math.max(height - child.getHeight(), Math.min(top, height));
            }
        }
    }

    class AccessibilityDelegate extends AccessibilityDelegateCompat {
        private final Rect mTmpRect = new Rect();

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            if (CAN_HIDE_DESCENDANTS) {
                super.onInitializeAccessibilityNodeInfo(host, info);
            } else {
                // Obtain a node for the host, then manually generate the list
                // of children to only include non-obscured views.
                final AccessibilityNodeInfoCompat superNode =
                        AccessibilityNodeInfoCompat.obtain(info);
                super.onInitializeAccessibilityNodeInfo(host, superNode);

                info.setSource(host);
                final ViewParent parent = ViewCompat.getParentForAccessibility(host);
                if (parent instanceof View) {
                    info.setParent((View) parent);
                }
                copyNodeInfoNoChildren(info, superNode);
                superNode.recycle();

                addChildrenForAccessibility(info, (ViewGroup) host);
            }

            info.setClassName(DrawerLayout.class.getName());

            // This view reports itself as focusable so that it can intercept
            // the back button, but we should prevent this view from reporting
            // itself as focusable to accessibility services.
            info.setFocusable(false);
            info.setFocused(false);
            info.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_FOCUS);
            info.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLEAR_FOCUS);
        }

        @Override
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);

            event.setClassName(DrawerLayout.class.getName());
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            // Special case to handle window state change events. As far as
            // accessibility services are concerned, state changes from
            // DrawerLayout invalidate the entire contents of the screen (like
            // an Activity or Dialog) and they should announce the title of the
            // new content.
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                final List<CharSequence> eventText = event.getText();
                final View visibleDrawer = findVisibleDrawer();
                if (visibleDrawer != null) {
                    final int edgeGravity = getDrawerViewAbsoluteGravity(visibleDrawer);
                    final CharSequence title = getDrawerTitle(edgeGravity);
                    if (title != null) {
                        eventText.add(title);
                    }
                }

                return true;
            }

            return super.dispatchPopulateAccessibilityEvent(host, event);
        }

        @Override
        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
                                                       AccessibilityEvent event) {
            if (CAN_HIDE_DESCENDANTS || includeChildForAccessibility(child)) {
                return super.onRequestSendAccessibilityEvent(host, child, event);
            }
            return false;
        }

        private void addChildrenForAccessibility(AccessibilityNodeInfoCompat info, ViewGroup v) {
            final int childCount = v.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = v.getChildAt(i);
                if (includeChildForAccessibility(child)) {
                    info.addChild(child);
                }
            }
        }

        /**
         * This should really be in AccessibilityNodeInfoCompat, but there unfortunately
         * seem to be a few elements that are not easily cloneable using the underlying API.
         * Leave it private here as it's not general-purpose useful.
         */
        private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat dest,
                                            AccessibilityNodeInfoCompat src) {
            final Rect rect = mTmpRect;

            src.getBoundsInParent(rect);
            dest.setBoundsInParent(rect);

            src.getBoundsInScreen(rect);
            dest.setBoundsInScreen(rect);

            dest.setVisibleToUser(src.isVisibleToUser());
            dest.setPackageName(src.getPackageName());
            dest.setClassName(src.getClassName());
            dest.setContentDescription(src.getContentDescription());

            dest.setEnabled(src.isEnabled());
            dest.setClickable(src.isClickable());
            dest.setFocusable(src.isFocusable());
            dest.setFocused(src.isFocused());
            dest.setAccessibilityFocused(src.isAccessibilityFocused());
            dest.setSelected(src.isSelected());
            dest.setLongClickable(src.isLongClickable());

            dest.addAction(src.getActions());
        }
    }
}
