package cn.ifafu.ifafu.ui.view.adapter.syllabus_setting

import android.widget.ImageView
import androidx.annotation.ColorInt

class ColorItem (
        val title: String,
        val subtitle: String?,
        @ColorInt val color: Int,
        val click: (iv: ImageView) -> Unit
): SettingItem()