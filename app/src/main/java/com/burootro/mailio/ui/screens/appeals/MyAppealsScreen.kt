package com.burootro.mailio.ui.screens.appeals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.Appeal
import com.burootro.mailio.ui.components.EmptyState
import com.burootro.mailio.ui.theme.*
import com.burootro.mailio.util.TimeUtils

@Composable
fun MyAppealsScreen(
    onBack: () -> Unit,
    viewModel: MyAppealsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = DeepVoid) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {

                // الهيدر
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "طلبات المراجعة",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (state.appeals.isEmpty()) "مفيش طلبات"
                            else "${state.appeals.size} طلب",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(CyanFaint)
                            .clickable { viewModel.load() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↻",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyanGlow
                        )
                    }
                }

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonCyan)
                        }
                    }

                    state.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.error ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ErrorRose,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CyanFaint)
                                        .clickable { viewModel.load() }
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "جرب تاني",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CyanGlow
                                    )
                                }
                            }
                        }
                    }

                    state.appeals.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "مفيش طلبات",
                                subtitle = "لو عندك عنوان موقوف، تقدر تطلب مراجعة من الصفحة الرئيسية",
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
                                top = 4.dp,
                                bottom = padding.calculateBottomPadding() + 30.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = state.appeals, key = { it.id }) { appeal ->
                                AppealCard(appeal = appeal)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppealCard(appeal: Appeal) {

    val (statusColor, statusText, statusIcon) = when {
        appeal.isApproved -> Triple(SuccessGreen, "تمت الموافقة", Icons.Rounded.CheckCircle)
        appeal.isRejected -> Triple(ErrorRose, "مرفوض", Icons.Rounded.Cancel)
        else -> Triple(WarningAmber, "قيد المراجعة", Icons.Rounded.HourglassEmpty)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .background(MailioGradients.cardSheen)
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {

        // الحالة
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )

                if (!appeal.addressEmail.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = appeal.addressEmail,
                        style = EmailAddressStyle.copy(
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        ),
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // نص الطلب
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DeepVoid)
                .padding(13.dp)
        ) {
            Text(
                text = "طلبك",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = appeal.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 21.sp
            )
        }

        // رد الإدارة
        if (!appeal.adminReply.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusColor.copy(alpha = 0.09f))
                    .padding(13.dp)
            ) {
                Text(
                    text = "رد الإدارة",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = appeal.adminReply,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 21.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "أُرسل ${TimeUtils.relativeShort(appeal.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )

            if (appeal.repliedAt != null) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "الرد ${TimeUtils.relativeShort(appeal.repliedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )
            }
        }
    }
}
