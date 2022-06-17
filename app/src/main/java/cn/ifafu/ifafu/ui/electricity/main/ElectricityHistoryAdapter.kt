package cn.ifafu.ifafu.ui.electricity.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.databinding.ElectricityHistoryItemBinding
import java.text.SimpleDateFormat
import java.util.*

class ElectricityHistoryAdapter :
    ListAdapter<ElectricityHistoryVO, ElectricityHistoryViewHolder>(ElectricityHistoryDiff) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ElectricityHistoryViewHolder {
        val binding = ElectricityHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ElectricityHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ElectricityHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class ElectricityHistoryViewHolder(
    private val binding: ElectricityHistoryItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val format = SimpleDateFormat("M月dd日 HH:mm:ss", Locale.getDefault())

    fun bind(history: ElectricityHistoryVO) {
        binding.balance = "电费余额:%s %s".format(history.balance, history.unit)
        binding.date = format.format(Date(history.time))
        binding.diff = history.diff
        binding.unit = history.unit
    }
}

object ElectricityHistoryDiff : DiffUtil.ItemCallback<ElectricityHistoryVO>() {
    override fun areItemsTheSame(
        oldItem: ElectricityHistoryVO,
        newItem: ElectricityHistoryVO
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: ElectricityHistoryVO,
        newItem: ElectricityHistoryVO
    ): Boolean {
        return oldItem == newItem
    }
}

@BindingAdapter("electricityDiff")
fun electricityDiff(textView: TextView, diff: Double) {
    textView.text = ("%+.2f".format(diff))
}