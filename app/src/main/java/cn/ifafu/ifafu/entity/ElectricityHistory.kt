package cn.ifafu.ifafu.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import cn.ifafu.ifafu.annotation.ElectricityFeeUnit

@Entity(
    tableName = "ElecHistory",
    primaryKeys = ["dorm", "timestamp"]
)
data class ElectricityHistory(
    var dorm: String = "", //宿舍号
    var timestamp: Long = 0L,
    var balance: Double = 0.0,

    @ElectricityFeeUnit
    @ColumnInfo(name = "type")
    var unit: Int = 0
) {
    val unitStr: String
        get() = when(unit) {
            ElectricityFeeUnit.POWER -> "度"
            ElectricityFeeUnit.MONEY -> "元"
            else -> ""
        }
}