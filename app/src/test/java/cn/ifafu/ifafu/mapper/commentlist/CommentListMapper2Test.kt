package cn.ifafu.ifafu.mapper.commentlist

import cn.ifafu.ifafu.service.mapper.commentlist.CommentListListMapper2
import cn.ifafu.ifafu.util.FileUtils
import org.junit.Test

class CommentListMapper2Test {

    private val mapper = CommentListListMapper2()

    @Test
    fun testAlready() {

    }

    @Test
    fun testHalf01() {
        val html =FileUtils.readRelative(this.javaClass, "/html/fafujs_half_01.html", "utf-8")
        val response = mapper.map(html)
        val data = response.data
        if (data == null) {
            println("未找到")
            return
        }
        data.forEach {
            println(it)
        }
    }
}