package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.Course
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.util.DateUtils
import cn.ifafu.ifafu.util.getInts
import org.jsoup.Jsoup
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by woolsen on 19/8/1
 */
class ProfessionalTimetableParser(user: User) {

    private val account: String = user.account

    fun parse(html: String): IFResponse<List<Course>> {
        val courses = ArrayList<Course>()
        //模拟html绘制表格，标记课程位置[1-12][1-7]，用于辅助解析课程时间
        val locFlag = Array(16) { BooleanArray(8) }
        //用于辅助解析课程时间
        val time = Time()
        val doc = Jsoup.parse(html)
        //解析调课信息
        val changeMap = HashMap<String, Pair<Course, Course?>>()
        val table = doc.getElementById("DBGrid")
            ?: return IFResponse.failure("未知错误")
        val changeTds = table.getElementsByTag("tr")
        for (i in 2 until changeTds.size) {
            val tds = changeTds[i].getElementsByTag("td")
            val tds0Text = tds[0].text()
            try {
                if (tds0Text.matches("补[0-9]{3,4}".toRegex())) {//补课
                    //直接添加到list中
                    val course = Course().apply {
                        name = tds[1].text()
                        parseText(tds[3].text())
                    }
                    courses.add(course)
                } else { //调课、换课、停课
                    //获取原上课信息
                    val beforeChangeCourse = Course().apply {
                        name = tds[1].text()
                        parseText(tds[2].text())
                    }
                    //获取现上课信息，若停课则为null
                    val afterChangeCourse =
                        if (tds0Text.matches("停[0-9]{3,4}".toRegex())) {
                            null
                        } else {
                            Course().apply {
                                name = tds[1].text()
                                parseText(tds[3].text())
                            }
                        }
                    changeMap[tds0Text] = Pair(beforeChangeCourse, afterChangeCourse)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        //定位到第几节课一行过的所有元素
        val nodeTrs = doc.getElementById("Table1")
            ?.getElementsByTag("tr")
            ?: emptyList()
        for (i in 2 until nodeTrs.size) {
            val tds = nodeTrs[i].getElementsByTag("td")
            //开始节数的备用方案，通过解析侧边节数获取
            var index = 0
            while (index < 2) {
                val s = tds[index].text()
                val nodeRegex = "第[0-9]{1,2}节"
                if (s.matches(nodeRegex.toRegex())) {
                    time.beginNode = s.getInts()[0]
                    index++
                    break
                }
                index++
            }
            for (j in 0 until tds.size - index) {  //定位到第几节课第几格的元素
                val td = tds[index + j]
                //列的备用方案
                var hNode = 0
                for (col in 1..7) {
                    if (locFlag[time.beginNode][col % 7]) {
                        hNode++
                    } else {
                        break
                    }
                }
                time.weekday = ((hNode + 1) % 7) + 1
                //通过“rowspan“获取课程节数(备用方案)
                if (td.hasAttr("rowspan")) {
                    time.nodeLength = Integer.parseInt(td.attr("rowspan"))
                } else {
                    time.nodeLength = 1
                }
                //标记课程位置，用于定位(备用方案)
                for (ii in 0 until time.nodeLength) {
                    locFlag[ii + time.beginNode][hNode + 1] = true
                }

//                testHelpPrintf()
//                println("weekday = ${DateUtils.getWeekdayCN(time.weekday)}, node = ${hNode + 1}")
//                println(td.text())
                //为空则继续解析下一块元素
                if (td.text().isEmpty()) {
                    continue
                }
                //解析块元素中的课程信息
                val list = ArrayList<Course>()
                for (s1 in td.html().split("(<br>){2,3}".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()) {
                    try {
                        val info =
                            s1.split("<br>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val course = Course()
                        course.name = info[0].trim()
                        course.into(Time.parse1(info[1], time))
                        course.teacher = info[2].trim()
                        if (info.size > 3) {
                            course.address = info[3].trim()
                        }
                        course.account = account
                        if (s1.contains("\\([调停换][0-9]{3,4}\\)".toRegex())) {
                            val m = Pattern.compile("[调停换][0-9]{3,4}").matcher(s1)
                            while (m.find()) {
                                changeMap[m.group()]?.let { change ->
                                    val before = change.first
                                    if (course.weekday == before.weekday &&
                                        course.beginNode == before.beginNode &&
                                        course.nodeCnt == before.nodeCnt &&
                                        course.weekSet.containsAll(before.weekSet)
                                    ) {
                                        course.weekSet.removeAll(before.weekSet)
                                        change.second?.let { list.add(it) }
                                    }
                                }
                            }
                        }
                        if (course.weekSet.isNotEmpty()) {
                            list.add(course)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                courses.addAll(list)
            }
        }
        //合并课程
        val map = HashMap<String, Array<BooleanArray>>()
        courses.forEach { course ->
            val key = "${course.name}❤${course.teacher}❤${course.address}❤${course.weekday}"
            val nodes = map.getOrPut(key, { Array(25) { BooleanArray(20) } })
            course.weekSet.forEach { week ->
                for (node in course.beginNode until (course.beginNode + course.nodeCnt)) {
                    nodes[week][node] = true
                }
            }
        }
        val afterMergeCourses = ArrayList<Course>()
        for ((k, nodes) in map) {
            val info = k.split("❤")
            for (week in 0 until 24) {
                for (node in 0 until 13) {
                    if (nodes[week][node]) {
                        var nodeLength = 1
                        while (nodes[week][node + nodeLength]) {
                            nodeLength++
                        }
                        val course = Course()
                        course.name = info[0]
                        course.teacher = info[1]
                        course.address = info[2]
                        course.weekday = info[3].toInt()
                        course.beginNode = node
                        course.nodeCnt = nodeLength
                        var weekDump = 0
                        lengthWhile@ while (week + weekDump <= 24) {
                            var flag = true
                            for (j1 in 0 until nodeLength) {
                                if (!nodes[week + weekDump][node + j1]) {
                                    flag = false
                                    break
                                }
                            }
                            if (flag) {
                                course.weekSet.add(week + weekDump)
                                for (j1 in 0 until nodeLength) {
                                    nodes[week + weekDump][node + j1] = false
                                }
                            }
                            weekDump++
                        }
                        afterMergeCourses.add(course)
                    }
                }
            }
        }
        afterMergeCourses.sortBy { it.name + it.weekday }
        val finalCourseList = afterMergeCourses.onEach {
            it.account = account
            it.id = it.hashCode()
        }
        return IFResponse.success(finalCourseList)
    }

    private fun Course.into(time: Time) {
        this.weekday = time.weekday
        this.weekSet = time.weekSet
        this.nodeCnt = time.nodeLength
        this.beginNode = time.beginNode
    }

    private fun Course.parseText(text: String) {
        text.split("/").run {
            into(Time.parse2(this[0]))
            if (this.size == 2) {
                teacher = this[1]
            } else {
                address = this[1]
                teacher = this[2]
            }
        }
    }

    internal class Time {

        var weekday: Int = 1
        var beginNode: Int = 0
        var nodeLength: Int = 0
        var weekSet: TreeSet<Int> = TreeSet()

        companion object {

            fun parse1(text: String, help: Time): Time {
                val time = Time()
                //解析上课节数
                val m1 = Pattern.compile("第.*节").matcher(text)
                if (m1.find() && !m1.group().contains("-")) {
                    val intList = m1.group().getInts()
                    time.beginNode = intList[0]
                    time.nodeLength = intList.size
                } else {
                    time.beginNode = help.beginNode
                    val m2 = Pattern.compile("[0-9]+节\\\\周").matcher(text)
                    time.nodeLength = if (m2.find()) {
                        m2.group().getInts()[0]
                    } else {
                        help.nodeLength
                    }
                }
                //解析星期
                var flag = false
                for (i in 0..6) {
                    if (text.contains(DateUtils.weekdays[i])) {
                        time.weekday = i + 1
                        flag = true
                        break
                    }
                }
                if (!flag) {
                    time.weekday = help.weekday
                }
                //解析周次
                val m2 = Pattern.compile("第[0-9]+-[0-9]+周").matcher(text)
                if (m2.find()) {
                    val intList = m2.group().getInts()
                    var beginWeek = intList[0]
                    val endWeek = intList[1]
                    //weekType
                    when {
                        text.contains("单周") -> {
                            beginWeek = if (beginWeek % 2 == 1) beginWeek else beginWeek + 1
                            var i = beginWeek
                            while (i <= endWeek) {
                                time.weekSet.add(i)
                                i += 2
                            }
                        }
                        text.contains("双周") -> {
                            beginWeek = if (beginWeek % 2 == 0) beginWeek else beginWeek + 1
                            var i = beginWeek
                            while (i <= endWeek) {
                                time.weekSet.add(i)
                                i += 2
                            }
                        }
                        else -> for (i in beginWeek..endWeek) {
                            time.weekSet.add(i)
                        }
                    }
                }
                return time
            }

            @Throws(ParseException::class)
            fun parse2(text: String): Time {
                val time = Time()
                Pattern.compile("周[1-7]").matcher(text).runCatching {
                    find()
                    time.weekday = this.group().getInts()[0]
                }.onFailure {
                    throw ParseException()
                }
                Pattern.compile("第[0-9]{1,2}节").matcher(text).runCatching {
                    find()
                    time.beginNode = this.group().getInts()[0]
                }.onFailure {
                    throw ParseException()
                }
                Pattern.compile("连续[0-9]{1,2}节").matcher(text).runCatching {
                    find()
                    time.nodeLength = this.group().getInts()[0]
                }.onFailure {
                    throw ParseException()
                }
                Pattern.compile("第[0-9]+(-[0-9]+)?周(单周|双周)?").matcher(text).runCatching {
                    find()
                    val t = this.group()
                    val l = t.getInts()
                    val start: Int = l[0]
                    val end = if (l.size == 1) l[0] else l[1]
                    val type = when {
                        t.contains("单周") -> 1
                        t.contains("双周") -> 0
                        else -> -1
                    }
                    for (i in start..end) {
                        if (i % 2 == type || type == -1) {
                            time.weekSet.add(i)
                        }
                    }
                }.onFailure {
                    throw ParseException()
                }
                return time
            }
        }
    }

    class ParseException : Exception()
}
