package cn.ifafu.ifafu.ui.main.new_theme

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.Exam
import cn.ifafu.ifafu.bean.vo.NextCourseVO
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.bean.vo.Weather
import cn.ifafu.ifafu.domain.course.LoadNextCourseUseCase
import cn.ifafu.ifafu.repository.ExamRepository
import cn.ifafu.ifafu.repository.IfUserRepository
import cn.ifafu.ifafu.repository.OtherRepository
import cn.ifafu.ifafu.repository.TimetableRepository
import cn.ifafu.ifafu.ui.view.timeline.TimeEvent
import cn.ifafu.ifafu.util.DateUtils
import cn.ifafu.ifafu.util.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class MainNewViewModel @Inject constructor(
    private val nextCourseUseCase: LoadNextCourseUseCase,
    private val ifafuUserRepository: IfUserRepository,
    private val timetableRepository: TimetableRepository,
    private val examRepository: ExamRepository,
    private val otherRepository: OtherRepository
) : BaseViewModel() {

    val weather = MutableLiveData<Weather>()

    val timeEvents = MediatorLiveData<List<TimeEvent>>()

    private val _nextCourse = MediatorLiveData<Resource<NextCourseVO>>()
    val nextCourse = _nextCourse.toLiveData()

    val isIFAFUUser = MutableLiveData<Boolean>()

    fun updateTimeAxis() {
        viewModelScope.launch {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val list = TreeSet<TimeEvent> { o1, o2 -> o1.text.compareTo(o2.text) }
            val now = Date()
            val holidays = timetableRepository.getHolidays()
            for (holiday in holidays) {
                val date = format.parse(holiday.from) ?: continue
                val day = DateUtils.calcLastDays(now, date)
                if (day >= 0) {
                    val top = if (day == 0) {
                        "${holiday.name} 今天"
                    } else {
                        "${holiday.name} ${day}天"
                    }
                    val event = TimeEvent(date.time, top)
                    list.add(event)
                }
            }
            val exams = examRepository.getNowExamsFromLocal()
            list.addAll(exams.toTimeEvents())

            timeEvents.postValue(list.toList())

            timeEvents.addSource(examRepository.getNowExamsLiveDataFromNet()) {
                if (it is Resource.Success) {
                    list.addAll(it.data.toTimeEvents())
                    timeEvents.postValue(list.toList())
                }
            }
        }
    }

    fun updateWeather() {
        viewModelScope.launch {
            otherRepository.getWeather("101230101")
                .flowOn(Dispatchers.IO)
                .catch {
                    Timber.e(it)
//                    toast(it.errorMessage())
                }
                .collectLatest {
                    weather.postValue(it)
                }
        }
    }

    fun updateIFAFUUser() {
        viewModelScope.launch {
            ifafuUserRepository.userInfo()
                .flowOn(Dispatchers.IO)
                .catch { Timber.e(it) }
                .collect {
                    isIFAFUUser.postValue(it != null)
                }
        }
    }

    fun updateNextCourse() {
        viewModelScope.launch {
            val vo = nextCourseUseCase.invoke(Unit)
            _nextCourse.postValue(vo)
        }
    }

    private fun List<Exam>.toTimeEvents(): List<TimeEvent> {
        val events = ArrayList<TimeEvent>()
        val now = Date()
        forEach { exam ->
            if (exam.startTime != 0L) { //暂无时间信息
                val date = Date(exam.startTime)
                val day = DateUtils.calcLastDays(now, date)
                if (day >= 0) {
                    val bottom = if (day == 0) {
                        "${exam.name} 今天"
                    } else {
                        "${exam.name} ${day}天"
                    }
                    val axis = TimeEvent(exam.startTime, bottom)
                    events.add(axis)
                }
            }
        }
        return events
    }
}