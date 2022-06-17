package cn.ifafu.ifafu.annotation

import androidx.annotation.IntDef


@IntDef(value = [ElectricityFeeUnit.MONEY, ElectricityFeeUnit.POWER], flag = true)
annotation class ElectricityFeeUnit {


    companion object {
        const val MONEY = 1 //电费
        const val POWER = 2 //电量
    }

}