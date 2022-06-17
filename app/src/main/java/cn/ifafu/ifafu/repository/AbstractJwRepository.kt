package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.bean.bo.Semester
import cn.ifafu.ifafu.db.dao.UserDao
import cn.ifafu.ifafu.entity.User
import java.util.*

abstract class AbstractJwRepository(
    protected val userDao: UserDao,
) {

    fun getSemester(): Semester {
        val account = userDao.getUsingAccount()
            ?: return Semester(arrayListOf("全部"), arrayListOf("1", "2", "全部"), 0, 0)
        val yearList: MutableList<String> = ArrayList()
        val c = Calendar.getInstance()
        val termIndex = if (c[Calendar.MONTH] < 1 || c[Calendar.MONTH] > 6) 0 else 1
        c.add(Calendar.MONTH, 5)
        val year = c[Calendar.YEAR]
        val yearByAccount = if (account.length == 10) {
            account.substring(1, 3).toInt() + 2000
        } else {
            account.substring(0, 2).toInt() + 2000
        } //通过学号判断学生
        for (i in yearByAccount until year) {
            yearList.add(0, "${i}-${i + 1}")
        }
        yearList.add("全部")
        return Semester(yearList, arrayListOf("1", "2", "全部"), 0, termIndex)
    }

    protected fun getUsingAccount(): String? {
        return userDao.getUsingAccount()
    }

    protected suspend fun getUsingUser(): User? {
        return userDao.getUsingUser()
    }

}