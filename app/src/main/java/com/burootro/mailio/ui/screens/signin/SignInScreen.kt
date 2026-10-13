package com.burootro.mailio.ui.screens.signin

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burootro.mailio.ui.components.BrandMark
import com.burootro.mailio.ui.components.IconTile
import com.burootro.mailio.ui.components.LocalFeedback
import com.burootro.mailio.ui.components.MxSheet
import com.burootro.mailio.ui.components.PrimaryButton
import com.burootro.mailio.ui.components.SheetHeader
import com.burootro.mailio.ui.components.bounceClick
import com.burootro.mailio.ui.components.mxFieldColors
import com.burootro.mailio.ui.theme.MonoStyle
import com.burootro.mailio.ui.theme.Mx
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val feedback = LocalFeedback.current
    var showRestore by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onSignInResult(result.data)
        else viewModel.onSignInCancelled()
    }

    LaunchedEffect(event) {
        when (val e = event) {
            is SignInEvent.Success -> {
                viewModel.consumeEvent()
                val name = e.name?.substringBefore(" ")
                feedback.success(if (name != null) "أهلاً يا $name" else "أهلاً بيك في Mailio", "👋")
                onSignedIn()
            }
            SignInEvent.GuestReady -> {
                viewModel.consumeEvent()
                feedback.success("يلا بينا، كله جاهز", "🚀")
                onSignedIn()
            }
            is SignInEvent.Restored -> {
                viewModel.consumeEvent()
                showRestore = false
                feedback.success("رجعنالك ${e.addresses} عنوان", "🔑")
                onSignedIn()
            }
            null -> Unit
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            feedback.error(it)
            viewModel.clearError()
        }
    }

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Mx.Canvas)
    ) {
        OrbitBackdrop(Modifier.fillMaxWidth().height(430.dp))

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(70.dp))
            BrandMark(
                size = 84.dp,
                modifier = Modifier.graphicsLayer {
                    val s = 0.7f + 0.3f * entrance.value
                    scaleX = s
                    scaleY = s
                    alpha = entrance.value
                }
            )
            Spacer(Modifier.height(22.dp))
            Text(
                "إيميل مؤقت في ثانية",
                style = MaterialTheme.typography.displaySmall,
                color = Mx.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = entrance.value
                    translationY = (1f - entrance.value) * 30f
                }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "عناوين نضيفة، رسائل فورية، وخصوصيتك في أمان",
                style = MaterialTheme.typography.bodyLarge,
                color = Mx.Text2,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = entrance.value }
            )

            Spacer(Modifier.height(30.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = entrance.value
                        translationY = (1f - entrance.value) * 60f
                    }
                    .shadow(18.dp, RoundedCornerShape(26.dp), clip = false, ambientColor = Mx.Royal.copy(alpha = 0.12f), spotColor = Mx.Royal.copy(alpha = 0.12f))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Mx.Surface)
                    .border(1.dp, Mx.Line, RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Feature(Icons.Rounded.Bolt, Mx.Royal, Mx.RoyalTint, "عنوان جديد بضغطة واحدة")
                Feature(Icons.Rounded.Notifications, Mx.Violet, Mx.VioletTint, "إشعار فوري ونسخ الكود من الإشعار")
                Feature(Icons.Rounded.Autorenew, Mx.Sky, Color(0xFFE6F6FE), "حسابك محفوظ على كل أجهزتك")
                Feature(Icons.Rounded.Shield, Mx.Green, Mx.GreenTint, "من غير إعلانات ولا تتبّع")
            }

            Spacer(Modifier.height(26.dp))

            GoogleButton(
                loading = state.isLoading,
                enabled = !state.isBusy,
                onClick = { launcher.launch(viewModel.getSignInIntent()) }
            )

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallChoice(
                    icon = Icons.Rounded.PersonOutline,
                    text = "من غير تسجيل",
                    loading = state.isSkipping,
                    enabled = !state.isBusy,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.continueAsGuest() }
                )
                SmallChoice(
                    icon = Icons.Rounded.VpnKey,
                    text = "عندي مفتاح",
                    loading = false,
                    enabled = !state.isBusy,
                    modifier = Modifier.weight(1f),
                    onClick = { showRestore = true }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "بدون تسجيل؟ عناوينك بتتحفظ على الموبايل ده، ومعاك مفتاح ترجّعها بيه في أي وقت",
                style = MaterialTheme.typography.bodySmall,
                color = Mx.Text3,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showRestore) {
        RestoreSheet(
            loading = state.isRestoring,
            error = state.restoreError,
            onDismiss = {
                showRestore = false
                viewModel.clearRestoreError()
            },
            onRestore = { viewModel.restoreWithKey(it) },
            onTyping = { viewModel.clearRestoreError() }
        )
    }
}

