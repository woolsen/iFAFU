package cn.ifafu.ifafu.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import cn.ifafu.ifafu.R
import com.bumptech.glide.Glide

@BindingAdapter("visibleGoneWhen")
fun visibleGoneWhen(view: View, gone: Boolean) {
    view.visibility = if (gone) View.GONE else View.VISIBLE
}

@BindingAdapter("isVisible")
fun isVisible(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("srcBitmap")
fun srcBitmap(imageView: ImageView, bitmap: Bitmap?) {
    imageView.setImageBitmap(bitmap)
}

@BindingAdapter("android:visibleWhen")
fun visibleWhen(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("android:goneWhen")
fun goneWhen(view: View, gone: Boolean) {
    view.visibility = if (gone) View.GONE else View.VISIBLE
}

@BindingAdapter("android:invisibleWhen")
fun invisibleWhen(view: View, invisible: Boolean) {
    view.visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

@BindingAdapter("android:hideKeyboardOnInputDone")
fun EditText.hideKeyboardOnInputDone(enabled: Boolean) {
    if (!enabled) return
    val listener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        false
    }
    setOnEditorActionListener(listener)
}

@BindingAdapter("android:srcUri")
fun ImageView.setImageUri(uri: Uri?) {
    if (uri == null) {
        this.setImageDrawable(null)
    } else {
        Glide.with(this)
            .load(uri)
            .error(R.drawable.information_ic_error_2)
            .into(this)
    }
}

@BindingAdapter("android:srcUrl")
fun ImageView.setImageUrl(url: String?) {
    if (url == null) {
        this.setImageDrawable(null)
    } else {
        Glide.with(this)
            .load(url)
            .error(R.drawable.information_ic_error_2)
            .into(this)
    }
}