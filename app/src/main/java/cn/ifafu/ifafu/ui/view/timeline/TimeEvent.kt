package cn.ifafu.ifafu.ui.view.timeline

data class TimeEvent(val timeStamp: Long, val text: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimeEvent

        if (timeStamp != other.timeStamp) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeStamp.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}