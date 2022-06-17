package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "option")
class OptionDO(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var year: String,
    var term: String,
    var account: String
)