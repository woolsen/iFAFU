package cn.ifafu.ifafu.domain

import cn.ifafu.ifafu.bean.vo.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.RuntimeException

/**
 * 通过协程同步或者异步执行业务逻辑
 */
abstract class CoroutineUseCase<in ParamsType, ReturnType>(private val coroutineDispatcher: CoroutineDispatcher) {

    /**
     * 异步执行用例，返回[Resource]
     *
     * @param parameters 执行用例需要的参数
     *
     * @return [Resource]
     */
    suspend operator fun invoke(parameters: ParamsType): Resource<ReturnType> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters).let {
                    Resource.Success(it)
                }
            }
        } catch (e: Exception) {
            Timber.d(e)
            Resource.Failure(e)
        }
    }

    /**
     * 将需要执行的代码重写在此方法里
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: ParamsType): ReturnType
}