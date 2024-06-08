package dev.scavazzini.clevent

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import dev.scavazzini.clevent.sync.CleventWorkerFactory
import dev.scavazzini.clevent.sync.SyncProductsWorker
import javax.inject.Inject

@HiltAndroidApp
class CleventApplication : Application(), Configuration.Provider {

    companion object {
        private const val SYNC_PRODUCTS_WORK = "SyncProductsWork"
    }

    @Inject
    lateinit var workerFactory: CleventWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncProducts()
    }

    private fun syncProducts() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncProductsRequest = OneTimeWorkRequest.Builder(SyncProductsWorker::class.java)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                SYNC_PRODUCTS_WORK,
                ExistingWorkPolicy.REPLACE,
                syncProductsRequest,
            )
    }
}
