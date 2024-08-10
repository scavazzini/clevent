package dev.scavazzini.clevent.feature.recharge.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.data.repository.NonCleventTagException
import dev.scavazzini.clevent.core.domain.GetCustomerFromTagUseCase
import dev.scavazzini.clevent.core.domain.WriteCustomerOnTagUseCase
import dev.scavazzini.clevent.nfc.R.string.non_clevent_tag_error
import dev.scavazzini.clevent.nfc.component.NfcHandlerReadingStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val readCustomerFromTagUseCase: GetCustomerFromTagUseCase,
    private val writeCustomerOnTagUseCase: WriteCustomerOnTagUseCase,
    private val application: Application,
) : ViewModel() {

    private var rechargeJob: Job? = null

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

    fun recharge(intent: Intent) {
        rechargeJob?.cancel()
        rechargeJob = viewModelScope.launch {
            try {
                val customer = readCustomerFromTagUseCase(intent)
                customer.recharge(_fieldValue.value.rawValue)

                writeCustomerOnTagUseCase(customer, intent)

                _rechargeUiState.value = _rechargeUiState.value.copy(
                    nfcReadingStatus = NfcHandlerReadingStatus.SUCCESS,
                    nfcReadingMessage = application.getString(
                        R.string.recharge_success_description,
                        CurrencyValue(customer.balance).toString(),
                    ),
                )
                delay(1500)
                _rechargeUiState.value = _rechargeUiState.value.copy(
                    nfcReadingStatus = NfcHandlerReadingStatus.HALTED,
                )

            } catch (e: Exception) {
                val message: String = when (e) {
                    is NonCleventTagException -> application.getString(non_clevent_tag_error)
                    else -> e.message ?: application.getString(R.string.recharge_error_title)
                }

                _rechargeUiState.value.updateNfcStateToError(message)
            }
        }
    }

    fun confirmRecharge() {
        _rechargeUiState.value.updateNfcStateToListening()
    }

    fun cancelRecharge() {
        rechargeJob?.cancel()
        rechargeJob = viewModelScope.launch {
            _rechargeUiState.value = _rechargeUiState.value.copy(
                nfcReadingStatus = NfcHandlerReadingStatus.HALTED,
            )
        }
    }

    data class RechargeUiState(
        val nfcReadingStatus: NfcHandlerReadingStatus = NfcHandlerReadingStatus.HALTED,
        val nfcReadingMessage: String? = null,
    )

    private suspend fun RechargeUiState.updateNfcStateToError(message: String) {
        _rechargeUiState.value = copy(
            nfcReadingStatus = NfcHandlerReadingStatus.ERROR,
            nfcReadingMessage = message,
        )
        delay(1500)
        _rechargeUiState.value.updateNfcStateToListening()
    }

    private fun RechargeUiState.updateNfcStateToListening() {
        _rechargeUiState.value = copy(
            nfcReadingStatus = NfcHandlerReadingStatus.LISTENING,
            nfcReadingMessage = null,
        )
    }
}
