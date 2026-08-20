package com.burootro.mailio.ui.screens.signin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.theme.*

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onSignInResult(result.data)
        } else {
            viewModel.onSignInCancelled()
        }
    }

    LaunchedEffect(event) {
        when (event) {
            is SignInEvent.Success, SignInEvent.GuestReady -> {
                viewModel.consumeEvent()
                onSignedIn()
            }
            null -> Unit
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val transition = rememberInfiniteTransition(label = "signInGlow")

    val glowScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val ring by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    val ringAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    val busy = state.isLoading || state.isSkipping

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepVoid)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .align(Alignment.TopCenter)
                .background(MailioGradients.backgroundGlow)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(ring)
                        .alpha(ringAlpha)
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(50))
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(glowScale)
                        .blur(32.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(MailioGradients.primaryDiagonal)
                        .alpha(0.7f)
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(MailioGradients.primaryDiagonal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MarkEmailUnread,
                        contentDescription = null,
                        tint = DeepVoid,
                        modifier = Modifier.size(58.dp)
                    )
                }
            }

            Spacer(Modifier.height(34.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + slideInVertically(
                    animationSpec = tween(800),
                    initialOffsetY = { it / 3 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MAILIO",
                        style = MaterialTheme.typography.displayMedium.copy(
                            letterSpacing = 8.sp
                        ),
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(2.dp)
                            .background(MailioGradients.primaryHorizontal)
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "إيميلات مؤقتة في ثانية",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, delayMillis = 300))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FeatureRow(
                        icon = Icons.Rounded.AutoAwesome,
                        text = "عناوين غير محدودة بضغطة واحدة"
                    )
                    FeatureRow(
                        icon = Icons.Rounded.Shield,
                        text = "رسايلك محفوظة على كل أجهزتك"
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ErrorRose.copy(alpha = 0.12f))
                        .padding(14.dp),
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

            GoogleSignInButton(
                isLoading = state.isLoading,
                enabled = !busy,
                onClick = {
                    viewModel.clearError()
                    launcher.launch(viewModel.getSignInIntent())
                }
            )

            Spacer(Modifier.height(14.dp))

            SkipButton(
                isLoading = state.isSkipping,
                enabled = !busy,
                onClick = {
                    viewModel.clearError()
                    viewModel.continueAsGuest()
                }
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "بدون تسجيل، عناوينك محفوظة على الجهاز ده بس",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyanFaint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanGlow,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) Color.White else Color.White.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF1A73E8),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF4285F4)
                )
                Text(
                    text = "تسجيل الدخول بجوجل",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF1F1F1F)
                )
            }
        }
    }
}

@Composable
private fun SkipButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CyanFaint)
            .border(
                1.dp,
                NeonCyan.copy(alpha = if (enabled) 0.35f else 0.15f),
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = CyanGlow,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = "المتابعة بدون تسجيل",
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) CyanGlow else TextDisabled
            )
        }
    }
}
