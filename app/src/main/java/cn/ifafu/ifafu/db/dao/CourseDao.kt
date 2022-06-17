package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.bean.dto.TermOptions
import cn.ifafu.ifafu.bean.dto.Term
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.OptionDO
import com.blankj.utilcode.util.SPUtils

@Dao
abstract class CourseDao : BaseDao() {

    @Query("SELECT * FROM new_course WHERE id=:id")
    abstract fun getCourseById(id: Int): NewCourse?

    @Query("SELECT * FROM new_course WHERE account=:account AND year=:year AND term=:term")
    abstract fun getAllCourses(account: String, year: String, term: String): List<NewCourse>

    @Query("SELECT * FROM new_course WHERE account=:account")
    abstract fun getAllCourses(account: String): List<NewCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveCourse(course: NewCourse)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveCourses(vararg course: NewCourse)

    @Query("DELETE FROM new_course WHERE account=:account")
    abstract fun delete(account: String)

    @Query("DELETE FROM new_course WHERE id=:id")
    abstract fun deleteCourseById(id: Int)

    @Query("DELETE FROM new_course WHERE account=:account AND year=:year AND term=:term")
    abstract fun delete(account: String, year: String, term: String)

    @Query("SELECT * FROM option WHERE account=:account")
    protected abstract fun getOptions(account: String): List<OptionDO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveOption(vararg option: OptionDO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun saveOption(option: OptionDO)

    @Query("DELETE FROM option WHERE account=:account")
    protected abstract fun deleteOption(account: String)

    /**
     * 保存课表选项，同时删除旧选项
     */
    fun saveOptions(list: List<Term>) {
        val account = getAccount()
        deleteOption(account)
        list.onEach {
            val option = OptionDO(year = it.year, term = it.term, account = account)
            saveOption(option)
        }
    }

    /**
     * 保存默认显示的课表选项
     */
    fun saveDefaultOptions(term: Term) {
        val sp = SPUtils.getInstance(getAccount())
        sp.put("default_year", term.year)
        sp.put("default_term", term.term)
    }

    /**
     * 获取默认显示的课表选项
     */
    fun getDefaultOptions(): Term? {
        val sp = SPUtils.getInstance(getAccount())
        val defaultYear = sp.getString("default_year")
        val defaultTerm = sp.getString("default_term")
        if (defaultYear.isEmpty() || defaultTerm.isEmpty()) {
            return null
        }
        return Term(defaultYear, defaultTerm)
    }


    /**
     * @return 不存在时返回null
     */
    fun getOptions(): TermOptions? {
        val account = getAccount()
        val o = getOptions(account)
        if (o.isEmpty()) {
            return null
        }
        val selected = getDefaultOptions() ?: return null

        val options = o.sortedWith { o1, o2 ->
            o2.year.compareTo(o1.year).let {
                if (it == 0) {
                    o1.term.compareTo(o2.term)
                } else {
                    it
                }
            }
        }
            .map { Term(it.year, it.term) }
            .toMutableList()

        return TermOptions(selected, options)
    }

}