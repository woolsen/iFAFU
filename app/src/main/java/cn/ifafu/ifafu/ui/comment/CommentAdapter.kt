package cn.ifafu.ifafu.ui.comment

import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.bo.CommentItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

@Suppress("DEPRECATION")
class CommentAdapter(val click: (item: CommentItem) -> Unit) :
    BaseQuickAdapter<CommentItem, BaseViewHolder>(R.layout.comment_item) {

    override fun convert(holder: BaseViewHolder, item: CommentItem) {
        holder.run {
            setText(R.id.tv_course_name, item.courseName)
            setText(R.id.tv_teacher_name, item.teacherName)
            val isDone = item.isDone
            if (isDone == null) {
                setText(R.id.tv_status, "")
            } else if (isDone) {
                setText(R.id.tv_status, "已评教")
                setTextColor(R.id.tv_status, context.resources.getColor(R.color.ifafu_blue))
            } else {
                setText(R.id.tv_status, "未评教")
                setTextColor(R.id.tv_status, context.resources.getColor(R.color.red))
            }
            itemView.setOnClickListener {
                click.invoke(item)
            }
        }
    }

//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val status = itemView.findViewById<TextView>(R.id.tv_status)
//        private val teacher = itemView.findViewById<TextView>(R.id.tv_teacher_name)
//        private val course = itemView.findViewById<TextView>(R.id.tv_course_name)
//
//        fun bind(item: CommentItem) {
//            val isDone = item.isDone
//            if (isDone == null) {
//                status.text = ""
//            } else if (isDone) {
//                status.text = "已评教"
//                status.setTextColor(context.resources.getColor(R.color.ifafu_blue))
//            } else {
//                status.text = "未评教"
//                status.setTextColor(context.resources.getColor(R.color.red))
//            }
//            teacher.text = item.teacherName
//            course.text = item.courseName
//            itemView.setOnClickListener {
//                click.invoke(item)
//            }
//        }
//    }
}
