package cn.ifafu.ifafu.ui.information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.entity.UserInfo
import cn.ifafu.ifafu.exception.IFResponseFailureException
import cn.ifafu.ifafu.repository.IfUserRepository
import cn.ifafu.ifafu.repository.InformationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InformationViewModel @Inject constructor(
    private val userRepository: IfUserRepository,
    repository: InformationRepository
) : BaseViewModel() {

    val information = repository.query()

    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?>
        get() = _userInfo

    init {
        refreshUserInfo()
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            userRepository.userInfo()
                .flowOn(Dispatchers.IO)
                .catch {
                    if (it is IFResponseFailureException) {
                        _userInfo.postValue(null)
                    } else {
                        Timber.e(it)
                    }
                }
                .collectLatest {
                    _userInfo.postValue(it)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
                .flowOn(Dispatchers.IO)
                .catch { _userInfo.value = null }
                .collectLatest {
                    _userInfo.value = null
                }
        }
    }

}