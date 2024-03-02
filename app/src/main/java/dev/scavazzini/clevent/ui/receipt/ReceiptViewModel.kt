package dev.scavazzini.clevent.ui.receipt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.core.model.CurrencyValue
import dev.scavazzini.clevent.data.core.model.Customer
import dev.scavazzini.clevent.data.core.model.EMPTY_CUSTOMER
import dev.scavazzini.clevent.data.core.model.Product
import dev.scavazzini.clevent.data.core.repository.ProductRepository
import dev.scavazzini.clevent.domain.core.FormatDateToStringUseCase
import dev.scavazzini.clevent.domain.core.GetCustomerFromTagUseCase
import dev.scavazzini.clevent.domain.receipt.GenerateQrCodeBitmapUseCase
import dev.scavazzini.clevent.ui.core.components.PrimaryButtonState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val readCustomerFromTagUseCase: GetCustomerFromTagUseCase,
    private val generateQrCodeBitmapUseCase: GenerateQrCodeBitmapUseCase,
    private val formatDateToStringUseCase: FormatDateToStringUseCase,
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

        val qrCode = generateQrCodeBitmapUseCase(
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

    private fun Customer.toReceiptString(
        calendar: Calendar = Calendar.getInstance(),
        context: Context? = null,
    ) = buildString {
        val receiptTitle = context?.getString(R.string.receipt_string_title) ?: "Receipt"
        val totalLabel = context?.getString(R.string.receipt_string_total) ?: "Total:"
        val balanceLabel =
            context?.getString(R.string.receipt_string_available_balance) ?: "Available balance:"
        val totalValue = CurrencyValue(total).toString()
        val balanceValue = CurrencyValue(balance).toString()

        append(String.format("%s (%s)%n%n", receiptTitle, formatDateToStringUseCase(calendar.time)))
        for ((product, quantity) in products) {
            val productTotal = CurrencyValue(product.price * quantity).toString()
            append(String.format("%dx %s: %s%n", quantity, product.name, productTotal))
        }
        append(String.format("%n%s %s%n", totalLabel, totalValue))
        append(String.format("%s %s", balanceLabel, balanceValue))
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
