package cn.ifafu.ifafu.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import cn.ifafu.ifafu.annotation.ElectricityFeeUnit
import cn.ifafu.ifafu.bean.bo.Dorm
import cn.ifafu.ifafu.bean.bo.ElecSelection
import cn.ifafu.ifafu.bean.bo.ElectricityFee
import cn.ifafu.ifafu.bean.bo.Options
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.db.dao.ElecCookieDao
import cn.ifafu.ifafu.db.dao.ElecQueryDao
import cn.ifafu.ifafu.db.dao.ElecUserDao
import cn.ifafu.ifafu.entity.ElecCookie
import cn.ifafu.ifafu.entity.ElecQuery
import cn.ifafu.ifafu.entity.ElecUser
import cn.ifafu.ifafu.entity.ElectricityHistory
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.service.XfbService
import cn.ifafu.ifafu.util.DateUtils
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.SPUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.RegExp
import timber.log.Timber
import java.net.URLEncoder
import java.util.regex.Pattern
import javax.inject.Inject

class XfbRepository @Inject constructor(
    private val xfbService: XfbService,
    database: JiaowuDatabase,
    @ApplicationContext private val context: Context,
) : AbstractJwRepository(database.userDao) {

    private val mQueryDao: ElecQueryDao = database.elecQueryDao
    private val mUserDao: ElecUserDao = database.elecUserDao
    private val mCookieDao: ElecCookieDao = database.elecCookieDao
    private val electricityHistoryDao = database.electricityHistoryDao

    fun queryCardBalance(): LiveData<Resource<Double>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            val responseBody = xfbService.queryBalance("true").execute().body()
            val msg = JSONObject.parseObject(responseBody!!.string())
                .getJSONObject("Msg")
                .getJSONObject("query_card")
                .getJSONArray("card")
                .getJSONObject(0)
            val value = (msg.getIntValue("db_balance") + msg.getIntValue("unsettle_amount")) / 100.0
            emit(Resource.Success(value))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Failure(e, "校园卡余额获取出错"))
        }
    }

    suspend fun checkLoginStatus(): Boolean = withContext(Dispatchers.IO) {
        try {
            val elecCookie = getElecCookie()
            //            initCookie()
            val html = xfbService.page(
                "31", "3", "2", "", "electricity",
                URLEncoder.encode("交电费", "gbk"),
                elecCookie["sourcetypeticket"],
                SPUtils.getInstance(Constants.SP_ELEC).getString("IMEI"),
                "0", "1"
            ).execute().body()!!.string()
            !(html.contains("<title>登录</title>") || html.contains("建设中~~~"))
        } catch (e: Exception) {
            Timber.d(e)
            true
        }
    }

    //    override suspend fun isInLogin(): Boolean = withContext(Dispatchers.IO) {
    //        try {
    //            val elecCookie = getElecCookie()
    //            elecCookieInit()
    //            val html = xfbService.page(
    //                "31", "3", "2", "", "electricity",
    //                URLEncoder.encode("交电费", "gbk"),
    //                elecCookie["sourcetypeticket"],
    //                SPUtils.getInstance(Constants.SP_ELEC).getString("IMEI"),
    //                "0", "1"
    //            ).execute().body()!!.string()
    //            !(html.contains("<title>登录</title>") || html.contains("建设中~~~"))
    //        } catch (e: Exception) {
    //            e.printStackTrace()
    //            false
    //        }
    //    }

    suspend fun getLastQueryDorm(): Dorm? = withContext(Dispatchers.IO) {
        val sp = SPUtils.getInstance(getUsingAccount())
        val id = sp.getInt("electricity_id", -1)
        val numberOfDorm = sp.getString("electricity_dorm")
        if (id == -1 || numberOfDorm.isEmpty()) {
            null
        } else {
            Dorm(id, numberOfDorm)
        }
    }

    suspend fun queryElectricityBalance(dorm: Dorm): ElectricityFee {
        val user = getElecUser()
        val selection = getSelectionList().find { it.id == dorm.id }!!
        val query = ElecQuery()
        query.aid = selection.aid
        query.areaId = selection.areaId
        query.area = selection.area
        query.building = selection.building
        query.buildingId = selection.buildingId
        query.floor = selection.floor
        query.floorId = selection.floorId
        query.room = dorm.dormNumber
        query.xfbId = user.xfbId
        val responseBody = xfbService
            .query(query.toFiledMap(ElecQuery.Query.ROOMINFO))
            .execute().body()
        val json = responseBody!!.string()
        val response = IFResponse.success(
            data = JSONObject.parseObject(json)
                .getJSONObject("Msg")
                .getJSONObject("query_elec_roominfo")
                .getString("errmsg")
        )
        val data = response.data

        val account = getUsingAccount()
            ?: return ElectricityFee(0.0, ElectricityFeeUnit.MONEY)
        val sp = SPUtils.getInstance(account)
        // 不论此次获取电费是否成功，都保存此次选项记录，下次直接使用此记录获取新电费信息
        val dormStr = selection.buildingAlias + " " + dorm.dormNumber
        sp.put("default_electricity_dorm", dormStr)
        sp.put("electricity_id", dorm.id)
        sp.put("electricity_dorm", dorm.dormNumber)
        saveElecQuery(query)

        if (response.code == IFResponse.SUCCESS && data != null) {
            //解析电费字符串
            val string = response.data
            if (string.contains("无法获取房间信息|查询数据库失败|注销".toRegex())) {
                throw Failure(string)
            }
            val parsers = listOf<String.() -> ElectricityFee?>(
                { get("电费-?[0-9]*(.[0-9]*)?", 2, ElectricityFeeUnit.MONEY) },
                { get("电量-?[0-9]*(.[0-9]*)?", 2, ElectricityFeeUnit.POWER) },
                { get("剩余金额:-?[0-9]*(.[0-9]*)?", 5, ElectricityFeeUnit.MONEY) },
                { get("剩余电费:-?[0-9]*(.[0-9]*)?", 5, ElectricityFeeUnit.MONEY) },
                { get("剩余电量:-?[0-9]*(.[0-9]*)?", 5, ElectricityFeeUnit.POWER) },
            )
            for (parser in parsers) {
                val result = parser.invoke(string)
                if (result != null) {

                    //保存电费历史记录
                    val first = getElectricityDefaultHistory().firstOrNull()
                    val current = System.currentTimeMillis()
                    if (DateUtils.isSameDay(current, first?.timestamp ?: 0)
                        && result.balance == first?.balance
                    ) {
                        electricityHistoryDao.delete(first)
                    }
                    val history = ElectricityHistory(
                        dormStr,
                        System.currentTimeMillis(),
                        result.balance,
                        result.unit
                    )
                    electricityHistoryDao.save(history)

                    return result
                }
            }

            throw Failure("查询出错")
        } else if (!checkLoginStatus()) {
            throw Failure("登录态失效，请重新登录")
        } else {
            throw Failure("查询出错")
        }
    }

    private fun String.get(
        @RegExp regex: String,
        startIndex: Int,
        @ElectricityFeeUnit type: Int,
    ): ElectricityFee? {
        val matcher = Pattern.compile(regex).matcher(this)
        return if (matcher.find()) {
            try {
                ElectricityFee(matcher.group().substring(startIndex = startIndex).toDouble(), type)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    suspend fun getSelectionList(): List<ElecSelection> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("room_info.json").reader().readText()
            JSONArray.parseArray(jsonString).map {
                val jo = JSONObject.parseObject(it.toString())
                ElecSelection(
                    id = jo["id"]!!.toString().toInt(),
                    aid = jo["aid"]!!.toString(),
                    name = jo["name"]!!.toString(),
                    areaId = jo["areaId"]!!.toString(),
                    area = jo["area"]!!.toString(),
                    buildingId = jo["buildingId"]!!.toString(),
                    building = jo["building"]!!.toString(),
                    floorId = jo["floorId"]!!.toString(),
                    floor = jo["floor"]!!.toString(),
                    areaAlias = jo["areaAlias"]!!.toString(),
                    buildingAlias = jo["buildingAlias"]!!.toString()
                )
            }
        } catch (e: Exception) {
            Timber.d(e)
            emptyList()
        }
    }

    private suspend fun saveElecQuery(elecQuery: ElecQuery) = withContext(Dispatchers.IO) {
        mQueryDao.save(elecQuery)
    }

    private suspend fun getElecCookie(): ElecCookie = withContext(Dispatchers.IO) {
        val account = getUsingAccount() ?: ""
        mCookieDao.elecCookie(account) ?: ElecCookie().apply {
            this.account = account
            saveElecCookie(this)
        }
    }

    private suspend fun saveElecCookie(cookie: ElecCookie) = withContext(Dispatchers.IO) {
        mCookieDao.save(cookie)
    }

    suspend fun getElecUser(): ElecUser = withContext(Dispatchers.IO) {
        val account = getUsingAccount() ?: ""
        return@withContext mUserDao.elecUser(account) ?: ElecUser().apply {
            this.sno = account
            if (this.sno.length == 9) {
                this.xfbAccount = '0' + this.sno
            } else {
                this.xfbAccount = this.sno
            }
        }
    }

    private suspend fun saveElecUser(elecUser: ElecUser) = withContext(Dispatchers.IO) {
        mUserDao.save(elecUser)
    }

    fun login(
        account: String,
        password: String,
        verify: String,
    ): LiveData<Resource<Boolean>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            val jsonResp = xfbService
                .login(
                    "http://cardapp.fafu.edu.cn:8088/Phone/Login?sourcetype=0&IMEI=" +
                            SPUtils.getInstance(Constants.SP_ELEC)
                                .getString("IMEI") + "&language=0",
                    account, String(Base64.encode(password.toByteArray(), Base64.DEFAULT)),
                    verify, "1", "1", "", "true"
                ).execute().body()!!.string()
            val json = JSONObject.parseObject(jsonResp)
            if (json.getBoolean("IsSucceed") == true) {

                val elecUser = ElecUser()
                elecUser.sno = getUsingAccount() ?: ""
                elecUser.xfbAccount = account
                elecUser.password = password
                elecUser.xfbId = json.getString("Obj")
                saveElecUser(elecUser)

                val obj2 = json.getJSONObject("Obj2")
                val elecCookie = getElecCookie().apply {
                    rescouseType = obj2.getString("RescouseType")
                }
                saveElecCookie(elecCookie)

                emit(Resource.Success(true))
            } else {
                if (json.containsKey("Msg")) {
                    emit(Resource.Failure(json.getString("Msg")))
                } else {
                    emit(Resource.Failure("未知错误"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Failure(e, "登录失败"))
        }
    }

    fun getVerifyBitmap(): Flow<Bitmap> = flow {
        val service = xfbService
        val bytes = service.verify(System.currentTimeMillis())
            .execute().body()?.bytes() ?: throw Failure("获取验证码失败")
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bitmap != null) {
            emit(bitmap)
        }
    }

    suspend fun getElectricityDefaultHistory(): List<ElectricityHistory> =
        withContext(Dispatchers.IO) {
            val dorm = SPUtils.getInstance(getUsingAccount()).getString("default_electricity_dorm")
            electricityHistoryDao.findAllByDorm(dorm)
                .sortedByDescending { it.timestamp }
        }

    suspend fun getElectricityHistoryOptions(): Options<String> =
        withContext(Dispatchers.IO) {
            val dorm = electricityHistoryDao.findAllDorm()
            val default =
                SPUtils.getInstance(getUsingAccount()).getString("default_electricity_dorm")
            Options(dorm, default)
        }
}