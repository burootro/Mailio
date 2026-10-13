package com.burootro.mailio.ui.screens.appeals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.Appeal
import com.burootro.mailio.ui.components.EmptyState
import com.burootro.mailio.ui.components.IconTile
import com.burootro.mailio.ui.components.MxCard
import com.burootro.mailio.ui.components.MxIconButton
import com.burootro.mailio.ui.components.Pill
import com.burootro.mailio.ui.components.Reveal
import com.burootro.mailio.ui.components.Skeleton
import com.burootro.mailio.ui.components.SoftButton
import com.burootro.mailio.ui.components.TopBar
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import com.burootro.mailio.util.TimeUtils

@Composable
fun MyAppealsScreen(
    onBack: () -> Unit,
    viewModel: MyAppealsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
            .statusBarsPadding()
    ) {
        TopBar(
            title = "طلبات المراجعة",
            subtitle = if (state.appeals.isEmpty()) "مفيش طلبات" else "${state.appeals.size} طلب",
            onBack = onBack
        ) {
            MxIconButton(Icons.Rounded.Refresh, "تحديث", onClick = { viewModel.load() })
        }

        when {
            state.isLoading && state.appeals.isEmpty() -> Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { Skeleton(Modifier.fillMaxWidth().height(130.dp)) }
            }

            state.error != null && state.appeals.isEmpty() -> EmptyState(
                icon = Icons.Rounded.CloudOff,
                title = "مقدرناش نجيب طلباتك",
                subtitle = state.error ?: "",
                action = { SoftButton("جرّب تاني", onClick = { viewModel.load() }, icon = Icons.Rounded.Refresh) }
            )

            state.appeals.isEmpty() -> EmptyState(
                icon = Icons.Rounded.Gavel,
                title = "مفيش طلبات",
                subtitle = "لو عندك عنوان موقوف، تقدر تطلب مراجعة من الصفحة الرئيسية"
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.appeals, key = { _, a -> a.id }) { index, appeal ->
                    Reveal(index) { AppealCard(appeal) }
                }
            }
        }
    }
}

private data class Look(val color: Color, val tint: Color, val label: String, val icon: ImageVector)

@Composable
private fun AppealCard(appeal: Appeal) {
    val look = when {
        appeal.isApproved -> Look(Mx.Green, Mx.GreenTint, "اتقبل", Icons.Rounded.CheckCircle)
        appeal.isRejected -> Look(Mx.Rose, Mx.RoseTint, "اترفض", Icons.Rounded.Cancel)
        else -> Look(Mx.Amber, Mx.AmberTint, "بنراجعه", Icons.Rounded.HourglassEmpty)
    }

    MxCard(padding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(look.icon, tint = look.color, background = look.tint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    appeal.addressEmail ?: "طلب عام",
                    style = MonoStyle.copy(color = Mx.Ink, textDirection = TextDirection.Ltr),
                    maxLines = 1
                )
                Text("اتبعت ${TimeUtils.relativeShort(appeal.createdAt)}", style = MaterialTheme.typography.labelSmall, color = Mx.Text3)
            }
            Pill(look.label, look.color, look.tint)
        }
        Spacer(Modifier.height(12.dp))
        Text("طلبك", style = MaterialTheme.typography.labelMedium, color = Mx.Text3)
        Text(appeal.message, style = MaterialTheme.typography.bodyMedium, color = Mx.InkSoft)

        appeal.adminReply?.takeIf { it.isNotBlank() }?.let { reply ->
            Spacer(Modifier.height(12.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(look.tint)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SupportAgent, null, tint = look.color, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("رد الإدارة", style = MaterialTheme.typography.labelMedium, color = look.color)
                    Spacer(Modifier.weight(1f))
                    appeal.repliedAt?.let {
                        Text(TimeUtils.relativeShort(it), style = MaterialTheme.typography.labelSmall, color = Mx.Text3)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(reply, style = MaterialTheme.typography.bodyMedium, color = Mx.Ink)
            }
        }
    }
}
