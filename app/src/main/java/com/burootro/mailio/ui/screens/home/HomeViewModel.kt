package com.burootro.mailio.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.repository.MailRepository
import com.burootro.mailio.data.repository.SyncRepository
import com.burootro.mailio.domain.model.AddressLifetime
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.notifications.MailioNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val addresses: List<MailAddress> = emptyList(),
    val totalUnread: Int = 0,
    val isLoading: Boolean = true,
    val recoveryKey: String? = null,
    val isSyncing: Boolean = false,
    val isConnected: Boolean = true
)

sealed interface HomeEvent {
    data class ShowMessage(val text: String) : HomeEvent
    data class AddressCreated(val addressId: String, val email: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MailRepository,
    private val syncRepository: SyncRepository,
    private val notifier: MailioNotifier,
    private val prefs: MailioPreferences
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _isConnected = MutableStateFlow(true)

    val availableDomains: List<String> = listOf("mailsio.uk")

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeAddresses(),
        repository.observeTotalUnread(),
        prefs.recoveryKey,
        _isSyncing,
        _isConnected
    ) { addresses, unread, key, syncing, connected ->
        HomeUiState(
            addresses = addresses,
            totalUnread = unread,
            isLoading = false,
            recoveryKey = key,
            isSyncing = syncing,
            isConnected = connected
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
            prefs.getOrCreateDeviceId()
            // بنصحّي السيرفر على طول عشان الطلب الجاي يبقى سريع
            syncRepository.wakeServer()
            syncIfRegistered()
        }

        viewModelScope.launch {
            while (true) {
                delay(20_000)
                quietSync()
            }
        }
    }

    private suspend fun syncIfRegistered() {
        if (prefs.getRecoveryKey() == null) return

        _isSyncing.value = true

        syncRepository.syncAddresses()
        syncRepository.syncMessages().fold(
            onSuccess = { _isConnected.value = true },
            onFailure = { _isConnected.value = false }
        )

        _isSyncing.value = false
    }

    fun refresh() {
        viewModelScope.launch {
            if (prefs.getRecoveryKey() == null) return@launch

            _isSyncing.value = true

            syncRepository.syncAddresses()
            syncRepository.syncMessages().fold(
                onSuccess = { newMessages ->
                    _isConnected.value = true
                    if (newMessages.isNotEmpty()) {
                        _events.value = HomeEvent.ShowMessage(
                            "وصل ${newMessages.size} رسالة جديدة"
                        )
                    }
                },
                onFailure = {
                    _isConnected.value = false
                }
            )

            _isSyncing.value = false
        }
    }

    /**
     * مزامنة صامتة — بتطلق إشعار لكل رسالة جديدة
     */
    private suspend fun quietSync() {
        if (_isSyncing.value) return
        if (prefs.getRecoveryKey() == null) return

        val notificationsOn = prefs.notificationsEnabled.first()

        syncRepository.syncMessages().fold(
            onSuccess = { newMessages ->
                _isConnected.value = true
                if (notificationsOn) {
                    newMessages.forEach { message ->
                        notifier.notifyNewMessage(message)
                    }
                }
            },
            onFailure = { _isConnected.value = false }
        )
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

            syncRepository.createAddress(
                localPart = localPart,
                label = label,
                lifetime = lifetime,
                domain = domain
            ).fold(
                onSuccess = { created ->
                    _isConnected.value = true
                    _events.value = HomeEvent.AddressCreated(
                        addressId = created.id,
                        email = created.email
                    )
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
            syncRepository.deleteAddress(address.id)
            _events.value = HomeEvent.ShowMessage("اتحذف العنوان")
        }
    }

    fun renameAddress(address: MailAddress, newLabel: String?) {
        viewModelScope.launch {
            syncRepository.updateLabel(address.id, newLabel)
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
