package com.burootro.mailio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.burootro.mailio.ui.theme.Mx

/**
 * The Mailio mark: a gradient squircle with a white envelope and a small spark.
 * [draw] goes 0..1 to animate the envelope being drawn (used on the splash).
 */
@Composable
fun BrandMark(
    size: Dp = 72.dp,
    draw: Float = 1f,
    spark: Float = 1f,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size * 0.3f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(size * 0.28f, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.45f), spotColor = Mx.Violet.copy(alpha = 0.45f))
            .clip(shape)
            .background(Mx.Signature),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val stroke = w * 0.065f
            val left = w * 0.22f
            val top = h * 0.31f
            val right = w * 0.78f
            val bottom = h * 0.71f

            // Envelope body
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                cornerRadius = CornerRadius(w * 0.07f),
                style = Stroke(width = stroke),
                alpha = draw.coerceIn(0f, 1f)
            )

            // Flap — drawn progressively
            val flap = Path().apply {
                moveTo(left + stroke, top + stroke)
                lineTo(w / 2f, top + (bottom - top) * 0.55f * draw.coerceIn(0f, 1f) + stroke * 0.5f)
                lineTo(right - stroke, top + stroke)
            }
            drawPath(
                flap,
                color = Color.White,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                alpha = draw.coerceIn(0f, 1f)
            )

            // Spark
            val s = spark.coerceIn(0f, 1f)
            if (s > 0f) {
                val cx = w * 0.76f
                val cy = h * 0.27f
                val r = w * 0.09f * s
                drawCircle(Color(0xFF7DD3FC), radius = r, center = Offset(cx, cy))
                drawCircle(Color.White, radius = r * 0.45f, center = Offset(cx, cy))
            }
        }
    }
}
