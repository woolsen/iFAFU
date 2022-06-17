package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.bean.dto.Version
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.bean.vo.Weather
import cn.ifafu.ifafu.exception.IFResponseFailureException
import cn.ifafu.ifafu.service.IFAFUService
import cn.ifafu.ifafu.service.WeatherService
import com.blankj.utilcode.util.SPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OtherRepository @Inject constructor(
    private val ifafuService: IFAFUService,
    private val weatherService: WeatherService,
) {

    fun getWeather(code: String): Flow<Weather> = flow {
        val resp = weatherService.getWeather(code)
        if (resp.isSuccess()) {
            emit(resp.data ?: throw IFResponseFailureException("获取天气出错"))
        } else {
            throw IFResponseFailureException(resp.message)
        }
    }

    suspend fun getNewVersion(): Resource<Version> = withContext(Dispatchers.IO) {
        try {
            Resource.create(ifafuService.getVersion())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Resource.Failure("检查版本更新失败")
        }
    }

    suspend fun once(versionCode: Int, versionName: String, systemVersion: Int) =
        withContext(Dispatchers.IO) {
            try {
                val account = SPUtils.getInstance(Constants.SP_USER_INFO).getString("account")
                val resp = ifafuService.once(account, versionCode, versionName, systemVersion)
                Timber.d("once: $resp")
                return@withContext
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}