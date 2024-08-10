package dev.scavazzini.clevent.feature.order.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.data.model.Product
import dev.scavazzini.clevent.core.data.model.exception.InsufficientBalanceException
import dev.scavazzini.clevent.core.data.repository.NonCleventTagException
import dev.scavazzini.clevent.core.data.repository.ProductRepository
import dev.scavazzini.clevent.core.domain.GetCustomerFromTagUseCase
import dev.scavazzini.clevent.core.domain.WriteCustomerOnTagUseCase
import dev.scavazzini.clevent.core.ui.components.PrimaryButtonState
import dev.scavazzini.clevent.nfc.R.string.non_clevent_tag_error
import dev.scavazzini.clevent.nfc.component.NfcHandlerReadingStatus
import kotlinx.coroutines.Job
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
    private val readCustomerFromTagUseCase: GetCustomerFromTagUseCase,
    private val writeCustomerOnTagUseCase: WriteCustomerOnTagUseCase,
    private val application: Application,
) : ViewModel() {

    companion object {
        private const val MODAL_CHANGE_STATE_DELAY = 2500L
    }

    private var orderJob: Job? = null

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

    fun performPurchase(intent: Intent) {
        orderJob?.cancel()
        orderJob = viewModelScope.launch {
            try {
                val customer = readCustomerFromTagUseCase(intent)

                productsOnCart.value.forEach {
                    customer.addProduct(it.key, it.value)
                }

                writeCustomerOnTagUseCase(customer, intent)

                _orderUiState.value = _orderUiState.value.copy(
                    nfcReadingStatus = NfcHandlerReadingStatus.SUCCESS,
                    nfcReadingMessage = application.getString(
                        R.string.order_success_description,
                        CurrencyValue(customer.balance).toString(),
                    ),
                )
                delay(MODAL_CHANGE_STATE_DELAY)
                clearOrder()

            } catch (e: Exception) {
                val message = when (e) {
                    is InsufficientBalanceException -> {
                        application.getString(R.string.order_not_enough_credits_description)
                    }

                    is NonCleventTagException -> {
                        application.getString(non_clevent_tag_error)
                    }

                    else -> {
                        e.message ?: application.getString(R.string.order_error_title)
                    }
                }

                _orderUiState.value.updateNfcStateToError(message)
            }
        }
    }

    fun clearOrder() {
        _products.update { it.keys.associateWith { 0 } }

        _orderUiState.value = _orderUiState.value.copy(
            nfcReadingStatus = NfcHandlerReadingStatus.HALTED,
            confirmOrderButtonState = PrimaryButtonState.DISABLED,
            orderValue = 0,
        )
    }


    fun confirmOrder() {
        _orderUiState.value.updateNfcStateToListening()
    }

    fun cancelOrder() {
        orderJob?.cancel()
        orderJob = viewModelScope.launch {
            _orderUiState.value = _orderUiState.value.copy(
                nfcReadingStatus = NfcHandlerReadingStatus.HALTED,
            )
        }
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
        val nfcReadingStatus: NfcHandlerReadingStatus = NfcHandlerReadingStatus.HALTED,
        val nfcReadingMessage: String? = null,
        val confirmOrderButtonState: PrimaryButtonState = PrimaryButtonState.DISABLED,
        val orderValue: Int = 0,
    )

    private suspend fun OrderUiState.updateNfcStateToError(message: String) {
        _orderUiState.value = copy(
            nfcReadingStatus = NfcHandlerReadingStatus.ERROR,
            nfcReadingMessage = message,
        )
        delay(MODAL_CHANGE_STATE_DELAY)
        _orderUiState.value.updateNfcStateToListening()
    }

    private fun OrderUiState.updateNfcStateToListening() {
        _orderUiState.value = copy(
            nfcReadingStatus = NfcHandlerReadingStatus.LISTENING,
            nfcReadingMessage = null,
        )
    }
}
