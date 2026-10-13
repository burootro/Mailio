package com.burootro.mailio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burootro.mailio.ui.theme.Mx

/** White bottom sheet with a slim handle — used for every action panel. */
@Composable
fun MxSheet(
    onDismiss: () -> Unit,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = Mx.Surface,
        scrimColor = Mx.Scrim,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Mx.LineStrong)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 20.dp)
                .navigationBarsPadding(),
            content = content
        )
    }
}

/** Big centered header used at the top of sheets. */
@Composable
fun SheetHeader(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = Mx.Royal,
    background: Color = Mx.RoyalTint
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        IconTile(icon, tint = tint, background = background, size = 54.dp, iconSize = 26.dp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Mx.Ink, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Mx.Text2, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(18.dp))
    }
}

/** Row inside an action sheet. */
@Composable
fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = Mx.Royal,
    background: Color = Mx.RoyalTint,
    titleColor: Color = Mx.Ink,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, tint = tint, background = background, size = 38.dp, iconSize = 19.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mx.Text3)
            }
        }
        Icon(Icons.Rounded.ChevronLeft, null, tint = Mx.Text4, modifier = Modifier.size(18.dp))
    }
}

/** Clean confirmation dialog. */
@Composable
fun ConfirmDialog(
    icon: ImageVector,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Mx.Surface,
        shape = RoundedCornerShape(26.dp),
        icon = {
            IconTile(
                icon,
                tint = if (danger) Mx.Rose else Mx.Royal,
                background = if (danger) Mx.RoseTint else Mx.RoyalTint,
                size = 52.dp,
                iconSize = 24.dp
            )
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Mx.Ink, textAlign = TextAlign.Center) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = Mx.Text2, textAlign = TextAlign.Center) },
        confirmButton = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton("إلغاء", onClick = onDismiss, modifier = Modifier.weight(1f), height = 46.dp)
                PrimaryButton(
                    confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    height = 46.dp,
                    brush = if (danger) androidx.compose.ui.graphics.Brush.linearGradient(listOf(Mx.Rose, Color(0xFFF43F5E))) else Mx.Signature
                )
            }
        }
    )
}

@Suppress("unused")
private val bold = FontWeight.Bold
