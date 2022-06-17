package cn.ifafu.ifafu.bean.bo

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult.Page
import androidx.paging.PagingState
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.Information
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class InformationPagingSource(
    private val query: suspend (page: Int, pageSize: Int) -> IFResponse<List<Information>>
) : PagingSource<Int, Information>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Information> =
        withContext(Dispatchers.IO) {
            try {
                val page = (params.key ?: 1)
                val pageSize = params.loadSize
                val data = query(page, pageSize).data
                if (data?.size == params.loadSize) {
                    Page(
                        data = data,
                        prevKey = null,
                        nextKey = page + 1
                    )
                } else {
                    Page(data ?: emptyList(), null, null)
                }
            } catch (e: IOException) {
                LoadResult.Error(e)
            } catch (e: HttpException) {
                LoadResult.Error(e)
            }
        }

    override fun getRefreshKey(state: PagingState<Int, Information>): Int? {
        return null
    }
}
