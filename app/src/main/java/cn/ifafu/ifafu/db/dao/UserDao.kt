package cn.ifafu.ifafu.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.entity.User
import com.blankj.utilcode.util.SPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
abstract class UserDao {

    @Query("SELECT * FROM User ORDER BY account")
    abstract suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE account=:account")
    abstract suspend fun getUser(account: String): User?

    fun getUsingAccount(): String? {
        return SPUtils.getInstance(Constants.SP_USER_INFO).getString("account", null)
    }

    suspend fun getUsingUser(): User? = withContext(Dispatchers.IO) {
        val usingAccount = getUsingAccount()
        val user = usingAccount?.let { getUser(it) } ?: getUserFirst()
        if (user != null) {
            SPUtils.getInstance(Constants.SP_USER_INFO).put("account", user.account)
        }
        return@withContext user
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun save(vararg user: User)

    suspend fun saveUsing(user: User) {
        val account = user.account
        save(user)
        SPUtils.getInstance(Constants.SP_USER_INFO).put("account", account)
    }

    suspend fun deleteAllData(account: String) {
        SPUtils.getInstance(Constants.SP_USER_INFO).remove("account")
        deleteInner(account)
        deleteCourse(account)
        deleteTimetableSetting(account)
        deleteExam(account)
        deleteScore(account)
        val users = getAllUsers()
        if (users.isNotEmpty()) {
            SPUtils.getInstance(Constants.SP_USER_INFO).put("account", users[0].account)
        }
    }

    suspend fun deleteUserOnly(account: String) {
        SPUtils.getInstance(Constants.SP_USER_INFO).remove("account")
        deleteInner(account)
        val users = getAllUsers()
        if (users.isNotEmpty()) {
            SPUtils.getInstance(Constants.SP_USER_INFO).put("account", users[0].account)
        }
    }

    @Query("SELECT * FROM User FIRST")
    protected abstract suspend fun getUserFirst(): User?

    @Query("DELETE FROM User WHERE account=:account")
    protected abstract suspend fun deleteInner(account: String)

    @Query("DELETE FROM new_course WHERE account=:account")
    protected abstract suspend fun deleteCourse(account: String)

    @Query("DELETE FROM SyllabusSetting WHERE account=:account")
    protected abstract suspend fun deleteTimetableSetting(account: String)

    @Query("DELETE FROM Exam WHERE account=:account")
    protected abstract suspend fun deleteExam(account: String)

    @Query("DELETE FROM Score WHERE account=:account")
    protected abstract suspend fun deleteScore(account: String)

}