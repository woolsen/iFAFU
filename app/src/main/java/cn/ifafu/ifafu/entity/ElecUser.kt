package cn.ifafu.ifafu.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ElecUser {
    @PrimaryKey
    @ColumnInfo(name = "account")
    var sno: String = ""
    var xfbAccount: String= ""
    var xfbId: String = ""
    var password: String= ""

}