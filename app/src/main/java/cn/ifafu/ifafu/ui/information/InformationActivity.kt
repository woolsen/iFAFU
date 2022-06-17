package cn.ifafu.ifafu.ui.information

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.InformationActivityListBinding
import cn.ifafu.ifafu.ui.center.CenterActivity
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.login2.LoginActivity
import cn.ifafu.ifafu.ui.upload.ShowPictureActivity
import cn.ifafu.ifafu.ui.upload.UploadActivity
import cn.ifafu.ifafu.ui.web.WebActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import timber.log.Timber

@AndroidEntryPoint
class InformationActivity : BaseActivity() {

    private val mAdapter: InformationAdapter by lazy {
        InformationAdapter(onItemClick = { _, _ ->
//            val intent = UploadActivity.intentFor(this, UploadActivity.TYPE_CHECK, info)
//            startActivity(intent)
        }, onPictureClick = { view, url ->
            val intent = ShowPictureActivity.intentFor(this, url)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, view, getString(R.string.information_picture_translate_name)
            )
            startActivity(intent, options.toBundle())
        })
    }

    private val viewModel: InformationViewModel by viewModels()

    private lateinit var binding: InformationActivityListBinding

    private val launchLogin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.refreshUserInfo()
        }
    }
    private val launchUpload = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            mAdapter.refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InformationActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.plant(Timber.DebugTree())
        setLightUiBar()

        initAdapter()
        initSwipeToRefresh()
        initFloatButton()
        initViewModel()

        // 初始化Toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViewModel() {
        viewModel.userInfo.observe(this) { userInfo ->
            val login = userInfo != null
            with(binding.toolbar.menu) {
                this?.findItem(R.id.information_menu_login)?.isVisible = !login
                this?.findItem(R.id.information_menu_center)?.isVisible = login
                this?.findItem(R.id.information_menu_logout)?.isVisible = login
            }
        }
    }

    private fun initFloatButton() {
        binding.fabAdd.setOnClickListener {
            val userInfo = viewModel.userInfo.value
            if (userInfo == null) {
                launchLogin.launch(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }
            launchUpload.launch(UploadActivity.intentForUpload(this))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.information_information, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.information_menu_about -> {
                val intent = WebActivity.intentFor(
                    this,
                    "http://woolsen.cn:8080/about/information",
                    "关于信息平台"
                )
                startActivity(intent)
            }
            R.id.information_menu_login -> {
                launchLogin.launch(Intent(this, LoginActivity::class.java))
            }
            R.id.information_menu_logout -> {
                viewModel.logout()
            }
            R.id.information_menu_center -> {
                val intent = Intent(this, CenterActivity::class.java)
                startActivity(intent)
            }
        }
        return true
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
            viewModel.information.collectLatest {
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

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUserInfo()
            mAdapter.refresh()
        }
    }


}