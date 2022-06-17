package cn.ifafu.ifafu.ui.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.ui.view.adapter.ScoreAdapter.ScoreViewHolder
import cn.ifafu.ifafu.util.ColorUtils
import cn.ifafu.ifafu.util.GlobalLib

class ScoreAdapter(private val mContext: Context) : RecyclerView.Adapter<ScoreViewHolder>() {

    var scoreList: List<Score> = ArrayList()

    private var mClickListener: ((View, Score) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.score_list_item, parent, false)
        return ScoreViewHolder(view)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scoreList[position]
        holder.tvName.text = score.name
        val calcScore = score.realScore
        if (calcScore == Score.FREE_COURSE) {
            holder.tvScore.text = "免修"
        } else {
            holder.tvScore.text = GlobalLib.formatFloat(calcScore, 2)
        }
        if (calcScore >= 60 || calcScore == Score.FREE_COURSE) {
            holder.tvScore.setTextColor(ColorUtils.getColor(mContext, R.color.ifafu_blue))
        } else {
            holder.tvScore.setTextColor(ColorUtils.getColor(mContext, R.color.red))
        }
        when {
            calcScore == Score.FREE_COURSE ->
                holder.ivTip.setImageResource(R.drawable.ic_score_mian)
            score.name.contains("体育") ->
                holder.ivTip.setImageResource(R.drawable.ic_score_ti)
            score.nature.contains("任意选修") || score.nature.contains("公共选修") ->
                holder.ivTip.setImageResource(R.drawable.ic_score_xuan)
            calcScore < 60 ->
                holder.ivTip.setImageResource(R.drawable.ic_score_warm)
            else ->
                holder.ivTip.setImageDrawable(null)
        }
        mClickListener?.let { listener ->
            holder.itemView.setOnClickListener { v: View? ->
                v?.let { view ->
                    listener.invoke(view, score)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return scoreList.size
    }

    fun setOnScoreClickListener(listener: (View, Score) -> Unit) {
        mClickListener = listener
    }

    class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_score_name)
        val tvScore: TextView = itemView.findViewById(R.id.tv_score)
        val ivTip: ImageView = itemView.findViewById(R.id.iv_tip)
    }

}