package cn.ifafu.ifafu.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * create by woolsen on 19/7/16
 */
object DateUtils {

    val weekdays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    private const val ONE_DAY_IN_MILL = 24 * 60 * 60 * 1000L
    private val calendar = Calendar.getInstance()
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    fun getWeekdayCN(weekday: Int): String {
        return weekdays[weekday - 1]
    }

    /**
     * 计算间隔的天数
     *
     * @param fromDate 起始时间
     * @param toDate   计算时间
     *
     * @return 间隔的天数
     */
    fun calcLastDays(fromDate: Date, toDate: Date): Int {
        calendar.time = fromDate
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val fromDay = calendar.timeInMillis / 1000 / 60 / 60 / 24
        calendar.time = toDate
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val toDay = calendar.timeInMillis / 1000 / 60 / 60 / 24
        return (toDay - fromDay).toInt()
    }

    /**
     * 计算间隔时间
     */
    fun calcIntervalTime(startMill: Long, endMill: Long): String {
        val sec = (endMill - startMill) / 1000
        if (sec < 0) {
            return ""
        }
        if (sec < 60) {
            return String.format("%d秒", sec)
        }
        val min = sec / 60
        if (min < 60) {
            return String.format("%d分钟", min)
        }
        val hour = min / 60
        if (hour < 24) {
            return if (min % 60 != 0L) {
                String.format("%d小时%d分钟", hour, min % 60)
            } else {
                String.format("%d小时", hour)
            }
        }
        val day = hour / 24
        return if (day < 7) {
            if (hour % 24 != 0L) {
                String.format("%d天%d小时", day, hour % 24)
            } else {
                String.format("%d天", day)
            }
        } else String.format("%d天", day)
    }

    /**
     * 获取[date]之后第几周的所有日期
     *
     * @param date           当日日期
     * @param offsetWeek     [date]之后第几周，0则表示当周
     * @param dateFormat     [SimpleDateFormat]
     *
     * @return [List]
     */
    fun getWeekOffsetDates(date: Date, offsetWeek: Int, dateFormat: String): List<String> {
        if (offsetWeek != 0) {
            date.time = date.time + offsetWeek * 7 * ONE_DAY_IN_MILL
        }
        return getCurrentWeekDate(date.time, dateFormat)
    }

    /**
     * 获取[date]之后第几周周日的月份
     *
     * @param date           当日日期
     * @param offsetWeek     [date]之后第几周，0则表示当周
     *
     * @return [List]
     */
    fun getWeekOffsetMonth(date: Date, offsetWeek: Int): String {
        if (offsetWeek != 0) {
            date.time = date.time + offsetWeek * ONE_DAY_IN_MILL
        }
        return SimpleDateFormat("M", Locale.getDefault()).format(date)
    }

    /**
     * 计算[now]是相对于[openingDay]的第几周
     * eg: now="2020-09-01"  openingDay="2020-09-01"  return=1
     *
     * @param openingDay 开学日期的时间戳，单位：毫秒
     * @param now        需要计算时间的时间戳，单位：毫秒
     *
     * @return week > 0 and week < 24，否则返回-1
     */
    fun getCurrentWeek(openingDay: Long, now: Long): Int {
        val c = Calendar.getInstance()
        c.timeInMillis = openingDay
        val offset = c[Calendar.DAY_OF_WEEK]
        val t = c.time.time - (offset - 1) * ONE_DAY_IN_MILL
        val day = (now - t) / ONE_DAY_IN_MILL
        if (day < 0) {
            return -1
        }
        val w = (day / 7 + 1).toInt()
        return if (w in 1..24) w else -1
    }

    /**
     * 计算当周的周日到周六的日期
     *
     * @param time   当天的时间戳，单位：毫秒
     * @param format 日期格式  eg: "yyyy-MM-dd"
     *
     * @return 当周的周日、周一、周二、周三、周四、周五、周六的日期
     */
    fun getCurrentWeekDate(time: Long, format: String): List<String> {
        val f = SimpleDateFormat(format, Locale.getDefault())
        val c = Calendar.getInstance()
        c.timeInMillis = time
        val offset = c.get(Calendar.DAY_OF_WEEK)
        c.add(Calendar.DAY_OF_YEAR, -offset)
        return (1..7).map {
            c.add(Calendar.DAY_OF_YEAR, 1)
            f.format(c.time)
        }
    }

    fun isToday(date: Date): Boolean {
        return format.format(date) == format.format(Date())
    }

    fun isYesterday(date: Date): Boolean {
        val time = date.time - ONE_DAY_IN_MILL
        return format.format(time) == format.format(Date())
    }

    fun isSameDay(time1: Long, time2: Long): Boolean {
        val c = Calendar.getInstance()

        c.timeInMillis = time1
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        c.timeInMillis = time2

        return (year == c[Calendar.YEAR])
                && (month == c[Calendar.MONTH])
                && (day == c[Calendar.DAY_OF_MONTH])
    }


    fun isSameHour(time1: Long, time2: Long): Boolean {
        val c = Calendar.getInstance()

        c.timeInMillis = time1
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        val hour = c[Calendar.HOUR_OF_DAY]

        c.timeInMillis = time2

        return (year == c[Calendar.YEAR])
                && (month == c[Calendar.MONTH])
                && (day == c[Calendar.DAY_OF_MONTH])
                && (hour == c[Calendar.HOUR_OF_DAY])
    }

}