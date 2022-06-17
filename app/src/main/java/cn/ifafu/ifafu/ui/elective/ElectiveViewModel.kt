package cn.ifafu.ifafu.ui.elective

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.bo.Elective
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.repository.ElectivesRepository
import cn.ifafu.ifafu.repository.ScoreRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.util.sumByFloat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElectiveViewModel @Inject constructor(
    private val electivesRepository: ElectivesRepository,
    private val scoreRepository: ScoreRepository
) : BaseViewModel() {

    val total = MutableLiveData<Elective?>()
    val zrkx = MutableLiveData<Elective>()
    val rwsk = MutableLiveData<Elective>()
    val ysty = MutableLiveData<Elective>()
    val wxsy = MutableLiveData<Elective>()
    val cxcy = MutableLiveData<Elective>()

    val loading = MutableLiveData<String?>()

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    init {
        try {
            viewModelScope.launch {
                loading.postValue("加载中")
                var scores = scoreRepository.getAllScoresFromLocal()
                if (scores.isEmpty()) {
                    when (val resource = scoreRepository.getAllScoresFromNet()) {
                        is Resource.Success -> {
                            scores = resource.data
                        }
                        is Resource.Failure -> {
                            _toast.postValue(resource.message)
                            loading.postValue(null)
                            return@launch
                        }
                    }
                }
                //过滤特殊情况：入学英语分级
                scores = scores.filterNot { it.remarks.contains("英语分级") }
                //获取选修学分要求
                val electives = try {
                    electivesRepository.get()
                } catch (e: Exception) {
                    loading.postValue(null)
                    _toast.postValue(e.errorMessage("获取选修学分需求失败"))
                    return@launch
                }
                var totalScores = scores
                    .filter { it.nature == "任意选修课" || it.nature == "公共选修课" }
                    .sortedBy { it.name }
                for (i in 1 until totalScores.size) {
                    if (totalScores[i].name == totalScores[i - 1].name) {
                        if (totalScores[i].score > totalScores[i - 1].score) {
                            if (totalScores[i - 1].score >= 60) {
                                totalScores[i - 1].credit = 0F
                            }
                        } else {
                            if (totalScores[i].score >= 60) {
                                totalScores[i].credit = 0F
                            }
                        }
                    }
                }
                totalScores = totalScores.sortedByDescending { it.score }
                val zrkxScores = totalScores.filter { it.attr == "自然科学类" }
                val rwskScores = totalScores.filter { it.attr == "人文社科类" }
                val ystyScores = totalScores.filter { it.attr == "艺术体育类" || it.attr == "艺术、体育类" }
                val wxsyScores = totalScores.filter { it.attr == "文学素养类" }
                val cxcyScores = totalScores.filter { it.attr == "创新创业教育类" }
                val zrkxCredit = zrkxScores.filter { it.realScore >= 60 }
                    .sumByFloat { it.credit }
                val rwskCredit = rwskScores.filter { it.realScore >= 60 }
                    .sumByFloat { it.credit }
                val ystyCredit = ystyScores.filter { it.realScore >= 60 }
                    .sumByFloat { it.credit }
                val wxsyCredit = wxsyScores.filter { it.realScore >= 60 }
                    .sumByFloat { it.credit }
                val cxcyCredit = cxcyScores.filter { it.realScore >= 60 }
                    .sumByFloat { it.credit }
                val totalCredit = zrkxCredit + rwskCredit + ystyCredit + wxsyCredit + cxcyCredit
                val zrkxDone = zrkxCredit >= electives.zrkx
                val rwskDone = rwskCredit >= electives.rwsk
                val ystyDone = ystyCredit >= electives.ysty
                val wxsyDone = wxsyCredit >= electives.wxsy
                val cxcyDone = cxcyCredit >= electives.cxcy
                val totalDone =
                    zrkxDone && rwskDone && ystyDone && wxsyDone && cxcyDone && totalCredit >= electives.total
                total.postValue(
                    Elective(
                        "全部选修课，已修${totalScores.size}门",
                        "需修满${electives.total}分，已修${totalCredit}分${totalDone.isDoneStr()}",
                        totalScores, totalDone
                    )
                )
                zrkx.postValue(
                    Elective(
                        "自然科学类，已修${zrkxScores.size}门",
                        "需修满${electives.zrkx}分，已修${zrkxCredit}分${zrkxDone.isDoneStr()}",
                        zrkxScores, zrkxDone
                    )
                )
                rwsk.postValue(
                    Elective(
                        "人文社科类，已修${rwskScores.size}门",
                        "需修满${electives.rwsk}分，已修${rwskCredit}分${rwskDone.isDoneStr()}",
                        rwskScores, rwskDone
                    )
                )
                ysty.postValue(
                    Elective(
                        "艺术体育类，已修${ystyScores.size}门",
                        "需修满${electives.ysty}分，已修${ystyCredit}分${ystyDone.isDoneStr()}",
                        ystyScores, ystyDone
                    )
                )
                wxsy.postValue(
                    Elective(
                        "文学素养类，已修${wxsyScores.size}门",
                        "需修满${electives.wxsy}分，已修${wxsyCredit}分${wxsyDone.isDoneStr()}",
                        wxsyScores, wxsyDone
                    )
                )
                cxcy.postValue(
                    Elective(
                        "创新创业教育类，已修${cxcyScores.size}门",
                        "需修满${electives.cxcy}分，已修${cxcyCredit}分${cxcyDone.isDoneStr()}",
                        cxcyScores, cxcyDone
                    )
                )
                loading.postValue(null)
            }

        } catch (e: Exception) {
            _toast.postValue(e.errorMessage())
            loading.postValue(null)
        }
    }

    private fun Boolean.isDoneStr(): String {
        return if (this) "（已修满）" else ""
    }

}