package com.burootro.mailio.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.repository.MailRepository
import com.burootro.mailio.domain.model.AddressLifetime
import com.burootro.mailio.domain.model.MailAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val addresses: List<MailAddress> = emptyList(),
    val totalUnread: Int = 0,
    val isLoading: Boolean = true,
    val recoveryKey: String? = null
)

sealed interface HomeEvent {
    data class ShowMessage(val text: String) : HomeEvent
    data class AddressCreated(val email: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MailRepository,
    private val prefs: MailioPreferences
) : ViewModel() {

    /** الدومين المتاح — هيتغير للسيرفر لما نربط الباك إند */
    val availableDomains = listOf("mailio.app")

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeAddresses(),
        repository.observeTotalUnread(),
        prefs.recoveryKey
    ) { addresses, unread, key ->
        HomeUiState(
            addresses = addresses,
            totalUnread = unread,
            isLoading = false,
            recoveryKey = key
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _events = MutableStateFlow<HomeEvent?>(null)
    val events: StateFlow<HomeEvent?> = _events.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.getOrCreateRecoveryKey()
            prefs.getOrCreateDeviceId()
        }
    }

    fun createAddress(
        localPart: String? = null,
        label: String? = null,
        lifetime: AddressLifetime = AddressLifetime.PERMANENT,
        domain: String = availableDomains.first()
    ) {
        if (_isCreating.value) return

        viewModelScope.launch {
            _isCreating.value = true

            repository.createAddress(
                domain = domain,
                localPart = localPart,
                label = label,
                lifetime = lifetime
            ).fold(
                onSuccess = { address ->
                    _events.value = HomeEvent.AddressCreated(address.email)
                },
                onFailure = { error ->
                    _events.value = HomeEvent.ShowMessage(
                        error.message ?: "حصل خطأ، جرب تاني"
                    )
                }
            )

            _isCreating.value = false
        }
    }

    fun togglePin(address: MailAddress) {
        viewModelScope.launch {
            repository.setPinned(address.id, !address.isPinned)
        }
    }

    fun deleteAddress(address: MailAddress) {
        viewModelScope.launch {
            repository.deleteAddress(address.id)
            _events.value = HomeEvent.ShowMessage("اتحذف العنوان")
        }
    }

    fun renameAddress(address: MailAddress, newLabel: String?) {
        viewModelScope.launch {
            repository.renameAddress(address.id, newLabel)
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
