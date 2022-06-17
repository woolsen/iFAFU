package cn.ifafu.ifafu.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import cn.ifafu.ifafu.R;
import cn.ifafu.ifafu.util.DensityUtils;
import cn.ifafu.ifafu.util.GlobalLib;

public class WoToolbar extends RelativeLayout {

    private ImageButton mNavButtonView;
    private TextView mTitleTextView;
    private TextView mSubtitleTextView;

    private final int mTitleTextAppearance;
    private final int mSubtitleTextAppearance;

    private CharSequence mTitleText;
    private CharSequence mSubtitleText;

    private ColorStateList mTitleTextColor;
    private ColorStateList mSubtitleTextColor;

    private LinearLayout mTitleLinearLayout;

    private OnClickListener mTitleClickListener;
    private OnClickListener mSubtitleClickListener;

    public WoToolbar(Context context) {
        this(context, null);
    }

    public WoToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.wToolbarStyle);
    }

    public WoToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.WoToolbar, defStyleAttr, 0);

        if (a.hasValue(R.styleable.WoToolbar_elevation)) {
            setElevation(a.getDimension(R.styleable.WoToolbar_elevation, 0));
        }

        mTitleTextAppearance = a.getResourceId(R.styleable.WoToolbar_titleTextAppearance, 0);
        mSubtitleTextAppearance = a.getResourceId(R.styleable.WoToolbar_subtitleTextAppearance, 0);

        final CharSequence title = a.getText(R.styleable.WoToolbar_title);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        final CharSequence subtitle = a.getText(R.styleable.WoToolbar_subtitle);
        if (!TextUtils.isEmpty(subtitle)) {
            setSubtitle(subtitle);
        }

        final Drawable navIcon = a.getDrawable(R.styleable.WoToolbar_navigationIcon);
        if (navIcon != null) {
            setNavigationIcon(navIcon);
        } else {
            setNavigationIcon(context.getDrawable(R.drawable.ic_back));
        }
        setNavigationOnClickListener(v -> {
            Activity activity = GlobalLib.getActivityFromView(WoToolbar.this);
            if (activity != null) {
                activity.finish();
            }
        });

        final CharSequence navDesc = a.getText(R.styleable.WoToolbar_navigationContentDescription);
        if (!TextUtils.isEmpty(navDesc)) {
            setNavigationContentDescription(navDesc);
        }

        a.recycle();
    }

    public CharSequence getTitle() {
        return mTitleText;
    }

    public void setTitle(@StringRes int resId) {
        setTitle(getContext().getText(resId));
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            if (mTitleTextView == null) {
                final Context context = getContext();
                mTitleTextView = new AppCompatTextView(context);
                LayoutParams lp = (LayoutParams) generateDefaultLayoutParams();
                mTitleTextView.setLayoutParams(lp);
                mTitleTextView.setSingleLine();
                mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                mTitleTextView.setOnClickListener(mTitleClickListener);
                if (mTitleTextAppearance != 0) {
                    mTitleTextView.setTextAppearance(context, mTitleTextAppearance);
                }
                if (mTitleTextColor != null) {
                    mTitleTextView.setTextColor(mTitleTextColor);
                }
                ensureTitleLinearLayout();
                mTitleLinearLayout.addView(mTitleTextView, 0);
            }
            mTitleTextView.setText(title);
            mTitleText = title;
        }
    }

    public CharSequence getSubtitle() {
        return mSubtitleText;
    }

    public void setSubtitle(@StringRes int resId) {
        setSubtitle(getContext().getText(resId));
    }

    public void setSubtitle(CharSequence subtitle) {
        if (!TextUtils.isEmpty(subtitle)) {
            ensureSubtitleTextView();
            mSubtitleTextView.setText(subtitle);
            mSubtitleText = subtitle;
        }
    }

    public void setTitleTextColor(@ColorInt int color) {
        setTitleTextColor(ColorStateList.valueOf(color));
    }

    public void setTitleTextColor(@NonNull ColorStateList color) {
        mTitleTextColor = color;
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
        }
    }

    public void setTitleClickListener(OnClickListener listener) {
        if (mTitleTextView != null) {
            mTitleTextView.setOnClickListener(listener);
        }
        mTitleClickListener = listener;
    }

    public void setSubtitleTextColor(@ColorInt int color) {
        setSubtitleTextColor(ColorStateList.valueOf(color));
    }

    public void setSubtitleTextColor(@NonNull ColorStateList color) {
        mSubtitleTextColor = color;
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setTextColor(color);
        }
    }

    public void ensureSubtitleTextView() {
        if (mSubtitleTextView == null) {
            final Context context = getContext();
            mSubtitleTextView = new AppCompatTextView(context);
            mSubtitleTextView.setSingleLine();
            mSubtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
            mSubtitleTextView.setOnClickListener(mSubtitleClickListener);
            LayoutParams lp = (LayoutParams) generateDefaultLayoutParams();
            lp.addRule(CENTER_IN_PARENT);
            mSubtitleTextView.setLayoutParams(lp);
            if (mSubtitleTextAppearance != 0) {
                mSubtitleTextView.setTextAppearance(context, mSubtitleTextAppearance);
            }
            if (mSubtitleTextColor != null) {
                mSubtitleTextView.setTextColor(mSubtitleTextColor);
            }
            ensureTitleLinearLayout();
            mTitleLinearLayout.addView(mSubtitleTextView);
        }
    }

    @Nullable
    public final TextView getTitleTextView() {
        return mTitleTextView;
    }

    @Nullable
    public final TextView getSubtitleTextView() {
        return mSubtitleTextView;
    }

    public void setSubtitleClickListener(OnClickListener listener) {
        if (mSubtitleTextView != null) {
            mSubtitleTextView.setOnClickListener(listener);
        }
        mSubtitleClickListener = listener;
    }

    public void setSubtitleDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top,
                                             @Nullable Drawable end, @Nullable Drawable bottom) {
        ensureSubtitleTextView();
        mSubtitleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
    }

    public void setNavigationContentDescription(@Nullable CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureNavButtonView();
        }
        if (mNavButtonView != null) {
            mNavButtonView.setContentDescription(description);
        }
    }

    @Nullable
    public Drawable getNavigationIcon() {
        return mNavButtonView != null ? mNavButtonView.getDrawable() : null;
    }

    public void setNavigationIcon(@Nullable Drawable icon) {
        if (icon != null) {
            ensureNavButtonView();
            addView(mNavButtonView);
        }
        if (mNavButtonView != null) {
            mNavButtonView.setImageDrawable(icon);
        }
    }

    public void setNavigationOnClickListener(OnClickListener listener) {
        ensureNavButtonView();
        mNavButtonView.setOnClickListener(listener);
    }

    private void ensureNavButtonView() {
        if (mNavButtonView == null) {
            mNavButtonView = new AppCompatImageButton(getContext());
            int dp24 = DensityUtils.dp2px(getContext(), 36);
            LayoutParams lp = new LayoutParams(dp24, dp24);
            lp.addRule(CENTER_VERTICAL);
            lp.addRule(ALIGN_PARENT_START);
            lp.setMarginStart(DensityUtils.dp2px(getContext(), 8));
            int dp4 = DensityUtils.dp2px(getContext(), 4);
            mNavButtonView.setPadding(dp4, dp4, dp4, dp4);
            mNavButtonView.setLayoutParams(lp);
        }
    }

    private void ensureTitleLinearLayout() {
        if (mTitleLinearLayout == null) {
            mTitleLinearLayout = new LinearLayout(getContext());
            mTitleLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mTitleLinearLayout.setGravity(Gravity.CENTER);
            LayoutParams lp = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.addRule(CENTER_IN_PARENT);
            mTitleLinearLayout.setLayoutParams(lp);
            addView(mTitleLinearLayout);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        ((LayoutParams) params).addRule(CENTER_VERTICAL);
        super.addView(child, index, params);
    }
}
