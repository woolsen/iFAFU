package cn.ifafu.ifafu.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.GlobalSetting

@Dao
interface GlobalSettingDao {

    @Query("SELECT * FROM GlobalSetting WHERE account=:account")
    fun globalSetting(account: String): GlobalSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(setting: GlobalSetting)

    @Query("DELETE FROM GlobalSetting WHERE account=:account")
    fun delete(account: String)

    @Query("SELECT * FROM GlobalSetting WHERE account=:account")
    fun get(account: String): LiveData<GlobalSetting>
}