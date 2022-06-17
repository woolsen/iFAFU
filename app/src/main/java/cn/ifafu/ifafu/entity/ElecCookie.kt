package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ElecCookie {
    @PrimaryKey
    var account: String = ""
    var rescouseType: String = ""

    var map: MutableMap<String, String> = hashMapOf(
        "imeiticket" to "",
        "sourcetypeticket" to "0"
    )

    operator fun set(name: String, value: String) {
        map[name] = value
    }

    operator fun get(name: String): String {
        return map[name] ?: ""
    }

    fun toCookieString(): String {
        return getACookie("ASP.NET_SessionId") +
                getACookie("imeiticket") +
                getACookie("hallticket") +
                getACookie("username") +
                getACookie("sourcetypeticket")
    }

    private fun getACookie(name: String): String {
        val value = map[name]
        return if (value != null) {
            "$name=$value; "
        } else {
            ""
        }
    }

}