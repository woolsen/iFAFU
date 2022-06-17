package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.constant.ResultCode
import org.jsoup.Jsoup
import java.util.regex.Pattern

class LoginParser {

    /**
     * @param html 网页信息
     * @return [IFResponse.success] 登录成功   返回学生名字
     *  [IFResponse.failure] 失败   返回错误信息
     */
    fun parse(html: String): IFResponse<String> {
        val doc = Jsoup.parse(html)
        val ele = doc.getElementById("xhxm")
        if (ele != null) {
            val name = ele.text().replace("同学", "")
            return IFResponse.success(name)
        } else if (html.contains("输入原密码")) {
            val notice = doc.select("span[id=lbMsgts]").text()
                .replace("注：", "")
            return if (notice.isNotBlank()) {
                IFResponse.failure(ResultCode.NEED_CHANGE_PASSWORD, notice)
            } else {
                IFResponse.failure(ResultCode.NEED_CHANGE_PASSWORD, "新生请先修改密码")
            }
        }
        val scripts = doc.select("script[language=javascript]")
        val message = scripts.map { it.html() }
            .find { script -> script.startsWith("alert") }
            ?.let { getAlertMessage(it) }
        return if (message != null) {
            IFResponse.failure(message)
        } else if (html.contains("ERROR")) {
            IFResponse.failure("教务系统又崩溃了！")
        } else {
            IFResponse.failure("网络异常")
        }
    }

    private fun getAlertMessage(script: String): String {
        val p = Pattern.compile("alert\\('.*'\\);")
        val m = p.matcher(script)
        if (m.find()) {
            val s = m.group()
            return s.substring(7, s.length - 3)
        }
        return ""
    }
}