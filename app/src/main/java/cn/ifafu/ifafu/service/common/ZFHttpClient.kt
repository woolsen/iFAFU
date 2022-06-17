package cn.ifafu.ifafu.service.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.constant.ResultCode
import cn.ifafu.ifafu.db.dao.UserDao
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.service.common.ZFFormBody.Companion.toZFFormBody
import cn.ifafu.ifafu.service.parser.BaseParser
import cn.ifafu.ifafu.service.parser.LoginParser
import cn.ifafu.ifafu.service.parser.ParamsParser
import cn.ifafu.ifafu.service.parser.VerifyParser
import cn.ifafu.ifafu.ui.login.LoginActivity
import cn.ifafu.ifafu.util.BitmapUtil
import cn.ifafu.ifafu.util.encode
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.readBomAsCharset
import java.io.IOException
import java.nio.charset.Charset

/**
 * 问答环节
 *
 * Q: 为什么把UserDao放入一个HTTP工具中？
 *
 * A: 教务系统登录系统不同于普通APP登录系统，教务系统的token有效期是比较短的。一旦教务系统token过
 * 期，便需要账号密码登录来重新获取token，在此处便将账号密码也作为Token的一部分。为了能获取到用户账
 * 号密码，此处就把[UserDao]作为构造参数传递给[ZFHttpClient]。
 */
