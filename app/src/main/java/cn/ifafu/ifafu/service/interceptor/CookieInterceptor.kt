package cn.ifafu.ifafu.service.interceptor

import cn.ifafu.ifafu.entity.ElecCookie
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.db.dao.ElecCookieDao
import com.blankj.utilcode.util.SPUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class CookieInterceptor(
    private val elecCookieDao: ElecCookieDao
): Interceptor {

    private var cookie: ElecCookie? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        var c = cookie
        if (c == null) {
            val account = SPUtils.getInstance(Constants.SP_USER_INFO).getString("account")
            c = elecCookieDao.elecCookie(account) ?: ElecCookie().apply {
                this.account = account
                elecCookieDao.save(this)
            }
        }
        builder.addHeader("Cookie", c.toCookieString())
        val response = chain.proceed(builder.build())
        if (response.headers("Set-Cookie").isNotEmpty()) {
            for (header in response.headers("Set-Cookie")) {
                val kv = header.substring(0, header.indexOf(";")).split("=").toTypedArray()
                if (kv.size < 2) {
                    c[kv[0]] = ""
                } else {
                    c[kv[0]] = kv[1]
                }
            }
            elecCookieDao.save(c)
        }
        cookie = c
        return response
    }
}