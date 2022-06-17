package cn.ifafu.ifafu.ui.information

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.ItemPictureBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class InformationPictureViewHolder(binding: ItemPictureBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val imageView = binding.informationIvPicture

    fun setOnPictureClick(onPictureClick: (View) -> Unit) {
        imageView.setOnClickListener { onPictureClick(it) }
    }

    fun showPicture(url: String) {
        Glide.with(imageView)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(R.drawable.information_ic_error_2)
            .into(imageView)
    }

}