class ZFHttpClient constructor(

    /**
     * 用于网络请求
     */
    private val client: OkHttpClient,

    /**
     * 用于获取账号密码
     */
    private val userDao: UserDao,

    /**
     * 用于加载Assert中的识别验证码文件
     */
    private val context: Context,
) {

    companion object {
        /**
         * 避免短时间内重复登录
         */
        private var lastLoginTime = 0L
        private var lastLoginDeferred: Deferred<IFResponse<User>>? = null
        private var lastLoginAccount = ""

        private fun Map<String, String>.toRequestBody(): RequestBody {
            val body = this.entries
                .joinToString(separator = "&") {
                    it.key + "=" + it.value.encode("gb2312")
                }
            return body.toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
        }
    }

    /**
     * 立即调用教务系统请求并阻塞，直到可以处理响应或响应出错。当教务系统返回要求登录时，使用[User]
     * 进行重新登录，并再次发送教务系统请求。若重新登录返回登录失败信息，则抛出[Failure]异常。否则
     * 返回教务系统请求的[Response]。
     *
     * 为了避免资源泄漏，调用者应该关闭[Response]，而[Response]又会关闭底层的[ResponseBody]。
     * ```java
     * // Java: 确保关闭响应（和底层响应主体）
     * try（Response response = client.newCall(request.execute()) {
     *     ...
     * }
     * ```
     * ```kotlin
     * // Kotlin: 确保关闭响应（和底层响应主体）
     * client.newCall(request.execute().use {
     *     ...
     * }
     * ```
     *
     * @param user 当不为null时，将自动处理Token过期情况，执行重新登录并再次发起请求。
     * @return 教务系统请求的[Response]。若触发重新登录，则返回最后一次请求的[Response]。
     *
     * @throws IOException 如果由于取消、连接问题或超时而无法执行请求。因为网络在交换过程中可能会
     * 失败，所以远程服务器可能在失败之前接受了请求。
     * @throws Failure 触发重新登录并且登录失败。
     */
    @Throws(IOException::class, Failure::class)
    fun execute(request: Request, user: User? = null): Response {
        val response = client.newCall(request).execute()
        val responseBody = response.body
        if (user == null || responseBody == null) {
            return response
        }

        val source = responseBody.source()
        val charset = responseBody.contentType()?.charset()
            ?: source.readBomAsCharset(Charset.forName("gb2312"))
        val buffer = source.buffer.clone()
        val html = buffer.readString(charset)

        var priorResponse: Response? = response.priorResponse
        while (priorResponse?.priorResponse != null) {
            priorResponse = priorResponse.priorResponse
        }

        if ((priorResponse != null &&
                    priorResponse.isRedirect &&
                    priorResponse.header("Location")?.contains("logout".toRegex()) == true)
            || html.contains("请登录|请重新登陆|302 Found|Object moved".toRegex())
        ) {
            val loginResponse = login(user)
            if (!loginResponse.isSuccess()) {
                throw Failure(loginResponse.code, loginResponse.message)
            }
        }

        return response
    }

    fun post(url: String, body: Map<String, String>): Response {
        val request = Request.Builder()
            .post(body.toRequestBody())
            .url(url)
            .build()
        return client.newCall(request).execute()
    }

    /**
     * 登录
     *
     * @return 学生名字
     */
    fun login(user: User): IFResponse<User> = runBlocking {
        val loginAccount = user.account + user.password
        val loginDeferred = lastLoginDeferred
        val current = System.currentTimeMillis()
        //只开启一个登陆Job
        if (loginAccount == lastLoginAccount && loginDeferred != null && current - lastLoginTime < 10 * 1000) {
            return@runBlocking loginDeferred.await()
        }
        lastLoginTime = System.currentTimeMillis()
        lastLoginAccount = loginAccount

        val deferred = async {
            val loginUrl = ZfUrlProvider.getUrl(ZFApiEnum.LOGIN, user)
            val verifyUrl = ZfUrlProvider.getUrl(ZFApiEnum.VERIFY, user)
            VerifyParser.getInstance(context).use { verifyParser ->
                needParams(url = loginUrl, checkLogin = false) { _, hiddenParams ->
                    val params = HashMap<String, String>()
                    params.putAll(hiddenParams)
                    params["txtUserName"] = user.account
                    params["Textbox1"] = ""
                    params["TextBox2"] = user.password
                    params["RadioButtonList1"] = ""
                    params["Button1"] = ""
                    params["lbLanguage"] = ""
                    params["hidPdrs"] = ""
                    params["hidsc"] = ""
                    val loginParser = LoginParser()

                    /**
                     * 如果验证码错误，则重复登录，最多重复登录10次
                     * 超出后调出循环返回登录错误信息
                     */
                    var repeat = 0
                    loop@ while (repeat++ < 10) {
                        val resp = getCapture(verifyUrl) { capture ->
                            val verifyCode = verifyParser.todo(capture)
                            params["txtSecretCode"] = verifyCode
                            val request = Request.Builder()
                                .url(loginUrl)
                                .post(params.toZFFormBody())
                                .build()
                            parseHtml(client.newCall(request).execute()) { html ->
                                loginParser.parse(html)
                            }
                        }
                        if (resp.code == IFResponse.SUCCESS) {
                            user.name = resp.data ?: ""
                            return@needParams IFResponse.success(data = user)
                        } else if (resp.code == IFResponse.FAILURE) {
                            if (!resp.message.contains("验证码")) {
                                return@needParams IFResponse.failure(resp.code, resp.message)
                            }
                        } else {
                            return@needParams IFResponse.create<User>(resp.code, resp.message)
                        }
                    }
                    return@needParams IFResponse.failure("登录出错")
                }
            }
        }
        lastLoginDeferred = deferred
        return@runBlocking deferred.await()
    }

    suspend fun <T> ensureLogin(user: User, block: suspend () -> IFResponse<T>): IFResponse<T> {
        val blockResult = block()
        if (blockResult.code == IFResponse.NO_AUTH) {
            val loginResult = login(user)
            return if (loginResult.code == IFResponse.SUCCESS) {
                block()
            } else {
                if (loginResult.code == ResultCode.NEED_CHANGE_PASSWORD ||
                    loginResult.message.contains("密码错误")
                ) {
                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            userDao.deleteUserOnly(user.account)
                        }
                        Toast.makeText(context, loginResult.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
                IFResponse.failure(loginResult.code, loginResult.message)
            }
        }
        return blockResult
    }

    /**
     * 获取验证码Bitmap
     *
     * @return 验证码Bitmap
     */
    private inline fun <T> getCapture(
        captureUrl: String,
        crossinline capture: (Bitmap) -> IFResponse<T>,
    ): IFResponse<T> {
        val request = Request.Builder()
            .get()
            .url(captureUrl)
            .build()
        val response = client.newCall(request).execute()
        if (response.code == 302 || response.body == null) {
            throw Failure("获取验证码错误")
        } else {
            val bitmap = BitmapUtil.bytesToBitmap(response.body?.bytes())
            return capture.invoke(bitmap)
        }
    }

    /**
     * 获取Html表单隐藏参数
     *
     * @param url Url
     * @param referer 请求头Referer
     * @param checkLogin 是否检查登录状态
     */
    suspend fun <T> needParams(
        url: String,
        referer: String? = null,
        checkLogin: Boolean = true,
        block: suspend (html: String, params: Map<String, String>) -> IFResponse<T>,
    ): IFResponse<T> {
        val requestBuilder = Request.Builder()
            .url(url)
            .get()
        if (referer != null) {
            requestBuilder.header("Referer", referer)
        }
        val response = runInterruptible(Dispatchers.IO) {
            client.newCall(requestBuilder.build()).execute()
        }
        return response.parseHtml2(checkLogin) { html ->
            val resp = ParamsParser().parse(html)
            when (resp.code) {
                IFResponse.SUCCESS -> {
                    block.invoke(html, resp.data!!)
                }
                else -> {
                    IFResponse.failure(resp.message)
                }
            }
        }
    }


    /**
     * 将ResponseBody转换为Html
     *
     * @param checkLogin 是否检查登录状态
     */
    fun <T> responseToHtml(
        response: Response,
        checkLogin: Boolean = false,
        checkAlert: Boolean = true,
        parser: (html: String) -> IFResponse<T>,
    ): IFResponse<T> {
        val html = response.body?.string() ?: ""
        //当金山学院学生无法查询课表时，会返回"Object moved"字样
        if (checkLogin && html.contains("请登录|请重新登陆|302 Found|Object moved".toRegex())) {
            return IFResponse.noAuth()
        }
        if (checkAlert) {
            var alertScriptStartIndex = html.indexOf("<script language='javascript'>alert")
            if (alertScriptStartIndex == -1) {
                alertScriptStartIndex = html.indexOf("<script>alert")
            }
            if (alertScriptStartIndex != -1) {
                val alertScriptEndIndex = html.indexOf("');", alertScriptStartIndex)
                if (alertScriptEndIndex > alertScriptStartIndex + 37) {
                    val alertString =
                        html.substring(alertScriptStartIndex + 37, alertScriptEndIndex)
                            .replace("\\n", "\n")
                    return IFResponse.failure(alertString)
                }
            }
        }
        return parser.invoke(html)
    }

    /**
     * 解析Html
     *
     * @param checkLogin 是否检查登录状态
     */
    private suspend fun <T> Response.parseHtml2(
        checkLogin: Boolean = false,
        parser: suspend (html: String) -> IFResponse<T>,
    ): IFResponse<T> {
        val html = this.body?.string() ?: ""
        //当金山学院学生无法查询课表时，会返回"Object moved"字样
        if (checkLogin && html.contains("请登录|请重新登陆|302 Found|Object moved".toRegex())) {
            return IFResponse.noAuth()
        }
        var alertScriptStartIndex = html.indexOf("<script language='javascript'>alert")
        if (alertScriptStartIndex == -1) {
            alertScriptStartIndex = html.indexOf("<script>alert")
        }
        if (alertScriptStartIndex != -1) {
            val alertScriptEndIndex = html.indexOf("');", alertScriptStartIndex)
            if (alertScriptEndIndex > alertScriptStartIndex + 37) {
                val alertString = html.substring(alertScriptStartIndex + 37, alertScriptEndIndex)
                    .replace("\\n", "\n")
                return IFResponse.failure(alertString)
            }
        }
        return parser.invoke(html)
    }

    fun post(url: String, referer: String, map: Map<String, String>): Response {
        return client.newCall(
            Request.Builder()
                .url(url)
                .header("Referer", referer)
                .post(map.toRequestBody())
                .build()
        ).execute()
    }

    fun get(url: String, referer: String): Response {
        return client.newCall(
            Request.Builder()
                .url(url)
                .header("Referer", referer)
                .get()
                .build()
        ).execute()
    }

    /**
     * 解析Html
     *
     * @param checkLogin 是否检查登录状态
     */
    fun <T> parseHtml(
        response: Response,
        checkLogin: Boolean = false,
        parser: (html: String) -> IFResponse<T>,
    ): IFResponse<T> {
        val html = response.body?.string() ?: ""
        //当金山学院学生无法查询课表时，会返回"Object moved"字样
        if (checkLogin && html.contains("请登录|请重新登陆|302 Found|Object moved".toRegex())) {
            return IFResponse.noAuth()
        }
        var alertScriptStartIndex = html.indexOf("<script language='javascript'>alert")
        if (alertScriptStartIndex == -1) {
            alertScriptStartIndex = html.indexOf("<script>alert")
        }
        if (alertScriptStartIndex != -1) {
            val alertScriptEndIndex = html.indexOf("');", alertScriptStartIndex)
            if (alertScriptEndIndex > alertScriptStartIndex + 37) {
                val alertString = html.substring(alertScriptStartIndex + 37, alertScriptEndIndex)
                    .replace("\\n", "\n")
                return IFResponse.failure(alertString)
            }
        }
        return parser.invoke(html)
    }

    fun <T> Response.parse(parser: BaseParser<T>): T {
        val html = this.body?.string() ?: ""
        return parser.parse(html)
    }
}