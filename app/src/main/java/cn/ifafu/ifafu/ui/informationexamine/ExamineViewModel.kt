package cn.ifafu.ifafu.ui.informationexamine

import androidx.lifecycle.*
import cn.ifafu.ifafu.annotation.InformationStatus
import cn.ifafu.ifafu.repository.ExamineRepository
import cn.ifafu.ifafu.ui.common.BaseViewModel
import cn.ifafu.ifafu.bean.vo.Event
import cn.ifafu.ifafu.bean.vo.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class ExamineViewModel @Inject constructor(
    private val repository: ExamineRepository
) : BaseViewModel() {

    val status = MutableLiveData<Int>()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val information = status.asFlow().flatMapLatest { status ->
        if (status == ExamineListFragment.STATUS_ALL) {
            repository.query()
        } else {
            repository.query(status)
        }
    }

    private val _examineResult = MediatorLiveData<Event<Resource<Boolean>>>()
    val examineResult: LiveData<Event<Resource<Boolean>>> = _examineResult

    fun changeStatus(id: Long, @InformationStatus status: Int) {
        _examineResult.value = Event(Resource.Loading())
        val resLD = repository.examine(id, status).asLiveData(Dispatchers.IO)
        _examineResult.addSource(resLD) {
            _examineResult.value = Event(it)
        }
    }

}