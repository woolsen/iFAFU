package cn.ifafu.ifafu.ui.view.adapter

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.util.DensityUtils
import java.util.*

class WeekItemAdapter(private val context: Context) : RecyclerView.Adapter<WeekItemAdapter.VH>() {
    var weekList: SortedSet<Int> = TreeSet()
    private var listener: OnItemClickListener? = null
    var editMode = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(context)
        val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(context, 64f))
        val px1 = DensityUtils.dp2px(context, 1f)
        params.setMargins(px1 shr 1, px1, px1 shr 1, 0)
        tv.layoutParams = params
        tv.gravity = Gravity.CENTER
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tv.setTextColor(Color.WHITE)
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.textView.text = (position + 1).toString()
        if (weekList.contains(position + 1)) {
            holder.textView.setBackgroundResource(R.color.ifafu_blue)
        } else {
            holder.textView.setBackgroundResource(R.color.light_gray)
        }
        holder.textView.setOnClickListener {
            listener?.onItemClick(position)
            if (!editMode) return@setOnClickListener
            if (weekList.contains(position + 1)) {
                weekList.remove(position + 1)
                holder.textView.setBackgroundResource(R.color.light_gray)
            } else {
                weekList.add(position + 1)
                holder.textView.setBackgroundResource(R.color.ifafu_blue)
            }
        }
    }

    override fun getItemCount(): Int {
        return 20
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView as TextView
    }

}