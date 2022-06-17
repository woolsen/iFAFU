package cn.ifafu.ifafu.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.service.UserService
import cn.ifafu.ifafu.util.TransformUtils.toResource
import com.blankj.utilcode.util.SPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userService: UserService,
    jiaowuDatabase: JiaowuDatabase,
) : AbstractJwRepository(jiaowuDatabase.userDao) {

    suspend fun login(
        account: String,
        password: String,
        school: String,
        token: String,
    ): Resource<User> {
        return withContext(Dispatchers.IO) {
            val user = User(
                account = account,
                password = password,
                school = school,
                token = token
            )
            login(user)
        }
    }

    suspend fun changePassword(user: User, newPassword: String): User {
        val resp = userService.changePassword(user, newPassword)
        when (resp.code) {
            IFResponse.SUCCESS -> {
                user.password = newPassword
                userDao.save(user)
                return user
            }
            else -> {
                throw Failure(resp.message)
            }
        }
    }

    suspend fun getUser(): User? {
        return userDao.getUsingUser()
    }

    suspend fun checkTo(user: User): Resource<User> = withContext(Dispatchers.IO) {
        userDao.saveUsing(user)
        login(user)
    }

    suspend fun login(user: User): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val loginResponse = userService.login(user)
            Timber.d(loginResponse.toString())
            loginResponse.peekData { data ->
                userDao.save(data)
                SPUtils.getInstance(Constants.SP_USER_INFO)
                    .put("account", data.account, true)
            }
            return@withContext loginResponse.toResource()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Resource.Failure(e)
        }
    }

    suspend fun deleteUserByAccount(account: String) = withContext(Dispatchers.IO) {
        userDao.deleteAllData(account)
        val users = userDao.getAllUsers()
        if (users.isEmpty()) {
            SPUtils.getInstance(Constants.SP_USER_INFO).remove("account")
        } else {
            SPUtils.getInstance(Constants.SP_USER_INFO).put("account", users[0].account)
        }
    }

    fun getAllUsersLiveData(): LiveData<List<User>> = liveData(Dispatchers.Main) {
        emit(userDao.getAllUsers())
    }

    suspend fun saveUsers(users: List<User>) {
        userDao.save(*users.toTypedArray())
    }
}