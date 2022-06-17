package cn.ifafu.ifafu.bean.vo

import cn.ifafu.ifafu.ui.view.timetable.TimetableItem
import kotlinx.coroutines.Deferred

data class TimetablePreviewSource(
    val year: String,
    val term: String,
    private val deferred: Deferred<List<TimetableItem>>
) {
    var selected: Boolean = false

    suspend fun get(): List<TimetableItem> {
        return deferred.await()
    }

}