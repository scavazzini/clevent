package dev.scavazzini.clevent.data.datasource

import android.content.SharedPreferences
import dev.scavazzini.clevent.data.database.ProductDao
import dev.scavazzini.clevent.data.models.Product
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

private const val LAST_SYNC_KEY = "last_sync"

class LocalProductDataSource @Inject constructor(
    private val productDao: ProductDao,
    private val sharedPreferences: SharedPreferences,
) {
    private val editor = sharedPreferences.edit()

    fun getAll(): Flow<List<Product>> {
        return productDao.getAll()
    }

    fun getLastSyncTime(): Long? {
        val lastSync = sharedPreferences.getLong(LAST_SYNC_KEY, -1)

        if (lastSync <= 0) {
            return null
        }

        return lastSync
    }

    suspend fun clearAndInsert(
        products: List<Product>,
        calendar: Calendar = Calendar.getInstance(),
    ): Long {
        productDao.clearAndInsert(products)

        val lastSync = calendar.timeInMillis

        editor.putLong(LAST_SYNC_KEY, lastSync).apply()
        return lastSync
    }
}
