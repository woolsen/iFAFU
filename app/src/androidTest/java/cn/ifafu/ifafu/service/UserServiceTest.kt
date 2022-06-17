package cn.ifafu.ifafu.service

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.interceptor.JWCookieInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserServiceTest {

    companion object {
        private const val TAG = "UserServiceTest"
    }


    private val users = listOf(
        User(
            account = "3206604021",
            password = "xm20020205.",
            school = User.FAFU
        ),
        User(
            account = "196712038",
            password = "zyh010420",
            school = User.FAFU_JS
        )
    )

    private lateinit var userService: UserService

    @Before
    fun before() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val loggingInterceptor = HttpLoggingInterceptor { Log.d(TAG, it) }.apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        val database = JiaowuDatabase.getInstance(appContext)
        val sharedPreferences =
            appContext.getSharedPreferences(Constants.SP_COOKIE, Context.MODE_PRIVATE)
        val client = OkHttpClient.Builder()
            .addInterceptor(JWCookieInterceptor(sharedPreferences))
            .addInterceptor(loggingInterceptor)
            .build()
        userService = UserService(ZFHttpClient(client, database.userDao, appContext))
    }

    @Test
    fun login_password_error_test(): Unit = runBlocking {
        for (user in users) {
            val u = User(
                account = user.account,
                password = "123456",
                school = user.school,
            )
            val response = userService.login(u)
            Log.d(TAG, "response: $response")
            assertTrue(!response.isSuccess())
            assertTrue(response.message.contains("密码错误"))
        }
    }

    @Test
    fun login_success_test(): Unit = runBlocking {
        for (user in users) {
            val u = User(
                account = user.account,
                password = user.password,
                school = user.school,
            )
            val response = userService.login(u)
            Log.d(TAG, "user: ${user}, response: $response")
            assertTrue(response.isSuccess())
        }
    }

    /**
     * 测试账号不存在
     */
    @Test
    fun login_account_not_found_test(): Unit = runBlocking {
        users.map { it.school }.toSet()
            .forEach { school ->
                val u = User(
                    account = "123456789",
                    password = "123456789",
                    school = school,
                )
                val response = userService.login(u)
                Log.d(TAG, "school: ${school}, response: $response")
                assertTrue(!response.isSuccess())
                assertTrue(response.message.contains("不存在".toRegex()))
            }
    }


}