package dev.scavazzini.clevent.ui.order

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.io.NFCWriter
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
        private val productRepository: ProductRepository,
        private val nfcWriter: NFCWriter,
) : ViewModel() {

    val selectedProducts: MutableMap<Product, Int> = mutableMapOf()

    fun getProducts(): LiveData<List<Product>> = productRepository.getProducts()

    suspend fun performPurchase(customer: Customer, tag: Tag) {
        for ((product, quantity) in selectedProducts) {
            customer.addProduct(product, quantity)
        }
        writeOnTag(tag, customer)
    }

    private suspend fun writeOnTag(tag: Tag, customer: Customer) {
        nfcWriter.write(tag, customer)
    }

}
