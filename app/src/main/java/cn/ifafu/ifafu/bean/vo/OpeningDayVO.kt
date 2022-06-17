package cn.ifafu.ifafu.bean.vo

import cn.ifafu.ifafu.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

data class OpeningDayVO(
    var year: String,
    var term: String,
    /**
     * example: 2020-09-01
     */
    var openingDay: String,
    var isCurrentTerm: Boolean = true
) {

    fun getOpeningDay(): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(openingDay)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前周
     *
     * @return -1：放假中
     */
    fun getCurrentWeek(): Int {
        if (!isCurrentTerm) return 1
        return try {
            val now = System.currentTimeMillis()
            val openingDay = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                .parse(openingDay)!!.time
            DateUtils.getCurrentWeek(openingDay, now)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }
}