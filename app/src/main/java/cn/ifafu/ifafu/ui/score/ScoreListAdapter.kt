package cn.ifafu.ifafu.ui.score

import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.entity.Score
import cn.ifafu.ifafu.util.GlobalLib
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ScoreListAdapter : BaseQuickAdapter<Score, BaseViewHolder>(R.layout.score_list_item) {

    override fun convert(holder: BaseViewHolder, item: Score) {
        holder.setText(R.id.tv_score_name, item.name)
        val calcScore = item.realScore
        if (calcScore == Score.FREE_COURSE) {
            holder.setText(R.id.tv_score, "免修")
        } else {
            holder.setText(R.id.tv_score, GlobalLib.formatFloat(calcScore, 2))
        }
        if (calcScore >= 60 || calcScore == Score.FREE_COURSE) {
            holder.setTextColor(R.id.tv_score, ContextCompat.getColor(context, R.color.ifafu_blue))
        } else {
            holder.setTextColor(R.id.tv_score, ContextCompat.getColor(context, R.color.red))
        }
        when {
            calcScore == Score.FREE_COURSE ->
                holder.setImageResource(R.id.iv_tip, R.drawable.ic_score_mian)
            item.name.contains("体育") ->
                holder.setImageResource(R.id.iv_tip, R.drawable.ic_score_ti)
            item.nature.contains("任意选修") || item.nature.contains("公共选修") ->
                holder.setImageResource(R.id.iv_tip, R.drawable.ic_score_xuan)
            calcScore < 60 ->
                holder.setImageResource(R.id.iv_tip, R.drawable.ic_score_warm)
            else ->
                holder.setImageDrawable(R.id.iv_tip, null)
        }
        holder.itemView.setOnClickListener { v ->
            val action = ScoreListFragmentDirections
                .actionFragmentScoreListToFragmentScoreDetail(item)
            v.findNavController().navigate(action)
        }
    }


}