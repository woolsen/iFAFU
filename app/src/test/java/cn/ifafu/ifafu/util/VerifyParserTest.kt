package cn.ifafu.ifafu.util

import android.graphics.BitmapFactory
import org.junit.Test
import java.io.File

class VerifyParserTest {

    @Test
    fun test() {
        val parser = VerifyParser()
        parser.init()
        val image = File("/Users/woolsen/StudioProjects/iFAFU-Android/app/src/test/java/cn/ifafu/ifafu/resource/capture/5B5CC564-BF75-4507-B451-B0E4AB2DBE22.png")
        val bitmap = BitmapFactory.decodeStream(image.inputStream())
        val answer = parser.todo(bitmap)
        println("Answer: $answer")
    }
}