package com.burootro.mailio.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AllInbox
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.domain.model.MailMessage
import com.burootro.mailio.ui.components.EmptyState
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.MxIconButton
import com.burootro.mailio.ui.components.Phrases
import com.burootro.mailio.ui.components.SearchBox
import com.burootro.mailio.ui.components.Segmented
import com.burootro.mailio.ui.components.SelectChip
import com.burootro.mailio.ui.components.SoftButton
import com.burootro.mailio.ui.components.SystemActions
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.screens.inbox.MessageRow
import com.burootro.mailio.ui.theme.Mx
import java.util.Calendar

private enum class AddressFilter(val label: String) {
    All("الكل"), Unread("فيها جديد"), Pinned("مثبّتة"), Temporary("مؤقتة")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddressClick: (MailAddress) -> Unit,
    onAddressCreated: (addressId: String) -> Unit,
    onMessageClick: (MailMessage) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val transferState by viewModel.transferState.collectAsStateWithLifecycle()
    val claimState by viewModel.claimState.collectAsStateWithLifecycle()
    val appealState by viewModel.appealState.collectAsStateWithLifecycle()
    val recent by viewModel.recentMessages.collectAsStateWithLifecycle()
    val starred by viewModel.starredMessages.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val needsBackup by viewModel.needsKeyBackup.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val feedback = LocalFeedback.current

    var tab by rememberSaveable { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf(AddressFilter.All) }
    var query by rememberSaveable { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var actionsFor by remember { mutableStateOf<MailAddress?>(null) }

    // Ask for notification permission once on Android 13+.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) feedback.success("هنبلّغك أول ما توصلك رسالة", "🔔")
    }
    var askedNotifications by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!askedNotifications && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askedNotifications = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(event) {
        when (val e = event) {
            is HomeEvent.ShowMessage -> {
                viewModel.consumeEvent()
                val isError = e.text.contains("خطأ") || e.text.contains("فشل") || e.text.contains("مفيش اتصال") || e.text.contains("بطيء")
                if (isError) feedback.error(e.text) else feedback.info(e.text, "📬")
            }
            is HomeEvent.AddressCreated -> {
                showCreate = false
                viewModel.consumeEvent()
                SystemActions.copy(context, e.email)
                feedback.success(Phrases.created() + " واتنسخ كمان", "🎉")
                onAddressCreated(e.addressId)
            }
            is HomeEvent.AddressClaimed -> {
                viewModel.consumeEvent()
                feedback.success("العنوان بقى بتاعك: ${e.email}", "🤝")
                onAddressCreated(e.addressId)
            }
            null -> Unit
        }
    }

