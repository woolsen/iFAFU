package cn.ifafu.ifafu.util

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> Activity.bind(@LayoutRes layoutRes: Int): T {
    return DataBindingUtil.setContentView(this, layoutRes)
}