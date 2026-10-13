package com.burootro.mailio.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.domain.model.AddressLifetime
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.ui.components.ActionRow
import com.burootro.mailio.ui.components.ConfirmDialog
import com.burootro.mailio.ui.components.MxSheet
import com.burootro.mailio.ui.components.OutlineButton
import com.burootro.mailio.ui.components.PrimaryButton
import com.burootro.mailio.ui.components.SelectChip
import com.burootro.mailio.ui.components.SheetHeader
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.components.mxFieldColors
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import kotlinx.coroutines.delay

// ============================================================
// Create address
// ============================================================

@Composable
fun CreateAddressSheet(
    domains: List<String>,
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (localPart: String?, label: String?, lifetime: AddressLifetime, domain: String) -> Unit
) {
    var custom by rememberSaveable { mutableStateOf(false) }
    var localPart by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf("") }
    var lifetime by rememberSaveable { mutableStateOf(AddressLifetime.PERMANENT) }
    val domain = domains.first()
    val validName = localPart.length in 3..32 && localPart.matches(Regex("^[a-z0-9]+([._-][a-z0-9]+)*$"))

    MxSheet(onDismiss = onDismiss) {
        SheetHeader(
            icon = Icons.Rounded.AutoAwesome,
            title = "عنوان جديد",
            subtitle = "عشوائي في ثانية، أو اختار اسمك بنفسك"
        )

        // Mode switch
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ModeCard(
                selected = !custom,
                icon = Icons.Rounded.Bolt,
                title = "عشوائي",
                subtitle = "أسرع حاجة",
                modifier = Modifier.weight(1f)
            ) { custom = false }
            ModeCard(
                selected = custom,
                icon = Icons.Rounded.Edit,
                title = "اسم مخصص",
                subtitle = "زي ما تحب",
                modifier = Modifier.weight(1f)
            ) { custom = true }
        }

        AnimatedVisibility(custom, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = localPart,
                    onValueChange = { v -> localPart = v.lowercase().filter { it.isLetterOrDigit() || it in "._-" }.take(32) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("اسم العنوان") },
                    singleLine = true,
                    textStyle = MonoStyle.copy(color = Mx.Ink, fontSize = 15.sp, textDirection = TextDirection.Ltr),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, capitalization = KeyboardCapitalization.None),
                    colors = mxFieldColors(),
                    isError = localPart.isNotEmpty() && !validName
                )
                Spacer(Modifier.height(8.dp))
                // Live preview of the final address
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Mx.SignatureSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (validName) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                        null,
                        tint = if (validName) Mx.Green else Mx.Text3,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${localPart.ifBlank { "الاسم" }}@$domain",
                        style = MonoStyle.copy(color = Mx.RoyalDeep, textDirection = TextDirection.Ltr),
                        maxLines = 1
                    )
                }
                Text(
                    "حروف إنجليزي وأرقام، ومسموح بـ . _ - في النص",
                    style = MaterialTheme.typography.bodySmall,
                    color = Mx.Text3,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = label,
            onValueChange = { label = it.take(40) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("اسم للتمييز (اختياري)") },
            placeholder = { Text("مثلاً: تسجيلات المواقع", color = Mx.Text4) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = mxFieldColors()
        )

        Spacer(Modifier.height(16.dp))
        Text("مدة الصلاحية", style = MaterialTheme.typography.labelLarge, color = Mx.Text2)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AddressLifetime.entries.forEach { option ->
                SelectChip(
                    text = option.label,
                    selected = lifetime == option,
                    icon = if (option == AddressLifetime.PERMANENT) null else Icons.Rounded.Schedule,
                    onClick = { lifetime = option }
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            text = if (custom) "إنشاء العنوان" else "اعملّي عنوان عشوائي",
            icon = if (custom) Icons.Rounded.CheckCircle else Icons.Rounded.Bolt,
            loading = isCreating,
            enabled = !custom || validName,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onCreate(
                    if (custom) localPart else null,
                    label.trim().ifBlank { null },
                    lifetime,
                    domain
                )
            }
        )
    }
}

@Composable
private fun ModeCard(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (selected) Mx.RoyalTint else Mx.SurfaceSoft)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Mx.Royal else Mx.Line, shape)
            .bounceClick(onClick = onClick)
            .padding(14.dp)
    ) {
        Icon(icon, null, tint = if (selected) Mx.Royal else Mx.Text3, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = if (selected) Mx.RoyalDeep else Mx.InkSoft)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mx.Text3)
    }
}

