package cn.ifafu.ifafu.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.bean.vo.LoadingResourceObserve
import cn.ifafu.ifafu.bean.vo.ResourceObserve
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.constant.Constants.EXTRA_ORIGIN
import cn.ifafu.ifafu.constant.ResultCode
import cn.ifafu.ifafu.databinding.LoginActivityBinding
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.main.MainActivity
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.ui.web.WebActivity
import com.blankj.utilcode.util.BarUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private var originActivityCode = 0

    private val mLoadingDialog = LoadingDialog(this, "登录中")

    private val mChangePasswordDialog by lazy { ChangePasswordDialog() }

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: LoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bind(R.layout.login_activity)

        BarUtils.transparentStatusBar(this)
        BarUtils.setStatusBarLightMode(this, true)
        BarUtils.addMarginTopEqualStatusBarHeight(binding.root)

        binding.vm = viewModel


        /**
         * 如果是从闪屏页来的，则是新登录用户，则不显示关闭按钮。其他情况则显示关闭按钮
         */
        originActivityCode = intent.getIntExtra(EXTRA_ORIGIN, Int.MAX_VALUE)
        binding.btnClose.isVisible = originActivityCode != Constants.ACTIVITY_SPLASH
        binding.btnClose.setOnClickListener { finish() }

        binding.btnLogin.setOnClickListener {
            if (!binding.checkboxPolicy.isChecked) {
                snackbar("请先同意《隐私政策》")
                return@setOnClickListener
            }
            viewModel.login()
        }

        binding.etPassword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.login()
                true
            } else {
                false
            }
        }


        // 隐私政策
        binding.checkboxPolicy.setChecked(true, false)
        binding.tvPolicy.setOnClickListener {
            WebActivity.intentFor(this, Constants.PRIVACY_POLICY_URL, "《隐私政策》")
                .also { intent -> startActivity(intent) }
        }

        initViewModel()
    }

    private fun initViewModel() {
        //初始化ViewModel
        viewModel.loginResult.observe(this, ResourceObserve {
            onSuccess { res ->
                mLoadingDialog.cancel()
                showToast("登录成功")
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                setResult(RESULT_OK)
                finish()
            }
            onLoading {
                mLoadingDialog.show("登录中")
            }
            onFailure {
                it.handleMessage { msg -> showToast(msg) }
                mLoadingDialog.cancel()
                if (it.code == ResultCode.NEED_CHANGE_PASSWORD) {
                    mChangePasswordDialog.show(supportFragmentManager, "ChangePasswordDialog")
                }
            }
        })

        viewModel.changePasswordResult.observe(this, LoadingResourceObserve {
            showLoading { mLoadingDialog.show(it) }
            hideLoading { mLoadingDialog.cancel() }
            onSuccess {
                //若修改密码成功，则更新输入框密码
                binding.etPassword.setText(it.data.password)
                mChangePasswordDialog.dismiss()
                viewModel.login()
            }
            onFailure {
                showToast(it.message)
            }
        })
    }

}
