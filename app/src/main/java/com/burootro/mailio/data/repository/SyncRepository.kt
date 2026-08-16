package com.burootro.mailio.data.repository

import com.burootro.mailio.data.local.dao.AddressDao
import com.burootro.mailio.data.local.dao.MessageDao
import com.burootro.mailio.data.mapper.toAddressEntities
import com.burootro.mailio.data.mapper.toEntity
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.remote.MailioApi
import com.burootro.mailio.data.remote.dto.CreateAddressRequest
import com.burootro.mailio.data.remote.dto.RestoreRequest
import com.burootro.mailio.data.remote.dto.UpdateLabelRequest
import com.burootro.mailio.domain.model.AddressLifetime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class SyncResult(
    val newMessages: Int = 0,
    val addressesUpdated: Int = 0
)

@Singleton
class SyncRepository @Inject constructor(
    private val api: MailioApi,
    private val addressDao: AddressDao,
    private val messageDao: MessageDao,
    private val prefs: MailioPreferences
) {

    /**
     * تسجيل مستخدم جديد على السيرفر والحصول على مفتاح
     */
    suspend fun ensureRegistered(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val existing = prefs.getRecoveryKey()
            if (existing != null) {
                return@withContext Result.success(existing)
            }

            val response = api.register()
            prefs.setRecoveryKey(response.recoveryKey)

            if (response.domains.isNotEmpty()) {
                prefs.setPreferredDomain(response.domains.first())
            }

            Result.success(response.recoveryKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * استرجاع حساب بمفتاح موجود
     */
    suspend fun restoreAccount(recoveryKey: String): Result<SyncResult> =
        withContext(Dispatchers.IO) {
            try {
                val key = recoveryKey.trim().uppercase()
                val response = api.restore(RestoreRequest(key))

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

    /**
     * إنشاء عنوان على السيرفر وحفظه محلياً
     */
    suspend fun createAddress(
        localPart: String?,
        label: String?,
        lifetime: AddressLifetime,
        domain: String?
    ): Result<String> = withContext(Dispatchers.IO) {
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
            Result.success(response.address.email)
        } catch (e: Exception) {
            Result.failure(mapError(e))
        }
    }

    /**
     * حذف عنوان
     */
    suspend fun deleteAddress(addressId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.deleteAddress(addressId)
        } catch (e: Exception) {
            // نكمل الحذف المحلي حتى لو السيرفر فشل
        }

        messageDao.deleteByAddress(addressId)
        addressDao.deleteById(addressId)
        Result.success(Unit)
    }

    /**
     * تغيير اسم العنوان
     */
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

    /**
     * سحب الرسايل الجديدة عبر كل العناوين
     */
    suspend fun syncMessages(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val since = prefs.lastSyncAt.first()
            val response = api.syncMessages(since = since)

            if (response.messages.isNotEmpty()) {
                val entities = response.messages.map { dto ->
                    val existing = messageDao.getById(dto.id)
                    dto.toEntity(
                        isRead = existing?.isRead ?: false,
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

            Result.success(SyncResult(newMessages = response.messages.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * سحب رسايل عنوان واحد
     */
    suspend fun syncAddressMessages(addressId: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getMessages(addressId)

                val entities = response.messages.map { dto ->
                    val existing = messageDao.getById(dto.id)
                    dto.toEntity(
                        isRead = existing?.isRead ?: false,
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
     * مزامنة قائمة العناوين من السيرفر
     */
    suspend fun syncAddresses(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.listAddresses()

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
            message.contains("timeout", true) -> Exception("السيرفر بيصحى، جرب تاني بعد شوية")
            else -> Exception("مفيش اتصال بالسيرفر")
        }
    }
}
