package dev.scavazzini.clevent.feature.receipt

import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.EMPTY_CUSTOMER
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.domain.GetCustomerFromTagUseCase
import dev.scavazzini.clevent.io.QRCodeGenerator
import dev.scavazzini.clevent.ui.components.PrimaryButtonState
import dev.scavazzini.clevent.utilities.extensions.toReceiptString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val readCustomerFromTagUseCase: GetCustomerFromTagUseCase,
    private val qrCodeGenerator: QRCodeGenerator,
) : ViewModel() {

    private var _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState

    private val products: MutableMap<Short, Product> = mutableMapOf()

    init {
        viewModelScope.launch {
            productRepository.getProducts().collectLatest { p ->
                products.clear()
                products.putAll(p.associateBy { it.id })
            }
        }
    }

    fun onNfcTagRead(intent: Intent) {
        try {
            val customer = readCustomerFromTagUseCase(intent)

            val enrichedProducts = customer.products.mapKeys {
                val enrichedProduct = products[it.key.id] ?: it.key

                it.key.copy(
                    name = enrichedProduct.name,
                    price = enrichedProduct.price,
                    category = enrichedProduct.category,
                )
            }

            _uiState.update {
                it.copy(
                    customer = Customer(
                        balance = customer.balance,
                        products = enrichedProducts,
                    ),
                    qrCode = null,
                    showQrCodeSheet = false,
                    qrCodeButtonState = PrimaryButtonState.ENABLED,
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()

            _uiState.update {
                it.copy(
                    customer = EMPTY_CUSTOMER,
                    qrCode = null,
                    showQrCodeSheet = false,
                    qrCodeButtonState = PrimaryButtonState.DISABLED,
                )
            }
        }
    }

    fun share() {
    }

    fun showQrCode(size: Int) = viewModelScope.launch {
        _uiState.update {
            it.copy(qrCodeButtonState = PrimaryButtonState.LOADING)
        }

        if (_uiState.value.qrCode != null) {
            return@launch _uiState.update {
                it.copy(
                    showQrCodeSheet = true,
                    qrCodeButtonState = PrimaryButtonState.ENABLED,
                )
            }
        }

        val qrCode = qrCodeGenerator.generate(
            content = uiState.value.customer.toReceiptString(),
            width = size,
            height = size,
        )

        _uiState.update {
            it.copy(
                qrCode = qrCode,
                showQrCodeSheet = true,
                qrCodeButtonState = PrimaryButtonState.ENABLED,
            )
        }
    }

    fun dismissQrCode() {
        _uiState.update {
            it.copy(
                showQrCodeSheet = false,
            )
        }
    }

    data class ReceiptUiState(
        val customer: Customer = EMPTY_CUSTOMER,
        val qrCode: Bitmap? = null,
        val showQrCodeSheet: Boolean = false,
        val qrCodeButtonState: PrimaryButtonState = PrimaryButtonState.DISABLED,
        val shareButtonState: PrimaryButtonState = PrimaryButtonState.DISABLED,
    )

}
