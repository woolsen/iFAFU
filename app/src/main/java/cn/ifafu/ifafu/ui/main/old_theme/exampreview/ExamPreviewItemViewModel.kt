package cn.ifafu.ifafu.ui.main.old_theme.exampreview

import cn.ifafu.ifafu.entity.Exam
import java.text.SimpleDateFormat
import java.util.*

internal class ExamPreviewItemViewModel(exam: Exam) {

    val examName: String
    val examTime: String
    val address: String
    val seatNumber: String
    val timeLeft: String
    val timeLeftUnit: String

    init {
        val now = System.currentTimeMillis()
        val timeText: String
        if (exam.startTime == 0L) {
            timeText = "暂无考试时间"
            timeLeft = ""
            timeLeftUnit = ""
        } else {
            timeText = dateFormat.format(Date(exam.startTime)) +
                    "(${timeFormat.format(Date(exam.startTime))}" +
                    "-" +
                    "${timeFormat.format(Date(exam.endTime))})"
            val second = (exam.startTime - now) / 1000
            "${second / (24 * 60 * 60)}"
            when {
                second >= (24 * 60 * 60) -> {
                    timeLeft = "${second / (24 * 60 * 60)}"
                    timeLeftUnit = "天"
                }
                second >= 60 * 60 -> {
                    timeLeft ="${second / (60 * 60)}"
                    timeLeftUnit = "小时"
                }
                else -> {
                    timeLeft = "${second / 60}"
                    timeLeftUnit = "分钟"
                }
            }
        }
        examName = exam.name
        examTime = timeText
        address = exam.address
        seatNumber = exam.seatNumber
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)
    }

}