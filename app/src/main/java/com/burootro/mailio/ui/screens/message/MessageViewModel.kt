package com.burootro.mailio.ui.screens.message

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.repository.MailRepository
import com.burootro.mailio.domain.model.MailAttachment
import com.burootro.mailio.domain.model.MailMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageUiState(
    val message: MailMessage? = null,
    val attachments: List<MailAttachment> = emptyList(),
    val showHtml: Boolean = true,
    val isLoading: Boolean = true
)

sealed interface MessageEvent {
    data class ShowToast(val text: String) : MessageEvent
    data object MessageDeleted : MessageEvent
}

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: MailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val messageId: String = checkNotNull(savedStateHandle["messageId"])

    private val _showHtml = MutableStateFlow(true)

    val uiState: StateFlow<MessageUiState> = combine(
        repository.observeMessage(messageId),
        repository.observeAttachments(messageId),
        _showHtml
    ) { message, attachments, showHtml ->
        MessageUiState(
            message = message,
            attachments = attachments,
            showHtml = showHtml,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MessageUiState()
    )

    private val _events = MutableStateFlow<MessageEvent?>(null)
    val events: StateFlow<MessageEvent?> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            repository.markRead(messageId, true)
        }
    }

    fun toggleStar() {
        viewModelScope.launch {
            val current = uiState.value.message ?: return@launch
            repository.setStarred(current.id, !current.isStarred)
        }
    }

    fun markUnread() {
        viewModelScope.launch {
            repository.markRead(messageId, false)
            _events.value = MessageEvent.ShowToast("اترجعت غير مقروءة")
        }
    }

    fun toggleView() {
        _showHtml.value = !_showHtml.value
    }

    fun delete() {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
            _events.value = MessageEvent.MessageDeleted
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
