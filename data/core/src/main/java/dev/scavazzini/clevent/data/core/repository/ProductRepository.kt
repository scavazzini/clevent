package dev.scavazzini.clevent.data.core.repository

import dev.scavazzini.clevent.data.core.datasource.LocalProductDataSource
import dev.scavazzini.clevent.data.core.datasource.RemoteProductDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val localProductDataSource: LocalProductDataSource,
    private val remoteProductDataSource: RemoteProductDataSource,
) {
    fun getProducts() = localProductDataSource.getAll()

    suspend fun sync(endpoint: String): Long? {
        try {
            val response = remoteProductDataSource.getProducts(endpoint)
            val products = response.body()

            if (response.isSuccessful && products != null) {
                return localProductDataSource.clearAndInsert(products)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getLastSync(): Long? {
        return localProductDataSource.getLastSyncTime()
    }
}
