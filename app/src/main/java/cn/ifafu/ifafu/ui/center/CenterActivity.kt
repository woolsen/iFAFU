package cn.ifafu.ifafu.ui.center

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.common.dialog.LoadingDialog
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.bean.vo.ResourceObserve
import cn.ifafu.ifafu.databinding.InformationActivityCenterBinding
import cn.ifafu.ifafu.ui.information.InformationAdapter
import cn.ifafu.ifafu.ui.information.InformationLoadStateAdapter
import cn.ifafu.ifafu.ui.informationexamine.ExamineActivity
import cn.ifafu.ifafu.ui.upload.ShowPictureActivity
import cn.ifafu.ifafu.ui.upload.UploadActivity
import cn.ifafu.ifafu.util.StatusBarUtils.transparentStatusBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class CenterActivity : BaseActivity() {

    private val mViewModel: CenterViewModel by viewModels()
    private val mLoadingDialog = LoadingDialog(this)
    private lateinit var binding: InformationActivityCenterBinding

    private val mAdapter: InformationAdapter by lazy {
        InformationAdapter(onItemClick = { _, _ ->

        }, onPictureClick = { view, url ->
            val intent = ShowPictureActivity.intentFor(this, url)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, view, getString(R.string.information_picture_translate_name)
            )
            startActivity(intent, options.toBundle())
        }, onDeleteClick = { v, info ->
            showDeleteConfirmDialog(v, info)
        })
    }

    private val launchUploadActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            mAdapter.refresh()
            binding.informationList.scrollToPosition(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InformationActivityCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transparentStatusBar()

        initToolbar()
        initAdapter()
        initFloatButton()
        initSwipeToRefresh()
        initViewModel()
    }

    private fun initFloatButton() {
        binding.informationFabAdd.setOnClickListener {
            launchUploadActivity.launch(UploadActivity.intentForUpload(this))
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_logout -> {
                    mViewModel.logout()
                    finish()
                }
                R.id.menu_examine -> {
                    val intent = Intent(this, ExamineActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun initViewModel() {
        mViewModel.finish.observe(this, { finish() })
        mViewModel.message.observe(this, { showToast(it) })
        mViewModel.userInfo.observe(this, {
            if (it != null) {
                binding.layoutToolbar.title = it.nickname
            }
        })
        mViewModel.canExamine.observe(this, { canExamine ->
            val examineMenu = binding.toolbar.menu.findItem(R.id.menu_examine)
            examineMenu?.isVisible = canExamine
        })
        mViewModel.deleteResult.observe(this, ResourceObserve {
            onLoading { mLoadingDialog.show("删除中") }
            onFailure {
                it.handleMessage { msg -> snackbar(msg) }
                mLoadingDialog.cancel()
            }
            onSuccess {
                snackbar("删除成功")
                mAdapter.refresh()
                binding.informationList.scrollToPosition(0)
                mLoadingDialog.cancel()
            }
        })
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { mAdapter.refresh() }
    }

    private fun initAdapter() {
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        decoration.setDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.information_shape_decoration_horizonal
            )!!
        )
        binding.informationList.addItemDecoration(decoration)

        binding.informationList.adapter = mAdapter.withLoadStateHeaderAndFooter(
            header = InformationLoadStateAdapter(mAdapter),
            footer = InformationLoadStateAdapter(mAdapter)
        )

        lifecycleScope.launchWhenCreated {
            @OptIn(ExperimentalCoroutinesApi::class)
            mAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading
            }
        }

        lifecycleScope.launchWhenCreated {
            @OptIn(ExperimentalCoroutinesApi::class)
            mViewModel.information.collectLatest {
                mAdapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            @OptIn(FlowPreview::class)
            mAdapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.informationList.scrollToPosition(0) }
        }
    }

    private fun showDeleteConfirmDialog(anchor: View, info: Information) {
        val popup = PopupMenu(this, anchor)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.information_menu_delete -> {
                    mViewModel.delete(info.id)
                }
            }
            popup.dismiss()
            true
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.information_delete, popup.menu)
        popup.show()
    }
}