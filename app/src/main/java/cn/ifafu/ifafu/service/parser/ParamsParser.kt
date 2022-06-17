package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.IFResponse
import org.jsoup.Jsoup
import java.util.*
import java.util.regex.Pattern

class ParamsParser {

    fun parse(html: String): IFResponse<MutableMap<String, String>> {
        val p = Pattern.compile("alert\\('.*'\\);")
        val m = p.matcher(html)
        if (m.find()) {
            val s = m.group()
            if (s.matches("现在不能查询".toRegex())) {
                return IFResponse.failure("现在不能查询")
            }
        }
        val params = HashMap<String, String>()
        val document = Jsoup.parse(html)
        val elements = document.select("input[type=\"hidden\"]")
        for (element in elements) {
            params[element.attr("name")] = element.attr("value")
        }
        return IFResponse.success(params)
    }

}
