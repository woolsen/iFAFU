package cn.ifafu.ifafu.ui.view.adapter.syllabus_setting

data class TextViewItem(
        val title: String,
        val subtitle: String?,
        val click: () -> Unit,
        val longClick: () -> Unit
): SettingItem()