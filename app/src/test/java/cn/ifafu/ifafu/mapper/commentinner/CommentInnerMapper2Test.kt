package cn.ifafu.ifafu.mapper.commentinner

import cn.ifafu.ifafu.service.mapper.commentinner.CommentInnerMapper2
import cn.ifafu.ifafu.util.FileUtils
import org.junit.Test

class CommentInnerMapper2Test {

    private val mapper = CommentInnerMapper2()

    @Test
    fun test01() {
        val html = FileUtils.readRelative(
            this.javaClass, "/html/2_01.html", "utf-8"
        )
        val response = mapper.map(html)
            .toSortedMap()
            .forEach { e ->
                println("${e.key}=${e.value}")
            }
    }

}