package dev.scavazzini.clevent.ui.order

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.exceptions.InsufficientBalanceException
import dev.scavazzini.clevent.io.NFCWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
        private val productRepository: ProductRepository,
        private val nfcWriter: NFCWriter,
) : ViewModel() {

    private val _orderUiState: MutableStateFlow<OrderUiState> = MutableStateFlow(OrderUiState.Empty)
    val orderUiState: StateFlow<OrderUiState> = _orderUiState
    val selectedProducts: MutableMap<Product, Int> = mutableMapOf()

    fun getProducts(): LiveData<List<Product>> = productRepository.getProducts()

    fun performPurchase(customer: Customer, tag: Tag) = viewModelScope.launch {
        _orderUiState.value = OrderUiState.Loading

        try {
            for ((product, quantity) in selectedProducts) {
                customer.addProduct(product, quantity)
            }
            writeOnTag(tag, customer)
            _orderUiState.value = OrderUiState.Success(customer)

        } catch (e: InsufficientBalanceException) {
            _orderUiState.value = OrderUiState.Error(R.string.not_enough_credits)

        } catch (e: Exception) {
            _orderUiState.value = OrderUiState.Error()
        }
    }

    private suspend fun writeOnTag(tag: Tag, customer: Customer) {
        nfcWriter.write(tag, customer)
    }

    sealed class OrderUiState {
        object Loading : OrderUiState()
        data class Success(val customer: Customer) : OrderUiState()
        data class Error(val messageId: Int? = null) : OrderUiState()
        object Empty : OrderUiState()
    }
}
