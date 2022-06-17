package cn.ifafu.ifafu.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cn.ifafu.ifafu.entity.UserInfo
import cn.ifafu.ifafu.db.converter.StringSetConverter
import cn.ifafu.ifafu.db.dao.UserInfoDao

@Database(
    entities = [
        UserInfo::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    value = [
        StringSetConverter::class
    ]
)
abstract class IfafuDatabase : RoomDatabase() {

    abstract val userInfoDao: UserInfoDao

}