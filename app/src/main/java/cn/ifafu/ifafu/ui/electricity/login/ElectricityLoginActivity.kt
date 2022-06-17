package cn.ifafu.ifafu.ui.electricity.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.databinding.ElectricityLoginActivityBinding
import cn.ifafu.ifafu.ui.electricity.main.ElectricityActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ElectricityLoginActivity : BaseActivity(), View.OnClickListener {

    private val mViewModel by viewModels<ElectricityLoginViewModel>()

    private lateinit var binding: ElectricityLoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ElectricityLoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this

        setLightUiBar()
        binding.viewModel = mViewModel

        mViewModel.result.observeResource("登录中") {
            showToast("登录成功")
            val intent = Intent(this, ElectricityActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tbElectricityLogin.setNavigationOnClickListener { finish() }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                mViewModel.login()
            }
            R.id.iv_verify -> {
                mViewModel.refreshVerify()
            }
        }
    }

}