package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.LoginDTO
import cn.ifafu.ifafu.bean.dto.RegisterDTO
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.db.dao.UserInfoDao
import cn.ifafu.ifafu.entity.UserInfo
import cn.ifafu.ifafu.service.IFAFUUserService
import cn.ifafu.ifafu.util.IFTokenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IfUserRepository @Inject constructor(
    private val userInfoDao: UserInfoDao,
    private val service: IFAFUUserService,
    private val userRepository: UserRepository
) {

    fun userInfo(): Flow<UserInfo?> = flow {
        val cache = userInfoDao.findUserFirst()
        emit(cache)
        val resp = service.userInfo()
        when (resp.code) {
            IFResponse.SUCCESS -> {
                val data = resp.data!!
                userInfoDao.saveInfo(data)
                emit(data)
            }
            else -> {
                userInfoDao.clearAll()
                emit(null)
            }
        }
    }

    suspend fun register(
        phone: String,
        password: String,
        code: String
    ): Resource<String> = withContext(Dispatchers.IO) {
        val sno = userRepository.getUser()?.account ?: ""
        val dto = RegisterDTO(phone, password, code, sno)
        val resp = service.register(dto)
        if (resp.code == IFResponse.SUCCESS) {
            Resource.Success("注册成功")
        } else {
            Resource.Failure(resp.message)
        }
    }

    suspend fun login(account: String, password: String): Resource<String> =
        withContext(Dispatchers.IO) {
            try {
                val dto = LoginDTO(account, password)
                val resp = service.login(dto)
                if (resp.code == IFResponse.SUCCESS) {
                    val data = resp.data
                    require(data != null) { "返回信息为空" }
                    IFTokenUtils.saveToken(data.token)
                    userInfoDao.saveInfo(data.info)
                    Resource.Success("登录成功")
                } else {
                    Resource.Failure(resp.message)
                }
            } catch (e: Exception) {
                Resource.Failure(e, "登录失败")
            }
        }

    fun sendCode(email: String) = flow {
        val resp = service.sendCode(email)
        if (resp.isSuccess()) {
            emit(Unit)
        } else {
            throw IllegalArgumentException(resp.message)
        }
    }

    fun logout(): Flow<Unit> = flow {
        try {
            userInfoDao.clearAll()
            IFTokenUtils.removeToken()
            service.logout()
            emit(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}