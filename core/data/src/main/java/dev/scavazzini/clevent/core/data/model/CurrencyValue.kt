package dev.scavazzini.clevent.core.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.absoluteValue

class CurrencyValue(
    val rawValue: Int,
    locale: Locale = Locale.getDefault(),
) {
    private val decimalFormat = DecimalFormat.getInstance(locale) as DecimalFormat

    private val symbol
        get() = decimalFormat.decimalFormatSymbols.currencySymbol ?: "$"

    private val wholeUnit
        get() = "%,d".format((rawValue / 100))

    private val fractionalSeparator
        get() = decimalFormat.decimalFormatSymbols.decimalSeparator

    private val fractionalUnit
        get() = "%02d".format((rawValue % 100).absoluteValue)

    override fun toString(): String {
        return "$symbol$wholeUnit$fractionalSeparator$fractionalUnit"
    }

    fun toAnnotatedString(): AnnotatedString {
        return buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Color(0xff55624C),
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                withStyle(style = SpanStyle(fontSize = 24.sp)) {
                    append(symbol)
                }
                withStyle(style = SpanStyle(fontSize = 64.sp)) {
                    append(wholeUnit)
                }
                withStyle(style = SpanStyle(fontSize = 24.sp)) {
                    append("${fractionalSeparator}${fractionalUnit}")
                }
            }
        }
    }
}
