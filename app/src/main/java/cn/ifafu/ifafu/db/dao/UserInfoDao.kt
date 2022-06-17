package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.UserInfo

@Dao
abstract class UserInfoDao {

    @Query("SELECT * FROM user_info LIMIT 1")
    abstract fun findUserFirst(): UserInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun save(user: UserInfo)

    @Query("DELETE FROM user_info")
    abstract fun clearAll()

    fun saveInfo(userInfo: UserInfo) {
        userInfo.id = userInfo.username.hashCode()
        save(userInfo)
    }
}