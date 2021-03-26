package dev.scavazzini.clevent.ui.recharge

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.io.NFCWriter
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor(
        private val nfcWriter: NFCWriter,
) : ViewModel() {

    suspend fun recharge(tag: Tag, customer: Customer, value: Int) {
        customer.recharge(value)
        nfcWriter.write(tag, customer)
    }

}
