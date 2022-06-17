package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.di.Jiaowu
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.service.parser.ScoreParser2
import cn.ifafu.ifafu.util.toZFRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

/**
 * @author KQiang Weng
 */
class ScoreService @Inject constructor(
    @Jiaowu private val client: OkHttpClient,
    private val zfHttpClient: ZFHttpClient,
) {

    /**
     * 获取成绩
     *
     * @return [IFResponse]
     */
    suspend fun getAllScore(user: User) = withContext(Dispatchers.IO) {
        return@withContext zfHttpClient.ensureLogin(user) {
            val scoreUrl = ZfUrlProvider.getUrl(ZFApiEnum.SCORE, user)
            val mainUrl = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
            zfHttpClient.needParams(scoreUrl, mainUrl, true) { _, hiddenParams ->
                val params = HashMap<String, String>()
                params.putAll(hiddenParams)
                params["ddlxn"] = "全部"
                params["ddlxq"] = "全部"
                params["btnCx"] = ""
                val request = Request.Builder()
                    .post(params.toZFRequestBody())
                    .url(scoreUrl)
                    .addHeader("Referer", scoreUrl)
                    .build()
                val response = client.newCall(request).execute()
                zfHttpClient.parseHtml(response) { html ->
                    ScoreParser2(user).parse(html)
                }
            }
        }
    }

}