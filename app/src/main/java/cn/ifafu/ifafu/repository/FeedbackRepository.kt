package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.bean.dto.Feedback
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.db.dao.UserDao
import cn.ifafu.ifafu.service.FeedbackService
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FeedbackRepository @Inject constructor(
    private val service: FeedbackService,
    private val userDao: UserDao,
) {

     suspend fun feedback(message: String, contact: String): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Resource.create(
                    service.feedback(
                        userDao.getUsingAccount() ?: "",
                        contact,
                        message,
                        AppUtils.getAppVersionName(),
                        DeviceUtils.getSDKVersionName(),
                        DeviceUtils.getSDKVersionCode().toString()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Failure("提交反馈失败，请加群反馈（群号在关于iFAFU里）")
            }
        }

     suspend fun query(): Resource<List<Feedback>> = withContext(Dispatchers.IO) {
        try {
            val sno = userDao.getUsingAccount() ?: ""
            Resource.create(service.query(sno))
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure("查询反馈失败")
        }
    }
}