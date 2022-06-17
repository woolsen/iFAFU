package cn.ifafu.ifafu.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.entity.Exam
import cn.ifafu.ifafu.databinding.ExamListItemBinding
import cn.ifafu.ifafu.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class ExamAdapter(
    var data: List<Exam> = emptyList(),
//    private val onClick: (ExamListItemBinding, Exam) -> Unit
) : RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {

    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val format2 = SimpleDateFormat("HH:mm", Locale.CHINA)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exam_list_item, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = this.data[position]
        holder.bind(exam)
    }

    override fun getItemCount(): Int {
        return this.data.size
    }

    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ExamListItemBinding.bind(itemView)

        fun bind(exam: Exam) {
            val start = Calendar.getInstance()
            start.time = Date(exam.startTime)
            val weekday = DateUtils.getWeekdayCN(start.get(Calendar.DAY_OF_WEEK))

//            binding.root.setOnClickListener {
//                onClick(binding, exam)
//            }

            binding.tvExamTime.text = if (exam.endTime == 0L) {
                "暂无考试时间"
            } else {
                String.format(
                    "%s (%s %s~%s)",
                    format.format(Date(exam.startTime)),
                    weekday,
                    format2.format(Date(exam.startTime)),
                    format2.format(Date(exam.endTime))
                )
            }

            binding.name.text = exam.name
            binding.tvExamAddress.text = String.format("%s   %s", exam.address, exam.seatNumber)
            when {
                exam.endTime == 0L -> {
                    binding.tvExamLast.text = "未知"
                }
                exam.endTime < System.currentTimeMillis() -> {
                    binding.tvExamLast.setText(R.string.exam_over)
                }
                else -> {
                    binding.tvExamLast.text = String.format(
                        "剩余%s",
                        DateUtils.calcIntervalTime(System.currentTimeMillis(), exam.startTime)
                    )
                }
            }
        }
    }
}
