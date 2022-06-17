package cn.ifafu.ifafu.ui.comment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.databinding.CommentActivityBinding
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.ui.web.WebActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.SPUtils
import com.gyf.immersionbar.ImmersionBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentActivity : BaseActivity() {

    private val mViewModel by viewModels<CommentViewModel>()

    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    private val mAdapter = CommentAdapter { item ->
        mViewModel.click(item)
    }

    companion object {
        private val REQUEST_COMMENT_ITEM = 1001
    }

    private lateinit var binding: CommentActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //沉浸状态栏
        ImmersionBar.with(this)
            .titleBarMarginTop(R.id.tb_comment)
            .statusBarColor("#FFFFFF")
            .statusBarDarkFont(true)
            .init()
        //检查是否同意过条款
        if (!SPUtils.getInstance().contains("comment_agree")) {
            SPUtils.getInstance().put("comment_agree", true)
            showCommentClause()
        }

        binding.rvComment.adapter = mAdapter

        binding.btnOne.setOnClickListener {
            mViewModel.buttonClick()
        }
        mViewModel.isAllCompleted.observe(this) { isAllCompleted ->
            if (isAllCompleted) {
                binding.btnOne.text = "提交最终评教"
            } else {
                binding.btnOne.text = "一键评教"
            }
        }

        //初始化ViewModel
        mViewModel.listRes.observe(this) { res ->
            when (res) {
                is Resource.Loading -> {
                    loadingDialog.show(res.message)
                }
                is Resource.Success -> {
                    loadingDialog.cancel()
                    mAdapter.setList(res.data)
                }
                is Resource.Failure -> {
                    loadingDialog.cancel()
                    res.message.let {
                        showToast(it)
                        finish()
                    }
                }
            }
        }
        mViewModel.autoCommentResult.observe(this, { res ->
            when (res) {
                is Resource.Loading -> {
                    loadingDialog.show(res.message)
                }
                is Resource.Success -> {
                    loadingDialog.cancel()
                    mViewModel.refresh()
                    showToast("一键评教完成")
                }
                is Resource.Failure -> {
                    loadingDialog.cancel()
                    showToast(res.message)
                }
            }
        })
        mViewModel.autoCommentResult.observe(this, { res ->
            when (res) {
                is Resource.Loading -> {
                    loadingDialog.show(res.message)
                }
                is Resource.Success -> {
                    loadingDialog.cancel()
                    mViewModel.refresh()
                    showToast("一键评教完成")
                }
                is Resource.Failure -> {
                    loadingDialog.cancel()
                    showToast(res.message)
                }
            }
        })
        mViewModel.commitResult.observe(this, { res ->
            when (res) {
                is Resource.Loading -> {
                    loadingDialog.show(res.message)
                }
                is Resource.Success -> {
                    loadingDialog.cancel()
                    showToast("评价完成！")
                    finish()
                }
                is Resource.Failure -> {
                    loadingDialog.cancel()
                    showToast(res.message)
                }
            }
        })
        mViewModel.jumpToItemComment.observe(this, { jumpInfo ->
            val intent = Intent(this, WebActivity::class.java)
            jumpInfo.forEach { (k, u) ->
                intent.putExtra(k, u)
            }
            startActivityForResult(intent, REQUEST_COMMENT_ITEM)
        })
    }

    private fun showCommentClause() {
        MaterialDialog(this).show {
            message(R.string.comment_1)
            positiveButton(text = "同意")
            negativeButton(text = "拒绝") {
                this@CommentActivity.finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_COMMENT_ITEM) {
            mViewModel.refresh()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}