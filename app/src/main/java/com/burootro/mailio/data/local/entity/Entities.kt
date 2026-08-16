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

    /** العنوان الكامل: something@domain.com */
    val email: String,

    /** الاسم اللي المستخدم بيسميه بيه العنوان */
    val label: String? = null,

    /** وقت الإنشاء - ميلي ثانية */
    val createdAt: Long,

    /** وقت انتهاء الصلاحية - null يعني دائم */
    val expiresAt: Long? = null,

    /** مفعّل ولا موقوف */
    val isActive: Boolean = true,

    /** مثبت في أول القائمة */
    val isPinned: Boolean = false,

    /** عدد الرسايل غير المقروءة */
    val unreadCount: Int = 0,

    /** آخر نشاط - للترتيب */
    val lastActivityAt: Long = createdAt
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

    /** العنوان اللي وصلت عليه */
    val addressId: String,

    val fromEmail: String,
    val fromName: String? = null,

    val subject: String,

    /** مقتطف للعرض في القائمة */
    val preview: String,

    /** النص العادي */
    val bodyText: String? = null,

    /** النسخة HTML */
    val bodyHtml: String? = null,

    val receivedAt: Long,

    val isRead: Boolean = false,
    val isStarred: Boolean = false,

    /** فيه مرفقات ولا لأ */
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

    /** رابط التحميل من السيرفر */
    val remoteUrl: String? = null,

    /** المسار المحلي بعد التحميل */
    val localPath: String? = null
)
