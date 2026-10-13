package com.burootro.mailio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.components.ProvideFeedback
import com.burootro.mailio.ui.navigation.MailioNavigation
import com.burootro.mailio.ui.theme.MailioTheme
import com.burootro.mailio.ui.theme.Mx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

/** Where a notification tap wants to go. */
data class DeepLink(val addressId: String?, val messageId: String?)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deepLink = MutableStateFlow<DeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) readDeepLink(intent)

        setContent {
            MailioTheme {
                val link by deepLink.collectAsStateWithLifecycle()
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Mx.Canvas)
                ) {
                    ProvideFeedback {
                        MailioNavigation(
                            deepLink = link,
                            onDeepLinkHandled = { deepLink.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readDeepLink(intent)
    }

    private fun readDeepLink(intent: Intent?) {
        val addressId = intent?.getStringExtra("addressId")
        val messageId = intent?.getStringExtra("messageId")
        if (addressId != null || messageId != null) {
            deepLink.value = DeepLink(addressId, messageId)
        }
    }
}
