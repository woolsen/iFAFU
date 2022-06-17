package cn.ifafu.ifafu.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @author KQiang Weng
 */


fun Map<String, String>.toZFRequestBody(): RequestBody {
    val body = this.entries
        .joinToString(separator = "&") {
            it.key + "=" + it.value.encode("gb2312")
        }
    return body.toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
}
