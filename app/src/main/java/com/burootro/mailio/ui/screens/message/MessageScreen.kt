package com.burootro.mailio.ui.screens.message

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.VpnKey
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.MailAttachment
import com.burootro.mailio.domain.model.MailMessage
import com.burootro.mailio.ui.components.ConfirmDialog
import com.burootro.mailio.ui.components.IconTile
import com.burootro.mailio.ui.components.LetterAvatar
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.MxCard
import com.burootro.mailio.ui.components.MxIconButton
import com.burootro.mailio.ui.components.Phrases
import com.burootro.mailio.ui.components.Reveal
import com.burootro.mailio.ui.components.Segmented
import com.burootro.mailio.ui.components.Skeleton
import com.burootro.mailio.ui.components.SoftButton
import com.burootro.mailio.ui.components.SystemActions
import com.burootro.mailio.ui.components.TopBar
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import com.burootro.mailio.util.TimeUtils
import kotlinx.coroutines.delay

@Composable
fun MessageScreen(
    onBack: () -> Unit,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val feedback = LocalFeedback.current
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        when (val e = event) {
            is MessageEvent.ShowToast -> {
                viewModel.consumeEvent()
                feedback.info(e.text, "📩")
            }
            MessageEvent.MessageDeleted -> {
                viewModel.consumeEvent()
                feedback.success(Phrases.deleted(), "🗑️")
                onBack()
            }
            null -> Unit
        }
    }

    val message = state.message

    Column(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
            .statusBarsPadding()
    ) {
        TopBar(title = "", onBack = onBack) {
            if (message != null) {
                MxIconButton(
                    if (message.isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    "تمييز",
                    tint = if (message.isStarred) Mx.Gold else Mx.InkSoft,
                    onClick = {
                        viewModel.toggleStar()
                        feedback.success(if (message.isStarred) Phrases.unstarred() else Phrases.starred(), "⭐")
                    }
                )
                MxIconButton(Icons.Rounded.MarkEmailUnread, "غير مقروءة", onClick = { viewModel.markUnread() })
                MxIconButton(Icons.Rounded.Send, "مشاركة", onClick = {
                    SystemActions.share(context, shareText(message), "مشاركة الرسالة")
                })
                MxIconButton(
                    Icons.Rounded.DeleteOutline,
                    "حذف",
                    tint = Mx.Rose,
                    background = Mx.RoseTint,
                    border = Color.Transparent,
                    onClick = { confirmDelete = true }
                )
            }
        }

        if (message == null) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Skeleton(Modifier.fillMaxWidth().height(60.dp))
                Skeleton(Modifier.fillMaxWidth().height(90.dp))
                Skeleton(Modifier.fillMaxWidth().height(260.dp))
            }
        } else {
            MessageBody(message, state, viewModel)
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            icon = Icons.Rounded.DeleteOutline,
            title = "تحذف الرسالة؟",
            message = "هتتمسح من هنا ومن السيرفر، ومش هترجع تاني.",
            confirmText = "احذف",
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun MessageBody(message: MailMessage, state: MessageUiState, viewModel: MessageViewModel) {
    val context = LocalContext.current
    val feedback = LocalFeedback.current

        val code = remember(message.id) { SystemActions.findCode(message.subject, message.preview, message.bodyText) }
        val links = remember(message.id) { SystemActions.findLinks(message.bodyText, message.bodyHtml) }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Reveal(0) {
                Text(
                    message.displaySubject,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Mx.Ink,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            Reveal(1) { SenderCard(message) {
                SystemActions.copy(context, message.fromEmail)
                feedback.success("إيميل المرسل اتنسخ", "📋")
            } }

            if (code != null) {
                Spacer(Modifier.height(12.dp))
                Reveal(2) {
                    CodeCard(code) {
                        SystemActions.copy(context, code, "code")
                        feedback.success(Phrases.codeCopied(), "🔐")
                    }
                }
            }

            if (links.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Reveal(3) {
                    LinksCard(links) { url ->
                        if (!SystemActions.openUrl(context, url)) feedback.error("مفيش متصفح يفتح الرابط")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val hasHtml = !message.bodyHtml.isNullOrBlank()
            if (hasHtml) {
                Segmented(
                    options = listOf("الشكل الأصلي", "نص فقط"),
                    selected = if (state.showHtml) 0 else 1,
                    onSelect = { index -> if ((index == 0) != state.showHtml) viewModel.toggleView() }
                )
                Spacer(Modifier.height(12.dp))
            }

            Reveal(4) {
                MxCard(padding = PaddingValues(if (hasHtml && state.showHtml) 6.dp else 16.dp)) {
                    AnimatedContent(
                        targetState = hasHtml && state.showHtml,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "body"
                    ) { html ->
                        if (html) {
                            HtmlBody(message.bodyHtml.orEmpty()) { url -> SystemActions.openUrl(context, url) }
                        } else {
                            SelectionContainer {
                                Text(
                                    message.bodyText?.takeIf { it.isNotBlank() } ?: message.preview.ifBlank { "(الرسالة فاضية)" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Mx.InkSoft
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoftButton(
                    "نسخ النص كله",
                    icon = Icons.Rounded.ContentCopy,
                    height = 38.dp,
                    onClick = {
                        SystemActions.copy(context, message.bodyText ?: message.preview)
                        feedback.success(Phrases.copied(), "📋")
                    }
                )
            }

            if (state.attachments.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "المرفقات (${state.attachments.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = Mx.Text2,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                state.attachments.forEach { attachment ->
                    AttachmentRow(attachment)
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(30.dp))
        }
}

private fun shareText(message: MailMessage): String = buildString {
    append(message.displaySubject)
    append("\n")
    append("من: ")
    append(message.fromName ?: message.fromEmail)
    append("\n\n")
    append(message.bodyText ?: message.preview)
}

@Composable
private fun SenderCard(message: MailMessage, onCopy: () -> Unit) {
    MxCard(padding = PaddingValues(14.dp), onClick = onCopy) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LetterAvatar(message.fromEmail, message.senderInitial, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(message.senderDisplay, style = MaterialTheme.typography.titleMedium, color = Mx.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    message.fromEmail,
                    style = MonoStyle.copy(color = Mx.Text2, fontWeight = FontWeight.Medium, textDirection = TextDirection.Ltr),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(TimeUtils.relativeShort(message.receivedAt), style = MaterialTheme.typography.labelMedium, color = Mx.Royal)
                Text(TimeUtils.fullDateTime(message.receivedAt), style = MaterialTheme.typography.labelSmall, color = Mx.Text3)
            }
        }
    }
}

@Composable
private fun CodeCard(code: String, onCopy: () -> Unit) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1800)
            copied = false
        }
    }
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, clip = false, ambientColor = Mx.Royal.copy(alpha = 0.3f), spotColor = Mx.Violet.copy(alpha = 0.3f))
            .clip(shape)
            .background(Mx.Signature)
            .bounceClick {
                copied = true
                onCopy()
            }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.VpnKey, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("كود التحقق", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            AnimatedContent(
                targetState = copied,
                transitionSpec = {
                    (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith (scaleOut() + fadeOut())
                },
                label = "codeCopied"
            ) { done ->
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (done) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(if (done) "اتنسخ" else "اضغط للنسخ", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            code,
            color = Color.White,
            style = MonoStyle.copy(fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp, textDirection = TextDirection.Ltr),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LinksCard(links: List<String>, onOpen: (String) -> Unit) {
    MxCard(padding = PaddingValues(12.dp)) {
        Text(
            "روابط في الرسالة",
            style = MaterialTheme.typography.labelLarge,
            color = Mx.Text2,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        links.forEachIndexed { index, url ->
            val host = url.substringAfter("://").substringBefore("/")
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (index == 0) Mx.RoyalTint else Color.Transparent)
                    .bounceClick(pressedScale = 0.98f) { onOpen(url) }
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile(Icons.Rounded.Link, size = 30.dp, iconSize = 15.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(host, style = MaterialTheme.typography.titleSmall, color = Mx.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        url,
                        style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr),
                        color = Mx.Text3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Mx.Royal, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AttachmentRow(attachment: MailAttachment) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Mx.Surface)
            .border(1.dp, Mx.Line, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Mx.AmberTint),
            contentAlignment = Alignment.Center
        ) {
            Text(
                attachment.extension.ifBlank { "FILE" }.take(4),
                color = Mx.Amber,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(attachment.fileName, style = MaterialTheme.typography.titleSmall, color = Mx.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(attachment.readableSize, style = MaterialTheme.typography.labelSmall, color = Mx.Text3)
        }
        Icon(Icons.Rounded.AttachFile, null, tint = Mx.Text3, modifier = Modifier.size(18.dp))
    }
}

/** Light-styled HTML view. JavaScript stays off; links open in the browser. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlBody(html: String, onLink: (String) -> Unit) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = false
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = false
                setBackgroundColor(AndroidColor.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        request?.url?.toString()?.let(onLink)
                        return true
                    }
                }
            }
        },
        update = { webView ->
            val page = """
                <html dir="auto">
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                  body { background: transparent; color: #2A3045; font-family: sans-serif;
                         font-size: 15px; line-height: 1.7; padding: 10px; margin: 0; word-wrap: break-word; }
                  a { color: #4F46E5; font-weight: 600; }
                  img { max-width: 100%; height: auto; border-radius: 10px; }
                  table { max-width: 100% !important; }
                  pre, code { background: #EFF1F8; padding: 8px; border-radius: 8px; overflow-x: auto; display: block; }
                </style>
                </head>
                <body>$html</body>
                </html>
            """.trimIndent()
            if (webView.tag != html.hashCode()) {
                webView.tag = html.hashCode()
                webView.loadDataWithBaseURL(null, page, "text/html", "UTF-8", null)
            }
        }
    )
}
