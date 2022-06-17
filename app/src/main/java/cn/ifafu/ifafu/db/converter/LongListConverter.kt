package cn.ifafu.ifafu.db.converter

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSONObject

class LongListConverter {
    @TypeConverter
    fun convertToEntityProperty(databaseValue: String): List<Long> {
        return JSONObject.parseArray(databaseValue, Long::class.java)
    }

    @TypeConverter
    fun convertToDatabaseValue(entityProperty: List<Long>): String {
        return JSONObject.toJSONString(entityProperty)
    }

}