package cn.ifafu.ifafu.ui.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.appcompat.widget.AppCompatSeekBar;

import cn.ifafu.ifafu.R;

public class PopWindowSeekBar extends AppCompatSeekBar {

    private final PopupWindow mPopupWindow;
    private       int         mShowProgress = -1;

    public PopWindowSeekBar(Context context) {
        this(context, null);
    }

    public PopWindowSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public PopWindowSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPopupWindow = new PopupWindow(getIndicator(), 100, 100);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                showIndicator();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                dismissIndicator();
                break;
            }
        }

        return result;
    }

    private View getIndicator() {
        ImageView view = new ImageView(getContext());
        view.setImageResource(R.drawable.ic_location);
        return view;
    }

    private void showIndicator() {
        int progress = getProgress();
        if (mShowProgress != progress) {
            mPopupWindow.showAtLocation(this, Gravity.TOP, 0, (int) getY());
            mShowProgress = progress;
        }
    }

    private void dismissIndicator() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mShowProgress = -1;
        }
    }
}
