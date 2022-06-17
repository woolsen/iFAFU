package cn.ifafu.ifafu.service.mapper.commentinner

import org.jsoup.Jsoup
import java.util.*

class CommentInnerMapper : ICommentInnerMapper {

    override fun map(html: String): MutableMap<String, String> {
        val document = Jsoup.parse(html)
        val table = document.select("table[id=\"Datagrid1\"]") //定位到表格
        val map = HashMap<String, String>()
        val names = table[0].getElementsByTag("table")
            .drop(1) // 去除标题栏
            .mapNotNull { ele -> //提取每个评教条目的第一个选项（最高分）
                ele.select("input").first()?.attr("name")
            }
        names.forEach { name ->
            map[name] = "94"
        }
        // 评教规则不允许所有同样分数
        // 随机一个评教条目为第二高分
        map[names.random()] = "82"
        return map
    }
}
