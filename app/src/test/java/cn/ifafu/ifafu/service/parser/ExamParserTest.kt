package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.Constants.PACKAGE_ABSOLUTE_PATH
import cn.ifafu.ifafu.util.FileUtils

object ExamParserTest  {

    private val html = FileUtils.read("$PACKAGE_ABSOLUTE_PATH/sample/Exam01.html", "utf-8")

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = OptionParser()

        println(parser.parse(html))
    }
}