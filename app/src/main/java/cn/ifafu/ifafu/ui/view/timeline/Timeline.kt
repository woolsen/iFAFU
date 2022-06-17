package cn.ifafu.ifafu.ui.view.timeline

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.util.DensityUtils
import java.text.SimpleDateFormat
import java.util.*

class Timeline @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @ColorInt
    private var color: Int = Color.BLACK
    private var count: Int = 4
    private var textSize: Float = 12F
    private var format = "yyyy-MM-dd"

    private val linePaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0F)
    }
    private val pointPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        strokeWidth = 10f
    }
    private val textPaint = TextPaint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
        isSubpixelText = true
    }

    private var timeEvents: List<TimeEvent> = emptyList()

    init {
        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.Timeline, defStyleAttr, 0
        )
        //获取时间轴颜色
        color = ta.getColor(R.styleable.Timeline_timeline_color, color)
        count = ta.getInt(R.styleable.Timeline_timeline_count, count)
        textSize = ta.getDimensionPixelOffset(
            R.styleable.Timeline_timeline_textSize,
            DensityUtils.dp2px(context, textSize)
        ).toFloat()
        //初始化画笔
        linePaint.color = color
        pointPaint.color = color
        textPaint.color = color
        textPaint.textSize = textSize
        ta.recycle()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        //通过坐标轴获取View的宽高
        val availableWidth = (super.getRight() - super.getLeft()).toFloat()
        val availableHeight = (super.getBottom() - super.getTop()).toFloat()
        //计算每个点的间距
        val intervalOfPoint = availableWidth / count
        //将画布原点移向下移{availableHeight/2}至左中点
        canvas.translate(0F, availableHeight / 2)
        //绘制中间虚线
        canvas.drawLine(0F, 0F, availableWidth, 0F, linePaint)
        //绘制点
        canvas.translate(intervalOfPoint / 2, 0F)
        //计算偏移量，以保证文本上下间距相同
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val centerOffsetY = textSize / 4
        for (i in 0 until timeEvents.size.coerceAtMost(count)) {
            val event = timeEvents[i]
            //绘制点
            canvas.drawCircle(0F, 0F, 10F, pointPaint)
            //通过StaticLayout实现自动换行
            val topTextLayout = StaticLayout(
                    format.format(event.timeStamp),
                textPaint,
                intervalOfPoint.toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                true
            )
            //计算偏移量，以保证文本上下间距相同
            val topOffsetY = topTextLayout.topPadding - topTextLayout.height
            canvas.save()
            canvas.translate(0F, topOffsetY.toFloat() - centerOffsetY)
            topTextLayout.draw(canvas)
            canvas.restore()
            val bottomTextLayout = StaticLayout(
                event.text,
                textPaint,
                intervalOfPoint.toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                true
            )
            canvas.save()
            canvas.translate(0F, centerOffsetY)
            bottomTextLayout.draw(canvas)
            canvas.restore()
            //画布往前移动
            canvas.translate(intervalOfPoint, 0F)
        }

    }

    fun setFormat(format: String) {
        this.format = format
    }

    fun setTimeEvents(events: List<TimeEvent>) {
        timeEvents = events.sortedBy { it.timeStamp }
        invalidate()
    }

}