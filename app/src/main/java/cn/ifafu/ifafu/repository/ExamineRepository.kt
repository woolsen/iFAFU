package cn.ifafu.ifafu.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cn.ifafu.ifafu.bean.bo.InformationPagingSource
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.service.ExamineService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ExamineRepository @Inject constructor(
    private val examineService: ExamineService
) {

    companion object {
        private const val PAGE_SIZE = 15
    }

    fun query(): Flow<PagingData<Information>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
            InformationPagingSource { page, pageSize ->
                examineService.query(page, pageSize)
            }
        }.flow


    fun query(status: Int): Flow<PagingData<Information>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
            InformationPagingSource { page, pageSize ->
                examineService.queryByStatus(page, pageSize, status)
            }
        }.flow

    fun examine(id: Long, status: Int): Flow<Resource<Boolean>> = flow {
        try {
            val resp = examineService.examine(id, status)
            if (resp.code == IFResponse.SUCCESS) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Failure(resp.message))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Failure(e, "审核状态更改失败"))
        }
    }
}