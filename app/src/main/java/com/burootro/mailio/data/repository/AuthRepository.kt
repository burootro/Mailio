package com.burootro.mailio.data.repository

import android.content.Intent
import com.burootro.mailio.data.auth.GoogleAuthManager
import com.burootro.mailio.data.local.dao.AddressDao
import com.burootro.mailio.data.local.dao.MessageDao
import com.burootro.mailio.data.mapper.toAddressEntities
import com.burootro.mailio.data.mapper.toEntity
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.remote.MailioApi
import com.burootro.mailio.data.remote.dto.GoogleSignInRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class SignInResult(
    val isNew: Boolean,
    val email: String?,
    val name: String?,
    val addresses: Int,
    val messages: Int
)

@Singleton
class AuthRepository @Inject constructor(
    private val api: MailioApi,
    private val googleAuth: GoogleAuthManager,
    private val addressDao: AddressDao,
    private val messageDao: MessageDao,
    private val prefs: MailioPreferences
) {

    /**
     * بيرجّع الـ Intent اللي بيفتح شاشة اختيار حساب جوجل
     */
    fun getSignInIntent(): Intent = googleAuth.getSignInIntent()

    /**
     * بيعالج نتيجة تسجيل الدخول ويسحب كل بيانات المستخدم
     */
    suspend fun signInWithGoogle(data: Intent?): Result<SignInResult> =
        withContext(Dispatchers.IO) {
            try {
                // 1. نجيب توكن Firebase
                val tokenResult = googleAuth.handleSignInResult(data)

                val idToken = tokenResult.getOrElse { error ->
                    return@withContext Result.failure(error)
                }

                // 2. نبعته للسيرفر
                val response = api.googleSignIn(GoogleSignInRequest(idToken))

                // 3. نحفظ المفتاح والبروفايل
                prefs.setRecoveryKey(response.accessKey)
                prefs.setKeyBackedUp(true)
                prefs.saveGoogleProfile(
                    email = response.profile.email,
                    name = response.profile.name,
                    photo = response.profile.photo
                )

                if (response.domains.isNotEmpty()) {
                    prefs.setPreferredDomain(response.domains.first())
                }

                // 4. نستبدل البيانات المحلية ببيانات الحساب
                messageDao.deleteAll()
                addressDao.deleteAll()

                if (response.addresses.isNotEmpty()) {
                    addressDao.insertAll(response.addresses.toAddressEntities())
                }

                if (response.messages.isNotEmpty()) {
                    val readIds = prefs.getReadMessageIds()
                    messageDao.insertAll(
                        response.messages.map { it.toEntity(isRead = it.id in readIds) }
                    )
                }

                refreshUnreadCounts()

                Result.success(
                    SignInResult(
                        isNew = response.isNew,
                        email = response.profile.email,
                        name = response.profile.name,
                        addresses = response.addresses.size,
                        messages = response.messages.size
                    )
                )
            } catch (e: Exception) {
                Result.failure(mapError(e))
            }
        }

    /**
     * تسجيل الخروج — بيمسح كل حاجة محلياً
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            googleAuth.signOut()

            messageDao.deleteAll()
            addressDao.deleteAll()
            prefs.clearAll()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isSignedIn(): Boolean = googleAuth.isSignedIn()

    private suspend fun refreshUnreadCounts() {
        addressDao.getAllOnce().forEach { address ->
            val unread = messageDao.countUnread(address.id)
            addressDao.setUnreadCount(address.id, unread)
        }
    }

    private fun mapError(e: Exception): Exception {
        val message = e.message ?: ""
        return when {
            message.contains("اتلغى") -> e
            message.contains("401") -> Exception("فشل التحقق، جرب تاني")
            message.contains("timeout", true) -> Exception("السيرفر بطيء، جرب تاني")
            message.contains("إعدادات") -> e
            message.contains("إنترنت") -> e
            else -> Exception("فشل تسجيل الدخول، جرب تاني")
        }
    }
}
