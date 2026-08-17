package com.burootro.mailio.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.components.GlowButton
import com.burootro.mailio.ui.components.OutlineGlowButton
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class OnboardPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardPage(
        icon = Icons.Rounded.AutoAwesome,
        title = "إيميلات فورية",
        description = "اعمل عدد غير محدود من العناوين في ثانية واحدة، واستخدمها في أي موقع من غير ما تدي إيميلك الحقيقي"
    ),
    OnboardPage(
        icon = Icons.Rounded.Shield,
        title = "رسايلك محفوظة",
        description = "كل الرسايل بتتخزن على جهازك إنت، وتقدر توصلها في أي وقت حتى من غير إنترنت"
    ),
    OnboardPage(
        icon = Icons.Rounded.Key,
        title = "مفتاح الاسترجاع",
        description = "هنديك مفتاح خاص بيك — بيه تنقل كل إيميلاتك لأي جهاز تاني، وتحافظ عليها لو ضاع الموبايل"
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onRestoreClick: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    val isRegistering by viewModel.isRegistering.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        when (val e = event) {
            OnboardingEvent.Registered -> {
                viewModel.consumeEvent()
                onFinished()
            }
            is OnboardingEvent.Error -> {
                snackbarHostState.showSnackbar(e.message)
                viewModel.consumeEvent()
            }
            null -> Unit
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
                    .height(420.dp)
                    .align(Alignment.TopCenter)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onRestoreClick,
                        modifier = Modifier.alpha(if (isLastPage) 0f else 1f)
                    ) {
                        Text(
                            text = "عندي مفتاح",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->

                    val offset = (
                        (pagerState.currentPage - page) +
                            pagerState.currentPageOffsetFraction
                        ).absoluteValue

                    val contentScale = lerp(0.82f, 1f, 1f - offset.coerceIn(0f, 1f))
                    val contentAlpha = lerp(0.3f, 1f, 1f - offset.coerceIn(0f, 1f))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp)
                            .scale(contentScale)
                            .alpha(contentAlpha),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .blur(34.dp)
                                    .clip(RoundedCornerShape(44.dp))
                                    .background(MailioGradients.primaryDiagonal)
                                    .alpha(0.55f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(44.dp))
                                    .background(CardSurface)
                                    .background(MailioGradients.cyanSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = pages[page].icon,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(58.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(46.dp))

                        Text(
                            text = pages[page].title,
                            style = MaterialTheme.typography.displaySmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = pages[page].description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 26.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val selected = pagerState.currentPage == index

                        val width by animateDpAsState(
                            targetValue = if (selected) 30.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "dotWidth"
                        )

                        val color by animateColorAsState(
                            targetValue = if (selected) NeonCyan else BorderSubtle,
                            animationSpec = tween(300),
                            label = "dotColor"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(width)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(bottom = padding.calculateBottomPadding() + 36.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlowButton(
                        text = when {
                            isRegistering -> "بيتم التجهيز..."
                            isLastPage -> "ابدأ حساب جديد"
                            else -> "التالي"
                        },
                        enabled = !isRegistering,
                        onClick = {
                            if (isLastPage) {
                                viewModel.startNewAccount()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isLastPage) {
                        OutlineGlowButton(
                            text = "عندي مفتاح استرجاع",
                            onClick = onRestoreClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (pagerState.currentPage > 0) {
                        OutlineGlowButton(
                            text = "رجوع",
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction
