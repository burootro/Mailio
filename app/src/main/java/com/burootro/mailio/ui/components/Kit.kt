package com.burootro.mailio.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.ui.theme.Mx

// ------------------------------------------------------------
// Surfaces
// ------------------------------------------------------------

/** White premium card with a soft tinted shadow. */
@Composable
fun MxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    radius: Dp = 20.dp,
    background: Color = Mx.Surface,
    borderColor: Color = Mx.Line,
    elevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .shadow(elevation, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.10f), spotColor = Mx.Royal.copy(alpha = 0.10f))
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) Modifier.bounceClick(pressedScale = 0.975f, onLongClick = onLongClick, onClick = onClick)
                else Modifier
            )
            .padding(padding),
        content = content
    )
}

/** Small rounded square holding an icon — used everywhere instead of big icons. */
@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color = Mx.Royal,
    background: Color = Mx.RoyalTint,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** Circular-ish icon button with spring press. */
@Composable
fun MxIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Mx.InkSoft,
    background: Color = Mx.Surface,
    border: Color = Mx.Line,
    size: Dp = 40.dp,
    iconSize: Dp = 19.dp,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(size * 0.34f))
            .bounceClick(enabled = enabled, pressedScale = 0.88f, onClick = onClick)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

// ------------------------------------------------------------
// Buttons
// ------------------------------------------------------------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    brush: Brush = Mx.Signature,
    height: Dp = 50.dp
) {
    val active = enabled && !loading
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(height)
            .shadow(if (active) 14.dp else 0.dp, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.35f), spotColor = Mx.Royal.copy(alpha = 0.35f))
            .clip(shape)
            .background(if (enabled) brush else Brush.linearGradient(listOf(Mx.FillStrong, Mx.FillStrong)))
            .bounceClick(enabled = active, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = { (fadeIn(tween(160)) + scaleIn(initialScale = 0.8f)) togetherWith fadeOut(tween(120)) },
            label = "primaryLoading"
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, null, tint = if (enabled) Color.White else Mx.Text3, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text,
                        color = if (enabled) Color.White else Mx.Text3,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Mx.Royal,
    background: Color = Mx.RoyalTint,
    height: Dp = 46.dp,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, color = tint, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Mx.InkSoft,
    height: Dp = 48.dp
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Mx.Surface)
            .border(1.dp, Mx.LineStrong, RoundedCornerShape(16.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = tint, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

// ------------------------------------------------------------
// Chips / pills / badges
// ------------------------------------------------------------

@Composable
fun Pill(
    text: String,
    color: Color,
    background: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Count badge that pops when the number changes. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = { (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith (scaleOut(targetScale = 1.3f) + fadeOut()) },
            label = "badge"
        ) { value ->
            Box(
                Modifier
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Mx.Signature)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (value > 99) "99+" else value.toString(),
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val bg by animateColorAsState(if (selected) Mx.Ink else Mx.Surface, tween(220), label = "chipBg")
    val fg by animateColorAsState(if (selected) Color.White else Mx.Text2, tween(220), label = "chipFg")
    val border by animateColorAsState(if (selected) Mx.Ink else Mx.Line, tween(220), label = "chipBorder")
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .bounceClick(pressedScale = 0.92f, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Two/three-way segmented control with a sliding white thumb. */
@Composable
fun Segmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badges: List<Int> = emptyList()
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Mx.FillStrong.copy(alpha = 0.7f))
            .padding(4.dp)
    ) {
        val segment = maxWidth / options.size
        val offset by animateDpAsState(
            targetValue = segment * selected,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
            label = "segOffset"
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(segment)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(11.dp), clip = false, ambientColor = Mx.Ink.copy(alpha = 0.12f), spotColor = Mx.Ink.copy(alpha = 0.12f))
                .clip(RoundedCornerShape(11.dp))
                .background(Mx.Surface)
        )
        Row(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                val active = index == selected
                val color by animateColorAsState(if (active) Mx.Ink else Mx.Text3, tween(200), label = "segText")
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .bounceClick(pressedScale = 0.94f) { onSelect(index) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(label, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    val badge = badges.getOrNull(index) ?: 0
                    if (badge > 0) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (active) Mx.Royal else Mx.Text4)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(if (badge > 99) "99+" else "$badge", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
// Headers
// ------------------------------------------------------------

@Composable
fun TopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            MxIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "رجوع", onClick = onBack)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Mx.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mx.Text3, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = Mx.Text3, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        trailing()
    }
}

// ------------------------------------------------------------
// Inputs
// ------------------------------------------------------------

@Composable
fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val focus = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Mx.Text3, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Mx.Text3, modifier = Modifier.size(19.dp)) },
        trailingIcon = {
            AnimatedVisibility(visible = value.isNotEmpty(), enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Mx.Fill)
                        .bounceClick { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, "مسح", tint = Mx.Text2, modifier = Modifier.size(14.dp))
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Mx.Ink),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        colors = mxFieldColors()
    )
}

@Composable
fun mxFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Mx.Royal,
    unfocusedBorderColor = Mx.Line,
    focusedContainerColor = Mx.Surface,
    unfocusedContainerColor = Mx.Surface,
    cursorColor = Mx.Royal,
    focusedTextColor = Mx.Ink,
    unfocusedTextColor = Mx.Ink,
    focusedLabelColor = Mx.Royal,
    unfocusedLabelColor = Mx.Text3,
    errorBorderColor = Mx.Rose,
    errorLabelColor = Mx.Rose
)

// ------------------------------------------------------------
// Avatars
// ------------------------------------------------------------

@Composable
fun LetterAvatar(seed: String, letter: String, size: Dp = 42.dp, modifier: Modifier = Modifier) {
    val (fg, bg) = Mx.avatar(seed)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.36f))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(letter.take(1).uppercase(), color = fg, fontWeight = FontWeight.Black, fontSize = (size.value * 0.4f).sp)
    }
}

// ------------------------------------------------------------
// States
// ------------------------------------------------------------

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val t = rememberInfiniteTransition(label = "emptyFloat")
    val dy by t.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1800), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "emptyY"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .graphicsLayer { translationY = dy }
                .size(84.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Mx.SignatureSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Mx.Royal, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = Mx.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Mx.Text2, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
fun Skeleton(modifier: Modifier = Modifier, radius: Dp = 18.dp) {
    val t = rememberInfiniteTransition(label = "skeleton")
    val x by t.animateFloat(
        initialValue = -600f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "skeletonX"
    )
    Box(
        modifier
            .clip(RoundedCornerShape(radius))
            .background(
                Brush.linearGradient(
                    listOf(Mx.Fill, Mx.SurfaceSoft, Mx.Fill),
                    start = Offset(x, 0f),
                    end = Offset(x + 500f, 160f)
                )
            )
    )
}

// ------------------------------------------------------------
// Progress ring (address lifetime)
// ------------------------------------------------------------

@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    track: Color = Mx.Fill,
    stroke: Dp = 3.dp
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "ring")
    Canvas(modifier) {
        val w = stroke.toPx()
        drawArc(track, 0f, 360f, false, style = Stroke(w))
        drawArc(color, -90f, 360f * animated, false, style = Stroke(w, cap = StrokeCap.Round))
    }
}

@Suppress("unused")
private val unusedStyle = TextStyle()

@Suppress("unused")
private val unusedBorder = BorderStroke(1.dp, Color.Transparent)
