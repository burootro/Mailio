package com.burootro.mailio.ui.screens.contact

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SUPPORT_EMAIL = "mio3900@outlook.com"

@Composable
fun ContactScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val transition = rememberInfiniteTransition(label = "contactGlow")

    val glowScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val ring by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    val ringAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    fun openMail(subject: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$SUPPORT_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Mailio — $subject")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("مفيش تطبيق بريد على الجهاز")
            }
        }
    }

    Scaffold(
        containerColor = DeepVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding())
                    .padding(bottom = padding.calculateBottomPadding() + 40.dp)
            ) {

                // الهيدر
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(CardSurface)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "رجوع",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // الأيقونة المتوهجة
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(ring)
                                .alpha(ringAlpha)
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(50))
                        )

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(glowScale)
                                .blur(28.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MailioGradients.primaryDiagonal)
                                .alpha(0.65f)
                        )

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MailioGradients.primaryDiagonal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = null,
                                tint = DeepVoid,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(700)) + slideInVertically(
                        animationSpec = tween(700),
                        initialOffsetY = { it / 4 }
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تواصل معانا",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.dp)
                                .background(MailioGradients.primaryHorizontal)
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = "أي مشكلة أو اقتراح، إحنا بنقرا كل رسالة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(900, delayMillis = 250))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        ReasonCard(
                            icon = Icons.Rounded.BugReport,
                            title = "الإبلاغ عن مشكلة",
                            subtitle = "حاجة مش شغالة أو بتقع",
                            tint = ErrorRose,
                            onClick = { openMail("الإبلاغ عن مشكلة") }
                        )

                        ReasonCard(
                            icon = Icons.Rounded.Lightbulb,
                            title = "اقتراح ميزة",
                            subtitle = "عندك فكرة تحسّن التطبيق",
                            tint = WarningAmber,
                            onClick = { openMail("اقتراح ميزة") }
                        )

                        ReasonCard(
                            icon = Icons.Rounded.HelpOutline,
                            title = "استفسار عام",
                            subtitle = "أي سؤال عن الخدمة",
                            tint = CyanGlow,
                            onClick = { openMail("استفسار") }
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // بطاقة الإيميل
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(1000, delayMillis = 450))
                ) {
                    EmailCard(
                        onCopy = {
                            val clip = context.getSystemService(android.content.ClipboardManager::class.java)
                            clip?.setPrimaryClip(
                                android.content.ClipData.newPlainText("email", SUPPORT_EMAIL)
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("اتنسخ الإيميل")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }

                Spacer(Modifier.height(26.dp))

                Text(
                    text = "بنرد عادة خلال ٢٤ ساعة",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ReasonCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "reasonScale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(140)
            pressed = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurface)
            .background(MailioGradients.cardSheen)
            .border(1.dp, tint.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
            .clickable {
                pressed = true
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }

        Icon(
            imageVector = Icons.Rounded.Send,
            contentDescription = null,
            tint = TextDisabled,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EmailCard(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }

    val transition = rememberInfiniteTransition(label = "emailGlow")
    val borderGlow by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DeepVoid)
            .border(1.dp, NeonCyan.copy(alpha = borderGlow), RoundedCornerShape(20.dp))
            .clickable {
                copied = true
                onCopy()
            }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.AlternateEmail,
                contentDescription = null,
                tint = CyanGlow,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "إيميل الدعم",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )

            Spacer(Modifier.weight(1f))

            Icon(
                imageVector = if (copied) Icons.Rounded.ContentCopy else Icons.Rounded.ContentCopy,
                contentDescription = "نسخ",
                tint = if (copied) SuccessGreen else TextDisabled,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = SUPPORT_EMAIL,
            style = EmailAddressStyle,
            color = if (copied) SuccessGreen else CyanGlow
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = if (copied) "اتنسخ ✓" else "اضغط للنسخ",
            style = MaterialTheme.typography.labelSmall,
            color = if (copied) SuccessGreen else TextDisabled
        )
    }
}
