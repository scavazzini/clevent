package dev.scavazzini.clevent.core.data.repository

import dev.scavazzini.clevent.core.data.datasource.LocalProductDataSource
import dev.scavazzini.clevent.core.data.datasource.RemoteProductDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val localProductDataSource: LocalProductDataSource,
    private val remoteProductDataSource: RemoteProductDataSource,
) {
    companion object {
        private const val PRODUCTS_ENDPOINT = "products.json"
    }

    fun getProducts() = localProductDataSource.getAll()

    suspend fun sync(): Long {
        val response = remoteProductDataSource.getProducts(PRODUCTS_ENDPOINT)
        val products = response.body()

        if (!response.isSuccessful || products == null) {
            throw Exception("Sync failed with status code ${response.code()}")
        }

        return localProductDataSource.clearAndInsert(products)
    }

    fun getLastSync(): Long? {
        return localProductDataSource.getLastSyncTime()
    }
}
