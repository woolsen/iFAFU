package cn.ifafu.ifafu.ui.common

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import cn.ifafu.ifafu.exception.IFResponseFailureException
import cn.ifafu.ifafu.exception.JiaowuPasswordErrorException
import cn.ifafu.ifafu.ui.login.LoginActivity
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseViewModel : ViewModel() {

    protected fun Throwable.errorMessage(default: String = ""): String {
        val message = this.message
        return if (this is UnknownHostException || this is ConnectException) {
            "网络异常，请检查网络设置"
        } else if (this is SocketTimeoutException) {
            "网络连接超时"
        } else if (this is SQLiteConstraintException) {
            "数据库数据错误，${message}"
        } else if (this is IOException && message?.contains("unexpected") == true) {
            "正方教务系统又崩溃了！"
        } else if (this is JiaowuPasswordErrorException) {
            val app = Utils.getApp()
            app.startActivity(Intent(app, LoginActivity::class.java))
            "教务系统密码错误，请重新登录"
        } else if (this is IFResponseFailureException) {
            this.message
        } else if (message == null || message.isEmpty()) {
            default
        } else if (default.isEmpty()) {
            message
        } else {
            "${default}，${message}"
        }
    }

    protected suspend fun toastInMain(message: String) = withContext(Dispatchers.Main) {
        ToastUtils.showShort(message)
    }

    protected fun toast(message: String) {
        ToastUtils.showShort(message)
    }

}