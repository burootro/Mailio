package com.burootro.mailio.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MarkEmailUnread
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    var startAnimation by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var minTimePassed by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(700),
        label = "logoAlpha"
    )

    val ringTransition = rememberInfiniteTransition(label = "rings")

    val ring1 by ringTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )

    val ring1Alpha by ringTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )

    val ring2 by ringTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1300)
        ),
        label = "ring2"
    )

    val ring2Alpha by ringTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1300)
        ),
        label = "ring2Alpha"
    )

    val glowBreath by ringTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBreath"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(500)
        showText = true
        delay(1500)
        minTimePassed = true
    }

    LaunchedEffect(destination, minTimePassed) {
        if (!minTimePassed) return@LaunchedEffect

        when (destination) {
            StartDestination.Home -> onNavigateToHome()
            StartDestination.SignIn -> onNavigateToSignIn()
            StartDestination.Loading -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepVoid),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(360.dp)
                .alpha(glowBreath * 0.6f)
                .background(MailioGradients.backgroundGlow)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring1)
                        .alpha(ring1Alpha)
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(50))
                )

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring2)
                        .alpha(ring2Alpha)
                        .border(1.5.dp, ElectricViolet, RoundedCornerShape(50))
                )

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha * glowBreath)
                        .blur(28.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .background(MailioGradients.primaryDiagonal)
                )

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .clip(RoundedCornerShape(34.dp))
                        .background(MailioGradients.primaryDiagonal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MarkEmailUnread,
                        contentDescription = null,
                        tint = DeepVoid,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(Modifier.height(34.dp))

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(tween(800)) + slideInVertically(
                    animationSpec = tween(800),
                    initialOffsetY = { it / 2 }
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
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(2.dp)
                            .background(MailioGradients.primaryHorizontal)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "إيميلات مؤقتة • خصوصية كاملة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(tween(1200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled,
                modifier = Modifier.padding(bottom = 44.dp)
            )
        }
    }
}
