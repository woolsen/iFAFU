package cn.ifafu.ifafu.util

import android.content.Context
import android.util.TypedValue

/**
 * create by woolsen on 19/7/24
 */
object DensityUtils {
    /**
     * dp转换为px
     * @param context 上下文
     * @param dpValue dp
     * @return px
     */
    @JvmStatic
    fun dp2px(context: Context, dpValue: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue, context.resources.displayMetrics
        ) + 0.5).toInt()
    }

    @JvmStatic
    fun sp2px(context: Context, spVal: Float): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal, context.resources.displayMetrics
        ) + 0.5).toInt()
    }

    /**
     * px转换为dp
     * @param context 上下文
     * @param pxValue px
     * @return dp
     */
    @JvmStatic
    fun px2dp(context: Context, pxValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return pxValue / scale
    }

    @JvmStatic
    fun px2sp(context: Context, pxVal: Float): Float {
        val scale = context.resources.displayMetrics.density
        return pxVal / scale
    }
}