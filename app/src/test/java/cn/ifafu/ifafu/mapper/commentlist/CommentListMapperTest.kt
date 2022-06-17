package cn.ifafu.ifafu.mapper.commentlist

import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.service.mapper.commentlist.CommentListListMapper
import cn.ifafu.ifafu.util.FileUtils
import org.junit.Assert
import org.junit.Test

class CommentListMapperTest {

    private val mapper = CommentListListMapper()

    @Test
    fun testAlready() {
        val html =
            FileUtils.readRelative(this.javaClass, "/html/fafu_already_comment.html", "utf-8")
        val response = mapper.map(html)
        Assert.assertEquals("您已经评价过！", response.message)
    }

    @Test
    fun testHalf01() {
        val html = FileUtils.readRelative(this.javaClass, "/html/fafu_half_comment01.html", "utf-8")
        val response = mapper.map(html)
        Assert.assertEquals(200, response.code)
        Assert.assertNotNull(response.data)
        val data = response.data!!
        val except = listOf(
            CommentItem(
                courseName = "海洋科学导论【课堂教学】",
                teacherName = "李晓东",
                commentFullUrl = "xsjxpj2_2.aspx?zgh=20180024&xkkh=(2020-2021-1)-55016077-20180024-1&xh=3175505020&bsx=ll",
                isDone = true
            ),
            CommentItem(
                courseName = "海藻与海藻栽培学【课堂教学】",
                teacherName = "褚瑶瑶",
                commentFullUrl = "xsjxpj2_2.aspx?zgh=20200005&xkkh=(2020-2021-1)-55017017-20200005-1&xh=3175505020&bsx=ll",
                isDone = false
            ),
            CommentItem(
                courseName = "水产品加工工艺学【课堂教学】",
                teacherName = "陈丽娇",
                commentFullUrl = "xsjxpj2_2.aspx?zgh=19820007&xkkh=(2020-2021-1)-58016030-19820007-1&xh=3175505020&bsx=ll",
                isDone = false
            ),
            CommentItem(
                courseName = "水产药物与药理学【课堂教学】",
                teacherName = "梅景良",
                commentFullUrl = "xsjxpj2_2.aspx?zgh=19910045&xkkh=(2020-2021-1)-55016048-19910045-1&xh=3175505020&bsx=ll",
                isDone = false
            ),
            CommentItem(
                courseName = "畜牧学【课堂教学】",
                teacherName = "高玉云",
                commentFullUrl = "xsjxpj2_2.aspx?zgh=20140179&xkkh=(2020-2021-1)-55016019-20140179-1&xh=3175505020&bsx=ll",
                isDone = false
            ),
            CommentItem(
                courseName = "畜牧学【课堂教学】",
                teacherName = "刘庆华",
                commentFullUrl = "xsjxpj2_2.aspx?zgh=19950046&xkkh=(2020-2021-1)-55016019-20140179-1&xh=3175505020&bsx=ll",
                isDone = false
            )
        )
        Assert.assertArrayEquals(
            except.sortedBy { it.commentFullUrl }.toTypedArray(),
            data.sortedBy { it.commentFullUrl }.toTypedArray()
        )
    }

    @Test
    fun fafuJS() {

    }


}