@Composable
private fun Feature(icon: ImageVector, tint: Color, bg: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint = tint, background = bg, size = 34.dp, iconSize = 17.dp)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, color = Mx.InkSoft)
    }
}

@Composable
private fun GoogleButton(loading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp), clip = false, ambientColor = Mx.Ink.copy(alpha = 0.25f), spotColor = Mx.Ink.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(18.dp))
            .background(Mx.Ink)
            .bounceClick(enabled = enabled, onClick = onClick)
            .graphicsLayer { alpha = if (enabled || loading) 1f else 0.6f },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.2.dp)
        } else {
            GoogleG(Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("المتابعة بحساب جوجل", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** Four-color "G" drawn with arcs — no image asset needed. */
@Composable
private fun GoogleG(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = size.width * 0.2f
        val inset = stroke / 2f
        val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)
        val style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
        drawArc(Color(0xFFEA4335), 200f, 110f, false, topLeft, arcSize, style = style)
        drawArc(Color(0xFFFBBC05), 130f, 70f, false, topLeft, arcSize, style = style)
        drawArc(Color(0xFF34A853), 40f, 90f, false, topLeft, arcSize, style = style)
        drawArc(Color(0xFF4285F4), -50f, 90f, false, topLeft, arcSize, style = style)
        drawLine(
            Color(0xFF4285F4),
            start = Offset(size.width / 2f, size.height / 2f),
            end = Offset(size.width - inset, size.height / 2f),
            strokeWidth = stroke
        )
    }
}

@Composable
private fun SmallChoice(
    icon: ImageVector,
    text: String,
    loading: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Mx.Surface)
            .border(1.dp, Mx.Line, RoundedCornerShape(16.dp))
            .bounceClick(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Mx.Royal, strokeWidth = 2.dp)
        } else {
            Icon(icon, null, tint = Mx.Royal, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = Mx.InkSoft, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Slowly orbiting soft blobs behind the hero — light and cheap to draw. */
@Composable
private fun OrbitBackdrop(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "orbit")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "orbitPhase"
    )
    Canvas(modifier) {
        drawRect(Mx.HeroWash)
        val w = size.width
        val h = size.height
        val a = Offset(w * (0.2f + 0.08f * cos(phase)), h * (0.25f + 0.06f * sin(phase)))
        val b = Offset(w * (0.85f + 0.06f * sin(phase)), h * (0.45f + 0.07f * cos(phase)))
        drawCircle(Brush.radialGradient(listOf(Mx.Royal.copy(alpha = 0.16f), Color.Transparent), center = a, radius = w * 0.55f), radius = w * 0.55f, center = a)
        drawCircle(Brush.radialGradient(listOf(Mx.Violet.copy(alpha = 0.14f), Color.Transparent), center = b, radius = w * 0.5f), radius = w * 0.5f, center = b)
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Mx.Canvas), startY = h * 0.6f, endY = h))
    }
}

@Composable
private fun RestoreSheet(
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit,
    onTyping: () -> Unit
) {
    var key by rememberSaveable { mutableStateOf("") }
    MxSheet(onDismiss = onDismiss) {
        SheetHeader(
            icon = Icons.Rounded.VpnKey,
            title = "رجّع حسابك",
            subtitle = "اكتب مفتاح الاسترجاع اللي حفظته قبل كده"
        )
        OutlinedTextField(
            value = key,
            onValueChange = {
                key = it.uppercase()
                onTyping()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("MLO-XXXX-XXXX-XXXX-XXXX", style = MonoStyle, color = Mx.Text4) },
            textStyle = MonoStyle.copy(fontSize = 15.sp, color = Mx.Ink),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            colors = mxFieldColors()
        )
        AnimatedVisibility(error != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Text(
                error ?: "",
                color = Mx.Rose,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            "استرجاع الحساب",
            onClick = { onRestore(key) },
            loading = loading,
            enabled = key.length >= 10,
            icon = Icons.Rounded.Autorenew,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "المفتاح بيرجّع كل عناوينك ورسائلك على الموبايل ده",
            style = MaterialTheme.typography.bodySmall,
            color = Mx.Text3,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
