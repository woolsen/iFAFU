package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.Version
import cn.ifafu.ifafu.entity.Holiday
import cn.ifafu.ifafu.entity.FirstWeek
import retrofit2.http.*

interface IFAFUService {

    @GET("/ifafu/version")
    suspend fun getVersion(): IFResponse<Version>

    @POST("/count/once")
    @FormUrlEncoded
    suspend fun once(
        @Field("sno") sno: String,
        @Field("versionCode") versionCode: Int,
        @Field("versionName") versionName: String,
        @Field("systemVersion") systemVersion: Int
    ): IFResponse<Boolean>

    @GET("/public/holiday")
    suspend fun holiday(): List<Holiday>

    @GET("/public/firstWeeks")
    suspend fun firstWeeks(): List<FirstWeek>
}