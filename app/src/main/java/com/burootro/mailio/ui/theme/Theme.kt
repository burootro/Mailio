package com.burootro.mailio.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val MailioColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepVoid,
    primaryContainer = CyanDeep,
    onPrimaryContainer = CyanGlow,

    secondary = ElectricViolet,
    onSecondary = TextPrimary,
    secondaryContainer = VioletFaint,
    onSecondaryContainer = VioletGlow,

    tertiary = CyanGlow,
    onTertiary = DeepVoid,

    background = DeepVoid,
    onBackground = TextPrimary,

    surface = MidnightBlue,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = ElevatedSurface,

    error = ErrorRose,
    onError = TextPrimary,

    outline = BorderSubtle,
    outlineVariant = BorderSubtle,

    scrim = Color(0xCC000000)
)

val MailioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// ===== تدرجات جاهزة للاستخدام في كل الشاشات =====
object MailioGradients {

    val primaryHorizontal = Brush.horizontalGradient(
        colors = listOf(GradientStart, GradientMid, GradientEnd)
    )

    val primaryDiagonal = Brush.linearGradient(
        colors = listOf(GradientStart, GradientEnd),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    val cyanSoft = Brush.linearGradient(
        colors = listOf(
            NeonCyan.copy(alpha = 0.25f),
            ElectricViolet.copy(alpha = 0.10f),
            Color.Transparent
        )
    )

    val cardSheen = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.06f),
            Color.Transparent
        )
    )

    val backgroundGlow = Brush.radialGradient(
        colors = listOf(
            NeonCyan.copy(alpha = 0.14f),
            ElectricViolet.copy(alpha = 0.06f),
            Color.Transparent
        ),
        radius = 900f
    )

    val shimmer = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase)
    )
}

@Composable
fun MailioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // التطبيق دارك دايماً — الفخامة في الثبات
    val colorScheme = MailioColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MailioTypography,
        shapes = MailioShapes,
        content = content
    )
}
