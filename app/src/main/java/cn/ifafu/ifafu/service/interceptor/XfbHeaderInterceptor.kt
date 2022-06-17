package cn.ifafu.ifafu.service.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class XfbHeaderInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .addHeader("Prama", "no-cache")
            .addHeader("Cache-Control", "no-cache")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.90 Mobile Safari/537.36"
            )
        return chain.proceed(builder.build())
    }
}