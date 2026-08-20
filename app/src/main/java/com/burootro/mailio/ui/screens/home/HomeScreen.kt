package com.burootro.mailio.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AllInbox
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.MailAddress
import com.burootro.mailio.ui.components.EmptyState
import com.burootro.mailio.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAddressClick: (MailAddress) -> Unit,
    onAddressCreated: (addressId: String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedForActions by remember { mutableStateOf<MailAddress?>(null) }

    LaunchedEffect(event) {
        when (val e = event) {
            is HomeEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(e.text)
                viewModel.consumeEvent()
            }
            is HomeEvent.AddressCreated -> {
                // نقفل النافذة وندخل الصندوق على طول
                showCreateSheet = false
                viewModel.consumeEvent()
                onAddressCreated(e.addressId)
            }
            null -> Unit
        }
    }

    Scaffold(
        containerColor = DeepVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateFab(
                expanded = showCreateSheet,
                onClick = { showCreateSheet = true }
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(modifier = Modifier.fillMaxSize()) {

                HomeHeader(
                    addressCount = state.addresses.size,
                    unreadCount = state.totalUnread,
                    onSettingsClick = onSettingsClick,
                    modifier = Modifier.padding(top = padding.calculateTopPadding())
                )

                if (state.addresses.isEmpty() && !state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "لسه مفيش عناوين",
                            subtitle = "اضغط على زرار الإضافة تحت عشان تعمل أول إيميل ليك",
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.AllInbox,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 18.dp,
                            end = 18.dp,
                            top = 6.dp,
                            bottom = padding.calculateBottomPadding() + 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.addresses,
                            key = { it.id }
                        ) { address ->
                            AddressCard(
                                address = address,
                                onClick = { onAddressClick(address) },
                                onLongClick = { selectedForActions = address },
                                onCopy = {
                                    copyToClipboard(context, address.email)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("اتنسخ العنوان")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateAddressSheet(
            domains = viewModel.availableDomains,
            isCreating = isCreating,
            onDismiss = { showCreateSheet = false },
            onCreate = { localPart, label, lifetime, domain ->
                viewModel.createAddress(localPart, label, lifetime, domain)
            }
        )
    }

    selectedForActions?.let { address ->
        AddressActionsSheet(
            address = address,
            onDismiss = { selectedForActions = null },
            onTogglePin = {
                viewModel.togglePin(address)
                selectedForActions = null
            },
            onDelete = {
                viewModel.deleteAddress(address)
                selectedForActions = null
            },
            onRename = { newLabel ->
                viewModel.renameAddress(address, newLabel)
                selectedForActions = null
            }
        )
    }
}

@Composable
private fun HomeHeader(
    addressCount: Int,
    unreadCount: Int,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MAILIO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildStatsText(addressCount, unreadCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "الإعدادات",
                    tint = TextSecondary,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

private fun buildStatsText(addresses: Int, unread: Int): String = when {
    addresses == 0 -> "ابدأ بإنشاء أول عنوان"
    unread == 0 -> "$addresses عنوان • كل الرسايل مقروءة"
    else -> "$addresses عنوان • $unread رسالة جديدة"
}

@Composable
private fun CreateFab(
    expanded: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fabRotation"
    )

    val transition = rememberInfiniteTransition(label = "fabGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabGlowAlpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .blur(22.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MailioGradients.primaryDiagonal)
                .scale(glowAlpha + 0.3f)
        )

        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MailioGradients.primaryDiagonal)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "عنوان جديد",
                tint = DeepVoid,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("email", text))
}
