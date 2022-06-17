package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.entity.SyllabusSetting

@Dao
abstract class SyllabusSettingDao {

    @Query("SELECT * FROM new_course WHERE account=:account")
    protected abstract fun getAllCourses(account: String): List<NewCourse>

    @Query("SELECT * FROM SyllabusSetting WHERE account=:account")
    protected abstract fun getSetting(account: String): SyllabusSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveSetting(syllabusSetting: SyllabusSetting)

    fun getTimetableSetting(account: String): SyllabusSetting {
        var setting = getSetting(account)
        if (setting == null) {
            setting = SyllabusSetting().apply {
                this.account = account
            }
        }

        val data = getAllCourses(account)
        if (!data.isNullOrEmpty()) {
            setting = setting.apply {
                //若教室存在旗教字样，则为旗山校区
                val isQiShan = data.find { it.classroom.contains("旗教") } != null
                beginTime = SyllabusSetting.intBeginTime[if (isQiShan) 1 else 0]
            }
        }
        return setting
    }

}