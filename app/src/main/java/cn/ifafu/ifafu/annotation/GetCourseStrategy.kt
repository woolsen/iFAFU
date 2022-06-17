package cn.ifafu.ifafu.annotation

import androidx.annotation.IntDef


/**
 * 课表获取策略
 */
@Retention(AnnotationRetention.BINARY)
@IntDef(
    GetCourseStrategy.LOCAL,
    GetCourseStrategy.NETWORK,
    GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY,
    GetCourseStrategy.LOCAL_AND_NETWORK
)
annotation class GetCourseStrategy {
    companion object {
        /**
         * 从本地数据获取课表
         */
        const val LOCAL = 1

        /**
         * 从网络数据获取课表
         */
        const val NETWORK = 2

        /**
         * 如果本地数据为空，则从网络获取课表
         */
        const val NETWORK_IF_LOCAL_EMPTY = 3

        /**
         * 先从本地数据获取课表，再从网络获取课表
         */
        const val LOCAL_AND_NETWORK = 4
    }
}
