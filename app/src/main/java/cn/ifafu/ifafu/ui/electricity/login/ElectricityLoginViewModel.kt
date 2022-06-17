package cn.ifafu.ifafu.ui.electricity.login

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.repository.XfbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ElectricityLoginViewModel @Inject constructor(
    private val repository: XfbRepository
) : BaseViewModel() {

    val account = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val verify = MutableLiveData<String>()

    private val _result = MediatorLiveData<Resource<Boolean>>()
    val result: LiveData<Resource<Boolean>> = _result

    private val _verifyBitmap = MutableLiveData<Bitmap>()
    val verifyBitmap: LiveData<Bitmap> = _verifyBitmap

    init {
        viewModelScope.launch {
            refreshVerify()
            val user = repository.getElecUser()
            val defaultAccount =
                if (user.xfbAccount.length == 9) {
                    '0' + user.xfbAccount
                } else {
                    user.xfbAccount
                }
            account.postValue(defaultAccount)
            password.postValue(user.password)
        }
    }

    fun login() {
        val account = account.value
        if (account == null || account.isEmpty()) {
            _result.value = Resource.Failure("账号不能为空")
            return
        }
        val password = password.value
        if (password == null || password.isEmpty()) {
            _result.value = Resource.Failure("密码不能为空")
            return
        }
        val verify = verify.value
        if (verify == null || verify.isEmpty()) {
            _result.value = Resource.Failure("验证码不能为空")
            return
        }
        _result.addSource(repository.login(account, password, verify)) {
            it.onFailure {
                refreshVerify()
            }
            _result.value = it
        }
    }

    fun refreshVerify() {
        viewModelScope.launch {
            repository.getVerifyBitmap()
                .flowOn(Dispatchers.IO)
                .catch { it.printStackTrace() }
                .collect { bitmap ->
                    Timber.d("接收到学付宝登录验证码")
                    _verifyBitmap.postValue(bitmap)
                }
        }
    }

}