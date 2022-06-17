package cn.ifafu.ifafu.bean.bo

import cn.ifafu.ifafu.entity.ElectricityHistory

data class ElectricityHistoryBO(
    val options: Options<String>,
    val history: List<ElectricityHistory>
)