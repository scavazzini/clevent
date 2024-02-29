package dev.scavazzini.clevent.ui.recharge

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import dev.scavazzini.clevent.data.models.CurrencyValue

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val filteredText = text.text.filter { it.isDigit() }.toInt()

        return TransformedText(
            text = CurrencyValue(filteredText).toAnnotatedString(),
            offsetMapping = OffsetMapping.Identity,
        )
    }
}
