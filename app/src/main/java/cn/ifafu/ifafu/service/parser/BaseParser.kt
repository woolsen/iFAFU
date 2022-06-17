package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.exception.NoAuthException
import org.jsoup.nodes.Document

abstract class BaseParser<T> {

    @Throws(Exception::class)
    abstract fun parse(html: String): T

    protected fun getAccount(document: Document): String {
        val e = document.select("span[id=\"Label5\"]")
        return e.text().replace("学号：", "")
    }

    protected fun check(html: String) {
        if (html.contains("请登录") || html.contains("请重新登陆") || html.contains("302 Found")) {
            throw NoAuthException()
        }
    }
}
