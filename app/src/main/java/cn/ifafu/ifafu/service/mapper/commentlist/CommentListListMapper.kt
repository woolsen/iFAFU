package cn.ifafu.ifafu.service.mapper.commentlist

import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.bean.dto.IFResponse
import org.jsoup.Jsoup
import java.util.*
import java.util.regex.Pattern

class CommentListListMapper : ICommentListMapper {

    override fun map(html: String): IFResponse<List<CommentItem>> {
        // 当评教系统开起来时，并且评教过，会显示『您已经评价过！』
        if (html.contains("您已经评价过！")) {
            return IFResponse.failure("您已经评价过！")
        }
        val document = Jsoup.parse(html)
        // 定位到评价列表表格
        val table = document.select("table[id=\"Table2\"]")
        if (table.size == 0) {
            return IFResponse.failure("评教系统暂未开放！")
        }

        val lines = table[0].getElementsByTag("tr")
        val list: MutableList<CommentItem> = ArrayList()

        val commentPathPattern = Pattern.compile("open\\(.*ll'")

        // 去除第一行标题
        lines.drop(1).forEach { element ->
            // 一行中每格的的信息
            val tds = element.getElementsByTag("td")
            // 获取课程名字
            val courseName = tds[0].text()
            // 获取老师和评教地址。一门课可能有多个老师。
            val teacherAndUrlElements = tds[1].getElementsByTag("a")
            teacherAndUrlElements.forEach { teacherAndUrlElement ->
                val urlMatcher = commentPathPattern.matcher(teacherAndUrlElement.outerHtml())
                if (urlMatcher.find()) {
                    val text = teacherAndUrlElement.text()
                    val isDone = text.contains("已评价")
                    val teacher = text.replace("\\(.*\\)".toRegex(), "")
                    var path = urlMatcher.group()
                        .replace("&amp;".toRegex(), "&")
                    path = path.substring(6, path.length - 1)
                    val item = CommentItem(
                        courseName = courseName,
                        teacherName = teacher,
                        commentFullUrl = path,
                        isDone = isDone
                    )
                    list.add(item)
                }
            }
        }
        return IFResponse.success(list)
    }

}