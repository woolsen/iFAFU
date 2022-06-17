package cn.ifafu.ifafu.util

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.vo.Resource

object TransformUtils {

    fun <T> IFResponse<T>.toResource(): Resource<T> {
        return Resource.create(this)
    }

}