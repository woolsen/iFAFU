package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.bo.CourseBO
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.TermWrap
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * @author KQiang Weng
 * @since 2021/12/23 11:00
 */
interface HtmlParseService {

    @POST("/html/parse/course")
    @FormUrlEncoded
    suspend fun parseCourseHtml(
        @Field("html") html: String,
    ): IFResponse<TermWrap<List<CourseBO>>>

}