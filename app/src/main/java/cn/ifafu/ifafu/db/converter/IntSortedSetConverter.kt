package cn.ifafu.ifafu.db.converter

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSONObject
import java.util.*

class IntSortedSetConverter {
    @TypeConverter
    fun convertToEntityProperty(databaseValue: String): SortedSet<Int> {
        return JSONObject.parseArray(databaseValue, Integer.TYPE).toSortedSet()
    }

    @TypeConverter
    fun convertToDatabaseValue(entityProperty: SortedSet<Int>): String {
        return JSONObject.toJSONString(entityProperty)
    }
}