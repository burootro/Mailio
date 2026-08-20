package com.burootro.mailio.data.auth

import android.content.Context
import android.content.Intent
import com.burootro.mailio.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val signInClient: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, options)
    }

    /**
     * بيرجّع الـ Intent اللي بيفتح شاشة اختيار حساب جوجل
     */
    fun getSignInIntent(): Intent = signInClient.signInIntent

    /**
     * بيعالج النتيجة ويرجّع الـ idToken بتاع Firebase
     */
    suspend fun handleSignInResult(data: Intent?): Result<String> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()

            val googleIdToken = account.idToken
                ?: return Result.failure(Exception("مفيش توكن من جوجل"))

            // نسجّل في Firebase Auth
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("فشل تسجيل الدخول"))

            // نجيب توكن Firebase — ده اللي السيرفر بيتحقق منه
            val firebaseToken = firebaseUser.getIdToken(false).await().token
                ?: return Result.failure(Exception("مفيش توكن"))

            Result.success(firebaseToken)
        } catch (e: Exception) {
            Result.failure(mapError(e))
        }
    }

    /**
     * تسجيل الخروج
     */
    suspend fun signOut() {
        try {
            auth.signOut()
            signInClient.signOut().await()
        } catch (e: Exception) {
            // مش مهم لو فشل
        }
    }

    /**
     * هل فيه حساب مسجّل حالياً
     */
    fun isSignedIn(): Boolean = auth.currentUser != null

    val currentEmail: String?
        get() = auth.currentUser?.email

    val currentName: String?
        get() = auth.currentUser?.displayName

    val currentPhoto: String?
        get() = auth.currentUser?.photoUrl?.toString()

    private fun mapError(e: Exception): Exception {
        val message = e.message ?: ""
        return when {
            message.contains("12501") || message.contains("cancel", true) ->
                Exception("اتلغى تسجيل الدخول")
            message.contains("10:") || message.contains("DEVELOPER_ERROR") ->
                Exception("إعدادات جوجل مش مظبوطة")
            message.contains("7:") || message.contains("network", true) ->
                Exception("مفيش اتصال بالإنترنت")
            else -> Exception("فشل تسجيل الدخول، جرب تاني")
        }
    }
}
