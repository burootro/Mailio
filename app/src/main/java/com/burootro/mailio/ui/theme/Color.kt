package com.burootro.mailio.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Mailio "Pearl" palette — a bright, premium light theme.
 * Pearl backgrounds, pure white surfaces, deep ink text and a royal indigo → violet signature.
 */
object Mx {
    // Canvas
    val Canvas = Color(0xFFF4F5FA)
    val CanvasDeep = Color(0xFFECEEF6)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFF8F9FD)
    val Fill = Color(0xFFEFF1F8)
    val FillStrong = Color(0xFFE4E7F2)
    val Line = Color(0xFFE6E8F1)
    val LineStrong = Color(0xFFD5D9E7)

    // Ink
    val Ink = Color(0xFF0C1020)
    val InkSoft = Color(0xFF2A3045)
    val Text2 = Color(0xFF5A6176)
    val Text3 = Color(0xFF8B91A5)
    val Text4 = Color(0xFFB4B9C9)

    // Signature
    val Royal = Color(0xFF4F46E5)
    val RoyalDeep = Color(0xFF3730A3)
    val Violet = Color(0xFF7C3AED)
    val Sky = Color(0xFF0EA5E9)
    val Aqua = Color(0xFF06B6D4)
    val RoyalTint = Color(0xFFEEF0FF)
    val RoyalTint2 = Color(0xFFE0E3FF)
    val VioletTint = Color(0xFFF3EDFF)

    // Luxury accent
    val Gold = Color(0xFFC8962B)
    val GoldTint = Color(0xFFFFF6E0)

    // Status
    val Green = Color(0xFF12A150)
    val GreenTint = Color(0xFFE6F7EE)
    val Amber = Color(0xFFE08A00)
    val AmberTint = Color(0xFFFFF4E0)
    val Rose = Color(0xFFE11D48)
    val RoseTint = Color(0xFFFFE8EE)

    val Scrim = Color(0x660C1020)

    // Gradients
    val Signature: Brush = Brush.linearGradient(listOf(Royal, Violet))
    val SignatureSoft: Brush = Brush.linearGradient(listOf(RoyalTint, VioletTint))
    val Aurora: Brush = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFFDB2777)))
    val Ocean: Brush = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF4F46E5)))
    val GoldBrush: Brush = Brush.linearGradient(listOf(Color(0xFFE9C46A), Color(0xFFC8962B)))
    val HeroWash: Brush = Brush.verticalGradient(
        listOf(Color(0xFFE7E9FF), Color(0xFFF1ECFF), Color(0xFFF4F5FA))
    )

    /** Stable pastel colors for avatars — the same seed always gets the same color. */
    private val avatarPairs = listOf(
        Color(0xFF4F46E5) to Color(0xFFEEF0FF),
        Color(0xFF7C3AED) to Color(0xFFF3EDFF),
        Color(0xFF0EA5E9) to Color(0xFFE6F6FE),
        Color(0xFF12A150) to Color(0xFFE6F7EE),
        Color(0xFFE08A00) to Color(0xFFFFF4E0),
        Color(0xFFE11D48) to Color(0xFFFFE8EE),
        Color(0xFFDB2777) to Color(0xFFFDE8F3),
        Color(0xFF0D9488) to Color(0xFFE3F7F4)
    )

    fun avatar(seed: String): Pair<Color, Color> {
        val h = seed.lowercase().hashCode()
        val index = (if (h < 0) -(h + 1) else h) % avatarPairs.size
        return avatarPairs[index]
    }
}
