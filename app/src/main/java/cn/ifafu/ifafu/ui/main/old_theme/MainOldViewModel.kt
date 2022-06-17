package cn.ifafu.ifafu.ui.main.old_theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.bean.vo.Weather
import cn.ifafu.ifafu.repository.SemesterRepository
import cn.ifafu.ifafu.util.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainOldViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository
) : BaseViewModel() {

    private val _weather = MutableLiveData<Weather>()
    val weather = _weather.toLiveData()

    val semester: LiveData<String> = liveData {
        val semester = semesterRepository.getSemester().toString()
        emit(semester)
    }

    val online = MutableLiveData(true)

}