package cn.ifafu.ifafu.util.jsoup

import cn.ifafu.ifafu.util.FileUtils
import org.junit.Test

class JsoupUtilsTest {

    @Test
    fun test() {
        val html = FileUtils.read(
            "/Users/woolsen/StudioProjects/iFAFU-Android/app/src/test/java/cn/ifafu/ifafu/mapper/comment/html/fafu_inner_comment01.html",
            "utf-8"
        )
        val element = JsoupUtils.findParentForm(html, " 保  存 ")
        if (element == null) {
            println("未找到")
            return
        }
        element.getElementsByTag("input")
            .dropWhile { el ->
                el.attr("type") == "submit"
                        || el.hasAttr("disabled")
            }
            .forEach {
                println(it)
            }
    }
}