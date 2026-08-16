package com.burootro.mailio.ui.screens.message

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.domain.model.MailAttachment
import com.burootro.mailio.ui.theme.*
import com.burootro.mailio.util.AvatarColors
import com.burootro.mailio.util.TimeUtils
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    onBack: () -> Unit,
    viewModel: MessageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(event) {
        when (val e = event) {
            is MessageEvent.ShowToast -> {
                snackbarHostState.showSnackbar(e.text)
                viewModel.consumeEvent()
            }
            MessageEvent.MessageDeleted -> {
                viewModel.consumeEvent()
                onBack()
            }
            null -> Unit
        }
    }

    val message = state.message

    Scaffold(
        containerColor = DeepVoid,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (state.isLoading || message == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(MailioGradients.backgroundGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {

                // شريط الأدوات
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBox(
                        icon = Icons.Rounded.ArrowForward,
                        description = "رجوع",
                        onClick = onBack
                    )

                    Spacer(Modifier.weight(1f))

                    IconBox(
                        icon = if (message.isStarred) Icons.Rounded.Star
                        else Icons.Rounded.StarOutline,
                        description = "تمييز",
                        onClick = { viewModel.toggleStar() },
                        tint = if (message.isStarred) WarningAmber else TextSecondary
                    )

                    Spacer(Modifier.width(8.dp))

                    IconBox(
                        icon = Icons.Rounded.MarkEmailUnread,
                        description = "غير مقروءة",
                        onClick = {
                            viewModel.markUnread()
                            onBack()
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    if (message.bodyHtml != null) {
                        IconBox(
                            icon = Icons.Rounded.Code,
                            description = "تبديل العرض",
                            onClick = { viewModel.toggleView() },
                            tint = if (state.showHtml) CyanGlow else TextSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    IconBox(
                        icon = Icons.Rounded.DeleteOutline,
                        description = "حذف",
                        onClick = { viewModel.delete() },
                        tint = ErrorRose
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .padding(bottom = padding.calculateBottomPadding() + 30.dp)
                ) {

                    // العنوان
                    Text(
                        text = message.displaySubject,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(18.dp))

                    // بطاقة المرسل
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardSurface)
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avatarColor = Color(AvatarColors.forSeed(message.fromEmail))

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(avatarColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message.senderInitial,
                                style = MaterialTheme.typography.titleLarge,
                                color = avatarColor
                            )
                        }

                        Spacer(Modifier.width(13.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.senderDisplay,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = message.fromEmail,
                                style = EmailAddressStyle.copy(
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                                ),
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanFaint)
                                .clickable {
                                    copyToClipboard(context, message.fromEmail)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("اتنسخ عنوان المرسل")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "نسخ",
                                tint = CyanGlow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = TimeUtils.fullDateTime(message.receivedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDisabled
                    )

                    Spacer(Modifier.height(22.dp))

                    // المرفقات
                    if (state.attachments.isNotEmpty()) {
                        Text(
                            text = "المرفقات (${state.attachments.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(10.dp))

                        state.attachments.forEach { attachment ->
                            AttachmentRow(attachment = attachment)
                            Spacer(Modifier.height(8.dp))
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                    // المحتوى
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardSurface)
                            .padding(4.dp)
                    ) {
                        val html = message.bodyHtml
                        if (state.showHtml && html != null) {
                            HtmlBody(html = html)
                        } else {
                            Text(
                                text = message.bodyText
                                    ?: message.preview.ifBlank { "(الرسالة فاضية)" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlBody(html: String) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = false
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                setBackgroundColor(AndroidColor.TRANSPARENT)
                isVerticalScrollBarEnabled = false
            }
        },
        update = { webView ->
            val styled = """
                <html dir="auto">
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                    body {
                        background: transparent;
                        color: #94A3B8;
                        font-family: -apple-system, sans-serif;
                        font-size: 15px;
                        line-height: 1.7;
                        padding: 12px;
                        margin: 0;
                        word-wrap: break-word;
                    }
                    a { color: #22D3EE; }
                    img { max-width: 100%; height: auto; border-radius: 8px; }
                    table { max-width: 100%; }
                    pre, code {
                        background: #1A2233;
                        padding: 8px;
                        border-radius: 6px;
                        overflow-x: auto;
                        display: block;
                    }
                </style>
                </head>
                <body>$html</body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL(
                null,
                styled,
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

@Composable
private fun AttachmentRow(attachment: MailAttachment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ElevatedSurface)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(CyanFaint),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = attachment.extension.take(4).ifBlank { "?" },
                style = MaterialTheme.typography.labelSmall,
                color = CyanGlow
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = attachment.readableSize,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun IconBox(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = TextSecondary
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

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("email", text))
}
