package dev.scavazzini.clevent.core.data.workers

import android.app.PendingIntent
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.scavazzini.clevent.core.data.repository.ProductRepository
import dev.scavazzini.clevent.notification.NotificationCenter
import dev.scavazzini.clevent.notification.NotificationPriority
import dev.scavazzini.clevent.notification.NotificationProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncProductsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val productRepository: ProductRepository,
    @Assisted private val notificationCenter: NotificationCenter,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val SYNC_PRODUCTS_WORK_NAME = "SyncProductsWork"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val notificationId = postSyncNotification()

            try {
                productRepository.sync()
                Result.success()

            } catch (e: Exception) {
                Result.failure(
                    Data.Builder().putString("exception", e.message).build()
                )

            } finally {
                notificationCenter.cancelNotification(notificationId)
            }
        }
    }

    private fun postSyncNotification(): Int {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return notificationCenter.postNotification(
            title = "Syncing products...",
            pendingIntent = pendingIntent,
            priority = NotificationPriority.LOW,
            progressBar = NotificationProgress.Indefinite,
            isOngoing = true,
            isAutoCancel = false,
        )
    }
}
