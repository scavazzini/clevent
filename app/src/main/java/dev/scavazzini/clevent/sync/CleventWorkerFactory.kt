package dev.scavazzini.clevent.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.scavazzini.clevent.core.data.repository.ProductRepository
import dev.scavazzini.clevent.core.data.workers.SyncProductsWorker
import javax.inject.Inject

class CleventWorkerFactory @Inject constructor(
    private val productRepository: ProductRepository,
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
                )
            }

            else -> null
        }
    }

}
