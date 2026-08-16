package com.burootro.mailio.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.repository.MailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val recoveryKey: String = "",
    val isKeyBackedUp: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val autoDeleteDays: Long = 14L,
    val addressCount: Int = 0,
    val totalUnread: Int = 0,
    val isLoading: Boolean = true
)

sealed interface SettingsEvent {
    data class ShowMessage(val text: String) : SettingsEvent
    data object DataWiped : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: MailioPreferences,
    private val repository: MailRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.recoveryKey,
        prefs.isKeyBackedUp,
        prefs.notificationsEnabled,
        prefs.autoDeleteDays,
        repository.observeAddressCount()
    ) { key, backedUp, notifications, autoDelete, addressCount ->
        SettingsUiState(
            recoveryKey = key ?: "",
            isKeyBackedUp = backedUp,
            notificationsEnabled = notifications,
            autoDeleteDays = autoDelete,
            addressCount = addressCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    private val _events = MutableStateFlow<SettingsEvent?>(null)
    val events: StateFlow<SettingsEvent?> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.getOrCreateRecoveryKey()
        }
    }

    fun markKeyBackedUp() {
        viewModelScope.launch {
            prefs.setKeyBackedUp(true)
            _events.value = SettingsEvent.ShowMessage("تمام، المفتاح محفوظ عندك")
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setNotificationsEnabled(enabled)
        }
    }

    fun setAutoDeleteDays(days: Long) {
        viewModelScope.launch {
            prefs.setAutoDeleteDays(days)
            _events.value = SettingsEvent.ShowMessage(
                if (days <= 0) "اتوقف الحذف التلقائي"
                else "الرسايل هتتحذف بعد $days يوم"
            )
        }
    }

    fun runCleanupNow() {
        viewModelScope.launch {
            val days = uiState.value.autoDeleteDays
            if (days <= 0) {
                _events.value = SettingsEvent.ShowMessage("الحذف التلقائي متوقف")
                return@launch
            }
            repository.runAutoCleanup(days)
            _events.value = SettingsEvent.ShowMessage("اتنضفت الرسايل القديمة")
        }
    }

    fun wipeEverything() {
        viewModelScope.launch {
            repository.wipeEverything()
            _events.value = SettingsEvent.DataWiped
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
