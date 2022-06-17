package cn.ifafu.ifafu.util

import com.blankj.utilcode.util.SPUtils

/**
 * 获取和保存iFAFU平台返回的Token信息
 */
object IFTokenUtils {

    private const val SP_NAME = "IFAFU"
    private const val TOKEN_KEY = "Access-Token"

    fun getToken(): String {
        return SPUtils.getInstance(SP_NAME).getString(TOKEN_KEY)
    }

    fun saveToken(token: String) {
        SPUtils.getInstance(SP_NAME).put(TOKEN_KEY, token)
    }

    fun removeToken() {
        SPUtils.getInstance(SP_NAME).remove(TOKEN_KEY)
    }

}