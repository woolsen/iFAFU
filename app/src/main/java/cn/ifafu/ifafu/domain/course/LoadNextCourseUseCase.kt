package cn.ifafu.ifafu.domain.course

import cn.ifafu.ifafu.annotation.GetCourseStrategy
import cn.ifafu.ifafu.bean.vo.NextCourseVO
import cn.ifafu.ifafu.domain.CoroutineUseCase
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.repository.TimetableRepository
import cn.ifafu.ifafu.util.DateUtils
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class LoadNextCourseUseCase @Inject constructor(
    private val repository: TimetableRepository,
) : CoroutineUseCase<Unit, NextCourseVO>(Dispatchers.IO) {

    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE)

    override suspend fun execute(parameters: Unit): NextCourseVO {
        val holidays = repository.getHolidays()
        val currentDate = Date()
        for (holiday in holidays) {
            val from = format.parse(holiday.from) ?: Date()
            val to = Date(from.time + holiday.days * 24 * 60 * 60 * 1000)
            if (currentDate.after(from) && currentDate.before(to)) {
                val termOptions = repository.getTermOptions()
                val date = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date())
                val currentWeekday: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                val dateText = "放假中 $date ${DateUtils.getWeekdayCN(currentWeekday)}"
                return NextCourseVO.NoClass(
                    title = termOptions?.title ?: "",
                    message = "[${holiday.name}]放假了呀！！",
                    dateText = dateText
                )
            }
        }
        val termOption = repository.getTermOptions() ?: throw Failure(
            "获取学期选项失败"
        )
        val op = termOption.selected
        try {
            val courses = repository.getCourses(
                op.year, op.term, GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY
            )
            val setting = repository.getTimetableSetting()
            val currentWeek = repository.getOpeningDay(op.year, op.term).getCurrentWeek()
            return NextCourseVO.convert2(
                courses, currentWeek, setting, op.year + "学年第" + op.term + "学期课表"
            )
        } catch (e: Exception) {
            throw e
        }
    }

}