package dev.scavazzini.clevent.feature.order

import android.content.Intent
import android.nfc.Tag
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.CurrencyValue
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.exceptions.InsufficientBalanceException
import dev.scavazzini.clevent.io.NFCReader
import dev.scavazzini.clevent.io.NFCWriter
import dev.scavazzini.clevent.ui.components.NfcBottomSheetReadingState
import dev.scavazzini.clevent.ui.components.PrimaryButtonState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val nfcWriter: NFCWriter,
    private val nfcReader: NFCReader,
) : ViewModel() {

    companion object {
        private const val MODAL_CHANGE_STATE_DELAY = 2500L
    }

    private val _orderUiState: MutableStateFlow<OrderUiState> = MutableStateFlow(OrderUiState())
    val orderUiState: StateFlow<OrderUiState> = _orderUiState

    private val _categories: MutableStateFlow<List<String>> = MutableStateFlow(listOf("All"))
    val categories: StateFlow<List<String>> = _categories

    private val _products: SnapshotStateMap<Product, Int> = mutableStateMapOf()
    val products: SnapshotStateMap<Product, Int> = _products

    init {
        viewModelScope.launch {
            productRepository.getProductsAsFlow().collectLatest { products ->
                _products.clear()
                _products.putAll(products.associateWith { 0 })
            }
        }
    }

    fun performPurchase(intent: Intent) = viewModelScope.launch {
        if (_orderUiState.value.sheetState !== NfcBottomSheetReadingState.WAITING) {
            return@launch
        }

        try {
            val (tag, customer) = nfcReader.extract(intent)

            _products.filter { it.value > 0 }
                .forEach { customer.addProduct(it.key, it.value) }

            nfcWriter.write(tag, customer)

            _orderUiState.value = _orderUiState.value.copy(
                sheetState = NfcBottomSheetReadingState.SUCCESS,
                title = R.string.order_success_title,
                description = R.string.order_success_description,
                descriptionArgs = listOf(CurrencyValue(customer.balance).toString()),
            )
            delay(MODAL_CHANGE_STATE_DELAY)
            _orderUiState.value = _orderUiState.value.copy(showSheet = false)

        } catch (e: InsufficientBalanceException) {
            _orderUiState.value = _orderUiState.value.copy(
                sheetState = NfcBottomSheetReadingState.ERROR,
                title = R.string.order_error_title,
                description = R.string.order_not_enough_credits_description,
            )
            delay(MODAL_CHANGE_STATE_DELAY)
            _orderUiState.value.updateSheetToWaiting()

        } catch (e: Exception) {
            _orderUiState.value = _orderUiState.value.copy(
                sheetState = NfcBottomSheetReadingState.ERROR,
                title = R.string.order_error_title,
                description = R.string.order_error_description,
                descriptionArgs = listOf(e.message ?: "")
            )
            delay(MODAL_CHANGE_STATE_DELAY)
            _orderUiState.value.updateSheetToWaiting()
        }
    }

    fun confirmOrder() {
        _orderUiState.value.updateSheetToWaiting()
    }

    fun cancelOrder() {
        _orderUiState.value = _orderUiState.value.copy(
            showSheet = false,
        )
    }

    fun increase(product: Product) {
        incrementQuantity(product, 1)
    }

    fun decrease(product: Product) {
        incrementQuantity(product, -1)
    }

    private fun incrementQuantity(product: Product, value: Int) {
        _products[product] = _products[product]
            ?.plus(value)
            ?.coerceIn(0..100)
            ?: 0

        _orderUiState.value = _orderUiState.value.copy(
            confirmOrderButtonState = _products.evaluateButtonState(),
            orderValue = _products.total(),
        )
    }

    private fun SnapshotStateMap<Product, Int>.evaluateButtonState(): PrimaryButtonState {
        if (any { it.value > 0 }) {
            return PrimaryButtonState.ENABLED
        }
        return PrimaryButtonState.DISABLED
    }

    private fun SnapshotStateMap<Product, Int>.total(): Int {
        return map { it.key.price * it.value }.sum()
    }

    data class OrderUiState(
        val sheetState: NfcBottomSheetReadingState = NfcBottomSheetReadingState.WAITING,
        val showSheet: Boolean = false,
        val title: Int? = null,
        val titleArgs: List<String> = emptyList(),
        val description: Int? = null,
        val descriptionArgs: List<String> = emptyList(),
        val confirmOrderButtonState: PrimaryButtonState = PrimaryButtonState.DISABLED,
        val orderValue: Int = 0,
    )

    private fun OrderUiState.updateSheetToWaiting() {
        _orderUiState.value = copy(
            sheetState = NfcBottomSheetReadingState.WAITING,
            showSheet = true,
            title = R.string.order_confirm_title,
            description = R.string.order_confirm_description,
        )
    }
}
