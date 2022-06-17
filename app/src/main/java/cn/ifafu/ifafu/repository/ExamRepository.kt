package cn.ifafu.ifafu.repository

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.Exam
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.service.ExamService
import cn.ifafu.ifafu.util.ComparatorUtils
import cn.ifafu.ifafu.util.ComparatorUtils.thenCompare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ExamRepository @Inject constructor(
    jiaowuDatabase: JiaowuDatabase,
    private val examService: ExamService,
) : AbstractJwRepository(jiaowuDatabase.userDao) {

    private val examDao = jiaowuDatabase.examDao

    private val now = System.currentTimeMillis()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val examTimeComparator = ComparatorUtils
        .compareLong<Exam> { if (it.startTime <= now) now - it.startTime else 0 }
        .thenCompare { it.startTime }

     fun getExamsLiveDataFromNet(
        year: String,
        term: String,
    ): LiveData<Resource<List<Exam>>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading("获取中"))
        val res = Resource.transformWithNull({
            val user = getUsingUser()
                ?: return@transformWithNull IFResponse.failure("获取用户失败")
            examService.getExams(user, year, term)
        }, ifSuccessDataNotNull = { exams ->
            val account = getUsingAccount() ?: ""
            var filteredList = if (year != "全部" && term != "全部") {
                examDao.deleteByYearAndTerm(account, year, term)
                exams.filter { it.term == term && it.year == year }
            } else if (year != "全部") {
                examDao.deleteByYear(account, year)
                exams.filter { it.year == year }
            } else if (term != "全部") {
                examDao.deleteByTerm(account, term)
                exams.filter { it.term == term }
            } else {
                examDao.delete(account)
                exams
            }
            filteredList = filteredList.sortedWith(examTimeComparator)
            examDao.save(*filteredList.toTypedArray())
            Resource.Success(filteredList, "考试获取成功")
        }, ifSuccessDataNull = {
            Resource.Success(emptyList(), "考试获取成功")
        })
        emit(res)
    }

     fun getNowExamsLiveDataFromNet(): LiveData<Resource<List<Exam>>> {
        val semester = getSemester()
        return getExamsLiveDataFromNet(semester.yearStr, semester.termStr)
    }

     suspend fun getNowExamsFromLocal(): List<Exam> = withContext(Dispatchers.IO) {
        val semester = getSemester()
        getAllExamsFromLocal(semester.yearStr, semester.termStr)
    }

     suspend fun getAllExamsFromLocal(year: String, term: String): List<Exam> =
        withContext(Dispatchers.IO) {
            val account = getUsingAccount()
                ?: return@withContext emptyList()
            if (year == "全部" && term == "全部") {
                examDao.getAll(account)
            } else if (year == "全部") {
                examDao.getAllByTerm(account, term)
            } else if (term == "全部") {
                examDao.getAllByYear(account, year)
            } else {
                examDao.getAll(account, year, term)
            }.sortedWith(examTimeComparator).onEach {
                Timber.d(it.toString())
            }
        }

}