package cn.ifafu.ifafu.ui.view.timetable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.gridlayout.widget.GridLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.ifafu.ifafu.R;
import cn.ifafu.ifafu.util.ColorPool;
import cn.ifafu.ifafu.util.DensityUtils;
import cn.ifafu.ifafu.util.LightColorPool;

/**
 * 为确保老版本设备兼容性
 * 继承于{@link androidx.gridlayout.widget.GridLayout}
 * 而非{@link android.widget.GridLayout}
 */
public class TimetableView extends GridLayout {

    private final Map<TimetableItem, TextView> itemViewMap = new HashMap<>();

    private final float    dateLayoutHeightWeight = 0.7F;
    private final float    sideLayoutWidthWeight  = 0.45F;
    private final String[] dayOfWeekCN            = {"日", "一", "二", "三", "四", "五", "六"};
    private final int      dp1                    = DensityUtils.dp2px(getContext(), 1F);

    private TextView         cornerTextView;
    private LinearLayout[]   weekLayouts;
    private RelativeLayout[] noteLayouts;

    private String[] timeTexts;
    private String[] dateTexts;

    //配置类
    private Config                  config = new Config();
    private OnItemClickListener     clickListener;
    private OnItemLongClickListener longClickListener;

    public TimetableView(Context context) {
        this(context, null);
    }

