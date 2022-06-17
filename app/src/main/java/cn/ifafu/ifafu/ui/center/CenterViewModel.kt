package cn.ifafu.ifafu.ui.center

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.bo.Authority
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.entity.UserInfo
import cn.ifafu.ifafu.repository.IfUserRepository
import cn.ifafu.ifafu.repository.InformationRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CenterViewModel @Inject constructor(
    private val userRepository: IfUserRepository,
    private val informationRepository: InformationRepository
) : BaseViewModel() {

    val information = informationRepository.queryMy()

    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?>
        get() = _userInfo

    val canExamine = _userInfo.map {
        val authorities = it?.authorities
        authorities?.contains(Authority.EXAMINE) ?: false
    }

    private val _deleteResult = MutableLiveData<Resource<Boolean>>()
    val deleteResult: LiveData<Resource<Boolean>>
        get() = _deleteResult

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    private val _finish = MutableLiveData<Boolean>()
    val finish: LiveData<Boolean>
        get() = _finish

    init {
        refreshUserInfo()
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            userRepository.userInfo()
                .flowOn(Dispatchers.IO)
                .catch { _message.postValue("获取个人资料失败") }
                .collect { res ->
                    _userInfo.value = res
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
                .flowOn(Dispatchers.IO)
                .catch { _message.postValue("退出登录失败") }
                .collectLatest {
                    _userInfo.value = null
                }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            informationRepository.delete(id)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    if (e is IOException) {
                        _deleteResult.postValue(Resource.Failure("网络异常"))
                    } else {
                        _deleteResult.postValue(Resource.Failure("删除失败"))
                    }
                }
                .collectLatest {
                    _deleteResult.postValue(it)
                }
        }
    }
}