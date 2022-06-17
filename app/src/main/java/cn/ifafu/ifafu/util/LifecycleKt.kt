package cn.ifafu.ifafu.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

inline fun <T, S> MediatorLiveData<T>.addLivedata(
    source: LiveData<S>,
    crossinline onChanged: MediatorLiveData<T>.(S) -> Unit
): MediatorLiveData<T> {
    this.addSource(source) {
        onChanged.invoke(this, it)
    }
    return this
}