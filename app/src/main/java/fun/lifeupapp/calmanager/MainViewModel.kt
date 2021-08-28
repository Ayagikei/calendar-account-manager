package `fun`.lifeupapp.calmanager

import `fun`.lifeupapp.calmanager.common.Resource
import `fun`.lifeupapp.calmanager.common.Resource.Companion.isError
import `fun`.lifeupapp.calmanager.datasource.CalendarDataSource
import `fun`.lifeupapp.calmanager.datasource.data.CalendarModel
import `fun`.lifeupapp.calmanager.utils.logD
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import splitties.init.appCtx


/**
 * main view model to receive data from datasource
 *
 * MIT License
 * Copyright (c) 2021 AyagiKei
 */
class MainViewModel : ViewModel(), LifecycleObserver {
    private val _calendarList = MutableStateFlow<Resource<List<CalendarModel>>>(Resource.Loading)
    val calendarList: StateFlow<Resource<List<CalendarModel>>> = _calendarList

    private var lastJob: Job? = null

    companion object{
        private const val TAG = "MainViewModel"
    }

    init {
        fetch()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun fetchIfError() {
        viewModelScope.launch(Dispatchers.IO) {
            if (calendarList.value.isError()){
                fetch()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun fetch() {
         viewModelScope.launch(Dispatchers.IO) {
             logD(TAG, "fetching calendar accounts list")
             if(lastJob?.isActive == true){
                 return@launch
             }
             lastJob = this.coroutineContext.job
             CalendarDataSource.listAccounts(appCtx).onSuccess {
                 _calendarList.value = Resource.success(it)
             }.onFailure {
                 _calendarList.value = Resource.error(it)
             }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            CalendarDataSource.deleteTheAccount(appCtx, id)
            fetch()
        }
    }
}