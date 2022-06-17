package cn.ifafu.ifafu.db.dao

import androidx.room.*
import cn.ifafu.ifafu.entity.ScoreFilter

@Dao
interface ScoreFilterDao {

    @Query("SELECT * FROM ScoreFilter WHERE account=:account")
    fun getAllScoreFilter(account: String): List<ScoreFilter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(scoreFilter: ScoreFilter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(scoreFilter: List<ScoreFilter>)

    @Query("DELETE FROM ScoreFilter WHERE scoreId=:id")
    fun delete(id: Int)

    @Query("DELETE FROM ScoreFilter WHERE account=:account")
    fun delete(account: String)

}