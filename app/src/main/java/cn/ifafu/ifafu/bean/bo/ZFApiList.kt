package cn.ifafu.ifafu.bean.bo

import cn.ifafu.ifafu.entity.User
import java.net.URLEncoder


class ZFApiList(
    val schoolCode: String,
    private val baseUrl: String,
    private val login: String,
    private val verify: String,
    private val main: String,
    private val apiEnumMap: Map<ZFApiEnum, Pair<String, String>>
) {

    private fun getBaseUrl(user: User): String {
        return baseUrl.replace("{token}", user.token)
    }

    fun get(filed: ZFApiEnum, user: User): String {
        val baseUrl = getBaseUrl(user)
        if (filed == ZFApiEnum.COMMENT && user.school == User.FAFU_JS) {
            return baseUrl + main + "?xh=" + user.account
        }
        return when (filed) {
            ZFApiEnum.BASE -> baseUrl
            ZFApiEnum.VERIFY -> "$baseUrl$verify"
            ZFApiEnum.LOGIN -> "$baseUrl$login"
            ZFApiEnum.MAIN -> "$baseUrl$main?xh=${user.account}"
            else -> {
                val api = apiEnumMap.getOrElse(filed) {
                    throw IllegalArgumentException("url is not found")
                }
                val xm = URLEncoder.encode(user.name, "GBK")
                if (api.second.isEmpty()) {
                    "$baseUrl${api.first}?xh=${user.account}&xm=$xm"
                } else {
                    "$baseUrl${api.first}?xh=${user.account}&xm=$xm&gnmkdm=${api.second}"
                }
            }
        }
    }

}