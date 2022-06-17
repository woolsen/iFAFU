package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.TermOptions
import org.jsoup.Jsoup

class OptionParser : BaseOptionParser() {

    /**
     * @return 学年和学期选项
     */
    fun parse(html: String): TermOptions {
        return parseOption(Jsoup.parse(html))
    }

}