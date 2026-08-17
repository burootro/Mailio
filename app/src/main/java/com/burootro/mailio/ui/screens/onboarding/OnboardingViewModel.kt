package com.burootro.mailio.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingEvent {
    data object Registered : OnboardingEvent
    data class Error(val message: String) : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()

    private val _events = MutableStateFlow<OnboardingEvent?>(null)
    val events: StateFlow<OnboardingEvent?> = _events.asStateFlow()

    fun startNewAccount() {
        if (_isRegistering.value) return

        viewModelScope.launch {
            _isRegistering.value = true

            syncRepository.ensureRegistered().fold(
                onSuccess = {
                    _events.value = OnboardingEvent.Registered
                },
                onFailure = { error ->
                    val message = error.message ?: ""
                    _events.value = OnboardingEvent.Error(
                        when {
                            message.contains("timeout", true) ->
                                "السيرفر بيصحى، جرب تاني بعد شوية"
                            else -> "مفيش اتصال بالسيرفر، اتأكد من الإنترنت"
                        }
                    )
                }
            )

            _isRegistering.value = false
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
