package dev.scavazzini.clevent.data.repositories

import dev.scavazzini.clevent.data.database.ProductDAO
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.webservice.CleventRetrofit
import dev.scavazzini.clevent.utilities.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
        private val productDAO: ProductDAO,
        private val preferences: Preferences,
) {
    fun getProducts() = productDAO.getAll()

    suspend fun sync(): Long? {
        try {
            val response = CleventRetrofit.productService.getProducts(preferences.endpoint)
            val products = response.body()

            if (response.isSuccessful && products != null) {
                productDAO.clearAndInsert(products)
                return Calendar.getInstance().timeInMillis.also { preferences.lastSync = it }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun loadData(products: List<Product>) = withContext(Dispatchers.IO) {
        val filledProducts = productDAO.getProducts(products.map { it.id })
        for (product in products) {
            for (filledProduct in filledProducts) {
                if (product.id != filledProduct.id) continue
                product.name = filledProduct.name
                product.price = filledProduct.price
                break
            }
        }
    }
}
