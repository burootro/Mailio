package com.burootro.mailio.ui.screens.inbox

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burootro.mailio.domain.model.MailMessage
import com.burootro.mailio.ui.theme.*
import com.burootro.mailio.util.AvatarColors
import com.burootro.mailio.util.TimeUtils

@Composable
fun MessageCard(
    message: MailMessage,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onStarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "messageScale"
    )

    val unread = !message.isRead
    val shape = RoundedCornerShape(20.dp)
    val avatarColor = Color(AvatarColors.forSeed(message.fromEmail))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(if (unread) ElevatedSurface else CardSurface)
            .background(MailioGradients.cardSheen)
            .border(
                width = 1.dp,
                color = if (unread) NeonCyan.copy(alpha = 0.3f) else BorderSubtle,
                shape = shape
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {

            // أفاتار المرسل
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(avatarColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderInitial,
                    style = MaterialTheme.typography.titleMedium,
                    color = avatarColor
                )
            }

            Spacer(Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.senderDisplay,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (unread) TextPrimary else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = TimeUtils.relativeShort(message.receivedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (unread) CyanGlow else TextDisabled
                    )
                }

                Spacer(Modifier.height(5.dp))

                Text(
                    text = message.displaySubject,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal
                    ),
                    color = if (unread) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = message.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (message.hasAttachments || message.isStarred) {
                    Spacer(Modifier.height(9.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (message.hasAttachments) {
                            Icon(
                                imageVector = Icons.Rounded.AttachFile,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "مرفقات",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (unread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(NeonCyan)
                    )
                    Spacer(Modifier.height(12.dp))
                } else {
                    Spacer(Modifier.height(20.dp))
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .combinedClickable(onClick = onStarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (message.isStarred) Icons.Rounded.Star
                        else Icons.Rounded.StarOutline,
                        contentDescription = "تمييز",
                        tint = if (message.isStarred) WarningAmber else TextDisabled,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
