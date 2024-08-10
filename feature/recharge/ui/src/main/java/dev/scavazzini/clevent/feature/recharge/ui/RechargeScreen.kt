package dev.scavazzini.clevent.feature.recharge.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.ui.components.PrimaryButton
import dev.scavazzini.clevent.core.ui.components.PrimaryButtonState
import dev.scavazzini.clevent.core.ui.theme.CleventTheme
import dev.scavazzini.clevent.nfc.NfcHandlerPrompt
import kotlinx.coroutines.launch

@Composable
fun RechargeScreen(
    viewModel: RechargeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val fieldValue by viewModel.fieldValue.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    NfcHandlerPrompt(
        title = stringResource(R.string.recharge_confirm_title),
        description = stringResource(
            id = R.string.recharge_confirm_description,
            fieldValue.toString(),
        ),
        readingStatus = uiState.nfcReadingStatus,
        onDismiss = viewModel::cancelRecharge,
        readingMessage = uiState.nfcReadingMessage,
        onTagRead = viewModel::recharge,
    )

    RechargeScreenContent(
        fieldValue = fieldValue,
        modifier = modifier,
        onFieldValueChange = { viewModel.onValueChange(it) },
        onConfirmRechargeButtonTapped = {
            coroutineScope.launch {
                focusManager.clearFocus()
                viewModel.confirmRecharge()
            }
        },
    )
}

@Composable
private fun RechargeScreenContent(
    fieldValue: CurrencyValue,
    onFieldValueChange: (String) -> Unit,
    onConfirmRechargeButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rechargeEnabled = fieldValue.rawValue > 0
    val imeAction = if (rechargeEnabled) ImeAction.Done else ImeAction.None

    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = fieldValue.toString(),
            visualTransformation = CurrencyVisualTransformation(),
            singleLine = true,
            textStyle = TextStyle(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onConfirmRechargeButtonTapped() }
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            onValueChange = onFieldValueChange,
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

        val buttonState = if (rechargeEnabled)
            PrimaryButtonState.ENABLED
        else
            PrimaryButtonState.DISABLED

        PrimaryButton(
            text = stringResource(R.string.recharge_confirm_button),
            onClick = onConfirmRechargeButtonTapped,
            state = buttonState,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun RechargeScreenContentPreview() {
    CleventTheme {
        RechargeScreenContent(
            fieldValue = CurrencyValue(0),
            onFieldValueChange = { },
            onConfirmRechargeButtonTapped = { },
        )
    }
}
