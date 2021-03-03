package dev.scavazzini.clevent.ui.recharge

import android.nfc.Tag
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.io.NFCWriter

class RechargeViewModel @ViewModelInject constructor(
        private val nfcWriter: NFCWriter,
) : ViewModel() {

    suspend fun recharge(tag: Tag, customer: Customer, value: Int) {
        customer.recharge(value)
        nfcWriter.write(tag, customer)
    }

}
