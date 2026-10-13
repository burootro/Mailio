package com.burootro.mailio.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burootro.mailio.ui.theme.Mx
import kotlinx.coroutines.delay

/**
 * App-wide feedback: haptics on every tap plus a friendly animated toast after every action.
 */
enum class Tone { Success, Info, Warning, Error }

data class ToastMessage(val id: Long, val text: String, val emoji: String, val tone: Tone)

@Stable
class Feedback internal constructor(private val view: View) {

    var current by mutableStateOf<ToastMessage?>(null)
        private set

    /** Light tick for ordinary taps. */
    fun tap() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /** Stronger confirmation for completed actions. */
    fun confirm() {
        val type = if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.VIRTUAL_KEY
        view.performHapticFeedback(type)
    }

    /** Double buzz for errors. */
    fun reject() {
        val type = if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.LONG_PRESS
        view.performHapticFeedback(type)
    }

    fun longPress() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun say(text: String, emoji: String = "✨", tone: Tone = Tone.Success) {
        when (tone) {
            Tone.Error -> reject()
            Tone.Success -> confirm()
            else -> tap()
        }
        current = ToastMessage(System.nanoTime(), text, emoji, tone)
    }

    fun success(text: String, emoji: String = "✨") = say(text, emoji, Tone.Success)
    fun info(text: String, emoji: String = "💡") = say(text, emoji, Tone.Info)
    fun warn(text: String, emoji: String = "⚠️") = say(text, emoji, Tone.Warning)
    fun error(text: String, emoji: String = "😕") = say(text, emoji, Tone.Error)

    internal fun dismiss(id: Long) {
        if (current?.id == id) current = null
    }
}

val LocalFeedback = staticCompositionLocalOf<Feedback> {
    error("Feedback is not provided")
}

/** Friendly copy for every action — picked at random so it never feels robotic. */
object Phrases {
    private fun pick(vararg options: String) = options.random()

    fun copied() = pick("اتنسخ وجاهز تلزقه", "في الحافظة يا معلم", "تمام، اتنسخ", "خلاص اتنسخ")
    fun codeCopied() = pick("الكود اتنسخ، الزقه علطول", "الكود في الحافظة", "الكود جاهز")
    fun created() = pick("عنوانك الجديد جاهز", "إيميل طازة لسه معمول", "اتعمل بنجاح")
    fun deleted() = pick("اتمسح خلاص", "اتشال من عندك", "تم الحذف")
    fun pinned() = pick("اتثبت فوق", "هيفضل في الأول دايماً")
    fun unpinned() = pick("اتلغى التثبيت", "رجع مكانه الطبيعي")
    fun starred() = pick("اتضاف للمميزة", "علّمناها بنجمة")
    fun unstarred() = pick("اتشالت من المميزة", "شيلنا النجمة")
    fun renamed() = pick("الاسم اتغير", "اسم جديد، شكل جديد")
    fun allRead() = pick("كله بقى مقروء", "خلّصت كل الجديد")
    fun refreshed() = pick("كله محدّث", "آخر حاجة عندك")
    fun noNew() = pick("مفيش جديد دلوقتي", "لسه مفيش رسائل جديدة")
    fun shared() = pick("اختار هتبعته فين", "يلا شاركه")
    fun saved() = pick("اتحفظ", "تمام، اتسجل")
}

@Composable
fun ProvideFeedback(content: @Composable BoxScope.() -> Unit) {
    val view = LocalView.current
    val feedback = remember(view) { Feedback(view) }
    CompositionLocalProvider(LocalFeedback provides feedback) {
        Box(Modifier.fillMaxSize()) {
            content()
            ToastHost(feedback)
        }
    }
}

@Composable
private fun BoxScope.ToastHost(feedback: Feedback) {
    val message = feedback.current
    var shown by remember { mutableStateOf<ToastMessage?>(null) }
    if (message != null && shown?.id != message.id) shown = message

    LaunchedEffect(message?.id) {
        val m = message ?: return@LaunchedEffect
        delay(if (m.tone == Tone.Error) 3200L else 2200L)
        feedback.dismiss(m.id)
    }

    AnimatedVisibility(
        visible = message != null,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 8.dp, start = 20.dp, end = 20.dp),
        enter = slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) { -it * 2 } +
            fadeIn() + scaleIn(initialScale = 0.85f),
        exit = slideOutVertically { -it * 2 } + fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        val m = shown ?: return@AnimatedVisibility
        val (accent, tint) = when (m.tone) {
            Tone.Success -> Mx.Green to Mx.GreenTint
            Tone.Info -> Mx.Royal to Mx.RoyalTint
            Tone.Warning -> Mx.Amber to Mx.AmberTint
            Tone.Error -> Mx.Rose to Mx.RoseTint
        }
        Row(
            modifier = Modifier
                .shadow(18.dp, RoundedCornerShape(20.dp), ambientColor = accent.copy(alpha = 0.25f), spotColor = accent.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(20.dp))
                .background(Mx.Surface)
                .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { feedback.dismiss(m.id) }
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(tint),
                contentAlignment = Alignment.Center
            ) {
                Text(m.emoji, fontSize = 15.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                m.text,
                style = MaterialTheme.typography.labelLarge,
                color = Mx.Ink,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Suppress("unused")
private val keepTransparent = Color.Transparent

@Suppress("unused")
private fun fullWidth() = Modifier.fillMaxWidth()