// ============================================================
// Address actions
// ============================================================

@Composable
fun AddressActionsSheet(
    address: MailAddress,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: (String?) -> Unit,
    onTransfer: () -> Unit,
    onDelete: () -> Unit
) {
    var renaming by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf(address.label ?: "") }

    MxSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(address.displayName, style = MaterialTheme.typography.titleLarge, color = Mx.Ink)
            Text(
                address.email,
                style = MonoStyle.copy(color = Mx.Text2, textDirection = TextDirection.Ltr)
            )
        }
        Spacer(Modifier.height(16.dp))

        AnimatedContent(targetState = renaming, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "rename") { editing ->
            if (editing) {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it.take(40) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("الاسم الجديد") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = mxFieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlineButton("رجوع", onClick = { renaming = false }, modifier = Modifier.weight(1f))
                        PrimaryButton(
                            "حفظ",
                            onClick = { onRename(newName.trim().ifBlank { null }) },
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )
                    }
                }
            } else {
                Column {
                    ActionRow(Icons.Rounded.ContentCopy, "نسخ العنوان", "عشان تلزقه في أي موقع", onClick = onCopy)
                    ActionRow(Icons.Rounded.Send, "مشاركة", "ابعته لأي حد أو أي تطبيق", tint = Mx.Sky, background = androidx.compose.ui.graphics.Color(0xFFE6F6FE), onClick = onShare)
                    ActionRow(
                        Icons.Rounded.PushPin,
                        if (address.isPinned) "إلغاء التثبيت" else "تثبيت فوق",
                        "يظهر في أول القائمة دايماً",
                        tint = Mx.Gold,
                        background = Mx.GoldTint,
                        onClick = onTogglePin
                    )
                    ActionRow(Icons.Rounded.DriveFileRenameOutline, "تغيير الاسم", "الاسم اللي بيظهر في القائمة", tint = Mx.Violet, background = Mx.VioletTint) {
                        renaming = true
                    }
                    ActionRow(Icons.Rounded.SwapHoriz, "نقل لشخص تاني", "العنوان ينتقل والرسائل تتمسح", tint = Mx.Amber, background = Mx.AmberTint, onClick = onTransfer)
                    ActionRow(
                        Icons.Rounded.DeleteForever,
                        "حذف العنوان",
                        "هيتمسح هو وكل رسائله",
                        tint = Mx.Rose,
                        background = Mx.RoseTint,
                        titleColor = Mx.Rose
                    ) { confirmDelete = true }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            icon = Icons.Rounded.DeleteForever,
            title = "متأكد إنك عايز تحذفه؟",
            message = "هيتمسح ${address.email} وكل الرسائل اللي عليه، ومفيش رجوع.",
            confirmText = "احذف",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

// ============================================================
// Transfer (give) / Claim (receive)
// ============================================================

@Composable
fun TransferSheet(
    email: String,
    code: String?,
    isLoading: Boolean,
    expiresAt: Long?,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit,
    onCopy: (String) -> Unit,
    onCancel: () -> Unit
) {
    MxSheet(onDismiss = onDismiss) {
        SheetHeader(
            icon = Icons.Rounded.SwapHoriz,
            title = "نقل العنوان",
            subtitle = email,
            tint = Mx.Amber,
            background = Mx.AmberTint
        )

        AnimatedContent(targetState = code, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "transfer") { current ->
            if (current == null) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Mx.AmberTint)
                            .padding(14.dp)
                    ) {
                        Icon(Icons.Rounded.WarningAmber, null, tint = Mx.Amber, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("قبل ما تكمّل", style = MaterialTheme.typography.labelLarge, color = Mx.Amber)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "• كل الرسائل هتتمسح نهائياً\n• العنوان هيبقى ملك الشخص التاني\n• مش هتقدر ترجّعه بعد كده",
                                style = MaterialTheme.typography.bodySmall,
                                color = Mx.InkSoft
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(
                        "توليد كود النقل",
                        onClick = onGenerate,
                        loading = isLoading,
                        icon = Icons.Rounded.AutoAwesome,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ادّي الكود ده للشخص التاني", style = MaterialTheme.typography.bodyMedium, color = Mx.Text2)
                    Spacer(Modifier.height(12.dp))
                    BigCode(current, onCopy = { onCopy(current) })
                    Spacer(Modifier.height(10.dp))
                    if (expiresAt != null) Countdown(expiresAt)
                    Spacer(Modifier.height(18.dp))
                    OutlineButton(
                        "إلغاء النقل",
                        onClick = onCancel,
                        tint = Mx.Rose,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BigCode(code: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1800)
            copied = false
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Mx.SignatureSoft)
            .border(1.5.dp, if (copied) Mx.Green else Mx.RoyalTint2, RoundedCornerShape(20.dp))
            .bounceClick {
                copied = true
                onCopy()
            }
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            code,
            style = MonoStyle.copy(fontSize = 30.sp, fontWeight = FontWeight.Black, color = Mx.RoyalDeep, letterSpacing = 6.sp, textDirection = TextDirection.Ltr)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (copied) "اتنسخ ✓" else "اضغط للنسخ",
            style = MaterialTheme.typography.labelMedium,
            color = if (copied) Mx.Green else Mx.Text3
        )
    }
}

@Composable
private fun Countdown(expiresAt: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(expiresAt) {
        while (now < expiresAt) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    val left = ((expiresAt - now) / 1000).coerceAtLeast(0)
    Text(
        if (left > 0) "الكود صالح لمدة ${left / 60}:${(left % 60).toString().padStart(2, '0')}" else "الكود انتهت صلاحيته",
        style = MaterialTheme.typography.labelMedium,
        color = if (left > 60) Mx.Text2 else Mx.Rose
    )
}

@Composable
fun ClaimTransferSheet(
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onClaim: (String) -> Unit,
    onClearError: () -> Unit
) {
    var code by rememberSaveable { mutableStateOf("") }
    MxSheet(onDismiss = onDismiss) {
        SheetHeader(
            icon = Icons.Rounded.SwapHoriz,
            title = "استلام عنوان",
            subtitle = "اكتب كود النقل اللي وصلك"
        )
        OutlinedTextField(
            value = code,
            onValueChange = {
                code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(12)
                onClearError()
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            textStyle = MonoStyle.copy(fontSize = 22.sp, color = Mx.Ink, textAlign = TextAlign.Center, letterSpacing = 4.sp, textDirection = TextDirection.Ltr),
            placeholder = { Text("الكود", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Mx.Text4) },
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            colors = mxFieldColors()
        )
        AnimatedVisibility(error != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Text(error ?: "", color = Mx.Rose, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "العنوان هيوصلك فاضي — رسائل المالك القديم بتتمسح تلقائياً",
            style = MaterialTheme.typography.bodySmall,
            color = Mx.Text3,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            "استلام",
            onClick = { onClaim(code) },
            loading = isLoading,
            enabled = code.length >= 4,
            icon = Icons.Rounded.CheckCircle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================
// Appeal for a suspended address
// ============================================================

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
    var text by rememberSaveable { mutableStateOf("") }
    MxSheet(onDismiss = onDismiss) {
        AnimatedContent(targetState = isSubmitted, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "appeal") { done ->
            if (done) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    SheetHeader(
                        icon = Icons.Rounded.CheckCircle,
                        title = "وصلنا طلبك",
                        subtitle = "هنراجع الموضوع ونرد عليك بإشعار",
                        tint = Mx.Green,
                        background = Mx.GreenTint
                    )
                    PrimaryButton("تمام", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Column {
                    SheetHeader(
                        icon = Icons.Rounded.Gavel,
                        title = "طلب مراجعة",
                        subtitle = email,
                        tint = Mx.Rose,
                        background = Mx.RoseTint
                    )
                    if (!blockedReason.isNullOrBlank()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Mx.RoseTint)
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Rounded.Block, null, tint = Mx.Rose, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("السبب: $blockedReason", style = MaterialTheme.typography.bodySmall, color = Mx.InkSoft)
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it.take(1000)
                            onClearError()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        label = { Text("لو شايف إن ده غلط، اشرحلنا") },
                        shape = RoundedCornerShape(16.dp),
                        isError = error != null,
                        colors = mxFieldColors()
                    )
                    AnimatedVisibility(error != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Text(error ?: "", color = Mx.Rose, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        "إرسال الطلب",
                        onClick = { onSubmit(text.trim()) },
                        loading = isLoading,
                        enabled = text.trim().length >= 10,
                        icon = Icons.Rounded.Send,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "بنراجع الطلبات بإيدينا، فممكن ياخد شوية وقت",
                        style = MaterialTheme.typography.bodySmall,
                        color = Mx.Text3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
