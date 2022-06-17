package cn.ifafu.ifafu.bean.vo

import cn.ifafu.ifafu.entity.Score

class ScoreVO private constructor(
        val hasInfo: Boolean,
        val message: String = "",
        val text: String = ""
) {
    companion object {
        fun convert(scores: List<Score>?): ScoreVO {
            return when {
                scores == null ->
                    ScoreVO(hasInfo = false, message = "获取成绩失败")
                scores.isEmpty() ->
                    ScoreVO(hasInfo = false, message = "暂无成绩信息")
                else ->
                    ScoreVO(hasInfo = true, text = "已出${scores.size}门成绩")
            }
        }
    }
}