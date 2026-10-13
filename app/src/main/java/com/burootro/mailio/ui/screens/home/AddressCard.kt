package com.burootro.mailio.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.ui.components.CountBadge
import com.burootro.mailio.ui.components.LetterAvatar
import com.burootro.mailio.ui.components.MxCard
import com.burootro.mailio.ui.components.Pill
import com.burootro.mailio.ui.components.ProgressRing
import com.burootro.mailio.ui.components.SoftButton
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import com.burootro.mailio.util.TimeUtils
import kotlinx.coroutines.delay

@Composable
fun AddressCard(
    address: MailAddress,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCopy: () -> Unit,
    onAppeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Live clock only for temporary addresses — permanent ones never tick.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    if (!address.isPermanent && !address.isBlocked) {
        LaunchedEffect(address.id, address.expiresAt) {
            while (true) {
                now = System.currentTimeMillis()
                val left = address.remainingMillis(now) ?: break
                if (left <= 0L) break
                delay(if (left < 3_600_000L) 1_000L else 30_000L)
            }
        }
    }

    val expired = address.isExpired(now)
    val blocked = address.isBlocked
    val accent = when {
        blocked -> Mx.Rose
        expired -> Mx.Text3
        else -> Mx.avatar(address.email).first
    }

    MxCard(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        padding = androidx.compose.foundation.layout.PaddingValues(start = 14.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
        background = if (blocked) Mx.RoseTint.copy(alpha = 0.45f) else Mx.Surface,
        borderColor = if (blocked) Mx.Rose.copy(alpha = 0.25f) else Mx.Line
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                val total = address.expiresAt?.let { it - address.createdAt } ?: 0L
                val left = address.remainingMillis(now) ?: 0L
                if (!address.isPermanent && total > 0 && !blocked) {
                    ProgressRing(
                        progress = left.toFloat() / total.toFloat(),
                        color = if (left < 600_000L) Mx.Amber else accent,
                        modifier = Modifier.size(50.dp)
                    )
                }
                LetterAvatar(seed = address.email, letter = address.displayName, size = 40.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        address.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (expired) Mx.Text3 else Mx.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (address.isPinned) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.PushPin, "مثبّت", tint = Mx.Gold, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    address.email,
                    style = MonoStyle.copy(color = Mx.Text2, fontWeight = FontWeight.Medium, textDirection = TextDirection.Ltr),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    when {
                        blocked -> Pill("موقوف", Mx.Rose, Mx.RoseTint, Icons.Rounded.Block)
                        expired -> Pill("انتهى", Mx.Text2, Mx.Fill, Icons.Rounded.HourglassEmpty)
                        address.isPermanent -> Pill("دائم", Mx.Green, Mx.GreenTint)
                        else -> Pill(
                            TimeUtils.countdown(address.remainingMillis(now) ?: 0L),
                            Mx.Amber,
                            Mx.AmberTint,
                            Icons.Rounded.Schedule
                        )
                    }
                    if (address.unreadCount > 0 && !blocked) {
                        Pill("${address.unreadCount} جديدة", Mx.Royal, Mx.RoyalTint)
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CountBadge(address.unreadCount)
                CopyButton(onCopy)
            }
        }

        if (blocked) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Mx.Surface)
                    .border(1.dp, Mx.Rose.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("العنوان ده موقوف", style = MaterialTheme.typography.labelLarge, color = Mx.Rose)
                    address.blockedReason?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Mx.Text2, maxLines = 2)
                    }
                }
                SoftButton(
                    "طلب مراجعة",
                    onClick = onAppeal,
                    icon = Icons.Rounded.Gavel,
                    tint = Mx.Rose,
                    background = Mx.RoseTint,
                    height = 38.dp
                )
            }
        }
    }
}

@Composable
private fun CopyButton(onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    Box(
        Modifier
            .padding(top = 4.dp)
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (copied) Mx.GreenTint else Mx.Fill)
            .bounceClick(pressedScale = 0.85f) {
                copied = true
                onCopy()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = copied,
            transitionSpec = { (scaleIn(tween(180)) + fadeIn()) togetherWith (scaleOut(tween(120)) + fadeOut()) },
            label = "copy"
        ) { done ->
            Icon(
                if (done) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy,
                contentDescription = "نسخ",
                tint = if (done) Mx.Green else Mx.InkSoft,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
