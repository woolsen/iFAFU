package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey
    var id: Int = 0,

    /**
     * 用户名
     */
    var username: String,

    /**
     * 昵称
     */
    var nickname: String,

    /**
     * 权限列表
     */
    var authorities: Set<String> = TreeSet()
)