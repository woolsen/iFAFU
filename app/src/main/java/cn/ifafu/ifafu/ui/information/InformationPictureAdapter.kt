package cn.ifafu.ifafu.ui.information

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.databinding.ItemPictureBinding

class InformationPictureAdapter(
    private val onPictureClick: (View, String) -> Unit
) : RecyclerView.Adapter<InformationPictureViewHolder>() {

    val data: MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationPictureViewHolder {
        val binding = ItemPictureBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InformationPictureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InformationPictureViewHolder, position: Int) {
        holder.showPicture(data[position])
        holder.setOnPictureClick {
            onPictureClick(it, data[position])
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}