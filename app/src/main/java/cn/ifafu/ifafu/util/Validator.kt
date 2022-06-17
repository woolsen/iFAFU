package cn.ifafu.ifafu.util

import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.exception.Failure

/**
 * @author KQiang Weng
 */
class Validator {

    @Throws(Failure::class)
    fun checkNewPassword(user: User, newPassword: String) {
        when {
            newPassword == user.password -> {
                throw Failure("密码不能和原密码相同")
            }
            newPassword == user.account -> {
                throw Failure("密码不能和账号相同")
            }
            newPassword.length < 6 -> {
                throw Failure("密码长度不能小于6位")
            }
            newPassword.length > 16 -> {
                throw Failure("密码长度不能超过16位")
            }
            newPassword.matches("[0-9]+".toRegex()) -> {
                throw Failure("密码不能为纯数字")
            }
            newPassword.matches("[a-zA-Z]+".toRegex()) -> {
                throw Failure("密码不能为纯字母")
            }
        }
    }

}