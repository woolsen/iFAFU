package cn.ifafu.ifafu.ui.timetable

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.bean.vo.OpeningDayVO
import cn.ifafu.ifafu.bean.vo.TimetableVO
import cn.ifafu.ifafu.entity.SyllabusSetting
import cn.ifafu.ifafu.ui.view.timetable.TimetableItem
import cn.ifafu.ifafu.ui.view.timetable.TimetableView
import cn.ifafu.ifafu.util.DateUtils
import java.util.*

class TimetablePageAdapter(
    private val onItemClickListener: (View, TimetableItem) -> Unit,
    private val onItemLongClickListener: (View, TimetableItem) -> Unit
) : RecyclerView.Adapter<TimetablePageAdapter.SyllabusViewHolder>() {

    /**
     * 按周分类
     */
    private var data: TimetableVO? = null
    private var dataVersion = 0

    private var setting: SyllabusSetting? = null
    private var settingVersion = 0 //用于记录setting的版本

    private var openingDay: OpeningDayVO? = null
    private var openingDayVersion = 0

    fun updateTimetable(timetable: TimetableVO) {
        this.data = timetable
        dataVersion++
        notifyDataSetChanged()
    }

    fun updateSetting(setting: SyllabusSetting) {
        this.setting = setting
        settingVersion++
        notifyDataSetChanged()
    }

    fun updateOpeningDay(openingDay: OpeningDayVO) {
        this.openingDay = openingDay
        openingDayVersion++
        notifyDataSetChanged()
    }

    //将View和ViewHolder绑定在一起
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyllabusViewHolder {
        val timetable = TimetableView(parent.context)
        timetable.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        timetable.setItemClickListener { v, item ->
            onItemClickListener.invoke(v, item)
        }
        timetable.setItemLongClickListener { v, item ->
            onItemLongClickListener.invoke(v, item)
        }
        return SyllabusViewHolder(timetable)
    }

    //将数据显示在View上
    override fun onBindViewHolder(holder: SyllabusViewHolder, position: Int) {
        val setting = setting
        if (setting != null) {
            if (holder.settingVersion < settingVersion) {
                holder.timetable.config = holder.timetable.config.apply {
                    totalNodeCount = setting.totalNode
                    showTime = setting.showBeginTimeText
                    showHorizontalLine = setting.showHorizontalLine
                    showVerticalLine = setting.showVerticalLine
                    itemTextSize = setting.textSize.toFloat()
                    otherTextColor = setting.themeColor
                }
                holder.timetable.setTimeText(setting.getBeginTimeText().drop(1).toTypedArray())
                holder.settingVersion = settingVersion
            }
        }

        if (holder.dataVersion != dataVersion || holder.showPosition != position) {
            holder.dataVersion = dataVersion
            val courseList = data?.getWeek(position + 1)
            holder.timetable.setItems(courseList)
        }

        if (holder.openingDayVersion != openingDayVersion || holder.showPosition != position) {
            holder.openingDayVersion = openingDayVersion
            val d = openingDay?.getOpeningDay()
            if (d == null) {
                holder.timetable.setMonth(-1)
                holder.timetable.setDateTexts(null)
            } else {
                val dates = DateUtils.getWeekOffsetDates(d, position, "d")
                val month = Calendar.getInstance().apply {
                    time = d
                }.get(Calendar.MONTH) + 1
                holder.timetable.setMonth(month)
                holder.timetable.setDateTexts(dates.toTypedArray())
            }
        }
        holder.showPosition = position
    }

    override fun getItemCount(): Int {
        return setting?.weekCnt ?: 0
    }

    class SyllabusViewHolder(itemView: TimetableView) : RecyclerView.ViewHolder(itemView) {
        val timetable: TimetableView = itemView
        var showPosition = -1

        var openingDayVersion = -1
        var dataVersion = -1
        var settingVersion = -1
    }

}