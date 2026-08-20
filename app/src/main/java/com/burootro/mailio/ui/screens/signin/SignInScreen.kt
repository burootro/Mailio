package com.burootro.mailio.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.burootro.mailio.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "MailioAuth"
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val signInClient: GoogleSignInClient by lazy {
        val webClientId = context.getString(R.string.default_web_client_id)
        Log.d(TAG, "webClientId = $webClientId")

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent = signInClient.signInIntent

    suspend fun handleSignInResult(data: Intent?): Result<String> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()

            Log.d(TAG, "account = ${account.email}, hasToken = ${account.idToken != null}")

            val googleIdToken = account.idToken
                ?: return Result.failure(Exception("مفيش توكن من جوجل — البصمة غلط"))

            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("فشل تسجيل الدخول"))

            val firebaseToken = firebaseUser.getIdToken(false).await().token
                ?: return Result.failure(Exception("مفيش توكن"))

            Log.d(TAG, "success")
            Result.success(firebaseToken)
        } catch (e: ApiException) {
            Log.e(TAG, "ApiException code=${e.statusCode}", e)
            Result.failure(mapApiError(e.statusCode))
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            Result.failure(Exception(e.message ?: "فشل تسجيل الدخول"))
        }
    }

    suspend fun signOut() {
        try {
            auth.signOut()
            signInClient.signOut().await()
        } catch (e: Exception) {
            // مش مهم
        }
    }

    fun isSignedIn(): Boolean = auth.currentUser != null

    val currentEmail: String?
        get() = auth.currentUser?.email

    val currentName: String?
        get() = auth.currentUser?.displayName

    val currentPhoto: String?
        get() = auth.currentUser?.photoUrl?.toString()

    private fun mapApiError(code: Int): Exception = when (code) {
        10 -> Exception("خطأ 10: البصمة SHA-1 مش مضافة في Firebase")
        12501 -> Exception("اتلغى تسجيل الدخول")
        12500 -> Exception("خطأ 12500: إعدادات جوجل ناقصة")
        7 -> Exception("مفيش اتصال بالإنترنت")
        else -> Exception("خطأ رقم $code")
    }
}
