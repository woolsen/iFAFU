package cn.ifafu.ifafu.util

import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object OkHttpUtils {

    const val IMAGE_GIF_VALUE = "image/gif"
    const val IMAGE_JPEG_VALUE = "image/jpeg"
    const val IMAGE_PNG_VALUE = "image/png"

    @JvmStatic
    fun File.toRequestBody(): RequestBody {
        val fileName = name
        val mediaType = when {
            fileName.endsWith(".png") -> {
                IMAGE_PNG_VALUE.toMediaType()
            }
            fileName.matches(".*\\.(jpg|png|jpe|jpeg)".toRegex()) -> {
                IMAGE_JPEG_VALUE.toMediaType()
            }
            fileName.endsWith(".gif") -> {
                IMAGE_GIF_VALUE.toMediaType()
            }
            else -> {
                throw IllegalArgumentException("不支持该格式图片")
            }
        }
        return this.asRequestBody(mediaType)
    }


}