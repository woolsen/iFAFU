package cn.ifafu.ifafu.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import cn.ifafu.ifafu.R;
import cn.ifafu.ifafu.util.DensityUtils;

public class EmptyView extends LinearLayout {

    private TextView mTitleTextView;

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.EmptyView, defStyleAttr, 0);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        ImageView mLogoImageView = new AppCompatImageView(context);
        mLogoImageView.setImageResource(R.drawable.icon_empty);
        int dp200 = DensityUtils.dp2px(context, 128);
        LayoutParams lp = new LayoutParams(dp200, dp200);
        mLogoImageView.setLayoutParams(lp);
        addView(mLogoImageView);

        final CharSequence title = a.getText(R.styleable.EmptyView_message);
        setTitle(title);

        a.recycle();
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            ensureTitleView();
            mTitleTextView.setText(title);
        }
    }

    private void ensureTitleView() {
        if (mTitleTextView == null) {
            Context context = getContext();
            mTitleTextView = new AppCompatTextView(context);
            mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            mTitleTextView.setTextColor(Color.GRAY);
            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = DensityUtils.dp2px(getContext(), 8);
            mTitleTextView.setLayoutParams(lp);
            addView(mTitleTextView);
        }
    }
}
