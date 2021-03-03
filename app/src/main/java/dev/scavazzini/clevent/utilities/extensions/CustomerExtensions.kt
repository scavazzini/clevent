package dev.scavazzini.clevent.utilities.extensions

import android.content.Context
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.Customer
import java.util.*

fun Customer.toReceiptString(calendar: Calendar, context: Context? = null) = buildString {
    val receiptTitle = context?.getString(R.string.title_receipt) ?: "Receipt"
    val totalLabel = context?.getString(R.string.label_total) ?: "Total:"
    val balanceLabel = context?.getString(R.string.label_current_balance) ?: "Current Balance:"

    append(String.format("%s (%s)%n%n", receiptTitle, calendar.time.formatted()))
    for ((product, quantity) in products) {
        val productTotal = product.price * quantity
        append(String.format("%dx %s: %s%n", quantity, product.name, productTotal.toCurrency()))
    }
    append(String.format("%n%s %s%n", totalLabel, total.toCurrency()))
    append(String.format("%s %s", balanceLabel, balance.toCurrency()))
}
