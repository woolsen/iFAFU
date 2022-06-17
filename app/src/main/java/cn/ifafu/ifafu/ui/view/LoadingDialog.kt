package cn.ifafu.ifafu.ui.view

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import cn.ifafu.ifafu.R
import com.github.ybq.android.spinkit.style.CubeGrid

/**
 * 将Dialog包装起来，通过延迟初始化，防止可出现在OnCreate之前初始化导致的崩溃
 */
class LoadingDialog constructor(context: Context, text: String? = null) {

    private val dialog by lazy {  Dialog(context, text) }

    val isShowing: Boolean
        get() = dialog.isShowing

    fun show() = dialog.show()
    fun show(@StringRes resId: Int) = dialog.show(resId)
    fun show(text: String) = dialog.show(text)
    fun cancel() = dialog.cancel()

    fun observe(lifecycleOwner: LifecycleOwner, livedata: LiveData<String?>) {
        livedata.observe(lifecycleOwner, {
            if (it.isNullOrBlank()) {
                dialog.cancel()
            } else {
                dialog.show(it)
            }
        })
    }

    private class Dialog(context: Context, text: String?) : android.app.Dialog(context, R.style.Dialog_Loading) {

        val loadingTV: TextView

        init {
            setContentView(R.layout.dialog_progress)
            loadingTV = findViewById(R.id.tv_progress_text)
            setText(text)
            findViewById<ProgressBar>(R.id.pb_progress).apply {
                indeterminateDrawable = CubeGrid().apply {
                    color = Color.WHITE
                }
            }
        }

        fun setText(text: String?) {
            if (text.isNullOrBlank()) {
                loadingTV.visibility = View.GONE
            } else {
                loadingTV.visibility = View.VISIBLE
                loadingTV.text = text
            }
        }

        fun show(text: String) {
            setText(text)
            show()
        }

        fun show(@StringRes resId: Int) {
            setText(context.getString(resId))
            show()
        }
    }
}