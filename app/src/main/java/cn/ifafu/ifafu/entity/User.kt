package cn.ifafu.ifafu.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.ifafu.ifafu.annotation.SchoolCode
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity
data class User(
    @PrimaryKey
    var account: String = "", // 学号

    var password: String = "", // 密码

    var name: String = "",// 名字

    @SchoolCode
    @ColumnInfo(name = "schoolCode")
    var school: String = FAFU, // 学校代号

    /**
     * 用于拼接FAFU教务管理系统
     */
    var token: String = generateToken(),

    @ColumnInfo(name = "last_login_time")
    var lastLoginTime: Long = 0 //上次登录时间
) : Parcelable {

    companion object {
        const val FAFU = "FAFU"
        const val FAFU_JS = "FAFU_JS"

        fun generateToken(): String {
            val randomStr = "abcdefghijklmnopqrstuvwxyz12345".toCharArray()
            val token = StringBuilder()
            val random = Random()
            for (i in 0..23) {
                token.append(randomStr[random.nextInt(31)])
            }
            return "($token)"
        }
    }
}