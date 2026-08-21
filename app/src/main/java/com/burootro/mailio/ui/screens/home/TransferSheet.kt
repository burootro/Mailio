package com.burootro.mailio.ui.screens.home

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.ui.components.GlowButton
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.delay

/**
 * نافذة نقل العنوان — المالك بيطلب كود
 */
@Composable
fun TransferSheet(
    email: String,
    code: String?,
    isLoading: Boolean,
    expiresAt: Long?,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit,
    onCopy: (String) -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBlue,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 10.dp, bottom = 34.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MailioGradients.primaryDiagonal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = DeepVoid,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(13.dp))

                Column {
                    Text(
                        text = "نقل العنوان",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = email,
                        style = EmailAddressStyle.copy(
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        ),
                        color = TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            AnimatedContent(
                targetState = code,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "transferState"
            ) { currentCode ->

                if (currentCode == null) {
                    // قبل التوليد
                    Column {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(WarningAmber.copy(alpha = 0.09f))
                                .border(
                                    1.dp,
                                    WarningAmber.copy(alpha = 0.22f),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "قبل ما تكمّل",
                                style = MaterialTheme.typography.titleSmall,
                                color = WarningAmber
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "• كل الرسايل هتتمسح نهائياً\n" +
                                    "• العنوان هيبقى ملك الشخص التاني\n" +
                                    "• مش هتقدر ترجّعه بعد كده",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        GlowButton(
                            text = if (isLoading) "بيتم التجهيز..." else "توليد كود النقل",
                            enabled = !isLoading,
                            onClick = onGenerate,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // بعد التوليد
                    Column {
                        Text(
                            text = "ادي الكود ده للشخص التاني",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(14.dp))

                        CodeBox(code = currentCode, onCopy = { onCopy(currentCode) })

                        Spacer(Modifier.height(12.dp))

                        expiresAt?.let { CountdownRow(expiresAt = it) }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardSurface)
                                .clickable(onClick = onCancel)
                                .padding(vertical = 15.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "إلغاء النقل",
                                style = MaterialTheme.typography.labelLarge,
                                color = ErrorRose
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBox(code: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }

    val transition = rememberInfiniteTransition(label = "codeGlow")
    val glow by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DeepVoid)
            .border(1.dp, NeonCyan.copy(alpha = glow), RoundedCornerShape(20.dp))
            .clickable {
                copied = true
                onCopy()
            }
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            ),
            color = CyanGlow,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (copied) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy,
                contentDescription = null,
                tint = if (copied) SuccessGreen else TextTertiary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (copied) "اتنسخ" else "اضغط للنسخ",
                style = MaterialTheme.typography.labelSmall,
                color = if (copied) SuccessGreen else TextTertiary
            )
        }
    }
}

@Composable
private fun CountdownRow(expiresAt: Long) {
    var remaining by remember { mutableStateOf(expiresAt - System.currentTimeMillis()) }

    LaunchedEffect(expiresAt) {
        while (remaining > 0) {
            delay(1000)
            remaining = expiresAt - System.currentTimeMillis()
        }
    }

    val minutes = (remaining / 60000).coerceAtLeast(0)
    val seconds = ((remaining % 60000) / 1000).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (remaining > 0)
                "الكود صالح لمدة $minutes:${seconds.toString().padStart(2, '0')}"
            else
                "الكود انتهت صلاحيته",
            style = MaterialTheme.typography.labelSmall,
            color = if (remaining > 0) WarningAmber else ErrorRose
        )
    }
}

/**
 * نافذة استلام عنوان بكود
 */
@Composable
fun ClaimTransferSheet(
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onClaim: (String) -> Unit,
    onClearError: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var code by remember { mutableStateOf("") }

    val isValid = Regex("^TR-[A-Z2-9]{4}-[A-Z2-9]{4}$").matches(code)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBlue,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 10.dp, bottom = 34.dp)
        ) {

            Text(
                text = "استلام عنوان",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "اكتب كود النقل اللي وصلك",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )

            Spacer(Modifier.height(22.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    val clean = input.uppercase().filter { it.isLetterOrDigit() }
                    val body = if (clean.startsWith("TR")) clean.drop(2) else clean

                    code = buildString {
                        append("TR")
                        body.take(8).forEachIndexed { index, char ->
                            if (index % 4 == 0) append('-')
                            append(char)
                        }
                    }

                    if (error != null) onClearError()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                placeholder = {
                    Text(
                        text = "TR-XXXX-XXXX",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        ),
                        color = TextDisabled,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                isError = error != null,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CyanGlow,
                    unfocusedTextColor = CyanGlow,
                    focusedBorderColor = if (isValid) SuccessGreen else NeonCyan,
                    unfocusedBorderColor = BorderSubtle,
                    errorBorderColor = ErrorRose,
                    cursorColor = NeonCyan,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    errorContainerColor = CardSurface
                )
            )

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ErrorRose.copy(alpha = 0.11f))
                        .padding(13.dp)
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRose
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyanFaint)
                    .padding(14.dp)
            ) {
                Text(
                    text = "العنوان هيوصلك فاضي",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanGlow
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "رسايل المالك القديم بتتمسح تلقائياً",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            Spacer(Modifier.height(22.dp))

            GlowButton(
                text = if (isLoading) "بيتم الاستلام..." else "استلام",
                enabled = isValid && !isLoading,
                onClick = { onClaim(code) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
