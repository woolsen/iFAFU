package cn.ifafu.ifafu.util

import cn.ifafu.ifafu.Constants
import java.io.*

object FileUtils {
    @JvmOverloads
    @Throws(IOException::class)
    fun read(filePath: String?, charset: String? = "gb2312"): String {
        val `is`: InputStream = FileInputStream(filePath) //文件读取
        val isr = InputStreamReader(`is`, charset) //解码
        var c: Int
        val sb = StringBuilder()
        while (isr.read().also { c = it } != -1) {
            sb.append(c.toChar())
        }
        return sb.toString()
    }

    @Throws(IOException::class)
    fun readRelative(relativePath: String): String {
        return read(Constants.PACKAGE_ABSOLUTE_PATH + relativePath, "gb2312")
    }

    @Throws(IOException::class)
    fun readRelative(clazz: Class<*>, relativePath: String, encode: String = "gb2312"): String {
        val relative = clazz.`package`!!.name.replace(".", "/")
        return read("${Constants.PACKAGE_ABSOLUTE_PATH}/$relative/$relativePath", encode)
    }

    fun list(dirPath: String?): Array<String> {
        val dir = File(dirPath)
        return dir.list()
    }
}