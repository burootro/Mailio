package com.burootro.mailio.notifications

import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.remote.MailioApi
import com.burootro.mailio.data.remote.dto.PushTokenRequest
import com.burootro.mailio.data.repository.NewMessageInfo
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MailioFirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var notifier: MailioNotifier

    @Inject
    lateinit var api: MailioApi

    @Inject
    lateinit var prefs: MailioPreferences

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * بيتنادى لما التوكن يتولّد أو يتجدد
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        scope.launch {
            try {
                prefs.setPushToken(token)

                if (prefs.getRecoveryKey() != null) {
                    api.registerPushToken(PushTokenRequest(token))
                }
            } catch (e: Exception) {
                // هيتسجل تاني أول ما التطبيق يفتح
            }
        }
    }

    /**
     * بيتنادى لما إشعار يوصل
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data

        val messageId = data["messageId"] ?: return
        val addressId = data["addressId"] ?: return
        val fromName = data["fromName"] ?: "رسالة جديدة"
        val subject = data["subject"] ?: ""
        val preview = data["preview"] ?: ""

        scope.launch {
            val notificationsOn = try {
                prefs.notificationsEnabled.first()
            } catch (e: Exception) {
                true
            }

            if (!notificationsOn) return@launch

            notifier.notifyNewMessage(
                NewMessageInfo(
                    id = messageId,
                    addressId = addressId,
                    fromName = fromName,
                    subject = subject,
                    preview = preview
                )
            )
        }
    }
}
