package cn.ifafu.ifafu.bean.vo

import androidx.lifecycle.Observer


/**
 * 通过对数据的包装，使LiveData能处理带有数据的事件
 */
class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // 允许读，但不允许写

    /**
     * 返回数据。防止被二次使用
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 返回数据。防止被二次使用
     */
    fun runContentIfNotHandled(block: (T) -> Unit) {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            block(content)
        }
    }

    /**
     * 返回数据。不论是否被使用过
     */
    fun peekContent(): T = content

}

class EventObserver<T>(private val handle: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(t: Event<T>?) {
        t?.runContentIfNotHandled {
            handle(it)
        }
    }

}