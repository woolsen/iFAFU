package cn.ifafu.ifafu.bean.vo

import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.ui.view.timetable.TimetableItem

class TimetableVO private constructor() {

    val data: MutableList<MutableList<TimetableItem>> = ArrayList()

    fun getWeek(week: Int): List<TimetableItem> {
        return data.getOrElse(week - 1) { emptyList() }
    }

    fun set(week: Int, item: TimetableItem) {
        if (data.size < week) {
            data.addAll(ArrayList(week - data.size))
        }
        data[week - 1].add(item)
    }

    fun add(course: NewCourse) {
        course.weeks.forEach { week ->
            if (data.size < week) {
                val newList = MutableList<ArrayList<TimetableItem>>(week - data.size) { ArrayList() }
                data.addAll(newList)
            }
            data[week - 1].add(course.toTimetableItem())
        }
    }

    companion object {
        fun create(data: List<NewCourse>): TimetableVO {
            return TimetableVO().apply {
                data.forEach { course ->
                    this.add(course)
                }
            }
        }
    }
}