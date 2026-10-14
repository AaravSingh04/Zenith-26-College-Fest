package com.zenith26.fest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.remote.NetworkResult
import com.zenith26.fest.data.repository.ZenithRepository
import com.zenith26.fest.ui.screens.student.ModalAlert
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: ZenithRepository = ZenithRepository()
) : ViewModel() {

    private val _eventsState = MutableStateFlow<NetworkResult<List<Event>>>(NetworkResult.Loading)
    val eventsState: StateFlow<NetworkResult<List<Event>>> = _eventsState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _modalAlert = MutableStateFlow<ModalAlert?>(null)
    val modalAlert: StateFlow<ModalAlert?> = _modalAlert.asStateFlow()

    private val _toast = MutableStateFlow<Pair<String, String>?>(null)
    val toast: StateFlow<Pair<String, String>?> = _toast.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _eventsState.value = NetworkResult.Loading
            val result = repository.getEvents(
                category = _selectedCategory.value,
                search = _searchQuery.value
            )
            _eventsState.value = result
        }
    }

    fun setCategory(category: String) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            loadEvents()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            loadEvents()
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        loadEvents()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
        loadEvents()
    }

    fun openEventDetails(event: Event) {
        viewModelScope.launch {
            _modalAlert.value = if (event.isFull) {
                ModalAlert.Warning("Registration is closed because this event has reached maximum capacity.")
            } else null

            // Optimistically set clicked event
            _selectedEvent.value = event

            // Fetch live details from GET /api/events/{id}
            when (val res = repository.getEvent(event.id)) {
                is NetworkResult.Success -> {
                    _selectedEvent.value = res.data
                    if (res.data.isFull) {
                        _modalAlert.value = ModalAlert.Warning("Registration is closed because this event has reached maximum capacity.")
                    }
                }
                is NetworkResult.Error -> {
                    // Keep the local event if live fetch had issue, but show error if needed
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun dismissModal() {
        _selectedEvent.value = null
        _modalAlert.value = null
        _isSubmitting.value = false
    }

    fun submitRegistration(eventId: Int, request: RegistrationRequest) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _modalAlert.value = null

            val result = repository.registerForEvent(eventId, request)
            _isSubmitting.value = false

            when (result) {
                is NetworkResult.Success -> {
                    val evTitle = _selectedEvent.value?.title ?: "the event"
                    _modalAlert.value = ModalAlert.Success(
                        "🎉 Registration Confirmed! You are registered for $evTitle. Confirmation ID: #${result.data.id}"
                    )
                    _toast.value = Pair("🎉", "Registration successful for ${request.studentName}!")

                    // Refresh event list to update seat counts live
                    loadEvents()

                    // Also refresh the modal event's live available seat count
                    val updatedEvent = repository.getEvent(eventId)
                    if (updatedEvent is NetworkResult.Success) {
                        _selectedEvent.value = updatedEvent.data
                    }
                }
                is NetworkResult.Error -> {
                    // Exact error returned by FastAPI backend (e.g. "Already registered", "Event is full", 400 validation error)
                    _modalAlert.value = ModalAlert.Error(result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearToast() {
        _toast.value = null
    }
}
