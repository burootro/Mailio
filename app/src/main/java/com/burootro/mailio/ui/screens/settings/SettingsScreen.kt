package com.burootro.mailio.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.burootro.mailio.ui.components.OutlineGlowButton
import com.burootro.mailio.ui.theme.*

private val autoDeleteOptions = listOf(0L, 7L, 14L, 30L)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSigningOut by viewModel.isSigningOut.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        when (val e = event) {
            is SettingsEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(e.text)
                viewModel.consumeEvent()
            }
            SettingsEvent.SignedOut -> {
                viewModel.consumeEvent()
                onSignedOut()
            }
            null -> Unit
        }
    }

    Scaffold(
        containerColor = DeepVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = padding.calculateBottomPadding() + 40.dp)
            ) {

                // الهيدر
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(CardSurface)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "رجوع",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Text(
                        text = "الإعدادات",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(6.dp))

                // بطاقة الحساب
                AccountCard(
                    email = state.googleEmail,
                    name = state.googleName,
                    photo = state.googlePhoto
                )

                Spacer(Modifier.height(26.dp))

                // إحصائيات
                SectionTitle("نظرة عامة")
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        icon = Icons.Rounded.Mail,
                        value = "${state.addressCount}",
                        label = "عنوان",
                        tint = CyanGlow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Rounded.AutoDelete,
                        value = if (state.autoDeleteDays <= 0) "∞" else "${state.autoDeleteDays}",
                        label = if (state.autoDeleteDays <= 0) "بدون حذف" else "يوم للحذف",
                        tint = ElectricViolet,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(26.dp))

                // الإشعارات
                SectionTitle("الإشعارات")
                Spacer(Modifier.height(10.dp))

                SwitchRow(
                    icon = Icons.Rounded.Notifications,
                    title = "تنبيه بالرسايل الجديدة",
                    subtitle = "إشعار فوري لما توصل رسالة",
                    checked = state.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications
                )

                Spacer(Modifier.height(26.dp))

                // الحذف التلقائي
                SectionTitle("الحذف التلقائي")
                Spacer(Modifier.height(6.dp))

                Text(
                    text = "الرسايل الأقدم من المدة دي هتتمسح لوحدها",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    autoDeleteOptions.forEach { days ->
                        DayChip(
                            days = days,
                            selected = state.autoDeleteDays == days,
                            onClick = { viewModel.setAutoDeleteDays(days) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlineGlowButton(
                    text = "نضّف الرسايل القديمة دلوقتي",
                    onClick = { viewModel.runCleanupNow() },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.CleaningServices,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(30.dp))

                // تسجيل الخروج
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ErrorRose.copy(alpha = 0.09f))
                        .border(1.dp, ErrorRose.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                        .clickable(enabled = !isSigningOut) { showSignOutConfirm = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ErrorRose.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSigningOut) {
                            CircularProgressIndicator(
                                color = ErrorRose,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Logout,
                                contentDescription = null,
                                tint = ErrorRose,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "تسجيل الخروج",
                            style = MaterialTheme.typography.titleMedium,
                            color = ErrorRose
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "هتقدر ترجع بنفس حساب جوجل",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "MAILIO v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSignOutConfirm) {
        SignOutDialog(
            onDismiss = { showSignOutConfirm = false },
            onConfirm = {
                showSignOutConfirm = false
                viewModel.signOut()
            }
        )
    }
}

@Composable
private fun AccountCard(
    email: String?,
    name: String?,
    photo: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardSurface)
            .background(MailioGradients.cardSheen)
            .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(50))
                .background(MailioGradients.cyanSoft),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(50))
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name ?: "حسابك",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = email ?: "مسجّل بجوجل",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, tint: Color = TextSecondary) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = tint
    )
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurface)
            .background(MailioGradients.cardSheen)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurface)
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyanFaint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanGlow,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepVoid,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = CardSurface,
                uncheckedBorderColor = BorderSubtle
            )
        )
    }
}

@Composable
private fun DayChip(
    days: Long,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) CyanFaint else CardSurface,
        animationSpec = tween(250),
        label = "dayBg"
    )
    val border by animateColorAsState(
        targetValue = if (selected) NeonCyan else BorderSubtle,
        animationSpec = tween(250),
        label = "dayBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) CyanGlow else TextTertiary,
        animationSpec = tween(250),
        label = "dayText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (days <= 0L) "أبداً" else "$days ي",
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
private fun SignOutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "تسجيل الخروج؟",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "هيتمسح كل حاجة من الجهاز ده. عناوينك ورسايلك محفوظة على حسابك، وهترجع أول ما تسجّل دخول تاني.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorRose)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "خروج",
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
