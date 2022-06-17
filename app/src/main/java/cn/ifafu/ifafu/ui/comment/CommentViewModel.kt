package cn.ifafu.ifafu.ui.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.ifafu.ifafu.bean.bo.CommentItem
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.entity.User
import cn.ifafu.ifafu.repository.CommentRepository
import cn.ifafu.ifafu.repository.UserRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    // 评教信息
    private val _listRes = MutableLiveData<Resource<List<CommentItem>>>()
    val listRes: LiveData<Resource<List<CommentItem>>> = _listRes

    // 是否所有评教都完成了（是否可以提交）
    private val _isAllCompleted = MutableLiveData(false)
    val isAllCompleted: LiveData<Boolean> = _isAllCompleted

    // 跳转至指定评教页面所需的Intent信息
    private val _jumpToItemComment = MutableLiveData<Map<String, String>>()
    val jumpToItemComment: LiveData<Map<String, String>> = _jumpToItemComment

    // 提交评教返回结果
    private val _autoCommentResult = MutableLiveData<Resource<String>>()
    val autoCommentResult: LiveData<Resource<String>> = _autoCommentResult

    // 提交评教返回结果
    private val _commitResult = MutableLiveData<Resource<Unit>>()
    val commitResult: LiveData<Resource<Unit>> = _commitResult

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _listRes.postValue(Resource.Loading("获取评教列表中"))
        val resource = commentRepository.getCommentList()
        resource.onSuccess { success ->
            val isAnyIncomplete = success.data.any { it.isDone == false }
            _isAllCompleted.postValue(!isAnyIncomplete)
        }
        _listRes.postValue(resource)
    }

    fun click(item: CommentItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.getUser() ?: return@launch
            if (user.school == User.FAFU) {
                val info = mapOf(
                    "title" to "评价教师【${item.teacherName}】",
                    "url" to item.commentFullUrl
                )
                _jumpToItemComment.postValue(info)
            }
        }
    }

    fun buttonClick() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isAllCompleted.value == true) {
                commit()
            } else {
                autoComment()
            }
        }
    }

    private suspend fun autoComment() {
        withContext(Dispatchers.IO) {
            _autoCommentResult.postValue(Resource.Loading("一键评教中"))
            val successRes = _listRes.value as? Resource.Success ?: return@withContext
            // 多线程提交数据
            val resources = successRes.data
                .filter { it.isDone == null || it.isDone == false }
                .map { item -> async { commentRepository.autoComment(item) } }
                .map { it.await() }
            // 更新列表
            successRes.data.forEachIndexed { index, commentItem ->
                commentItem.isDone = resources[index] is Resource.Success
            }
            var success = 0
            var failure = 0
            resources.forEach { res ->
                if (res is Resource.Success) {
                    success++
                } else {
                    failure++
                }
            }
            // 返回评教结果
            if (resources.isNotEmpty() && success == 0) {
                _autoCommentResult.postValue(Resource.Failure("一键评教失败，请联系iFAFU管理员修复问题"))
            } else if (failure == 0) {
                _autoCommentResult.postValue(Resource.Success("一键评教成功"))
            } else {
                _autoCommentResult.postValue(Resource.Failure("评教成功${success}门课，评教失败${failure}门课，请重试"))
            }
        }
    }

    private fun commit() {
        _commitResult.postValue(Resource.Loading("提交评教中"))
        viewModelScope.launch {
            val res = commentRepository.commit()
            _commitResult.postValue(res)
        }
    }


}