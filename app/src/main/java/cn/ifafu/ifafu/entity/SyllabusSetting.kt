package cn.ifafu.ifafu.entity

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
class SyllabusSetting {
    @PrimaryKey
    var account: String = ""
    var weekCnt = 20 //总共周数
    var totalNode = 12 //每日课程节数
    var showSaturday = true //显示周六
    var showSunday = true //显示周日
    var showBeginTimeText = true //显示侧边栏时间
    var showHorizontalLine = true //显示水平分割线
    var showVerticalLine = true //显示竖直分割线
    var openingDay = "2020-02-16" //开学时间
        @SuppressLint("SimpleDateFormat")
        set(value) {
            if (openingDay != value) {
                //验证是否合法
                SimpleDateFormat("yyyy-MM-dd").parse(value)
                field = value
            }
        }
    var nodeLength = 45 //一节课的时间
    var firstDayOfWeek = Calendar.SUNDAY //每周的第一天
    var background: String = "" //课表背景
    var textSize = 12 //课程字体大小
    var themeColor = Color.BLACK //主题颜色
    var statusDartFont = true //状态栏深色字体
    var isForceRefresh = false // 每次进入课表，自动刷新课表
    var parseType = 1 //1:本地解析   2：网络解析
    var beginTime: List<Int> = intBeginTime[0]

    @Ignore
    constructor(account: String) {
        this.account = account
    }

    constructor()

    fun getBeginTimeText(): List<String> {
        return beginTime.map {
            "%d:%02d".format(it / 100, it % 100)
        }.toList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as SyllabusSetting
        return weekCnt == that.weekCnt &&
                totalNode == that.totalNode &&
                showSaturday == that.showSaturday &&
                showSunday == that.showSunday &&
                showBeginTimeText == that.showBeginTimeText &&
                showHorizontalLine == that.showHorizontalLine &&
                showVerticalLine == that.showVerticalLine &&
                nodeLength == that.nodeLength &&
                firstDayOfWeek == that.firstDayOfWeek &&
                textSize == that.textSize &&
                themeColor == that.themeColor &&
                statusDartFont == that.statusDartFont &&
                isForceRefresh == that.isForceRefresh &&
                account == that.account &&
                openingDay == that.openingDay &&
                background == that.background &&
                beginTime == that.beginTime
    }

    override fun hashCode(): Int {
        return Objects.hash(
            account,
            weekCnt,
            totalNode,
            showSaturday,
            showSunday,
            showBeginTimeText,
            showHorizontalLine,
            showVerticalLine,
            openingDay,
            nodeLength,
            firstDayOfWeek,
            background,
            textSize,
            themeColor,
            statusDartFont,
            isForceRefresh,
            beginTime
        )
    }

    companion object {
        //0:本部上课时间  1:旗山上课时间
        @JvmField
        var intBeginTime = arrayOf(
            listOf(0, 800, 850, 955, 1045, 1135, 1400, 1450, 1550, 1640, 1825, 1915, 2005),
            listOf(0, 820, 910, 1010, 1100, 1150, 1410, 1500, 1555, 1645, 1825, 1915, 2005)
        )
    }
}