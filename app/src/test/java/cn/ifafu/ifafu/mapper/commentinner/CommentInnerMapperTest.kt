package cn.ifafu.ifafu.mapper.commentinner

import cn.ifafu.ifafu.service.mapper.commentinner.CommentInnerMapper
import cn.ifafu.ifafu.util.FileUtils
import org.junit.Test

class CommentInnerMapperTest {

    private val mapper = CommentInnerMapper()

    @Test
    fun test01() {
        val html =
            FileUtils.readRelative(this.javaClass, "/html/fafu_inner_comment01.html", "utf-8")
        val response = mapper.map(html)
        println(response)
    }

}