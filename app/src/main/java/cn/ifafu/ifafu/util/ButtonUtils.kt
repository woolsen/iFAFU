package cn.ifafu.ifafu.util

import android.view.View

/**
 * Created by woolsen on 18-11-4.
 */
object ButtonUtils {
    private const val DEFAULT_DIFF: Long = 1000
    private var lastClickTime: Long = 0
    private var lastButtonId = -1

    /**
     * 判断两次点击的间隔，如果小于1200，则认为是多次无效点击
     *
     * @return 点击有效性
     */
    fun isFastDoubleClick(): Boolean {
        return isFastDoubleClick(-1, DEFAULT_DIFF)
    }

    fun isFastDoubleClick(diff: Long): Boolean {
        return isFastDoubleClick(-1, diff)
    }

    /**
     * 判断两次点击的间隔，如果小于1000，则认为是多次无效点击
     *
     * @return 点击有效性
     */
    fun isFastDoubleClick(buttonId: Int): Boolean {
        return isFastDoubleClick(buttonId, DEFAULT_DIFF)
    }

    /**
     * 判断两次点击的间隔，如果小于diff，则认为是多次无效点击
     *
     * @param diff 间隔时间
     * @return 点击有效性
     */
    fun isFastDoubleClick(buttonId: Int, diff: Long): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - lastClickTime
        if (lastButtonId == buttonId && lastClickTime > 0 && timeD < diff) {
            lastClickTime = 0
            return true
        }
        lastClickTime = time
        lastButtonId = buttonId
        return false
    }
}

/**
 * 防连续双击
 *
 * @param intervalInMill 最小间隔时间，单位：毫秒
 * @param listener
 */
fun View.setDeBoundClickListener(intervalInMill: Long = 1000L, listener: View.OnClickListener) {
    val deBound = DeBound(intervalInMill)
    setOnClickListener { view ->
        if (deBound.clickable()) {
            listener.onClick(view)
        }
    }
}


private class DeBound(private val intervalInMill: Long) {

    private var lastTimeInMill = -1L

    fun clickable(): Boolean {
        val current = System.currentTimeMillis()
        if (current - lastTimeInMill > intervalInMill) {
            lastTimeInMill = current
            return true
        }
        return false
    }
}