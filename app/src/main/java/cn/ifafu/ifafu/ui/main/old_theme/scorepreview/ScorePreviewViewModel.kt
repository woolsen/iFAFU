package cn.ifafu.ifafu.ui.main.old_theme.scorepreview

import androidx.lifecycle.*
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.bean.vo.ScoreVO
import cn.ifafu.ifafu.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScorePreviewViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository
) : BaseViewModel() {

    val semester: LiveData<String> = liveData {
        val semester = scoreRepository.getSemester().toSemesterString() + "学习成绩"
        emit(semester)
    }

    private val _scores = MediatorLiveData<List<Score>>()
    val scoreVO: LiveData<ScoreVO> = _scores.map { ScoreVO.convert(it) }

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        kotlin.runCatching {
            val localData = scoreRepository.getNowScoresFromLocal()
            _scores.postValue(localData)
            val netData = scoreRepository.getNowScoresLiveDataFromNet()
            _scores.addSource(netData) {
                if (it is Resource.Success && it.data.isNotEmpty()) {
                    _scores.postValue(it.data!!)
                }
            }
        }
    }
}