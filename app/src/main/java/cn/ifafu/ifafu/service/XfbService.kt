package cn.ifafu.ifafu.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface XfbService {
    /**
     * 初始化Cookie
     */
    @GET("/Phone/Login")
    fun initCookie(
        @Query("sourcetype") sourcetype: Int =0,
    ): Call<ResponseBody?>

    @GET("/Phone/GetValidateCode")
    fun verify(
        @Query("time") time: Long
    ): Call<ResponseBody?>

    @POST("/Phone/Login")
    @FormUrlEncoded
    fun login(
        @Header("Referer") referer: String?,
        @Field("sno") sno: String?,
        @Field("pwd") pwd: String?,
        @Field("yzm") yzm: String?,
        @Field("remember") remember: String?,
        @Field("uclass") uclass: String?,
        @Field("zqcode") zqcode: String?,
        @Field("json") json: String?
    ): Call<ResponseBody?>

//    /**
//     * 初始化Cookie
//     */
//    @POST("/NoBase/TPGetAppList")
//    @FormUrlEncoded
//    fun default2(
//            @Field("sourcetype") sourcetype: String?
//    ): Call<ResponseBody?>

    /**
     * 获取交电费页面，更新Cookie
     */
    @POST("/Page/Page")
    @FormUrlEncoded
    fun page(
        @Field("flowid") flowid: String?,
        @Field("type") type: String?,
        @Field("apptype") apptype: String?,
        @Field("url") url: String?,
        @Field("EMenuName") EMenuName: String?,
        @Field("MenuName") MenuName: String?,
        @Field("sourcetype") sourcetype: String?,
        @Field("IMEI") IMEI: String?,
        @Field("language") language: String?,
        @Field("comeapp") comeapp: String?
    ): Call<ResponseBody?>

    /**
     * 查询余额
     */
    @POST("/User/GetCardInfoByAccountNoParm")
    @FormUrlEncoded
    fun queryBalance(
        @Field("json") json: String?
    ): Call<ResponseBody?>

    /**
     * 查询电费信息
     */
    @POST("/Tsm/TsmCommon")
    @FormUrlEncoded
    fun query(
        @Field("jsondata") jsondata: String?,
        @Field("funname") funname: String?,
        @Field("json") json: String?
    ): Call<ResponseBody?>

    /**
     * 查询接口
     */
    @POST("/Tsm/TsmCommon")
    @FormUrlEncoded
    fun query(
        @FieldMap map: Map<String, String>
    ): Call<ResponseBody?>

//    /**
//     * 充值电费
//     */
//    @POST("/Tsm/Elec_Pay")
//    @FormUrlEncoded
//    fun elecPay(
//            @Header("Referer") referer: String,
//            @Field("acctype") acceptype: String,
//            @Field("paytype") paytype: String,
//            @Field("aid") aid: String,
//            @Field("account") account: String,
//            @Field("tran") tran: String,
//            @Field("roomid") roomid: String,
//            @Field("room") room: String,
//            @Field("floorid") floorid: String,
//            @Field("floor") floor: String,
//            @Field("buildingid") buildingid: String,
//            @Field("building") building: String,
//            @Field("areaid") areaid: String,
//            @Field("areaname") areaname: String,
//            @Field("json") json: String
//    ): Call<ResponseBody?>
}