package cn.ifafu.ifafu.bean.vo

import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

sealed class NextCourseVO(
    val title: String,
    val dateText: String
) {

    class HasClass(
        title: String,
        val nextClass: String,
        val address: String,
        val numberOfClasses: Pair<Int, Int>,
        val isInClass: Boolean,
        val classTime: String,
        val timeLeft: String,
        dateText: String
    ) : NextCourseVO(
        title = title,
        dateText = dateText
    )

    class NoClass(
        title: String,
        val message: String,
        dateText: String
    ) : NextCourseVO(
        title = title,
        dateText = dateText
    )

    companion object {

        /**
         * 将课程转化为课程预览
         *
         * @param courses 当前学期所有课程
         * @param currentWeek 当前周
         * @param setting 课表设置
         *
         * @return [NextCourseVO]
         */
        fun convert2(
            courses: List<NewCourse>,
            currentWeek: Int,
            setting: SyllabusSetting,
            title: String
        ): NextCourseVO {
            val currentWeekday: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val date = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date())

            //当前周不在读书范围期间，则提示放假了
            if (currentWeek <= 0 || currentWeek > setting.weekCnt) {
                val dateText = "放假中 $date ${DateUtils.getWeekdayCN(currentWeekday)}"
                return NoClass(
                    title = title,
                    message = "放假了呀！！",
                    dateText = dateText
                )
            }

            val dateText = "第${currentWeek}周 $date ${DateUtils.getWeekdayCN(currentWeekday)}"
            //当数据库中课程信息为空，则提示无信息
            if (courses.isEmpty()) {
                return NoClass(
                    title = title,
                    message = "暂无课程信息",
                    dateText = dateText
                )
            }

            //获取当天课程
            val todayCourses = courses
                .filter { course ->
                    course.weeks.contains(currentWeek) && course.weekday == currentWeekday
                }.sortedBy {
                    it.beginNode
                }

            if (todayCourses.isEmpty()) {
                return NoClass(
                    title = title,
                    message = "今天没课哦~",
                    dateText = dateText
                )
            }

            //计算下一节是第几节课
            val intTime: List<Int> = setting.beginTime
            //将课程按节数排列
            val courseMap: MutableMap<Int, NewCourse> = HashMap()
            for (course in todayCourses) {
                for (i in course.beginNode..course.endNode) {
                    courseMap[i] = course
                }
            }
            val totalNode = courseMap.size //上课总节数
            val c: Calendar = Calendar.getInstance()
            val now = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE)
            var currentNode = 0 //当前上课节数
            var courseInfo: NewCourse? = null
            var classTime = 0
            var afterClassTime = 0
            for ((i, course) in courseMap) {
                currentNode++
                classTime = intTime[i]
                afterClassTime = if (classTime % 100 + setting.nodeLength >= 60) {
                    classTime + 100 - classTime % 100 + (classTime % 100 + setting.nodeLength % 100) % 60
                } else {
                    classTime + setting.nodeLength
                }
                if (now < afterClassTime) {
                    courseInfo = course
                    break
                }
            }
            if (courseInfo != null) {
                val classTimeText = "%d:%02d-%d:%02d".format(
                    classTime / 100,
                    classTime % 100,
                    afterClassTime / 100,
                    afterClassTime % 100
                )
                val isInClass: Boolean
                val timeLeft: String
                if (now >= classTime) {
                    timeLeft = calcIntervalTimeForNextClass(now, afterClassTime) + "后下课"
                    isInClass = true
                } else {
                    timeLeft = calcIntervalTimeForNextClass(now, classTime) + "后上课"
                    isInClass = false
                }
                return HasClass(
                    title = title,
                    nextClass = courseInfo.name,
                    address = courseInfo.classroom,
                    numberOfClasses = Pair(currentNode, totalNode),
                    isInClass = isInClass,
                    classTime = classTimeText,
                    timeLeft = timeLeft,
                    dateText = dateText
                )
            }
            return NoClass(
                title = title,
                message = "今天${totalNode}节课都上完了",
                dateText = dateText
            )
        }

        private fun calcIntervalTimeForNextClass(start: Int, end: Int): String {
            val last = (end / 100 - start / 100) * 60 + (end % 100 - start % 100)
            var result = ""
            if (last >= 60) {
                result += "${last / 60}小时"
            }
            if (last % 60 != 0) {
                result += "${last % 60}分钟"
            }
            return result
        }
    }
}
