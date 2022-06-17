package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.Electives

@Dao
interface ElectivesDao {
    @Query("SELECT * FROM Electives WHERE account=:account")
    fun electives(account: String): Electives?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(electives: Electives)

    @Query("DELETE FROM Electives WHERE account=:account")
    fun delete(account: String)
}