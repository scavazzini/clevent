package dev.scavazzini.clevent.data.core.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.scavazzini.clevent.data.core.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncProductsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val productRepository: ProductRepository,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val SYNC_PRODUCTS_WORK_NAME = "SyncProductsWork"
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            productRepository.sync("products.json")
                ?: return@withContext Result.failure()

            return@withContext Result.success()
        }
    }
}
