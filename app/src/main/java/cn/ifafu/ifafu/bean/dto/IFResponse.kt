package cn.ifafu.ifafu.bean.dto

import androidx.annotation.IntDef
import androidx.annotation.Keep

@Keep
data class IFResponse<out T>(
    @HttpCode
    val code: Int,
    val message: String,
    val data: T?
) {

    /**
     * 获取返回的数据，并且数据不能为空
     */
    inline fun peekData(peek: (T) -> Unit) {
        if (data != null) {
            peek(data)
        }
    }

    fun isSuccess(): Boolean = code == SUCCESS

    companion object {

        const val SUCCESS = 200 //成功
        const val FAILURE = 400 //失败
        const val NO_AUTH = 401 //未登录
        const val NOT_FOUND = 404 //资源未找到

        fun <T> success(data: T, message: String = ""): IFResponse<T> {
            return IFResponse(SUCCESS, message, data)
        }

        fun failure(code: Int, message: String): IFResponse<Nothing> {
            return IFResponse(code, message, null)
        }

        fun failure(message: String): IFResponse<Nothing> {
            return IFResponse(FAILURE, message, null)
        }

        fun <T> noAuth(): IFResponse<T> {
            return IFResponse(NO_AUTH, "未登录", null)
        }

        fun <T> create(@HttpCode code: Int, message: String = "", data: T? = null): IFResponse<T> {
            return IFResponse(code, message, data)
        }
    }

    @IntDef(value = [SUCCESS, FAILURE, NO_AUTH, NOT_FOUND])
    annotation class HttpCode
}