package com.burootro.mailio.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * عنوان إيميل مملوك للمستخدم
 */
@Entity(
    tableName = "addresses",
    indices = [Index(value = ["email"], unique = true)]
)
data class AddressEntity(
    @PrimaryKey
    val id: String,

    val email: String,
    val label: String? = null,
    val createdAt: Long,
    val expiresAt: Long? = null,
    val isActive: Boolean = true,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val lastActivityAt: Long = createdAt,

    /** موقوف من الأدمن */
    val isBlocked: Boolean = false,

    /** سبب الإيقاف */
    val blockedReason: String? = null
)

/**
 * رسالة واردة
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = ["id"],
            childColumns = ["addressId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["addressId"]),
        Index(value = ["receivedAt"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    val addressId: String,
    val fromEmail: String,
    val fromName: String? = null,
    val subject: String,
    val preview: String,
    val bodyText: String? = null,
    val bodyHtml: String? = null,
    val receivedAt: Long,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val hasAttachments: Boolean = false
)

/**
 * مرفق
 */
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["messageId"])]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,

    val messageId: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val remoteUrl: String? = null,
    val localPath: String? = null
)
