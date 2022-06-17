package cn.ifafu.ifafu.ui.common

import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.ui.common.dialog.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import com.gyf.immersionbar.ImmersionBar

abstract class BaseActivity : AppCompatActivity {

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }

    private var toast: Toast? = null

    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    protected fun setTransparentStatusBar() {
        ImmersionBar.with(this).init()
    }

    /**
     * 设置亮色状态栏（黑色图标）
     */
    fun setLightUiBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    protected fun <VDB : ViewDataBinding> bind(@LayoutRes layoutRes: Int): VDB {
        return DataBindingUtil.setContentView<VDB>(this, layoutRes).apply {
            lifecycleOwner = this@BaseActivity
        }
    }

    /**
     * when you override [showLoading], remember to override [hideLoading]
     */
    protected open fun showLoading(message: String) {
        mLoadingDialog.show(message)
    }

    protected open fun hideLoading() {
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.cancel()
        }
    }

    override fun onStop() {
        hideLoading()
        super.onStop()
    }

    protected open fun snackbar(message: String) {
        Snackbar.make(window.decorView, message, Snackbar.LENGTH_SHORT).show()
    }

    protected open fun showToast(message: String) {
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast.makeText(application, message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    fun <T> LiveData<Resource<T>>.observeResource(
        loadingMessage: String,
        onSuccess: (T) -> Unit
    ) {
        this.observe(this@BaseActivity, { res ->
            when (res) {
                is Resource.Success -> {
                    onSuccess(res.data)
                    hideLoading()
                }
                is Resource.Failure -> {
                    res.handleMessage { msg ->
                        snackbar(msg)
                    }
                    hideLoading()
                }
                is Resource.Loading -> {
                    showLoading(loadingMessage)
                }
            }
        })
    }

}