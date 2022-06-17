package cn.ifafu.ifafu.service.common

import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.bo.ZFApiList
import cn.ifafu.ifafu.entity.User

/**
 * 正方教务管理系统网址提供器
 */
object ZfUrlProvider {

    private val URL_MAP: Map<String, ZFApiList> = mapOf(
        User.FAFU to ZFApiList(
            User.FAFU, "http://jwgl.fafu.edu.cn/{token}/",
            "default2.aspx",
            "CheckCode.aspx",
            "xs_main.aspx",
            mapOf(
                ZFApiEnum.TIMETABLE to Pair("xskbcx.aspx", "N121602"),
                ZFApiEnum.EXAM to Pair("xskscx.aspx", "N121604"),
                ZFApiEnum.SCORE to Pair("xscjcx_dq_fafu.aspx", "N121605"),
                ZFApiEnum.COMMENT to Pair("xsjxpj.aspx", "N121401"),
                ZFApiEnum.ELECTIVES to Pair("pyjh.aspx", "N121607"),
                ZFApiEnum.PROFESSIONAL_TIMETABLE to Pair("tjkbcx.aspx", "N121601"),
                ZFApiEnum.CHANGE_PASSWORD to Pair("mmxg.aspx", "N121502")
            )
        ),
        User.FAFU_JS to ZFApiList(
            User.FAFU_JS, "http://js.ifafu.cn/",
            "default.aspx",
            "CheckCode.aspx",
            "xs_main.aspx",
            mapOf(
                ZFApiEnum.TIMETABLE to Pair("xskbcx.aspx", "N121602"),
                ZFApiEnum.EXAM to Pair("xskscx.aspx", "N121603"),
                ZFApiEnum.SCORE to Pair("xscjcx_dq.aspx", "N121613"),
                ZFApiEnum.COMMENT to Pair("xsjxpj.aspx", "N121401"),
                ZFApiEnum.ELECTIVES to Pair("pyjh.aspx", "N121606"),
                ZFApiEnum.PROFESSIONAL_TIMETABLE to Pair("tjkbcx.aspx", "N121601"),
                ZFApiEnum.CHANGE_PASSWORD to Pair("mmxg.aspx", "")
            )
        )
    )


    /**
     * 获取对应Url
     *
     * @param filed [ZFApiEnum]
     * @param user  user
     * @return url
     */
    fun getUrl(filed: ZFApiEnum, user: User): String {
        val url = URL_MAP.getOrElse(user.school) {
            throw IllegalAccessException("未知学校代码")
        }
        return url.get(filed, user)
    }

}