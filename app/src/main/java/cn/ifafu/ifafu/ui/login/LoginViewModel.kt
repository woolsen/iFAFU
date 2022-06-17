package cn.ifafu.ifafu.ui.login

import androidx.lifecycle.*
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.domain.user.ChangePasswordUseCase
import cn.ifafu.ifafu.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val userRepository: UserRepository
) : BaseViewModel() {

    val account = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val newPassword = MutableLiveData<String>()

    private val _loginResult = MediatorLiveData<Resource<User>>()
    val loginResult: LiveData<Resource<User>>
        get() = _loginResult

    private val _changePasswordResult = MutableLiveData<Resource<User>>()
    val changePasswordResult: LiveData<Resource<User>>
        get() = _changePasswordResult

    val isShowCloseBtn: LiveData<Boolean> = liveData {
        emit(userRepository.getUser() != null)
    }

    private var user : User? = null

    fun login() {
        val account = account.value ?: ""
        val password = password.value ?: ""
        val school = if (account.length == 9) User.FAFU_JS else User.FAFU
        _loginResult.value = Resource.Loading("登录中")
        viewModelScope.launch(Dispatchers.IO) {
            //检查账号格式
            if (account.isEmpty()) {
                _loginResult.postValue(Resource.Failure("账号不能为空！"))
                return@launch
            } else if (account.length != 9 && account.length != 10) {
                _loginResult.postValue(Resource.Failure("账号格式错误！"))
                return@launch
            }
            //检查密码格式
            if (password.isEmpty()) {
                _loginResult.postValue(Resource.Failure("密码不能为空！"))
                return@launch
            } else if (password.length < 6) {
                _loginResult.postValue(Resource.Failure("密码格式错误！"))
                return@launch
            }
            val user = User(
                account = account,
                password = password,
                school = school,
                token = User.generateToken()
            ).apply { this@LoginViewModel.user = this }
            userRepository.login(user).let { res ->
                _loginResult.postValue(res)
            }
        }
    }

    fun changePassword() {
        _changePasswordResult.value = Resource.Loading("修改中")
        viewModelScope.launch {
            val user = this@LoginViewModel.user
            if (user == null) {
                _changePasswordResult.postValue(Resource.failure("密码修改错误，请联系iFAFU管理员"))
                return@launch
            }
            val params = ChangePasswordUseCase.Params(user, newPassword.value ?: "")
            changePasswordUseCase(params).let { res ->
                _changePasswordResult.postValue(res)
            }
        }
    }

}