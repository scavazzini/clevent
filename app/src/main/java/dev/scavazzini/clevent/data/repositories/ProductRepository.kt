package dev.scavazzini.clevent.data.repositories

import dev.scavazzini.clevent.data.database.ProductDAO
import dev.scavazzini.clevent.data.webservice.ProductService
import dev.scavazzini.clevent.utilities.Preferences
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDAO: ProductDAO,
    private val productService: ProductService,
    private val preferences: Preferences,
) {
    fun getProducts() = productDAO.getAll()

    suspend fun sync(endpoint: String = preferences.endpoint): Long? {
        try {
            val response = productService.getProducts(endpoint)
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
}
