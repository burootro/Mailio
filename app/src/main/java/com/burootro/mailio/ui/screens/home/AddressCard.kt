package com.burootro.mailio.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.ui.components.PulsingDot
import com.burootro.mailio.ui.theme.*
import com.burootro.mailio.util.TimeUtils
import kotlinx.coroutines.delay

@Composable
fun AddressCard(
    address: MailAddress,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    // العداد التنازلي للعناوين المؤقتة
    var remaining by remember(address.id) {
        mutableStateOf(address.remainingMillis())
    }

    LaunchedEffect(address.id, address.expiresAt) {
        if (address.expiresAt != null) {
            while (true) {
                remaining = address.remainingMillis()
                delay(1000)
            }
        }
    }

    val shape = RoundedCornerShape(22.dp)
    val hasUnread = address.unreadCount > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(CardSurface)
            .background(MailioGradients.cardSheen)
            .border(
                width = 1.dp,
                color = if (hasUnread) NeonCyan.copy(alpha = 0.4f) else BorderSubtle,
                shape = shape
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(18.dp)
    ) {
        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // الأفاتار
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            if (hasUnread) MailioGradients.primaryDiagonal
                            else MailioGradients.cyanSoft
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = address.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (hasUnread) DeepVoid else CyanGlow
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = address.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (address.isPinned) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = address.email,
                        style = EmailAddressStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(10.dp))

                // عداد غير المقروء
                AnimatedVisibility(
                    visible = hasUnread,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 26.dp, minHeight = 26.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MailioGradients.primaryHorizontal)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (address.unreadCount > 99) "99+" else "${address.unreadCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepVoid
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // الشريط السفلي
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (address.isPermanent) {
                    PulsingDot(color = SuccessGreen, size = 7.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "دائم",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen
                    )
                } else {
                    val expired = remaining == null || remaining == 0L
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = if (expired) ErrorRose else WarningAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = TimeUtils.countdown(remaining ?: 0L),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (expired) ErrorRose else WarningAmber
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = TimeUtils.relativeShort(address.lastActivityAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )

                Spacer(Modifier.width(12.dp))

                CopyIconButton(onCopy = onCopy)
            }
        }
    }
}

@Composable
private fun CopyIconButton(onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (copied) 360f else 0f,
        animationSpec = tween(500),
        label = "copyRotation"
    )

    LaunchedEffect(copied) {
        if (copied) {
            delay(1400)
            copied = false
        }
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (copied) SuccessGreen.copy(alpha = 0.18f) else CyanFaint)
            .combinedClickable(
                onClick = {
                    copied = true
                    onCopy()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.ContentCopy,
            contentDescription = "نسخ",
            tint = if (copied) SuccessGreen else CyanGlow,
            modifier = Modifier
                .size(15.dp)
                .scale(if (copied) 1.15f else 1f)
        )
    }
}
