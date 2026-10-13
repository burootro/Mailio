package com.burootro.mailio.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.burootro.mailio.notifications.MailioNotifier

/** Small helpers for clipboard, sharing and links. */
object SystemActions {

    fun copy(context: Context, text: String, label: String = "mailio") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun share(context: Context, text: String, title: String = "مشاركة") {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(send, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }

    fun openUrl(context: Context, url: String): Boolean = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }.isSuccess

    fun mailTo(context: Context, email: String, subject: String): Boolean = runCatching {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.isSuccess

    /** Verification code (OTP) inside a message, if any. */
    fun findCode(vararg sources: String?): String? = MailioNotifier.extractCode(*sources)

    private val urlRegex = Regex("""https?://[^\s<>"')\]]+""", RegexOption.IGNORE_CASE)

    /** Unique links found in the message text/html, most useful first (verification links). */
    fun findLinks(text: String?, html: String?): List<String> {
        val raw = buildList<String> {
            text?.let { addAll(urlRegex.findAll(it).map { m -> m.value }) }
            html?.let { h ->
                addAll(Regex("""href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                    .findAll(h).map { m -> m.groupValues[1] }
                    .filter { it.startsWith("http", ignoreCase = true) })
            }
        }
        val cleaned = raw
            .map { it.trimEnd('.', ',', ';', ':').replace("&amp;", "&") }
            .filter { !it.contains("unsubscribe", ignoreCase = true) }
            .distinct()
        val keywords = listOf("verify", "confirm", "activate", "login", "signin", "reset", "token", "auth", "code")
        return cleaned.sortedByDescending { link -> keywords.count { link.contains(it, ignoreCase = true) } }.take(6)
    }
}
