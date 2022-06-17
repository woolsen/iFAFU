package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.service.parser.ExamParser2
import cn.ifafu.ifafu.service.parser.OptionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * @author KQiang Weng
 */
class ExamService @Inject constructor(
    private val client: ZFHttpClient,
) {

    /**
     * 获取考试
     *
     * @return [IFResponse]
     */
    suspend fun getExams(user: User, year: String, term: String) = withContext(Dispatchers.IO) {
        client.ensureLogin(user) {
            val examUrl = ZfUrlProvider.getUrl(ZFApiEnum.EXAM, user)
            val mainUrl = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
            client.needParams(examUrl, mainUrl, true) { paramHtml, hiddenParams ->
                val defaultOptions = try {
                    OptionParser().parse(paramHtml)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@needParams IFResponse.failure("学期选项解析失败")
                }.selected

                /**
                 * 若查询的学期为默认显示学期，则只能通过GET方式获取网页，否则获取不到考试信息
                 */
                val defaultYear = defaultOptions.year
                val defaultTerm = defaultOptions.term
                if (year == defaultYear && term == defaultTerm) {
                    // 这边直接解析当前页面考试信息，无需再次获取
                    val parser = ExamParser2(user)
                    parser.parse(paramHtml)
                } else {
                    val params = HashMap<String, String>()
                    params.putAll(hiddenParams)
                    params["xnd"] = year
                    params["xqd"] = term
                    params["btnCx"] = " 查  询 "
                    client.parseHtml(client.post(examUrl, mainUrl, params)) { html ->
                        ExamParser2(user).parse(html)
                    }
                }
            }
        }
    }

}