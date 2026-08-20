package com.burootro.mailio.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burootro.mailio.domain.model.AddressLifetime
import com.burootro.mailio.ui.components.GlowButton
import com.burootro.mailio.ui.theme.*

@Composable
fun CreateAddressSheet(
    domains: List<String>,
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (localPart: String?, label: String?, lifetime: AddressLifetime, domain: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var manualMode by remember { mutableStateOf(false) }
    var localPart by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var selectedLifetime by remember { mutableStateOf(AddressLifetime.PERMANENT) }
    val selectedDomain = domains.first()

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

            Text(
                text = "عنوان جديد",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            Spacer(Modifier.height(20.dp))

            // ===== الزرار السريع: عشوائي =====
            if (!manualMode) {
                GlowButton(
                    text = if (isCreating) "بيتعمل..." else "إيميل عشوائي فوراً",
                    enabled = !isCreating,
                    onClick = {
                        onCreate(null, null, AddressLifetime.PERMANENT, selectedDomain)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = DeepVoid,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyanFaint)
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                        .clickable { manualMode = true }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "أكتب الاسم بنفسي",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyanGlow
                    )
                }
            }

            // ===== الوضع اليدوي =====
            AnimatedVisibility(
                visible = manualMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {

                    OutlinedTextField(
                        value = localPart,
                        onValueChange = { input ->
                            localPart = input.lowercase().filter {
                                it.isLetterOrDigit() || it == '.' || it == '_' || it == '-'
                            }.take(32)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "اسم العنوان",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDisabled
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None
                        ),
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

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyanFaint)
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${localPart.ifBlank { "الاسم" }}@$selectedDomain",
                            style = EmailAddressStyle,
                            color = CyanGlow,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "اسم للتمييز (اختياري)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it.take(30) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "مثلاً: تسجيلات المواقع",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDisabled
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
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

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "مدة الصلاحية",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AddressLifetime.entries.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowItems.forEach { lifetime ->
                                    LifetimeChip(
                                        lifetime = lifetime,
                                        selected = selectedLifetime == lifetime,
                                        onClick = { selectedLifetime = lifetime },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    GlowButton(
                        text = if (isCreating) "بيتعمل..." else "إنشاء العنوان",
                        enabled = !isCreating && localPart.length >= 3,
                        onClick = {
                            onCreate(
                                localPart.takeIf { it.isNotBlank() },
                                label.takeIf { it.isNotBlank() },
                                selectedLifetime,
                                selectedDomain
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    TextButton(
                        onClick = { manualMode = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "رجوع للعشوائي",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LifetimeChip(
    lifetime: AddressLifetime,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) NeonCyan else BorderSubtle,
        animationSpec = tween(250),
        label = "chipBorder"
    )

    val bgColor by animateColorAsState(
        targetValue = if (selected) CyanFaint else CardSurface,
        animationSpec = tween(250),
        label = "chipBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) CyanGlow else TextTertiary,
        animationSpec = tween(250),
        label = "chipText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = lifetime.label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
