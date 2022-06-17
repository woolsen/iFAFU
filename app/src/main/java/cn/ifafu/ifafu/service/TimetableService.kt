package cn.ifafu.ifafu.service

import cn.ifafu.ifafu.bean.bo.CourseBO
import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.bean.dto.TermOptions
import cn.ifafu.ifafu.bean.dto.TimetableDTO
import cn.ifafu.ifafu.di.Ifafu
import cn.ifafu.ifafu.entity.NewCourse
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.service.common.ZFHttpClient
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.service.parser.OptionParser
import cn.ifafu.ifafu.service.parser.TimetableParser
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.runInterruptible
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject

class TimetableService @Inject constructor(
    private val client: ZFHttpClient,
    @Ifafu private val retrofit: Retrofit
) {

    private val mOptionParser = OptionParser()

    private val service = retrofit.create(HtmlParseService::class.java)

    /**
     * 获取学期选项
     * @param user 用户信息
     * @return [TermOptions]
     */
    suspend fun getTermOptions(user: User): IFResponse<TermOptions> {
        val url = ZfUrlProvider.getUrl(ZFApiEnum.TIMETABLE, user)
        val referer = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
        return client.ensureLogin(user) {
            val resp = client.parseHtml(client.get(url, referer), checkLogin = true) { html ->
                try {
                    val option = mOptionParser.parse(html)
                    IFResponse.success(option)
                } catch (e: Exception) {
                    e.printStackTrace()
                    IFResponse.failure("选项解析错误")
                }
            }
            // 金山学院假期期间会出现"Object moved"，会被解析器解析为未登录，所以
            // 这边需要做特别判断
            if (user.school == User.FAFU_JS && resp.code == IFResponse.NO_AUTH) {
                IFResponse.failure("暂时无法查询")
            } else {
                resp
            }
        }
    }

    /**
     * 获取课表
     * @param user 用户信息
     * @param year 学年选项
     * @param term 学期选项
     * @return [TimetableDTO]
     */
    suspend fun getTimetable(
        user: User,
        year: String,
        term: String,
    ): IFResponse<List<NewCourse>> {
        val url = ZfUrlProvider.getUrl(ZFApiEnum.TIMETABLE, user)
        val referer = ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user)
        return client.ensureLogin(user) {
            client.needParams(url, referer, checkLogin = true) { paramHtml, hiddenParams ->
                val defaultOptions = try {
                    mOptionParser.parse(paramHtml)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@needParams IFResponse.failure("课表选项解析失败")
                }.selected
                val defaultYear = defaultOptions.year
                val defaultTerm = defaultOptions.term

                // 如果需要获取的课表是教务管理系统上默认显示的课表
                // 则直接解析当前html。否则通过POST获取相应课表
                val html = if (year == defaultYear && term == defaultTerm) {
                    paramHtml
                } else {
                    val params = HashMap<String, String>()
                    params.putAll(hiddenParams)
                    params["xnd"] = year
                    params["xqd"] = term

                    runInterruptible {
                        client.post(url, referer, params).body?.string()
                    } ?: return@needParams IFResponse.failure("获取课表页面失败")
                }
                val courses = parseCourseHtml(html).map {
                    NewCourse(
                        name = it.name,
                        teacher = it.teacher,
                        classroom = it.classroom,
                        weekday = (it.weekday % 7) + 1,
                        weeks = it.weeks.toSortedSet(),
                        nodeLength = it.nodeLength,
                        beginNode = it.beginNode,
                        account = user.account,
                        term = term,
                        year = year,
                    ).apply { this.id = it.hashCode() }
                }
                IFResponse.success(courses)
            }
        }
    }

    private suspend fun parseCourseHtml(html: String): List<CourseBO> {
        val courses = try {
            service.parseCourseHtml(html).data
        } catch (e: Exception) {
            Timber.e(e, "通过网络解析课表失败")
            null
        }
        return if (courses == null) {
            val timetableParser = TimetableParser()
            timetableParser.parse(html)
        } else {
            courses.data
        }
    }

}