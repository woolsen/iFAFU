package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.Information
import retrofit2.http.*

interface ExamineService {

    @GET("/information/examine/query")
    suspend fun query(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): IFResponse<List<Information>>

    @GET("/information/examine/query")
    suspend fun queryByStatus(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("status") status: Int
    ): IFResponse<List<Information>>

    @POST("/information/examine/change")
    @FormUrlEncoded
    suspend fun examine(
        @Field("id") id: Long,
        @Field("status") status: Int
    ): IFResponse<Boolean>

}