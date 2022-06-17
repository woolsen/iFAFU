package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.service.mapper.CommentCommitMapperProvider
import cn.ifafu.ifafu.service.mapper.CommentInnerMapperProvider
import cn.ifafu.ifafu.service.mapper.CommentListMapperProvider
import javax.inject.Inject

class CommentService @Inject constructor(
    private val zfHttpClient: ZFHttpClient,
) {

    suspend fun autoComment(user: User, item: CommentItem): IFResponse<Unit> {
        val mapper = CommentInnerMapperProvider.get(user.school)
        return zfHttpClient.needParams(item.commentFullUrl) { parmaHtml, hiddenParams ->
            val params = mapper.map(parmaHtml)
            params.putAll(hiddenParams)
            if (user.school == User.FAFU_JS) {
                params["pjxx"] = ""
                params["txt1"] = ""
                params["TextBox1"] = "0"
                params["Button1"] = " 保  存 "
            } else {
                params["txt_pjxx"] = ""
                params["Button1"] = " 提 交 "
            }
            val response = zfHttpClient.post(item.commentFullUrl, item.commentFullUrl, params)
            zfHttpClient.responseToHtml(response) { html ->
                if (html.contains("提交成功")) {
                    IFResponse.success(Unit)
                } else {
                    IFResponse.failure("提交失败")
                }
            }
        }
    }

    suspend fun getCommentList(user: User): IFResponse<List<CommentItem>> {
        val mapper = CommentListMapperProvider.get(user.school)
        val baseUrl = ZfUrlProvider.getUrl(ZFApiEnum.BASE, user)
        val commentListUrl = ZfUrlProvider.getUrl(ZFApiEnum.COMMENT, user)
        val refererUrl = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
        return zfHttpClient.ensureLogin(user) {
            val response = zfHttpClient.get(commentListUrl, refererUrl)
            zfHttpClient.parseHtml(response, checkLogin = true) { html ->
                mapper.map(html).apply {
                    // 将Url补充为完整地址
                    peekData {
                        it.map { item ->
                            item.commentFullUrl = baseUrl + item.commentFullUrl
                        }
                    }
                }
            }
        }
    }

    suspend fun commit(user: User): IFResponse<Unit> {
        val mapper = CommentCommitMapperProvider.get(user.school)
        val commentListUrl = ZfUrlProvider.getUrl(ZFApiEnum.COMMENT, user)
        val refererUrl = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
        return zfHttpClient.needParams(commentListUrl, refererUrl, true) { _, hiddenParams ->
            val param = hiddenParams.toMutableMap()
            param["btn_tj"] = ""
            val response = zfHttpClient.post(
                url = commentListUrl,
                referer = commentListUrl,
                map = param
            )
            zfHttpClient.responseToHtml(response, checkAlert = false) { html ->
                mapper.map(html)
            }
        }
    }
}