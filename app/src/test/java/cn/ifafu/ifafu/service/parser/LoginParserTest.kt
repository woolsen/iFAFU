package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.constant.ResultCode
import cn.ifafu.ifafu.util.TestUtils
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

/**
 * @author Woolsen
 */
class LoginParserTest {

    private val parser = LoginParser()
    private val charset = Charset.forName("gb2312")

    @Test
    fun change_password_test() {
        val html =
            TestUtils.getResourceAsStream("html/login/change_password/change_password_01.html")!!
                .reader(charset = Charset.forName("gb2312"))
                .readText()
        val response = parser.parse(html)
        println(response)
        Assert.assertTrue(!response.isSuccess())
        Assert.assertEquals(ResultCode.NEED_CHANGE_PASSWORD, response.code)
    }

    @Test
    fun password_error_test() {
        val dir = TestUtils.getResource("html/login/password_error")!!
        File(dir.path).listFiles()!!.forEach { file ->
            val html = file.readText(charset)
            val response = parser.parse(html)
            println(response)
            Assert.assertTrue(file.name, !response.isSuccess())
            Assert.assertEquals(file.name, ResultCode.PASSWORD_ERROR, response.code)
        }
    }
}