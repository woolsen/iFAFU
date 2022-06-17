package cn.ifafu.ifafu.ui.view.adapter.syllabus_setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.view.SmoothCheckBox
import me.drakeet.multitype.ItemViewBinder

class CheckBoxBinder
    : ItemViewBinder<CheckBoxItem, CheckBoxBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.setting_item_checkbox, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: CheckBoxItem) {
        holder.tvTitle.text = item.title
        if (item.tip.isNotBlank()) {
            holder.tvTip.visibility = View.VISIBLE
            holder.tvTip.text = item.tip
        } else {
            holder.tvTip.visibility = View.GONE
        }
        holder.checkBox.setChecked(item.checked, false)
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            holder.checkBox.setChecked(holder.checkBox.isChecked, true)
            item.listener.invoke(holder.checkBox.isChecked)
        }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.listener.invoke(isChecked)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle : TextView = itemView.findViewById(R.id.tv_title)
        val tvTip: TextView = itemView.findViewById(R.id.tv_tip)
        val checkBox: SmoothCheckBox = itemView.findViewById(R.id.checkbox)
    }
}