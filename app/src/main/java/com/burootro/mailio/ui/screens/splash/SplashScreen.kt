package com.burootro.mailio.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.components.BrandMark
import com.burootro.mailio.ui.theme.Mx
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Fast splash (~0.7s): the envelope draws itself, then we jump straight in. */
@Composable
fun SplashScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    val scale = remember { Animatable(0.6f) }
    val draw = remember { Animatable(0f) }
    val spark = remember { Animatable(0f) }
    val text = remember { Animatable(0f) }
    var animationDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
            val d = async { draw.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
            launch { text.animateTo(1f, tween(380, delayMillis = 150)) }
            d.await()
            spark.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
        }
        animationDone = true
    }

    LaunchedEffect(destination, animationDone) {
        if (!animationDone) return@LaunchedEffect
        when (destination) {
            StartDestination.Home -> onNavigateToHome()
            StartDestination.SignIn -> onNavigateToSignIn()
            StartDestination.Loading -> Unit
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Mx.HeroWash),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandMark(
                size = 92.dp,
                draw = draw.value,
                spark = spark.value,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            )
            Spacer(Modifier.height(22.dp))
            Text(
                "Mailio",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Mx.Ink,
                modifier = Modifier.graphicsLayer {
                    alpha = text.value
                    translationY = (1f - text.value) * 16f
                }
            )
            Text(
                "إيميلات مؤقتة • خصوصية كاملة",
                style = MaterialTheme.typography.bodyMedium,
                color = Mx.Text2,
                modifier = Modifier.graphicsLayer { alpha = text.value }
            )
        }
    }
}
