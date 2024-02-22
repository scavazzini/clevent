package dev.scavazzini.clevent.feature.recharge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.CurrencyValue
import dev.scavazzini.clevent.ui.OnNewIntentHandler
import dev.scavazzini.clevent.ui.components.NfcModalBottomSheet
import dev.scavazzini.clevent.ui.components.PrimaryButton
import dev.scavazzini.clevent.ui.theme.CleventTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    viewModel: RechargeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val fieldValue by viewModel.fieldValue.observeAsState(CurrencyValue(0))
    val coroutineScope = rememberCoroutineScope()

    OnNewIntentHandler { viewModel.recharge(it) }

    RechargeScreenContent(
        fieldValue = fieldValue.toString(),
        modifier = modifier,
        state = uiState,
        onDismiss = viewModel::cancelOrder,
        onFieldValueChange = { viewModel.onValueChange(it) },
        onConfirmOrderButtonTapped = {
            coroutineScope.launch {
                viewModel.confirmOrder()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RechargeScreenContent(
    fieldValue: String,
    onFieldValueChange: (String) -> Unit,
    onConfirmOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit = { },
    state: RechargeViewModel.RechargeUiState,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = fieldValue,
            visualTransformation = CurrencyVisualTransformation(),
            singleLine = true,
            textStyle = TextStyle(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
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
        PrimaryButton(
            text = stringResource(R.string.recharge_confirm_button),
            onClick = onConfirmOrderButtonTapped,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        )
    }

    if (state.showSheet) {
        val title = state.title?.let { titleRes ->
            stringResource(titleRes, *state.titleArgs.toTypedArray())
        }

        val description = state.description?.let { descriptionRes ->
            stringResource(descriptionRes, *state.descriptionArgs.toTypedArray())
        }

        NfcModalBottomSheet(
            onDismiss = onDismiss,
            title = title ?: "",
            description = description ?: "",
            sheetState = sheetState,
            nfcReadingState = state.sheetState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun RechargeScreenContentPreview() {
    CleventTheme {
        RechargeScreenContent(
            fieldValue = "0",
            onFieldValueChange = { },
            onConfirmOrderButtonTapped = { },
            state = RechargeViewModel.RechargeUiState(),
        )
    }
}
