package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.IFResponse
import java.util.regex.Pattern

class ChangePasswordParser : BaseParser<IFResponse<Unit>>() {

    companion object {
        private const val REGEX_ALERT_SCRIPT = "<script language='javascript'>alert\\('.*'\\);</script>"
    }

    override fun parse(html: String): IFResponse<Unit> {
        check(html)
        val pattern = Pattern.compile(REGEX_ALERT_SCRIPT)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            //去除无效信息
            val alert = matcher.group()
                .replace(
                    REGEX_ALERT_SCRIPT.toRegex(),
                    ""
                )
            if (alert.contains("成功")) {
                IFResponse.success(Unit)
            } else {
                IFResponse.failure(alert)
            }
        } else {
            IFResponse.failure("密码修改失败")
        }
    }
}