package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.entity.GlobalSetting
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.db.JiaowuDatabase
import com.blankj.utilcode.util.SPUtils
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityRetainedScoped
class GlobalSettingRepository @Inject constructor(
    db: JiaowuDatabase
) {

    private val globalSettingDao = db.globalSettingDao

    private fun getAccount() = SPUtils.getInstance(Constants.SP_USER_INFO).getString("account")

    suspend fun get(): GlobalSetting = withContext(Dispatchers.IO) {
        globalSettingDao.globalSetting(getAccount()) ?: GlobalSetting(getAccount()).apply {
            save(this)
        }
    }

    suspend fun get(account: String): GlobalSetting = withContext(Dispatchers.IO) {
        globalSettingDao.globalSetting(account) ?: GlobalSetting(getAccount()).apply {
            save(this)
        }
    }

    suspend fun save(setting: GlobalSetting) = withContext(Dispatchers.IO) {
        globalSettingDao.save(setting)
    }

}