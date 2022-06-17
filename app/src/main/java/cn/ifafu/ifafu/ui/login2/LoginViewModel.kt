package cn.ifafu.ifafu.ui.login2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.repository.IfUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: IfUserRepository,
) : ViewModel() {

    val state = MutableLiveData(LoginActivity.LOGIN)
    val loginResult = MutableLiveData(false)
    val countdown = MutableLiveData(0)

    val phone = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val code = MutableLiveData<String>()

    val message = MutableLiveData<String>()

    private val pureNumberString = "[0-9]+".toRegex()

    companion object {
        private const val CODE_TIME = 60
    }

    fun loginOrRegister() {
        val state = this.state.value
        if (state == LoginActivity.LOGIN) {
            login()
        } else if (state == LoginActivity.REGISTER) {
            register()
        }
    }

    private fun login() {
        val username = phone.value
        val password = password.value
        if (username.isNullOrBlank()) {
            message.postValue("用户名不能空")
        } else if (password.isNullOrBlank()) {
            message.postValue("密码不能为空")
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val res = repository.login(username, password)
                    if (res is Resource.Success) {
                        message.postValue("登录成功")
                        loginResult.postValue(true)
                    } else if (res is Resource.Failure) {
                        message.postValue(res.message)
                    }
                } catch (e: IOException) {
                    message.postValue("网络异常")
                } catch (e: Exception) {
                    message.postValue("登录出错")
                }
            }
        }
    }

    private fun register() {
        val phone = phone.value
        val password = password.value
        val code = code.value
        if (phone.isNullOrBlank()) {
            message.postValue("手机号不能为空")
        } else if (!phone.matches("[0-9]{11}".toRegex())) {
            message.postValue("手机号格式不正确")
        } else if (password.isNullOrBlank()) {
            message.postValue("密码不能为空")
        } else if (password.matches(pureNumberString)) {
            message.postValue("密码不能为纯数字")
        } else if (password.length < 8) {
            message.postValue("密码长度不能小于8")
        } else if (password.length > 16) {
            message.postValue("密码长度不能大于16")
        } else if (code.isNullOrBlank()) {
            message.postValue("验证码不能为空")
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val res = repository.register(phone, password, code)
                    if (res is Resource.Success) {
                        state.postValue(LoginActivity.LOGIN)
                        message.postValue("注册成功")
                    } else if (res is Resource.Failure) {
                        message.postValue(res.message)
                    }
                } catch (e: IOException) {
                    message.postValue("网络异常")
                } catch (e: Exception) {
                    e.printStackTrace()
                    message.postValue("注册出错")
                }
            }
        }
    }

    fun goRegister() {
        state.value = LoginActivity.REGISTER
    }

    fun back() {
        state.value = LoginActivity.LOGIN
    }

    fun sendCode() {
        if (countdown.value != 0) {
            return
        }
        val phone = this.phone.value
        if (phone.isNullOrBlank()) {
            message.value = "手机号不能为空"
            return
        }
        viewModelScope.launch {
            repository.sendCode(phone)
                .flowOn(Dispatchers.IO)
                .catch { e -> message.postValue(e.message) }
                .collectLatest {
                    message.postValue("验证码发送成功")
                    countdown.postValue(CODE_TIME)
                    for (i in CODE_TIME - 1 downTo 0) {
                        delay(1000)
                        countdown.postValue(i)
                    }
                }
        }
    }
}