package cn.ifafu.ifafu.db.dao

import androidx.room.*
import cn.ifafu.ifafu.entity.ElectricityHistory

@Dao
interface ElectricityHistoryDao {

    @Query("SELECT * FROM ElecHistory WHERE dorm=:dorm")
    fun findAllByDorm(dorm: String): List<ElectricityHistory>

    @Query("SELECT DISTINCT dorm FROM ElecHistory")
    fun findAllDorm(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(electricityHistory: ElectricityHistory)

    @Delete
    fun delete(history: ElectricityHistory)

}