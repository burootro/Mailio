package com.burootro.mailio.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val PearlScheme = lightColorScheme(
    primary = Mx.Royal,
    onPrimary = Color.White,
    primaryContainer = Mx.RoyalTint,
    onPrimaryContainer = Mx.RoyalDeep,
    secondary = Mx.Violet,
    onSecondary = Color.White,
    secondaryContainer = Mx.VioletTint,
    onSecondaryContainer = Mx.Violet,
    tertiary = Mx.Sky,
    onTertiary = Color.White,
    background = Mx.Canvas,
    onBackground = Mx.Ink,
    surface = Mx.Surface,
    onSurface = Mx.Ink,
    surfaceVariant = Mx.Fill,
    onSurfaceVariant = Mx.Text2,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Mx.SurfaceSoft,
    surfaceContainer = Mx.Surface,
    surfaceContainerHigh = Mx.Surface,
    surfaceContainerHighest = Mx.Fill,
    outline = Mx.LineStrong,
    outlineVariant = Mx.Line,
    error = Mx.Rose,
    onError = Color.White,
    errorContainer = Mx.RoseTint,
    onErrorContainer = Mx.Rose,
    scrim = Mx.Scrim
)

val MailioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MailioTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    // The interface is Arabic, so it is always laid out right-to-left.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = PearlScheme,
            typography = MailioTypography,
            shapes = MailioShapes,
            content = content
        )
    }
}
