package cn.ifafu.ifafu.repository

import cn.ifafu.ifafu.annotation.GetCourseStrategy
import cn.ifafu.ifafu.bean.bo.WeekAndDay
import cn.ifafu.ifafu.bean.dto.*
import cn.ifafu.ifafu.bean.vo.OpeningDayVO
import cn.ifafu.ifafu.db.JiaowuDatabase
import cn.ifafu.ifafu.db.dao.CourseDao
import cn.ifafu.ifafu.db.dao.OpeningDayDao
import cn.ifafu.ifafu.di.Ifafu
import cn.ifafu.ifafu.entity.Holiday
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.service.IFAFUService
import cn.ifafu.ifafu.service.TimetableService
import cn.ifafu.ifafu.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class TimetableRepository @Inject constructor(
    database: JiaowuDatabase,
    private val service: TimetableService,
    @Ifafu private val retrofit: Retrofit,
) : AbstractJwRepository(database.userDao) {

    private val ifafuService: IFAFUService = retrofit.create(IFAFUService::class.java)

    private val openingDayDao: OpeningDayDao = database.openingDayDao
    private val courseDao: CourseDao = database.newCourseDao
    private val syllabusSettingDao = database.syllabusSettingDao
    private val holidayDao = database.holidayDao

    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val firstWeeks = ifafuService.firstWeeks()
                val holidays = ifafuService.holiday()
                holidayDao.save(holidays)
                openingDayDao.save(firstWeeks)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun getTermOptionsResource(): Flow<TermOptions> = flow {
        val local = courseDao.getOptions()
        if (local != null) {
            emit(local)
        }
        try {
            val termOptions = getTermOptionsFromNetworkInner()
            courseDao.saveDefaultOptions(termOptions.selected)
            emit(termOptions)
        } catch (e: Exception) {
            Timber.e("获取网络课表出错, err: ${e.message}")
        }
    }

    suspend fun getTermOptions(): TermOptions? {
        return withContext(Dispatchers.IO) {
            val options = courseDao.getOptions()
            if (options != null) {
                return@withContext options
            }

            val user = getUsingUser()
                ?: return@withContext null
            val resp = service.getTermOptions(user)
            val data = resp.data
            if (resp.code == IFResponse.SUCCESS && data != null) {
                courseDao.saveOptions(data.options)
            }
            return@withContext data
        }
    }

    private suspend fun getCoursesNetwork(year: String, term: String): List<NewCourse> {
        val user = getUsingUser() ?: return emptyList()
        val resp = service.getTimetable(user, year, term)
        return if (resp.code == IFResponse.SUCCESS) {
            val courses = resp.data ?: emptyList()
            if (courses.isNotEmpty()) {
                courseDao.delete(user.account, year, term)
                courseDao.saveCourses(*courses.toTypedArray())
            }
            courses
        } else {
            throw Failure(resp.message)
        }
    }

    fun getCoursesFlow(
        year: String,
        term: String,
        @GetCourseStrategy strategy: Int = GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY,
    ): Flow<List<NewCourse>> = flow {
        println("Timetable TAG, year: ${year}, term: ${term}")
        if (year.isBlank() || term.isBlank()) {
            emit(emptyList())
            return@flow
        }
        println("Timetable TAG, getUsingUser start")
        val account = getUsingUser()?.account ?: return@flow
        println("Timetable TAG, getUsingUser end")
        val localFirstWeek = openingDayDao.find(year, term)
        val localHolidays = holidayDao.findAll()
        var courses = emptyList<NewCourse>()
        if (strategy == GetCourseStrategy.LOCAL
            || strategy == GetCourseStrategy.LOCAL_AND_NETWORK
            || strategy == GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY
        ) {
            courses = courseDao.getAllCourses(account, year, term)
            if (courses.isNotEmpty()) {
                emit(holidayChange(courses, localHolidays, localFirstWeek))
            }
        }
        if (strategy == GetCourseStrategy.NETWORK
            || strategy == GetCourseStrategy.LOCAL_AND_NETWORK
            || (strategy == GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY && courses.isEmpty())
        ) {
            courses = getCoursesNetwork(year, term)
            if (courses.isNotEmpty()) {
                emit(holidayChange(courses, localHolidays, localFirstWeek))
            }
        }
    }

    suspend fun getCourses(
        year: String,
        term: String,
        @GetCourseStrategy strategy: Int = GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY,
    ): List<NewCourse> = withContext(Dispatchers.IO) {
        if (year.isBlank() || term.isBlank()) {
            return@withContext emptyList()
        }
        val account = getUsingUser()?.account ?: return@withContext emptyList()
        val localFirstWeek = openingDayDao.find(year, term)
        val localHolidays = holidayDao.findAll()
        var courses = emptyList<NewCourse>()
        if (strategy == GetCourseStrategy.LOCAL
            || strategy == GetCourseStrategy.LOCAL_AND_NETWORK
            || strategy == GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY
        ) {
            courses = courseDao.getAllCourses(account, year, term)
            return@withContext holidayChange(courses, localHolidays, localFirstWeek)
        }
        if (strategy == GetCourseStrategy.NETWORK
            || strategy == GetCourseStrategy.LOCAL_AND_NETWORK
            || (strategy == GetCourseStrategy.NETWORK_IF_LOCAL_EMPTY && courses.isEmpty())
        ) {
            courses = getCoursesNetwork(year, term)
            if (courses.isNotEmpty()) {
                return@withContext holidayChange(courses, localHolidays, localFirstWeek)
            }
        }
        return@withContext emptyList<NewCourse>()
    }

    private fun holidayChange(
        courses: List<NewCourse>,
        holidays: List<Holiday>,
        firstWeek: String,
    ): List<NewCourse> {
        if (holidays.isEmpty() || courses.isEmpty()) {
            return courses
        }
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val openingDayInMill = format.parse(firstWeek)!!.time
        val calendar = Calendar.getInstance()

        /**
         * 计算放假的的时间（不需要上课的时间）
         */
        val inHoliday = ArrayList<WeekAndDay>()
        for (holiday in holidays) {
            val fromTime = format.parse(holiday.from)!!.time
            var week = DateUtils.getCurrentWeek(openingDayInMill, fromTime)
            if (week != -1) {
                calendar.timeInMillis = fromTime
                var weekday = calendar[Calendar.DAY_OF_WEEK]
                //将假期放假的每一天都加入数组
                repeat(holiday.days) {
                    inHoliday.add(WeekAndDay(week = week, weekday = weekday))
                    weekday += 1
                    if (weekday > 7) {
                        week += 1
                        weekday = 1
                    }
                }
            }
        }

        /**
         * 计算需要调课的时间，把[Pair.first]的课移动到[Pair.second]
         */
        val needChange = ArrayList<Pair<WeekAndDay, WeekAndDay>>()
        val changes = holidays.flatMap { it.changes.entries }
        for (change in changes) {
            val originTime = format.parse(change.key)!!.time
            val originWeek = DateUtils.getCurrentWeek(openingDayInMill, originTime)
            calendar.timeInMillis = originTime
            val originWeekday = calendar[Calendar.DAY_OF_WEEK]

            val toTime = format.parse(change.value)!!.time
            val toWeek = DateUtils.getCurrentWeek(openingDayInMill, toTime)
            calendar.timeInMillis = toTime
            val toWeekday = calendar[Calendar.DAY_OF_WEEK]

            needChange.add(
                Pair(
                    WeekAndDay(originWeek, originWeekday),
                    WeekAndDay(toWeek, toWeekday)
                )
            )
        }

        val newCourses = ArrayList<NewCourse>()
        //调课
        for (course in courses) {
            val cc = needChange.firstOrNull {
                course.weekday == it.first.weekday && course.weeks.contains(it.first.week)
            } ?: continue
            course.weeks.remove(cc.first.week)
            //直接创建新的课程
            val newCourse = NewCourse(
                id = course.id,
                name = "[调课]" + course.name,
                weeks = sortedSetOf(cc.second.week),
                weekday = cc.second.weekday,
                beginNode = course.beginNode,
                nodeLength = course.nodeLength,
                classroom = course.classroom,
                teacher = course.teacher,
                account = course.account
            )
            newCourses.add(newCourse)
        }

        //移除放假的课
        for (course in courses) {
            inHoliday.forEach { weekAndDay ->
                if (course.weekday == weekAndDay.weekday && course.weeks.contains(weekAndDay.week)) {
                    course.weeks.remove(weekAndDay.week)
                }
            }
        }

        return courses.dropLastWhile { it.weeks.isEmpty() } + newCourses
    }

    private suspend fun getTermOptionsFromNetworkInner(): TermOptions {
        val user = getUsingUser() ?: throw IllegalArgumentException("获取学期信息失败")
        val resp = service.getTermOptions(user)
        if (resp.code == IFResponse.SUCCESS && resp.data != null) {
            courseDao.saveOptions(resp.data.options)
            courseDao.saveDefaultOptions(resp.data.selected)
        }
        return resp.data ?: throw IllegalArgumentException("获取学期信息失败")
    }

    suspend fun getTimetableSetting(): SyllabusSetting {
        return withContext(Dispatchers.IO) {
            val account = getUsingAccount() ?: return@withContext SyllabusSetting()
            syllabusSettingDao.getTimetableSetting(account)
        }
    }

    suspend fun getCourseById(id: Int): NewCourse? {
        return withContext(Dispatchers.IO) {
            courseDao.getCourseById(id)
        }
    }

    suspend fun deleteCourse(course: NewCourse) {
        withContext(Dispatchers.IO) {
            courseDao.deleteCourseById(course.id)
        }
    }

    suspend fun saveCourse(course: NewCourse) {
        withContext(Dispatchers.IO) {
            courseDao.saveCourse(course)
        }
    }

    suspend fun getOpeningDay(year: String, term: String): OpeningDayVO =
        withContext(Dispatchers.IO) {
            val openingDay = openingDayDao.find(year, term)
            val currentTerm = courseDao.getOptions()
            val isC = currentTerm == null ||
                    currentTerm.selected.year == year &&
                    currentTerm.selected.term == term
            OpeningDayVO(
                year = year, term = term,
                openingDay = openingDay,
                isCurrentTerm = isC
            )
        }

    suspend fun getHolidays(): List<Holiday> {
        return withContext(Dispatchers.IO) {
            holidayDao.findAll()
        }
    }

    suspend fun saveTimetableSetting(setting: SyllabusSetting) {
        withContext(Dispatchers.IO) {
            syllabusSettingDao.saveSetting(setting)
        }
    }


}