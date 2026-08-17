package com.burootro.mailio.ui.screens.restore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.components.GlowButton
import com.burootro.mailio.ui.theme.*

@Composable
fun RestoreScreen(
    onBack: () -> Unit,
    onRestored: () -> Unit,
    viewModel: RestoreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        when (val e = event) {
            is RestoreEvent.Success -> {
                snackbarHostState.showSnackbar(
                    "رجّعنا ${e.addresses} عنوان و ${e.messages} رسالة"
                )
                viewModel.consumeEvent()
                onRestored()
            }
            null -> Unit
        }
    }

    val glowTransition = rememberInfiniteTransition(label = "restoreGlow")
    val glowScale by glowTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Scaffold(
        containerColor = DeepVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = padding.calculateBottomPadding() + 40.dp)
            ) {

                // زرار الرجوع
                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
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

                Spacer(Modifier.height(20.dp))

                // الأيقونة
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(glowScale)
                                .blur(30.dp)
                                .clip(RoundedCornerShape(34.dp))
                                .background(MailioGradients.primaryDiagonal)
                        )
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(34.dp))
                                .background(CardSurface)
                                .background(MailioGradients.cyanSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Restore,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "استرجاع حسابك",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "اكتب مفتاح الاسترجاع وهنرجّعلك كل عناوينك ورسايلك",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(34.dp))

                // خانة المفتاح
                OutlinedTextField(
                    value = state.key,
                    onValueChange = viewModel::onKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    placeholder = {
                        Text(
                            text = "MLO-XXXX-XXXX-XXXX-XXXX",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = TextDisabled,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    isError = state.error != null,
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyanGlow,
                        unfocusedTextColor = CyanGlow,
                        focusedBorderColor = if (state.isValidFormat) SuccessGreen else NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        errorBorderColor = ErrorRose,
                        cursorColor = NeonCyan,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        errorContainerColor = CardSurface
                    )
                )

                // رسالة الخطأ
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ErrorRose.copy(alpha = 0.11f))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = state.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRose
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                GlowButton(
                    text = if (state.isLoading) "بيتم الاسترجاع..." else "استرجاع",
                    enabled = state.isValidFormat && !state.isLoading,
                    onClick = { viewModel.restore() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(26.dp))

                // تحذير مهم
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
                        text = "خد بالك",
                        style = MaterialTheme.typography.titleSmall,
                        color = WarningAmber
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "الاسترجاع هيمسح أي عناوين ورسايل موجودة على الجهاز ده دلوقتي، ويحط مكانها بيانات الحساب اللي بتسترجعه.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}
