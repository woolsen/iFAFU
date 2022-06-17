package cn.ifafu.ifafu.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal const val DEFAULT_TIMEOUT = 5000L

fun <T> mutableLiveData(
        context: CoroutineContext = EmptyCoroutineContext,
        timeoutInMs: Long = DEFAULT_TIMEOUT,
        block: suspend LiveDataScope<T>.() -> Unit
): MutableLiveData<T> = liveData(context, timeoutInMs, block) as MutableLiveData<T>

fun <T> LiveData<T>.toLiveData(): LiveData<T> = this