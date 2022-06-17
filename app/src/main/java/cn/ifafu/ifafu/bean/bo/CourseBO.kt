package cn.ifafu.ifafu.bean.bo

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * @author KQiang Weng
 * @since 2021/12/23 00:30
 */
class CourseBO(

    @SerializedName("name")
    var name: String,

    @SerializedName("teacher")
    var teacher: String,

    @SerializedName("address")
    var classroom: String,

    @SerializedName("weeks")
    var weeks: SortedSet<Int> = TreeSet(),

    /**
     * - 周一: 1
     * - 周二: 2
     * - 周三: 3
     * - 周四: 4
     * - 周五: 5
     * - 周六: 6
     * - 周天: 7
     */
    @SerializedName("weekday")
    var weekday: Int = 0,

    @SerializedName("startNode")
    var beginNode: Int = 0, // 开始节数 = 0

    @SerializedName("nodeCount")
    var nodeLength: Int = 0,
)