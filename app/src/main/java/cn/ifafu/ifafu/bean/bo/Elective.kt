package cn.ifafu.ifafu.bean.bo

import cn.ifafu.ifafu.entity.Score

class Elective (
        var category: String,
        var statistics: String,
        var scores: List<Score>,
        var done: Boolean
)