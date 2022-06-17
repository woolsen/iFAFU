package cn.ifafu.ifafu.db.converter

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSONObject

class StringMapConverter {

    @Suppress("UNCHECKED_CAST")
    @TypeConverter
    fun convertToEntityProperty(databaseValue: String): MutableMap<String, String> {
        return HashMap(JSONObject.parseObject(databaseValue).innerMap as Map<String, String>)
    }

    @TypeConverter
    fun convertToDatabaseValue(entityProperty: MutableMap<String, String>): String {
        return JSONObject.toJSONString(entityProperty)
    }
}