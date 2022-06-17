package cn.ifafu.ifafu.service.interceptor

import cn.ifafu.ifafu.util.IFTokenUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 请求拦截器
 * 为Request添加Access-Token请求头
 *
 * @author Woolsen 2020/10/01
 */
class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = IFTokenUtils.getToken()
        val req = if (accessToken.isNotEmpty()) {
            chain.request().newBuilder()
                .addHeader("Access-Token", accessToken)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(req)
    }
}