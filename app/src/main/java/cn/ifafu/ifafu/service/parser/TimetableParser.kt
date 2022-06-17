package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.bo.CourseBO
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.util.findByRegex
import cn.ifafu.ifafu.util.findFirstByRegex
import cn.ifafu.ifafu.util.getFirstInt
import cn.ifafu.ifafu.util.getInts
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by woolsen on 19/8/1
 *
 * Update on 20/7/29
 */
class TimetableParser : BaseOptionParser() {

    private val level1Ignore = "上午|中午|下午|晚上".toRegex()
    private val level2Ignore = "第[0-9]{1,2}节".toRegex()
    private val locationFlag = Array(20) { BooleanArray(10) }

    private val weekdays = mapOf(
        "周日" to 7,
        "周一" to 1,
        "周二" to 2,
        "周三" to 3,
        "周四" to 4,
        "周五" to 5,
        "周六" to 6
    )

    fun parse(html: String): List<CourseBO> {
        val courses = ArrayList<CourseBO>()
        val document = Jsoup.parse(html)

        // 获取调、停、换课信息
        val changesList = getChangeInfo(document)
        val changesMap = changesList
            .filter { it !is Change.Bu }
            .associateBy { it.id }

        //将补课加入课表数组
        courses.addAll(
            changesList.filterIsInstance<Change.Bu>()
                .map { it.afterInfo.createCourse(it.name) }
        )

        // 解析课表信息，并合并调、停、换课情况
        courses.addAll(
            try {
                parseCourse(document, changesMap)
            } catch (e: Exception) {
                throw Failure("课表解析失败（parseCourse）")
            }
        )

        //合并同类课程
        return courses.merge()
    }

