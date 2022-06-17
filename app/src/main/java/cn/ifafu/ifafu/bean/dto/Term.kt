package cn.ifafu.ifafu.bean.dto

class TermOptions(
    var selected: Term,
    val options: List<Term>
) {

    val title: String
        get() {
            val yearTerm = selected
            return "${yearTerm.year}学年第${yearTerm.term}学期"
        }

    override fun toString(): String {
        return "TermOptions(selected=$selected, options=$options)"
    }

}

data class Term(
    val year: String,
    val term: String,
) {

    val title: String
        get() = "${year}学年第${term}学期"
}

data class TermWrap<T>(
    val term: Term,
    val data: T
)