package com.burootro.mailio.ui.screens.inbox

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.repository.MailRepository
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.domain.model.MailMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InboxUiState(
    val address: MailAddress? = null,
    val messages: List<MailMessage> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
) {
    val filteredMessages: List<MailMessage>
        get() = if (searchQuery.isBlank()) messages
        else messages.filter { message ->
            message.subject.contains(searchQuery, ignoreCase = true) ||
                message.fromEmail.contains(searchQuery, ignoreCase = true) ||
                message.preview.contains(searchQuery, ignoreCase = true) ||
                (message.fromName?.contains(searchQuery, ignoreCase = true) == true)
        }

    val unreadCount: Int
        get() = messages.count { !it.isRead }
}

sealed interface InboxEvent {
    data class ShowMessage(val text: String) : InboxEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: MailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val addressId: String = checkNotNull(savedStateHandle["addressId"])

    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    private val addressFlow = MutableStateFlow<MailAddress?>(null)

    val uiState: StateFlow<InboxUiState> = combine(
        repository.observeMessages(addressId),
        addressFlow,
        _searchQuery,
        _isRefreshing
    ) { messages, address, query, refreshing ->
        InboxUiState(
            address = address,
            messages = messages,
            searchQuery = query,
            isLoading = false,
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InboxUiState()
    )

    private val _events = MutableStateFlow<InboxEvent?>(null)
    val events: StateFlow<InboxEvent?> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            addressFlow.value = repository.getAddress(addressId)
        }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markAllRead(addressId)
            _events.value = InboxEvent.ShowMessage("كل الرسايل اتقرت")
        }
    }

    fun toggleStar(message: MailMessage) {
        viewModelScope.launch {
            repository.setStarred(message.id, !message.isStarred)
        }
    }

    fun deleteMessage(message: MailMessage) {
        viewModelScope.launch {
            repository.deleteMessage(message.id)
            _events.value = InboxEvent.ShowMessage("اتحذفت الرسالة")
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // هنا هيتم سحب الرسايل من السيرفر لما نربط الباك إند
            kotlinx.coroutines.delay(800)
            addressFlow.value = repository.getAddress(addressId)
            _isRefreshing.value = false
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
