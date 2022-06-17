package cn.ifafu.ifafu.bean.dto

import androidx.annotation.Keep
import java.util.*

@Keep
data class Feedback(
    val sno: String,
    val content: String,
    val date: Date,
    val reply: FeedbackReply?
)