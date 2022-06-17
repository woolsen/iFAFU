package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.collections.HashMap

/**
 * 节假日
 */
@Entity(tableName = "holiday")
data class Holiday(

    /**
     * 使用[name]的hashCode作为id
     */
    @PrimaryKey
    var id: Int = 0,

    /**
     * 节假日名称
     */
    var name: String,

    /**
     * 放假天数
     */
    var days: Int,

    /**
     * 开始放假的日期。日期格式: yyyy-MM-dd
     */
    var from: String,

    /**
     * 调课情况。将[Map.Entry.key]日期的课程调至[Map.Entry.value]日期.
     * 日期格式: yyyy-MM-dd
     */
    var changes: Map<String, String> = HashMap()
)