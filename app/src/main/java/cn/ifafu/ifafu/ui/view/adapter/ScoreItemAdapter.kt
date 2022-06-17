package cn.ifafu.ifafu.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ScoreItemAdapter(private val data: List<Pair<String, String>>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.score_detail_item, parent, false)
        view.findViewById<TextView>(R.id.tv_value).transitionName =
            context.getString(R.string.transition_name_score_title)
        return BaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = data[position]
        holder.setText(R.id.tv_title, item.first)
            .setText(R.id.tv_value, item.second)
    }

    override fun getItemCount(): Int = data.size


}