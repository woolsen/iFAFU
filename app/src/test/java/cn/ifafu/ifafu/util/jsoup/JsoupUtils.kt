package cn.ifafu.ifafu.util.jsoup

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object JsoupUtils {

    /**
     * 查找value为[buttonText]的submit按钮所在的form表单
     *
     * @return 对应的[Element]，未找到返回null
     */
    fun findParentForm(html: String, buttonText: String): Element? {
        val jsoup = Jsoup.parse(html)
        val button = jsoup.select("input[value=$buttonText]").first()
            ?: return null

        // 查找父<form>
        var form = button.parent()
        while (form != null && form.tagName() != "form") {
            form = form.parent()
        }
        if (form == null) {
            return null
        }



        return form
    }

}