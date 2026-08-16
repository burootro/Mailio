package com.burootro.mailio.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.ui.components.GlowButton
import com.burootro.mailio.ui.components.OutlineGlowButton
import com.burootro.mailio.ui.theme.*

@Composable
fun AddressActionsSheet(
    address: MailAddress,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightBlue,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BorderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 34.dp)
        ) {

            // رأس النافذة
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MailioGradients.cyanSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = address.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = CyanGlow
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = address.email,
                        style = EmailAddressStyle.copy(
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        ),
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            ActionRow(
                icon = Icons.Rounded.PushPin,
                title = if (address.isPinned) "إلغاء التثبيت" else "تثبيت في الأعلى",
                subtitle = "يظهر في أول القائمة دايماً",
                tint = WarningAmber,
                onClick = onTogglePin
            )

            Spacer(Modifier.height(10.dp))

            ActionRow(
                icon = Icons.Rounded.DriveFileRenameOutline,
                title = "تغيير الاسم",
                subtitle = "الاسم اللي بيتعرض في القائمة",
                tint = CyanGlow,
                onClick = { showRenameDialog = true }
            )

            Spacer(Modifier.height(10.dp))

            ActionRow(
                icon = Icons.Rounded.DeleteForever,
                title = "حذف العنوان",
                subtitle = "هيتمسح هو وكل رسايله",
                tint = ErrorRose,
                onClick = { showDeleteConfirm = true }
            )
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            currentLabel = address.label ?: "",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newLabel ->
                showRenameDialog = false
                onRename(newLabel)
            }
        )
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            email = address.email,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun RenameDialog(
    currentLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var text by remember { mutableStateOf(currentLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "تغيير الاسم",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(30) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "اكتب اسم جديد",
                        color = TextDisabled
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderSubtle,
                    cursorColor = NeonCyan,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                )
            )
        },
        confirmButton = {
            GlowButton(
                text = "حفظ",
                onClick = { onConfirm(text.takeIf { it.isNotBlank() }) },
                modifier = Modifier.width(110.dp)
            )
        },
        dismissButton = {
            OutlineGlowButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.width(110.dp)
            )
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    email: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "متأكد؟",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "هيتمسح $email وكل الرسايل اللي عليه، ومفيش رجوع.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorRose)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "احذف",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
        },
        dismissButton = {
            OutlineGlowButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.width(110.dp)
            )
        }
    )
}
