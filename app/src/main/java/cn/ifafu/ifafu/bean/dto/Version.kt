package cn.ifafu.ifafu.bean.dto

import androidx.annotation.Keep

@Keep
data class Version(
    val versionName: String,
    val versionCode: Int,
    val url: String?
)