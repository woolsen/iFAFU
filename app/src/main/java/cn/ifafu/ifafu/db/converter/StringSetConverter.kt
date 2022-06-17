package cn.ifafu.ifafu.db.converter

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSONObject

class StringSetConverter {
    @TypeConverter
    fun convertToEntityProperty(databaseValue: String): Set<String> {
        return JSONObject.parseArray(databaseValue, String::class.java).toSet()
    }

    @TypeConverter
    fun convertToDatabaseValue(entityProperty: Set<String>): String {
        return JSONObject.toJSONString(entityProperty)
    }

}