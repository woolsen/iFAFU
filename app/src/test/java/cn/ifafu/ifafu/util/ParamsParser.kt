package cn.ifafu.ifafu.util

import org.jsoup.Jsoup
import java.util.*
import java.util.regex.Pattern

class ParamsParser {

    fun parse(html: String): Map<String, String> {
        val p = Pattern.compile("alert\\('.*'\\);")
        val m = p.matcher(html)
        if (m.find()) {
            val s = m.group()
            if (s.matches("现在不能查询".toRegex())) {
                return emptyMap()
            }
        }
        val params = HashMap<String, String>()
        val document = Jsoup.parse(html)
        val elements = document.select("input[type=\"hidden\"]")
        for (element in elements) {
            params[element.attr("name")] = element.attr("value")
        }
        return params
    }

}
