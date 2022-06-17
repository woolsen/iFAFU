package cn.ifafu.ifafu.util

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Color
import android.os.Build

object StatusBarUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun Activity.transparentStatusBar() {
        window.statusBarColor = Color.TRANSPARENT//将状态栏设置成透明色
    }


}