package cn.ifafu.ifafu.util

import java.io.InputStream
import java.net.URL

/**
 * @author Woolsen
 */

object TestUtils {
    fun getResourceAsStream(name: String): InputStream? {
        return TestUtils.javaClass.classLoader!!.getResourceAsStream(name)
    }

    fun getResource(name: String): URL? {
        return TestUtils.javaClass.classLoader!!.getResource(name)
    }
}
