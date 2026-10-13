package com.burootro.mailio.ui.screens.contact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.burootro.mailio.ui.components.IconTile
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.MxCard
import com.burootro.mailio.ui.components.Phrases
import com.burootro.mailio.ui.components.Reveal
import com.burootro.mailio.ui.components.SystemActions
import com.burootro.mailio.ui.components.TopBar
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import kotlinx.coroutines.delay

private const val SUPPORT_EMAIL = "mio3900@outlook.com"

@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val feedback = LocalFeedback.current

    fun openMail(subject: String) {
        if (!SystemActions.mailTo(context, SUPPORT_EMAIL, "Mailio — $subject")) {
            SystemActions.copy(context, SUPPORT_EMAIL)
            feedback.warn("مفيش تطبيق بريد، فنسخنالك الإيميل", "📋")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
            .statusBarsPadding()
    ) {
        TopBar(title = "تواصل معانا", onBack = onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Reveal(0) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Mx.HeroWash)
                        .padding(vertical = 26.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconTile(Icons.Rounded.SupportAgent, size = 60.dp, iconSize = 28.dp, background = Mx.Surface)
                    Spacer(Modifier.height(14.dp))
                    Text("إحنا هنا عشانك", style = MaterialTheme.typography.headlineSmall, color = Mx.Ink)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "أي مشكلة أو فكرة، ابعتلنا وإحنا بنقرا كل رسالة بنفسنا",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Mx.Text2,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Reveal(1) {
                MxCard(padding = PaddingValues(6.dp)) {
                    ReasonRow(Icons.Rounded.BugReport, "الإبلاغ عن مشكلة", "حاجة مش شغالة أو بتقفل", Mx.Rose, Mx.RoseTint) { openMail("الإبلاغ عن مشكلة") }
                    ReasonRow(Icons.Rounded.Lightbulb, "اقتراح ميزة", "عندك فكرة تخلّي التطبيق أحلى", Mx.Amber, Mx.AmberTint) { openMail("اقتراح ميزة") }
                    ReasonRow(Icons.Rounded.HelpOutline, "استفسار", "أي سؤال عن الخدمة", Mx.Royal, Mx.RoyalTint) { openMail("استفسار") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Reveal(2) {
                EmailCard {
                    SystemActions.copy(context, SUPPORT_EMAIL)
                    feedback.success(Phrases.copied(), "📋")
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.Schedule, null, tint = Mx.Text3, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("بنرد عادةً خلال ٢٤ ساعة", style = MaterialTheme.typography.labelMedium, color = Mx.Text3)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ReasonRow(
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
private fun EmailCard(onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    MxCard(
        padding = PaddingValues(16.dp),
        onClick = {
            copied = true
            onCopy()
        }
    ) {
        Text("إيميل الدعم", style = MaterialTheme.typography.labelMedium, color = Mx.Text3)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                SUPPORT_EMAIL,
                style = MonoStyle.copy(color = Mx.Ink, textDirection = TextDirection.Ltr),
                modifier = Modifier.weight(1f)
            )
            AnimatedContent(targetState = copied, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "emailCopy") { done ->
                Icon(
                    if (done) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy,
                    null,
                    tint = if (done) Mx.Green else Mx.Royal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
