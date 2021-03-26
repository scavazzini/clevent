package dev.scavazzini.clevent.ui.receipt

import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.io.NFCReader
import dev.scavazzini.clevent.io.QRCodeGenerator
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
        private val productRepository: ProductRepository,
        private val qrCodeGenerator: QRCodeGenerator,
        private val nfcReader: NFCReader,
) : ViewModel() {

    var customer: Customer? = null
    var qrCodeBitmap: Bitmap? = null

    suspend fun loadProductsData(products: List<Product>) = productRepository.loadData(products)

    suspend fun generateQrCode(content: String, width: Int, height: Int): Bitmap {
        return qrCodeGenerator.generate(content, width, height)
    }

    fun extractFromIntent(intent: Intent): Customer {
        val customer = nfcReader.extract(intent).second
        this.customer = customer
        return customer
    }

}
