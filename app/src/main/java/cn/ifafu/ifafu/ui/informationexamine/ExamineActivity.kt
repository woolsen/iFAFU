package cn.ifafu.ifafu.ui.informationexamine

import android.os.Bundle
import cn.ifafu.ifafu.bean.dto.Information
import cn.ifafu.ifafu.databinding.InformationActivityExamineBinding
import cn.ifafu.ifafu.ui.common.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExamineActivity : BaseActivity() {

    private val status = listOf(
        ExamineListFragment.STATUS_ALL,
        Information.STATUS_REVIEWING,
        Information.STATUS_PASS,
        Information.STATUS_FAILURE
    )
    private val statusTitles = listOf("All", "待审核", "已通过", "未通过")
    private lateinit var binding: InformationActivityExamineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InformationActivityExamineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLightUiBar()
        initToolbar()
        initViewPager()
    }

    private fun initViewPager() {
        val adapter = ExamineListFragmentAdapter(status, this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = statusTitles[position]
        }.attach()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}