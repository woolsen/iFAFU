package cn.ifafu.ifafu.ui.login

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import cn.ifafu.ifafu.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

class ChangePasswordDialog : DialogFragment() {

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .title(text = "修改密码")
            .customView(viewRes = R.layout.dialog_change_password)
            .noAutoDismiss()
            .apply {
                val passwordEt = getCustomView().findViewById<EditText>(R.id.et_password)
                val passwordConfirmEt =
                    getCustomView().findViewById<EditText>(R.id.et_password_confirm)
                positiveButton(text = "确认修改") {
                    val password = passwordEt.text.toString()
                    val passwordConfirm = passwordConfirmEt.text.toString()
                    if (password != passwordConfirm) {
                        Toast.makeText(requireContext(), "两次输入密码不同", Toast.LENGTH_SHORT).show()
                        return@positiveButton
                    }
                    viewModel.newPassword.value = password
                    viewModel.changePassword()
                }
            }
    }

}