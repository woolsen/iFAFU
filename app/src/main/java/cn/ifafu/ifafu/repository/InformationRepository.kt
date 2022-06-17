package cn.ifafu.ifafu.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cn.ifafu.ifafu.bean.bo.InformationPagingSource
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.exception.IFResponseFailureException
import cn.ifafu.ifafu.service.InformationService
import cn.ifafu.ifafu.util.OkHttpUtils.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.io.File
import java.util.*
import javax.inject.Inject

class InformationRepository @Inject constructor(
    private val service: InformationService
) {

    companion object {
        private const val PAGE_SIZE = 15
    }

    fun query(): Flow<PagingData<Information>> {
        return Pager(PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE)) {
            InformationPagingSource { page, pageSize ->
                service.query(page, pageSize)
            }
        }.flow
    }

    fun queryMy(): Flow<PagingData<Information>> {
        return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            InformationPagingSource { page, pageSize ->
                service.queryMy(page, pageSize)
            }
        }.flow
    }

    fun delete(id: Long): Flow<Resource<Boolean>> = flow {
        val resp = service.delete(id)
        if (resp.code == IFResponse.SUCCESS) {
            emit(Resource.Success(true))
        } else {
            emit(Resource.Failure(resp.message))
        }
    }

    suspend fun upload(
        content: String,
        contact: String,
        contactType: Int,
        images: List<File>,
    ): Flow<Boolean> = flow {
        val resp = service.upload(
            content = content,
            contact = contact,
            contactType = contactType,
            category = 0L,
            images = images.toMultipartBodyParts()
        )
        if (resp.code == IFResponse.SUCCESS) {
            emit(true)
        } else {
            throw IFResponseFailureException(resp.message)
        }
    }

    suspend fun edit(
        id: Long,
        content: String,
        contact: String,
        contactType: Int
    ): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val resp = service.edit(id, content, contact, contactType, 0L)
            if (resp.code == IFResponse.SUCCESS) {
                if (resp.data == true) {
                    Resource.Success(true)
                } else {
                    Resource.Failure("编辑失败")
                }
            } else {
                Resource.Failure(resp.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e, "编辑失败")
        }
    }

    private fun List<File>.toMultipartBodyParts(): List<MultipartBody.Part> {
        return this.map {
            MultipartBody.Part.createFormData(
                "images", it.name, it.toRequestBody()
            )
        }
    }
}