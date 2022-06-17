package cn.ifafu.ifafu.repository

import androidx.annotation.WorkerThread
import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.db.dao.UserDao
import cn.ifafu.ifafu.service.CommentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val service: CommentService,
    private val userDao: UserDao
) {

    @WorkerThread
    suspend fun getCommentList(): Resource<List<CommentItem>> =
        withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUsingUser()!!
                val response = service.getCommentList(user)
                if (response.code == IFResponse.SUCCESS) {
                    Resource.Success(response.data!!)
                } else {
                    Resource.Failure(response.message)
                }
            } catch (e: Exception) {
                Timber.e(e)
                Resource.Failure(e)
            }
        }

    suspend fun autoComment(commentItem: CommentItem): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUsingUser()!!
                val response = service.autoComment(user, commentItem)
                if (response.code == IFResponse.SUCCESS) {
                    Resource.Success(Unit)
                } else {
                    Resource.Failure(response.message)
                }
            } catch (e: Exception) {
                Timber.e(e)
                Resource.Failure(e)
            }
        }

    suspend fun commit(): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUsingUser()!!
                val response = service.commit(user)
                if (response.code == IFResponse.SUCCESS) {
                    Resource.Success(Unit)
                } else {
                    Resource.Failure(response.message)
                }
            } catch (e: Exception) {
                Timber.e(e)
                Resource.Failure(e)
            }
        }
}