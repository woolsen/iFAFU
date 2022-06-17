package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.ElecUser

@Dao
interface ElecUserDao {
    @Query("SELECT * FROM ElecUser WHERE account=:sno")
    fun elecUser(sno: String): ElecUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(elecUser: ElecUser)

    @Query("DELETE FROM ElecUser WHERE account=:sno")
    fun delete(sno: String)
}