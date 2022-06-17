package cn.ifafu.ifafu.service

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.interceptor.JWCookieInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test

class ZFHttpClientTest {

    private lateinit var client: ZFHttpClient

    companion object {
        private const val TAG = "ZFHttpClientTest"
    }

    @Before
    fun before() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val loggingInterceptor = HttpLoggingInterceptor { Log.d(TAG, it) }.apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        val database = JiaowuDatabase.getInstance(appContext)
        val sharedPreferences =
            appContext.getSharedPreferences(Constants.SP_COOKIE, Context.MODE_PRIVATE)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(JWCookieInterceptor(sharedPreferences))
            .addInterceptor(loggingInterceptor)
            .build()
        client = ZFHttpClient(okHttpClient, database.userDao, appContext)
    }

    @Test
    fun test() {
        val user = User("3206604021", "xm20020205.", school = User.FAFU)
        val request = Request.Builder()
            .get()
            .url("http://jwgl.fafu.edu.cn/${user.token}/xs_main.aspx?xh=3206604021")
            .build()
        val response = client.execute(request, user)
        println("<<< response >>>")
        println(response.body?.string())
    }

}