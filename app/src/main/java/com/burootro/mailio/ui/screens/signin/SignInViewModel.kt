package com.burootro.mailio.ui.screens.signin

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.repository.AuthRepository
import com.burootro.mailio.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInUiState(
    val isLoading: Boolean = false,
    val isSkipping: Boolean = false,
    val isRestoring: Boolean = false,
    val restoreError: String? = null,
    val error: String? = null
) {
    val isBusy: Boolean get() = isLoading || isSkipping || isRestoring
}

sealed interface SignInEvent {
    data class Success(val isNew: Boolean, val name: String?) : SignInEvent
    data object GuestReady : SignInEvent
    data class Restored(val addresses: Int) : SignInEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<SignInEvent?>(null)
    val events: StateFlow<SignInEvent?> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            syncRepository.wakeServer()
        }
    }

    fun getSignInIntent(): Intent = authRepository.getSignInIntent()

    fun onSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = SignInUiState(isLoading = true)

            authRepository.signInWithGoogle(data).fold(
                onSuccess = { result ->
                    _uiState.value = SignInUiState()
                    syncRepository.registerPushToken()

                    _events.value = SignInEvent.Success(
                        isNew = result.isNew,
                        name = result.name
                    )
                },
                onFailure = { error ->
                    _uiState.value = SignInUiState(
                        error = error.message ?: "فشل تسجيل الدخول"
                    )
                }
            )
        }
    }

    /**
     * الدخول كضيف — بيعمل حساب مجهول
     */
    fun continueAsGuest() {
        if (_uiState.value.isSkipping) return

        viewModelScope.launch {
            _uiState.value = SignInUiState(isSkipping = true)

            syncRepository.ensureRegistered().fold(
                onSuccess = {
                    _uiState.value = SignInUiState()
                    syncRepository.registerPushToken()
                    _events.value = SignInEvent.GuestReady
                },
                onFailure = { error ->
                    _uiState.value = SignInUiState(
                        error = error.message ?: "مفيش اتصال بالسيرفر"
                    )
                }
            )
        }
    }

    /**
     * Restore a guest account on a new phone using its recovery key (MLO-XXXX-XXXX-XXXX-XXXX).
     */
    fun restoreWithKey(rawKey: String) {
        if (_uiState.value.isRestoring) return
        val key = rawKey.trim().uppercase().replace(" ", "")

        if (!Regex("^MLO(-[A-Z0-9]{4}){4}$").matches(key)) {
            _uiState.value = _uiState.value.copy(restoreError = "المفتاح شكله مش مظبوط — لازم يبدأ بـ MLO-")
            return
        }

        viewModelScope.launch {
            _uiState.value = SignInUiState(isRestoring = true)

            syncRepository.restoreAccount(key).fold(
                onSuccess = { result ->
                    _uiState.value = SignInUiState()
                    syncRepository.registerPushToken()
                    _events.value = SignInEvent.Restored(result.addressesUpdated)
                },
                onFailure = { error ->
                    val message = error.message.orEmpty()
                    _uiState.value = SignInUiState(
                        restoreError = when {
                            message.contains("404") || message.contains("401") -> "المفتاح ده مش موجود"
                            message.contains("403") -> "الحساب ده موقوف"
                            else -> "مفيش اتصال بالسيرفر، جرّب تاني"
                        }
                    )
                }
            )
        }
    }

    fun clearRestoreError() {
        _uiState.value = _uiState.value.copy(restoreError = null)
    }

    fun onSignInCancelled() {
        _uiState.value = SignInUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun consumeEvent() {
        _events.value = null
    }
}
