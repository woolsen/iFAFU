package cn.ifafu.ifafu

import cn.ifafu.ifafu.service.common.ZFFormBody.Companion.toZFFormBody
import okio.Buffer
import java.nio.charset.Charset

object Test {


    @JvmStatic
    fun main(args: Array<String>) {
       val formBody = mapOf("user" to "123+=.").toZFFormBody()
        val sink = Buffer()
        formBody.writeTo(sink)
        sink.readString(Charset.forName("gb2312"))
    }

}