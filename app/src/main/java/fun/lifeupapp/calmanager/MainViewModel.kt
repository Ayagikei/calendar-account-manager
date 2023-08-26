package `fun`.lifeupapp.calmanager

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `fun`.lifeupapp.calmanager.common.Resource
import `fun`.lifeupapp.calmanager.common.Resource.Companion.isError
import `fun`.lifeupapp.calmanager.datasource.CalendarDataSource
import `fun`.lifeupapp.calmanager.datasource.data.CalendarModel
import `fun`.lifeupapp.calmanager.utils.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import splitties.init.appCtx

/**
 * main view model to receive data from datasource
 *
 * MIT License
 * Copyright (c) 2023 AyagiKei
 */
class MainViewModel : ViewModel(), LifecycleObserver {
    private val _calendarList = MutableStateFlow<Resource<List<CalendarModel>>>(Resource.Loading)
    val calendarList: StateFlow<Resource<List<CalendarModel>>> = _calendarList

    private var lastJob: Job? = null

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            fetchIfError()
        }
    }

    init {
        fetch()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    fun fetchIfError() {
        viewModelScope.launch(Dispatchers.IO) {
            if (calendarList.value.isError()) {
                fetch()
            }
        }
    }

    private fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            if (lastJob?.isActive == true) {
                return@launch
            }
            logD(TAG, "fetching calendar accounts list")
            lastJob = this.coroutineContext.job
            CalendarDataSource.listAccounts(appCtx).onSuccess {
                _calendarList.value = Resource.success(it)
            }.onFailure {
                _calendarList.value = Resource.error(it)
            }
        }
    }

    /**
     * delete calendar account with [id]
     */
    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            CalendarDataSource.deleteTheAccount(appCtx, id)
            fetch()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }
}