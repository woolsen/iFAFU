@file:JvmName("Converter")

package cn.ifafu.ifafu.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.annotation.SchoolCode
import cn.ifafu.ifafu.entity.User

fun schoolToIconWhite(context: Context, @SchoolCode school: String?): Drawable? {
    val resId = when (school) {
        User.FAFU -> R.drawable.fafu_bb_icon_white
        User.FAFU_JS -> R.drawable.fafu_js_icon_white
        else -> R.drawable.icon_ifafu_round
    }
    return ContextCompat.getDrawable(context, resId)
}

fun schoolToString(@SchoolCode school: String?): String? {
    return when (school) {
        User.FAFU -> "福建农林大学"
        User.FAFU_JS -> "福建农林大学金山学院"
        else -> null
    }
}
