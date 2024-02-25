package dev.scavazzini.clevent.data.repositories

import dev.scavazzini.clevent.data.datasource.LocalProductDataSource
import dev.scavazzini.clevent.data.datasource.RemoteProductDataSource
import dev.scavazzini.clevent.utilities.Preferences
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val localProductDataSource: LocalProductDataSource,
    private val remoteProductDataSource: RemoteProductDataSource,
    private val preferences: Preferences,
) {
    fun getProducts() = localProductDataSource.getAll()

    suspend fun sync(endpoint: String = preferences.endpoint): Long? {
        try {
            val response = remoteProductDataSource.getProducts(endpoint)
            val products = response.body()

            if (response.isSuccessful && products != null) {
                localProductDataSource.clearAndInsert(products)
                return Calendar.getInstance().timeInMillis.also { preferences.lastSync = it }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
