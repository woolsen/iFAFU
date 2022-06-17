package cn.ifafu.ifafu.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 开学日期，用于计算第几周
 */
@Entity(tableName = "to_week")
data class FirstWeek(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    /**
     * 学年。样例: 2020-2021
     */
    var year: String,

    /**
     * 学期。样例: 1
     */
    var term: String,

    /**
     * 第一周日期格式: 2020-09-01
     */
    @ColumnInfo(name = "openingDay")
    var firstWeek: String
)