package com.burootro.mailio.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.burootro.mailio.data.prefs.MailioPreferences
import com.burootro.mailio.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * بيسحب الرسايل الجديدة كل 15 دقيقة حتى لو التطبيق مقفول
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val notifier: MailioNotifier,
    private val prefs: MailioPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (prefs.getRecoveryKey() == null) {
            return Result.success()
        }

        val notificationsOn = prefs.notificationsEnabled.first()

        return try {
            syncRepository.syncMessages().fold(
                onSuccess = { newMessages ->
                    if (notificationsOn) {
                        newMessages.forEach { message ->
                            notifier.notifyNewMessage(message)
                        }
                    }
                    Result.success()
                },
                onFailure = {
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "mailio_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
