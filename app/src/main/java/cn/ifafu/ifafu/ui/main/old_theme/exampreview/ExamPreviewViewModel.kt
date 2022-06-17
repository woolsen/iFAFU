package cn.ifafu.ifafu.ui.main.old_theme.exampreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.Exam
import cn.ifafu.ifafu.repository.ExamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamPreviewViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : BaseViewModel() {

    private val _semester = MutableLiveData<String>()
    val semester: LiveData<String>
        get() = _semester

    private val _exams = MutableLiveData<List<Exam>>()
    val exams: LiveData<List<Exam>>
        get() = _exams

    init {
        refresh()
        viewModelScope.launch(Dispatchers.IO) {
            val str = examRepository.getSemester().toSemesterString() + "学生考试"
            _semester.postValue(str)
        }
    }

    fun refresh() = viewModelScope.launch(Dispatchers.Main) {
        val now = System.currentTimeMillis()
        val exams = examRepository.getNowExamsFromLocal()
            .filter { it.startTime > now || it.startTime == 0L }
            .take(2)
        _exams.value = exams
    }

}