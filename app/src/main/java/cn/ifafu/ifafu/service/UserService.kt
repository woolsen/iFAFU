package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.service.parser.ChangePasswordParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val zfClient: ZFHttpClient,
) {

    fun login(user: User): IFResponse<User> {
        return zfClient.login(user)
    }

    suspend fun changePassword(user: User, newPassword: String): IFResponse<Unit> {
        val url = ZfUrlProvider.getUrl(ZFApiEnum.CHANGE_PASSWORD, user)
        return zfClient.ensureLogin(user) {
            val ss = zfClient.needParams(url, referer = null, checkLogin = true) { _, p ->
                val params = p.toMutableMap()
                params["Button1"] = "修  改" //必须参数
                params["TextBox2"] = user.password //原密码
                params["TextBox3"] = newPassword    //新密码
                params["TextBox4"] = newPassword    //确认密码
                val html = zfClient.post(url, params).body?.string() ?: ""
                ChangePasswordParser().parse(html)
            }
            ss
        }
    }
}