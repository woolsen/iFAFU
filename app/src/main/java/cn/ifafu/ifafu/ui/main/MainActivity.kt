package cn.ifafu.ifafu.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commitNow
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.common.dialog.mutiluser.MultiUserDialog
import cn.ifafu.ifafu.ui.view.DragLayout
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.entity.GlobalSetting
import cn.ifafu.ifafu.ui.login.LoginActivity
import cn.ifafu.ifafu.ui.main.new_theme.MainNewFragment
import cn.ifafu.ifafu.ui.main.old_theme.MainOldFragment
import cn.ifafu.ifafu.ui.main.vo.CheckoutResult
import cn.ifafu.ifafu.ui.main.vo.DeleteResult
import cn.ifafu.ifafu.ui.main.vo.MainTheme
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.util.ButtonUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.gyf.immersionbar.ImmersionBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private var nowTheme = -999

    private val mMultiUserDialog by lazy {
        MultiUserDialog(context = this,
            onAddClick = {
                it.cancel()
                startLoginActivityForResult()
            }, onItemClick = { user ->
                MaterialDialog(this).show {
                    title(text = "${user.name} ${user.account}")
                    customView(viewRes = R.layout.dialog_user_detail)
                    getCustomView().findViewById<EditText>(R.id.et_password).setText(user.password)
                    negativeButton(text = "删除账号") {
                        viewModel.deleteUser(user)
                    }
                    positiveButton(text = "切换账号") {
                        viewModel.checkoutTo(user)
                    }
                }
            })
    }

    private val loadingDialog = LoadingDialog(this)

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        ImmersionBar.with(this).init()

        viewModel.theme.observe(this, { theme ->
            supportFragmentManager.commitNow(allowStateLoss = true) {
                when (theme) {
                    null, MainTheme.NEW -> replace(R.id.view_content, MainNewFragment())
                    MainTheme.OLD -> replace(R.id.view_content, MainOldFragment())
                }
            }
        })

        viewModel.showMultiUserDialog.observe(this, { show ->
            if (show) {
                viewModel.getUsersLiveData().observe(this, { users ->
                    mMultiUserDialog.setUsers(users)
                })
                mMultiUserDialog.show()
            } else {
                mMultiUserDialog.cancel()
            }
        })

        viewModel.deleteResult.observe(this, {
            when (it) {
                is DeleteResult.NeedLogin -> {
                    loadingDialog.cancel()
                    startLoginActivityForResult()
                    finish()
                }
                is DeleteResult.Success -> {
                    showToast("删除成功")
                    loadingDialog.cancel()
                }
                is DeleteResult.Ing -> {
                    loadingDialog.show("删除中")
                }
                is DeleteResult.CheckTo -> {
                    showToast("成功切换到${it.user.account}")
                    mMultiUserDialog.cancel()
                    loadingDialog.cancel()
                }
            }
        })

        viewModel.checkoutResult.observe(this, {
            when (it) {
                is CheckoutResult.Ing -> {
                    loadingDialog.show("切换中")
                }
                is CheckoutResult.Success -> {
                    showToast("成功切换到${it.user.account}")
                    mMultiUserDialog.cancel()
                    loadingDialog.cancel()
                }
                is CheckoutResult.Failure -> {
                    showToast(it.message)
                    loadingDialog.cancel()
                }
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                if (nowTheme == GlobalSetting.THEME_NEW && findViewById<DragLayout>(R.id.drawer_main).status == DragLayout.Status.Open) {
                    findViewById<DragLayout>(R.id.drawer_main).close(true)
                } else if (nowTheme == GlobalSetting.THEME_OLD && findViewById<DrawerLayout>(R.id.layout_drawer).isDrawerOpen(
                        GravityCompat.START
                    )
                ) {
                    findViewById<DrawerLayout>(R.id.layout_drawer).closeDrawer(GravityCompat.START)
                } else if (ButtonUtils.isFastDoubleClick()) {
                    finish()
                } else {
                    Toast.makeText(this, R.string.back_again, Toast.LENGTH_SHORT).show()
                }
                return true
            }
            return super.onKeyDown(keyCode, event)
        } catch (e: Exception) {
            e.printStackTrace()
            return super.onKeyDown(keyCode, event)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.hideMultiUserDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                viewModel.addAccountSuccess()
            }
        } else if (requestCode == Constants.ACTIVITY_SETTING) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.updateSetting()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startLoginActivityForResult() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("from", Constants.ACTIVITY_MAIN)
        startActivityForResult(intent, Constants.REQUEST_LOGIN)
    }
}