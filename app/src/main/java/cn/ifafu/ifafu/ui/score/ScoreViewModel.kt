package cn.ifafu.ifafu.ui.score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.bo.Semester
import cn.ifafu.ifafu.bean.vo.Event
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.entity.ScoreFilter
import cn.ifafu.ifafu.repository.ScoreRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ScoreViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository
) : BaseViewModel() {

    /**
     * 刷新智育分、IES、成绩数量等仅需更新_scores
     * 智育分、IES、成绩数量等通过监听[_scoresResource]实现转换
     */
    private val _scoresResource = MediatorLiveData<Resource<List<Score>>>()

    val scoresResource: LiveData<Resource<List<Score>>> = _scoresResource
    val gpa: LiveData<String> = _scoresResource.successMap { scores ->
        scores.filter { it.gpa > 0 }
            .sumByFloat { it.gpa }
            .toString(2)
    }
    val ies: LiveData<Float> = _scoresResource.successMap { scores ->
        scores.calcIES()
    }

    private lateinit var _semester: Semester
    val semester = MutableLiveData<Semester>()
    val iesDetail = MutableLiveData<Event<String>>()

    init {
        viewModelScope.launch {
            //获取数据库学期信息
            _semester = scoreRepository.getSemester()
            semester.postValue(_semester)
            val year = _semester.yearStr
            val term = _semester.termStr
            updateScoreList(year, term)
        }
    }

    /**
     * 先加载数据库缓存，若成绩信息为空，从教务管理系统上获取最新的成绩信息
     */
    fun switchYearAndTerm(yearIndex: Int, termIndex: Int) {
        _semester.setYearTermIndex(yearIndex, termIndex)
        semester.value = _semester
        val year = _semester.yearStr
        val term = _semester.termStr
        updateScoreList(year, term)
    }

    fun refreshScoreList() {
        viewModelScope.launch {
            updateScoreListFromNet()
        }
    }

    private fun updateScoreList(year: String, term: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val localData = scoreRepository.getScoresFromLocal(year, term)
            _scoresResource.postValue(Resource.Success(localData))
            updateScoreListFromNet()
        }
    }

    private suspend fun updateScoreListFromNet() {
        withContext(Dispatchers.Main) {
            val year = _semester.yearStr
            val term = _semester.termStr
            //在前台从教务管理系统获取成绩信息并更新View
            scoreRepository.getScoresLiveDataFromNet(year, term).apply {
                _scoresResource.addSource(this) {
                    _scoresResource.postValue(it)
                }
            }
        }
    }

    fun itemChecked(score: Score, isCheck: Boolean) = (viewModelScope + Dispatchers.IO).launch {
        score.isIESItem = isCheck
        val scores = (_scoresResource.value as? Resource.Success)?.data ?: return@launch
        _scoresResource.postValue(Resource.Success(scores))
        val filter = ScoreFilter(score.id, score.account, score.isIESItem)
        scoreRepository.saveFilter(filter)
    }

    fun allChecked() = (viewModelScope + Dispatchers.IO).launch {
        val scores = (_scoresResource.value as? Resource.Success)?.data ?: return@launch
        scores.forEach {
            it.isIESItem = true
        }
        _scoresResource.postValue(Resource.Success(scores))
        val filters = scores.map { ScoreFilter(it.id, it.account, it.isIESItem) }
        scoreRepository.saveFilter(filters)
    }

    fun iesCalculationDetail() {
        viewModelScope.launch {
            try {
                val all = (scoresResource.value as? Resource.Success<List<Score>>)?.data
                    ?: return@launch
                val passingList = ArrayList<Score>() //及格
                val failList = ArrayList<Score>() //不及格
                val filterList = ArrayList<Score>() //过滤
                var totalScore = 0F
                var credit = 0F
                all.forEach {
                    if (!it.isIESItem) {
                        filterList.add(it)
                    } else if (it.score >= 60) {
                        passingList.add(it)
                    } else {
                        failList.add(it)
                    }
                }
                iesDetail.postValue(Event("总共${all.size}门成绩，排除课程：[" +
                        StringBuilder().apply {
                            var first = true
                            filterList.forEach {
                                if (first) {
                                    first = false
                                } else {
                                    append("、")
                                }
                                append(it.name).append(
                                    when {
                                        it.nature.contains("任意选修") -> "(任意选修课)"
                                        it.name.contains("体育") -> "(体育课)"
                                        else -> "(自定义)"
                                    }
                                )
                            }
                        } + "]，还有${passingList.size + failList.size}门纳入计算范围，" +
                        "其中${failList.size}门课程不及格。加权总分为" +
                        StringBuilder().apply {
                            var first = true
                            (passingList + failList).forEach {
                                if (first) {
                                    first = false
                                } else {
                                    append(" + ")
                                }
                                append("${it.realScore.trimEnd(2)} × ${it.credit.trimEnd(2)}")
                                totalScore += (it.realScore * it.credit)
                            }
                            append(" = ${totalScore.trimEnd(2)}")
                        } + "，总学分为" +
                        StringBuilder().apply {
                            (passingList + failList).forEachIndexed { index, score ->
                                if (index == 0) {
                                    append(score.credit.trimEnd(2))
                                } else {
                                    append(" + ${score.credit.trimEnd(2)}")
                                }
                                credit += score.credit
                            }
                            append(" = ${credit.trimEnd(2)}")
                        } +
                        StringBuilder().apply {
                            var ies = totalScore / credit
                            var totalMinus = 0F
                            if (ies.isNaN()) {
                                ies = 0F
                            }
                            append(
                                "，则${totalScore.trimEnd(2)} / ${credit.trimEnd(2)} = ${
                                    ies.trimEnd(
                                        2
                                    )
                                }分"
                            )
                            failList.forEach {
                                totalMinus += it.credit
                            }
                            append(
                                "，减去不及格的学分共${totalMinus.trimEnd(2)}分，最终为${
                                    (ies - totalMinus).trimEnd(
                                        2
                                    )
                                }分。"
                            )
                        }
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun List<Score>.calcIES(): Float {
        // 过滤 不计入智育分的成绩 和 免修成绩
        val scores = this
            .filter { it.isIESItem && it.realScore != Score.FREE_COURSE }
        if (scores.isEmpty()) {
            return 0f
        }
        var totalScore = 0f
        var totalCredit = 0f
        var totalMinus = 0f
        for (score in scores) {
            val realScore = score.realScore
            totalScore += (realScore * score.credit)
            totalCredit += score.credit
            if (realScore < 60) {
                totalMinus += score.credit
            }
        }
        var result = totalScore / totalCredit - totalMinus
        if (result.isNaN()) {
            result = 0F
        }
//    Timber.d("IES, result = [${result}]")
//    Timber.d("IES, firsh = [${JSONObject.toJSONString(scores[0])}]")
//    Timber.d("IES, list = [${JSONObject.toJSONString(scores.map { it.name })}]")

        return result
    }
}