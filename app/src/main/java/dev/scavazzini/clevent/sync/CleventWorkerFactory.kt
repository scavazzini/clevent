package dev.scavazzini.clevent.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.scavazzini.clevent.core.data.repository.ProductRepository
import dev.scavazzini.clevent.core.data.workers.SyncProductsWorker
import dev.scavazzini.clevent.notification.NotificationCenter
import javax.inject.Inject

class CleventWorkerFactory @Inject constructor(
    private val productRepository: ProductRepository,
    private val notificationCenter: NotificationCenter,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncProductsWorker::class.java.name -> {
                SyncProductsWorker(
                    context = appContext,
                    workerParams = workerParameters,
                    productRepository = productRepository,
                    notificationCenter = notificationCenter,
                )
            }

            else -> null
        }
    }

}
