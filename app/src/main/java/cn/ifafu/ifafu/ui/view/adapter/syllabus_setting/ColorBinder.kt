package cn.ifafu.ifafu.ui.view.adapter.syllabus_setting

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import me.drakeet.multitype.ItemViewBinder

class ColorBinder : ItemViewBinder<ColorItem, ColorBinder.VH>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH {
        return VH(inflater.inflate(R.layout.setting_item_color, parent, false))
    }

    override fun onBindViewHolder(holder: VH, item: ColorItem) {
        holder.setTitle(item.title)
                .setSubTitle(item.subtitle)
                .setColor(item.color)
                .setOnClickListener {
                    item.click.invoke(it)
                }
    }


    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val ivColor: ImageView = itemView.findViewById(R.id.iv_color)

        fun setColor(@ColorInt color: Int): VH {
            val grad = ivColor.background as GradientDrawable?
            grad?.setColor(color)
//            ivColor.setColorFilter(color)
            return this
        }

        fun setTitle(title: String): VH {
            tvTitle.text = title
            return this
        }

        fun setSubTitle(subtitle: String?): VH {
            if (subtitle.isNullOrEmpty()) {
                tvSubtitle.visibility = View.GONE
            } else {
                tvSubtitle.text = subtitle
            }
            return this
        }

        fun setOnClickListener(click :(ivColor: ImageView) -> Unit): VH {
            itemView.setOnClickListener {
                click.invoke(ivColor)
            }
            return this
        }

    }
}