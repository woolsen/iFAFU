package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.FirstWeek
import cn.ifafu.ifafu.util.getInts
import java.util.*

@Dao
abstract class OpeningDayDao {

    @Query("SELECT * FROM to_week WHERE year=:year AND term=:term")
    protected abstract fun getOpeningInner(year: String, term: String): FirstWeek?

    @Query("DELETE FROM to_week WHERE year=:year AND term=:term")
    protected abstract fun deleteInner(year: String, term: String)

    @Query("DELETE FROM to_week")
    protected abstract fun deleteAllInner()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveInner(firstWeek: FirstWeek)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveInner(firstWeeks: List<FirstWeek>)

    fun save(firstWeeks: List<FirstWeek>) {
        deleteAllInner()
        saveInner(firstWeeks)
    }

    fun save(firstWeek: FirstWeek) {
        deleteInner(firstWeek.year, firstWeek.term)
        saveInner(firstWeek)
    }

    /**
     * 返回第一周周一日期
     */
    fun find(year: String, term: String): String {
        val openingDay = getOpeningInner(year, term)
        if (openingDay != null) {
            return openingDay.firstWeek
        }
        return if (year.isBlank() || term.isBlank() || year.matches("[0-9]{4}-[0-9]{4}".toRegex())) {
            val current = Calendar.getInstance()
            if (term == "1") {
                "${current.get(Calendar.YEAR)}-09-01"
            } else {
                "${current.get(Calendar.YEAR)}-03-01"
            }
        } else {
            if (term == "1") {
                "${year.getInts()[0]}-09-01"
            } else {
                "${year.getInts()[1]}-03-01"
            }
        }
    }
}