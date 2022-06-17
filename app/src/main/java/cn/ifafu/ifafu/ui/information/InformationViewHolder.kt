package cn.ifafu.ifafu.ui.information

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.databinding.InformationListItemBinding
import cn.ifafu.ifafu.util.DateUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.text.SimpleDateFormat
import java.util.*

open class InformationViewHolder(
    private val binding: InformationListItemBinding,
    private val onItemClick: (View, Information) -> Unit,
    private val onMoreClick: ((View, Information) -> Unit)? = null,
    private val onDeleteClick: ((View, Information) -> Unit)? = null,
    onPictureClick: (View, String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val mReviewingColor = 0xFFFF9800.toInt()
    private val mPassColor = 0xFF2196F3.toInt()
    private val mFailureColor = 0xFFf44336.toInt()

    private val contentTv = binding.content
    private val timeTv: TextView = binding.time
    private val contactTv: TextView = binding.contact
    private val contactIconIv: ImageView = binding.contactIcon
    private val pictureRv = binding.pictures
    private var information: Information? = null
    private val mPictureAdapter = InformationPictureAdapter(onPictureClick)

    init {
        pictureRv.adapter = mPictureAdapter
    }

    @CallSuper
    open fun bind(information: Information) {
        this.information = information
        if (information.avatarUrl.isNullOrBlank()) {
            Glide.with(binding.avatar)
                .load(R.drawable.img_default_avatar)
                .into(binding.avatar)
        } else {
            Glide.with(binding.avatar)
                .load(information.avatarUrl)
                .into(binding.avatar)
        }
        binding.nickname.text = information.nickname

        contentTv.isVisible = information.content.isNotBlank()
        contentTv.text = information.content.trimEnd()

        val date = information.date
        timeTv.text = when {
            DateUtils.isToday(date) -> "今天${timeFormat.format(date)}"
            DateUtils.isYesterday(date) -> "昨天${timeFormat.format(date)}"
            else -> "${dateFormat.format(date)}${timeFormat.format(date)}"
        }
        setContact(information.contact, information.contactType)

        binding.btnExamine.isVisible = onMoreClick != null
        if (onMoreClick != null) {
            binding.btnExamine.setOnClickListener {
                onMoreClick.invoke(it, information)
            }
        }

        binding.btnDelete.isVisible = onDeleteClick != null
        if (onDeleteClick != null) {
            binding.btnDelete.setOnClickListener {
                onDeleteClick.invoke(it, information)
            }
        }

        binding.status.isVisible = information.status != null
        when (information.status) {
            Information.STATUS_PASS -> {
                binding.status.text = "审核通过"
                binding.status.setTextColor(mPassColor)
            }
            Information.STATUS_FAILURE -> {
                binding.status.text = "审核失败"
                binding.status.setTextColor(mFailureColor)
            }
            Information.STATUS_REVIEWING -> {
                binding.status.text = "待审核"
                binding.status.setTextColor(mReviewingColor)
            }
        }

        /* 初始化图片 */
        mPictureAdapter.data.clear()
        (information.imageUrls as? List<String>)?.let {
            mPictureAdapter.data.addAll(it)
        }
        mPictureAdapter.notifyDataSetChanged()

        itemView.setOnClickListener {
            onItemClick(it, information)
        }
    }

    private fun setContact(contact: String?, contactType: Int?) {
        binding.contactLayout.isVisible = contactType == Information.CONTACT_TYPE_WECHAT ||
                contactType == Information.CONTACT_TYPE_PHONE ||
                contactType == Information.CONTACT_TYPE_QQ
        binding.contactLayout.setOnLongClickListener {
            ClipboardUtils.copyText(contact)
            ToastUtils.showShort(R.string.contact_copy_to_clipboard)
            true
        }
        val contactIcon: Int = when (contactType) {
            Information.CONTACT_TYPE_WECHAT -> {
                R.drawable.information_ic_wechat
            }
            Information.CONTACT_TYPE_PHONE -> {
                R.drawable.information_ic_phone
            }
            Information.CONTACT_TYPE_QQ -> {
                R.drawable.information_ic_qq
            }
            else -> {
                R.drawable.information_ic_notice
            }
        }
        Glide.with(contactIconIv)
            .load(contactIcon)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(contactIconIv)
        this.contactTv.text = contact
    }
}