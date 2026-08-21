package com.burootro.mailio.domain.model

/**
 * عنوان إيميل بشكله النهائي للعرض
 */
data class MailAddress(
    val id: String,
    val email: String,
    val label: String?,
    val createdAt: Long,
    val expiresAt: Long?,
    val isActive: Boolean,
    val isPinned: Boolean,
    val unreadCount: Int,
    val lastActivityAt: Long,
    val isBlocked: Boolean = false,
    val blockedReason: String? = null
) {
    val localPart: String
        get() = email.substringBefore("@")

    val domain: String
        get() = email.substringAfter("@")

    val displayName: String
        get() = label?.takeIf { it.isNotBlank() } ?: localPart

    val isPermanent: Boolean
        get() = expiresAt == null

    fun remainingMillis(now: Long = System.currentTimeMillis()): Long? =
        expiresAt?.let { (it - now).coerceAtLeast(0L) }

    fun isExpired(now: Long = System.currentTimeMillis()): Boolean =
        expiresAt != null && now >= expiresAt
}

/**
 * رسالة واردة
 */
data class MailMessage(
    val id: String,
    val addressId: String,
    val fromEmail: String,
    val fromName: String?,
    val subject: String,
    val preview: String,
    val bodyText: String?,
    val bodyHtml: String?,
    val receivedAt: Long,
    val isRead: Boolean,
    val isStarred: Boolean,
    val hasAttachments: Boolean
) {
    val senderDisplay: String
        get() = fromName?.takeIf { it.isNotBlank() } ?: fromEmail.substringBefore("@")

    val senderInitial: String
        get() = senderDisplay.trim().firstOrNull()?.uppercase() ?: "?"

    val displaySubject: String
        get() = subject.takeIf { it.isNotBlank() } ?: "(بدون عنوان)"
}

/**
 * مرفق
 */
data class MailAttachment(
    val id: String,
    val messageId: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val remoteUrl: String?,
    val localPath: String?
) {
    val isDownloaded: Boolean
        get() = localPath != null

    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val extension: String
        get() = fileName.substringAfterLast('.', "").uppercase()

    val readableSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

/**
 * مدة صلاحية العنوان
 */
enum class AddressLifetime(
    val label: String,
    val millis: Long?
) {
    TEN_MINUTES("١٠ دقايق", 10 * 60 * 1000L),
    ONE_HOUR("ساعة", 60 * 60 * 1000L),
    ONE_DAY("يوم", 24 * 60 * 60 * 1000L),
    ONE_WEEK("أسبوع", 7 * 24 * 60 * 60 * 1000L),
    PERMANENT("دائم", null)
}

/**
 * طلب مراجعة
 */
data class Appeal(
    val id: String,
    val addressEmail: String?,
    val message: String,
    val status: String,
    val adminReply: String?,
    val createdAt: Long,
    val repliedAt: Long?
) {
    val isPending: Boolean get() = status == "pending"
    val isApproved: Boolean get() = status == "approved"
    val isRejected: Boolean get() = status == "rejected"
}
