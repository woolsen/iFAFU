package cn.ifafu.ifafu.util

import cn.ifafu.ifafu.constant.Constants
import com.blankj.utilcode.util.SPUtils

object DataUtils {
    fun getSno(): String {
        return SPUtils.getInstance(Constants.SP_USER_INFO)
            .getString("account")
    }
}