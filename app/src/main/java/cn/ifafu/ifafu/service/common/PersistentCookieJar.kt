package cn.ifafu.ifafu.service.common

import cn.ifafu.ifafu.constant.Constants
import com.blankj.utilcode.util.SPUtils
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import timber.log.Timber

/**
 * Cookie持久化
 */
class PersistentCookieJar : CookieJar {

    private val sp = SPUtils.getInstance("cookie_xfb")

    private val account: String
        get() = SPUtils.getInstance(Constants.SP_USER_INFO).getString("account")

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        Timber.d("loadForRequest >> ${sp.getStringSet(account).joinToString(", ")}")
        return emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Timber.d("saveFromResponse >> ${cookies.joinToString(", ")}")
    }

}