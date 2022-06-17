package cn.ifafu.ifafu.bean.bo

import cn.ifafu.ifafu.annotation.ElectricityFeeUnit

data class ElectricityFee(
    val balance: Double,
    @ElectricityFeeUnit val unit: Int //单位类型
) {
    val unitStr: String
        get() = if (unit == ElectricityFeeUnit.MONEY) "元" else "度"
}