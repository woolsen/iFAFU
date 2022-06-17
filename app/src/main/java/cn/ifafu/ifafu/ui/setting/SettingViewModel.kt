package cn.ifafu.ifafu.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.GlobalSetting
import cn.ifafu.ifafu.repository.GlobalSettingRepository
import cn.ifafu.ifafu.ui.view.adapter.syllabus_setting.CheckBoxItem
import cn.ifafu.ifafu.ui.view.adapter.syllabus_setting.SettingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val globalSettingRepository: GlobalSettingRepository
) : BaseViewModel() {

    private var originalTheme = -1
    val settings by lazy { MutableLiveData<List<SettingItem>>() }
    val needCheckTheme by lazy { MutableLiveData<Boolean>() }

    private lateinit var setting: GlobalSetting

    fun initSetting() {
        viewModelScope.launch {
            try {
                setting = globalSettingRepository.get()
                originalTheme = setting.theme
                settings.postValue(
                    listOf(
                        CheckBoxItem(
                            "旧版主页主题",
                            "应需求而来，喜欢0.9版本iFAFU界面就快来呀",
                            setting.theme == GlobalSetting.THEME_OLD
                        ) {
                            setting.theme =
                                if (it) GlobalSetting.THEME_OLD else GlobalSetting.THEME_NEW
                            needCheckTheme.postValue(originalTheme != setting.theme)
                        })
                )
            } catch (e: Exception) {
                // TODO
                e.printStackTrace()
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            globalSettingRepository.save(setting)
        }
    }
}