    val shownAddresses by remember(state.addresses, filter, query) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            state.addresses
                .filter {
                    when (filter) {
                        AddressFilter.All -> true
                        AddressFilter.Unread -> it.unreadCount > 0
                        AddressFilter.Pinned -> it.isPinned
                        AddressFilter.Temporary -> !it.isPermanent && !it.isExpired(now)
                    }
                }
                .filter {
                    query.isBlank() ||
                        it.email.contains(query.trim(), ignoreCase = true) ||
                        (it.label?.contains(query.trim(), ignoreCase = true) == true)
                }
        }
    }
    val addressNames = remember(state.addresses) { state.addresses.associate { it.id to it.displayName } }

    val listState = rememberLazyListState()
    val fabVisible by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }
    val pullState = rememberPullToRefreshState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Mx.HeroWash)
        )

        PullToRefreshBox(
            isRefreshing = state.isSyncing,
            onRefresh = { viewModel.refresh() },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = state.isSyncing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding(),
                    containerColor = Mx.Surface,
                    color = Mx.Royal
                )
            }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)
            ) {
                item(key = "header") {
                    Header(
                        name = userName,
                        onClaim = { viewModel.openClaim() },
                        onSettings = onSettingsClick
                    )
                }
                item(key = "hero") {
                    HeroCard(
                        addresses = state.addresses.size,
                        unread = state.totalUnread,
                        starred = starred.size,
                        connected = state.isConnected,
                        creating = isCreating,
                        onQuickCreate = { viewModel.quickCreate() },
                        onCustomCreate = { showCreate = true }
                    )
                }
                if (needsBackup) {
                    item(key = "backup") {
                        BackupBanner(onClick = onSettingsClick, modifier = Modifier.animateItem())
                    }
                }
                item(key = "tabs") {
                    Segmented(
                        options = listOf("العناوين", "الرسائل", "المميزة"),
                        selected = tab,
                        onSelect = { tab = it },
                        badges = listOf(0, state.totalUnread, 0),
                        modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                    )
                }

                when (tab) {
                    0 -> {
                        if (state.addresses.size > 3) {
                            item(key = "search") {
                                SearchBox(
                                    value = query,
                                    onValueChange = { query = it },
                                    placeholder = "دوّر في عناوينك",
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        if (state.addresses.isNotEmpty()) {
                            item(key = "filters") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AddressFilter.entries.forEach { f ->
                                        SelectChip(
                                            text = f.label,
                                            selected = filter == f,
                                            icon = when (f) {
                                                AddressFilter.All -> Icons.Rounded.AllInbox
                                                AddressFilter.Unread -> Icons.Rounded.MarkEmailUnread
                                                AddressFilter.Pinned -> Icons.Rounded.PushPin
                                                AddressFilter.Temporary -> Icons.Rounded.Schedule
                                            },
                                            onClick = { filter = f }
                                        )
                                    }
                                }
                            }
                        }
                        if (state.addresses.isEmpty() && !state.isLoading) {
                            item(key = "emptyAddresses") {
                                EmptyState(
                                    icon = Icons.Rounded.AlternateEmail,
                                    title = "يلا نعمل أول عنوان",
                                    subtitle = "اضغط على «إيميل جديد» فوق وهيبقى جاهز في ثانية",
                                    action = {
                                        SoftButton("إيميل جديد", onClick = { viewModel.quickCreate() }, icon = Icons.Rounded.Bolt)
                                    }
                                )
                            }
                        } else if (shownAddresses.isEmpty() && state.addresses.isNotEmpty()) {
                            item(key = "emptyFilter") {
                                EmptyState(
                                    icon = Icons.Rounded.Tune,
                                    title = "مفيش حاجة هنا",
                                    subtitle = "جرّب فلتر تاني أو امسح البحث"
                                )
                            }
                        }
                        items(shownAddresses, key = { "a_" + it.id }) { address ->
                            AddressCard(
                                address = address,
                                onClick = { onAddressClick(address) },
                                onLongClick = { actionsFor = address },
                                onCopy = {
                                    SystemActions.copy(context, address.email)
                                    feedback.success(Phrases.copied(), "📋")
                                },
                                onAppeal = { viewModel.openAppeal(address) },
                                modifier = Modifier
                                    .animateItem()
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }

                    1 -> {
                        if (recent.isEmpty()) {
                            item(key = "emptyMessages") {
                                EmptyState(
                                    icon = Icons.Rounded.Inbox,
                                    title = "لسه مفيش رسائل",
                                    subtitle = "أي رسالة توصل على أي عنوان من عناوينك هتظهر هنا"
                                )
                            }
                        } else {
                            if (state.totalUnread > 0) {
                                item(key = "markAll") {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        SoftButton(
                                            "علّم الكل مقروء",
                                            onClick = {
                                                viewModel.markAllMessagesRead()
                                                feedback.success(Phrases.allRead(), "✅")
                                            },
                                            icon = Icons.Rounded.DoneAll,
                                            height = 36.dp
                                        )
                                    }
                                }
                            }
                            items(recent, key = { "m_" + it.id }) { message ->
                                MessageRow(
                                    message = message,
                                    addressLabel = addressNames[message.addressId],
                                    onClick = { onMessageClick(message) },
                                    onToggleStar = {
                                        viewModel.toggleStar(message)
                                        feedback.success(if (message.isStarred) Phrases.unstarred() else Phrases.starred(), "⭐")
                                    },
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(vertical = 5.dp)
                                )
                            }
                        }
                    }

                    else -> {
                        if (starred.isEmpty()) {
                            item(key = "emptyStarred") {
                                EmptyState(
                                    icon = Icons.Rounded.StarOutline,
                                    title = "مفيش رسائل مميزة",
                                    subtitle = "دوس على النجمة جنب أي رسالة عشان تحفظها هنا"
                                )
                            }
                        } else {
                            items(starred, key = { "s_" + it.id }) { message ->
                                MessageRow(
                                    message = message,
                                    addressLabel = addressNames[message.addressId],
                                    onClick = { onMessageClick(message) },
                                    onToggleStar = {
                                        viewModel.toggleStar(message)
                                        feedback.success(Phrases.unstarred(), "⭐")
                                    },
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating create button — appears once the hero scrolls away.
        AnimatedVisibility(
            visible = fabVisible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Box(
                Modifier
                    .size(58.dp)
                    .shadow(18.dp, RoundedCornerShape(20.dp), clip = false, ambientColor = Mx.Royal.copy(alpha = 0.5f), spotColor = Mx.Violet.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Mx.Signature)
                    .bounceClick(pressedScale = 0.88f) { showCreate = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, "عنوان جديد", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    }

    // ---------------- Sheets ----------------

    if (showCreate) {
        CreateAddressSheet(
            domains = viewModel.availableDomains,
            isCreating = isCreating,
            onDismiss = { showCreate = false },
            onCreate = { localPart, label, lifetime, domain ->
                viewModel.createAddress(localPart, label, lifetime, domain)
            }
        )
    }

    actionsFor?.let { address ->
        AddressActionsSheet(
            address = address,
            onDismiss = { actionsFor = null },
            onCopy = {
                SystemActions.copy(context, address.email)
                feedback.success(Phrases.copied(), "📋")
                actionsFor = null
            },
            onShare = {
                actionsFor = null
                SystemActions.share(context, address.email, "مشاركة العنوان")
            },
            onTogglePin = {
                viewModel.togglePin(address)
                feedback.success(if (address.isPinned) Phrases.unpinned() else Phrases.pinned(), "📌")
                actionsFor = null
            },
            onRename = { newLabel ->
                viewModel.renameAddress(address, newLabel)
                feedback.success(Phrases.renamed(), "✏️")
                actionsFor = null
            },
            onTransfer = {
                actionsFor = null
                viewModel.openTransfer(address)
            },
            onDelete = {
                viewModel.deleteAddress(address)
                actionsFor = null
            }
        )
    }

    transferState?.let { transfer ->
        TransferSheet(
            email = transfer.email,
            code = transfer.code,
            isLoading = transfer.isLoading,
            expiresAt = transfer.expiresAt,
            onDismiss = { viewModel.closeTransfer() },
            onGenerate = { viewModel.generateTransferCode() },
            onCopy = { code ->
                SystemActions.copy(context, code)
                feedback.success(Phrases.codeCopied(), "🔐")
            },
            onCancel = { viewModel.cancelTransfer() }
        )
    }

    claimState?.let { claim ->
        ClaimTransferSheet(
            isLoading = claim.isLoading,
            error = claim.error,
            onDismiss = { viewModel.closeClaim() },
            onClaim = { code -> viewModel.claimAddress(code) },
            onClearError = { viewModel.clearClaimError() }
        )
    }

    appealState?.let { appeal ->
        AppealSheet(
            email = appeal.email,
            blockedReason = appeal.blockedReason,
            isLoading = appeal.isLoading,
            error = appeal.error,
            isSubmitted = appeal.isSubmitted,
            onDismiss = { viewModel.closeAppeal() },
            onSubmit = { message -> viewModel.submitAppeal(message) },
            onClearError = { viewModel.clearAppealError() }
        )
    }
}

// ============================================================
// Header + hero
// ============================================================

private fun greeting(): Pair<String, String> {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "صباح الفل" to "☀️"
        in 12..16 -> "يومك سعيد" to "🌤️"
        in 17..20 -> "مساء الخير" to "🌇"
        else -> "سهرانين؟" to "🌙"
    }
}

@Composable
private fun Header(name: String?, onClaim: () -> Unit, onSettings: () -> Unit) {
    val (hello, emoji) = remember { greeting() }
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 14.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "$hello $emoji",
                style = MaterialTheme.typography.bodyMedium,
                color = Mx.Text2
            )
            Text(
                name?.substringBefore(" ")?.let { "أهلاً يا $it" } ?: "Mailio",
                style = MaterialTheme.typography.headlineMedium,
                color = Mx.Ink
            )
        }
        MxIconButton(Icons.Rounded.SwapHoriz, "استلام عنوان", onClick = onClaim, tint = Mx.Royal)
        Spacer(Modifier.width(8.dp))
        MxIconButton(Icons.Rounded.Settings, "الإعدادات", onClick = onSettings)
    }
}

@Composable
private fun HeroCard(
    addresses: Int,
    unread: Int,
    starred: Int,
    connected: Boolean,
    creating: Boolean,
    onQuickCreate: () -> Unit,
    onCustomCreate: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val boltSpin by animateFloatAsState(
        targetValue = if (creating) 360f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "bolt"
    )
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(24.dp, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.35f), spotColor = Mx.Violet.copy(alpha = 0.35f))
            .clip(shape)
            .background(Mx.Aurora)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "صندوقك الخاص",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            AnimatedVisibility(!connected, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CloudOff, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("مفيش اتصال", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            HeroStat(addresses, "عنوان", Modifier.weight(1f))
            HeroStat(unread, "رسالة جديدة", Modifier.weight(1f))
            HeroStat(starred, "مميزة", Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .bounceClick(enabled = !creating, onClick = onQuickCreate),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Bolt,
                    null,
                    tint = Mx.Royal,
                    modifier = Modifier
                        .size(19.dp)
                        .graphicsLayer { rotationZ = boltSpin }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (creating) "بنجهزه…" else "إيميل جديد",
                    color = Mx.RoyalDeep,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .bounceClick(onClick = onCustomCreate),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Tune, "تخصيص", tint = Color.White, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun HeroStat(value: Int, label: String, modifier: Modifier) {
    Column(modifier) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                } else {
                    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                }
            },
            label = "heroStat"
        ) { v ->
            Text("$v", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
        Text(label, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun BackupBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Mx.GoldTint)
            .border(1.dp, Mx.Gold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Mx.GoldBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.VpnKey, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("احفظ مفتاح الاسترجاع", style = MaterialTheme.typography.titleSmall, color = Mx.Ink)
            Text("لو غيّرت الموبايل، المفتاح بيرجّعلك كل عناوينك", style = MaterialTheme.typography.bodySmall, color = Mx.Text2)
        }
    }
}
