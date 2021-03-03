package dev.scavazzini.clevent.utilities.extensions

import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

fun Int.toCurrency(): String {
    val locale = Locale.getDefault()
    return NumberFormat.getCurrencyInstance(locale).format(this / 100.0)
}

fun String.toIntCurrency(): Int = (toDouble() * 100.0).toInt()

fun Date.formatted(): String {
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.SHORT, Locale.getDefault())
    return dateFormat.format(time)
}
