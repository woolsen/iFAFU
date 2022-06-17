package cn.ifafu.ifafu.ui.feedback.item

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.databinding.FeedbackActivityDetailBinding
import cn.ifafu.ifafu.repository.FeedbackRepository
import cn.ifafu.ifafu.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedbackItemActivity : BaseActivity() {

    @Inject
    lateinit var repo: FeedbackRepository

    private lateinit var adapter: FeedbackAdapter
    private lateinit var binding: FeedbackActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FeedbackActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLightUiBar()
        initToolbar()
        initAdapter()
    }

    private fun initToolbar() {
        binding.tbFeedbackItem.setNavigationOnClickListener { finish() }
    }

    private fun initAdapter() {
        adapter = FeedbackAdapter()
        binding.rvList.adapter = adapter

        lifecycleScope.launchWhenCreated {
            when (val res = repo.query()) {
                is Resource.Success -> {
                    adapter.data = res.data
                    adapter.notifyDataSetChanged()
                }
                is Resource.Failure -> {
                    snackbar(res.message)
                }
                is Resource.Loading -> {
                    snackbar("查询出错")
                }
            }
            binding.pbProgress.visibility = View.GONE
        }
    }
}