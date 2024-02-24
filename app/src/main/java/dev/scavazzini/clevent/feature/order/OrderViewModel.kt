package dev.scavazzini.clevent.feature.order

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.CurrencyValue
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.data.models.exception.InsufficientBalanceException
import dev.scavazzini.clevent.io.NFCReader
import dev.scavazzini.clevent.io.NFCWriter
import dev.scavazzini.clevent.ui.components.NfcBottomSheetReadingState
import dev.scavazzini.clevent.ui.components.PrimaryButtonState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
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

    private val _categories: MutableStateFlow<List<Pair<String, Boolean>>> = MutableStateFlow(
        emptyList(),
    )
    val categories: StateFlow<List<Pair<String, Boolean>>> = _categories

    private val _searchFieldValue = MutableStateFlow("")
    val searchFieldValue: StateFlow<String> = _searchFieldValue

    private val _products: MutableStateFlow<Map<Product, Int>> = MutableStateFlow(emptyMap())

    val products: StateFlow<Map<Product, Int>> = _products
        .combine(_searchFieldValue) { products, term ->
            products.filter { it.key.name.contains(term, ignoreCase = true) }
        }
        .combine(_categories) { products, categories ->
            val category = categories.firstOrNull { it.second }?.first
                ?: return@combine products

            products.filter { it.key.category == category }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val productsOnCart: StateFlow<Map<Product, Int>> = _products
        .transform { products -> emit(products.filter { it.value > 0 }) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        viewModelScope.launch {
            productRepository.getProducts().collectLatest { products ->
                _products.value = products.associateWith { 0 }
                _categories.value = products
                    .map { it.category }
                    .distinct()
                    .associateWith { false }
                    .toList()
            }
        }
    }

    fun onSearchFieldValueChange(value: String) {
        _searchFieldValue.value = value
    }

    fun onCategoryChange(category: String?) {
        _categories.value = _categories.value.map {
            it.copy(second = it.first == category)
        }
    }

    fun performPurchase(intent: Intent) = viewModelScope.launch {
        if (!_orderUiState.value.isReadyToOrder()) {
            return@launch
        }

        try {
            val (tag, customer) = nfcReader.extract(intent)

            productsOnCart.value.forEach {
                customer.addProduct(it.key, it.value)
            }

            nfcWriter.write(tag, customer)

            _orderUiState.value = _orderUiState.value.copy(
                sheetState = NfcBottomSheetReadingState.SUCCESS,
                title = R.string.order_success_title,
                description = R.string.order_success_description,
                descriptionArgs = listOf(CurrencyValue(customer.balance).toString()),
            )
            delay(MODAL_CHANGE_STATE_DELAY)
            _products.update { it.keys.associateWith { 0 } }
            _orderUiState.value = _orderUiState.value.copy(
                showSheet = false,
                confirmOrderButtonState = PrimaryButtonState.DISABLED,
                orderValue = 0,
            )

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
        _products.value = _products.value.toMutableMap().apply {
            this[product] = this[product]
                ?.plus(value)
                ?.coerceIn(0..100)
                ?: 0
        }

        _orderUiState.value = _orderUiState.value.copy(
            confirmOrderButtonState = _products.value.evaluateButtonState(),
            orderValue = _products.value.total(),
        )
    }

    private fun Map<Product, Int>.evaluateButtonState(): PrimaryButtonState {
        if (any { it.value > 0 }) {
            return PrimaryButtonState.ENABLED
        }
        return PrimaryButtonState.DISABLED
    }

    private fun Map<Product, Int>.total(): Int {
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
    ) {
        fun isReadyToOrder(): Boolean {
            return sheetState == NfcBottomSheetReadingState.WAITING && showSheet
        }
    }

    private fun OrderUiState.updateSheetToWaiting() {
        _orderUiState.value = copy(
            sheetState = NfcBottomSheetReadingState.WAITING,
            showSheet = true,
            title = R.string.order_confirm_title,
            description = R.string.order_confirm_description,
        )
    }
}
