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
import `fun`.lifeupapp.calmanager.ui.page.home.DeleteConfirmDialogState
import `fun`.lifeupapp.calmanager.utils.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _shouldShownRateUs = MutableStateFlow(true)
    val shouldShownRateUs: StateFlow<Boolean> = _shouldShownRateUs

    private val _deleteConfirmDialogState =
        MutableStateFlow(DeleteConfirmDialogState(emptyList(), 3))
    val deleteConfirmDialogState: StateFlow<DeleteConfirmDialogState> = _deleteConfirmDialogState

    val enterSelectMode = MutableStateFlow(false)

    val multiSelectHint = MutableStateFlow(true)

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            fetch()
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

    fun hasShownRateUs() {
        _shouldShownRateUs.value = false
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    fun deleteItem(calendarModels: List<CalendarModel>) {
        viewModelScope.launch {
            _deleteConfirmDialogState.value = DeleteConfirmDialogState(
                calendarModels,
                3
            )
            launch {
                for (i in 3 downTo 0) {
                    if (_deleteConfirmDialogState.value.calendars.isNotEmpty()) {
                        _deleteConfirmDialogState.value = _deleteConfirmDialogState.value.copy(
                            countdown = i
                        )
                        delay(1000)
                    }
                }
            }
        }
    }

    fun dismissDeleteConfirmDialog() {
        viewModelScope.launch {
            _deleteConfirmDialogState.value = DeleteConfirmDialogState(emptyList(), 3)
        }
    }

    fun confirmDelete(list: List<CalendarModel>) {
        viewModelScope.launch {
            list.forEach {
                delete(it.id)
            }
            dismissDeleteConfirmDialog()
        }
    }


    fun toggleSelect(calendarModel: CalendarModel) {
        viewModelScope.launch {
            val data = calendarList.value as? Resource.Success ?: return@launch
            val value = data.copy(
                item = data.item.map {
                    if (it.id == calendarModel.id) {
                        it.copy(isSelected = !it.isSelected)
                    } else {
                        it
                    }
                }
            )
            _calendarList.value = value
            enterSelectMode.value = value.item.any { it.isSelected }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val data = calendarList.value as? Resource.Success ?: return@launch
            val selectedItems = data.item.filter { it.isSelected }
            deleteItem(selectedItems)
            unselectedAll()
        }
    }

    fun unselectedAll() {
        viewModelScope.launch {
            val data = calendarList.value as? Resource.Success ?: return@launch
            _calendarList.value = data.copy(
                item = data.item.map {
                    it.copy(isSelected = false)
                }
            )
            enterSelectMode.value = false
        }
    }

    fun shownMultiSelectHint() {
        viewModelScope.launch {
            multiSelectHint.value = false
        }
    }
}