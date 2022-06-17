package cn.ifafu.ifafu.service.parser

import cn.ifafu.ifafu.bean.dto.IFResponse
import cn.ifafu.ifafu.entity.Exam
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.util.getInts
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.ArrayList

class ExamParser2(user: User) : BaseParser<IFResponse<List<Exam>>>() {

    private val account: String = user.account

    override fun parse(html: String): IFResponse<List<Exam>> {
        val document = Jsoup.parse(html)
        val elementsTemp = document.select("table[id=\"DataGrid1\"]")
        if (elementsTemp.size == 0) {
            return IFResponse.success(ArrayList())
        }
        val elements = elementsTemp[0].getElementsByTag("tr")
        val list = ArrayList<Exam>()
        val termAndYear = document.select("option[selected=\"selected\"]")
        val year = termAndYear[0].text()
        val term = termAndYear[1].text()
        for (i in 1 until elements.size) {
            val exam = getExam(elements[i].children())
            exam.term = term
            exam.year = year
            list.add(exam)
        }
        return IFResponse.success(list)
    }

    private fun getExam(e: Elements): Exam {
        val exam = Exam()

        val numbers = e[3].text().getInts()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (numbers.size > 6) {
            calendar.set(Calendar.YEAR, numbers[0])
            calendar.set(Calendar.MONTH, numbers[1] - 1)
            calendar.set(Calendar.DAY_OF_MONTH, numbers[2])

            val start = calendar.apply {
                set(Calendar.HOUR_OF_DAY, numbers[3])
                set(Calendar.MINUTE, numbers[4])
            }.time.time

            val end = calendar.apply {
                set(Calendar.HOUR_OF_DAY, numbers[5])
                set(Calendar.MINUTE, numbers[6])
            }.time.time

            exam.startTime = start
            exam.endTime = end
        }


        exam.id = e[0].text().hashCode()
        exam.name = e[1].text()
        exam.address = if (e.size > 4) e[4].text() else ""
        exam.seatNumber = if (e.size > 6) e[6].text().run {
            if (this.matches("[0-9]+".toRegex())) this + "号"
            else this
        } else ""
        exam.account = account

        return exam
    }

}
