package cn.ifafu.ifafu.exception

import cn.ifafu.ifafu.bean.dto.IFResponse
import java.io.IOException

/**
 * 当服务器返回数据中 [IFResponse.code]!=200 时，将返回的错误信息[IFResponse.message]附带在异常中抛出
 * Repository层与ViewModel层通过抛出异常来传递错误信息
 */
class IFResponseFailureException(override val message: String) : IOException()