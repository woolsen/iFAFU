package cn.ifafu.ifafu.service.mapper.commentcommit

import cn.ifafu.ifafu.bean.dto.IFResponse
import java.util.regex.Pattern

class CommentCommitMapper : ICommentCommitMapper {
    override fun map(html: String): IFResponse<Unit> {
        val pattern = Pattern.compile("alert\\('.*'\\);")
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            var alertMessage = matcher.group()
            alertMessage = alertMessage.substring(7, alertMessage.length - 3)
            if (alertMessage.contains("完成评价")) {
                IFResponse.success(Unit)
            } else {
                IFResponse.failure(alertMessage)
            }
        } else {
            IFResponse.failure("评教失败")
        }
    }
}