package com.burootro.mailio.data.mapper

import com.burootro.mailio.data.local.entity.AddressEntity
import com.burootro.mailio.data.local.entity.AttachmentEntity
import com.burootro.mailio.data.local.entity.MessageEntity
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.domain.model.MailAttachment
import com.burootro.mailio.domain.model.MailMessage

// ===== Address =====

fun AddressEntity.toDomain(): MailAddress = MailAddress(
    id = id,
    email = email,
    label = label,
    createdAt = createdAt,
    expiresAt = expiresAt,
    isActive = isActive,
    isPinned = isPinned,
    unreadCount = unreadCount,
    lastActivityAt = lastActivityAt,
    isBlocked = isBlocked,
    blockedReason = blockedReason
)

fun MailAddress.toEntity(): AddressEntity = AddressEntity(
    id = id,
    email = email,
    label = label,
    createdAt = createdAt,
    expiresAt = expiresAt,
    isActive = isActive,
    isPinned = isPinned,
    unreadCount = unreadCount,
    lastActivityAt = lastActivityAt,
    isBlocked = isBlocked,
    blockedReason = blockedReason
)

fun List<AddressEntity>.toAddressDomainList(): List<MailAddress> = map { it.toDomain() }

// ===== Message =====

fun MessageEntity.toDomain(): MailMessage = MailMessage(
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

fun MailMessage.toEntity(): MessageEntity = MessageEntity(
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

fun List<MessageEntity>.toMessageDomainList(): List<MailMessage> = map { it.toDomain() }

// ===== Attachment =====

fun AttachmentEntity.toDomain(): MailAttachment = MailAttachment(
    id = id,
    messageId = messageId,
    fileName = fileName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    remoteUrl = remoteUrl,
    localPath = localPath
)

fun MailAttachment.toEntity(): AttachmentEntity = AttachmentEntity(
    id = id,
    messageId = messageId,
    fileName = fileName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    remoteUrl = remoteUrl,
    localPath = localPath
)

fun List<AttachmentEntity>.toAttachmentDomainList(): List<MailAttachment> = map { it.toDomain() }
