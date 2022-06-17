package cn.ifafu.ifafu.bean.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import cn.ifafu.ifafu.db.converter.StringMapConverter
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity(tableName = "information", indices = [Index(value = ["status"])])
data class Information(
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    var id: Long,
    @SerializedName("content")
    val content: String,
    @SerializedName("date")
    val date: Date,
    @SerializedName("status")
    var status: Int?,
    @SerializedName("imageUrls")
    val imageUrls: List<String>,
    @SerializedName("contact")
    val contact: String?,
    @SerializedName("contactType")
    val contactType: Int,
    @SerializedName("title")
    val title: String?,
    val avatarUrl: String?,
    val nickname: String,
) : Serializable {

    @ColumnInfo(name = "type")
    var type: Int = 0

    companion object {
        const val STATUS_PASS = 1 //审核通过
        const val STATUS_REVIEWING = 0 //审核中
        const val STATUS_FAILURE = -1 //审核失败

        const val CONTACT_TYPE_NULL = 0 //无
        const val CONTACT_TYPE_PHONE = 1 //手机
        const val CONTACT_TYPE_QQ = 2 //QQ
        const val CONTACT_TYPE_WECHAT = 3 //微信
    }

}