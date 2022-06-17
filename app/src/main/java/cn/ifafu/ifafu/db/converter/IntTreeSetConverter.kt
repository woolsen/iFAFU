package cn.ifafu.ifafu.db.converter

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSONObject
import java.util.*

class IntTreeSetConverter {
    @TypeConverter
    fun convertToEntityProperty(databaseValue: String): TreeSet<Int> {
        return TreeSet(JSONObject.parseArray(databaseValue, Integer.TYPE))
    }

    @TypeConverter
    fun convertToDatabaseValue(entityProperty: TreeSet<Int>): String {
        return JSONObject.toJSONString(entityProperty)
    }
}