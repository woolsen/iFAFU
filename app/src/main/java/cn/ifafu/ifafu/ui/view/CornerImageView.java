package cn.ifafu.ifafu.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import cn.ifafu.ifafu.R;

public class CornerImageView extends AppCompatImageView {

    private final Paint mPaint;
    private final Path path;
    private final RectF rect;

    private final float mCornerSize;

    private final float   mStrokeWidth;
    private final int     mStrokeColor;
    private final boolean mStrokeFront;

    public CornerImageView(@NonNull Context context) {
        this(context, null);
    }

    public CornerImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CornerImageView, defStyleAttr, 0);
        mCornerSize = ta.getDimension(R.styleable.CornerImageView_o_cornerSize, 0);
        mStrokeWidth = ta.getDimension(R.styleable.CornerImageView_o_strokeSize, 0);
        mStrokeColor = ta.getColor(R.styleable.CornerImageView_o_strokeColor, -1);
        mStrokeFront = ta.getBoolean(R.styleable.CornerImageView_o_strokeFront, false);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        if (mStrokeColor != -1) {
            mPaint.setColor(mStrokeColor);
        }

        path = new Path();
        rect = new RectF();

        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (!mStrokeFront) {
            drawStroke(canvas);
        }
        super.onDraw(canvas);
        if (mStrokeFront) {
            drawStroke(canvas);
        }
        canvas.restore();
    }

    private void drawStroke(Canvas canvas) {
        path.reset();
        final int width = getWidth();
        final int height = getHeight();
        rect.right = width;
        rect.bottom = height;
        path.addRoundRect(rect, mCornerSize, mCornerSize, Path.Direction.CW);
        canvas.clipPath(path);
        /* draw stroke */
        if (mStrokeColor != -1 && mStrokeWidth != 0) {
            canvas.drawPath(path, mPaint);
        }
    }
}
