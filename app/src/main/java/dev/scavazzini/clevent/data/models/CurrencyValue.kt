package dev.scavazzini.clevent.data.models

import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.absoluteValue

class CurrencyValue(
    val rawValue: Int,
    locale: Locale = Locale.getDefault(),
) {
    private val decimalFormat = DecimalFormat.getInstance(locale) as DecimalFormat

    val symbol
        get() = decimalFormat.decimalFormatSymbols.currencySymbol ?: "$"

    val wholeUnit
        get() = "%,d".format((rawValue / 100))

    val fractionalSeparator
        get() = decimalFormat.decimalFormatSymbols.decimalSeparator

    val fractionalUnit
        get() = "%02d".format((rawValue % 100).absoluteValue)

    override fun toString(): String {
        return "$symbol$wholeUnit$fractionalSeparator$fractionalUnit"
    }
}
