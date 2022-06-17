package cn.ifafu.ifafu.ui.electricity.main

import androidx.annotation.MainThread
import androidx.lifecycle.*
import cn.ifafu.ifafu.bean.bo.Dorm
import cn.ifafu.ifafu.bean.bo.ElecSelection
import cn.ifafu.ifafu.bean.bo.ElectricityFee
import cn.ifafu.ifafu.bean.vo.Event
import cn.ifafu.ifafu.bean.vo.Resource
import cn.ifafu.ifafu.entity.ElectricityHistory
import cn.ifafu.ifafu.repository.XfbRepository
import cn.ifafu.ifafu.util.toLiveData
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class ElectricityViewModel @Inject constructor(
    private val repository: XfbRepository,
) : ViewModel() {

    private var selections = emptyList<ElecSelection>()

    private val _defaultPickerOption = MutableLiveData<Pair<Int, Int>>()
    private val _queryOptions = MutableLiveData<Pair<List<String>, List<List<String>>>>()

    private val _startLoginActivity = MutableLiveData<Event<Unit>>()
    val startLoginActivity: LiveData<Event<Unit>> = _startLoginActivity

    val defaultPickerOption: LiveData<Pair<Int, Int>> = _defaultPickerOption
    val queryOptions: LiveData<Pair<List<String>, List<List<String>>>> = _queryOptions

    private val _building = MutableLiveData("")  //楼号
    val building: LiveData<String> = _building

    val numberOfDorm = MutableLiveData("") //宿舍号

    //学生卡余额
    private val _balanceOfStudentCard = MediatorLiveData<Resource<Double>>().apply {
        value = Resource.Success(0.0)
    }
    val balanceOfStudentCard = _balanceOfStudentCard.map {
        if (it is Resource.Success) "%.2f".format(it.data) else "-"
    }

    //电费余额
    private val _balanceOfElectricity = MediatorLiveData<Resource<ElectricityFee?>>()
    val balanceOfElectricity: LiveData<String> = MediatorLiveData<String>().apply {
        value = "- 度"
        addSource(_balanceOfElectricity) { res ->
            res.onSuccess { successRes ->
                val data = successRes.data
                value = if (data == null) {
                    "- 度"
                } else {
                    "${"%.2f".format(data.balance)} ${data.unitStr}"
                }
            }
        }
    }

    //电费查询加载动画
    val balanceOfElectricityLoading = _balanceOfElectricity.map {
        Event(it is Resource.Loading)
    }

    // Toast信息
    val toast: LiveData<Event<String>> = MediatorLiveData<Event<String>>().apply {
        addSource(_balanceOfElectricity) { res ->
            if (res is Resource.Failure) {
                value = Event(res.message)
            }
        }
    }

    //历史记录查询
    private val _history = MutableLiveData<Resource<List<ElectricityHistory>>>()
    val history: LiveData<List<ElectricityHistoryVO>> = _history.switchMap { res ->
        liveData(Dispatchers.IO) {
            if (res is Resource.Success) {
                var last = ElectricityHistory()
                val list = res.data.asReversed()
                    .map {
                        val l = last
                        last = it
                        ElectricityHistoryVO(
                            balance = it.balance,
                            time = it.timestamp,
                            unit = it.unitStr,
                            diff = it.balance - l.balance
                        )
                    }.asReversed()
                emit(list)
            }
        }
    }

    //每月电费增减情况
    val monthlyElectricity: LiveData<Pair<String, String>> =
        MediatorLiveData<Pair<String, String>>().apply {
            addSource(history) { data ->
                viewModelScope.launch(Dispatchers.IO) {
                    val c = Calendar.getInstance()
                    val month = c[Calendar.MONTH]
                    var add = 0.0
                    var minus = 0.0
                    for (history in data) {
                        c.timeInMillis = history.time
                        if (c[Calendar.MONTH] == month) {
                            if (history.diff > 0) {
                                add += history.diff
                            } else {
                                minus += history.diff
                            }
                        } else {
                            break
                        }
                    }
                    val first = data.getOrNull(0)
                    if (first == null) {
                        val str = " - "
                        this@apply.postValue(Pair(str, str))
                    } else {
                        val addStr = "%.2f %s".format(abs(add), first.unit)
                        val minusStr = "%.2f %s".format(abs(minus), first.unit)
                        this@apply.postValue(Pair(addStr, minusStr))
                    }
                }
            }
        }

    private val _showSettingsDialog = MutableLiveData<Event<Unit>>()
    val showSettingsDialog = _showSettingsDialog.toLiveData()

    val onOptionsSelectListener = OnOptionsSelectListener { o1, o2, _, _ ->
        _building.value = _queryOptions.value?.second?.getOrNull(o1)?.getOrNull(o2)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            /* 检查是否在登录态 */
            val isInLogin = repository.checkLoginStatus()
            if (!isInLogin) {
                _startLoginActivity.postValue(Event(Unit))
                return@launch
            }
            /* 获取宿舍楼选项 */
            selections = repository.getSelectionList()
            val group = selections
                .groupBy { it.areaAlias }
                .mapValues { entry ->
                    entry.value.map { it.buildingAlias }.sorted()
                }
            val group1 = group.keys.toList()
            val group2 = group.values.toList()
            _queryOptions.postValue(Pair(group1, group2))
            /* 获取历史查询选择 */
            val dorm = repository.getLastQueryDorm()
            if (dorm == null) {
                _showSettingsDialog.postValue(Event((Unit)))
                return@launch
            }
            val lastSelect = selections.find { it.id == dorm.id }
            if (lastSelect == null) {
                _showSettingsDialog.postValue(Event((Unit)))
                return@launch
            }
            _building.postValue(lastSelect.buildingAlias)
            numberOfDorm.postValue(dorm.dormNumber)
            withContext(Dispatchers.Main) {
                /* 查询校园卡余额 */
                queryCardBalance()
                /* 查询电费 */
                queryElecBalance()
                /* 查询电费历史 */
                queryHistory()
            }
            /* 初始化选择器默认选择位置 */
            val o1 = group1.indexOfFirst { it == lastSelect.areaAlias }
            val o2 = group2[o1].indexOfFirst { it == lastSelect.buildingAlias }
            if (o1 == -1 || o2 == -1) {
                return@launch
            }
            _defaultPickerOption.postValue(Pair(o1, o2))
        }
    }

    private var last: Dorm? = null

    @MainThread
    fun queryElecBalance() {
        viewModelScope.launch(Dispatchers.IO) {
            val id = selections.find { it.buildingAlias == building.value }?.id
            if (id == null) {
                _balanceOfElectricity.postValue(Resource.Failure("请选择宿舍楼"))
                return@launch
            }
            val dormNumber = this@ElectricityViewModel.numberOfDorm.value
            if (dormNumber == null) {
                _balanceOfElectricity.postValue(Resource.Failure("请输入宿舍号"))
                return@launch
            }

            val dorm = Dorm(id, dormNumber)
            //若查询的宿舍与上次不同，先清空数据
            if (last?.dormNumber != dormNumber || last?.id != id) {
                last = dorm
                _history.postValue(Resource.Success(emptyList()))
                _balanceOfElectricity.postValue(Resource.Success(null))
            }
            _balanceOfElectricity.postValue(Resource.Loading())
            val res = try {
                val balance = repository.queryElectricityBalance(dorm)
                Resource.Success(balance)
            } catch (e: Exception) {
                Timber.e(e)
                Resource.failure("电费查询出错")
            }
            _balanceOfElectricity.postValue(res)
            // 查完电费后，更新电费查询记录
            queryHistory()
        }
    }

    @MainThread
    fun queryCardBalance() {
        _balanceOfStudentCard.addSource(repository.queryCardBalance()) {
            _balanceOfStudentCard.value = it
        }
    }

    @MainThread
    fun queryHistory() {
        viewModelScope.launch {
            try {
                _history.postValue(Resource.Success(repository.getElectricityDefaultHistory()))
            } catch (e: Exception) {
                Timber.e(e)
                _history.postValue(Resource.failure("电费记录查询出错"))
            }
        }
    }


}