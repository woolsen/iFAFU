package cn.ifafu.ifafu.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import cn.ifafu.ifafu.R;

public class PopSeekBar extends AppCompatSeekBar {

    private final Drawable mIndicator;

    private boolean mIsDragging;

    public PopSeekBar(Context context) {
        this(context, null);
    }

    public PopSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public PopSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mIndicator = ContextCompat.getDrawable(context, R.drawable.ic_location);
        if (mIndicator != null) {
            int paddingTop = getPaddingTop() + mIndicator.getIntrinsicHeight();
            setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), getPaddingBottom());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    private void drawIndicator(Canvas canvas) {
        if (mIndicator != null && mIsDragging) {
            final int saveCount = canvas.save();
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            int indicatorOffset = mIndicator.getIntrinsicWidth() / 2;
            canvas.translate(getPaddingLeft() - indicatorOffset, 0);
            mIndicator.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateIndicatorPos(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsDragging = true;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsDragging = false;
                break;
            }
        }
        updateIndicatorPos(getWidth(), getHeight());
        invalidate();
        return result;
    }

    private void updateIndicatorPos(int w, int h) {
        final int paddedHeight = h - getPaddingTop() - getPaddingBottom();
        final Drawable indicator = mIndicator;

        // The max height does not incorporate padding, whereas the height
        // parameter does.
        final int trackHeight;
        if (Build.VERSION.SDK_INT >= 29) {
            trackHeight = Math.min(getMaxHeight(), paddedHeight);
        } else {
            trackHeight = paddedHeight;
        }
        final int thumbHeight = indicator == null ? 0 : indicator.getIntrinsicHeight();

        // Apply offset to whichever item is taller.
        final int thumbOffset;
        if (thumbHeight > trackHeight) {
            thumbOffset = (paddedHeight - thumbHeight) / 2;
        } else {
            final int offsetHeight = (paddedHeight - trackHeight) / 2;
            thumbOffset = offsetHeight + (trackHeight - thumbHeight) / 2;
        }

        if (indicator != null) {
            setIndicatorPos(w, indicator, getScale(), thumbOffset);
        }
    }

    private void setIndicatorPos(int w, Drawable indicator, float scale, int offset) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        int available = w - paddingLeft - paddingRight;  //实际ProgressBar长度

        final int indicatorWidth = indicator.getIntrinsicWidth(); //指示器宽度
        final int indicatorHeight = indicator.getIntrinsicHeight();


        final int indicatorPos = (int) (1F * available * scale + 0.5F);

        final int top, bottom;
        if (offset == Integer.MIN_VALUE) {
            final Rect oldBounds = indicator.getBounds();
            top = oldBounds.top;
            bottom = oldBounds.bottom;
        } else {
            top = offset;
            bottom = offset + indicatorHeight;
        }

        final int left = isLayoutRtl() ? available - indicatorPos : indicatorPos;
        final int right = left + indicatorWidth;

        // Canvas will be translated, so 0,0 is where we start drawing
        indicator.setBounds(left, top, right, bottom);
    }


    private boolean isLayoutRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private float getScale() {
        int min = getMinimum();
        int max = getMax();
        int range = max - min;
        return range > 0 ? (getProgress() - min) / (float) range : 0;
    }

    private int getMinimum() {
        return Build.VERSION.SDK_INT >= 26 ? getMin() : 0;
    }

    private int getMaximumHeight() {
        return Build.VERSION.SDK_INT >= 29 ? getMaxHeight() : Integer.MAX_VALUE;
    }

    private int getMaximumWidth() {
        return Build.VERSION.SDK_INT >= 29 ? getMaxWidth() : Integer.MAX_VALUE;
    }
}
