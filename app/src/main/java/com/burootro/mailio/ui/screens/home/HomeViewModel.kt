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

/** حالة نافذة النقل */
data class TransferState(
    val addressId: String = "",
    val email: String = "",
    val code: String? = null,
    val expiresAt: Long? = null,
    val isLoading: Boolean = false
)

/** حالة نافذة الاستلام */
data class ClaimState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface HomeEvent {
    data class ShowMessage(val text: String) : HomeEvent
    data class AddressCreated(val addressId: String, val email: String) : HomeEvent
    data class AddressClaimed(val addressId: String, val email: String) : HomeEvent
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

    private val _transferState = MutableStateFlow<TransferState?>(null)
    val transferState: StateFlow<TransferState?> = _transferState.asStateFlow()

    private val _claimState = MutableStateFlow<ClaimState?>(null)
    val claimState: StateFlow<ClaimState?> = _claimState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.getOrCreateDeviceId()
            syncRepository.wakeServer()
            syncRepository.registerPushToken()
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
                onFailure = { _isConnected.value = false }
            )

            _isSyncing.value = false
        }
    }

    private suspend fun quietSync() {
        if (_isSyncing.value) return
        if (prefs.getRecoveryKey() == null) return

        syncRepository.syncAddresses()
        syncRepository.syncMessages().fold(
            onSuccess = { _isConnected.value = true },
            onFailure = { _isConnected.value = false }
        )
    }

    // ===== إنشاء العناوين =====

    fun createAddress(
        localPart: String? = null,
        label: String? = null,
        lifetime: AddressLifetime = AddressLifetime.PERMANENT,
        domain: String = availableDomains.first()
    ) {
        if (_isCreating.value) return

        viewModelScope.launch {
            _isCreating.value = true

            val before = repository.observeAddresses().first().map { it.id }.toSet()

            syncRepository.createAddress(
                localPart = localPart,
                label = label,
                lifetime = lifetime,
                domain = domain
            ).fold(
                onSuccess = { created ->
                    _isConnected.value = true
                    _events.value = HomeEvent.AddressCreated(created.id, created.email)
                },
                onFailure = { error ->
                    val recovered = recoverCreatedAddress(before)

                    if (recovered != null) {
                        _isConnected.value = true
                        _events.value = HomeEvent.AddressCreated(
                            recovered.id,
                            recovered.email
                        )
                    } else {
                        _events.value = HomeEvent.ShowMessage(
                            error.message ?: "حصل خطأ، جرب تاني"
                        )
                    }
                }
            )

            _isCreating.value = false
        }
    }

    private suspend fun recoverCreatedAddress(beforeIds: Set<String>): MailAddress? {
        repeat(3) {
            delay(2000)

            val synced = syncRepository.syncAddresses()

            if (synced.isSuccess) {
                val after = repository.observeAddresses().first()
                val newOne = after.firstOrNull { it.id !in beforeIds }
                if (newOne != null) return newOne
            }
        }
        return null
    }

    // ===== النقل =====

    fun openTransfer(address: MailAddress) {
        _transferState.value = TransferState(
            addressId = address.id,
            email = address.email
        )
    }

    fun closeTransfer() {
        _transferState.value = null
    }

    fun generateTransferCode() {
        val current = _transferState.value ?: return
        if (current.isLoading) return

        viewModelScope.launch {
            _transferState.value = current.copy(isLoading = true)

            syncRepository.startTransfer(current.addressId).fold(
                onSuccess = { result ->
                    _transferState.value = current.copy(
                        code = result.code,
                        expiresAt = result.expiresAt,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _transferState.value = current.copy(isLoading = false)
                    _events.value = HomeEvent.ShowMessage(
                        error.message ?: "فشل توليد الكود"
                    )
                }
            )
        }
    }

    fun cancelTransfer() {
        val current = _transferState.value ?: return

        viewModelScope.launch {
            syncRepository.cancelTransfer(current.addressId)
            _transferState.value = null
            _events.value = HomeEvent.ShowMessage("اتلغى النقل")
        }
    }

    // ===== الاستلام =====

    fun openClaim() {
        _claimState.value = ClaimState()
    }

    fun closeClaim() {
        _claimState.value = null
    }

    fun clearClaimError() {
        _claimState.value = _claimState.value?.copy(error = null)
    }

    fun claimAddress(code: String) {
        val current = _claimState.value ?: return
        if (current.isLoading) return

        viewModelScope.launch {
            _claimState.value = ClaimState(isLoading = true)

            syncRepository.claimTransfer(code).fold(
                onSuccess = { claimed ->
                    _claimState.value = null
                    _events.value = HomeEvent.AddressClaimed(claimed.id, claimed.email)
                },
                onFailure = { error ->
                    _claimState.value = ClaimState(
                        error = error.message ?: "فشل الاستلام"
                    )
                }
            )
        }
    }

    // ===== باقي العمليات =====

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
