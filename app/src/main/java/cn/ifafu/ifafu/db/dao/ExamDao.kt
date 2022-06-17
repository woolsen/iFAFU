package cn.ifafu.ifafu.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.Exam

@Dao
interface ExamDao {
    @Query("SELECT * FROM Exam WHERE account=:account")
    fun getAll(account: String): List<Exam>

    @Query("SELECT * FROM Exam WHERE account=:account AND year=:year AND term=:term")
    fun getAll(account: String, year: String, term: String): List<Exam>

    @Query("SELECT * FROM Exam WHERE account=:account AND year=:year AND term=:term")
    fun getAllLiveData(account: String, year: String, term: String): LiveData<List<Exam>>

    @Query("SELECT * FROM Exam WHERE account=:account AND term=:term")
    fun getAllByTerm(account: String, term: String): List<Exam>

    @Query("SELECT * FROM Exam WHERE account=:account AND year=:year")
    fun getAllByYear(account: String, year: String): List<Exam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg exam: Exam)

    @Query("DELETE FROM Exam WHERE account=:account")
    fun delete(account: String)

    @Query("DELETE FROM Exam WHERE account=:account AND year=:year AND term=:term")
    fun deleteByYearAndTerm(account: String, year: String, term: String)

    @Query("DELETE FROM Exam WHERE account=:account AND year=:year")
    fun deleteByYear(account: String, year: String)

    @Query("DELETE FROM Exam WHERE account=:account AND term=:term")
    fun deleteByTerm(account: String, term: String)

}