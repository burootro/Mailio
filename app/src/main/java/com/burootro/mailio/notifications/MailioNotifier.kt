package com.burootro.mailio.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.burootro.mailio.MainActivity
import com.burootro.mailio.data.repository.NewMessageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MailioNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "mailio_messages"
        const val CHANNEL_NAME = "الرسايل الجديدة"

        /**
         * استخراج كود التحقق من نص الرسالة
         * بيدور على 4-8 أرقام متتالية
         */
        fun extractCode(vararg sources: String?): String? {
            val patterns = listOf(
                Regex("""\b(\d{6})\b"""),
                Regex("""\b(\d{8})\b"""),
                Regex("""\b(\d{5})\b"""),
                Regex("""\b(\d{4})\b"""),
                Regex("""\b([A-Z0-9]{6,8})\b""")
            )

            sources.filterNotNull().forEach { text ->
                patterns.forEach { pattern ->
                    val match = pattern.find(text)
                    if (match != null) {
                        val code = match.groupValues[1]
                        // نستبعد السنين والأرقام العادية
                        if (!code.startsWith("19") && !code.startsWith("20")) {
                            return code
                        }
                    }
                }
            }
            return null
        }
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه لما توصل رسالة جديدة"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun notifyNewMessage(message: NewMessageInfo) {
        if (!hasPermission()) return

        val code = extractCode(message.subject, message.preview)

        // فتح التطبيق على صندوق الوارد
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("addressId", message.addressId)
            putExtra("messageId", message.id)
        }

        val openPending = PendingIntent.getActivity(
            context,
            message.id.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(message.fromName)
            .setContentText(
                if (code != null) "كود التحقق: $code" else message.subject
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    buildString {
                        if (code != null) {
                            append("الكود: $code\n\n")
                        }
                        if (message.subject.isNotBlank()) {
                            append(message.subject)
                            append("\n")
                        }
                        append(message.preview)
                    }
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EMAIL)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // زرار نسخ الكود — بيظهر بس لو فيه كود
        if (code != null) {
            val copyIntent = Intent(context, CopyCodeReceiver::class.java).apply {
                action = CopyCodeReceiver.ACTION_COPY
                putExtra(CopyCodeReceiver.EXTRA_CODE, code)
                putExtra(CopyCodeReceiver.EXTRA_NOTIFICATION_ID, message.id.hashCode())
            }

            val copyPending = PendingIntent.getBroadcast(
                context,
                message.id.hashCode() + 1,
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                android.R.drawable.ic_menu_edit,
                "نسخ الكود",
                copyPending
            )
        }

        try {
            NotificationManagerCompat.from(context)
                .notify(message.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // مفيش صلاحية
        }
    }
}
