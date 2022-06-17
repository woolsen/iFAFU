package cn.ifafu.ifafu.bean.dto

import androidx.annotation.Keep
import java.util.*

@Keep
data class FeedbackReply(
        val id: Long,
        val content: String,
        val date: Date
)