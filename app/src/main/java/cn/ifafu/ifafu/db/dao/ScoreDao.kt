package cn.ifafu.ifafu.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import cn.ifafu.ifafu.entity.Score

@Dao
interface ScoreDao {

    @Query("SELECT * FROM Score WHERE account=:account ORDER BY id")
    fun getAll(account: String): List<Score>

    @Query("SELECT * FROM Score WHERE account=:account AND term=:term AND year=:year ORDER BY id")
    fun getAll(account: String, year: String, term: String): List<Score>

    @Query("SELECT * FROM Score WHERE account=:account AND year=:year ORDER BY id")
    fun getAllByYear(account: String, year: String): List<Score>

    @Query("SELECT * FROM Score WHERE account=:account AND term=:term ORDER BY id")
    fun getAllByTerm(account: String, term: String): List<Score>

    /**
     * LiveData
     */
    @Query("SELECT * FROM Score WHERE account=:account ORDER BY id")
    fun getAllLiveData(account: String): LiveData<List<Score>>

    @Query("SELECT * FROM Score WHERE account=:account AND term=:term AND year=:year ORDER BY id")
    fun getAllLiveData(account: String, year: String, term: String): LiveData<List<Score>>

    @Query("SELECT * FROM Score WHERE account=:account AND year=:year ORDER BY id")
    fun getAllLiveDataByYear(account: String, year: String): LiveData<List<Score>>

    @Query("SELECT * FROM Score WHERE account=:account AND term=:term ORDER BY id")
    fun getAllLiveDataByTerm(account: String, term: String): LiveData<List<Score>>


    @Query("SELECT * FROM Score WHERE id=:id")
    fun getScoreById(id: Int): Score

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(score: Score)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(score: List<Score>)

    @Delete
    fun delete(vararg score: Score)

    @Query("DELETE FROM Score WHERE account=:account")
    fun delete(account: String)

    @Query("DELETE FROM Score WHERE account=:account AND year=:year")
    fun deleteByYear(account: String, year: String)

    @Query("DELETE FROM Score WHERE account=:account AND term=:term")
    fun deleteByTerm(account: String, term: String)

    @Query("DELETE FROM Score WHERE account=:account  AND term=:term AND year=:year")
    fun deleteByYearAndTerm(account: String, year: String, term: String)

    @Query("DELETE FROM Score WHERE term=:term AND year=:year")
    fun delete(year: String, term: String)

    @Query("SELECT * FROM Score WHERE account=:account AND term=:term AND year=:year ORDER BY id")
    fun getAllLD(account: String, year: String, term: String): LiveData<List<Score>>

}