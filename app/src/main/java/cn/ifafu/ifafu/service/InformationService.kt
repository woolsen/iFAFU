package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.Information
import okhttp3.MultipartBody
import retrofit2.http.*

interface InformationService {

    @GET("/v2/information/query")
    suspend fun query(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): IFResponse<List<Information>>

    @GET("/v2/information/query/my")
    suspend fun queryMy(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): IFResponse<List<Information>>

    @POST("/v2/information/delete")
    suspend fun delete(@Query("id") id: Long): IFResponse<Boolean>

    @POST("/v3/information/upload")
    @Multipart
    suspend fun upload(
        @Part("content", encoding = "UTF-8") content: String,
        @Part("contact", encoding = "UTF-8") contact: String,
        @Part("contactType") contactType: Int,
        @Part("category") category: Long,
        @Part images: List<MultipartBody.Part>
    ): IFResponse<Information>

    @POST("/v2/information/edit")
    @Multipart
    suspend fun edit(
        @Part("id") id: Long,
        @Part("content") content: String,
        @Part("contact") contact: String,
        @Part("contactType") contactType: Int,
        @Part("category") category: Long
    ): IFResponse<Boolean>

}