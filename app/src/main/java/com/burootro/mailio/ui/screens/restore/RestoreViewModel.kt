package com.burootro.mailio.ui.screens.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestoreUiState(
    val key: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** الصيغة الصحيحة: MLO-XXXX-XXXX-XXXX-XXXX */
    val isValidFormat: Boolean
        get() = Regex("^MLO-[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}$")
            .matches(key.trim().uppercase())
}

sealed interface RestoreEvent {
    data class Success(val addresses: Int, val messages: Int) : RestoreEvent
}

@HiltViewModel
class RestoreViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestoreUiState())
    val uiState: StateFlow<RestoreUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<RestoreEvent?>(null)
    val events: StateFlow<RestoreEvent?> = _events.asStateFlow()

    fun onKeyChange(input: String) {
        // تنسيق تلقائي: حروف كبيرة وشرطات كل 4 خانات
        val clean = input.uppercase().filter { it.isLetterOrDigit() }

        val formatted = buildString {
            val body = if (clean.startsWith("MLO")) clean.drop(3) else clean
            append("MLO")

            body.take(16).forEachIndexed { index, char ->
                if (index % 4 == 0) append('-')
                append(char)
            }
        }

        _uiState.value = _uiState.value.copy(
            key = formatted,
            error = null
        )
    }

    fun clearKey() {
        _uiState.value = RestoreUiState()
    }

    fun restore() {
        val state = _uiState.value

        if (!state.isValidFormat) {
            _uiState.value = state.copy(
                error = "المفتاح لازم يكون بالشكل MLO-XXXX-XXXX-XXXX-XXXX"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            syncRepository.restoreAccount(state.key).fold(
                onSuccess = { result ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.value = RestoreEvent.Success(
                        addresses = result.addressesUpdated,
                        messages = result.newMessages
                    )
                },
                onFailure = { error ->
                    val message = error.message ?: ""
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = when {
                            message.contains("404") -> "المفتاح ده مش موجود عندنا"
                            message.contains("403") -> "الحساب ده متوقف"
                            message.contains("timeout", true) ->
                                "السيرفر بيصحى، جرب تاني بعد شوية"
                            else -> "مفيش اتصال بالسيرفر"
                        }
                    )
                }
            )
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
