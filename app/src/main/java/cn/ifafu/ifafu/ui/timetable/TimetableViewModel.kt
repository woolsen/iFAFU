package cn.ifafu.ifafu.ui.timetable

import android.net.Uri
import androidx.lifecycle.*
import cn.ifafu.ifafu.annotation.GetCourseStrategy
import cn.ifafu.ifafu.bean.dto.Term
import cn.ifafu.ifafu.bean.dto.TermOptions
import cn.ifafu.ifafu.bean.vo.*
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.repository.TimetableRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.ui.view.timetable.TimetableItem
import cn.ifafu.ifafu.util.addLivedata
import com.blankj.utilcode.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: TimetableRepository
) : BaseViewModel() {

    /**
     * 正在显示的学年学期
     */
    private val showingYearTerm = MutableLiveData<Term>()

    /**
     * 开学日期，用于计算`当前周`和`课表的日期栏`
     * 监听[showingYearTerm]来切换开学日期
     */
    val openingDay = showingYearTerm.switchMap { ops ->
        liveData(Dispatchers.IO) {
            if (ops == null) return@liveData
            emit(repository.getOpeningDay(ops.year, ops.term))
        }
    }

    val timetablePreviews = MutableLiveData<Resource<List<TimetablePreviewSource>>>()

    /**
     * 当前课表
     * 与[showingYearTerm]关联，随着其改变而改变
     */
    val timetableVO = MediatorLiveData<Resource<TimetableVO>>()
        .addLivedata(showingYearTerm) { ops ->
            if (ops == null) return@addLivedata
            updateTimetable(ops.year, ops.term, GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY)
        }


    /**
     * 课表设置
     */
    val timetableSetting = MutableLiveData<SyllabusSetting>()

    val background = MutableLiveData<Uri?>()

    val message = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            updateBackground()
            updateTimetableSetting()
            repository.getTermOptionsResource()
                .flowOn(Dispatchers.IO)
                .catch { e -> Timber.e(e, "初始化课表失败") }
                .collect { options ->
                    Timber.d("Options: $options")
                    showingYearTerm.postValue(options.selected)
                    updateTimetable(
                        options.selected.year,
                        options.selected.term,
                        GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY,
                        showLoading = false,
                    )
                    updateTimetablePreviews(options)
                }
        }
    }

    /**
     * 从网络获取课表
     */
    fun updateSyllabusFromNet() {
        val ops = showingYearTerm.value ?: return
        updateTimetable(ops.year, ops.term, GetCourseStrategy.NETWORK, true)
    }

    private fun updateTimetable(
        year: String,
        term: String,
        @GetCourseStrategy strategy: Int,
        showLoading: Boolean = false
    ) {
        viewModelScope.launch {
            if (showLoading) {
                timetableVO.postValue(Resource.Loading())
            }
            repository.getCoursesFlow(year, term, strategy)
                .flowOn(Dispatchers.IO)
                .map { TimetableVO.create(it) }
                .catch { message.postValue(it.errorMessage("查询课表失败")) }
                .collectLatest {
                    if (showLoading) {
                        message.postValue("课表刷新成功")
                    }
                    timetableVO.postValue(Resource.Success(it))
                }
        }
    }

    /**
     * 切换学期
     */
    fun switchOption(year: String, term: String) {
        showingYearTerm.value = Term(year, term)
    }

    /**
     * 更新课表。用于修改课程后刷新课表
     */
    fun updateTimetableLocal() {
        showingYearTerm.value = showingYearTerm.value
    }

    /**
     * 更新课表设置
     */
    fun updateTimetableSetting() {
        viewModelScope.launch {
            val setting = repository.getTimetableSetting()
            timetableSetting.value = setting
        }
    }

    /**
     * 当[Uri]相同时，调用[android.widget.ImageView.setImageURI]不重绘
     * 背景，则需要先emit null，将ImageView中的Uri设置为null，再设置Uri
     */
    fun updateBackground() {
        viewModelScope.launch {
            val image = File(Utils.getApp().getExternalFilesDir("background"), "syllabus.jpg")
            background.value = null
            if (image.exists()) {
                background.value = Uri.fromFile(image)
            }
        }
    }

    /**
     * 重置背景
     */
    fun resetBackground() {
        viewModelScope.launch {
            val image = File(Utils.getApp().getExternalFilesDir("background"), "syllabus.jpg")
            if (image.exists()) {
                image.delete()
            }
            background.value = null
        }
    }

    private fun updateTimetablePreviews(termOptions: TermOptions) {
        viewModelScope.launch {
            val selected = termOptions.selected
            val vos = termOptions.options.map { op ->
                val year = op.year
                val term = op.term
                getTimePreviewSource(year, term).apply {
                    this.selected = (year == selected.year && term == selected.term)
                }
            }
            timetablePreviews.postValue(Resource.Success(vos))
        }
    }

    private fun getTimePreviewSource(year: String, term: String): TimetablePreviewSource {
        return TimetablePreviewSource(year, term, viewModelScope.async(Dispatchers.IO) {
            repository.getCoursesFlow(year, term, GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY)
                .map { it.findFirstWeekHasCourse() }
                .catch { }
                .firstOrNull() ?: emptyList()
        })
    }

    private fun List<NewCourse>.findFirstWeekHasCourse(): List<TimetableItem> {
        var firstHaveCourseWeek = Int.MAX_VALUE
        for (course in this) {
            val first = course.weeks.firstOrNull() ?: continue
            if (firstHaveCourseWeek > first) {
                firstHaveCourseWeek = first
            }
        }
        return this.filter { it.weeks.contains(firstHaveCourseWeek) }
            .map { it.toTimetableItem() }
    }


}