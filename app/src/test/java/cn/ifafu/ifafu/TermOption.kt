package cn.ifafu.ifafu


class TermOption(val title: String = "", list: List<Op>) : ArrayList<TermOption.Op>(list) {

    var selectedIndex: Int = -1
    val selectedOption: Op
        get() = get(selectedIndex)

    data class Op(val text: String, val value: String)
}