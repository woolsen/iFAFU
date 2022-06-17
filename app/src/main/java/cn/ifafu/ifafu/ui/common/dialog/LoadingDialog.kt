package cn.ifafu.ifafu.ui.common.dialog

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import cn.ifafu.ifafu.R

class LoadingDialog(
    private val context: Context,
    private var text: String = "加载中"
) {

    init {
        if (text.isBlank()) {
            text = "加载中"
        }
    }

    private val dialog: LDialog by lazy { LDialog(context) }

    fun show() {
        dialog.setLoadingMessage(text)
        dialog.show()
    }

    fun show(text: String? = null) {
        if (text == null) {
            this.text = "加载中"
        } else {
            this.text = text
        }
        dialog.setLoadingMessage(text)
        dialog.show()
    }

    fun cancel() {
        dialog.cancel()
    }

    fun isShowing() = dialog.isShowing

    private class LDialog(context: Context) : Dialog(context) {

        private val title: TextView

        init {
            setContentView(R.layout.dialog_base_progress)
            title = findViewById(R.id.base_tv_loading)
        }

        fun setLoadingMessage(title: CharSequence?) {
            this.title.text = title
        }
    }
}