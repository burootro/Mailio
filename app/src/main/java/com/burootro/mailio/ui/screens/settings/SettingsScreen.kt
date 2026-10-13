package com.burootro.mailio.ui.screens.settings

import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.AutoDelete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.burootro.mailio.ui.components.BrandMark
import com.burootro.mailio.ui.components.ConfirmDialog
import com.burootro.mailio.ui.components.IconTile
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.MxCard
import com.burootro.mailio.ui.components.Phrases
import com.burootro.mailio.ui.components.PrimaryButton
import com.burootro.mailio.ui.components.Reveal
import com.burootro.mailio.ui.components.SectionLabel
import com.burootro.mailio.ui.components.SelectChip
import com.burootro.mailio.ui.components.SoftButton
import com.burootro.mailio.ui.components.SystemActions
import com.burootro.mailio.ui.components.TopBar
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx

private const val PRIVACY_URL = "https://mailio-privacy.pages.dev"
private const val SITE_URL = "https://mailio-app.pages.dev"

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    onAppealsClick: () -> Unit,
    onContactClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSigningOut by viewModel.isSigningOut.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val recoveryKey by viewModel.recoveryKey.collectAsStateWithLifecycle()
    val isGoogle by viewModel.isGoogleAccount.collectAsStateWithLifecycle()
    val backedUp by viewModel.isKeyBackedUp.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val feedback = LocalFeedback.current
    var confirmSignOut by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        when (val e = event) {
            is SettingsEvent.ShowMessage -> {
                viewModel.consumeEvent()
                if (e.text.contains("فشل")) feedback.error(e.text) else feedback.success(e.text, "🧹")
            }
            SettingsEvent.SignedOut -> {
                viewModel.consumeEvent()
                feedback.info("نشوفك على خير", "👋")
                onSignedOut()
            }
            null -> Unit
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
            .statusBarsPadding()
    ) {
        TopBar(title = "الإعدادات", onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Reveal(0) {
                AccountCard(
                    name = state.googleName,
                    email = state.googleEmail,
                    photo = state.googlePhoto,
                    addressCount = state.addressCount,
                    isGoogle = isGoogle
                )
            }

            if (!isGoogle && recoveryKey != null) {
                Spacer(Modifier.height(14.dp))
                Reveal(1) {
                    RecoveryKeyCard(
                        key = recoveryKey.orEmpty(),
                        backedUp = backedUp,
                        onCopy = {
                            SystemActions.copy(context, recoveryKey.orEmpty(), "recovery key")
                            feedback.success("المفتاح اتنسخ — احفظه في مكان أمين", "🔑")
                        },
                        onShare = { SystemActions.share(context, "مفتاح استرجاع Mailio:\n${recoveryKey.orEmpty()}", "احفظ المفتاح") },
                        onConfirmSaved = {
                            viewModel.markKeyBackedUp()
                            feedback.success("تمام، كده حسابك في أمان", "🛡️")
                        }
                    )
                }
            }

            SectionLabel("الإشعارات")
            Reveal(2) {
                MxCard(padding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                    SwitchRow(
                        icon = Icons.Rounded.Notifications,
                        title = "تنبيه بالرسائل الجديدة",
                        subtitle = "إشعار فوري ونسخ الكود من الإشعار",
                        checked = state.notificationsEnabled,
                        onChange = {
                            viewModel.setNotifications(it)
                            if (it) feedback.success("الإشعارات شغالة", "🔔") else feedback.info("الإشعارات اتقفلت", "🔕")
                        }
                    )
                }
            }

            SectionLabel("الحذف التلقائي")
            Reveal(3) {
                MxCard(padding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Rounded.AutoDelete, tint = Mx.Violet, background = Mx.VioletTint)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("امسح الرسائل القديمة لوحدها", style = MaterialTheme.typography.titleSmall, color = Mx.Ink)
                            Text(
                                if (state.autoDeleteDays <= 0) "متوقف — الرسائل مش هتتمسح" else "بعد ${state.autoDeleteDays} يوم من وصولها",
                                style = MaterialTheme.typography.bodySmall,
                                color = Mx.Text3
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1L, 3L, 7L, 14L, 30L, 0L).forEach { days ->
                            SelectChip(
                                text = if (days <= 0L) "أبداً" else "$days يوم",
                                selected = state.autoDeleteDays == days,
                                onClick = { viewModel.setAutoDeleteDays(days) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SoftButton(
                        "نضّف الرسائل القديمة دلوقتي",
                        icon = Icons.Rounded.CleaningServices,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.runCleanupNow() }
                    )
                }
            }

            SectionLabel("الدعم")
            Reveal(4) {
                MxCard(padding = PaddingValues(6.dp)) {
                    NavRow(Icons.Rounded.SupportAgent, "تواصل معانا", "مشكلة أو اقتراح؟ إحنا سامعينك", Mx.Royal, Mx.RoyalTint, onContactClick)
                    NavRow(Icons.Rounded.Gavel, "طلبات المراجعة", "تابع حالة طلباتك وردود الإدارة", Mx.Amber, Mx.AmberTint, onAppealsClick)
                    NavRow(Icons.Rounded.PrivacyTip, "سياسة الخصوصية", "إيه اللي بنحفظه وليه", Mx.Green, Mx.GreenTint) {
                        if (!SystemActions.openUrl(context, PRIVACY_URL)) feedback.error("مفيش متصفح على الجهاز")
                    }
                    NavRow(Icons.Rounded.Send, "شارك التطبيق", "ابعته لصحابك", Mx.Sky, Color(0xFFE6F6FE)) {
                        SystemActions.share(context, "جرّب Mailio — إيميلات مؤقتة في ثانية:\n$SITE_URL", "شارك Mailio")
                        feedback.info(Phrases.shared(), "💌")
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Reveal(5) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Mx.RoseTint)
                        .bounceClick(enabled = !isSigningOut) { confirmSignOut = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = Mx.Rose, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isSigningOut) "بنخرج…" else "تسجيل الخروج",
                        color = Mx.Rose,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                BrandMark(size = 40.dp)
                Spacer(Modifier.height(8.dp))
                Text("Mailio ${appVersion(context)}", style = MaterialTheme.typography.labelMedium, color = Mx.Text3)
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (confirmSignOut) {
        ConfirmDialog(
            icon = Icons.AutoMirrored.Rounded.Logout,
            title = "تسجيل الخروج؟",
            message = if (isGoogle) "كل حاجة هتتمسح من الموبايل ده، وعناوينك هترجع أول ما تدخل بجوجل تاني."
            else "كل حاجة هتتمسح من الموبايل ده. اتأكد إنك حافظ مفتاح الاسترجاع الأول.",
            confirmText = "خروج",
            onConfirm = {
                confirmSignOut = false
                viewModel.signOut()
            },
            onDismiss = { confirmSignOut = false }
        )
    }
}

private fun appVersion(context: Context): String = runCatching {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    info.versionName ?: ""
}.getOrDefault("")

@Composable
private fun AccountCard(
    name: String?,
    email: String?,
    photo: String?,
    addressCount: Int,
    isGoogle: Boolean
) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(20.dp, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.3f), spotColor = Mx.Violet.copy(alpha = 0.3f))
            .clip(shape)
            .background(Mx.Aurora)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        (name ?: "M").take(1).uppercase(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name ?: if (isGoogle) "حسابك" else "حساب ضيف",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    email ?: "محفوظ على الموبايل ده",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccountStat(Icons.Rounded.AlternateEmail, "$addressCount", "عنوان", Modifier.weight(1f))
            AccountStat(
                if (isGoogle) Icons.Rounded.CheckCircle else Icons.Rounded.Shield,
                if (isGoogle) "جوجل" else "ضيف",
                "نوع الحساب",
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AccountStat(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun RecoveryKeyCard(
    key: String,
    backedUp: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onConfirmSaved: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }
    MxCard(
        padding = PaddingValues(16.dp),
        background = if (backedUp) Mx.Surface else Mx.GoldTint,
        borderColor = if (backedUp) Mx.Line else Mx.Gold.copy(alpha = 0.35f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Mx.GoldBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.VpnKey, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("مفتاح الاسترجاع", style = MaterialTheme.typography.titleSmall, color = Mx.Ink)
                Text("هو اللي بيرجّعلك عناوينك لو غيّرت الموبايل", style = MaterialTheme.typography.bodySmall, color = Mx.Text2)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Mx.Surface)
                .border(1.dp, Mx.Line, RoundedCornerShape(14.dp))
                .bounceClick { revealed = !revealed }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = revealed,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "keyReveal",
                modifier = Modifier.weight(1f)
            ) { show ->
                Text(
                    if (show) key else key.map { if (it == '-') '-' else '•' }.joinToString(""),
                    style = MonoStyle.copy(color = Mx.Ink, fontSize = 14.sp, textDirection = TextDirection.Ltr, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                if (revealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                if (revealed) "إخفاء" else "إظهار",
                tint = Mx.Text3,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SoftButton("نسخ", onClick = onCopy, icon = Icons.Rounded.ContentCopy, modifier = Modifier.weight(1f), height = 40.dp)
            SoftButton("حفظ/مشاركة", onClick = onShare, icon = Icons.Rounded.Send, modifier = Modifier.weight(1f), height = 40.dp)
        }
        AnimatedVisibility(!backedUp, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column {
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    "حفظته في مكان أمين ✓",
                    onClick = onConfirmSaved,
                    brush = Mx.GoldBrush,
                    height = 44.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun NavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, tint = tint, background = bg)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Mx.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mx.Text3)
        }
        Icon(Icons.Rounded.ChevronLeft, null, tint = Mx.Text4, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, tint = Mx.Royal, background = Mx.RoyalTint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Mx.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Mx.Text3)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Mx.Royal,
                uncheckedThumbColor = Mx.Text4,
                uncheckedTrackColor = Mx.Fill,
                uncheckedBorderColor = Mx.LineStrong
            )
        )
    }
}

@Suppress("unused")
private val sdk = Build.VERSION.SDK_INT
