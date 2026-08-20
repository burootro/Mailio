package com.burootro.mailio.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mailio_prefs"
)

@Singleton
class MailioPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val RECOVERY_KEY = stringPreferencesKey("recovery_key")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val PUSH_TOKEN = stringPreferencesKey("push_token")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_BACKED_UP = booleanPreferencesKey("key_backed_up")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_DELETE_DAYS = longPreferencesKey("auto_delete_days")
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
        val PREFERRED_DOMAIN = stringPreferencesKey("preferred_domain")
    }

    // ===== مفتاح الاسترجاع =====

    val recoveryKey: Flow<String?> = context.dataStore.data
        .map { it[Keys.RECOVERY_KEY] }

    suspend fun getRecoveryKey(): String? =
        context.dataStore.data.first()[Keys.RECOVERY_KEY]

    suspend fun getOrCreateRecoveryKey(): String {
        val existing = getRecoveryKey()
        if (existing != null) return existing

        val generated = generateRecoveryKey()
        context.dataStore.edit { it[Keys.RECOVERY_KEY] = generated }
        return generated
    }

    suspend fun setRecoveryKey(key: String) {
        context.dataStore.edit { it[Keys.RECOVERY_KEY] = key }
    }

    val isKeyBackedUp: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.KEY_BACKED_UP] ?: false }

    suspend fun setKeyBackedUp(value: Boolean) {
        context.dataStore.edit { it[Keys.KEY_BACKED_UP] = value }
    }

    // ===== توكن الإشعارات =====

    val pushToken: Flow<String?> = context.dataStore.data
        .map { it[Keys.PUSH_TOKEN] }

    suspend fun getPushToken(): String? =
        context.dataStore.data.first()[Keys.PUSH_TOKEN]

    suspend fun setPushToken(token: String) {
        context.dataStore.edit { it[Keys.PUSH_TOKEN] = token }
    }

    // ===== معرّف الجهاز =====

    val deviceId: Flow<String?> = context.dataStore.data
        .map { it[Keys.DEVICE_ID] }

    suspend fun getOrCreateDeviceId(): String {
        val existing = context.dataStore.data.first()[Keys.DEVICE_ID]
        if (existing != null) return existing

        val generated = randomHex(16)
        context.dataStore.edit { it[Keys.DEVICE_ID] = generated }
        return generated
    }

    // ===== حالة التطبيق =====

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = value }
    }

    // ===== الإعدادات =====

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    }

    val autoDeleteDays: Flow<Long> = context.dataStore.data
        .map { it[Keys.AUTO_DELETE_DAYS] ?: 14L }

    suspend fun setAutoDeleteDays(days: Long) {
        context.dataStore.edit { it[Keys.AUTO_DELETE_DAYS] = days }
    }

    val preferredDomain: Flow<String?> = context.dataStore.data
        .map { it[Keys.PREFERRED_DOMAIN] }

    suspend fun setPreferredDomain(domain: String) {
        context.dataStore.edit { it[Keys.PREFERRED_DOMAIN] = domain }
    }

    val lastSyncAt: Flow<Long> = context.dataStore.data
        .map { it[Keys.LAST_SYNC_AT] ?: 0L }

    suspend fun setLastSyncAt(time: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNC_AT] = time }
    }

    // ===== مسح كامل =====

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    // ===== أدوات التوليد =====

    companion object {

        private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun generateRecoveryKey(): String {
            val random = SecureRandom()
            val groups = (1..4).map {
                buildString {
                    repeat(4) {
                        append(ALPHABET[random.nextInt(ALPHABET.length)])
                    }
                }
            }
            return "MLO-" + groups.joinToString("-")
        }

        fun randomHex(bytes: Int): String {
            val random = SecureRandom()
            val buffer = ByteArray(bytes)
            random.nextBytes(buffer)
            return buffer.joinToString("") { "%02x".format(it) }
        }
    }
}
