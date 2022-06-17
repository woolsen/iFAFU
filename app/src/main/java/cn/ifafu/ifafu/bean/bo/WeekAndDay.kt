package cn.ifafu.ifafu.bean.bo

import java.util.*

data class WeekAndDay(
    /**
     * 第几周，base on 1
     */
    val week: Int,
    /**
     * 周几，格式见[Calendar]
     */
    val weekday: Int
)