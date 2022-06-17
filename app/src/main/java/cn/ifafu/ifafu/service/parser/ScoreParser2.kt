package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.entity.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class ScoreParser2(user: User) : BaseParser<IFResponse<List<Score>>>() {

    private val account: String = user.account

    override fun parse(html: String): IFResponse<List<Score>> {
        check(html)
        try {
            //定位到成绩的表格
            val table = Jsoup.parse(html)
                    .select("table[id=\"Datagrid1\"]")
                    .getOrElse(0) { return IFResponse.failure("成绩获取失败") }
                    .getElementsByTag("tr")
            val matching = match(table[0])
            val list = table.drop(1)
                    .map { it.children() }
                    .map { it.textNodes().map { n -> n.text() } }
                    .map { paresToScore(matching, it) }
            return IFResponse.success(list.sortedBy { it.id })
        } catch (e: Exception) {
            e.printStackTrace()
            return IFResponse.failure("成绩解析出错")
        }
    }

    /**
     * 金山学院格式：
     *  <tr class="datelisthead">
     *      <td>学年</td>     //0
     *      <td>学期</td>     //1
     *      <td>课程代码</td>  //2
     *      <td>课程名称</td>  //3
     *      <td>课程性质</td>  //4
     *      <td>课程归属</td>  //5
     *      <td>学分</td>     //6
     *      <td>期中成绩</td>  //7
     *      <td>期末成绩</td>  //8
     *      <td>实验成绩</td>  //9
     *      <td>成绩</td>     //10
     *      <td>补考成绩</td>  //11
     *      <td>是否重修</td>  //12
     *      <td>开课学院</td>  //13
     *      <td>备注</td>     //14
     *      <td>补考备注</td>  //15
     *  </tr>
     */
    private fun paresToScore(matching: IntArray, elements: List<String>): Score {
        val score = Score()
//        println(elements)
        score.account = account
        score.year = elements[matching[0]]
        score.term = elements[matching[1]]
        score.name = elements[matching[2]]
        score.nature = elements[matching[3]]
        score.attr = elements[matching[4]]
        score.credit = elements[matching[5]].toFloatOrNull() ?: -1F
        val ele7 = elements[matching[6]]
        if (ele7.contains("免修")) {
            score.score = Score.FREE_COURSE
        } else {
            score.score = ele7.toFloatOrNull() ?: -1F
        }
        score.makeupScore = elements[matching[7]].toFloatOrNull() ?: -1F
        score.restudy = elements[matching[8]].isEmpty()
        score.institute = elements[matching[12]]
        score.remarks = elements[matching[9]]
        score.makeupRemarks = elements[matching[10]]
        score.gpa = elements[matching[11]].toFloatOrNull() ?: -1F
        score.isIESItem = !(score.score == Score.FREE_COURSE
                || score.nature.contains("任意选修")
                || score.nature.contains("公共选修")
                || score.name.contains("体育"))
        score.id = score.hashCode()
        return score
    }

    private val matchTitles = arrayOf(
        "学年",  //0
        "学期",  //1
        "课程名称",  //2
        "课程性质",  //3
        "课程归属",  //4
        "学分",  //5
        "成绩",  //6
        "补考成绩",  //7
        "是否重修",  //8
        "备注",  //9
        "补考备注",  //10
        "绩点|gpa|GPA",  //11
        "开课学院"  //12
    ).map { it.toRegex() }

    /**
     * 用于匹配标题栏的成绩属性，以便于对号入座
     * 通过调整数组的值，来避免属性无法对号入座问题
     *
     * @return 数组的值代表着相对应属性[matchTitles]所在的下标位置
     */
    private fun match(element: Element): IntArray {
        val matching = IntArray(15) { 0 }
        val titles = element.children().map { it.text() }
        titles.forEachIndexed { index1, title ->
            for ((index2, regex) in matchTitles.withIndex()) {
                if (title.matches(regex)) {
                    matching[index2] = index1
                    break
                }
            }
        }
        return matching
    }
}