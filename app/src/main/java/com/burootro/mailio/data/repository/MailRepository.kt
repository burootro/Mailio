package com.burootro.mailio.data.repository

import com.burootro.mailio.data.local.dao.AddressDao
import com.burootro.mailio.data.local.dao.AttachmentDao
import com.burootro.mailio.data.local.dao.MessageDao
import com.burootro.mailio.data.mapper.toAddressDomainList
import com.burootro.mailio.data.mapper.toAttachmentDomainList
import com.burootro.mailio.data.mapper.toDomain
import com.burootro.mailio.data.mapper.toEntity
import com.burootro.mailio.data.mapper.toMessageDomainList
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.domain.model.AddressLifetime
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.domain.model.MailAttachment
import com.burootro.mailio.domain.model.MailMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MailRepository @Inject constructor(
    private val addressDao: AddressDao,
    private val messageDao: MessageDao,
    private val attachmentDao: AttachmentDao,
    private val prefs: MailioPreferences
) {

    // ===== العناوين =====

    fun observeAddresses(): Flow<List<MailAddress>> =
        addressDao.observeAll().map { it.toAddressDomainList() }

    fun observeActiveAddresses(): Flow<List<MailAddress>> =
        addressDao.observeActive().map { it.toAddressDomainList() }

    fun observeAddressCount(): Flow<Int> = addressDao.observeCount()

    suspend fun getAddress(id: String): MailAddress? =
        addressDao.getById(id)?.toDomain()

    /**
     * إنشاء عنوان جديد.
     * لو localPart فاضي، بيتولّد اسم عشوائي.
     */
    suspend fun createAddress(
        domain: String,
        localPart: String? = null,
        label: String? = null,
        lifetime: AddressLifetime = AddressLifetime.PERMANENT
    ): Result<MailAddress> {
        return try {
            val now = System.currentTimeMillis()
            val name = localPart?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
                ?: generateLocalPart()

            if (!isValidLocalPart(name)) {
                return Result.failure(
                    IllegalArgumentException("الاسم لازم يكون حروف وأرقام بس، من 3 لـ 32 حرف")
                )
            }

            val email = "$name@$domain"

            if (addressDao.getByEmail(email) != null) {
                return Result.failure(IllegalStateException("العنوان ده موجود عندك بالفعل"))
            }

            val address = MailAddress(
                id = UUID.randomUUID().toString(),
                email = email,
                label = label,
                createdAt = now,
                expiresAt = lifetime.millis?.let { now + it },
                isActive = true,
                isPinned = false,
                unreadCount = 0,
                lastActivityAt = now
            )

            addressDao.insert(address.toEntity())
            Result.success(address)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameAddress(id: String, label: String?) =
        addressDao.rename(id, label?.trim()?.takeIf { it.isNotBlank() })

    suspend fun setPinned(id: String, pinned: Boolean) =
        addressDao.setPinned(id, pinned)

    suspend fun setAddressActive(id: String, active: Boolean) =
        addressDao.setActive(id, active)

    suspend fun deleteAddress(id: String) {
        messageDao.deleteByAddress(id)
        addressDao.deleteById(id)
    }

    // ===== الرسايل =====

    fun observeMessages(addressId: String): Flow<List<MailMessage>> =
        messageDao.observeByAddress(addressId).map { it.toMessageDomainList() }

    fun observeRecentMessages(limit: Int = 100): Flow<List<MailMessage>> =
        messageDao.observeRecent(limit).map { it.toMessageDomainList() }

    fun observeStarred(): Flow<List<MailMessage>> =
        messageDao.observeStarred().map { it.toMessageDomainList() }

    fun observeTotalUnread(): Flow<Int> = messageDao.observeTotalUnread()

    fun observeMessage(id: String): Flow<MailMessage?> =
        messageDao.observeById(id).map { it?.toDomain() }

    fun searchMessages(query: String): Flow<List<MailMessage>> =
        messageDao.search(query).map { it.toMessageDomainList() }

    suspend fun saveMessages(messages: List<MailMessage>) {
        if (messages.isEmpty()) return
        messageDao.insertAll(messages.map { it.toEntity() })

        messages.map { it.addressId }.distinct().forEach { addressId ->
            refreshAddressState(addressId)
        }
    }

    suspend fun markRead(id: String, read: Boolean = true) {
        messageDao.setRead(id, read)
        messageDao.getById(id)?.let { refreshAddressState(it.addressId) }
    }

    suspend fun markAllRead(addressId: String) {
        messageDao.markAllReadForAddress(addressId)
        refreshAddressState(addressId)
    }

    suspend fun setStarred(id: String, starred: Boolean) =
        messageDao.setStarred(id, starred)

    suspend fun deleteMessage(id: String) {
        val message = messageDao.getById(id)
        messageDao.deleteById(id)
        message?.let { refreshAddressState(it.addressId) }
    }

    // ===== المرفقات =====

    fun observeAttachments(messageId: String): Flow<List<MailAttachment>> =
        attachmentDao.observeByMessage(messageId).map { it.toAttachmentDomainList() }

    suspend fun setAttachmentPath(id: String, path: String) =
        attachmentDao.setLocalPath(id, path)

    // ===== الصيانة =====

    /** حذف الرسايل الأقدم من المدة المحددة في الإعدادات */
    suspend fun runAutoCleanup(days: Long) {
        if (days <= 0) return
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        messageDao.deleteOlderThan(cutoff)
        addressDao.getAllOnce().forEach { refreshAddressState(it.id) }
    }

    suspend fun wipeEverything() {
        messageDao.deleteAll()
        addressDao.deleteAll()
        prefs.clearAll()
    }

    // ===== داخلي =====

    private suspend fun refreshAddressState(addressId: String) {
        val unread = messageDao.countUnread(addressId)
        addressDao.setUnreadCount(addressId, unread)
        addressDao.touch(addressId, System.currentTimeMillis())
    }

    private fun generateLocalPart(): String {
        val random = SecureRandom()
        val adjective = ADJECTIVES[random.nextInt(ADJECTIVES.size)]
        val noun = NOUNS[random.nextInt(NOUNS.size)]
        val number = random.nextInt(9000) + 1000
        return "$adjective$noun$number"
    }

    private fun isValidLocalPart(value: String): Boolean =
        value.length in 3..32 && value.matches(Regex("^[a-z0-9]+([._-][a-z0-9]+)*$"))

    companion object {
        private val ADJECTIVES = listOf(
            "swift", "clever", "bright", "silent", "cosmic", "royal",
            "noble", "rapid", "solar", "lunar", "vivid", "mystic"
        )
        private val NOUNS = listOf(
            "falcon", "tiger", "comet", "raven", "phoenix", "wolf",
            "eagle", "shark", "lynx", "cobra", "orca", "puma"
        )
    }
}
