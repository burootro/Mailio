package com.burootro.mailio.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.ui.components.GlowButton
import com.burootro.mailio.ui.theme.*

@Composable
fun AppealSheet(
    email: String,
    blockedReason: String?,
    isLoading: Boolean,
    error: String?,
    isSubmitted: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    onClearError: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var message by remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 10.dp, bottom = 34.dp)
        ) {

            if (isSubmitted) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "وصلنا طلبك",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "هنراجع الموضوع ونرد عليك بإشعار",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(26.dp))

                    GlowButton(
                        text = "تمام",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ErrorRose.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Block,
                            contentDescription = null,
                            tint = ErrorRose,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Spacer(Modifier.width(13.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "العنوان موقوف",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = email,
                            style = EmailAddressStyle.copy(
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            ),
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!blockedReason.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ErrorRose.copy(alpha = 0.09f))
                            .border(
                                1.dp,
                                ErrorRose.copy(alpha = 0.22f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "السبب",
                            style = MaterialTheme.typography.labelMedium,
                            color = ErrorRose
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = blockedReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    text = "لو تعتقد إن ده غلط، اشرحلنا",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it.take(1000)
                        if (error != null) onClearError()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 130.dp),
                    enabled = !isLoading,
                    placeholder = {
                        Text(
                            text = "اكتب هنا شرح للموضوع...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextDisabled
                        )
                    },
                    isError = error != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        errorBorderColor = ErrorRose,
                        cursorColor = NeonCyan,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        errorContainerColor = CardSurface
                    )
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "${message.length} / 1000",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled,
                    modifier = Modifier.align(Alignment.End)
                )

                if (error != null) {
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ErrorRose.copy(alpha = 0.11f))
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRose,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRose
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                GlowButton(
                    text = if (isLoading) "بيتم الإرسال..." else "إرسال الطلب",
                    enabled = message.trim().length >= 10 && !isLoading,
                    onClick = { onSubmit(message.trim()) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "بنراجع الطلبات يدوياً، فممكن ياخد وقت",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
