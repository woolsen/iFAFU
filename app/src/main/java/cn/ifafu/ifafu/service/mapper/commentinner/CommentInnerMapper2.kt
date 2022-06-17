package cn.ifafu.ifafu.service.mapper.commentinner

import org.jsoup.Jsoup

class CommentInnerMapper2 : ICommentInnerMapper {
    override fun map(html: String): MutableMap<String, String> {
        val params = HashMap<String, String>()
        val table = Jsoup.parse(html)
            .getElementById("DataGrid1")
            ?: return mutableMapOf()
        // 添加空value参数
        table.select("input[type=text]").forEach { ele ->
            val name = ele.attr("name")
            params[name] = ""
        }
        // 添加select参数
        val names = table.getElementsByTag("select")
            .map { it.attr("name") }
        names.forEach { name ->
            params[name] = "优秀"
        }
        params[names.random()] = "良好"
        return params
    }
}