package com.burootro.mailio.data.repository

import com.burootro.mailio.data.local.dao.AddressDao
import com.burootro.mailio.data.local.dao.MessageDao
import com.burootro.mailio.data.mapper.toAddressEntities
import com.burootro.mailio.data.mapper.toEntity
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.remote.MailioApi
import com.burootro.mailio.data.remote.dto.CancelTransferRequest
import com.burootro.mailio.data.remote.dto.ClaimTransferRequest
import com.burootro.mailio.data.remote.dto.CreateAddressRequest
import com.burootro.mailio.data.remote.dto.PushTokenRequest
import com.burootro.mailio.data.remote.dto.RestoreRequest
import com.burootro.mailio.data.remote.dto.StartTransferRequest
import com.burootro.mailio.data.remote.dto.UpdateLabelRequest
import com.burootro.mailio.domain.model.AddressLifetime
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class SyncResult(
    val newMessages: Int = 0,
    val addressesUpdated: Int = 0
)

data class CreatedAddress(
    val id: String,
    val email: String
)

data class NewMessageInfo(
    val id: String,
    val addressId: String,
    val fromName: String,
    val subject: String,
    val preview: String
)

data class TransferCode(
    val code: String,
    val email: String,
    val expiresAt: Long
)

@Singleton
class SyncRepository @Inject constructor(
    private val api: MailioApi,
    private val addressDao: AddressDao,
    private val messageDao: MessageDao,
    private val prefs: MailioPreferences
) {

    suspend fun wakeServer() = withContext(Dispatchers.IO) {
        try {
            api.health()
        } catch (e: Exception) {
            // مش مهم
        }
    }

    suspend fun registerPushToken(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (prefs.getRecoveryKey() == null) {
                return@withContext Result.success(Unit)
            }

            val token = prefs.getPushToken() ?: fetchFirebaseToken()

            if (token.isNullOrBlank()) {
                return@withContext Result.failure(Exception("مفيش توكن"))
            }

            prefs.setPushToken(token)
            api.registerPushToken(PushTokenRequest(token))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchFirebaseToken(): String? =
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    cont.resume(if (task.isSuccessful) task.result else null)
                }
        }

    private suspend fun <T> retrying(
        attempts: Int = 3,
        block: suspend () -> T
    ): T {
        var lastError: Exception? = null

        repeat(attempts) { index ->
            try {
                return block()
            } catch (e: Exception) {
                lastError = e
                if (index < attempts - 1) {
                    delay(2500L * (index + 1))
                }
            }
        }

        throw lastError ?: Exception("فشل الاتصال")
    }

    suspend fun ensureRegistered(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val existing = prefs.getRecoveryKey()
            if (existing != null) {
                return@withContext Result.success(existing)
            }

            val response = retrying { api.register() }
            prefs.setRecoveryKey(response.recoveryKey)

            if (response.domains.isNotEmpty()) {
                prefs.setPreferredDomain(response.domains.first())
            }

            Result.success(response.recoveryKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreAccount(recoveryKey: String): Result<SyncResult> =
        withContext(Dispatchers.IO) {
            try {
                val key = recoveryKey.trim().uppercase()
                val response = retrying { api.restore(RestoreRequest(key)) }

                prefs.setRecoveryKey(key)
                prefs.setKeyBackedUp(true)

                if (response.domains.isNotEmpty()) {
                    prefs.setPreferredDomain(response.domains.first())
                }

                messageDao.deleteAll()
                addressDao.deleteAll()

                addressDao.insertAll(response.addresses.toAddressEntities())
                messageDao.insertAll(response.messages.map { it.toEntity(isRead = false) })

                refreshAllUnreadCounts()

                Result.success(
                    SyncResult(
                        newMessages = response.messages.size,
                        addressesUpdated = response.addresses.size
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createAddress(
        localPart: String?,
        label: String?,
        lifetime: AddressLifetime,
        domain: String?
    ): Result<CreatedAddress> = withContext(Dispatchers.IO) {
        try {
            val response = api.createAddress(
                CreateAddressRequest(
                    localPart = localPart,
                    label = label,
                    lifetimeMs = lifetime.millis,
                    domain = domain
                )
            )

            addressDao.insert(response.address.toEntity())

            Result.success(
                CreatedAddress(
                    id = response.address.id,
                    email = response.address.email
                )
            )
        } catch (e: Exception) {
            Result.failure(mapError(e))
        }
    }

    // ===== النقل =====

    /**
     * طلب كود نقل للعنوان
     */
    suspend fun startTransfer(addressId: String): Result<TransferCode> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.startTransfer(StartTransferRequest(addressId))

                Result.success(
                    TransferCode(
                        code = response.code,
                        email = response.email,
                        expiresAt = response.expiresAt
                    )
                )
            } catch (e: Exception) {
                Result.failure(mapTransferError(e))
            }
        }

    suspend fun cancelTransfer(addressId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                api.cancelTransfer(CancelTransferRequest(addressId))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(mapTransferError(e))
            }
        }

    /**
     * استلام عنوان بكود نقل
     */
    suspend fun claimTransfer(code: String): Result<CreatedAddress> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.claimTransfer(
                    ClaimTransferRequest(code.trim().uppercase())
                )

                // نسحب العناوين من السيرفر عشان الجديد يظهر
                syncAddresses()

                Result.success(
                    CreatedAddress(
                        id = response.addressId,
                        email = response.email
                    )
                )
            } catch (e: Exception) {
                Result.failure(mapTransferError(e))
            }
        }

    // ===== باقي العمليات =====

    suspend fun deleteAddress(addressId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.deleteAddress(addressId)
        } catch (e: Exception) {
            // نكمل الحذف المحلي
        }

        messageDao.deleteByAddress(addressId)
        addressDao.deleteById(addressId)
        Result.success(Unit)
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val message = messageDao.getById(messageId)

        try {
            api.deleteMessage(messageId)
        } catch (e: Exception) {
            // نكمل الحذف المحلي
        }

        messageDao.deleteById(messageId)
        message?.let { refreshUnreadCount(it.addressId) }

        Result.success(Unit)
    }

    suspend fun updateLabel(addressId: String, label: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                api.updateLabel(addressId, UpdateLabelRequest(label))
            } catch (e: Exception) {
                // نكمل التعديل المحلي
            }

            addressDao.rename(addressId, label)
            Result.success(Unit)
        }

    suspend fun syncMessages(): Result<List<NewMessageInfo>> = withContext(Dispatchers.IO) {
        try {
            val since = prefs.lastSyncAt.first()
            val response = api.syncMessages(since = since)

            val newOnes = mutableListOf<NewMessageInfo>()
            val readIds = prefs.getReadMessageIds()

            if (response.messages.isNotEmpty()) {
                val entities = response.messages.map { dto ->
                    val existing = messageDao.getById(dto.id)
                    val wasRead = existing?.isRead ?: (dto.id in readIds)

                    if (existing == null && !wasRead) {
                        newOnes.add(
                            NewMessageInfo(
                                id = dto.id,
                                addressId = dto.addressId,
                                fromName = dto.fromName ?: dto.fromEmail.substringBefore("@"),
                                subject = dto.subject,
                                preview = dto.preview
                            )
                        )
                    }

                    dto.toEntity(
                        isRead = wasRead,
                        isStarred = existing?.isStarred ?: false
                    )
                }

                messageDao.insertAll(entities)
                refreshAllUnreadCounts()
            }

            val serverTime = if (response.serverTime > 0) {
                response.serverTime
            } else {
                System.currentTimeMillis()
            }
            prefs.setLastSyncAt(serverTime)

            Result.success(newOnes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAddressMessages(addressId: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMessages(addressId)
                val readIds = prefs.getReadMessageIds()

                val entities = response.messages.map { dto ->
                    val existing = messageDao.getById(dto.id)
                    dto.toEntity(
                        isRead = existing?.isRead ?: (dto.id in readIds),
                        isStarred = existing?.isStarred ?: false
                    )
                }

                if (entities.isNotEmpty()) {
                    messageDao.insertAll(entities)
                }

                refreshUnreadCount(addressId)

                Result.success(entities.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * مزامنة العناوين — بتمسح اللي اتشال من السيرفر
     */
    suspend fun syncAddresses(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.listAddresses()
            val serverIds = response.addresses.map { it.id }.toSet()

            // مسح العناوين اللي اتسحبت أو اتنقلت
            addressDao.getAllOnce().forEach { local ->
                if (local.id !in serverIds) {
                    messageDao.deleteByAddress(local.id)
                    addressDao.deleteById(local.id)
                }
            }

            response.addresses.forEach { dto ->
                val existing = addressDao.getById(dto.id)
                addressDao.insert(
                    dto.toEntity().copy(
                        isPinned = existing?.isPinned ?: false,
                        unreadCount = existing?.unreadCount ?: 0
                    )
                )
            }

            Result.success(response.addresses.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== داخلي =====

    private suspend fun refreshUnreadCount(addressId: String) {
        val unread = messageDao.countUnread(addressId)
        addressDao.setUnreadCount(addressId, unread)
        addressDao.touch(addressId, System.currentTimeMillis())
    }

    private suspend fun refreshAllUnreadCounts() {
        addressDao.getAllOnce().forEach { address ->
            refreshUnreadCount(address.id)
        }
    }

    private fun mapError(e: Exception): Exception {
        val message = e.message ?: ""
        return when {
            message.contains("409") -> Exception("العنوان ده محجوز، جرب اسم تاني")
            message.contains("429") -> Exception("وصلت للحد الأقصى، استنى شوية")
            message.contains("400") -> Exception("الاسم مش صالح")
            else -> Exception("السيرفر بطيء دلوقتي، جرب تاني")
        }
    }

    private fun mapTransferError(e: Exception): Exception {
        val message = e.message ?: ""
        return when {
            message.contains("404") -> Exception("الكود ده مش موجود")
            message.contains("410") -> Exception("الكود انتهت صلاحيته")
            message.contains("403") -> Exception("العنوان ده موقوف")
            message.contains("400") -> Exception("الكود مش صالح")
            message.contains("429") -> Exception("محاولات كتير، استنى شوية")
            else -> Exception("مفيش اتصال بالسيرفر")
        }
    }
}
