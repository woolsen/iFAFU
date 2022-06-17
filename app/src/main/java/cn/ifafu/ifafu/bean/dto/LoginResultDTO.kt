package cn.ifafu.ifafu.bean.dto

import cn.ifafu.ifafu.entity.UserInfo

data class LoginResultDTO(
    val token: String,
    val info: UserInfo
)