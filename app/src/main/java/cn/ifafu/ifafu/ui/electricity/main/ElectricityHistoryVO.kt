package cn.ifafu.ifafu.ui.electricity.main

data class ElectricityHistoryVO(
    val balance: Double,
    val time: Long,
    val diff: Double,
    val unit: String
)