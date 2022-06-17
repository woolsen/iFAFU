package cn.ifafu.ifafu.bean.vo

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

class MenuVO(
        @IdRes
        val id: Int = 0,
        @DrawableRes
        val icon: Int, //图标
        val title: String
)
