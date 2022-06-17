package cn.ifafu.ifafu.ui.feedback

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.FeedbackActivityBinding
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.feedback.item.FeedbackItemActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackActivity : BaseActivity() {

    private val viewModel: FeedbackViewModel by viewModels()
    private lateinit var binding: FeedbackActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FeedbackActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLightUiBar()
        binding.vm = viewModel
        viewModel.result.observe(this, { res ->
            res.onSuccess {
                snackbar("提交反馈成功，收到后会第一时间处理~")
            }
            res.onFailure {
                snackbar(it.message)
            }
        })
        viewModel.message.observe(this, { snackbar(it) })

        initToolbar()
    }

    private fun initToolbar() {
        binding.tbFeedback.setNavigationOnClickListener { finish() }
        binding.tbFeedback.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_feedback -> {
                    val intent = Intent(this, FeedbackItemActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }
}