package cn.ifafu.ifafu.mapper.commentcomit

import cn.ifafu.ifafu.service.mapper.commentcommit.CommentCommitMapper
import cn.ifafu.ifafu.util.FileUtils
import org.junit.Test

class CommentCommitMapperTest {

    private val mapper = CommentCommitMapper()

    @Test
    fun test01() {
        val html = FileUtils.readRelative(
            this.javaClass, "/html/fafu_success.html", "utf-8"
        )
        val response = mapper.map(html)
        println(response)
    }

}