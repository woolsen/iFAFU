package cn.ifafu.ifafu.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.ifafu.ifafu.ui.view.timetable.TimetableItem
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "new_course")
data class NewCourse(
    @PrimaryKey
    var id: Int = 0,
    var name: String = "",
    var teacher: String = "",
    var classroom: String = "",
    var weeks: SortedSet<Int> = TreeSet(),
    /**
     * [Calendar.SUNDAY]    1
     * [Calendar.MONDAY]    2
     * [Calendar.TUESDAY]   3
     * [Calendar.WEDNESDAY] 4
     * [Calendar.THURSDAY]  5
     * [Calendar.FRIDAY]    6
     * [Calendar.SATURDAY]  7
     */
    var weekday: Int = 0,
    var beginNode: Int = 0, // 开始节数 = 0
    var nodeLength: Int = 0,
    var year: String = "",
    var term: String = "",
    var account: String = "",
    var local: Boolean = false
) : Cloneable, Parcelable {

    val endNode: Int
        get() = beginNode + nodeLength - 1

    val weekdayCN: Int
        get() = if (weekday == -1) -1 else (weekday + 5) % 7 + 1

    public override fun clone(): NewCourse {
        return super.clone() as NewCourse
    }

    override fun toString(): String {
        return "NewCourse{$name, $classroom, $teacher, 周${weekdayCN}第$beginNode-${endNode}节, $weeks, ${year}, ${term}}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NewCourse

        if (name != other.name) return false
        if (teacher != other.teacher) return false
        if (classroom != other.classroom) return false
        if (weeks != other.weeks) return false
        if (weekday != other.weekday) return false
        if (beginNode != other.beginNode) return false
        if (nodeLength != other.nodeLength) return false
        if (account != other.account) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + teacher.hashCode()
        result = 31 * result + classroom.hashCode()
        result = 31 * result + weeks.hashCode()
        result = 31 * result + weekday
        result = 31 * result + beginNode
        result = 31 * result + nodeLength
        result = 31 * result + account.hashCode()
        return result
    }

    fun toTimetableItem(): TimetableItem {
        val c = this
        return TimetableItem().apply {
            this.name = c.name
            this.address = c.classroom
            this.dayOfWeek = c.weekday
            this.nodeCount = c.nodeLength
            this.startNode = c.beginNode
            this.tag = c
        }
    }
}