package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.Constants.PACKAGE_ABSOLUTE_PATH
import cn.ifafu.ifafu.util.FileUtils
import org.junit.Test

class JWServiceTest {

    private val path = PACKAGE_ABSOLUTE_PATH

    @Test
    fun parseAlertHtmlTest() {
        val html = FileUtils.read("$path/service/JSAlertHtml.html", "UTF-8")
        val alertScriptStartIndex = html.indexOf("<script language='javascript'>alert")
        if (alertScriptStartIndex != -1) {
            val alertScriptEndIndex = html.indexOf("');", alertScriptStartIndex)
            if (alertScriptEndIndex > alertScriptStartIndex + 37) {
                val alertString = html.substring(alertScriptStartIndex + 37, alertScriptEndIndex)
                        .replace("\\n", "\n")
                println(alertString)
            }
        } else {
            println("未找到Alert")
        }
    }

}