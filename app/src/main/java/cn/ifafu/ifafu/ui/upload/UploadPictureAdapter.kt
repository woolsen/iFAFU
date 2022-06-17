package cn.ifafu.ifafu.ui.upload

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.ItemSelectPictureBinding
import cn.ifafu.ifafu.util.setImageUri
import cn.ifafu.ifafu.util.setImageUrl

/**
 * @param onAddAction 若为null，则不显示添加View
 */
class UploadPictureAdapter(
    @UploadActivity.Type private val type: Int,
    private val max: Int = 0,
    private val onAddAction: (() -> Unit)? = null,
    private val onPictureClick: (View, Uri) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val imageUris = ArrayList<Uri>()
    val imageUrls = ArrayList<String>()

    companion object {
        private const val VIEW_TYPE_CELL = 0x7777
        private const val VIEW_TYPE_FOOTER = 0x8888
    }

    override fun getItemViewType(position: Int): Int {
        if (imageUris.size == max || type == UploadActivity.PAGE_TYPE_CHECK || type == UploadActivity.PAGE_TYPE_EDIT) {
            return VIEW_TYPE_CELL
        }
        return if (position == imageUris.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CELL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_CELL) {
            val binding = ItemSelectPictureBinding.inflate(inflater, parent, false)
            CellVH(binding)
        } else {
            val view = inflater.inflate(R.layout.item_select_picture_add, parent, false)
            view.setOnClickListener { onAddAction?.invoke() }
            AddVH(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CellVH) {
            if (type == UploadActivity.PAGE_TYPE_UPLOAD) {
                imageUris.getOrNull(position)?.also { uri ->
                    holder.setImageUri(uri)
                }
            } else if (type == UploadActivity.PAGE_TYPE_EDIT) {
                imageUrls.getOrNull(position)?.also { url ->
                    holder.setImageUrl(url)
                }
            }
            holder.setOnClickAction {
                onPictureClick.invoke(it, imageUris[position])
            }
            if (type == UploadActivity.PAGE_TYPE_UPLOAD) {
                holder.setShowDelete(true)
                holder.setDeleteAction {
                    imageUris.removeAt(position)
                    notifyItemRemoved(position)
                }
            } else {
                holder.setShowDelete(false)
            }
        }
    }

    override fun getItemCount(): Int {
        if (imageUris.size == max || type == UploadActivity.PAGE_TYPE_CHECK || type == UploadActivity.PAGE_TYPE_EDIT) {
            return imageUris.size
        }
        return imageUris.size + 1
    }

    class CellVH(binding: ItemSelectPictureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val image = binding.informationIvPicture
        private val deleteButton = binding.informationBtnDelete

        fun setShowDelete(show: Boolean) {
            deleteButton.isVisible = show
        }

        fun setDeleteAction(onDelete: () -> Unit) {
            deleteButton.setOnClickListener { onDelete() }
        }

        fun setOnClickAction(onPictureClick: (View) -> Unit) {
            image.setOnClickListener { onPictureClick(image) }
        }

        fun setImageUri(uri: Uri) {
            image.setImageUri(uri)
        }

        fun setImageUrl(url: String) {
            image.setImageUrl(url)
        }
    }

    class AddVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}