package cn.ifafu.ifafu.db.dao

import androidx.room.*
import cn.ifafu.ifafu.entity.Holiday

@Dao
abstract class HolidayDao {

    @Query("SELECT * FROM holiday")
    abstract fun findAll(): List<Holiday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveIn(holidays: List<Holiday>)

    fun save(holidays: List<Holiday>) {
        holidays.onEach {
            it.id = it.name.hashCode()
        }
        saveIn(holidays)
    }

    @Delete
    abstract fun delete(holidays: List<Holiday>)
}