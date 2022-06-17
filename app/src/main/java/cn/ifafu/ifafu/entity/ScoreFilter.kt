package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScoreFilter(
        @PrimaryKey
        val scoreId: Int,
        val account: String,
        val isIESItem: Boolean //是否计入智育分
)