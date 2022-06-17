package cn.ifafu.ifafu.ui.examlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.bo.Semester
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.entity.Exam
import cn.ifafu.ifafu.repository.ExamRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamListViewModel @Inject constructor(
    private val examRepository: ExamRepository
) : BaseViewModel() {

    private lateinit var _semester: Semester
    val semester = MutableLiveData<Semester>()

    private val _exams = MediatorLiveData<Resource<List<Exam>>>()
    val exams: LiveData<Resource<List<Exam>>> = _exams

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _semester = examRepository.getSemester()
            _semester.yearList.remove("全部")
            _semester.termList.remove("全部")
            semester.postValue(_semester)
            val year = _semester.yearStr
            val term = _semester.termStr
            val localData = examRepository.getAllExamsFromLocal(year, term)
            _exams.postValue(Resource.Success(localData))
            refresh()
        }
    }

    fun refresh() = viewModelScope.launch {
        val year = _semester.yearStr
        val term = _semester.termStr
        val netData = examRepository.getExamsLiveDataFromNet(year, term)
        _exams.addSource(netData) {
            _exams.postValue(it)
        }
    }

    fun switchYearAndTerm(yearIndex: Int, termIndex: Int) = viewModelScope.launch {
        _semester.yearIndex = yearIndex
        _semester.termIndex = termIndex
        semester.postValue(_semester)
        refresh()
    }

}
