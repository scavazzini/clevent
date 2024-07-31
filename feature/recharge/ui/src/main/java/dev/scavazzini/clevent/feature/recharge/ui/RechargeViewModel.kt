package dev.scavazzini.clevent.feature.recharge.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.domain.GetCustomerFromTagUseCase
import dev.scavazzini.clevent.core.domain.WriteCustomerOnTagUseCase
import dev.scavazzini.clevent.core.ui.components.NfcBottomSheetReadingState
import dev.scavazzini.clevent.core.ui.components.NfcReadingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val readCustomerFromTagUseCase: GetCustomerFromTagUseCase,
    private val writeCustomerOnTagUseCase: WriteCustomerOnTagUseCase,
) : ViewModel() {

    private val _rechargeUiState = MutableStateFlow(RechargeUiState())
    val uiState: StateFlow<RechargeUiState> = _rechargeUiState

    private val _fieldValue = MutableStateFlow(CurrencyValue(0))
    val fieldValue: StateFlow<CurrencyValue> = _fieldValue

    fun onValueChange(newValue: String) {
        try {
            val rawValue = newValue.filter { it.isDigit() }.toInt()

            if (rawValue <= 9999999) {
                _fieldValue.value = CurrencyValue(rawValue)
            }
        } catch (_: Exception) {
        }
    }

    fun recharge(intent: Intent) = viewModelScope.launch {
        if (!_rechargeUiState.value.isReadyToRecharge()) {
            return@launch
        }

        try {
            val customer = readCustomerFromTagUseCase(intent)
            customer.recharge(_fieldValue.value.rawValue)

            writeCustomerOnTagUseCase(customer, intent)

            _rechargeUiState.value = _rechargeUiState.value.copy(
                sheetState = NfcReadingState(state = NfcBottomSheetReadingState.SUCCESS),
                title = R.string.recharge_success_title,
                description = R.string.recharge_success_description,
                descriptionArgs = listOf(CurrencyValue(customer.balance).toString()),
            )
            delay(1500)
            _rechargeUiState.value = _rechargeUiState.value.copy(showSheet = false)

        } catch (e: Exception) {
            _rechargeUiState.value = _rechargeUiState.value.copy(
                sheetState = NfcReadingState(state = NfcBottomSheetReadingState.ERROR),
                title = R.string.recharge_error_title,
                description = R.string.recharge_error_description,
                descriptionArgs = listOf(e.message ?: ""),
            )
            delay(1500)
            _rechargeUiState.value = _rechargeUiState.value.copy(
                sheetState = NfcReadingState(state = NfcBottomSheetReadingState.WAITING),
                title = R.string.recharge_confirm_title,
                description = R.string.recharge_confirm_description,
                descriptionArgs = listOf(fieldValue.value.toString()),
            )
        }
    }

    fun confirmOrder() {
        _rechargeUiState.value = _rechargeUiState.value.copy(
            sheetState = NfcReadingState(state = NfcBottomSheetReadingState.WAITING),
            showSheet = true,
            title = R.string.recharge_confirm_title,
            description = R.string.recharge_confirm_description,
            descriptionArgs = listOf(fieldValue.value.toString()),
        )
    }

    fun cancelOrder() {
        _rechargeUiState.value = _rechargeUiState.value.copy(
            showSheet = false,
        )
    }

    data class RechargeUiState(
        val sheetState: NfcReadingState = NfcReadingState(state = NfcBottomSheetReadingState.WAITING),
        val showSheet: Boolean = false,
        val title: Int? = null,
        val titleArgs: List<String> = emptyList(),
        val description: Int? = null,
        val descriptionArgs: List<String> = emptyList(),
    ) {
        fun isReadyToRecharge(): Boolean {
            return sheetState.state == NfcBottomSheetReadingState.WAITING && showSheet
        }
    }
}
