package cn.ifafu.ifafu.ui.activity

import android.content.Intent
import android.os.Bundle
import cn.ifafu.ifafu.BuildConfig
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.databinding.AboutActivityBinding
import cn.ifafu.ifafu.ui.feedback.FeedbackActivity
import cn.ifafu.ifafu.ui.web.WebActivity
import cn.ifafu.ifafu.util.AppUtils
import com.afollestad.materialdialogs.MaterialDialog

class AboutActivity : BaseActivity() {

    private lateinit var binding: AboutActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AboutActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLightUiBar()

        if (BuildConfig.DEBUG) {
            binding.ivAppIcon.setImageResource(R.drawable.icon_ifafu_round_test)
        } else {
            binding.ivAppIcon.setImageResource(R.drawable.icon_ifafu_round)
        }

        val versionName = AppUtils.getVersionName(this)
        binding.tvVersionName.text = getString(R.string.app_sub_name, versionName)

        binding.tvVersionName.setOnClickListener {
            showToast("迭代版本号：${AppUtils.getVersionCode(this)}")
        }

        binding.tbAbout.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_feedback -> {
                    startActivity(Intent(this, FeedbackActivity::class.java))
                }
            }
            true
        }

        binding.btnFeed.setOnClickListener {
            MaterialDialog(this).show {
                setContentView(R.layout.view_about_feed)
            }
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            WebActivity.intentFor(this, Constants.PRIVACY_POLICY_URL, "《隐私政策》")
                .also { intent -> startActivity(intent) }
        }

        binding.tbAbout.setNavigationOnClickListener { finish() }
    }

}
