package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.ifafu.ifafu.util.DateUtils
import java.util.*

@Entity
class Course {

    @PrimaryKey
    var id: Int = 0
    var name: String = "" // 课程名
    var address: String = ""// 上课地点
    var teacher: String = "" // 老师名
    var weekday = 0 // 星期几 = 0
    var beginNode = 0 // 开始节数 = 0
    var nodeCnt = 0 // 上课节数
    var weekSet: SortedSet<Int> = TreeSet() //第几周需要上课
    var color = 0 // 课程颜色
    var account: String = "" // 课程归属账号
    var local = false // 是否是自定义课程
    val endNode: Int
        get() = beginNode + nodeCnt - 1

    fun clone(): Course {
        val course = Course()
        course.id = id
        course.name = name
        course.teacher = teacher
        course.weekday = weekday
        course.beginNode = beginNode
        course.nodeCnt = nodeCnt
        course.weekSet = weekSet
        course.color = color
        course.account = account
        course.local = local
        return course
    }

    override fun toString(): String {
        return "Course{" + name +
                ", " + address +
                ", " + teacher +
                ", " + DateUtils.getWeekdayCN(weekday) +
                "第" + beginNode + "-" + (beginNode + nodeCnt - 1) + "节" +
                ", " + weekSet +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val course = other as Course
        return weekday == course.weekday && beginNode == course.beginNode && nodeCnt == course.nodeCnt && local == course.local &&
                name == course.name &&
                address == course.address &&
                teacher == course.teacher &&
                weekSet == course.weekSet &&
                account == course.account
    }

    override fun hashCode(): Int {
        return Objects.hash(name, address, teacher, weekday, beginNode, nodeCnt, weekSet, account, local)
    }
}