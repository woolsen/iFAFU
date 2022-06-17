package cn.ifafu.ifafu.domain.user

import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.domain.CoroutineUseCase
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repository: UserRepository,
) : CoroutineUseCase<ChangePasswordUseCase.Params, User>(Dispatchers.IO) {

    override suspend fun execute(parameters: Params): User {
        val newPassword = parameters.newPassword
        val user = parameters.user
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
        return repository.changePassword(parameters.user, parameters.newPassword)
    }

    data class Params(
        val user: User,
        val newPassword: String
    )
}