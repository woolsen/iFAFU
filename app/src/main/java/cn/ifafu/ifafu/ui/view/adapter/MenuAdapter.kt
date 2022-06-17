package cn.ifafu.ifafu.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.bean.vo.MenuVO
import cn.ifafu.ifafu.databinding.MainNewTabItemBinding

class MenuAdapter(
    private val listener: (menu: MenuVO) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    var data: LinkedHashSet<MenuVO> = LinkedHashSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MainNewTabItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data.elementAt(position))
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(
        binding: MainNewTabItemBinding,
        private val listener: (menu: MenuVO) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val iconIv = binding.icon
        private val titleTv = binding.title

        fun bind(menu: MenuVO) {
            itemView.setOnClickListener {
                listener.invoke(menu)
            }
            iconIv.setImageResource(menu.icon)
            titleTv.text = menu.title
        }

    }
}