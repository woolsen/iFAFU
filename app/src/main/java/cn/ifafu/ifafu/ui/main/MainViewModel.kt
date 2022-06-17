package cn.ifafu.ifafu.ui.main

import androidx.lifecycle.*
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.GlobalSetting
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.repository.OtherRepository
import cn.ifafu.ifafu.repository.UserRepository
import cn.ifafu.ifafu.repository.GlobalSettingRepository
import cn.ifafu.ifafu.ui.main.vo.CheckoutResult
import cn.ifafu.ifafu.ui.main.vo.DeleteResult
import cn.ifafu.ifafu.ui.main.vo.MainTheme
import cn.ifafu.ifafu.util.toLiveData
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val globalSettingRepository: GlobalSettingRepository,
    private val otherRepository: OtherRepository,
) : BaseViewModel() {

    private val _checkoutResult = MutableLiveData<CheckoutResult>()
    val checkoutResult: LiveData<CheckoutResult> = _checkoutResult

    private val _deleteResult = MutableLiveData<DeleteResult>()
    val deleteResult = _deleteResult.toLiveData()

    private val _user = MediatorLiveData<User>().apply {
        addSource(_checkoutResult) {
            if (it is CheckoutResult.Success) {
                this.value = it.user
            }
        }
        addSource(_deleteResult) {
            if (it is DeleteResult.CheckTo) {
                this.value = it.user
            }
        }
    }
    val user = _user.toLiveData()
    val isShowComment = user.map { it.school == User.FAFU }

    private val _theme = MediatorLiveData<MainTheme>().apply {
        addSource(_user) {
            if (it == null) return@addSource
            viewModelScope.launch {
                val theme = globalSettingRepository.get(it.account).theme
                if (theme == GlobalSetting.THEME_OLD) {
                    this@apply.postValue(MainTheme.OLD)
                } else {
                    this@apply.postValue(MainTheme.NEW)
                }
            }
        }
    }
    val theme = _theme.toLiveData()

    private val _showMultiUserDialog = MutableLiveData<Boolean>()
    val showMultiUserDialog = _showMultiUserDialog.toLiveData()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            /* 初始化User */
            val user = userRepository.getUser()
            _user.postValue(user)
            /* 提交访问量 */
            val versionName = AppUtils.getAppVersionName()
            val versionCode = AppUtils.getAppVersionCode()
            val systemVersion = DeviceUtils.getSDKVersionCode()
            otherRepository.once(versionCode, versionName, systemVersion)
        }
    }

    fun getUsersLiveData(): LiveData<List<User>> =
        userRepository.getAllUsersLiveData()

    fun updateSetting() {
        viewModelScope.launch {
            val theme = globalSettingRepository.get().theme
            if (theme == GlobalSetting.THEME_OLD) {
                _theme.postValue(MainTheme.OLD)
            } else {
                _theme.postValue(MainTheme.NEW)
            }
        }
    }

    /**
     * 添加账号成功
     * 重新获取账号管理Dialog数据（配合LiveData自动更新数据，无需手动刷新），刷新界面元素
     */
    fun addAccountSuccess() {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user != null) {
                _checkoutResult.postValue(CheckoutResult.Success(user))
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            val currentUser = _user.value
            userRepository.deleteUserByAccount(user.account)
            if (user.account != currentUser?.account) { //不为当前用户，则提示删除成功
                _deleteResult.postValue(DeleteResult.Success)
                return@launch
            }
            //若删除的账号为当前正在使用账号，则切换到其他账号
            val nextUser = userRepository.getUser()
            if (nextUser == null) { //不存在其他账号，跳转到登录界面
                _deleteResult.postValue(DeleteResult.NeedLogin)
                return@launch
            } else {
                _deleteResult.postValue(DeleteResult.CheckTo(nextUser))
            }
        }
    }

    /**
     * 切换账号
     *
     * @param user 切换至[user]账号
     */
    fun checkoutTo(user: User) {
        _checkoutResult.value = CheckoutResult.Ing
        viewModelScope.launch {
            val currentUser = _user.value
            if (user.account == currentUser?.account) {
                _checkoutResult.postValue(CheckoutResult.Failure("正在使用:${user.account}，无需切换"))
                return@launch
            }
            userRepository.checkTo(user)
            _checkoutResult.postValue(CheckoutResult.Success(user))
            _user.postValue(user)
        }
    }

    /**
     * 检查更新
     */
    fun upgradeApp() {
        viewModelScope.launch {
            when (val res = otherRepository.getNewVersion()) {
                is Resource.Success -> {
                    if (res.data.versionCode <= AppUtils.getAppVersionCode()) {
                        this@MainViewModel.toastInMain("当前为最新版本")
                    } else {
                        this@MainViewModel.toastInMain("有更新！最新版本为:${res.data.versionName}\n若未自动更新，请前往ifafu官网手动更新")
                    }
                }
                is Resource.Failure -> {
                    this@MainViewModel.toastInMain(res.message)
                }
            }
        }
    }

    fun showMultiUserDialog() {
        _showMultiUserDialog.value = true
    }

    fun hideMultiUserDialog() {
        _showMultiUserDialog.value = false
    }
}