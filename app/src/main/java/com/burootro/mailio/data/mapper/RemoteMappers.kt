package com.burootro.mailio.data.mapper

import com.burootro.mailio.data.local.entity.AddressEntity
import com.burootro.mailio.data.local.entity.MessageEntity
import com.burootro.mailio.data.remote.dto.AddressDto
import com.burootro.mailio.data.remote.dto.MessageDto

/**
 * تحويل عنوان من السيرفر لكيان محلي
 */
fun AddressDto.toEntity(unreadCount: Int = 0): AddressEntity = AddressEntity(
    id = id,
    email = email,
    label = label,
    createdAt = createdAt,
    expiresAt = expiresAt,
    isActive = isActive && !isBlocked,
    isPinned = false,
    unreadCount = unreadCount,
    lastActivityAt = lastMessageAt ?: createdAt
)

fun List<AddressDto>.toAddressEntities(): List<AddressEntity> = map { it.toEntity() }

/**
 * تحويل رسالة من السيرفر لكيان محلي
 * الرسايل الجاية من السيرفر بتبقى غير مقروءة افتراضياً
 */
fun MessageDto.toEntity(isRead: Boolean = false, isStarred: Boolean = false): MessageEntity =
    MessageEntity(
        id = id,
        addressId = addressId,
        fromEmail = fromEmail,
        fromName = fromName,
        subject = subject,
        preview = preview,
        bodyText = bodyText,
        bodyHtml = bodyHtml,
        receivedAt = receivedAt,
        isRead = isRead,
        isStarred = isStarred,
        hasAttachments = hasAttachments
    )

fun List<MessageDto>.toMessageEntities(): List<MessageEntity> = map { it.toEntity() }
