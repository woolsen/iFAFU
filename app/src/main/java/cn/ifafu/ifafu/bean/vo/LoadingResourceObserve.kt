package cn.ifafu.ifafu.bean.vo

import androidx.lifecycle.Observer

class LoadingResourceObserve<T>(
    onCreate: LoadingResourceObserve<T>.() -> Unit
) : Observer<Resource<T>> {

    private var mOnSuccess: ((Resource.Success<T>) -> Unit)? = null
    private var mOnFailure: ((Resource.Failure) -> Unit)? = null
    private var mShowLoading: ((String) -> Unit)? = null
    private var mHideLoading: (() -> Unit)? = null

    init {
        this.onCreate()
    }

    fun onSuccess(onSuccess: (Resource.Success<T>) -> Unit) {
        this.mOnSuccess = onSuccess
    }

    fun onFailure(onFailure: (Resource.Failure) -> Unit) {
        this.mOnFailure = onFailure
    }

    fun showLoading(showLoading: (String) -> Unit) {
        this.mShowLoading = showLoading
    }

    fun hideLoading(hideLoading: () -> Unit) {
        this.mHideLoading = hideLoading
    }

    override fun onChanged(t: Resource<T>) {
        when (t) {
            is Resource.Success -> {
                mHideLoading?.invoke()
                mOnSuccess?.invoke(t)
            }
            is Resource.Failure -> {
                mHideLoading?.invoke()
                mOnFailure?.invoke(t)
            }
            is Resource.Loading -> {
                mShowLoading?.invoke(t.message)
            }
        }
    }
}