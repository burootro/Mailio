package com.burootro.mailio.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RecoveryKeyCard(
    recoveryKey: String,
    isBackedUp: Boolean,
    onCopy: () -> Unit,
    onMarkBackedUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var revealed by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1800)
            copied = false
        }
    }

    // اللمعان المتحرك
    val shimmerTransition = rememberInfiniteTransition(label = "keyShimmer")
    val shimmerX by shimmerTransition.animateFloat(
        initialValue = -800f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    // تنفس التوهج
    val glowBreath by shimmerTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "keyGlow"
    )

    val shape = RoundedCornerShape(26.dp)

    Box(modifier = modifier.fillMaxWidth()) {

        // توهج خلفي
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(14.dp)
                .blur(30.dp)
                .clip(shape)
                .background(MailioGradients.primaryDiagonal)
                .then(Modifier)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(ElevatedSurface)
                .drawWithContent {
                    drawContent()
                    // شريط اللمعان المتحرك
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.07f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX, 0f),
                            end = Offset(shimmerX + 380f, size.height)
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
                .border(1.dp, NeonCyan.copy(alpha = glowBreath * 0.5f), shape)
                .padding(20.dp)
        ) {

            // الرأس
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MailioGradients.primaryDiagonal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VpnKey,
                        contentDescription = null,
                        tint = DeepVoid,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مفتاح الاسترجاع",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "هويتك الوحيدة — احفظه في مكان آمن",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // المفتاح
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepVoid)
                    .clickable { revealed = !revealed }
                    .padding(vertical = 18.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = revealed,
                    transitionSpec = {
                        fadeIn(tween(350)) togetherWith fadeOut(tween(250))
                    },
                    label = "keyReveal"
                ) { isRevealed ->
                    Text(
                        text = if (isRevealed) recoveryKey
                        else maskKey(recoveryKey),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        ),
                        color = if (isRevealed) CyanGlow else TextDisabled,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // الأزرار
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                KeyActionButton(
                    icon = if (revealed) Icons.Rounded.VisibilityOff
                    else Icons.Rounded.Visibility,
                    text = if (revealed) "إخفاء" else "إظهار",
                    onClick = { revealed = !revealed },
                    modifier = Modifier.weight(1f)
                )

                KeyActionButton(
                    icon = if (copied) Icons.Rounded.CheckCircle
                    else Icons.Rounded.ContentCopy,
                    text = if (copied) "اتنسخ" else "نسخ",
                    tint = if (copied) SuccessGreen else CyanGlow,
                    onClick = {
                        copied = true
                        onCopy()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // تحذير النسخ الاحتياطي
            if (!isBackedUp) {
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WarningAmber.copy(alpha = 0.11f))
                        .clickable(onClick = onMarkBackedUp)
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(Modifier.width(11.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لسه محفظتوش",
                            style = MaterialTheme.typography.titleSmall,
                            color = WarningAmber
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "لو ضاع الموبايل من غير المفتاح ده، كل عناوينك هتضيع. اضغط هنا بعد ما تحفظه.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SuccessGreen.copy(alpha = 0.10f))
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(11.dp))
                    Text(
                        text = "المفتاح محفوظ عندك",
                        style = MaterialTheme.typography.titleSmall,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = CyanGlow
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyanFaint)
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}

private fun maskKey(key: String): String {
    if (key.isBlank()) return "••••••••••••"
    return key.map { char ->
        if (char == '-') '-' else '•'
    }.joinToString("")
}
