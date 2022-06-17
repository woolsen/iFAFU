package cn.ifafu.ifafu.ui.electricity.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import cn.ifafu.ifafu.bean.vo.EventObserver
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.common.recycleview.withHeaderAndFooter
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.ElectricityActivityBinding
import cn.ifafu.ifafu.ui.electricity.login.ElectricityLoginActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ElectricityActivity : BaseActivity() {

    private val viewModel by viewModels<ElectricityViewModel>()

    private lateinit var binding: ElectricityActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.electricity_activity)
        binding = DataBindingUtil.setContentView(this, R.layout.electricity_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initButton()
        initToolbar()
        initViewModel()
        initAdapter()
    }

    private val animation: RotateAnimation by lazy {
        RotateAnimation(
            0.0f, 360.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            fillAfter = true
            duration = 500
            repeatCount = RotateAnimation.INFINITE
            interpolator = LinearInterpolator()
        }
    }

    private fun startAnimForRefreshButton() {
        binding.ibRefresh.startAnimation(animation)
    }

    private fun stopAnimForRefreshButton() {
        animation.cancel()
        animation.reset()
    }

    private fun initToolbar() {
        binding.toolbar.navigationIcon?.setTint(Color.WHITE)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initButton() {
        // 修改 FAB 图标颜色
        binding.fab.setColorFilter(Color.WHITE)
        binding.fab.setOnClickListener {
            ElectricitySettingsFragment().show(
                supportFragmentManager,
                "ElectricitySettingsFragment"
            )
        }
        binding.ibRefresh.setOnClickListener {
            viewModel.queryElecBalance()
        }
    }

    private fun initViewModel() {
        viewModel.startLoginActivity.observe(this, {
            val intent = Intent(this, ElectricityLoginActivity::class.java)
            startActivity(intent)
            finish()
        })
        viewModel.balanceOfElectricityLoading.observe(this, EventObserver { loading ->
            println("is loading anim: $loading")
            if (loading) {
                startAnimForRefreshButton()
            } else {
                stopAnimForRefreshButton()
            }
        })
        viewModel.toast.observe(this, EventObserver { message ->
            showToast(message)
        })
        viewModel.showSettingsDialog.observe(this, EventObserver {
            ElectricitySettingsFragment().show(
                supportFragmentManager,
                "ElectricitySettingsFragment"
            )
        })
    }

    private fun initAdapter() {
        val adapter = ElectricityHistoryAdapter()
        binding.rvList.adapter = adapter.withHeaderAndFooter(
            R.layout.electricity_history_item_header,
            R.layout.electricity_history_item_footer,
        )
        ContextCompat.getDrawable(this, R.drawable.shape_divider)?.let { drawable ->
            val itemDecoration = DividerItemDecoration(this, RecyclerView.HORIZONTAL)
            itemDecoration.setDrawable(drawable)
            binding.rvList.addItemDecoration(itemDecoration)
        }

        viewModel.history.observe(this@ElectricityActivity, { list ->
            adapter.submitList(list)
        })
    }

}