package com.burootro.mailio.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ===== المصادقة =====

@Serializable
data class RegisterResponse(
    val recoveryKey: String,
    val userId: String,
    val createdAt: Long,
    val domains: List<String> = emptyList()
)

@Serializable
data class RestoreRequest(
    val recoveryKey: String
)

@Serializable
data class RestoreResponse(
    val userId: String,
    val createdAt: Long,
    val domains: List<String> = emptyList(),
    val addresses: List<AddressDto> = emptyList(),
    val messages: List<MessageDto> = emptyList()
)

@Serializable
data class MeResponse(
    val userId: String,
    val createdAt: Long,
    val lastSeenAt: Long,
    val addressCount: Int,
    val domains: List<String> = emptyList()
)

// ===== الإشعارات =====

@Serializable
data class PushTokenRequest(
    val token: String
)

// ===== طلبات المراجعة =====

@Serializable
data class CreateAppealRequest(
    val addressId: String? = null,
    val message: String
)

@Serializable
data class CreateAppealResponse(
    val ok: Boolean = true,
    val appealId: String,
    val message: String = ""
)

@Serializable
data class AppealDto(
    val id: String,
    @SerialName("address_id") val addressId: String? = null,
    @SerialName("address_email") val addressEmail: String? = null,
    val message: String,
    val status: String,
    @SerialName("admin_reply") val adminReply: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("replied_at") val repliedAt: Long? = null
)

@Serializable
data class AppealListResponse(
    val appeals: List<AppealDto> = emptyList()
)

// ===== العناوين =====

@Serializable
data class AddressDto(
    val id: String,
    val email: String,
    @SerialName("local_part") val localPart: String? = null,
    val domain: String? = null,
    val label: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
    @SerialName("revoked_reason") val revokedReason: String? = null,
    @SerialName("message_count") val messageCount: Int = 0,
    @SerialName("last_message_at") val lastMessageAt: Long? = null
)

@Serializable
data class CreateAddressRequest(
    val localPart: String? = null,
    val label: String? = null,
    val lifetimeMs: Long? = null,
    val domain: String? = null
)

@Serializable
data class CreateAddressResponse(
    val address: AddressDto
)

@Serializable
data class AddressListResponse(
    val addresses: List<AddressDto> = emptyList()
)

@Serializable
data class UpdateLabelRequest(
    val label: String? = null
)

// ===== الرسايل =====

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("address_id") val addressId: String,
    @SerialName("from_email") val fromEmail: String,
    @SerialName("from_name") val fromName: String? = null,
    val subject: String = "",
    val preview: String = "",
    @SerialName("body_text") val bodyText: String? = null,
    @SerialName("body_html") val bodyHtml: String? = null,
    @SerialName("received_at") val receivedAt: Long,
    @SerialName("has_attachments") val hasAttachments: Boolean = false,
    @SerialName("spam_score") val spamScore: Float = 0f
)

@Serializable
data class MessageListResponse(
    val messages: List<MessageDto> = emptyList(),
    val serverTime: Long = 0L
)

// ===== عام =====

@Serializable
data class OkResponse(
    val ok: Boolean = true
)

@Serializable
data class ErrorResponse(
    val error: String = "UNKNOWN",
    val message: String = "حصل خطأ"
)
