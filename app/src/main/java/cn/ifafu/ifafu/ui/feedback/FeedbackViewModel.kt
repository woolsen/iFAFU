package cn.ifafu.ifafu.ui.feedback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: FeedbackRepository
) : BaseViewModel() {

    val contact = MutableLiveData<String>()
    val content = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    private val _result = MutableLiveData<Resource<String>>()
    val result: LiveData<Resource<String>> = _result

    private var lastUploadTime = 0L
    private var lastUploadContent = ""

    fun submit() {
        val content = content.value
        val contact = contact.value
        if (content.isNullOrBlank()) {
            toast("请输入反馈内容")
        } else if (contact.isNullOrBlank()) {
            toast("请输入联系方式")
        } else if (System.currentTimeMillis() - lastUploadTime < 10000) {
            toast("请勿频繁提交，10秒后再试")
        } else if (lastUploadContent == content) {
            toast("反馈已提交，请勿重复提交")
        } else {
            viewModelScope.launch {
                when (val res = repository.feedback(content, contact)) {
                    is Resource.Success -> {
                        lastUploadContent = content
                        lastUploadTime = System.currentTimeMillis()
                        toastInMain("提交反馈成功，收到后会第一时间处理~")
                    }
                    is Resource.Failure -> {
                        toastInMain(res.message)
                    }
                }
            }
        }
    }

}