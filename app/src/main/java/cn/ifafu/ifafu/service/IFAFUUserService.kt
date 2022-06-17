package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.dto.LoginDTO
import cn.ifafu.ifafu.bean.dto.LoginResultDTO
import cn.ifafu.ifafu.bean.dto.RegisterDTO
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.UserInfo
import retrofit2.http.*

interface IFAFUUserService {

    @POST("/v2/user/login")
    suspend fun login(
        @Body dto: LoginDTO
    ): IFResponse<LoginResultDTO>

    @GET("/v2/user/logout")
    suspend fun logout(): IFResponse<Boolean>

    @GET("/v2/user/userinfo")
    suspend fun userInfo(): IFResponse<UserInfo>

    @POST("/v2/user/register/phone")
    suspend fun register(
        @Body dto: RegisterDTO
    ): IFResponse<Boolean>

    @GET("/capture/phone")
    suspend fun sendCode(
        @Query(value = "phone") phone: String
    ): IFResponse<Boolean>
}