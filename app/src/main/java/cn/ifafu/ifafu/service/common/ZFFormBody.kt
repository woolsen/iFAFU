package cn.ifafu.ifafu.service.common

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.internal.toImmutableList
import okio.Buffer
import okio.BufferedSink
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * @author Woolsen
 */
class ZFFormBody(
    names: List<String>,
    values: List<String>,
) : RequestBody() {

    private val names: List<String> = names.toImmutableList()
    private val values: List<String> = values.toImmutableList()

    /**
     * FormBody中键值对的大小
     */
    val size: Int
        get() = names.size

    fun name(index: Int) = names[index]

    fun value(index: Int) = values[index]

    override fun contentType() = CONTENT_TYPE

    override fun contentLength() = writeOrCountBytes(null, true)

    override fun writeTo(sink: BufferedSink) {
        writeOrCountBytes(sink, false)
    }

    /**
     * 将此请求写入[sink]或测量其内容长度。我们有一种方法可以执行双重任务，以确保计数和内容一致，
     * 特别是当涉及到诸如测量头字符串的编码长度或编码整数的位数长度等棘手的操作时。
     */
    private fun writeOrCountBytes(sink: BufferedSink?, countBytes: Boolean): Long {
        var byteCount = 0L
        val buffer: Buffer = if (countBytes) Buffer() else sink!!.buffer

        for (i in names.indices) {
            if (i > 0) buffer.writeByte('&'.code)
            buffer.writeUtf8(URLEncoder.encode(names[i], "gb2312"))
            buffer.writeByte('='.code)
            buffer.writeUtf8(URLEncoder.encode(values[i], "gb2312"))
        }

        if (countBytes) {
            byteCount = buffer.size
            buffer.clear()
        }

        return byteCount
    }

    class Builder {
        private val names = mutableListOf<String>()
        private val values = mutableListOf<String>()

        fun add(name: String, value: String) = apply {
            names += name
            values += value
        }

        fun build(): ZFFormBody = ZFFormBody(names, values)
    }

    companion object {
        private val CONTENT_TYPE: MediaType = "application/x-www-form-urlencoded".toMediaType()
        private val CHARSET: Charset = Charset.forName("gb2312")

        fun Map<String, String>.toZFFormBody(): ZFFormBody {
            return ZFFormBody(keys.toList(), values.toList())
        }
    }
}