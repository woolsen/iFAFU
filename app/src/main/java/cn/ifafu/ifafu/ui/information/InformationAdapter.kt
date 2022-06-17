package cn.ifafu.ifafu.ui.information

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.databinding.InformationListItemBinding

class InformationAdapter(
    private val onItemClick: (View, Information) -> Unit,
    private val onPictureClick: (View, String) -> Unit,
    private val onMoreClick: ((View, Information) -> Unit)? = null,
    private val onDeleteClick: ((View, Information) -> Unit)? = null,
) : PagingDataAdapter<Information, InformationViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationViewHolder {
        val binding = InformationListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InformationViewHolder(
            binding = binding,
            onItemClick = onItemClick,
            onPictureClick = onPictureClick,
            onMoreClick = onMoreClick,
            onDeleteClick = onDeleteClick,
        )
    }

    override fun onBindViewHolder(holder: InformationViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<Information>() {
            override fun areItemsTheSame(oldItem: Information, newItem: Information): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Information, newItem: Information): Boolean {
                return oldItem.content == newItem.content
            }
        }
    }

}