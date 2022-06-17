package cn.ifafu.ifafu.ui.setting

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.SettingActivityBinding
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.view.adapter.syllabus_setting.*
import dagger.hilt.android.AndroidEntryPoint
import me.drakeet.multitype.MultiTypeAdapter

@AndroidEntryPoint
class SettingActivity : BaseActivity() {

    private val mAdapter by lazy {
        MultiTypeAdapter().apply {
            register(SeekBarItem::class, SeekBarBinder())
            register(CheckBoxItem::class, CheckBoxBinder())
            register(TextViewItem::class, TextViewBinder())
            register(ColorItem::class, ColorBinder())
        }
    }

    private val mViewModel: SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightUiBar()
        val binding = bind<SettingActivityBinding>(R.layout.setting_activity)

        val decoration = DividerItemDecoration(this@SettingActivity, DividerItemDecoration.VERTICAL)
        decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.shape_divider)!!)
        binding.rvSetting.addItemDecoration(decoration)
        binding.rvSetting.adapter = mAdapter

        mViewModel.settings.observe(this, {
            mAdapter.items = it
            mAdapter.notifyDataSetChanged()
        })
        mViewModel.needCheckTheme.observe(this, {
            if (it) {
                setResult(Activity.RESULT_OK)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
        })
        mViewModel.initSetting()
    }

    override fun onPause() {
        mViewModel.save()
        super.onPause()
    }
}