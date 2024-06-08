package dev.scavazzini.clevent.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.scavazzini.clevent.data.core.repository.ProductRepository
import dev.scavazzini.clevent.data.core.workers.SyncProductsWorker
import javax.inject.Inject

class CleventWorkerFactory @Inject constructor(
    private val productRepository: ProductRepository,
) : WorkerFactory() {

    override fun createWorker(
        context: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncProductsWorker::class.java.name -> {
                SyncProductsWorker(
                    context = context,
                    workerParams = workerParameters,
                    productRepository = productRepository,
                )
            }

            else -> null
        }
    }

}
