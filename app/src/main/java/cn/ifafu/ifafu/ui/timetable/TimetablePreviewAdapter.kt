package cn.ifafu.ifafu.ui.timetable

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.TimetablePreviewSource
import cn.ifafu.ifafu.databinding.TimetableItemPreviewBinding
import cn.ifafu.ifafu.ui.view.TimetablePreviewView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimetablePreviewAdapter(
    private val onSelectedCheckListener: (year: String, term: String) -> Unit
) : RecyclerView.Adapter<TimetablePreviewAdapter.ViewHolder>() {

    var data: List<TimetablePreviewSource> = ArrayList()
        set(value) {
            selected = value.find { it.selected }
            field = value
        }

    private val scope = CoroutineScope(Dispatchers.Main)

    private var selected: TimetablePreviewSource? = null
    private var selectedViewHolder: ViewHolder? = null

    fun getSelected(): TimetablePreviewSource? {
        return selected
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TimetableItemPreviewBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vo = data[position]
        val title = "${vo.year}学年\n第${vo.term}学期"
        holder.titleTv.text = title
        if (vo.selected) {
            selectedViewHolder = holder
        }
        scope.launch {
            val res = vo.get()
            holder.previewView.setItems(res)
        }
        holder.select(vo.selected)
        holder.itemView.setOnClickListener {
            if (vo != selected) {
                selectedViewHolder?.select(false)
                holder.select(true)
                selectedViewHolder = holder
                onSelectedCheckListener.invoke(vo.year, vo.term)
                selected = vo
            }
        }
    }

    class ViewHolder(binding: TimetableItemPreviewBinding) : RecyclerView.ViewHolder(binding.root) {

        val titleTv: TextView = binding.titleTv
        val previewView: TimetablePreviewView = binding.previewImage

        init {
            Glide.with(itemView)
                .load(R.drawable.bg_timetable)
                .into(previewView)
        }

        fun select(select: Boolean) {
            titleTv.paint.isFakeBoldText = select
            titleTv.invalidate()
        }
    }
}