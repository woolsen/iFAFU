package cn.ifafu.ifafu.ui.main.old_theme.coursepreview

import androidx.lifecycle.*
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.bean.vo.NextCourseVO
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.bean.vo.Weather
import cn.ifafu.ifafu.domain.course.LoadNextCourseUseCase
import cn.ifafu.ifafu.repository.OtherRepository
import cn.ifafu.ifafu.repository.SemesterRepository
import cn.ifafu.ifafu.util.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursePreviewViewModel @Inject constructor(
    private val nextCourseUseCase: LoadNextCourseUseCase,
    private val semesterRepository: SemesterRepository,
    private val otherRepository: OtherRepository
) : BaseViewModel() {

    val semester: LiveData<String> = liveData {
        val semester = semesterRepository.getSemester().toSemesterString() + "课表"
        emit(semester)
    }

    private val _nextCourse = MediatorLiveData<Resource<NextCourseVO>>()
    val nextCourseVO = _nextCourse.toLiveData()

    val weather = MutableLiveData<Weather>()

    fun refresh() {
        updateClassPreview()
        updateWeather()
    }

    private fun updateClassPreview() {
        viewModelScope.launch {
            val vo = nextCourseUseCase.invoke(Unit)
            _nextCourse.postValue(vo)
        }
    }

    private fun updateWeather() {
        viewModelScope.launch {
            otherRepository.getWeather("101230101")
                .catch { toast(it.errorMessage()) }
                .collectLatest {
                    weather.postValue(it)
                }
        }
    }

}