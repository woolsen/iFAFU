package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.vo.Weather
import cn.ifafu.ifafu.di.Ifafu
import com.alibaba.fastjson.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class WeatherService @Inject constructor(
    @Ifafu private val client: OkHttpClient,
) {

    /**
     * 获取天气
     *
     * @param code 城市代码 福州：101230101
     *
     * @return [Weather]
     */
    fun getWeather(code: String): IFResponse<Weather> {
        val weather = Weather()
        val referer = "http://www.weather.com.cn/weather1d/$code.shtml"

        // 获取城市名和当前温度
        val url1 = "http://d1.weather.com.cn/sk_2d/$code.html"
        val request = Request.Builder()
            .get()
            .header("Referer", referer)
            .url(url1)
            .build()
        val body1 = client.newCall(request).execute().body
        var jsonStr1: String = body1!!.string()
        jsonStr1 = jsonStr1.replace("var dataSK = ", "")
        val jo1 = JSONObject.parseObject(jsonStr1)
        weather.cityName = jo1.getString("cityname")
        weather.nowTemp = jo1.getInteger("temp")
        weather.weather = jo1.getString("weather")

        // 获取白天温度和晚上温度
        val url2 = "http://d1.weather.com.cn/dingzhi/$code.html"
        val request2 = Request.Builder()
            .get()
            .header("Referer", referer)
            .url(url2)
            .build()
        val body2 = client.newCall(request2).execute().body
        var jsonStr2: String = body2!!.string()
        jsonStr2 = jsonStr2.substring(jsonStr2.indexOf('=') + 1, jsonStr2.indexOf(";"))
        var jo2: JSONObject = JSONObject.parseObject(jsonStr2)
        jo2 = jo2.getJSONObject("weatherinfo")
        weather.amTemp = Integer.valueOf(jo2.getString("temp").replace("℃", ""))
        weather.pmTemp = Integer.valueOf(jo2.getString("tempn").replace("℃", ""))
        return IFResponse.success(weather)
    }

}