    public TimetableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimetableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCornerLayout();
        initNodeLayout();
        initWeekLayout();
    }

    @NotNull
    public Config getConfig() {
        try {
            return config.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new Config();
        }
    }

    public void setConfig(Config config) {
        Config previous = this.config;
        this.config = config;
        if (previous.totalNodeCount != config.totalNodeCount) {
            removeAllViews();
            initWeekLayout();
            initNodeLayout();
            initCornerLayout();
            initTimeTextView();
            itemViewMap.clear();
        } else {
            if (previous.itemTextSize != config.itemTextSize) {
                for (TextView view : itemViewMap.values()) {
                    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.itemTextSize);
                }
            }
            if (previous.showHorizontalLine != config.showHorizontalLine ||
                    previous.showVerticalLine != config.showVerticalLine) {
                invalidate();
            }
            if (previous.otherTextColor != config.otherTextColor) {
                int textColor = config.otherTextColor;
                for (RelativeLayout layout : noteLayouts) {
                    TextView timeTextView = layout.findViewById(R.id.id_time_textview);
                    if (timeTextView != null) {
                        timeTextView.setTextColor(textColor);
                    }
                    TextView nodeTextView = layout.findViewById(R.id.id_node_textview);
                    if (nodeTextView != null) {
                        nodeTextView.setTextColor(textColor);
                    }
                }
                for (LinearLayout layout : weekLayouts) {
                    TextView weekTextView = layout.findViewById(R.id.id_week_textview);
                    if (weekTextView != null) {
                        weekTextView.setTextColor(textColor);
                    }
                    TextView dateTextView = layout.findViewById(R.id.id_date_textview);
                    if (dateTextView != null) {
                        dateTextView.setTextColor(textColor);
                    }
                }
                cornerTextView.setTextColor(textColor);
            }
            if (previous.showDate != config.showDate) {
                int visibility;
                if (config.showDate) {
                    visibility = View.VISIBLE;
                } else {
                    visibility = View.GONE;
                }
                for (LinearLayout layout : weekLayouts) {
                    TextView dateTextView = layout.findViewById(R.id.id_date_textview);
                    if (dateTextView != null) {
                        dateTextView.setVisibility(visibility);
                    }
                }
            }
            if (previous.showTime != config.showTime) {
                int visibility;
                if (config.showTime) {
                    visibility = View.VISIBLE;
                } else {
                    visibility = View.GONE;
                }
                for (RelativeLayout layout : noteLayouts) {
                    TextView timeTextView = layout.findViewById(R.id.id_time_textview);
                    if (timeTextView != null) {
                        timeTextView.setVisibility(visibility);
                    }
                }
            }
        }
    }

    public void setItemClickListener(final OnItemClickListener listener) {
        if (listener != clickListener) {
            clickListener = listener;
            for (Map.Entry<TimetableItem, TextView> e : itemViewMap.entrySet()) {
                e.getValue().setOnClickListener(v -> clickListener.onClick(e.getValue(), e.getKey()));
            }
        }
    }

    public void setItemLongClickListener(final OnItemLongClickListener listener) {
        if (listener != longClickListener) {
            longClickListener = listener;
            for (Map.Entry<TimetableItem, TextView> e : itemViewMap.entrySet()) {
                e.getValue().setOnLongClickListener(v -> {
                    longClickListener.onLongClick(e.getValue(), e.getKey());
                    return true;
                });
            }
        }
    }

    public void setMonth(int month) {
        if (month < 1 || month > 12) {
            cornerTextView.setText("");
        } else {
            cornerTextView.setText((month + "\n月"));
        }
    }

    public void setTimeText(String[] timeTexts) {
        this.timeTexts = timeTexts;
        initTimeTextView();
    }

    public void setDateTexts(String[] dateTexts) {
        if (this.dateTexts == dateTexts) {
            return;
        }
        this.dateTexts = dateTexts;
        for (int i = 0; i < weekLayouts.length; i++) {
            TextView dateTextView = weekLayouts[i].findViewById(R.id.id_date_textview);
            if (dateTextView == null) {
                dateTextView = new TextView(getContext());
                dateTextView.setId(R.id.id_date_textview);
                dateTextView.setGravity(Gravity.CENTER);
                dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                dateTextView.setTextColor(config.otherTextColor);
                LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                weekLayouts[i].addView(dateTextView, ilp);
            }
            if (dateTexts == null) {
                dateTextView.setVisibility(View.GONE);
            } else {
                dateTextView.setVisibility(View.VISIBLE);
                dateTextView.setText(dateTexts[i]);
            }
        }
    }

    public void addItems(final Collection<TimetableItem> items) {
        for (TimetableItem item : items) {
            addItem(item);
        }
    }

    public void addItem(final TimetableItem item) {
        TextView itemView = new TextView(getContext());
        itemView.setPadding(dp1, dp1, dp1, dp1);
        itemView.setGravity(Gravity.CENTER);
        itemView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.itemTextSize);
        itemView.setTextColor(Color.WHITE);
        itemView.getPaint().setFakeBoldText(true);
        String text = item.name;
        if (item.address != null && !item.address.isEmpty()) {
            text += "\n@" + item.address;
        }
        itemView.setText(text);
        itemView.setTag(item);
        itemView.setBackgroundColor(config.colorPool.getColor(text));
        if (clickListener != null) {
            itemView.setOnClickListener(v -> clickListener.onClick(v, item));
        }
        if (longClickListener != null) {
            itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(v, item);
                return true;
            });
        }
        LayoutParams lp = new LayoutParams(
                spec(item.startNode, item.nodeCount, 1F),
                spec(item.dayOfWeek, 1, 1F)
        );
        lp.height = 0;
        lp.width = 0;
        itemViewMap.put(item, itemView);
        addView(itemView, lp);
    }

    public void setItems(@Nullable final Collection<TimetableItem> items) {
        for (TextView itemView : itemViewMap.values()) {
            removeView(itemView);
        }
        itemViewMap.clear();
        if (items != null) {
            addItems(items);
        }
    }

    public void removeItem(TimetableItem item) {
        TextView itemView = itemViewMap.get(item);
        if (itemView != null) {
            itemViewMap.remove(item);
            removeView(itemView);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (config.showHorizontalLine || config.showVerticalLine) {
            //初始化虚线画笔
            Paint paint = new Paint();
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(2F);
            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(new DashPathEffect(new float[]{4F, 4F}, 0F));

            //获取跟布局的宽高
            final float availableWidth = super.getRight() - super.getLeft();
            final float availableHeight = super.getBottom() - super.getTop();

            if (config.showHorizontalLine) {
                float totalHeightWeight = dateLayoutHeightWeight + config.totalNodeCount;
                float perItemHeight = 1F / totalHeightWeight * availableHeight;
                //第一条水平分割线的y轴位置
                float y = dateLayoutHeightWeight / totalHeightWeight * availableHeight;
                for (int i = 0; i < config.totalNodeCount; i++) {
                    canvas.drawLine(0F, y, availableWidth, y, paint);
                    y += perItemHeight;
                }
            }

            if (config.showVerticalLine) {
                float totalWidthWeight = sideLayoutWidthWeight + 7F;
                float perItemWidth = 1F / totalWidthWeight * availableWidth;
                //第一条垂直分割线的y轴位置
                float x = sideLayoutWidthWeight / totalWidthWeight * availableWidth;
                for (int i = 0; i < 7; i++) {
                    canvas.drawLine(x, 0F, x, availableHeight, paint);
                    x += perItemWidth;
                }
            }
        }

        super.dispatchDraw(canvas);
    }

    private void initNodeLayout() {
        noteLayouts = new RelativeLayout[config.totalNodeCount];
        RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ); // 可复用，无需重复new
        ilp.addRule(RelativeLayout.CENTER_IN_PARENT);
        for (int row = 0; row < config.totalNodeCount; row++) {
            RelativeLayout layout = new RelativeLayout(getContext());
            //添加节数TextView
            TextView nodeTextView = new TextView(getContext());
            nodeTextView.setId(R.id.id_node_textview);
            nodeTextView.setGravity(Gravity.CENTER);
            nodeTextView.setTextColor(config.otherTextColor);
            nodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13F);
            nodeTextView.getPaint().setFakeBoldText(true);
            nodeTextView.setText(String.valueOf(row + 1));
            layout.addView(nodeTextView, ilp);
            noteLayouts[row] = layout;
            LayoutParams lp = getDefaultLayoutParams(
                    row + 1, 0, 1F, sideLayoutWidthWeight);
            addView(layout, lp);
        }
    }

    private void initTimeTextView() {
        if (timeTexts == null || noteLayouts == null) {
            return;
        }
        RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );// 可复用，无需重复new
        ilp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ilp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int loop = Math.min(timeTexts.length, noteLayouts.length);
        for (int i = 0; i < loop; i++) {
            AppCompatTextView timeTextView = noteLayouts[i].findViewById(R.id.id_time_textview);
            if (timeTextView == null) {
                timeTextView = new AppCompatTextView(getContext());
                timeTextView.setId(R.id.id_time_textview);
                timeTextView.setMaxLines(1);
                timeTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                timeTextView.setTextColor(config.otherTextColor);
                //设置兼容低版本的自适应字体大小
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                        timeTextView,
                        5,
                        9,
                        1,
                        TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
                );
                noteLayouts[i].addView(timeTextView, ilp);
            }
            timeTextView.setText(timeTexts[i]);
        }
    }

    private void initWeekLayout() {
        weekLayouts = new LinearLayout[7];
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ); // 可复用，无需重复new
        for (int column = 0; column < 7; column++) {
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.CENTER);
            TextView weekTextView = new TextView(getContext());
            weekTextView.setId(R.id.id_week_textview);
            weekTextView.setTextColor(config.otherTextColor);
            weekTextView.setGravity(Gravity.CENTER);
            weekTextView.setText(dayOfWeekCN[column]);
            weekTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13F);
            weekTextView.getPaint().setFakeBoldText(true);
            ll.addView(weekTextView, ilp);
            weekLayouts[column] = ll;
            LayoutParams lp = getDefaultLayoutParams(
                    0, column + 1, dateLayoutHeightWeight, 1F);
            addView(ll, lp);
        }
    }

    private void initCornerLayout() {
        cornerTextView = new TextView(getContext());
        cornerTextView.setGravity(Gravity.CENTER);
        cornerTextView.getPaint().setFakeBoldText(true);
        cornerTextView.setTextColor(config.otherTextColor);
        cornerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F);
        LayoutParams lp = getDefaultLayoutParams(0, 0, dateLayoutHeightWeight, sideLayoutWidthWeight);
        addView(cornerTextView, lp);
    }

    private LayoutParams getDefaultLayoutParams(int row, int column, float heightWeight, float widthWeight) {
        LayoutParams lp = new LayoutParams(
                spec(row, 1, heightWeight),
                spec(column, 1, widthWeight));
        lp.width = 0;
        lp.height = 0;
        return lp;
    }

    public interface OnItemClickListener {
        void onClick(View v, TimetableItem item);
    }

    public interface OnItemLongClickListener {
        void onLongClick(View v, TimetableItem item);
    }

    /**
     * 配置类
     */
    public static class Config implements Cloneable {
        public float     itemTextSize       = 12F;
        public int       totalNodeCount     = 12;
        public int       otherTextColor     = Color.BLACK;
        public boolean   showVerticalLine   = true;
        public boolean   showHorizontalLine = true;
        public boolean   showDate           = true;
        public boolean   showTime           = true;
        public ColorPool colorPool          = new LightColorPool();

        @NotNull
        @Override
        protected Config clone() throws CloneNotSupportedException {
            return (Config) super.clone();
        }

    }
}
