package cn.ifafu.ifafu.util

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KProperty

class ActivityBindingDelegate<out VDB : ViewDataBinding>(
        @LayoutRes private val layoutRes: Int
) {

    private var binding: VDB? = null

    operator fun getValue(activity: AppCompatActivity, property: KProperty<*>): VDB {
        return binding ?: DataBindingUtil.setContentView<VDB>(activity, layoutRes).also {
            it.lifecycleOwner = activity
            binding = it
        }
    }
}

fun <VDB : ViewDataBinding> contentView(
        @LayoutRes layoutRes: Int
): ActivityBindingDelegate<VDB> = ActivityBindingDelegate(layoutRes)