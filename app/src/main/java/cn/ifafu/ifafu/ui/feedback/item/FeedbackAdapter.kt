package cn.ifafu.ifafu.ui.feedback.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.bean.dto.Feedback
import cn.ifafu.ifafu.databinding.FeedbackListItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FeedbackAdapter : RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    private val format = SimpleDateFormat("MM月dd日 hh:mm", Locale.getDefault())

    var data: List<Feedback> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FeedbackListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(binding: FeedbackListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val content = binding.content
        private val state = binding.tvState
        private val time = binding.time
        private val replyLayout = binding.layoutReply
        private val reply = binding.tvReply

        fun bind(feedback: Feedback) {
            content.text = feedback.content
            time.text = format.format(feedback.date)
            if (feedback.reply != null) {
                state.text = ("已回复")
                reply.text = feedback.reply.content
                replyLayout.visibility = View.VISIBLE
            } else {
                state.text = ("处理中")
                replyLayout.visibility = View.GONE
            }
        }

    }
}