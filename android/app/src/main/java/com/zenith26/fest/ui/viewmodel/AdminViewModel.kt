package com.zenith26.fest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.EventCreateRequest
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.StatsResponse
import com.zenith26.fest.data.remote.NetworkResult
import com.zenith26.fest.data.repository.ZenithRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: ZenithRepository = ZenithRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _stats = MutableStateFlow<StatsResponse?>(null)
    val stats: StateFlow<StatsResponse?> = _stats.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _registrations = MutableStateFlow<List<RegistrationDetail>>(emptyList())
    val registrations: StateFlow<List<RegistrationDetail>> = _registrations.asStateFlow()

    private val _selectedRegFilterEventId = MutableStateFlow<Int?>(null)
    val selectedRegFilterEventId: StateFlow<Int?> = _selectedRegFilterEventId.asStateFlow()

    private val _currentTab = MutableStateFlow(0) // 0: Events, 1: Registrations
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _showAddEventDialog = MutableStateFlow(false)
    val showAddEventDialog: StateFlow<Boolean> = _showAddEventDialog.asStateFlow()

    private val _pendingDeleteReg = MutableStateFlow<RegistrationDetail?>(null)
    val pendingDeleteReg: StateFlow<RegistrationDetail?> = _pendingDeleteReg.asStateFlow()

    private val _toast = MutableStateFlow<Pair<String, String>?>(null)
    val toast: StateFlow<Pair<String, String>?> = _toast.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val statsResult = repository.getStats()
            val eventsResult = repository.getEvents(category = null, search = null)
            val regsResult = repository.getRegistrations(_selectedRegFilterEventId.value)

            _isLoading.value = false

            if (statsResult is NetworkResult.Success) {
                _stats.value = statsResult.data
            }
            if (eventsResult is NetworkResult.Success) {
                // Sort by utilization percentage descending (exact logic as admin.js)
                _events.value = eventsResult.data.sortedByDescending { it.utilizationPct }
            }
            if (regsResult is NetworkResult.Success) {
                _registrations.value = regsResult.data
            }

            if (statsResult is NetworkResult.Error) {
                _errorMessage.value = statsResult.message
            } else if (eventsResult is NetworkResult.Error) {
                _errorMessage.value = eventsResult.message
            }
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun filterRegistrationsByEvent(eventId: Int?) {
        _selectedRegFilterEventId.value = eventId
        viewModelScope.launch {
            val result = repository.getRegistrations(eventId)
            if (result is NetworkResult.Success) {
                _registrations.value = result.data
            }
        }
    }

    fun openAddEventDialog() {
        _showAddEventDialog.value = true
    }

    fun dismissAddEventDialog() {
        _showAddEventDialog.value = false
    }

    fun createEvent(request: EventCreateRequest) {
        viewModelScope.launch {
            when (val res = repository.createEvent(request)) {
                is NetworkResult.Success -> {
                    _showAddEventDialog.value = false
                    _toast.value = Pair("🎉", "Event '${request.title}' created successfully!")
                    loadAllData()
                }
                is NetworkResult.Error -> {
                    _toast.value = Pair("⚠️", res.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun promptDeleteRegistration(reg: RegistrationDetail) {
        _pendingDeleteReg.value = reg
    }

    fun dismissDeleteDialog() {
        _pendingDeleteReg.value = null
    }

    fun confirmDeleteRegistration() {
        val reg = _pendingDeleteReg.value ?: return
        viewModelScope.launch {
            _pendingDeleteReg.value = null
            when (val res = repository.deleteRegistration(reg.id)) {
                is NetworkResult.Success -> {
                    _toast.value = Pair("🗑️", "Registration #${reg.id} cancelled.")
                    loadAllData()
                }
                is NetworkResult.Error -> {
                    _toast.value = Pair("⚠️", res.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearToast() {
        _toast.value = null
    }
}
