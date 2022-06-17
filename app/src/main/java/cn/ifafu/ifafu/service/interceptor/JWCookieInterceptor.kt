package cn.ifafu.ifafu.service.interceptor

import android.content.SharedPreferences
import cn.ifafu.ifafu.constant.Constants
import com.blankj.utilcode.util.SPUtils
import okhttp3.Interceptor
import okhttp3.Response

class JWCookieInterceptor(
    private val sharedPreferences: SharedPreferences,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val cookie = sharedPreferences.getString("cookie", null)
        val request = if (!cookie.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Cookie", cookie)
                .build()
        } else {
            chain.request()
        }
        val response = chain.proceed(request)
        val cookieString = response.header("Set-Cookie")
        if (cookieString != null) {
            val cookies = cookieString.substringBefore(";")
            SPUtils.getInstance(Constants.SP_COOKIE).put("cookie", cookies)
        }
        return response
    }
}