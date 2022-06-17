package cn.ifafu.ifafu.ui.common.dialog.mutiluser

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.exception.Failure
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.repository.UserRepository
import cn.ifafu.ifafu.ui.main.MainViewModel
import cn.ifafu.ifafu.util.Validator
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MultiUserItemDialog : AppCompatDialogFragment() {

    private val viewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        fun newInstance(user: User): MultiUserItemDialog {
            val fragment = MultiUserItemDialog()
            val bundle = Bundle().apply {
                putParcelable("user", user)
            }
            return fragment.apply { arguments = bundle }
        }
    }

    private lateinit var user: User

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        user = arguments?.get("user") as? User ?: savedInstanceState?.get("user") as User
        return MaterialDialog(requireContext()).show {
            title(text = "${user.name} ${user.account}")
            customView(viewRes = R.layout.dialog_user_detail)
            getCustomView().findViewById<EditText>(R.id.et_password).setText(user.password)
            neutralButton(text = "修改密码") {
                showChangePasswordDialog()
            }
            negativeButton(text = "删除账号") {
                lifecycleScope.launch {
                    userRepository.deleteUserByAccount(user.account)
                }
            }
            positiveButton(text = "切换账号") {
                viewModel.checkoutTo(user)
            }
        }
    }

    private fun showChangePasswordDialog() {
        MaterialDialog(requireContext())
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
                        showToast("两次输入密码不同")
                        return@positiveButton
                    }
                    lifecycleScope.launch {
                        try {
                            Validator().checkNewPassword(user, password)
                            userRepository.changePassword(user, password)
                        } catch (e: Failure) {
                            showToast(e.message)
                        } catch (e: Exception) {
                            Timber.e(e)
                            showToast(e.message ?: "ERROR")
                        }
                    }

                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}