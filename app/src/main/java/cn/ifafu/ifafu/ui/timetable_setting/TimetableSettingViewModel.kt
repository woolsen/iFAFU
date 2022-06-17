package cn.ifafu.ifafu.ui.timetable_setting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.repository.TimetableRepository
import cn.ifafu.ifafu.repository.UserRepository
import cn.ifafu.ifafu.service.common.ZFHttpClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableSettingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val repository: TimetableRepository,
    private val client: ZFHttpClient,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    val setting: LiveData<SyllabusSetting> = liveData(Dispatchers.IO) {
        emit(repository.getTimetableSetting())
    }

    fun save() {
        viewModelScope.launch {
            val setting = setting.value ?: return@launch
            repository.saveTimetableSetting(setting)
        }
    }

    fun outputHtml() {
        viewModelScope.launch {
            try {
                val user: User? = userRepository.getUser()
                if (user == null) {
                    toastInMain("用户信息不存在")
                    return@launch
                }
                val url: String = ZfUrlProvider.getUrl(ZFApiEnum.TIMETABLE, user)
                val referer: String = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
                val html = client.get(url, referer).body?.string() ?: ""
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("Label", html))
                    toastInMain("测试数据已复制至剪切板")
                } else {
                    toastInMain("获取剪切板失败")
                }
            } catch (e: Exception) {
                toastInMain(e.errorMessage())
            }
        }
    }

}
