package com.burootro.mailio.ui.screens.inbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInbox
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.domain.model.MailMessage
import com.burootro.mailio.ui.components.EmptyState
import com.burootro.mailio.ui.components.LetterAvatar
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.MxCard
import com.burootro.mailio.ui.components.MxIconButton
import com.burootro.mailio.ui.components.Phrases
import com.burootro.mailio.ui.components.Pill
import com.burootro.mailio.ui.components.SearchBox
import com.burootro.mailio.ui.components.SelectChip
import com.burootro.mailio.ui.components.SoftButton
import com.burootro.mailio.ui.components.SystemActions
import com.burootro.mailio.ui.components.TopBar
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import com.burootro.mailio.util.TimeUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onBack: () -> Unit,
    onMessageClick: (MailMessage) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val feedback = LocalFeedback.current
    var filter by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(event) {
        when (val e = event) {
            is InboxEvent.ShowMessage -> {
                viewModel.consumeEvent()
                when {
                    e.text.contains("مفيش اتصال") -> feedback.error(e.text)
                    e.text.contains("مفيش رسايل جديدة") -> feedback.info(Phrases.noNew(), "📭")
                    e.text.contains("اتحذفت") -> feedback.success(Phrases.deleted(), "🗑️")
                    e.text.contains("اتقرت") -> feedback.success(Phrases.allRead(), "✅")
                    else -> feedback.info(e.text)
                }
            }
            null -> Unit
        }
    }

    val address = state.address
    val shown = state.filteredMessages.filter {
        when (filter) {
            1 -> !it.isRead
            2 -> it.isStarred
            else -> true
        }
    }
    val pullState = rememberPullToRefreshState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
            .statusBarsPadding()
    ) {
        TopBar(
            title = address?.displayName ?: "صندوق الوارد",
            subtitle = when {
                state.messages.isEmpty() -> "مفيش رسائل لسه"
                state.unreadCount == 0 -> "${state.messages.size} رسالة"
                else -> "${state.messages.size} رسالة • ${state.unreadCount} جديدة"
            },
            onBack = onBack
        ) {
            if (address != null) {
                MxIconButton(Icons.Rounded.Send, "مشاركة", onClick = {
                    SystemActions.share(context, address.email, "مشاركة العنوان")
                })
            }
            if (state.unreadCount > 0) {
                MxIconButton(Icons.Rounded.DoneAll, "قراءة الكل", tint = Mx.Royal, onClick = { viewModel.markAllRead() })
            }
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Mx.Surface,
                    color = Mx.Royal
                )
            }
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp)
            ) {
                if (address != null) {
                    item(key = "addressCard") {
                        AddressBanner(address) {
                            SystemActions.copy(context, address.email)
                            feedback.success(Phrases.copied(), "📋")
                        }
                    }
                }
                if (state.messages.size > 2) {
                    item(key = "search") {
                        SearchBox(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchChange,
                            placeholder = "ابحث في الرسائل",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
                if (state.messages.isNotEmpty()) {
                    item(key = "filters") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectChip("الكل", filter == 0, onClick = { filter = 0 }, icon = Icons.Rounded.AllInbox)
                            SelectChip("غير مقروءة", filter == 1, onClick = { filter = 1 }, icon = Icons.Rounded.MarkEmailUnread)
                            SelectChip("مميزة", filter == 2, onClick = { filter = 2 }, icon = Icons.Rounded.StarOutline)
                        }
                    }
                    item(key = "hint") {
                        Text(
                            "اسحب الرسالة شمال للتمييز ⭐ أو يمين للحذف 🗑️",
                            style = MaterialTheme.typography.labelSmall,
                            color = Mx.Text4,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                when {
                    state.messages.isEmpty() && !state.isLoading -> item(key = "empty") {
                        WaitingState()
                    }
                    shown.isEmpty() -> item(key = "noResults") {
                        EmptyState(
                            icon = Icons.Rounded.Inbox,
                            title = "مفيش نتايج",
                            subtitle = "جرّب كلمة تانية أو فلتر تاني"
                        )
                    }
                    else -> items(shown, key = { it.id }) { message ->
                        SwipeableMessage(
                            message = message,
                            onOpen = { onMessageClick(message) },
                            onStar = {
                                viewModel.toggleStar(message)
                                feedback.success(if (message.isStarred) Phrases.unstarred() else Phrases.starred(), "⭐")
                            },
                            onDelete = { viewModel.deleteMessage(message) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableMessage(
    message: MailMessage,
    onOpen: () -> Unit,
    onStar: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Always call the latest callbacks (the state keeps the first lambda forever).
    val currentOnStar by rememberUpdatedState(onStar)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val lastAction = remember { longArrayOf(0L) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // Guard against the callback firing twice for one gesture.
            val now = System.currentTimeMillis()
            val fresh = now - lastAction[0] > 600L
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (fresh) {
                        lastAction[0] = now
                        currentOnStar()
                    }
                    false // snap back — starring doesn't remove the row
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (fresh) {
                        lastAction[0] = now
                        currentOnDelete()
                    }
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isDelete = direction == SwipeToDismissBoxValue.EndToStart
            val active = direction != SwipeToDismissBoxValue.Settled
            val bg by animateColorAsState(
                when {
                    !active -> Color.Transparent
                    isDelete -> Mx.RoseTint
                    else -> Mx.GoldTint
                },
                tween(150),
                label = "swipeBg"
            )
            val iconScale by animateFloatAsState(if (dismissState.progress > 0.35f) 1.2f else 0.9f, label = "swipeIcon")
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (active) {
                    Icon(
                        if (isDelete) Icons.Rounded.DeleteOutline else if (message.isStarred) Icons.Rounded.StarOutline else Icons.Rounded.Star,
                        null,
                        tint = if (isDelete) Mx.Rose else Mx.Gold,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
            }
        }
    ) {
        MessageRow(message = message, onClick = onOpen, onToggleStar = onStar)
    }
}

@Composable
private fun AddressBanner(address: MailAddress, onCopy: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    if (!address.isPermanent) {
        LaunchedEffect(address.id) {
            while (true) {
                delay(1000)
                now = System.currentTimeMillis()
                if (address.isExpired(now)) break
            }
        }
    }
    MxCard(
        modifier = Modifier.padding(top = 4.dp),
        onClick = onCopy,
        padding = PaddingValues(14.dp),
        background = Mx.SurfaceSoft
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LetterAvatar(seed = address.email, letter = address.displayName, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    address.email,
                    style = MonoStyle.copy(color = Mx.Ink, textDirection = TextDirection.Ltr),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                when {
                    address.isPermanent -> Pill("عنوان دائم", Mx.Green, Mx.GreenTint)
                    address.isExpired(now) -> Pill("انتهت صلاحيته", Mx.Text2, Mx.Fill, Icons.Rounded.HourglassEmpty)
                    else -> Pill(
                        "باقي ${TimeUtils.countdown(address.remainingMillis(now) ?: 0L)}",
                        Mx.Amber,
                        Mx.AmberTint,
                        Icons.Rounded.Schedule
                    )
                }
            }
            SoftButton("نسخ", onClick = onCopy, icon = Icons.Rounded.ContentCopy, height = 38.dp)
        }
    }
}

/** Shown while the inbox is empty — a calm "waiting for mail" animation. */
@Composable
private fun WaitingState() {
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "waiting")
    val pulse by t.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(1100),
            androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "waitingPulse"
    )
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                        alpha = 1.4f - pulse
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(Mx.RoyalTint)
            )
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Mx.Signature),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Inbox, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("مستنيين أول رسالة…", style = MaterialTheme.typography.titleLarge, color = Mx.Ink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "استخدم العنوان في أي موقع، والرسالة هتظهر هنا لوحدها",
            style = MaterialTheme.typography.bodyMedium,
            color = Mx.Text2
        )
    }
}
