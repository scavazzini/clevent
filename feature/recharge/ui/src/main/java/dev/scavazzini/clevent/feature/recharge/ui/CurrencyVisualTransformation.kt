package dev.scavazzini.clevent.feature.recharge.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import dev.scavazzini.clevent.core.data.model.CurrencyValue

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val filteredText = text.text.filter { it.isDigit() }.toInt()

        return TransformedText(
            text = CurrencyValue(filteredText).toAnnotatedString(),
            offsetMapping = OffsetMapping.Identity,
        )
    }
}
