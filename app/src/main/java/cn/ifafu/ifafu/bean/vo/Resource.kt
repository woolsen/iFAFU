package cn.ifafu.ifafu.bean.vo

import androidx.lifecycle.Observer
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.constant.ResultCode
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

/**
 * ViewModel <-> View
 */
sealed class Resource<out T> {

    private var onSuccess: ((Success<T>) -> Unit)? = null
    private var onFailure: ((Failure) -> Unit)? = null
    private var onLoading: ((Loading) -> Unit)? = null

    fun onSuccess(onSuccess: (Success<T>) -> Unit): Resource<T> {
        if (this is Success) {
            onSuccess(this)
        }
        return this
    }

    fun onFailure(onFailure: (Failure) -> Unit): Resource<T> {
        if (this is Failure) {
            onFailure(this)
        }
        return this
    }

    fun onLoading(onLoading: (Loading) -> Unit): Resource<T> {
        if (this is Loading) {
            onLoading(this)
        }
        return this
    }

    fun handle(handle: Resource<T>.() -> Unit) {
        this.handle()
    }

    /**
     * message获取一次后将变为空
     * 避免设置LiveData监听时，页面重置导致反复提示信息
     * 例如1：当Activity/Fragment销毁时，ViewModel并未销毁。当横竖屏切换时，消息会重复提示
     * 例如2：某些情况下存在多个fragments使用同一个ViewModel。当进行消息通知时，
     *       所有的界面可能都会进行消息的处理
     */
    private var hasBeenHandled = false

    fun handleMessage(handle: (message: String) -> Unit) {
        if (!hasBeenHandled) {
            val message = if (this is Success) {
                this.message
            } else if (this is Failure) {
                this.message
            } else {
                return
            }
            if (message.isNotBlank()) {
                handle(message)
            }
            hasBeenHandled = true
        }
    }

    class Success<out T>(val data: T, val message: String = "") : Resource<T>()

    class Failure(
        val code: Int,
        val message: String,
    ) : Resource<Nothing>() {

        constructor(message: String) : this(ResultCode.FAILURE, message)

        constructor(exception: Throwable, otherMessage: String = "") : this(
            ResultCode.FAILURE,
            when (exception) {
                is SocketTimeoutException -> "网络连接超时"
                is TimeoutException -> "网络连接超时"
                is IOException -> "网络连接异常"
                else -> {
                    exception.printStackTrace()
                    if (otherMessage.isEmpty()) {
                        "未知错误"
                    } else {
                        otherMessage
                    }
                }
            }
        )

        constructor(exception: Throwable) : this(
            ResultCode.FAILURE,
            when (exception) {
                is SocketTimeoutException -> "网络连接超时"
                is TimeoutException -> "网络连接超时"
                is IOException -> "网络连接异常"
                is cn.ifafu.ifafu.exception.Failure -> exception.message
                else -> {
                    exception.printStackTrace()
                    "未知错误"
                }
            }
        )
    }

    class Loading(val message: String = "") : Resource<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success -> "Success[data: ${data}, message: ${message}]"
            is Failure -> "Error[message: ${message}]"
            is Loading -> "Loading"
        }
    }

    companion object {

        fun <T> create(response: IFResponse<T>): Resource<T> {
            return if (response.isSuccess() && response.data != null) {
                Success(response.data)
            } else if (response.message.isNotBlank()) {
                Failure(response.code, response.message)
            } else {
                when (response.code) {
                    IFResponse.SUCCESS -> Failure(response.code, "返回数据为空")
                    IFResponse.FAILURE -> Failure(response.code, response.message)
                    IFResponse.NO_AUTH -> Failure(response.code, "登录态失效")
                    IFResponse.NOT_FOUND -> Failure(response.code, "资源未找到")
                    else -> Failure(response.code, "失败")
                }
            }
        }


        fun failure(message: String): Failure {
            return Failure(message)
        }

        inline fun <T, R> transformWithNull(
            response: IFResponse<T>,
            crossinline ifSuccessDataNotNull: (T) -> Resource<R>,
            crossinline ifSuccessDataNull: IFResponse<T>.() -> Resource<R>,
        ): Resource<R> {
            return if (response.isSuccess() && response.data != null) {
                ifSuccessDataNotNull(response.data)
            } else if (response.message.isNotBlank()) {
                Failure(response.message)
            } else {
                when (response.code) {
                    IFResponse.SUCCESS -> ifSuccessDataNull(response)
                    IFResponse.FAILURE -> Failure(response.message)
                    IFResponse.NO_AUTH -> Failure("登录态失效")
                    IFResponse.NOT_FOUND -> Failure("资源未找到")
                    else -> Failure("失败")
                }
            }
        }

        suspend inline fun <T, R> transformWithNull(
            crossinline respBlock: suspend () -> IFResponse<T>,
            crossinline ifSuccessDataNotNull: (T) -> Resource<R>,
            crossinline ifSuccessDataNull: IFResponse<T>.() -> Resource<R>,
        ): Resource<R> {
            return try {
                val resp = respBlock()
                transformWithNull(resp, ifSuccessDataNotNull, ifSuccessDataNull)
            } catch (e: Exception) {
                e.printStackTrace()
                Failure(e)
            }
        }
    }
}

class ResourceObserve<T>(onCreate: ResourceObserve<T>.() -> Unit) : Observer<Resource<T>> {

    private var onSuccess: ((Resource.Success<T>) -> Unit)? = null
    private var onFailure: ((Resource.Failure) -> Unit)? = null
    private var onLoading: ((Resource.Loading) -> Unit)? = null

    fun onSuccess(onSuccess: (Resource.Success<T>) -> Unit) {
        this.onSuccess = onSuccess
    }

    fun onFailure(onFailure: (Resource.Failure) -> Unit) {
        this.onFailure = onFailure
    }

    fun onLoading(onLoading: (Resource.Loading) -> Unit) {
        this.onLoading = onLoading
    }

    init {
        this.onCreate()
    }

    override fun onChanged(t: Resource<T>) {
        when (t) {
            is Resource.Success -> onSuccess?.invoke(t)
            is Resource.Failure -> onFailure?.invoke(t)
            is Resource.Loading -> onLoading?.invoke(t)
        }
    }
}
