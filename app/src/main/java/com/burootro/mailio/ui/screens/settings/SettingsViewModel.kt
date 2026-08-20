package com.burootro.mailio.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.repository.AuthRepository
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
    val googleEmail: String? = null,
    val googleName: String? = null,
    val googlePhoto: String? = null,
    val notificationsEnabled: Boolean = true,
    val autoDeleteDays: Long = 14L,
    val addressCount: Int = 0,
    val isLoading: Boolean = true
)

sealed interface SettingsEvent {
    data class ShowMessage(val text: String) : SettingsEvent
    data object SignedOut : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: MailioPreferences,
    private val repository: MailRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val profileFlow = combine(
        prefs.googleEmail,
        prefs.googleName,
        prefs.googlePhoto
    ) { email, name, photo ->
        Triple(email, name, photo)
    }

    private val settingsFlow = combine(
        prefs.notificationsEnabled,
        prefs.autoDeleteDays,
        repository.observeAddressCount()
    ) { notifications, days, count ->
        Triple(notifications, days, count)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        profileFlow,
        settingsFlow
    ) { profile, settings ->
        SettingsUiState(
            googleEmail = profile.first,
            googleName = profile.second,
            googlePhoto = profile.third,
            notificationsEnabled = settings.first,
            autoDeleteDays = settings.second,
            addressCount = settings.third,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    private val _events = MutableStateFlow<SettingsEvent?>(null)
    val events: StateFlow<SettingsEvent?> = _events.asStateFlow()

    private val _isSigningOut = MutableStateFlow(false)
    val isSigningOut: StateFlow<Boolean> = _isSigningOut.asStateFlow()

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

    fun signOut() {
        if (_isSigningOut.value) return

        viewModelScope.launch {
            _isSigningOut.value = true

            authRepository.signOut().fold(
                onSuccess = { _events.value = SettingsEvent.SignedOut },
                onFailure = {
                    _events.value = SettingsEvent.ShowMessage("فشل تسجيل الخروج")
                }
            )

            _isSigningOut.value = false
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
