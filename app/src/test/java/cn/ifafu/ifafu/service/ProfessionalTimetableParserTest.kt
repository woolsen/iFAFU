package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.Constants.PACKAGE_ABSOLUTE_PATH
import cn.ifafu.ifafu.TermOption
import cn.ifafu.ifafu.util.FileUtils
import org.jsoup.Jsoup


class ProfessionalTimetableParserTest {
    companion object {

        val html: String =
            FileUtils.read("$PACKAGE_ABSOLUTE_PATH/service/ProfessionalTimetable01.html", "utf-8")

        @JvmStatic
        fun main(args: Array<String>) {
            optionParserTest()
        }

        private fun optionParserTest() {
            val optionsEles = Jsoup.parse(html)
                .getElementById("Table1")
                .children()[0]
                .children()
                .flatMap { it.select("td") }
                .map { oneOptions ->
                    val head = oneOptions.ownText().replace(":", "")
                    var selectedOp: TermOption.Op? = null
                    val ops = oneOptions
                        .getElementsByTag("option")
                        .map {
                            TermOption.Op(it.text(), it.`val`()).apply {
                                if (it.attr("selected").isNotEmpty()) {
                                    selectedOp = this
                                }
                            }
                        }
                    TermOption(head, ops).apply {
                        this.selectedIndex = ops.indexOf(selectedOp)
                    }
                }
            optionsEles.forEach {
                println(it)
                println("\n ------------- \n")
            }
        }
    }
}