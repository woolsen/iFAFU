package cn.ifafu.ifafu

import android.app.Application
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.entity.User
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.didichuxing.doraemonkit.DoKit
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.tencent.bugly.crashreport.CrashReport
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltAndroidApp
class IFAFU : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    companion object {

        private val usingAccount: String
            get() = SPUtils.getInstance(Constants.SP_USER_INFO).getString("account")

        private var isInitConfig = false

        /**
         * 启动界面时调用，防止长时间白屏
         * 必须在主线程初始化！！！(已设置Dispatchers.Main)
         *
         * @param application Application
         */
        suspend fun initConfig(
            application: Application,
            user: User?,
        ) = withContext(Dispatchers.Main) {
            if (isInitConfig) {
                return@withContext
            }
            isInitConfig = true

            DoKit.Builder(application).build()

            /* 初始化Bugly */
            Bugly.setUserId(application, user?.account ?: "")
            if (user != null && user.account.isNotEmpty()) {
                if (user.account.length == 9) {
                    Bugly.setAppChannel(application, "FAFU_JS")
                } else {
                    Bugly.setAppChannel(application, "FAFU")
                }
            }
            val strategy = CrashReport.UserStrategy(application)
            strategy.setCrashHandleCallback(CrashCallback())
            strategy.appVersion = AppUtils.getAppVersionName() + "-" + AppUtils.getAppVersionCode()
            Bugly.init(application, "46836c4eaa", BuildConfig.DEBUG, strategy)
        }
    }

    private class CrashCallback : CrashReport.CrashHandleCallback() {
        override fun onCrashHandleStart(
            crashType: Int,
            errorType: String?,
            errorMessage: String?,
            errorStack: String?,
        ): MutableMap<String, String> {
            return (super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack)
                ?: HashMap()).apply {
                put("account", usingAccount)
            }
        }
    }


}
