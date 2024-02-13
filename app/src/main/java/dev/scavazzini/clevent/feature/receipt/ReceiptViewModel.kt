package dev.scavazzini.clevent.feature.receipt

import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.EMPTY_CUSTOMER
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.io.NFCReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val nfcReader: NFCReader,
) : ViewModel() {

    private var _customer = MutableStateFlow(EMPTY_CUSTOMER)
    val customer: StateFlow<Customer> = _customer

    suspend fun onNfcTagRead(intent: Intent) {
        try {
            val customer = nfcReader.extract(intent).second
            productRepository.loadData(customer.products.keys.toList())

            _customer.value = customer

        } catch (e: Exception) {
            e.printStackTrace()
            _customer.value = EMPTY_CUSTOMER
        }
    }

    fun share() {
    }

    fun generateQrCode() {
    }

}
