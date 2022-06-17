package cn.ifafu.ifafu.ui.common.dialog.mutiluser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.databinding.MutliUserItemBinding

class MultiUserAdapter(
    private val onItemClick: (View, User) -> Unit
) : RecyclerView.Adapter<MultiUserAdapter.ViewHolder>() {

    var items: List<User> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MutliUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        binding: MutliUserItemBinding,
        private val onItemClick: (View, User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val schoolIconIv = binding.ivSchool
        private val textTv = binding.tvText

        fun bind(user: User) {
            schoolIconIv.setImageResource(
                when (user.school) {
                    User.FAFU -> R.drawable.fafu_bb_icon_white
                    User.FAFU_JS -> R.drawable.fafu_js_icon_white
                    else -> R.drawable.icon_ifafu_round
                }
            )
            textTv.text = ("${user.name} ${user.account}")
            itemView.setOnClickListener {
                onItemClick(it, user)
            }
        }

    }
}