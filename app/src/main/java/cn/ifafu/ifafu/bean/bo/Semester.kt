package cn.ifafu.ifafu.bean.bo

/**
 * 学年和学期选项
 * Created by woolsen on 19/9/18
 */
data class Semester(
    var yearList: MutableList<String>,
    var termList: MutableList<String>,
    var yearIndex: Int = 0,
    var termIndex: Int = 0,
) {

    val yearStr: String
        get() = yearList[yearIndex]

    val termStr: String
        get() = termList[termIndex]

    fun setYearTermIndex(yearIndex: Int, termIndex: Int) {
        this.yearIndex = yearIndex
        this.termIndex = termIndex
    }

    fun toSemesterString(): String {
        return if (termStr == "全部" && yearStr == "全部") {
            "全部"
        } else if (termStr == "全部") {
            "${yearStr}学年全部"
        } else {
            "${yearStr}学年第${termStr}学期"
        }
    }

    override fun toString(): String {
        return if (termStr == "全部" && yearStr == "全部") {
            "全部"
        } else if (termStr == "全部") {
            "${yearStr}学年全部"
        } else {
            "${yearStr}学年第${termStr}学期"
        }
    }
}