    private fun parseCourse(
        document: Document,
        changesMap: Map<String, Change>
    ): ArrayList<CourseBO> {
        val courses = ArrayList<CourseBO>()

        /**
         * 定位到课程表Table元素
         * 并去除 星期栏 和 早晨栏
         */
        val table = document.getElementById("Table1")
            ?.getElementsByTag("tr")
            ?.drop(2) //去除 星期栏 和 早晨栏
            ?.map { it.children().toList() } ?: emptyList()

        for ((rowIndex, rowElements) in table.withIndex()) {
            var beginNode = -1 // 记录左侧标题栏第几节课
            for ((columnIndex, nodeElements) in rowElements.withIndex()) {
                val rowSpan = nodeElements.attr("rowspan").toIntOrNull() ?: 1
                val texts = nodeElements.textNodes()
                    .map { it.text() }
//                println("NodeElement Text: ${texts}")
                val weekday = markLocationFlag(columnIndex, rowIndex, rowSpan)
                when {
                    texts.isEmpty() -> {

                    }
                    texts[0].contains(level1Ignore) -> {
                        //忽略上午、下午、晚上
                    }
                    texts[0].contains(level2Ignore) -> {
                        beginNode = texts[0].getFirstInt()
                    }
                    else -> {
                        val nodeHtml = nodeElements.html().replace("&nbsp;", "")
                        // 通过<br><br>将块元素内的课程html信息拆分为单节课html信息
                        val textList = nodeHtml.split("(<br>){2,3}".toRegex())
                            .dropLastWhile { it.isEmpty() }
                        for (text in textList) {
                            val course = try {
                                if (text.startsWith("<font")) {
                                    courses.last()
                                } else {
                                    val course = textToCourse(text)
                                    /**
                                     * 如果值为-1，则使用辅助定位
                                     */
                                    if (course.nodeLength == -1) {
                                        course.nodeLength = rowSpan
                                    }
                                    if (course.beginNode == -1) {
                                        course.beginNode = beginNode
                                    }
                                    if (course.weekday == -1) {
                                        course.weekday = weekday
                                    }
                                    course
                                }
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                                courses.last()
                            }

                            /**
                             * 获取停、调、换课情况，并修改课程
                             */
                            text.findByRegex("[补停换调][0-9]{3,4}").forEach { changeId ->
//                                println("调课情况：${changeId}")
                                when (val change = changesMap[changeId]) {
                                    is Change.Ting -> {
                                        if (course.name == change.name && change.beforeInfo.isSameTime(
                                                course
                                            )
                                        ) {
                                            course.weeks.removeAll(change.beforeInfo.weeks)
                                        }
                                    }
                                    is Change.Tiao -> {
                                        if (course.name == change.name && change.beforeInfo.isSameTime(
                                                course
                                            )
                                        ) {
                                            course.weeks.removeAll(change.beforeInfo.weeks)
                                            if (change.afterInfo.isSameTime(course)) {
                                                course.weeks.addAll(change.afterInfo.weeks)
                                            } else {
                                                val newCourse =
                                                    change.afterInfo.createCourse(change.name)
                                                courses.add(newCourse)
//                                                println("调课：新增课程：${newCourse}")
                                            }
                                        }
                                    }
                                    is Change.Huan -> {
                                        if (course.name == change.name && change.beforeInfo.isSameTime(
                                                course
                                            )
                                        ) {
                                            course.weeks.removeAll(change.beforeInfo.weeks.toSet())
                                            if (change.afterInfo.isSameTime(course)) {
                                                course.weeks.addAll(change.afterInfo.weeks)
                                            } else {
                                                val newCourse =
                                                    change.afterInfo.createCourse(change.name)
                                                courses.add(newCourse)
//                                                println("换课：新增课程：${newCourse}")
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }

                            /**
                             * 去除无上课周数课程
                             */
                            if (course.weeks.isNotEmpty()) {
                                courses.add(course)
                            }
                        }
                    }
                }
            }
        }
        return courses
    }

    private fun List<CourseBO>.merge(): List<CourseBO> {
        val comparator = compareBy<CourseBO> { it.name }
            .thenBy { it.classroom }
            .thenBy { it.teacher }
            .thenBy { it.weekday }

        val courses = this.sortedWith(comparator).toMutableList()
        for (i in (0 until courses.size).reversed()) {
            for (j in (0 until i).reversed()) {
                if (comparator.compare(courses[i], courses[j]) != 0) {
                    break
                }
                val cmin: CourseBO
                val cmax: CourseBO
                if (courses[i].beginNode > courses[j].beginNode) {
                    cmin = courses[j]
                    cmax = courses[i]
                } else {
                    cmin = courses[i]
                    cmax = courses[j]
                }
                if (cmin.weeks.isSame(cmax.weeks)) {
                    if (cmin.beginNode + cmin.nodeLength == cmax.nodeLength) {
                        cmin.nodeLength += cmax.nodeLength
                        courses.remove(cmax)
                    }
                } else if (cmin.beginNode == cmax.beginNode && cmin.nodeLength == cmax.nodeLength) {
                    cmin.weeks.addAll(cmax.weeks)
                    courses.remove(cmax)
                }
            }
        }
        return courses
    }

    private fun Set<Int>.isSame(other: Set<Int>): Boolean {
        other.forEach {
            if (!this.contains(it)) {
                return false
            }
        }
        return true
    }

    /**
     * 将文本解析为课程
     *
     * eg: 中级财务管理1<br>周四第1,2节{第14-14周}<br>崔静<br>诚智D303<br>2020年01月08日(08:30-10:30)<br>诚智D304
     *
     */
    private fun textToCourse(text: String): CourseBO {
        val info = text.split("<br>".toRegex()).dropLastWhile { it.isEmpty() }
        val name = info[0].trim()
        val teacher = info[2].trim()

        /**
         * 正常情况第三位是教室，但也有可能没有上课教室
         */
        val classroom = if (info.size > 3) {
            info[3].trim()
        } else {
            ""
        }

        /**
         * 解析时间
         * - eg: 周四第1,2节{第14-14周}
         * - eg: 周三第3,4节{第2-2周|双周}
         * - eg: {第1-11周|2节/周}
         */
        val timeText = info[1]
        var beginNode: Int = -1
        var nodeLength: Int = -1
        if (timeText.contains("(第[0-9]+(-[0-9]+)?节)|(第[0-9]+(,[0-9]+)+)节".toRegex())) {
            val nodes = timeText.findFirstByRegex("第.*节").getInts()
            if (nodes.isNotEmpty()) {
                beginNode = nodes[0]
                nodeLength = if (nodes.size > 1) {
                    nodes.last() - nodes[0] + 1
                } else {
                    1
                }
            }
        } else if (timeText.contains("[0-9]+节[/\\\\]周".toRegex())) {
            nodeLength = timeText.findFirstByRegex("[0-9]+节[/\\\\]周").getFirstInt()
        }
//        println("beginNode: ${beginNode}, nodeLength: ${nodeLength}")

        //解析星期
        var weekday = -1
        weekdays.forEach { (t, u) ->
            if (text.contains(t)) {
                weekday = u
                return@forEach
            }
        }
        //解析周次
        val weekText = timeText.findFirstByRegex("第[0-9]+-[0-9]+周")
        val intList = weekText.getInts()
        var weeks = (intList[0]..intList[1]).toList()
        if (text.contains("单周")) {
            weeks = weeks.filter { it % 2 != 0 }
        } else if (text.contains("双周")) {
            weeks = weeks.filter { it % 2 == 0 }
        }

        return CourseBO(
            name = name,
            teacher = teacher,
            classroom = classroom,
            weekday = weekday,
            weeks = weeks.toSortedSet(),
            nodeLength = nodeLength,
            beginNode = beginNode
        )
    }

    /**
     * 用于辅助定位
     *
     * @return 当前星期，周一(1)，周二(2)....周天(7)
     */
    private fun markLocationFlag(columnIndex: Int, rowIndex: Int, rowSpan: Int): Int {
        var columnOffset = 0
        while (locationFlag[rowIndex][columnIndex + columnOffset]) {
            columnOffset++
        }
        (0 until rowSpan).forEach { row ->
            locationFlag[rowIndex + row][columnIndex + columnOffset] = true
        }
        return columnIndex + columnOffset - 1
    }

    /**
     * 解析调课信息
     */
    private fun getChangeInfo(document: Document): List<Change> {
        val table = document.getElementById("DBGrid")
            ?.children()
            ?.get(0)
            ?.children() ?: return emptyList()
        return table.drop(1) //去除标题
            .map { ele -> ele.children().map { it.text() } }
            .mapNotNull { info ->
                try {
                    when {
                        info[0].startsWith("调") ->
                            Change.Tiao(info[0], info[1], info[2], info[3])
                        info[0].startsWith("停") ->
                            Change.Ting(info[0], info[1], info[2])
                        info[0].startsWith("补") ->
                            Change.Bu(info[0], info[1], info[3])
                        info[0].startsWith("换") ->
                            Change.Huan(info[0], info[1], info[2], info[3])
                        else -> null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
    }

    sealed class Change(val id: String, val name: String) {

        /**
         * 调课
         */
        class Tiao(id: String, name: String, before: String, after: String) : Change(id, name) {
            val beforeInfo = parse(before)
            val afterInfo = parse(after)
        }

        /**
         * 换课
         */
        class Huan(id: String, name: String, before: String, after: String) : Change(id, name) {
            val beforeInfo = parse(before)
            val afterInfo = parse(after)
        }

        /**
         * 补课
         */
        class Bu(id: String, name: String, after: String) : Change(id, name) {
            val afterInfo = parse(after)
        }

        /**
         * 停课
         */
        class Ting(id: String, name: String, before: String) : Change(id, name) {
            val beforeInfo = parse(before)
        }

        /**
         * example: 周1第1节连续2节{第10周}/诚智D302/王明惠
         */
        fun parse(text: String): Info {
            val ts = text.split("/")
            val weekday = ts[0].findFirstByRegex("周[1-7]").getFirstInt()
            if (weekday == -1) {
                throw IllegalStateException("星期解析出错：${text}")
            }
            val beginNode = ts[0].findFirstByRegex("第[0-9]{1,2}节").getFirstInt()
            if (beginNode == -1) {
                throw IllegalStateException("开始上课节数解析出错：${text}")
            }
            val nodeLength = ts[0].findFirstByRegex("连续[0-9]{1,2}节").getFirstInt()
            if (nodeLength == -1) {
                throw IllegalStateException("上课节数解析出错：${text}")
            }
            var weeks = ts[0].findFirstByRegex("第[0-9]+(-[0-9]+)?周").getInts().run {
                if (this.size == 1) {
                    listOf(this[0])
                } else {
                    (this[0] until this[1]).toList()
                }
            }
            if (ts[0].contains("单周")) {
                weeks = weeks.dropLastWhile { it % 2 == 0 }
            } else if (ts[0].contains("双周")) {
                weeks = weeks.dropLastWhile { it % 2 != 0 }
            }
            val classroom = ts.getOrElse(1) { "" }
            val teacher = ts.getOrElse(2) { "" }
            return Info(
                weeks = weeks.sorted(), weekday = weekday,
                beginNode = beginNode, nodeLength = nodeLength,
                classroom = classroom, teacher = teacher
            )
        }

        class Info(
            val weeks: List<Int>,
            val weekday: Int,
            val beginNode: Int,
            val nodeLength: Int,
            val classroom: String,
            val teacher: String
        ) {
            fun isSameTime(course: CourseBO): Boolean {
                return course.weekday == weekday &&
                        course.beginNode == beginNode &&
                        course.nodeLength == nodeLength
            }

            fun createCourse(name: String): CourseBO {
                return CourseBO(
                    name = name,
                    weeks = weeks.toSortedSet(),
                    weekday = weekday,
                    beginNode = beginNode,
                    nodeLength = nodeLength,
                    classroom = classroom,
                    teacher = teacher
                )
            }
        }
    }

}
