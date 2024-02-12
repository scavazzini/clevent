package dev.scavazzini.clevent.recharge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.CurrencyValue
import dev.scavazzini.clevent.ui.components.PrimaryButton

@Composable
fun RechargeScreen(
    modifier: Modifier = Modifier,
) {
    RechargeScreenContent(
        onConfirmOrderButtonTapped = { },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun RechargeScreenContent(
    onConfirmOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fieldValue by remember { mutableStateOf(CurrencyValue(0)) }

    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = fieldValue.toString(),
            visualTransformation = CurrencyVisualTransformation(),
            singleLine = true,
            textStyle = TextStyle(textAlign = TextAlign.Center),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onValueChange = { newValue ->
                try {
                    val rawValue = newValue.filter { it.isDigit() }.toInt()

                    if (rawValue <= 9999999) {
                        fieldValue = CurrencyValue(rawValue)
                    }
                } catch (_: Exception) {
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
        )
        PrimaryButton(
            text = stringResource(R.string.recharge_confirm_button),
            onClick = onConfirmOrderButtonTapped,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun RechargeScreenContentPreview() {
    RechargeScreenContent(
        onConfirmOrderButtonTapped = { },
    )
}
