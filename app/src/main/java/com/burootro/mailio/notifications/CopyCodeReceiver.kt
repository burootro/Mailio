package com.burootro.mailio.notifications

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

/**
 * بينسخ كود التحقق من الإشعار من غير ما تفتح التطبيق
 */
class CopyCodeReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COPY = "com.burootro.mailio.COPY_CODE"
        const val EXTRA_CODE = "extra_code"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COPY) return

        val code = intent.getStringExtra(EXTRA_CODE) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("code", code))

        // أندرويد 13+ بيعرض تأكيد النسخ لوحده
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "اتنسخ الكود: $code", Toast.LENGTH_SHORT).show()
        }

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}
