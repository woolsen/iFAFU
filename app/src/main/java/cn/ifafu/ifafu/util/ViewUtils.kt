package cn.ifafu.ifafu.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

object ViewUtils {
    fun TextView.hideKeyboard() {
        this.clearFocus()
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }
}