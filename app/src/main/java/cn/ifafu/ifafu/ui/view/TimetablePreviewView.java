package cn.ifafu.ifafu.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.List;

import cn.ifafu.ifafu.ui.view.timetable.TimetableItem;
import cn.ifafu.ifafu.util.ColorUtils;

public class TimetablePreviewView extends AppCompatImageView {

    private final Paint mPaint;
    private final Rect rect;

    private static final int ROW_COUNT = 10;

    private List<TimetableItem> items;

    public TimetablePreviewView(Context context) {
        this(context, null);
    }

    public TimetablePreviewView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimetablePreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        rect = new Rect();
    }

    public void setItems(List<TimetableItem> items) {
        this.items = items;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (items == null || items.isEmpty()) {
            return;
        }

        final int width = getWidth();
        final int height = getHeight();
        final float itemHeight = 1.f * height / ROW_COUNT;
        final float itemWidth = 1.f * width / 7;

        for (int i = 0; i < items.size(); i++) {
            canvas.save();
            TimetableItem item = items.get(i);
            final float dx = itemWidth * (item.dayOfWeek - 1);
            final float dy = itemHeight * (item.startNode - 1);
            canvas.translate(dx, dy);

            mPaint.setColor(getRandomColor());
            rect.set(0, 0, (int) (itemWidth + 0.5f), (int) (itemHeight * item.nodeCount + 0.5f));
            canvas.drawRect(rect, mPaint);

            canvas.restore();
        }
    }

    @ColorInt
    private int getRandomColor() {
        return ColorUtils.getRandomLightColor();
    }
}
