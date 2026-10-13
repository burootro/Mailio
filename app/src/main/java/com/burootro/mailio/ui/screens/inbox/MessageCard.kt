package com.burootro.mailio.ui.screens.inbox

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burootro.mailio.domain.model.MailMessage
import com.burootro.mailio.ui.components.LetterAvatar
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.Phrases
import com.burootro.mailio.ui.components.SystemActions
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import com.burootro.mailio.util.TimeUtils
import kotlinx.coroutines.delay

/**
 * One message in a list. Shows a one-tap verification-code chip when the message has a code.
 * [addressLabel] is shown in the unified inbox so you know which address got it.
 */
@Composable
fun MessageRow(
    message: MailMessage,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier,
    addressLabel: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val feedback = LocalFeedback.current
    val code = remember(message.id) { SystemActions.findCode(message.subject, message.preview) }
    val unread = !message.isRead

    val border by animateColorAsState(if (unread) Mx.RoyalTint2 else Mx.Line, tween(300), label = "rowBorder")
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (unread) 10.dp else 4.dp, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.08f), spotColor = Mx.Royal.copy(alpha = 0.08f))
            .clip(shape)
            .background(Mx.Surface)
            .border(1.dp, border, shape)
            .bounceClick(pressedScale = 0.98f, onLongClick = onLongClick, onClick = onClick)
            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box {
            LetterAvatar(seed = message.fromEmail, letter = message.senderInitial, size = 42.dp)
            if (unread) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(Mx.Surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Mx.Royal)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    message.senderDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (unread) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = Mx.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (message.hasAttachments) {
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Rounded.AttachFile, null, tint = Mx.Text3, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    TimeUtils.relativeShort(message.receivedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unread) Mx.Royal else Mx.Text3,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                message.displaySubject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                color = if (unread) Mx.InkSoft else Mx.Text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (message.preview.isNotBlank()) {
                Text(
                    message.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = Mx.Text3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (code != null || addressLabel != null) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (code != null) {
                        CodeChip(code) {
                            SystemActions.copy(context, code, "code")
                            feedback.success(Phrases.codeCopied(), "🔐")
                        }
                    }
                    if (addressLabel != null) {
                        Text(
                            addressLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Mx.Text3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Mx.Fill)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        StarButton(starred = message.isStarred, onClick = onToggleStar)
    }
}

/** Gold star that pops when toggled. */
@Composable
fun StarButton(starred: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .bounceClick(pressedScale = 0.8f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = starred,
            transitionSpec = {
                (scaleIn(spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium), initialScale = 0.3f) + fadeIn()) togetherWith
                    (scaleOut(targetScale = 0.6f) + fadeOut())
            },
            label = "star"
        ) { on ->
            Icon(
                if (on) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "تمييز",
                tint = if (on) Mx.Gold else Mx.Text4,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Monospace code pill — tap to copy, morphs into a check for a moment. */
@Composable
fun CodeChip(code: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    val bg by animateColorAsState(if (copied) Mx.GreenTint else Mx.RoyalTint, tween(200), label = "codeBg")
    val fg by animateColorAsState(if (copied) Mx.Green else Mx.Royal, tween(200), label = "codeFg")
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .bounceClick(pressedScale = 0.92f) {
                copied = true
                onCopy()
            }
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(targetState = copied, transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }, label = "codeIcon") { done ->
            Icon(
                if (done) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy,
                null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(code, style = MonoStyle.copy(color = fg, letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing), maxLines = 1)
    }
}
