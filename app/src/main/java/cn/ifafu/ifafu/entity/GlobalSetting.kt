package cn.ifafu.ifafu.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
class GlobalSetting {
    @PrimaryKey
    var account: String = ""
    var theme = THEME_NEW

    @Ignore
    constructor(account: String) {
        this.account = account
    }

    constructor()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val setting = other as GlobalSetting
        return theme == setting.theme &&
                account == setting.account
    }

    override fun hashCode(): Int {
        return Objects.hash(account, theme)
    }

    companion object {
        const val THEME_NEW = 0
        const val THEME_OLD = 1
    }
}