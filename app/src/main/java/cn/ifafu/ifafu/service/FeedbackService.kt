package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.dto.Feedback
import cn.ifafu.ifafu.bean.dto.IFResponse
import retrofit2.http.*

interface FeedbackService {

    @GET("/v2/feedback/query")
    suspend fun query(@Query("sno") sno: String): IFResponse<List<Feedback>>

    @POST("/v2/feedback/upload/android")
    @FormUrlEncoded
    suspend fun feedback(
        @Field("sno") sno: String,
        @Field("contact") contact: String,
        @Field("content") content: String,
        @Field("version") version: String,
        @Field("sdkName") sdkName: String,
        @Field("sdkVersion") sdkVersion: String
    ): IFResponse<Boolean>
}