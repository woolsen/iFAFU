package cn.ifafu.ifafu.ui.information

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.databinding.ItemNetworkStateBinding

class InformationLoadStateAdapter(
    private val adapter: PagingDataAdapter<Information, out RecyclerView.ViewHolder>
) : LoadStateAdapter<InformationLoadStateAdapter.NetworkStateItemViewHolder>() {

    override fun onBindViewHolder(holder: NetworkStateItemViewHolder, loadState: LoadState) {
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateItemViewHolder {
        val binding = ItemNetworkStateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NetworkStateItemViewHolder(binding) { adapter.retry() }
    }

    class NetworkStateItemViewHolder(
        binding: ItemNetworkStateBinding,
        private val retryCallback: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val progressBar = binding.progressBar
        private val errorMsg = binding.errorMsg
        private val retry = binding.retryButton.also {
            it.setOnClickListener { retryCallback() }
        }

        fun bindTo(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retry.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
            errorMsg.text = (loadState as? LoadState.Error)?.error?.message
        }
    }
}