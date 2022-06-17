package cn.ifafu.ifafu.ui.login2

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.databinding.LoginInfomationActivityBinding
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.util.hideKeyboardOnInputDone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity(), TextView.OnEditorActionListener {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: LoginInfomationActivityBinding

    companion object {
        const val LOGIN = 1
        const val REGISTER = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginInfomationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTransparentStatusBar()

        binding.lifecycleOwner = this
        binding.vm = viewModel

        binding.password.setOnEditorActionListener(this)
        binding.etCode.setOnEditorActionListener(this)

        viewModel.countdown.observe(this, { countdown ->
            if (countdown == 0) {
                binding.sendCode.setText(R.string.send_code)
            } else {
                binding.sendCode.text = getString(R.string.send_code_countdown, countdown)
            }
        })
        viewModel.message.observe(this, { message ->
            showToast(message)
        })
        viewModel.state.observe(this, { state ->
            if (state == LOGIN) {
                binding.password.hideKeyboardOnInputDone(true)
                binding.password.imeOptions = EditorInfo.IME_ACTION_DONE
            } else if (state == REGISTER) {
                binding.password.hideKeyboardOnInputDone(false)
                binding.password.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        })
        viewModel.loginResult.observe(this, { result ->
            if (result) {
                setResult(RESULT_OK)
                finish()
            }
        })
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            viewModel.loginOrRegister()
        }
        return true
    }

    override fun onBackPressed() {
        if (viewModel.state.value != LOGIN) {
            viewModel.state.value = LOGIN
            return
        }
        super.onBackPressed()
    }
}