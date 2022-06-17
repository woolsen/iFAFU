package cn.ifafu.ifafu.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.ui.view.SmoothCheckBox
import cn.ifafu.ifafu.util.GlobalLib

class ScoreFilterAdapter(context: Context, private val onCheckedChangeListener: ((score: Score, isChecked: Boolean) -> Unit))
    : RecyclerView.Adapter<ScoreFilterAdapter.ViewHolder>() {

    var data: List<Score> = ArrayList()

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.score_filter_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val score = data[position]
        holder.titleTV.text = score.name
        score.realScore.run {
            if (this == Score.FREE_COURSE) {
                holder.scoreTV.text = "免修"
            } else {
                holder.scoreTV.text = GlobalLib.formatFloat(this, 2) + "分"
            }
        }
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setChecked(score.isIESItem, false)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChangeListener.invoke(score, isChecked)
        }
        holder.itemView.setOnClickListener {
            holder.checkBox.setChecked(!holder.checkBox.isChecked, true)
        }
    }

    fun setAllChecked() {
        for (score in data) {
            score.isIESItem = true
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTV: TextView = itemView.findViewById(R.id.tv_score_name)
        val scoreTV: TextView = itemView.findViewById(R.id.tv_score)
        val checkBox: SmoothCheckBox = itemView.findViewById(R.id.checkbox)
    }
}
