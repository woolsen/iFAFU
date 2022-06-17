package cn.ifafu.ifafu.db.converter

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSONObject

class IntListConverter {
    @TypeConverter
    fun convertToEntityProperty(databaseValue: String): List<Int> {
        return JSONObject.parseArray(databaseValue, Integer.TYPE)
    }

    @TypeConverter
    fun convertToDatabaseValue(entityProperty: List<Int>): String {
        return JSONObject.toJSONString(entityProperty)
    }

}