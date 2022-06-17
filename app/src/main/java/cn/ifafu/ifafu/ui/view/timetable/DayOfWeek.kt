package cn.ifafu.ifafu.ui.view.timetable

import androidx.annotation.IntDef
import java.util.*

@MustBeDocumented
@IntDef(
    value = [
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    ], flag = true
)
annotation class DayOfWeek