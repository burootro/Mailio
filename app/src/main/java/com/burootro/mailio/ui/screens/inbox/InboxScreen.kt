package com.burootro.mailio.ui.screens.inbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.MailMessage
import com.burootro.mailio.ui.components.EmptyState
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(
    onBack: () -> Unit,
    onMessageClick: (MailMessage) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        when (val e = event) {
            is InboxEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(e.text)
                viewModel.consumeEvent()
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
                    .height(300.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(modifier = Modifier.fillMaxSize()) {

                InboxHeader(
                    title = state.address?.displayName ?: "صندوق الوارد",
                    email = state.address?.email ?: "",
                    messageCount = state.messages.size,
                    unreadCount = state.unreadCount,
                    onBack = onBack,
                    onSearchToggle = {
                        showSearch = !showSearch
                        if (!showSearch) viewModel.clearSearch()
                    },
                    onMarkAllRead = { viewModel.markAllRead() },
                    onCopyEmail = {
                        state.address?.let {
                            copyToClipboard(context, it.email)
                            scope.launch {
                                snackbarHostState.showSnackbar("اتنسخ العنوان")
                            }
                        }
                    },
                    modifier = Modifier.padding(top = padding.calculateTopPadding())
                )

                AnimatedVisibility(
                    visible = showSearch,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = viewModel::onSearchChange,
                        onClear = viewModel::clearSearch
                    )
                }

                val messages = state.filteredMessages

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonCyan)
                        }
                    }

                    messages.isEmpty() && state.searchQuery.isNotBlank() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "مفيش نتايج",
                                subtitle = "جرب كلمة بحث تانية",
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = CyanGlow,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            )
                        }
                    }

                    messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "الصندوق فاضي",
                                subtitle = "لسه مفيش رسايل وصلت على العنوان ده",
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Inbox,
                                        contentDescription = null,
                                        tint = CyanGlow,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 6.dp,
                                bottom = padding.calculateBottomPadding() + 28.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = messages,
                                key = { it.id }
                            ) { message ->
                                MessageCard(
                                    message = message,
                                    onClick = { onMessageClick(message) },
                                    onLongClick = { viewModel.deleteMessage(message) },
                                    onStarClick = { viewModel.toggleStar(message) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxHeader(
    title: String,
    email: String,
    messageCount: Int,
    unreadCount: Int,
    onBack: () -> Unit,
    onSearchToggle: () -> Unit,
    onMarkAllRead: () -> Unit,
    onCopyEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            IconBox(
                icon = Icons.Rounded.ArrowForward,
                description = "رجوع",
                onClick = onBack
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = when {
                        messageCount == 0 -> "مفيش رسايل"
                        unreadCount == 0 -> "$messageCount رسالة"
                        else -> "$messageCount رسالة • $unreadCount جديدة"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            IconBox(
                icon = Icons.Rounded.Search,
                description = "بحث",
                onClick = onSearchToggle
            )

            Spacer(Modifier.width(8.dp))

            IconBox(
                icon = Icons.Rounded.DoneAll,
                description = "قراءة الكل",
                onClick = onMarkAllRead,
                tint = if (unreadCount > 0) CyanGlow else TextDisabled
            )
        }

        Spacer(Modifier.height(14.dp))

        // شريط العنوان
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyanFaint)
                .clickable(onClick = onCopyEmail)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = email,
                style = EmailAddressStyle.copy(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                ),
                color = CyanGlow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "نسخ",
                tint = CyanGlow,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun IconBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = TextSecondary
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(CardSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        placeholder = {
            Text(
                text = "ابحث في الرسايل",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDisabled
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "مسح",
                        tint = TextTertiary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
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
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("email", text))
}
