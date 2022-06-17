package cn.ifafu.ifafu.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@Entity
data class Exam(
    @PrimaryKey
    var id: Int = 0,
    var name: String = "",
    var startTime: Long = 0,  //无时间信息则为0
    var endTime: Long = 0,
    var address: String = "",
    var seatNumber: String = "",
    var account: String = "",
    var year: String = "",
    var term: String = ""
) : Parcelable {

    override fun toString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.CHINA)
        return "Exam{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startTime=" + format.format(Date(startTime)) +
                ", endTime=" + format.format(Date(endTime)) +
                ", address='" + address + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", account='" + account + '\'' +
                ", year='" + year + '\'' +
                ", term='" + term + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Exam

        if (name != other.name) return false
        if (account != other.account) return false
        if (year != other.year) return false
        if (term != other.term) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + term.hashCode()
        return result
    }
}