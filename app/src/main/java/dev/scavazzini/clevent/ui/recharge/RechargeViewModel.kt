package dev.scavazzini.clevent.ui.recharge

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.io.NFCWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor(
        private val nfcWriter: NFCWriter,
) : ViewModel() {

    private val _rechargeUiState = MutableStateFlow<RechargeUiState>(RechargeUiState.Empty)
    val rechargeUiState: StateFlow<RechargeUiState> = _rechargeUiState

    fun recharge(tag: Tag, customer: Customer, value: Int) = viewModelScope.launch {
        _rechargeUiState.value = RechargeUiState.Loading

        try {
            customer.recharge(value)
            nfcWriter.write(tag, customer)
            _rechargeUiState.value = RechargeUiState.Success(customer)

        } catch (e: IllegalArgumentException) {
            _rechargeUiState.value = RechargeUiState.Error(e.message)

        } catch (e: Exception) {
            _rechargeUiState.value = RechargeUiState.Error()
        }
    }

    sealed class RechargeUiState {
        object Loading : RechargeUiState()
        data class Success(val customer: Customer) : RechargeUiState()
        data class Error(val message: String? = null) : RechargeUiState()
        object Empty : RechargeUiState()
    }

}
