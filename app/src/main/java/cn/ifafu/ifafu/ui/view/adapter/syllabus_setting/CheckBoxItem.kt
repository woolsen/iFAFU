package cn.ifafu.ifafu.ui.view.adapter.syllabus_setting

data class CheckBoxItem(
        val title: String,
        val tip: String,
        var checked: Boolean,
        val listener: (checked: Boolean) -> Unit
) : SettingItem()