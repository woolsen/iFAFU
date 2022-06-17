package cn.ifafu.ifafu.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import cn.ifafu.ifafu.bean.bo.Semester
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.db.dao.ScoreDao
import cn.ifafu.ifafu.db.dao.ScoreFilterDao
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.entity.ScoreFilter
import cn.ifafu.ifafu.service.ScoreService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class ScoreRepository @Inject constructor(
    jiaowuDatabase: JiaowuDatabase,
    private val scoreService: ScoreService,
) : AbstractJwRepository(jiaowuDatabase.userDao) {

    private val scoreDao: ScoreDao = jiaowuDatabase.scoreDao
    private val scoreFilterDao: ScoreFilterDao = jiaowuDatabase.scoreFilterDao

    /**
     * 从教务管理系统获取成绩，若获取成功则保存到数据库
     *
     * @param year 学年
     * @param term 学期
     * @return LiveData<Resource<List<Score>>>
     *     监听整个获取过程的LiveData，包括Loading状态
     */
    fun getScoresLiveDataFromNet(
        year: String,
        term: String,
    ): LiveData<Resource<List<Score>>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        val user = getUsingUser()
        if (user == null) {
            emit(Resource.failure("获取用户失败"))
            return@liveData
        }
        val res = Resource.transformWithNull(respBlock = {
            scoreService.getAllScore(user)
        }, ifSuccessDataNotNull = { scores ->
            // 由于获取的是所有学期成绩，则删除所有当前账号成绩，再保存新获取到的成绩即可
            scoreDao.delete(user.account)
            scoreDao.save(scores)
            val data = scores
                .filter(year, term)
                .filterIES()
            Resource.Success(data, "成绩获取成功")
        }, ifSuccessDataNull = {
            Resource.Failure("无法获取课表")
        })
        emit(res)
    }

    /**
     * 查询所有成绩
     */
    suspend fun getAllScoresFromNet(): Resource<List<Score>> =
        withContext(Dispatchers.IO) {
            val user = getUsingUser() ?: return@withContext Resource.failure("获取用户失败")
            Resource.transformWithNull(respBlock = {
                scoreService.getAllScore(user)
            }, ifSuccessDataNotNull = { scores ->
                scoreDao.delete(getUsingAccount() ?: "")
                scoreDao.save(scores)
                val data = scores.filterIES()
                Resource.Success(data, "成绩获取成功")
            }, ifSuccessDataNull = {
                Resource.Success(emptyList(), "成绩获取成功")
            })
        }

    suspend fun getNowScoresFromLocal(): List<Score> = withContext(Dispatchers.IO) {
        val semester = getSemester()
        getScoresFromLocal(semester.yearStr, semester.termStr)
    }

    suspend fun getNowScoresLiveDataFromNet() = withContext(Dispatchers.IO) {
        val semester = getSemester()
        getScoresLiveDataFromNet(semester.yearStr, semester.termStr)
    }

    suspend fun getAllScoresFromLocal(): List<Score> = withContext(Dispatchers.IO) {
        val account = getUsingAccount() ?: ""
        scoreDao.getAll(account).filterIES()
    }

    suspend fun getScoresFromLocal(year: String, term: String): List<Score> =
        withContext(Dispatchers.IO) {
            val account = getUsingAccount() ?: ""
            if (year != "全部" && term != "全部") {
                scoreDao.getAll(account, year, term)
            } else if (year != "全部") {
                scoreDao.getAllByYear(account, year)
            } else if (term != "全部") {
                scoreDao.getAllByTerm(account, term)
            } else {
                scoreDao.getAll(account)
            }.filterIES()
        }

    private fun List<Score>.filter(year: String, term: String): List<Score> {
        return if (year != "全部" && term != "全部") {
            this@filter.filter { it.term == term && it.year == year }
        } else if (year != "全部") {
            this@filter.filter { it.year == year }
        } else if (term != "全部") {
            this@filter.filter { it.term == term }
        } else {
            this@filter
        }
    }

    /**
     * 排除不计入智育分的成绩
     */
    private fun List<Score>.filterIES(): List<Score> {
        val account = getUsingAccount() ?: ""
        val filters = scoreFilterDao.getAllScoreFilter(account)
        this@filterIES.forEach { score ->
            val filter = filters.find { it.scoreId == score.id }
            if (filter != null) {
                score.isIESItem = filter.isIESItem
            }
        }
        return this@filterIES
    }

    suspend fun saveFilter(scoreFilter: ScoreFilter) = withContext(Dispatchers.IO) {
        scoreFilterDao.save(scoreFilter)
    }

    suspend fun saveFilter(scoreFilter: List<ScoreFilter>) = withContext(Dispatchers.IO) {
        scoreFilterDao.save(scoreFilter)
    }

    suspend fun getTermOption(): Semester = withContext(Dispatchers.IO) {
        val yearList: MutableList<String> = ArrayList()
        val c = Calendar.getInstance()
        val termIndex =
            if (c[Calendar.MONTH] < Calendar.FEBRUARY || c[Calendar.MONTH] > Calendar.JULY) 0 else 1
        c.add(Calendar.MONTH, 5)
        val year = c[Calendar.YEAR]
        val yearByAccount: Int //通过学号判断学生
        val account = getUsingAccount() ?: ""
        yearByAccount = if (account.length == 10) {
            account.substring(1, 3).toInt() + 2000
        } else {
            account.substring(0, 2).toInt() + 2000
        }
        for (i in yearByAccount until year) {
            yearList.add(0, "${i}-${i + 1}")
        }
        yearList.add("全部")
        Semester(yearList, arrayListOf("1", "2", "全部"), 0, termIndex)
    }

}