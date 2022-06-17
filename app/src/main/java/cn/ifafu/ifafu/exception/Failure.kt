package cn.ifafu.ifafu.exception

import cn.ifafu.ifafu.constant.ResultCode

class Failure(
    val errorCode: Int,
    val errorMessage: String,
    throwable: Throwable? = null,
) : Exception(errorMessage, throwable) {

    constructor(errorMessage: String) : this(ResultCode.FAILURE, errorMessage)

    override val message: String
        get() = errorMessage
}