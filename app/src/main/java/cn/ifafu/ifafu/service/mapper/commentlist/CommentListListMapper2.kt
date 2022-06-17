package cn.ifafu.ifafu.service.mapper.commentlist

import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.bean.dto.IFResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

class CommentListListMapper2 : ICommentListMapper {

    override fun map(html: String): IFResponse<MutableList<CommentItem>> {
        val document = Jsoup.parse(html)
        val table = document.select("li[class=\"top\"]")
        var menu: Element? = null
        for (t in table) {
            if (t.text().contains("教学质量评价")) {
                menu = t
                break
            }
        }
        if (menu == null) {
            return IFResponse.failure("一键评教出错")
        }
        val lis = menu.getElementsByTag("a")
        if (lis.size == 1) {
            return IFResponse.failure("无需评教")
        }
        val commentItems: MutableList<CommentItem> = ArrayList()
        for (i in 1 until lis.size) {
            val li = lis[i]
            val item = CommentItem(
                courseName = li.text(),
                commentFullUrl = li.attr("href")
            )
            commentItems.add(item)
        }
        return IFResponse.success(commentItems)
    }
}