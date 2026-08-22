package com.burootro.mailio.ui.screens.appeals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.repository.SyncRepository
import com.burootro.mailio.domain.model.Appeal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyAppealsUiState(
    val appeals: List<Appeal> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MyAppealsViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAppealsUiState())
    val uiState: StateFlow<MyAppealsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            syncRepository.getMyAppeals().fold(
                onSuccess = { appeals ->
                    _uiState.value = MyAppealsUiState(
                        appeals = appeals,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = MyAppealsUiState(
                        isLoading = false,
                        error = error.message ?: "مفيش اتصال بالسيرفر"
                    )
                }
            )
        }
    }
}
